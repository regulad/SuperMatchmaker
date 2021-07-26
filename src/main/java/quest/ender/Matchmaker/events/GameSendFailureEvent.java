package quest.ender.Matchmaker.events;

import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GameSendFailureEvent extends GameSendEvent {
    private final @Nullable Throwable reason;
    private final @NotNull ProxiedPlayer targetPlayer;
    private final @NotNull String targetGame;

    public GameSendFailureEvent(@NotNull ProxiedPlayer targetPlayer, @NotNull String targetGame, @Nullable Throwable reason) {
        this.targetPlayer = targetPlayer;
        this.targetGame = targetGame;
        this.reason = reason;
    }

    public @Nullable Throwable getReason() {
        return this.reason;
    }

    public @NotNull ProxiedPlayer getTargetPlayer() {
        return this.targetPlayer;
    }

    public @NotNull String getTargetGame() {
        return this.targetGame;
    }
}
