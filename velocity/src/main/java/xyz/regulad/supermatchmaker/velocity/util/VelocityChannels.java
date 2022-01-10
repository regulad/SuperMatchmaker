package xyz.regulad.supermatchmaker.velocity.util;

import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import org.jetbrains.annotations.NotNull;
import xyz.regulad.supermatchmaker.api.Channels;

public final class VelocityChannels {
    private VelocityChannels() {
    }

    public final static @NotNull MinecraftChannelIdentifier TO_BACKEND_CHANNEL = MinecraftChannelIdentifier.create(Channels.MATCHMAKER_CHANNEL_NAMESPACE, Channels.BACKEND_CHANNEL_NAME);
    public final static @NotNull MinecraftChannelIdentifier TO_PROXY_CHANNEL = MinecraftChannelIdentifier.create(Channels.MATCHMAKER_CHANNEL_NAMESPACE, Channels.PROXY_CHANNEL_NAME);
}
