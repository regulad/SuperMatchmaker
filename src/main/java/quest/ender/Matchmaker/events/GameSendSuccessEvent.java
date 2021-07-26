package quest.ender.Matchmaker.events;

import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class GameSendSuccessEvent extends GameSendEvent {
    private final @NotNull List<ProxiedPlayer> movedPlayers;
    private final @NotNull ServerInfo targetServer;
    private final @NotNull String targetGame;
    private final @NotNull CompletableFuture<Boolean> connectionFuture;

    public GameSendSuccessEvent(final @NotNull List<ProxiedPlayer> movedPlayers, final @NotNull ServerInfo targetServer, final @NotNull String targetGame, final @NotNull CompletableFuture<Boolean> connectionFuture) {
        this.movedPlayers = movedPlayers;
        this.targetServer = targetServer;
        this.targetGame = targetGame;
        this.connectionFuture = connectionFuture;
    }

    public @NotNull List<ProxiedPlayer> getMovedPlayers() {
        return this.movedPlayers;
    }

    public @NotNull ServerInfo getTargetServer() {
        return this.targetServer;
    }

    public @NotNull String getTargetGame() {
        return this.targetGame;
    }

    public @NotNull CompletableFuture<Boolean> getConnectionFuture() {
        return this.connectionFuture;
    }
}
