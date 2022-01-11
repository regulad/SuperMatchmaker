package xyz.regulad.supermatchmaker.velocity.command;

import com.google.common.base.Strings;
import com.velocitypowered.api.command.SimpleCommand;
import lombok.Data;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import ninja.leaping.configurate.ConfigurationNode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.regulad.supermatchmaker.velocity.MatchmakerVelocity;

@Data
public final class ReloadCommand implements SimpleCommand {
    public static final @NotNull String CONFIG_NAMESPACE = "reload";

    public ReloadCommand(final @NotNull MatchmakerVelocity matchmaker) {
        this.matchmaker = matchmaker;
        this.configurationNode = matchmaker.getConfig().getNode("commands", CONFIG_NAMESPACE);
    }

    private final @NotNull MatchmakerVelocity matchmaker;
    private final @NotNull ConfigurationNode configurationNode;

    private static final @NotNull TextComponent RELOADED = Component.text("Reloaded successfully.", NamedTextColor.GREEN);
    private static final @NotNull TextComponent UNKNOWN_ERROR = Component.text("An unknown error occurred. Check the console.", NamedTextColor.RED);

    @Override
    public void execute(Invocation invocation) {
        try {
            this.matchmaker.reloadConfig();
            invocation.source().sendMessage(RELOADED);
        } catch (Exception e) {
            invocation.source().sendMessage(UNKNOWN_ERROR);
            e.printStackTrace();
        }
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        final @Nullable String permission = this.configurationNode.getNode("permission").getString();
        return Strings.isNullOrEmpty(permission) || invocation.source().hasPermission(permission);
    }
}
