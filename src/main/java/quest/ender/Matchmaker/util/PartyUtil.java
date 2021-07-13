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
    /**
     * Gets all players actively affiliated with a player, via a {@link PlayerParty}.
     *
     * @param player The target {@link ProxiedPlayer}.
     * @return An {@link ArrayList} of players that are affiliated with the player. This includes the player.
     */
    public static @NotNull ArrayList<ProxiedPlayer> getAffiliatedPlayers(ProxiedPlayer player) {
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
}
