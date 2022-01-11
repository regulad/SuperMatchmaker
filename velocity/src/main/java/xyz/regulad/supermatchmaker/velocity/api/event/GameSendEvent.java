package xyz.regulad.supermatchmaker.velocity.api.event;

import com.velocitypowered.api.proxy.ConnectionRequestBuilder;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

@Data
@RequiredArgsConstructor
public final class GameSendEvent {
    final @NotNull Collection<@NotNull Player> movedPlayers;
    final @Nullable Player targetPlayer;
    final @NotNull String targetGame;
    final @NotNull RegisteredServer targetServer;
    final @NotNull CompletableFuture<ConnectionRequestBuilder.@NotNull Result> connectionFuture;
}
