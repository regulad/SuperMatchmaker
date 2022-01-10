package xyz.regulad.supermatchmaker.velocity.api;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.regulad.supermatchmaker.api.MatchmakerAPI;
import xyz.regulad.supermatchmaker.velocity.MatchmakerVelocity;

import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class VelocityAPI implements MatchmakerAPI {
    private final @NotNull MatchmakerVelocity matchmakerVelocity;

    public VelocityAPI(final @NotNull MatchmakerVelocity matchmakerVelocity) {
        this.matchmakerVelocity = matchmakerVelocity;
    }

    @Override
    public @Nullable CompletableFuture<@NotNull String> sendToGame(@NotNull UUID player, @NotNull String gameName) {
        return null;
    }

    @Override
    public @Nullable CompletableFuture<@NotNull String> getGame(@NotNull UUID player) {
        return null;
    }

    @Override
    public @Nullable CompletableFuture<@NotNull Integer> getGamePlayerCount(@NotNull String gameName) {
        return null;
    }

    @Override
    public @Nullable CompletableFuture<@NotNull Collection<@NotNull String>> getGames() {
        return null;
    }
}
