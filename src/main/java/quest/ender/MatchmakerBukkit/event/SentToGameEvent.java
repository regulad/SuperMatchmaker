package quest.ender.MatchmakerBukkit.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class SentToGameEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final @NotNull String gameSentTo;
    private final @NotNull List<Player> playersSent;

    public SentToGameEvent(final @NotNull List<Player> playersSent, final @NotNull String gameSentTo) {
        this.gameSentTo = gameSentTo;
        this.playersSent = playersSent;
    }

    public static @NotNull HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    public @NotNull List<Player> getPlayersSent() {
        return this.playersSent;
    }

    public @NotNull String getGameSentTo() {
        return this.gameSentTo;
    }
}
