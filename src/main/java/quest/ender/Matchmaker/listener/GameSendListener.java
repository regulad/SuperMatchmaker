package quest.ender.Matchmaker.listener;

import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import quest.ender.Matchmaker.Matchmaker;
import quest.ender.Matchmaker.events.GameSendFailureEvent;
import quest.ender.Matchmaker.events.GameSendSuccessEvent;

import java.util.ArrayList;

public class GameSendListener implements Listener {
    private final Matchmaker matchmaker;

    public GameSendListener(Matchmaker matchmaker) {
        this.matchmaker = matchmaker;
    }

    @EventHandler
    public void onSendSuccess(GameSendSuccessEvent gameSendSuccessEvent) {
        ArrayList<String> displayNameList = new ArrayList<>();
        for (ProxiedPlayer proxiedPlayer : gameSendSuccessEvent.getMovedPlayers()) {
            displayNameList.add(proxiedPlayer.getDisplayName());
        }

        this.matchmaker.getLogger().info(String.join(", ", displayNameList) + " connected to " + gameSendSuccessEvent.getTargetServer().getName() + " for " + this.matchmaker.getGame(gameSendSuccessEvent.getTargetServer()));
    }

    @EventHandler
    public void onSendFailure(GameSendFailureEvent gameSendFailureEvent) {
        this.matchmaker.getLogger().warning(gameSendFailureEvent.getTargetPlayer().getDisplayName() + " couldn't connect to the game " + gameSendFailureEvent.getTargetGame() + " because of " + gameSendFailureEvent.getReason().getClass().getName());
        gameSendFailureEvent.getReason().printStackTrace();
    }
}
