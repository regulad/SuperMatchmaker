package quest.ender.Matchmaker.handler;

import net.md_5.bungee.api.ReconnectHandler;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.jetbrains.annotations.NotNull;

public class ForcedServerReconnectHandler implements ReconnectHandler {
    private final @NotNull ServerInfo targetServerInfo;

    public ForcedServerReconnectHandler(@NotNull ServerInfo targetServerInfo) {
        this.targetServerInfo = targetServerInfo;
    }

    @Override
    public ServerInfo getServer(ProxiedPlayer player) {
        return targetServerInfo;
    }

    @Override
    public void setServer(ProxiedPlayer player) {
        /* no-op, we don't implement this */
    }

    @Override
    public void save() {
        /* no-op, we don't implement this */
    }

    @Override
    public void close() {
        /* no-op, we don't implement this */
    }
}
