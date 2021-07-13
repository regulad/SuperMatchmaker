package quest.ender.Matchmaker.events;

import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.List;

public class GameSendSuccessEvent extends GameSendEvent {
    private final List<ProxiedPlayer> movedPlayers;
    private final ServerInfo targetServer;

    public GameSendSuccessEvent(List<ProxiedPlayer> movedPlayers, ServerInfo targetServer) {
        this.movedPlayers = movedPlayers;
        this.targetServer = targetServer;
    }

    public List<ProxiedPlayer> getMovedPlayers() {
        return this.movedPlayers;
    }

    public ServerInfo getTargetServer() {
        return this.targetServer;
    }
}
