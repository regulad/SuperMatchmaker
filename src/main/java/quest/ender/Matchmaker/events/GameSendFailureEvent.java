package quest.ender.Matchmaker.events;

import net.md_5.bungee.api.connection.ProxiedPlayer;

public class GameSendFailureEvent extends GameSendEvent {
    private final Throwable reason;
    private final ProxiedPlayer targetPlayer;
    private final String targetGame;

    public GameSendFailureEvent(ProxiedPlayer targetPlayer, String targetGame, Throwable reason) {
        this.targetPlayer = targetPlayer;
        this.targetGame = targetGame;
        this.reason = reason;
    }

    public Throwable getReason() {
        return this.reason;
    }

    public ProxiedPlayer getTargetPlayer() {
        return this.targetPlayer;
    }

    public String getTargetGame() {
        return this.targetGame;
    }
}
