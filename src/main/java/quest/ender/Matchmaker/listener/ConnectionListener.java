package quest.ender.Matchmaker.listener;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.event.ServerKickEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.scheduler.ScheduledTask;
import net.md_5.bungee.event.EventHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import quest.ender.Matchmaker.Matchmaker;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class ConnectionListener implements Listener {
    private final @NotNull Matchmaker matchmaker;

    public ConnectionListener(final @NotNull Matchmaker matchmaker) {
        this.matchmaker = matchmaker;
    }

    @EventHandler
    public void onPlayerJoin(final @NotNull PostLoginEvent postLoginEvent) {
        this.matchmaker.getProxy().getScheduler().schedule(this.matchmaker, () -> {
            final @Nullable Server currentServer = postLoginEvent.getPlayer().getServer();
            final @Nullable ServerInfo currentServerInfo = currentServer != null ? currentServer.getInfo() : null;
            final @Nullable String currentGame = currentServerInfo != null ? this.matchmaker.getGame(currentServerInfo) : null;

            final @NotNull String targetGame = this.matchmaker.getConfig().getString("login.game");

            if (currentGame == null || !currentGame.equals(targetGame)) {
                // In short: Schedule this task login.game seconds away, if either the current game is not part of a game or the current game login.game.
                final @NotNull ProxiedPlayer targetPlayer = postLoginEvent.getPlayer();
                final @Nullable CompletableFuture<ServerInfo> serverInfoCompletableFuture = this.matchmaker.getServer(targetGame, 1, postLoginEvent.getPlayer());

                if (serverInfoCompletableFuture != null) {
                    final boolean[] isMessagedSingleton = {false};

                    final @NotNull ScheduledTask scheduledTask = this.matchmaker.getProxy().getScheduler().schedule(this.matchmaker, () -> {
                        if (!serverInfoCompletableFuture.isDone()) {
                            isMessagedSingleton[0] = true;
                            targetPlayer.sendMessage(new ComponentBuilder("Couldn't find you a lobby. We are still trying...").color(ChatColor.RED).create());
                        }
                    }, this.matchmaker.getConfig().getLong("timeout"), this.matchmaker.getConfig().getLong("timeout") * 4, TimeUnit.MILLISECONDS); // Arbitrary.

                    serverInfoCompletableFuture.thenApply((targetServerInfo) -> {
                        scheduledTask.cancel();
                        if (isMessagedSingleton[0])
                            targetPlayer.sendMessage(new ComponentBuilder("Found one! Sending you to " + targetServerInfo.getName() + "...").color(ChatColor.GREEN).create());
                        targetPlayer.connect(targetServerInfo);

                        return targetServerInfo;
                    });
                }
            }
        }, this.matchmaker.getConfig().getLong("login.after"), TimeUnit.SECONDS);
    }

    @EventHandler
    public void onPlayerKick(final @NotNull ServerKickEvent serverKickEvent) { // It may take too long for the fallback server to be found. That's fine, I guess?
        final @NotNull ProxiedPlayer affectedPlayer = serverKickEvent.getPlayer();
        final @NotNull String gameName = this.matchmaker.getConfig().getString("fallback.game");
        final @NotNull ArrayList<@NotNull ServerInfo> servers = this.matchmaker.getServers(gameName);

        if (servers.size() == 1) {
            serverKickEvent.setCancelServer(servers.get(0));
            serverKickEvent.setCancelled(true);
            affectedPlayer.sendMessage(new ComponentBuilder(this.matchmaker.getConfig().getString("fallback.message")).create());
        } else {
            final @Nullable CompletableFuture<ServerInfo> serverInfoCompletableFuture = this.matchmaker.getServer(gameName, 1, serverKickEvent.getPlayer());

            if (serverInfoCompletableFuture != null) {
                serverInfoCompletableFuture.thenApply((targetServerInfo) -> {
                    serverKickEvent.setCancelServer(targetServerInfo);
                    serverKickEvent.setCancelled(true);
                    affectedPlayer.sendMessage(new ComponentBuilder(this.matchmaker.getConfig().getString("fallback.message")).create());

                    return targetServerInfo;
                });

                this.matchmaker.getProxy().getScheduler().schedule(this.matchmaker, () -> {
                    if (!serverInfoCompletableFuture.isDone()) {
                        serverInfoCompletableFuture.cancel(true);
                        serverKickEvent.setKickReasonComponent(new ComponentBuilder("Failed to find a fallback server when an issue occurred on your current server: ").color(ChatColor.RED).append(serverKickEvent.getKickReasonComponent()).create());
                    }
                }, this.matchmaker.getConfig().getLong("timeout"), TimeUnit.MILLISECONDS);
            }
        }
    }
}
