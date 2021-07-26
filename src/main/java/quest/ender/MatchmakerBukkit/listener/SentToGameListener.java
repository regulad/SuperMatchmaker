package quest.ender.MatchmakerBukkit.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import quest.ender.MatchmakerBukkit.MatchmakerBukkit;
import quest.ender.MatchmakerBukkit.event.SentToGameEvent;

public class SentToGameListener implements Listener {
    private final @NotNull MatchmakerBukkit matchmakerBukkit;

    public SentToGameListener(final @NotNull MatchmakerBukkit matchmakerBukkit) {
        this.matchmakerBukkit = matchmakerBukkit;
    }

    @EventHandler
    public final void onSentToGameEvent(final @NotNull SentToGameEvent sentToGameEvent) {
        if (sentToGameEvent.getPlayersSent().size() > 0) // On some servers, this event will get called before the player has actually joined. This is just a bit of protection against that.
            this.matchmakerBukkit.getGame(sentToGameEvent.getPlayersSent().get(0)).thenApply(gameName -> {
                if (!gameName.equalsIgnoreCase(sentToGameEvent.getGameSentTo()))
                    SentToGameListener.this.matchmakerBukkit.getLogger().warning("The server has received players for a game that the server is not a part of. This may indicate a misconfiguration on the proxy.");

                return gameName;
            });
    }
}
