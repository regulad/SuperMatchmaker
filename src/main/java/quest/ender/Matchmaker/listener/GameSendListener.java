package quest.ender.Matchmaker.listener;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import quest.ender.Matchmaker.Matchmaker;
import quest.ender.Matchmaker.events.GameSendFailureEvent;
import quest.ender.Matchmaker.events.GameSendSuccessEvent;
import quest.ender.Matchmaker.events.PreGameSendEvent;
import quest.ender.Matchmaker.util.PartyUtil;

import java.util.ArrayList;

public class GameSendListener implements Listener {
    private final @NotNull Matchmaker matchmaker;

    public GameSendListener(final @NotNull Matchmaker matchmaker) {
        this.matchmaker = matchmaker;
    }

    @EventHandler
    public void checkForPermissions(final @NotNull PreGameSendEvent preGameSendEvent) {
        final @NotNull String baseGamePermission = this.matchmaker.getConfig().getString("base_game_permission");
        if (baseGamePermission.length() > 0 && !preGameSendEvent.getTargetPlayer().hasPermission(baseGamePermission + "." + preGameSendEvent.getTargetGame()))
            preGameSendEvent.setCancelled(true);
        // Cancel the event if the config has a value for base_game_permission and the player does not have it.
    }

    @EventHandler
    public void checkIfLeader(final @NotNull PreGameSendEvent preGameSendEvent) {
        if (!PartyUtil.leadsParty(preGameSendEvent.getTargetPlayer()))
            preGameSendEvent.setCancelled(true); // Player is not the leader of the party, we can move on.
    }

    @EventHandler
    public void onGameSuccess(final @NotNull GameSendSuccessEvent gameSendSuccessEvent) {
        final @NotNull ArrayList<String> displayNameList = new ArrayList<>();
        for (ProxiedPlayer proxiedPlayer : gameSendSuccessEvent.getMovedPlayers()) {
            displayNameList.add(proxiedPlayer.getDisplayName());
        }

        gameSendSuccessEvent.getConnectionFuture().thenApply(result -> {
            final @NotNull ByteArrayDataOutput byteArrayDataOutput = ByteStreams.newDataOutput();

            byteArrayDataOutput.writeUTF("SentToGame");
            byteArrayDataOutput.writeUTF(String.join(", ", displayNameList));
            byteArrayDataOutput.writeUTF(gameSendSuccessEvent.getTargetGame());

            gameSendSuccessEvent.getTargetServer().sendData("matchmaker:in", byteArrayDataOutput.toByteArray());

            return result;
        });

        this.matchmaker.getLogger().info(String.join(", ", displayNameList) + " connected to " + gameSendSuccessEvent.getTargetServer().getName() + " for " + this.matchmaker.getGame(gameSendSuccessEvent.getTargetServer()));
    }

    @EventHandler
    public void onSendFailure(final @NotNull GameSendFailureEvent gameSendFailureEvent) {
        final @Nullable Throwable throwableReason = gameSendFailureEvent.getReason();
        if (throwableReason != null)
            this.matchmaker.getLogger().warning(gameSendFailureEvent.getTargetPlayer().getDisplayName() + " couldn't connect to the game " + gameSendFailureEvent.getTargetGame() + " because of " + gameSendFailureEvent.getReason().getClass().getName() + ".");
        else
            this.matchmaker.getLogger().warning(gameSendFailureEvent.getTargetPlayer().getDisplayName() + " couldn't connect to the game " + gameSendFailureEvent.getTargetGame() + ".");
        gameSendFailureEvent.getReason().printStackTrace();
    }
}
