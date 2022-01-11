package xyz.regulad.supermatchmaker.velocity;

import com.google.common.reflect.TypeToken;
import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Dependency;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import lombok.Getter;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.yaml.YAMLConfigurationLoader;
import org.bstats.velocity.Metrics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.regulad.supermatchmaker.common.api.MatchmakerAPI;
import xyz.regulad.supermatchmaker.velocity.api.VelocityAPI;
import xyz.regulad.supermatchmaker.velocity.command.LobbyCommand;
import xyz.regulad.supermatchmaker.velocity.command.MakeMatchCommand;
import xyz.regulad.supermatchmaker.velocity.command.ReloadCommand;
import xyz.regulad.supermatchmaker.velocity.listener.ConnectionListener;
import xyz.regulad.supermatchmaker.velocity.listener.GameSendListener;
import xyz.regulad.supermatchmaker.velocity.listener.PluginMessageListener;
import xyz.regulad.supermatchmaker.velocity.util.VelocityChannels;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Objects;
import java.util.logging.Logger;

// fixme
@Plugin(
        id = "supermatchmaker",
        name = "SuperMatchmaker",
        version = "1.4.0-SNAPSHOT",
        url = "https://github.com/regulad/SuperMatchmaker",
        description = "Matchmaker is a cross-platform matchmaking plugin for Waterfall, Velocity, and PaperSpigot>=1.8.8.",
        authors = {"regulad"},
        dependencies = {@Dependency(id = "partyandfriends")}
)
public class MatchmakerVelocity {
    @Getter
    private static @Nullable MatchmakerVelocity instance;

    @Inject
    @Getter
    private @NotNull ProxyServer proxy;
    @Inject
    @Getter
    private @NotNull Logger logger;

    @Inject
    @Getter
    private @NotNull Metrics.Factory metricsFactory;
    @Getter
    private @Nullable Metrics metrics;

    @Inject
    @DataDirectory
    private @NotNull Path pluginFolder;
    @Getter
    private @Nullable File pluginFolderFile;
    @Getter
    private @Nullable File configFile;
    @Getter
    private @Nullable YAMLConfigurationLoader configurationLoader;
    @Getter
    private @Nullable ConfigurationNode config;

    @Getter
    private final @NotNull VelocityAPI api = new VelocityAPI(this);

    public MatchmakerVelocity() {
        instance = this;
    }

    @Subscribe
    public void initializeMetrics(final @NotNull ProxyInitializeEvent proxyInitializeEvent) {
        this.metrics = this.metricsFactory.make(this, 13899); // TODO: Replace this in your plugin!
    }

    @Subscribe
    public void assertInstance(final @NotNull ProxyInitializeEvent proxyInitializeEvent) {
        MatchmakerAPI.setInstance(this.api);
    }

    @Subscribe
    public void registerListeners(final @NotNull ProxyInitializeEvent proxyInitializeEvent) {
        this.getProxy().getEventManager().register(this, new PluginMessageListener(this));
        this.getProxy().getEventManager().register(this, new GameSendListener(this));
        this.getProxy().getEventManager().register(this, new ConnectionListener(this));
    }


    @Subscribe
    public void registerCommands(final @NotNull ProxyInitializeEvent proxyInitializeEvent) {
        final @NotNull LobbyCommand lobbyCommand = new LobbyCommand(this);
        final @NotNull MakeMatchCommand makeMatchCommand = new MakeMatchCommand(this);
        final @NotNull ReloadCommand reloadCommand = new ReloadCommand(this);
        try {
            this.getProxy().getCommandManager().register(
                    lobbyCommand.getConfigurationNode().getNode("name").getString("makematch"),
                    lobbyCommand,
                    lobbyCommand.getConfigurationNode().getNode("aliases").getList(TypeToken.of(String.class)).toArray(String[]::new)
            );
            this.getProxy().getCommandManager().register(
                    makeMatchCommand.getConfigurationNode().getNode("name").getString("lobby"),
                    makeMatchCommand,
                    makeMatchCommand.getConfigurationNode().getNode("aliases").getList(TypeToken.of(String.class)).toArray(String[]::new)
            );
            this.getProxy().getCommandManager().register(
                    reloadCommand.getConfigurationNode().getNode("name").getString("mmreload"),
                    reloadCommand,
                    reloadCommand.getConfigurationNode().getNode("aliases").getList(TypeToken.of(String.class)).toArray(String[]::new)
            );
        } catch (ObjectMappingException e) {
            e.printStackTrace();
        }
    }

    @Subscribe
    public void releaseInstance(final @NotNull ProxyShutdownEvent proxyShutdownEvent) {
        MatchmakerAPI.setInstance(null);
    }

    @Subscribe
    public void registerPluginChannel(final @NotNull ProxyInitializeEvent proxyInitializeEvent) {
        this.getProxy().getChannelRegistrar().register(VelocityChannels.TO_BACKEND_CHANNEL, VelocityChannels.TO_PROXY_CHANNEL);
    }

    @Subscribe
    public void unregisterPluginChannel(final @NotNull ProxyShutdownEvent proxyShutdownEvent) {
        this.getProxy().getChannelRegistrar().unregister(VelocityChannels.TO_BACKEND_CHANNEL, VelocityChannels.TO_PROXY_CHANNEL);
    }

    public void reloadConfig() throws IOException {
        this.config = Objects.requireNonNull(this.configurationLoader).load();
    }

    @Subscribe
    public void initiallyLoadConfig(final @NotNull ProxyInitializeEvent proxyInitializeEvent) {
        try {
            this.pluginFolderFile = this.pluginFolder.toFile();

            if (!this.pluginFolderFile.exists()) {
                this.pluginFolderFile.mkdir();
            }

            this.configFile = new File(this.pluginFolderFile, "config.yml");

            if (!this.configFile.exists()) {
                try (final @Nullable InputStream configStream = MatchmakerVelocity.class.getClassLoader().getResourceAsStream("config.yml")) {
                    if (configStream != null) {
                        try (final @NotNull FileOutputStream outputStream = new FileOutputStream(this.configFile)) {
                            configStream.transferTo(outputStream);
                        }
                    }
                }
            }

            this.configurationLoader = YAMLConfigurationLoader.builder()
                    .setFile(this.configFile)
                    .build();

            reloadConfig();

            logger.info("Config version: " + this.config.getNode("version").getInt());
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }
}
