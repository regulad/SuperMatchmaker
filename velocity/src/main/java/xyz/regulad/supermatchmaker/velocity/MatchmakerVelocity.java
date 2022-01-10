package xyz.regulad.supermatchmaker.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import lombok.Getter;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.yaml.YAMLConfigurationLoader;
import org.bstats.velocity.Metrics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.regulad.supermatchmaker.velocity.api.VelocityAPI;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.logging.Logger;

// fixme
@Plugin(
        id = "supermatchmaker",
        name = "SuperMatchmaker",
        version = "1.4.0-SNAPSHOT",
        description = "Matchmaker is a cross-platform matchmaking plugin for Waterfall, Velocity, and PaperSpigot>=1.8.8.",
        authors = {"regulad"}
)
public class MatchmakerVelocity {
    @Getter
    private static @Nullable MatchmakerVelocity instance;

    @Inject
    private @NotNull ProxyServer server;
    @Inject
    private @NotNull Logger logger;

    @Inject
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
    private @Nullable ConfigurationNode configurationRoot;

    @Getter
    private final @NotNull VelocityAPI velocityAPI = new VelocityAPI(this);

    public MatchmakerVelocity() {
        instance = this;
    }

    @Subscribe
    public void initializeMetrics(final @NotNull ProxyInitializeEvent proxyInitializeEvent) {
        this.metrics = this.metricsFactory.make(this, 13899); // TODO: Replace this in your plugin!
    }

    @Subscribe
    public void loadConfig(final @NotNull ProxyInitializeEvent proxyInitializeEvent) {
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

            this.configurationRoot = this.configurationLoader.load();

            logger.info("Config version: " + this.configurationRoot.getNode("version").getInt());
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }
}
