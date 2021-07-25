package quest.ender.Matchmaker.util;

import de.simonsator.partyandfriends.api.pafplayers.OnlinePAFPlayer;
import de.simonsator.partyandfriends.api.pafplayers.PAFPlayerManager;
import de.simonsator.partyandfriends.api.party.PlayerParty;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

/**
 * A collection of utilities for {@link PlayerParty}ies.
 */
public class PartyUtil {
    private PartyUtil() {
    }

    /**
     * Gets all players actively affiliated with a player, via a {@link PlayerParty}.
     *
     * @param player The target {@link ProxiedPlayer}.
     * @return An {@link ArrayList} of players that are affiliated with the player. This includes the player.
     */
    public static @NotNull ArrayList<ProxiedPlayer> getAffiliatedPlayers(final @NotNull ProxiedPlayer player) {
        final @NotNull OnlinePAFPlayer onlinePAFPlayer = PAFPlayerManager.getInstance().getPlayer(player);
        final @Nullable PlayerParty playerParty = onlinePAFPlayer.getParty();
        final @NotNull ArrayList<ProxiedPlayer> proxiedPlayers = new ArrayList<>();

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
     * @param player The {@link ProxiedPlayer} in question.
     * @return The {@link ProxiedPlayer} that leads {@code player}'s party. If {@code player} is not in a party, return {@code player}.
     */
    public static @NotNull ProxiedPlayer getLeader(final @NotNull ProxiedPlayer player) {
        final @NotNull OnlinePAFPlayer onlinePAFPlayer = PAFPlayerManager.getInstance().getPlayer(player);
        final @Nullable PlayerParty playerParty = onlinePAFPlayer.getParty();

        if (playerParty != null) {
            return playerParty.getLeader().getPlayer();
        } else {
            return player;
        }
    }

    /**
     * Sees if a {@link ProxiedPlayer} is the leader of a party.
     *
     * @param player The {@link ProxiedPlayer} in question.
     * @return {@code true} if the player is not in a party or is the leader, else {@code false}.
     */
    public static boolean leadsParty(final @NotNull ProxiedPlayer player) {
        final @NotNull OnlinePAFPlayer onlinePAFPlayer = PAFPlayerManager.getInstance().getPlayer(player);
        final @Nullable PlayerParty playerParty = onlinePAFPlayer.getParty();

        return playerParty == null || playerParty.isLeader(onlinePAFPlayer);
    }
}
