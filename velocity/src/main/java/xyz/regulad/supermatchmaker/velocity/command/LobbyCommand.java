package xyz.regulad.supermatchmaker.velocity.command;

import com.google.common.base.Strings;
import com.velocitypowered.api.command.SimpleCommand;
import lombok.Data;
import ninja.leaping.configurate.ConfigurationNode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.regulad.supermatchmaker.velocity.MatchmakerVelocity;

@Data
public final class LobbyCommand implements SimpleCommand {
    public static final @NotNull String CONFIG_NAMESPACE = "lobby";

    public LobbyCommand(final @NotNull MatchmakerVelocity matchmaker) {
        this.matchmaker = matchmaker;
        this.configurationNode = matchmaker.getConfig().getNode("commands", CONFIG_NAMESPACE);
    }

    private final @NotNull MatchmakerVelocity matchmaker;
    private final @NotNull ConfigurationNode configurationNode;

    @Override
    public void execute(Invocation invocation) {
        this.matchmaker.getProxy().getCommandManager().executeImmediatelyAsync(
                invocation.source(),
                String.format(
                        "%s %s",
                        matchmaker.getConfig().getNode("commands", MakeMatchCommand.CONFIG_NAMESPACE, "name").getString("makematch"),
                        matchmaker.getConfig().getNode("lobby_game").getString("lobby")
                )
        );
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        final @Nullable String permission = this.configurationNode.getNode("permission").getString();
        return Strings.isNullOrEmpty(permission) || invocation.source().hasPermission(permission);
    }
}
