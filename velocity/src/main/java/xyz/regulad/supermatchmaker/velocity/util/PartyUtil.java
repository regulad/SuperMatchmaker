package xyz.regulad.supermatchmaker.velocity.util;

import com.velocitypowered.api.proxy.Player;
import de.simonsator.partyandfriends.velocity.api.pafplayers.OnlinePAFPlayer;
import de.simonsator.partyandfriends.velocity.api.pafplayers.PAFPlayerManager;
import de.simonsator.partyandfriends.velocity.api.party.PlayerParty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

/**
 * A collection of utilities for {@link PlayerParty}ies. This is a port of the BungeeCord class.
 */
public final class PartyUtil {
    private PartyUtil() {
    }

    /**
     * Gets all players actively affiliated with a player, via a {@link Player}.
     *
     * @param player The target {@link Player}.
     * @return An {@link ArrayList} of players that are affiliated with the player. This includes the player.
     */
    public static @NotNull ArrayList<Player> getAffiliatedPlayers(final @NotNull Player player) {
        final @NotNull OnlinePAFPlayer onlinePAFPlayer = PAFPlayerManager.getInstance().getPlayer(player);
        final @Nullable PlayerParty playerParty = onlinePAFPlayer.getParty();
        final @NotNull ArrayList<Player> proxiedPlayers = new ArrayList<>();

        if (playerParty != null) {
            for (OnlinePAFPlayer partyMember : playerParty.getAllPlayers()) proxiedPlayers.add(partyMember.getPlayer());
        } else {
            proxiedPlayers.add(player);
        }

        return proxiedPlayers;
    }

    /**
     * Gets the leader of a {@link PlayerParty}.
     *
     * @param player The {@link Player} in question.
     * @return The {@link Player} that leads {@code player}'s party. If {@code player} is not in a party, return {@code player}.
     */
    public static @NotNull Player getLeader(final @NotNull Player player) {
        final @NotNull OnlinePAFPlayer onlinePAFPlayer = PAFPlayerManager.getInstance().getPlayer(player);
        final @Nullable PlayerParty playerParty = onlinePAFPlayer.getParty();

        if (playerParty != null) {
            return playerParty.getLeader().getPlayer();
        } else {
            return player;
        }
    }

    /**
     * Sees if a {@link Player} is the leader of a party.
     *
     * @param player The {@link Player} in question.
     * @return {@code true} if the player is not in a party or is the leader, else {@code false}.
     */
    public static boolean leadsParty(final @NotNull Player player) {
        final @NotNull OnlinePAFPlayer onlinePAFPlayer = PAFPlayerManager.getInstance().getPlayer(player);
        final @Nullable PlayerParty playerParty = onlinePAFPlayer.getParty();

        return playerParty == null || playerParty.isLeader(onlinePAFPlayer);
    }
}
