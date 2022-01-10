package xyz.regulad.supermatchmaker.api;

import org.jetbrains.annotations.NotNull;

public class Channels {
    private Channels() {
    }

    public static final @NotNull String MATCHMAKER_CHANNEL_NAMESPACE = "matchmaker";

    public static final @NotNull String BACKEND_CHANNEL_NAME = "in";
    public static final @NotNull String PROXY_CHANNEL_NAME = "out";

    public static final @NotNull String TO_BACKEND_CHANNEL = MATCHMAKER_CHANNEL_NAMESPACE + ":" + BACKEND_CHANNEL_NAME;
    public static final @NotNull String TO_PROXY_CHANNEL = MATCHMAKER_CHANNEL_NAMESPACE + ":" + PROXY_CHANNEL_NAME;
}
