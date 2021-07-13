package quest.ender.Matchmaker;

import com.google.common.collect.Iterables;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.PluginManager;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import quest.ender.Matchmaker.command.LobbyCommand;
import quest.ender.Matchmaker.command.MakeMatchCommand;
import quest.ender.Matchmaker.events.PreGameSendEvent;
import quest.ender.Matchmaker.listener.ConnectionListener;
import quest.ender.Matchmaker.listener.GameSendListener;
import quest.ender.Matchmaker.listener.PluginMessageListener;
import quest.ender.Matchmaker.util.PartyUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class Matchmaker extends Plugin {
    private @Nullable Configuration configuration = null;

    @Override
    public void onEnable() {
        if (this.getConfig() == null) this.saveDefaultConfig();

        if (this.getConfig() == null) this.getLogger().warning("Couldn't load configuration!");

        final @NotNull ProxyServer proxyServer = this.getProxy();

        proxyServer.registerChannel("matchmaker:in");
        proxyServer.registerChannel("matchmaker:out");

        final @NotNull PluginManager pluginManager = proxyServer.getPluginManager();

        pluginManager.registerCommand(this, new MakeMatchCommand(this, this.getConfig().getString("commands.makematch.name"), this.getConfig().getString("commands.makematch.permission"), Iterables.toArray(this.getConfig().getStringList("commands.makematch.aliases"), String.class)));
        pluginManager.registerCommand(this, new LobbyCommand(this, this.getConfig().getString("commands.lobby.name"), this.getConfig().getString("commands.lobby.permission"), Iterables.toArray(this.getConfig().getStringList("commands.lobby.aliases"), String.class)));

        pluginManager.registerListener(this, new GameSendListener(this));
        pluginManager.registerListener(this, new PluginMessageListener(this));
        pluginManager.registerListener(this, new ConnectionListener(this));

        this.getLogger().info("All done loading.");
    }

    @Override
    public void onDisable() {
        final @NotNull ProxyServer proxyServer = this.getProxy();

        proxyServer.unregisterChannel("matchmaker:in");
        proxyServer.unregisterChannel("matchmaker:out");

        final @NotNull PluginManager pluginManager = proxyServer.getPluginManager();
        pluginManager.unregisterCommands(this);
        pluginManager.unregisterListeners(this);

        this.getLogger().info("Bye!");
    }

    public void saveDefaultConfig() {
        if (!this.getDataFolder().exists()) this.getDataFolder().mkdir();

        final @NotNull File file = new File(this.getDataFolder(), "config.yml");

        if (!file.exists()) {
            try (InputStream in = this.getResourceAsStream("config.yml")) {
                Files.copy(in, file.toPath());
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }
    }

    /**
     * Reloads the plugin's {@link Configuration}. Intended for other plugins that add {@link ServerInfo}s to games.
     */
    public void reloadConfig() {
        try {
            final @NotNull ConfigurationProvider configurationProvider = ConfigurationProvider.getProvider(YamlConfiguration.class);
            // Technically Nullable, but the server would have already crashed if that was the case.

            final @NotNull Configuration defaultConfiguration;
            try (InputStream defaultConfig = this.getResourceAsStream("config.yml")) {
                defaultConfiguration = configurationProvider.load(defaultConfig); // Will this break? It is barely documented.
            }

            this.configuration = configurationProvider.load(new File(this.getDataFolder(), "config.yml"), defaultConfiguration);
        } catch (IOException exception) {
            this.configuration = null;
        }
    }

    /**
     * Provides the plugin's {@link Configuration}, reloading it if it has not yet been loaded.
     *
     * @return The loaded {@link Configuration}, which may be null if an exception occurs.
     */
    public @Nullable Configuration getConfig() {
        if (this.configuration == null) this.reloadConfig();
        return this.configuration;
    }

    /**
     * Gets all games present in the plugin's {@link Configuration}.
     *
     * @return A {@link Collection} of games. Not all of these games may be valid, since some may not have servers attached.
     */
    public @NotNull Collection<String> getGames() {
        return this.getConfig().getSection("games").getKeys();
    }

    /**
     * Gets the servers part of a given game.
     *
     * @param gameName The game that will be searched for servers.
     * @return An {@link ArrayList} of servers attached to the game. If a game is not real or has no servers, it will be an empty list.
     */
    public @NotNull ArrayList<ServerInfo> getServers(String gameName) {
        final @NotNull List<String> serverList = this.getConfig().getStringList("games." + gameName);
        final @NotNull ArrayList<ServerInfo> validServers = new ArrayList<>();

        for (String serverName : serverList) {
            ServerInfo serverInfo = this.getProxy().getServerInfo(serverName);
            if (serverInfo != null) validServers.add(serverInfo);
        }

        return validServers;
    }

    /**
     * Gets the game a {@link ServerInfo} is affiliated with.
     *
     * @param serverInfo A {@link ServerInfo} that may or may not be part of a game.
     * @return A game, in {@link String} form. May be null if the serverInfo is not part of a game.
     */
    public @Nullable String getGame(ServerInfo serverInfo) {
        for (String game : this.getGames()) {
            for (ServerInfo compare : this.getServers(game)) {
                if (compare.equals(serverInfo)) return game;
            }
        }
        return null;
    }

    /**
     * Gets the amount of players current on servers hosting a game.
     *
     * @param gameName The name of the game, in {@link String} form.
     * @return An {@link Integer} value of how many players are on servers hosting the gameName. May be 0 if the game does not exist or no servers are hosting it.
     */
    public int getGamePlayerCount(String gameName) {
        int playerCount = 0;

        for (ServerInfo serverInfo : this.getServers(gameName)) {
            playerCount += serverInfo.getPlayers().size(); // May be inaccurate, but way more efficient.
        }

        return playerCount;
    }

    /**
     * Get a {@link ServerInfo} capable of receiving the proxiedPlayers. This may take as long as a second, since it has to ping servers.
     *
     * @param gameName       The name of the game, in {@link String} form.
     * @param proxiedPlayers A {@link List} of players that must be accommodated.
     * @return A future that returns a {@link ServerInfo} that must accommodate players. If no servers are found, the future will not complete. This is only valid in the instant that is received, since the state of the server may change. (i.e. a player joining, putting the server over it's limit)
     */
    public @NotNull CompletableFuture<ServerInfo> getServer(String gameName, List<ProxiedPlayer> proxiedPlayers) {
        final @NotNull ArrayList<ServerInfo> serverList = this.getServers(gameName);

        final @NotNull CompletableFuture<ServerInfo> serverPingCompletableFuture = new CompletableFuture<>();
        for (ServerInfo serverInfo : serverList) {
            serverInfo.ping((ServerPing serverPing, Throwable throwable) -> {
                if (serverPing != null && throwable == null) { // This is so if one server causes an issue, the proxy will continue pinging.
                    final @NotNull ServerPing.Players players = serverPing.getPlayers();
                    if (players.getMax() - players.getOnline() >= proxiedPlayers.size()) {
                        serverPingCompletableFuture.complete(serverInfo); // The first server to respond that can accept players will get selected.
                    }
                }
            });
        }

        return serverPingCompletableFuture;
    }

    /**
     * Sends a player or party to a game. Broadcasts events, and may be cancelled by them.
     *
     * @param player   The {@link ProxiedPlayer} to be sent to the game.
     * @param gameName The name of a game, in {@link String} form.
     */
    public @Nullable CompletableFuture<ServerInfo> sendToGame(ProxiedPlayer player, String gameName) {
        final @NotNull PreGameSendEvent preGameSendEvent = new PreGameSendEvent(player, gameName);
        this.getProxy().getPluginManager().callEvent(preGameSendEvent);
        if (!preGameSendEvent.isCancelled()) {
            final @NotNull ArrayList<ProxiedPlayer> players = PartyUtil.getAffiliatedPlayers(player);
            final @Nullable CompletableFuture<ServerInfo> targetServer = this.getServer(gameName, players);

            targetServer.thenApply(serverInfo -> {
                for (ProxiedPlayer proxiedPlayer : players) {
                    if (!serverInfo.getPlayers().contains(proxiedPlayer)) proxiedPlayer.connect(serverInfo);
                }

                return serverInfo;
            });

            return targetServer;
        }

        return null; // Event was cancelled.
    }
}
