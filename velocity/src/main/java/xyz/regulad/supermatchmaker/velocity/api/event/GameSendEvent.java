package xyz.regulad.supermatchmaker.velocity.api.event;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.server.ServerInfo;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

@Data
@RequiredArgsConstructor
public final class GameSendEvent {
    final @NotNull Collection<@NotNull Player> sent;
    final @NotNull String gameName;
    final @NotNull RegisteredServer server;
    final @NotNull CompletableFuture<Boolean> connectionFuture;
}
