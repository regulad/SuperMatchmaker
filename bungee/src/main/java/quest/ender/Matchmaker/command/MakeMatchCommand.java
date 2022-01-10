package quest.ender.Matchmaker.command;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import quest.ender.Matchmaker.Matchmaker;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class MakeMatchCommand extends Command implements TabExecutor {
    private final @NotNull Matchmaker matchmaker;

    public MakeMatchCommand(@NotNull Matchmaker matchmaker, @NotNull String name) {
        super(name);
        this.matchmaker = matchmaker;
    }

    public MakeMatchCommand(@NotNull Matchmaker matchmaker, @NotNull String name, @Nullable String permission, @Nullable String... aliases) {
        super(name, permission, aliases);
        this.matchmaker = matchmaker;
    }

    @Override
    public void execute(@NotNull CommandSender sender, @NotNull String[] args) {
        if (!(sender instanceof ProxiedPlayer)) {
            sender.sendMessage(new TextComponent("Only players may execute this command."));
        } else if (args.length != 1) {
            sender.sendMessage(new TextComponent("This command only excepts one argument, the game name."));
        } else {
            final @NotNull ProxiedPlayer proxiedPlayer = (ProxiedPlayer) sender;
            final @NotNull String gameName = args[0];

            final @Nullable CompletableFuture<ServerInfo> serverInfoCompletableFuture = this.matchmaker.sendToGame(proxiedPlayer, gameName);

            if (serverInfoCompletableFuture != null) {
                serverInfoCompletableFuture.thenApply((targetServerInfo) -> {
                    sender.sendMessage(new ComponentBuilder("Sent you to " + targetServerInfo.getName() + ".").color(ChatColor.GREEN).create());
                    return targetServerInfo;
                });

                this.matchmaker.getProxy().getScheduler().schedule(this.matchmaker, () -> {
                    if (!serverInfoCompletableFuture.isDone()) {
                        serverInfoCompletableFuture.cancel(true);
                        sender.sendMessage(new ComponentBuilder("We were unable to send you to a game. Try again later.").color(ChatColor.RED).create());
                    }
                }, this.matchmaker.getConfig().getLong("timeout"), TimeUnit.MILLISECONDS);
            } else {
                sender.sendMessage(new ComponentBuilder("Something prevented you from entering that game. You may not have permission to do so.").color(ChatColor.RED).create());
            }
        }
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        return switch (args.length) {
            case 1 -> this.matchmaker.getGamesInstantly();
            default -> null;
        };
    }
}
