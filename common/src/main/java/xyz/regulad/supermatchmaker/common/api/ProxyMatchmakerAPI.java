package xyz.regulad.supermatchmaker.common.api;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface ProxyMatchmakerAPI<P, S> extends MatchmakerAPI<P, S> {
    @Contract(pure = true)
    default @Nullable CompletableFuture<@Nullable S> getServer(final @NotNull String gameName, int proxiedPlayers) {
        return getServer(gameName, proxiedPlayers, null);
    }

    @Nullable CompletableFuture<@NotNull S> getServer(final @NotNull String gameName, int proxiedPlayers, final @Nullable P targetPlayer);

    @Nullable String getGameFromServer(final @NotNull S serverInfo);

    @NotNull List<@NotNull String> getGamesInstantly();

    @Override
    @Contract(pure = true)
    default @NotNull CompletableFuture<@NotNull List<@NotNull String>> getGames() {
        return CompletableFuture.completedFuture(getGamesInstantly());
    }

    @NotNull Collection<@NotNull S> getServers(final @NotNull String gameName);
}
