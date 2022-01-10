package xyz.regulad.supermatchmaker.velocity.api.event;

import com.velocitypowered.api.event.ResultedEvent;
import com.velocitypowered.api.proxy.Player;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

@Data
@RequiredArgsConstructor
public final class PreGameSendEvent implements ResultedEvent<PreGameSendEvent.PreGameSendEventResult> {
    final @NotNull Collection<@NotNull Player> toBeSent;
    final @Nullable Player targetPlayer;
    final @NotNull String targetGame;

    @NotNull PreGameSendEventResult result = PreGameSendEventResult.CAN_CONNECT;

    @Data
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class PreGameSendEventResult implements Result {
        final boolean allowed;

        public static @NotNull PreGameSendEventResult CAN_CONNECT = new PreGameSendEventResult(true);
        public static @NotNull PreGameSendEventResult CANNOT_CONNECT = new PreGameSendEventResult(false);
    }
}
