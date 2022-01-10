package xyz.regulad.supermatchmaker.velocity.listener;

import com.google.common.base.Strings;
import com.velocitypowered.api.event.EventTask;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.event.player.KickedFromServerEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.server.ServerInfo;
import com.velocitypowered.api.scheduler.ScheduledTask;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.regulad.supermatchmaker.velocity.MatchmakerVelocity;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * BungeeCord port
 */
@RequiredArgsConstructor
public final class ConnectionListener {
    private final @NotNull MatchmakerVelocity matchmaker;

    private final static @NotNull TextComponent TAKING_A_WHILE = Component.text("Couldn't find you a lobby. We are still trying...", NamedTextColor.RED);
    private final static @NotNull TextComponent FOUND_A_SERVER = Component.text("Found one! Sending you to %s...", NamedTextColor.GREEN);
    private final static @NotNull TextComponent UNABLE_TO_FIND_FALLBACK_NO_ERROR = Component.text("Failed to find a fallback server when an issue occurred on your current server.", NamedTextColor.RED);
    private final static @NotNull TextComponent UNABLE_TO_FIND_FALLBACK = Component.text("Failed to find a fallback server when an issue occurred on your current server: ", NamedTextColor.RED);

    private final static @NotNull String DEFAULT_FALLBACK_MESSAGE = "Something went wrong! You have been sent to limbo.";

    @Subscribe
    public @NotNull EventTask onPlayerJoin(final @NotNull PostLoginEvent postLoginEvent) {
        return EventTask.async(() -> {
            final @NotNull Player targetPlayer = postLoginEvent.getPlayer();
            final @NotNull Optional<ServerConnection> currentServer = targetPlayer.getCurrentServer();
            final @NotNull Optional<RegisteredServer> registeredServer = currentServer.map(ServerConnection::getServer);
            final @NotNull Optional<String> currentGame = registeredServer.map(this.matchmaker.getApi()::getGameFromServer);

            final @Nullable String targetGame = Objects.requireNonNull(this.matchmaker.getConfig()).getNode("login", "game").getString();

            if (targetGame != null && (currentGame.isEmpty() || !currentGame.get().equals(targetGame))) {
                // In short: Schedule this task login.game seconds away, if either the current game is not part of a game or the current game login.game.
                final @Nullable CompletableFuture<RegisteredServer> serverInfoCompletableFuture = this.matchmaker.getApi().getServer(targetGame, 1, postLoginEvent.getPlayer());

                if (serverInfoCompletableFuture != null) {
                    final @NotNull ScheduledTask scheduledTask = this.matchmaker.getProxy().getScheduler().buildTask(this.matchmaker, () -> {
                        if (!serverInfoCompletableFuture.isDone()) {
                            targetPlayer.sendMessage(TAKING_A_WHILE);
                        }
                    }).repeat(this.matchmaker.getConfig().getNode("timeout").getLong(750) * 4, TimeUnit.MILLISECONDS).schedule(); // Arbitrary.

                    serverInfoCompletableFuture.thenAccept((targetServerInfo) -> {
                        scheduledTask.cancel();
                        targetPlayer.sendMessage(FOUND_A_SERVER.content(String.format(FOUND_A_SERVER.content(), targetServerInfo.getServerInfo().getName())));
                    });
                }
            }
        });
    }

    @Subscribe
    public @Nullable EventTask onPlayerKick(final @NotNull KickedFromServerEvent serverKickEvent) {
        final @Nullable String gameName = Objects.requireNonNull(this.matchmaker.getConfig()).getNode("fallback", "game").getString();

        if (!Strings.isNullOrEmpty(gameName)) {
            final @NotNull Player affectedPlayer = serverKickEvent.getPlayer();
            final @NotNull Collection<@NotNull RegisteredServer> servers = this.matchmaker.getApi().getServers(gameName);

            final @NotNull String fallbackMessage = this.matchmaker.getConfig().getNode("fallback", "message").getString(DEFAULT_FALLBACK_MESSAGE);
            final @NotNull Component fallbackComponent = LegacyComponentSerializer.legacyAmpersand().deserialize(fallbackMessage);

            if (servers.size() == 1) {
                serverKickEvent.setResult(KickedFromServerEvent.RedirectPlayer.create(servers.stream().findFirst().get(), fallbackComponent));
            } else {
                final @Nullable CompletableFuture<RegisteredServer> serverInfoCompletableFuture = this.matchmaker.getApi().getServer(gameName, 1, affectedPlayer);

                if (serverInfoCompletableFuture != null) {
                    return EventTask.async(() -> {
                        serverInfoCompletableFuture.whenComplete((targetServerInfo, exception) -> {
                            if (exception == null) {
                                serverKickEvent.setResult(KickedFromServerEvent.RedirectPlayer.create(targetServerInfo, fallbackComponent));
                            } else {
                                final @NotNull Component kickReason = serverKickEvent.getServerKickReason().isPresent()
                                        ? UNABLE_TO_FIND_FALLBACK.append(serverKickEvent.getServerKickReason().get())
                                        : UNABLE_TO_FIND_FALLBACK_NO_ERROR;
                                serverKickEvent.setResult(KickedFromServerEvent.DisconnectPlayer.create(kickReason));
                            }
                        });

                        serverInfoCompletableFuture.orTimeout(this.matchmaker.getConfig().getNode("timeout").getLong(750), TimeUnit.MILLISECONDS);

                        // Do this, so we can hold on until the server decides it's time to end or we get the timeout limit.
                        serverInfoCompletableFuture.join();
                    });
                }
            }
        }
        return null; // We didn't already return
    }
}
