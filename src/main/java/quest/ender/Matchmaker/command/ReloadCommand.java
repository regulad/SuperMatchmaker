package quest.ender.Matchmaker.command;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.plugin.Command;
import org.jetbrains.annotations.NotNull;
import quest.ender.Matchmaker.Matchmaker;

public class ReloadCommand extends Command {
    private final @NotNull Matchmaker matchmaker;

    public ReloadCommand(@NotNull Matchmaker matchmaker, String name) {
        super(name);
        this.matchmaker = matchmaker;
    }

    public ReloadCommand(@NotNull Matchmaker matchmaker, String name, String permission, String... aliases) {
        super(name, permission, aliases);
        this.matchmaker = matchmaker;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        this.matchmaker.reloadConfig();
        sender.sendMessage(new ComponentBuilder("Reloaded configuration.").color(ChatColor.GREEN).create());
    }
}
