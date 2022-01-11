package xyz.regulad.supermatchmaker.velocity.command;

import com.google.common.base.Strings;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import lombok.Data;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import ninja.leaping.configurate.ConfigurationNode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.regulad.supermatchmaker.velocity.MatchmakerVelocity;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;

@Data
public final class MakeMatchCommand implements SimpleCommand {
    public static final @NotNull String CONFIG_NAMESPACE = "makematch";

    public MakeMatchCommand(final @NotNull MatchmakerVelocity matchmaker) {
        this.matchmaker = matchmaker;
        this.configurationNode = matchmaker.getConfig().getNode("commands", CONFIG_NAMESPACE);
    }

    private final @NotNull MatchmakerVelocity matchmaker;
    private final @NotNull ConfigurationNode configurationNode;

    private static final @NotNull TextComponent PLAYER_ONLY = Component.text("This command can only be executed by a player.", NamedTextColor.RED);
    private static final @NotNull TextComponent BAD_ARGUMENTS = Component.text("This command only accepts one argument, the game name.", NamedTextColor.RED);

    private static final @NotNull TextComponent OTHER_ERROR = Component.text("Something prevented you from entering that game. You may not have permission to do so.", NamedTextColor.RED);
    private static final @NotNull TextComponent CONNECTED = Component.text("Sent you to %s.", NamedTextColor.GREEN);
    private static final @NotNull TextComponent TIMED_OUT = Component.text("We were unable to send you to a game. Try again later.", NamedTextColor.RED);
    private static final @NotNull TextComponent GAME_DOESNT_EXIST = Component.text("This game does not exist.", NamedTextColor.RED);

    @Override
    public void execute(Invocation invocation) {
        if (invocation.source() instanceof final @NotNull Player player) {
            if (invocation.arguments().length == 1) {
                final @Nullable CompletableFuture<@NotNull RegisteredServer> future = this.matchmaker.getApi().sendToGame(player, invocation.arguments()[0]);
                if (future != null) {
                    future.whenComplete(((registeredServer, throwable) -> {
                        if (throwable == null) {
                            player.sendMessage(CONNECTED.content(String.format(CONNECTED.content(), registeredServer.getServerInfo().getName())));
                        } else {
                            player.sendMessage(throwable instanceof TimeoutException ? TIMED_OUT : OTHER_ERROR);
                        }
                    }));
                } else {
                    player.sendMessage(GAME_DOESNT_EXIST);
                }
            } else {
                player.sendMessage(BAD_ARGUMENTS);
            }
        } else {
            invocation.source().sendMessage(PLAYER_ONLY);
        }
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        return this.matchmaker.getApi().getGamesInstantly();
    }

    @Override
    public CompletableFuture<List<String>> suggestAsync(Invocation invocation) {
        return this.matchmaker.getApi().getGames();
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        final @Nullable String permission = this.configurationNode.getNode("permission").getString();
        return Strings.isNullOrEmpty(permission) || invocation.source().hasPermission(permission);
    }
}
