package quest.ender.MatchmakerBukkit;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import quest.ender.MatchmakerBukkit.event.SentToGameEvent;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.concurrent.CompletableFuture;

/**
 * The Bukkit extension of Matchmaker is an API middleman.
 */
public class MatchmakerBukkit extends JavaPlugin implements PluginMessageListener {
    final @NotNull HashMap<@NotNull Player, @NotNull CompletableFuture<@NotNull String>> sendToGameFutures = new HashMap<>();
    final @NotNull HashMap<@NotNull String, @NotNull CompletableFuture<@NotNull String>> getGameStatsFutures = new HashMap<>();
    final @NotNull LinkedList<@NotNull CompletableFuture<@NotNull String>> getGameFutures = new LinkedList<>();
    final @NotNull LinkedList<@NotNull CompletableFuture<@NotNull String>> getGamesFutures = new LinkedList<>();

    @Override
    public void onEnable() {
        this.getServer().getMessenger().registerOutgoingPluginChannel(this, "matchmaker:out");
        this.getServer().getMessenger().registerIncomingPluginChannel(this, "matchmaker:in", this);

        if (this.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new quest.ender.MatchmakerBukkit.placeholder.MatchmakerExpansion(this).register();
        }
    }

    @Override
    public void onDisable() {
        this.getServer().getMessenger().unregisterOutgoingPluginChannel(this, "matchmaker:out");
        this.getServer().getMessenger().unregisterIncomingPluginChannel(this, "matchmaker:in", this);
    }

    @Override
    public void onPluginMessageReceived(@NotNull String channel, @NotNull Player player, byte[] message) {
        if (channel.equals("matchmaker:in")) {
            final @NotNull ByteArrayDataInput byteArrayDataInput = ByteStreams.newDataInput(message);
            switch (byteArrayDataInput.readUTF()) {
                case "SendToGame":
                    final @NotNull Player playerSent = this.getServer().getPlayer(byteArrayDataInput.readUTF());
                    final @Nullable CompletableFuture<@NotNull String> sendToGameCompletableFuture = this.sendToGameFutures.remove(playerSent);

                    if (sendToGameCompletableFuture != null) {
                        sendToGameCompletableFuture.complete(byteArrayDataInput.readUTF());
                    }

                    break;
                case "GetGameStats":
                    final @Nullable CompletableFuture<@NotNull String> getGameStatsCompletableFuture = this.getGameStatsFutures.remove(byteArrayDataInput.readUTF());

                    if (getGameStatsCompletableFuture != null) {
                        getGameStatsCompletableFuture.complete(byteArrayDataInput.readUTF());
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
                        getGameFuture.complete(byteArrayDataInput.readUTF());
                    }

                    break;
                case "GetGames":
                    @Nullable CompletableFuture<@NotNull String> getGamesFuture;
                    try {
                        getGamesFuture = this.getGamesFutures.pop();
                    } catch (NoSuchElementException noSuchElementException) {
                        getGamesFuture = null;
                    }

                    if (getGamesFuture != null) {
                        getGamesFuture.complete(byteArrayDataInput.readUTF());
                    }

                    break;
                case "SentToGame":
                    this.getServer().getPluginManager().callEvent(new SentToGameEvent(byteArrayDataInput.readUTF()));

                    break;
            }
        }
    }

    public @NotNull CompletableFuture<@NotNull String> sendToGame(@NotNull Player player, @NotNull String gameName) {
        final @NotNull CompletableFuture<@NotNull String> completableFuture = new CompletableFuture<>();

        this.sendToGameFutures.put(player, completableFuture);

        final @NotNull ByteArrayDataOutput byteArrayDataOutput = ByteStreams.newDataOutput();

        byteArrayDataOutput.writeUTF("SendToGame");
        byteArrayDataOutput.writeUTF(gameName);

        player.sendPluginMessage(this, "matchmaker:out", byteArrayDataOutput.toByteArray());

        return completableFuture;
    }

    public @NotNull CompletableFuture<@NotNull String> getGameStats(@NotNull Player player, @NotNull String gameName) {
        final @NotNull CompletableFuture<@NotNull String> completableFuture = new CompletableFuture<>();

        this.getGameStatsFutures.put(gameName, completableFuture);

        final @NotNull ByteArrayDataOutput byteArrayDataOutput = ByteStreams.newDataOutput();

        byteArrayDataOutput.writeUTF("GetGameStats");
        byteArrayDataOutput.writeUTF(gameName);

        player.sendPluginMessage(this, "matchmaker:out", byteArrayDataOutput.toByteArray());

        return completableFuture;
    }

    public @NotNull CompletableFuture<@NotNull String> getGame(@NotNull Player player) {
        final @NotNull CompletableFuture<@NotNull String> completableFuture = new CompletableFuture<>();

        this.getGameFutures.push(completableFuture);

        final @NotNull ByteArrayDataOutput byteArrayDataOutput = ByteStreams.newDataOutput();

        byteArrayDataOutput.writeUTF("GetGame");

        player.sendPluginMessage(this, "matchmaker:out", byteArrayDataOutput.toByteArray());

        return completableFuture;
    }

    public @NotNull CompletableFuture<@NotNull String> getGames(@NotNull Player player) {
        final @NotNull CompletableFuture<@NotNull String> completableFuture = new CompletableFuture<>();

        this.getGamesFutures.push(completableFuture);

        final @NotNull ByteArrayDataOutput byteArrayDataOutput = ByteStreams.newDataOutput();

        byteArrayDataOutput.writeUTF("GetGames");

        player.sendPluginMessage(this, "matchmaker:out", byteArrayDataOutput.toByteArray());

        return completableFuture;
    }
}
