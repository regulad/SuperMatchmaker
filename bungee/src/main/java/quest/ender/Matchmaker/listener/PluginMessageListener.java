package quest.ender.Matchmaker.listener;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import quest.ender.Matchmaker.Matchmaker;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class PluginMessageListener implements Listener {
    private final @NotNull Matchmaker matchmaker;

    public PluginMessageListener(final @NotNull Matchmaker matchmaker) {
        this.matchmaker = matchmaker;
    }

    @EventHandler
    public void onPluginMessage(final @NotNull PluginMessageEvent event) {
        if (event.getTag().equalsIgnoreCase("matchmaker:out")) {
            final @NotNull ByteArrayDataInput inputStream = ByteStreams.newDataInput(event.getData());
            final @NotNull ProxiedPlayer proxiedPlayer = this.matchmaker.getProxy().getPlayer(event.getReceiver().toString());
            final @NotNull ServerInfo serverInfo = proxiedPlayer.getServer().getInfo();

            switch (inputStream.readUTF()) {
                case "GetGames" -> {
                    final @NotNull ByteArrayDataOutput getGamesOutput = ByteStreams.newDataOutput();
                    getGamesOutput.writeUTF("GetGames");
                    getGamesOutput.writeUTF(String.join(", ", this.matchmaker.getGames()));
                    serverInfo.sendData("matchmaker:in", getGamesOutput.toByteArray());
                }
                case "SendToGame" -> {
                    final @NotNull String gameToSend = inputStream.readUTF();
                    final @Nullable CompletableFuture<ServerInfo> serverInfoCompletableFuture = this.matchmaker.sendToGame(proxiedPlayer, gameToSend);
                    if (serverInfoCompletableFuture != null) {
                        serverInfoCompletableFuture.thenApply((targetServerInfo) -> {
                            final @NotNull ByteArrayDataOutput sendGameOutput = ByteStreams.newDataOutput();
                            sendGameOutput.writeUTF("SendToGame");
                            sendGameOutput.writeUTF(proxiedPlayer.getDisplayName());
                            sendGameOutput.writeUTF(targetServerInfo.getName());

                            serverInfo.sendData("matchmaker:in", sendGameOutput.toByteArray());

                            return targetServerInfo;
                        });

                        this.matchmaker.getProxy().getScheduler().schedule(this.matchmaker, () -> {
                            if (!serverInfoCompletableFuture.isDone()) {
                                serverInfoCompletableFuture.cancel(true);
                                final @NotNull ByteArrayDataOutput sendGameOutput = ByteStreams.newDataOutput();
                                sendGameOutput.writeUTF("SendToGame");
                                sendGameOutput.writeUTF(proxiedPlayer.getDisplayName());
                                sendGameOutput.writeUTF("null");

                                serverInfo.sendData("matchmaker:in", sendGameOutput.toByteArray());
                            }
                        }, this.matchmaker.getConfig().getLong("timeout"), TimeUnit.MILLISECONDS);
                        // A timeout of sorts.
                    } else {
                        final @NotNull ByteArrayDataOutput sendGameOutput = ByteStreams.newDataOutput();
                        sendGameOutput.writeUTF("SendToGame");
                        sendGameOutput.writeUTF(proxiedPlayer.getDisplayName());
                        sendGameOutput.writeUTF("null");

                        serverInfo.sendData("matchmaker:in", sendGameOutput.toByteArray());
                    }
                }
                case "GetGame" -> {
                    final @Nullable String currentGame = this.matchmaker.getGame(serverInfo);
                    final @Nullable ByteArrayDataOutput getGameOutput = ByteStreams.newDataOutput();
                    getGameOutput.writeUTF("GetGame");
                    getGameOutput.writeUTF(currentGame != null ? currentGame : "null");
                    serverInfo.sendData("matchmaker:in", getGameOutput.toByteArray());
                }
                case "GetGameStats" -> {
                    final @NotNull String gameForStats = inputStream.readUTF();
                    final @NotNull ByteArrayDataOutput gameStatsOutput = ByteStreams.newDataOutput();
                    gameStatsOutput.writeUTF("GetGameStats");
                    gameStatsOutput.writeUTF(gameForStats);
                    gameStatsOutput.writeUTF(String.valueOf(this.matchmaker.getGamePlayerCount(gameForStats)));
                    serverInfo.sendData("matchmaker:in", gameStatsOutput.toByteArray());
                }
            }
        }
    }
}
