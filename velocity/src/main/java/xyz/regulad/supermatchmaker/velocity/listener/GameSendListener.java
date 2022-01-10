package xyz.regulad.supermatchmaker.velocity.listener;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.velocitypowered.api.event.Subscribe;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.regulad.supermatchmaker.velocity.MatchmakerVelocity;
import xyz.regulad.supermatchmaker.velocity.api.event.GameSendEvent;
import xyz.regulad.supermatchmaker.velocity.api.event.PreGameSendEvent;
import xyz.regulad.supermatchmaker.velocity.util.PartyUtil;
import xyz.regulad.supermatchmaker.velocity.util.VelocityChannels;

import java.util.ArrayList;
import java.util.Objects;

/**
 * BungeeCord port
 */
@RequiredArgsConstructor
public final class GameSendListener {
    private final @NotNull MatchmakerVelocity matchmaker;

    @Subscribe
    public void checkForPermissions(final @NotNull PreGameSendEvent preGameSendEvent) {
        final @Nullable String baseGamePermission = Objects.requireNonNull(this.matchmaker.getConfig()).getNode("base_game_permission").getString();
        if (baseGamePermission != null
                && preGameSendEvent.getTargetPlayer() != null
                && !preGameSendEvent.getTargetPlayer().hasPermission(baseGamePermission + "." + preGameSendEvent.getTargetGame())) {
            preGameSendEvent.setResult(PreGameSendEvent.PreGameSendEventResult.CANNOT_CONNECT);
        }
        // Cancel the event if the config has a value for base_game_permission and the player does not have it.
    }

    @Subscribe
    public void checkIfLeader(final @NotNull PreGameSendEvent preGameSendEvent) {
        if (preGameSendEvent.getTargetPlayer() != null && !PartyUtil.leadsParty(preGameSendEvent.getTargetPlayer())) {
            preGameSendEvent.setResult(PreGameSendEvent.PreGameSendEventResult.CANNOT_CONNECT);
        }
        // Player is not the leader of the party, we can move on.
    }

    @Subscribe
    public void onGameSuccess(final @NotNull GameSendEvent gameSendEvent) {
        final @NotNull ArrayList<String> displayNameList = new ArrayList<>();
        gameSendEvent.getTargetServer().getPlayersConnected().forEach(player -> displayNameList.add(player.getUsername()));

        gameSendEvent.getConnectionFuture().thenAccept(result -> {
            final @NotNull ByteArrayDataOutput byteArrayDataOutput = ByteStreams.newDataOutput();

            byteArrayDataOutput.writeUTF("SentToGame");
            byteArrayDataOutput.writeUTF(String.join(", ", displayNameList));
            byteArrayDataOutput.writeUTF(gameSendEvent.getTargetGame());

            gameSendEvent.getTargetServer().sendPluginMessage(VelocityChannels.TO_BACKEND_CHANNEL, byteArrayDataOutput.toByteArray());
        });

        this.matchmaker.getLogger().info(String.join(", ", displayNameList) + " connected to " + gameSendEvent.getTargetServer().getServerInfo().getName() + " for " + gameSendEvent.getTargetGame());
    }

    @Subscribe
    public void onSendFailure(final @NotNull GameSendEvent gameSendEvent) {
        gameSendEvent.getConnectionFuture().exceptionally(throwable -> {
            this.matchmaker.getLogger().warning(gameSendEvent.getTargetPlayer().getUsername() + " couldn't connect to the game " + gameSendEvent.getTargetGame());
            throwable.printStackTrace();

            return false;
        });
    }
}
