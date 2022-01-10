package quest.ender.MatchmakerBukkit.command;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import quest.ender.MatchmakerBukkit.MatchmakerBukkit;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public final class LocalMatchCommand implements CommandExecutor, TabCompleter {
    private final @NotNull MatchmakerBukkit matchmakerBukkit;
    private @Nullable CompletableFuture<@NotNull Collection<@NotNull String>> gameList = null;

    public LocalMatchCommand(final @NotNull MatchmakerBukkit matchmakerBukkit) {
        this.matchmakerBukkit = matchmakerBukkit;
    }

    private static final @NotNull TextComponent SENT_TO_SERVER = Component.text("Sent you to %s.").color(NamedTextColor.GREEN);
    private static final @NotNull TextComponent SEND_FAILED = Component.text("We were unable to send you to a game. Try again later.").color(NamedTextColor.RED);
    private static final @NotNull TextComponent PLAYERS_ONLY = Component.text("Only players may execute this command.").color(NamedTextColor.RED);
    private static final @NotNull TextComponent ARGUMENT_MISMATCH = Component.text("This command only excepts one argument, the game name.").color(NamedTextColor.RED);

    @Override
    public boolean onCommand(final @NotNull CommandSender sender, final @NotNull Command command, final @NotNull String label, String[] args) {
        final @NotNull Audience audience = this.matchmakerBukkit.getBukkitAudiences().sender(sender);
        if (!(sender instanceof Player)) {
            audience.sendMessage(PLAYERS_ONLY);
            return false;
        } else if (args.length != 1) {
            audience.sendMessage(ARGUMENT_MISMATCH);
            return false;
        } else {
            final @NotNull Player player = (Player) sender;

            final @NotNull String gameName = args[0];

            final @Nullable CompletableFuture<@NotNull String> serverInfoCompletableFuture = this.matchmakerBukkit.sendToGame(player, gameName);

            serverInfoCompletableFuture.thenApply((targetServer) -> {
                if (!targetServer.equals("null")) {
                    audience.sendMessage(SENT_TO_SERVER.content(String.format(SENT_TO_SERVER.content(), targetServer)));
                } else {
                    audience.sendMessage(SEND_FAILED);
                }
                return targetServer;
            });

            return true;
        }
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {
        if (sender instanceof Player) {
            final @NotNull Player playerSender = (Player) sender;

            if (this.gameList == null) this.gameList = this.matchmakerBukkit.getGames(playerSender);

            if (!this.gameList.isDone()) {
                return Collections.singletonList("Waiting...");
            } else {
                try {
                    return (List<String>) this.gameList.get();
                } catch (ExecutionException | InterruptedException exception) {
                    exception.printStackTrace();
                    return Collections.singletonList("Error!");
                }
            }
        } else {
            return null;
        }
    }
}
