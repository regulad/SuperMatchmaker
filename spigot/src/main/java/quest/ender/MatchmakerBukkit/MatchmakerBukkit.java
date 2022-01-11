package quest.ender.MatchmakerBukkit;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import lombok.Getter;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bstats.bukkit.Metrics;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import quest.ender.MatchmakerBukkit.command.LocalMatchCommand;
import quest.ender.MatchmakerBukkit.event.SentToGameEvent;
import quest.ender.MatchmakerBukkit.listener.SentToGameListener;
import xyz.regulad.supermatchmaker.common.api.MatchmakerAPI;
import xyz.regulad.supermatchmaker.common.util.Channels;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The Bukkit extension of Matchmaker is an API middleman.
 */
public final class MatchmakerBukkit extends JavaPlugin implements PluginMessageListener, MatchmakerAPI<Player, String> {
    private final @NotNull Metrics metrics = new Metrics(this, 13907);
    private final @NotNull ConcurrentHashMap<@NotNull Player, @NotNull CompletableFuture<@NotNull String>> sendToGameFutures = new ConcurrentHashMap<>(64);
    private final @NotNull ConcurrentHashMap<@NotNull String, @NotNull CompletableFuture<@NotNull Integer>> getGameStatsFutures = new ConcurrentHashMap<>(64);
    private final @NotNull LinkedList<@NotNull CompletableFuture<@NotNull String>> getGameFutures = new LinkedList<>();
    private final LinkedList<CompletableFuture<@NotNull List<@NotNull String>>> getGamesFutures = new LinkedList<CompletableFuture<@NotNull List<@NotNull String>>>();
    @Getter
    private @Nullable BukkitAudiences bukkitAudiences;

    @Override
    public void onEnable() {
        MatchmakerAPI.setInstance(this);

        this.getServer().getMessenger().registerOutgoingPluginChannel(this, Channels.TO_PROXY_CHANNEL);
        this.getServer().getMessenger().registerIncomingPluginChannel(this, Channels.TO_BACKEND_CHANNEL, this);

        this.getServer().getPluginManager().registerEvents(new SentToGameListener(this), this);

        if (this.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new quest.ender.MatchmakerBukkit.placeholder.MatchmakerExpansion(this).register();
        }

        final @NotNull PluginCommand localmatchCommand = this.getCommand("localmatch");
        final @NotNull LocalMatchCommand localMatchCommandExecutor = new LocalMatchCommand(this);
        localmatchCommand.setExecutor(localMatchCommandExecutor);
        localmatchCommand.setTabCompleter(localMatchCommandExecutor);

        this.bukkitAudiences = BukkitAudiences.create(this);
    }

    @Override
    public void onDisable() {
        MatchmakerAPI.setInstance(null);

        this.getServer().getMessenger().unregisterOutgoingPluginChannel(this, Channels.TO_PROXY_CHANNEL);
        this.getServer().getMessenger().unregisterIncomingPluginChannel(this, Channels.TO_BACKEND_CHANNEL, this);

        this.bukkitAudiences = null;
    }

    @Override
    public void onPluginMessageReceived(@NotNull String channel, @NotNull Player player, byte[] message) {
        if (channel.equals(Channels.TO_BACKEND_CHANNEL)) {
            final @NotNull ByteArrayDataInput byteArrayDataInput = ByteStreams.newDataInput(message);
            switch (byteArrayDataInput.readUTF()) {
                case "SendToGame":
                    final @Nullable Player playerSent = this.getServer().getPlayer(byteArrayDataInput.readUTF());
                    final @Nullable CompletableFuture<@NotNull String> sendToGameCompletableFuture = playerSent != null ? this.sendToGameFutures.remove(playerSent) : null;
                    if (sendToGameCompletableFuture != null) {
                        sendToGameCompletableFuture.complete(byteArrayDataInput.readUTF());
                    }
                    break;
                case "GetGameStats":
                    final @Nullable CompletableFuture<@NotNull Integer> getGameStatsCompletableFuture = this.getGameStatsFutures.remove(byteArrayDataInput.readUTF());
                    if (getGameStatsCompletableFuture != null) {
                        getGameStatsCompletableFuture.complete(Integer.parseInt(byteArrayDataInput.readUTF()));
                    }
                    break;
                case "GetGame":
                    @Nullable CompletableFuture<@NotNull String> getGameFuture;
                    try {
                        getGameFuture = this.getGameFutures.pop();
                    } catch (NoSuchElementException noSuchElementException) {
                        getGameFuture = null;
                    }
                    if (getGameFuture != null) {
                        final @NotNull String result = byteArrayDataInput.readUTF();
                        getGameFuture.complete(!result.equals("null") ? result : null);
                    }
                    break;
                case "GetGames":
                    CompletableFuture<@NotNull List<@NotNull String>> getGamesFuture;
                    try {
                        getGamesFuture = this.getGamesFutures.pop();
                    } catch (NoSuchElementException noSuchElementException) {
                        getGamesFuture = null;
                    }
                    if (getGamesFuture != null) {
                        getGamesFuture.complete(Arrays.asList(byteArrayDataInput.readUTF().split(", ")));
                    }
                    break;
                case "SentToGame":
                    final @NotNull ArrayList<@NotNull Player> playerArrayList = new ArrayList<>();
                    final @NotNull String[] playerNames = byteArrayDataInput.readUTF().split(", ");

                    for (final @NotNull String playerName : playerNames) {
                        final @Nullable Player realPlayer = this.getServer().getPlayer(playerName);
                        if (realPlayer != null)
                            playerArrayList.add(realPlayer);
                    }

                    this.getServer().getPluginManager().callEvent(new SentToGameEvent(playerArrayList, byteArrayDataInput.readUTF()));
                    break;
            }
        }
    }

