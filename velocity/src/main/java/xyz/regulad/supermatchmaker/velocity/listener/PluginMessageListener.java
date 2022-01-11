package xyz.regulad.supermatchmaker.velocity.listener;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.velocitypowered.api.event.EventTask;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.regulad.supermatchmaker.velocity.MatchmakerVelocity;
import xyz.regulad.supermatchmaker.velocity.util.VelocityChannels;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * BungeeCord port
 */
@AllArgsConstructor
public final class PluginMessageListener {
    private final @NotNull MatchmakerVelocity matchmaker;

    @Subscribe
    public @Nullable EventTask onPluginMessage(final @NotNull PluginMessageEvent event) {
        if (event.getIdentifier().equals(VelocityChannels.TO_PROXY_CHANNEL)
                && event.getSource() instanceof final @NotNull ServerConnection serverConnection
                && event.getTarget() instanceof final @NotNull Player player) {
            event.setResult(PluginMessageEvent.ForwardResult.handled());

            final @NotNull ByteArrayDataInput inputStream = ByteStreams.newDataInput(event.getData());


            switch (inputStream.readUTF()) {
                case "GetGames" -> {
                    final @NotNull CompletableFuture<@NotNull List<@NotNull String>> future = this.matchmaker.getApi().getGames();
                    if (future != null) {
                        future.thenAccept(games -> {
                            final @NotNull ByteArrayDataOutput getGamesOutput = ByteStreams.newDataOutput();
                            getGamesOutput.writeUTF("GetGames");
                            getGamesOutput.writeUTF(String.join(", ", games));
                            serverConnection.sendPluginMessage(VelocityChannels.TO_BACKEND_CHANNEL, getGamesOutput.toByteArray());
                        });
                    }
                }
                case "SendToGame" -> {
                    final @NotNull String gameToSend = inputStream.readUTF();

                    final @Nullable CompletableFuture<@NotNull RegisteredServer> serverInfoCompletableFuture = this.matchmaker.getApi().sendToGame(player, gameToSend);
                    if (serverInfoCompletableFuture != null) {
                        serverInfoCompletableFuture.whenComplete((targetServerInfo, throwable) -> {
                            final @NotNull ByteArrayDataOutput sendGameOutput = ByteStreams.newDataOutput();
                            sendGameOutput.writeUTF("SendToGame");
                            sendGameOutput.writeUTF(player.getUsername());
                            sendGameOutput.writeUTF(throwable == null ? targetServerInfo.getServerInfo().getName() : "null");

                            serverConnection.sendPluginMessage(VelocityChannels.TO_BACKEND_CHANNEL, sendGameOutput.toByteArray());
                        });

                        serverInfoCompletableFuture.orTimeout(this.matchmaker.getConfig().getNode("timeout").getLong(750), TimeUnit.MILLISECONDS);
                        // A timeout of sorts.
                    } else {
                        return EventTask.async(() -> {
                            final @NotNull ByteArrayDataOutput sendGameOutput = ByteStreams.newDataOutput();
                            sendGameOutput.writeUTF("SendToGame");
                            sendGameOutput.writeUTF(player.getUsername());
                            sendGameOutput.writeUTF("null");

                            serverConnection.sendPluginMessage(VelocityChannels.TO_BACKEND_CHANNEL, sendGameOutput.toByteArray());
                        });
                    }
                }
                case "GetGame" -> {
                    final @Nullable CompletableFuture<@Nullable String> future = this.matchmaker.getApi().getGame(player);
                    if (future != null) {
                        future.thenAccept(game -> {
                            final @NotNull ByteArrayDataOutput getGameOutput = ByteStreams.newDataOutput();
                            getGameOutput.writeUTF("GetGame");
                            getGameOutput.writeUTF(game != null ? game : "null");
                            serverConnection.sendPluginMessage(VelocityChannels.TO_BACKEND_CHANNEL, getGameOutput.toByteArray());
                        });
                    }
                }
                case "GetGameStats" -> {
                    final @NotNull String gameForStats = inputStream.readUTF();
                    final @Nullable CompletableFuture<@NotNull Integer> future = this.matchmaker.getApi().getGamePlayerCount(gameForStats);
                    if (future != null) {
                        future.thenAccept(playerCount -> {
                            final @NotNull ByteArrayDataOutput gameStatsOutput = ByteStreams.newDataOutput();
                            gameStatsOutput.writeUTF("GetGameStats");
                            gameStatsOutput.writeUTF(gameForStats);
                            gameStatsOutput.writeUTF(String.valueOf(playerCount));
                            serverConnection.sendPluginMessage(VelocityChannels.TO_BACKEND_CHANNEL, gameStatsOutput.toByteArray());
                        });
                    }
                }
            }
        }
        return null;
    }
}
