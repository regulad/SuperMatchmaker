package xyz.regulad.supermatchmaker.common.api;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * An API for interacting with Matchmaker, either via a proxy implementation (Velocity or Waterfall) or a server implementation like PaperSpigot>=1.8.8.
 * Any methods of classes outside this API are subject to change. The API will stay the same between Major revisions.
 *
 * @param <P> The player type.
 * @param <S> The server type.
 */
public interface MatchmakerAPI<P, S> {
    static @Nullable MatchmakerAPI<?, ?> getInstance() {
        return InstanceHolder.getMatchmakerAPI();
    }

    static void setInstance(final @Nullable MatchmakerAPI<?, ?> matchmakerAPI) {
        InstanceHolder.setMatchmakerAPI(matchmakerAPI);
    }

    class InstanceHolder {
        @Getter
        @Setter
        private static @Nullable MatchmakerAPI<?, ?> matchmakerAPI;
    }

    /**
     * Sends a player or party to a game. Broadcasts events, and may be cancelled by them.
     *
     * @param player   The player to be sent to the game.
     * @param gameName The name of a game, in {@link String} form.
     */
    @Nullable CompletableFuture<@NotNull S> sendToGame(final @NotNull P player, final @NotNull String gameName);

    /**
     * Gets the game a player is in.
     *
     * @param player The player to get the game of.
     * @return The name of the game in a future.
     */
    @Nullable CompletableFuture<@Nullable String> getGame(final @NotNull P player);

    /**
     * Gets the amount of players current on servers hosting a game. On a server implementation, this may not be completed instantly, and this may be {@code null} if the server cannot a carrier to deliver the message.
     *
     * @param gameName The name of the game, in {@link String} form.
     * @return A future holding an {@link Integer} value of how many players are on servers hosting the gameName. May be 0 if the game does not exist or no servers are hosting it.
     */
    @Nullable CompletableFuture<@NotNull Integer> getGamePlayerCount(final @NotNull String gameName);

    /**
     * Gets all games present in the proxy's configuration. This may be {@code null} server implementation if it cannot find a carrier.
     *
     * @return A future containing {@link Collection} of games. Not all of these games may be valid, since some may not have servers attached.
     */
    @NotNull CompletableFuture<@NotNull List<@NotNull String>> getGames();
}
