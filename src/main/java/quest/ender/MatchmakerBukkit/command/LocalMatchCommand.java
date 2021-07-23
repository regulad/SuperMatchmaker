package quest.ender.MatchmakerBukkit.command;

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

public class LocalMatchCommand implements CommandExecutor, TabCompleter {
    private final @NotNull MatchmakerBukkit matchmakerBukkit;
    private @Nullable CompletableFuture<@NotNull String> gameList = null;

    public LocalMatchCommand(@NotNull MatchmakerBukkit matchmakerBukkit) {
        this.matchmakerBukkit = matchmakerBukkit;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players may execute this command.");
            return false;
        } else if (args.length != 1) {
            sender.sendMessage("This command only excepts one argument, the game name.");
            return false;
        } else {
            final @NotNull Player player = (Player) sender;
            final @NotNull String gameName = args[0];

            final @Nullable CompletableFuture<@NotNull String> serverInfoCompletableFuture = this.matchmakerBukkit.sendToGame(player, gameName);

            serverInfoCompletableFuture.thenApply((targetServer) -> {
                if (!targetServer.equals("null")) {
                    sender.sendMessage("Sent you to " + targetServer + ".");
                } else {
                    sender.sendMessage("We were unable to send you to a game. Try again later.");
                }
                return targetServer;
            });

            return true;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (sender instanceof Player) {
            final @NotNull Player playerSender = (Player) sender;
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
