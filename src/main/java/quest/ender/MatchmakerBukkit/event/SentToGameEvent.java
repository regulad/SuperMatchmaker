package quest.ender.MatchmakerBukkit.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class SentToGameEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final @NotNull String gameSentTo;

    public SentToGameEvent(@NotNull String gameSentTo) {
        this.gameSentTo = gameSentTo;
    }

    public static @NotNull HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    public @NotNull String getGameSentTo() {
        return this.gameSentTo;
    }
}
