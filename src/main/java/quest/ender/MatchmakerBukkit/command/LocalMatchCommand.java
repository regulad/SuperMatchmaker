package quest.ender.MatchmakerBukkit.command;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import quest.ender.MatchmakerBukkit.MatchmakerBukkit;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public final class LocalMatchCommand implements CommandExecutor, TabCompleter {
    private final @NotNull MatchmakerBukkit matchmakerBukkit;
    private @Nullable CompletableFuture<@NotNull String> gameList = null;

    public LocalMatchCommand(final @NotNull MatchmakerBukkit matchmakerBukkit) {
        this.matchmakerBukkit = matchmakerBukkit;
    }

    @Override
    public boolean onCommand(final @NotNull CommandSender sender, final @NotNull Command command, final @NotNull String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Only players may execute this command."));
            return false;
        } else if (args.length != 1) {
            sender.sendMessage(Component.text("This command only excepts one argument, the game name."));
            return false;
        } else {
            final @NotNull String gameName = args[0];

            final @Nullable CompletableFuture<@NotNull String> serverInfoCompletableFuture = this.matchmakerBukkit.sendToGame(player, gameName);

            serverInfoCompletableFuture.thenApply((targetServer) -> {
                if (!targetServer.equals("null")) {
                    sender.sendMessage(Component.text("Sent you to " + targetServer + ".").color(TextColor.color(5635925)));
                } else {
                    sender.sendMessage(Component.text("We were unable to send you to a game. Try again later.").color(TextColor.color(16733525)));
                }
                return targetServer;
            });

            return true;
        }
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {
        if (sender instanceof final @NotNull Player playerSender) {
            if (this.gameList == null) this.gameList = this.matchmakerBukkit.getGames(playerSender);

            if (!this.gameList.isDone()) {
                return Collections.singletonList("Waiting...");
            } else {
                try {
                    return Arrays.asList(this.gameList.get().split(", "));
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
