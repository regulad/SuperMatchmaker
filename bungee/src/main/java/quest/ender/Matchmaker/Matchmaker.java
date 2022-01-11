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
import org.bstats.bungeecord.Metrics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import quest.ender.Matchmaker.command.LobbyCommand;
import quest.ender.Matchmaker.command.MakeMatchCommand;
import quest.ender.Matchmaker.command.ReloadCommand;
import quest.ender.Matchmaker.events.GameSendFailureEvent;
import quest.ender.Matchmaker.events.GameSendSuccessEvent;
import quest.ender.Matchmaker.events.PreGameSendEvent;
import quest.ender.Matchmaker.handler.ForcedServerReconnectHandler;
import quest.ender.Matchmaker.listener.ConnectionListener;
import quest.ender.Matchmaker.listener.GameSendListener;
import quest.ender.Matchmaker.listener.PluginMessageListener;
import quest.ender.Matchmaker.util.BungeePlayerUtil;
import quest.ender.Matchmaker.util.PartyUtil;
import xyz.regulad.supermatchmaker.common.api.MatchmakerAPI;
import xyz.regulad.supermatchmaker.common.api.ProxyMatchmakerAPI;
import xyz.regulad.supermatchmaker.common.util.Channels;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class Matchmaker extends Plugin implements ProxyMatchmakerAPI<ProxiedPlayer, ServerInfo> {
    private @Nullable Metrics metrics = null;
    private @Nullable Configuration configuration = null;

    @Override
    public void onEnable() {
        MatchmakerAPI.setInstance(this);

        if (this.getConfig() == null) this.saveDefaultConfig();

        if (this.getConfig() == null) this.getLogger().warning("Couldn't load configuration!");

        this.getProxy().registerChannel(Channels.TO_BACKEND_CHANNEL);
        this.getProxy().registerChannel(Channels.TO_PROXY_CHANNEL);

        this.getProxy().getPluginManager().registerCommand(this, new MakeMatchCommand(this, this.getConfig().getString("commands.makematch.name"), this.getConfig().getString("commands.makematch.permission"), Iterables.toArray(this.getConfig().getStringList("commands.makematch.aliases"), String.class)));
        this.getProxy().getPluginManager().registerCommand(this, new LobbyCommand(this, this.getConfig().getString("commands.lobby.name"), this.getConfig().getString("commands.lobby.permission"), Iterables.toArray(this.getConfig().getStringList("commands.lobby.aliases"), String.class)));
        this.getProxy().getPluginManager().registerCommand(this, new ReloadCommand(this, this.getConfig().getString("commands.mmreload.name"), this.getConfig().getString("commands.mmreload.permission"), Iterables.toArray(this.getConfig().getStringList("commands.mmreload.aliases"), String.class)));

        this.getProxy().getPluginManager().registerListener(this, new GameSendListener(this));
        this.getProxy().getPluginManager().registerListener(this, new PluginMessageListener(this));
        this.getProxy().getPluginManager().registerListener(this, new ConnectionListener(this));

        final @Nullable ServerInfo forcedServer = this.getProxy().getServerInfo(this.getConfig().getString("login.holding"));

        if (forcedServer != null) this.getProxy().setReconnectHandler(new ForcedServerReconnectHandler(forcedServer));

        this.metrics = new Metrics(this, 13908);
    }

    @Override
    public void onDisable() {
        MatchmakerAPI.setInstance(null);

        final @NotNull ProxyServer proxyServer = this.getProxy();

        proxyServer.unregisterChannel(Channels.TO_PROXY_CHANNEL);
        proxyServer.unregisterChannel(Channels.TO_BACKEND_CHANNEL);

        final @NotNull PluginManager pluginManager = proxyServer.getPluginManager();
        pluginManager.unregisterCommands(this);
        pluginManager.unregisterListeners(this);

        this.metrics = null; // Should I just let it GC?
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
    public @NotNull List<@NotNull String> getGamesInstantly() {
        return this.getConfig().getSection("games").getKeys().stream().toList();
    }

    /**
     * Gets the servers part of a given game.
     *
     * @param gameName The game that will be searched for servers.
     * @return An {@link ArrayList} of servers attached to the game. If a game is not real or has no servers, it will be an empty list.
     */
    @Override
    public @NotNull List<ServerInfo> getServers(@NotNull String gameName) {
        final @NotNull List<String> serverList = this.getConfig().getStringList("games." + gameName);
        final @NotNull ArrayList<ServerInfo> validServers = new ArrayList<>();

        for (String serverName : serverList) {
            ServerInfo serverInfo = this.getProxy().getServerInfo(serverName);
            if (serverInfo != null) validServers.add(serverInfo);
        }

        return validServers;
    }

    @Override
    public @Nullable CompletableFuture<@Nullable String> getGame(@NotNull ProxiedPlayer player) {
        return CompletableFuture.completedFuture(this.getGameFromServer(player.getServer().getInfo()));
    }

    /**
     * Gets the game a {@link ServerInfo} is affiliated with.
     *
     * @param serverInfo A {@link ServerInfo} that may or may not be part of a game.
     * @return A game, in {@link String} form. May be null if the serverInfo is not part of a game.
     */
    @Override
    public @Nullable String getGameFromServer(@NotNull ServerInfo serverInfo) {
        for (String game : this.getGamesInstantly()) {
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
    @Override
    public @NotNull CompletableFuture<@NotNull Integer> getGamePlayerCount(@NotNull String gameName) {
        int playerCount = 0;

        for (ServerInfo serverInfo : this.getServers(gameName)) {
            playerCount += serverInfo.getPlayers().size(); // May be inaccurate, but way more efficient.
        }

        return CompletableFuture.completedFuture(playerCount);
    }

    public int getGamePlayerCountInstantly(final @NotNull String gameName) {
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
     * @param proxiedPlayers The number of players the server must accept.
     * @param targetPlayer   The player to test for in the server. Optional, may be null.
     * @return A future that returns a {@link ServerInfo} that must accommodate players. If no servers are found, the future will not complete. This is only valid in the instant that is received, since the state of the server may change. (i.e. a player joining, putting the server over it's limit)
     */
    @Override
    public @Nullable CompletableFuture<ServerInfo> getServer(final @NotNull String gameName, int proxiedPlayers, final @Nullable ProxiedPlayer targetPlayer) {
        if (!this.getGamesInstantly().contains(gameName)) return null;

        final @NotNull List<ServerInfo> serverList = this.getServers(gameName);

        final @NotNull CompletableFuture<ServerInfo> serverPingCompletableFuture = new CompletableFuture<>();
        for (final @NotNull ServerInfo serverInfo : serverList) {
            if (targetPlayer != null && serverInfo.getPlayers().contains(targetPlayer))
                continue; // The player is here, lets move on.

            serverInfo.ping((final @Nullable ServerPing serverPing, final @Nullable Throwable throwable) -> {
                if (serverPing != null && throwable == null) { // This is so if one server causes an issue, the proxy will continue pinging.
                    final @NotNull ServerPing.Players players = serverPing.getPlayers();
                    if (players.getMax() - players.getOnline() >= proxiedPlayers) {
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
    @Override
    public @Nullable CompletableFuture<ServerInfo> sendToGame(final @NotNull ProxiedPlayer player, final @NotNull String gameName) {
        final @NotNull PreGameSendEvent preGameSendEvent = new PreGameSendEvent(player, gameName);
        this.getProxy().getPluginManager().callEvent(preGameSendEvent);
        if (!preGameSendEvent.isCancelled()) {
            final @NotNull ArrayList<ProxiedPlayer> players = PartyUtil.getAffiliatedPlayers(player);
            final @Nullable CompletableFuture<ServerInfo> targetServer = this.getServer(gameName, players.size(), PartyUtil.getLeader(player));

            if (targetServer != null)
                targetServer.thenApply(serverInfo -> {
                    ;
                    this.getProxy().getPluginManager().callEvent(new GameSendSuccessEvent(players, serverInfo, gameName, BungeePlayerUtil.sendPlayerFuture(PartyUtil.getLeader(player), serverInfo)));

                    return serverInfo;
                });
            else
                this.getProxy().getPluginManager().callEvent(new GameSendFailureEvent(player, gameName, null));

            return targetServer;
        }

        return null; // Event was cancelled.
    }
}
