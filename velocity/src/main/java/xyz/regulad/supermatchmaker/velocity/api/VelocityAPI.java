package xyz.regulad.supermatchmaker.velocity.api;

import com.google.common.reflect.TypeToken;
import com.velocitypowered.api.proxy.ConnectionRequestBuilder;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import lombok.RequiredArgsConstructor;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.regulad.supermatchmaker.api.ProxyMatchmakerAPI;
import xyz.regulad.supermatchmaker.velocity.MatchmakerVelocity;
import xyz.regulad.supermatchmaker.velocity.api.event.GameSendEvent;
import xyz.regulad.supermatchmaker.velocity.api.event.PreGameSendEvent;
import xyz.regulad.supermatchmaker.velocity.util.PartyUtil;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;

@RequiredArgsConstructor
public class VelocityAPI implements ProxyMatchmakerAPI<Player, RegisteredServer> {
    private final @NotNull MatchmakerVelocity matchmakerVelocity;

    // Consider adding this method to the API later down the line.
    public @NotNull Map<@NotNull String, @NotNull List<@NotNull RegisteredServer>> getGameMap() {
        final @NotNull Map<@NotNull String, @NotNull List<@NotNull RegisteredServer>> scratchMap = new HashMap<>(); // Determines maximum amount of games.
        Objects.requireNonNull(this.matchmakerVelocity.getConfig()).getNode("games").getChildrenMap().forEach((obj, node) -> {
            try {
                scratchMap.put(
                        (String) obj,
                        node.getList(TypeToken.of(String.class)).stream()
                                .map(this.matchmakerVelocity.getProxy()::getServer)
                                .filter(Optional::isPresent)
                                .map(Optional::get)
                                .toList()
                        );
            } catch (ObjectMappingException e) {
                e.printStackTrace(); // What?
            }
        });
        return scratchMap;
    }

    @Override
    public @Nullable CompletableFuture<@NotNull Collection<@NotNull String>> getGames() {
        return CompletableFuture.completedFuture(this.getGameMap().keySet());
    }

    @Override
    public @NotNull Collection<RegisteredServer> getServers(@NotNull String gameName) {
        return this.getGameMap().getOrDefault(gameName, List.of());
    }

    @Override
    public @Nullable String getGameFromServer(@NotNull RegisteredServer serverInfo) {
        final @NotNull AtomicReference<@Nullable String> game = new AtomicReference<>();
        this.getGameMap().forEach((gameName, serverList) -> {
            if (serverList.contains(serverInfo)) {
                game.set(gameName);
            }
        });
        return game.get();
    }

    @Override
    public @Nullable CompletableFuture<@Nullable String> getGame(@NotNull Player player) {
        return CompletableFuture.completedFuture(player.getCurrentServer().isPresent()
                ? this.getGameFromServer(player.getCurrentServer().get().getServer())
                : null);
    }

    @Override
    public @Nullable CompletableFuture<@NotNull Integer> getGamePlayerCount(@NotNull String gameName) {
        return CompletableFuture.supplyAsync(() -> {
            int playerCount = 0;
            for (final @NotNull RegisteredServer server : this.getServers(gameName)) {
                playerCount += server.getPlayersConnected().size();
            }
            return playerCount;
        });
    }

    @Override
    public @Nullable CompletableFuture<@NotNull RegisteredServer> sendToGame(@NotNull Player player, @NotNull String gameName) {
        final @NotNull CompletableFuture<@NotNull RegisteredServer> future = new CompletableFuture<>();
        final @NotNull List<@NotNull Player> partyPlayers = PartyUtil.getAffiliatedPlayers(player);
        final @Nullable CompletableFuture<@NotNull RegisteredServer> getServerFuture = this.getServer(gameName, partyPlayers.size(), player);
        if (getServerFuture != null) {
            getServerFuture.thenAccept(future::complete);
            this.matchmakerVelocity.getProxy().getEventManager().fire(new PreGameSendEvent(partyPlayers, player, gameName)).thenAccept(
                    allowed -> {
                        if (allowed.getResult().isAllowed()) {
                            getServerFuture.thenAccept(registeredServer -> {
                                partyPlayers.stream()
                                        .filter(Predicate.not(player::equals))
                                        .forEach(partyPlayer -> partyPlayer.createConnectionRequest(registeredServer).fireAndForget());
                                final @NotNull CompletableFuture<ConnectionRequestBuilder.@NotNull Result> connectionFuture = player.createConnectionRequest(registeredServer).connect(); // we have our own indication
                                this.matchmakerVelocity.getProxy().getEventManager().fireAndForget(new GameSendEvent(allowed.getToBeSent(), allowed.getTargetPlayer(), allowed.getTargetGame(), registeredServer, connectionFuture));
                            });
                        }
                    }
            );
        }
        return getServerFuture != null ? future : null;
    }

    @Override
    public @Nullable CompletableFuture<RegisteredServer> getServer(@NotNull String gameName, int proxiedPlayers, @Nullable Player targetPlayer) {
        return null;
    }
}
