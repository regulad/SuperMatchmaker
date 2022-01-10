package xyz.regulad.supermatchmaker.velocity.api;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.regulad.supermatchmaker.api.ProxyMatchmakerAPI;
import xyz.regulad.supermatchmaker.velocity.MatchmakerVelocity;

import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class VelocityAPI implements ProxyMatchmakerAPI<Player, RegisteredServer> {
    private final @NotNull MatchmakerVelocity matchmakerVelocity;

    public VelocityAPI(final @NotNull MatchmakerVelocity matchmakerVelocity) {
        this.matchmakerVelocity = matchmakerVelocity;
    }

    @Override
    public @Nullable CompletableFuture<@NotNull RegisteredServer> sendToGame(@NotNull Player player, @NotNull String gameName) {
        return null;
    }

    @Override
    public @Nullable CompletableFuture<@Nullable String> getGame(@NotNull Player player) {
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

    @Override
    public @Nullable CompletableFuture<RegisteredServer> getServer(@NotNull String gameName, int proxiedPlayers, @Nullable Player targetPlayer) {
        return null;
    }

    @Override
    public @Nullable String getGameFromServer(@NotNull RegisteredServer serverInfo) {
        return null;
    }

    @Override
    public @NotNull Collection<RegisteredServer> getServers(@NotNull String gameName) {
        return null;
    }
}
