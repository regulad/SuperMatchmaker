package quest.ender.Matchmaker.util;

import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ServerConnectEvent;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public final class BungeePlayerUtil {
    private BungeePlayerUtil() {
    }

    @Contract(pure = true)
    public static @NotNull CompletableFuture<Boolean> sendPlayerFuture(final @NotNull ProxiedPlayer proxiedPlayer, final @NotNull ServerInfo targetServer, final @Nullable ServerConnectEvent.Reason reason) {
        final @NotNull CompletableFuture<Boolean> completeFuture = new CompletableFuture<>();
        if (reason != null)
            proxiedPlayer.connect(targetServer, (result, throwable) -> {
                if (result != null || throwable == null)
                    completeFuture.complete(result);
                else
                    completeFuture.completeExceptionally(throwable);
            }, reason);
        else
            proxiedPlayer.connect(targetServer, (result, throwable) -> {
                if (result != null || throwable == null)
                    completeFuture.complete(result);
                else
                    completeFuture.completeExceptionally(throwable);
            });
        return completeFuture;
    }

    @Contract(pure = true)
    public static @NotNull CompletableFuture<Boolean> sendPlayerFuture(final @NotNull ProxiedPlayer proxiedPlayer, final @NotNull ServerInfo targetServer) {
        return sendPlayerFuture(proxiedPlayer, targetServer, null);
    }
}
