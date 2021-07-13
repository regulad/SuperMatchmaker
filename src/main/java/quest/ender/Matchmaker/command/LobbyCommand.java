package quest.ender.Matchmaker.command;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import quest.ender.Matchmaker.Matchmaker;

public class LobbyCommand extends Command {
    private final @NotNull Matchmaker matchmaker;

    public LobbyCommand(@NotNull Matchmaker matchmaker, @NotNull String name) {
        super(name);
        this.matchmaker = matchmaker;
    }

    public LobbyCommand(@NotNull Matchmaker matchmaker, @NotNull String name, @Nullable String permission, @Nullable String... aliases) {
        super(name, permission, aliases);
        this.matchmaker = matchmaker;
    }

    @Override
    public void execute(@NotNull CommandSender sender, @NotNull String[] args) {
        this.matchmaker.getProxy().getPluginManager().dispatchCommand(sender, this.matchmaker.getConfig().getString("commands.makematch.name") + " " + this.matchmaker.getConfig().getString("login"));
        // I hate this, but BungeeCord is weird as hell.
    }
}