    @Override
    public @NotNull CompletableFuture<@NotNull String> sendToGame(final @NotNull Player player, final @NotNull String gameName) {
        final @NotNull CompletableFuture<@NotNull String> completableFuture = new CompletableFuture<>();

        this.sendToGameFutures.put(player, completableFuture);

        final @NotNull ByteArrayDataOutput byteArrayDataOutput = ByteStreams.newDataOutput();

        byteArrayDataOutput.writeUTF("SendToGame");
        byteArrayDataOutput.writeUTF(gameName);

        player.sendPluginMessage(this, Channels.TO_PROXY_CHANNEL, byteArrayDataOutput.toByteArray());

        return completableFuture;
    }

    @Override
    public @Nullable CompletableFuture<@NotNull Integer> getGamePlayerCount(@NotNull String gameName) {
        final @Nullable Player carrier = this.getServer().getOnlinePlayers().stream().findFirst().orElse(null);
        return carrier != null ? this.getGamePlayerCount(carrier, gameName) : null;
    }

    public @NotNull CompletableFuture<@NotNull Integer> getGamePlayerCount(final @NotNull Player player, final @NotNull String gameName) {
        final @NotNull CompletableFuture<@NotNull Integer> completableFuture = new CompletableFuture<>();

        this.getGameStatsFutures.put(gameName, completableFuture);

        final @NotNull ByteArrayDataOutput byteArrayDataOutput = ByteStreams.newDataOutput();

        byteArrayDataOutput.writeUTF("GetGameStats");
        byteArrayDataOutput.writeUTF(gameName);

        player.sendPluginMessage(this, Channels.TO_PROXY_CHANNEL, byteArrayDataOutput.toByteArray());

        return completableFuture;
    }

    @Override
    public @NotNull CompletableFuture<@Nullable String> getGame(final @NotNull Player player) {
        final @NotNull CompletableFuture<@Nullable String> completableFuture = new CompletableFuture<>();

        this.getGameFutures.push(completableFuture);

        final @NotNull ByteArrayDataOutput byteArrayDataOutput = ByteStreams.newDataOutput();

        byteArrayDataOutput.writeUTF("GetGame");

        player.sendPluginMessage(this, Channels.TO_PROXY_CHANNEL, byteArrayDataOutput.toByteArray());

        return completableFuture;
    }

    @Override
    public @NotNull CompletableFuture<@NotNull List<@NotNull String>> getGames() {
        final @Nullable Player carrier = this.getServer().getOnlinePlayers().stream().findFirst().orElse(null);
        return carrier != null ? this.getGames(carrier) : null;
    }

    public @NotNull CompletableFuture<@NotNull List<@NotNull String>> getGames(final @NotNull Player player) {
        final @NotNull CompletableFuture<@NotNull List<@NotNull String>> completableFuture = new CompletableFuture<>();

        this.getGamesFutures.push(completableFuture);

        final @NotNull ByteArrayDataOutput byteArrayDataOutput = ByteStreams.newDataOutput();

        byteArrayDataOutput.writeUTF("GetGames");

        player.sendPluginMessage(this, Channels.TO_PROXY_CHANNEL, byteArrayDataOutput.toByteArray());

        return completableFuture;
    }
}
