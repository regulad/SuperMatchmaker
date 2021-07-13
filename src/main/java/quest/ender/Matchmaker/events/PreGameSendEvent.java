package quest.ender.Matchmaker.events;

import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Cancellable;

public class PreGameSendEvent extends GameSendEvent implements Cancellable {
    private final ProxiedPlayer targetPlayer;
    private final String targetGame;
    private boolean isCancelled = false;

    public PreGameSendEvent(ProxiedPlayer targetPlayer, String targetGame) {
        this.targetPlayer = targetPlayer;
        this.targetGame = targetGame;
    }

    public String getTargetGame() {
        return this.targetGame;
    }

    public ProxiedPlayer getTargetPlayer() {
        return this.targetPlayer;
    }

    @Override
    public boolean isCancelled() {
        return this.isCancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.isCancelled = cancel;
    }
}
