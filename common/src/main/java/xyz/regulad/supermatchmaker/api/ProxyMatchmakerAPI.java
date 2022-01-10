package xyz.regulad.supermatchmaker.api;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public interface ProxyMatchmakerAPI<P, S> extends MatchmakerAPI<P, S> {
    default @Nullable CompletableFuture<@Nullable S> getServer(final @NotNull String gameName, int proxiedPlayers) {
        return getServer(gameName, proxiedPlayers, null);
    }

    @Nullable CompletableFuture<S> getServer(final @NotNull String gameName, int proxiedPlayers, final @Nullable P targetPlayer);

    @Nullable String getGameFromServer(final @NotNull S serverInfo);

    @NotNull Collection<S> getServers(final @NotNull String gameName);
}
