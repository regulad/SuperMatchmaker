package xyz.regulad.supermatchmaker.api;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public interface ProxyMatchmakerAPI<PlayerType, ServerType> extends MatchmakerAPI<PlayerType, ServerType> {
    @Contract(pure = true)
    default @Nullable CompletableFuture<@Nullable ServerType> getServer(final @NotNull String gameName, int proxiedPlayers) {
        return getServer(gameName, proxiedPlayers, null);
    }

    @Nullable CompletableFuture<ServerType> getServer(final @NotNull String gameName, int proxiedPlayers, final @Nullable PlayerType targetPlayer);

    @Nullable String getGameFromServer(final @NotNull ServerType serverInfo);

    @NotNull Collection<ServerType> getServers(final @NotNull String gameName);
}
