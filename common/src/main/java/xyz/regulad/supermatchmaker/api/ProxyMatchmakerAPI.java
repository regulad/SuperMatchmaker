package xyz.regulad.supermatchmaker.api;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public interface ProxyMatchmakerAPI<P, S> extends MatchmakerAPI<P, S> {
    @Contract(pure = true)
    default @Nullable CompletableFuture<@Nullable S> getServer(final @NotNull String gameName, int proxiedPlayers) {
        return getServer(gameName, proxiedPlayers, null);
    }

    @Nullable CompletableFuture<@NotNull S> getServer(final @NotNull String gameName, int proxiedPlayers, final @Nullable P targetPlayer);

    @Nullable String getGameFromServer(final @NotNull S serverInfo);

    @NotNull Collection<@NotNull String> getGamesInstantly();

    @Override
    @Contract(pure = true)
    default @NotNull CompletableFuture<@NotNull Collection<@NotNull String>> getGames() {
        return CompletableFuture.completedFuture(getGamesInstantly());
    }

    @NotNull Collection<@NotNull S> getServers(final @NotNull String gameName);
}
