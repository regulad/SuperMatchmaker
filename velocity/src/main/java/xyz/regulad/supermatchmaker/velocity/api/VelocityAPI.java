package xyz.regulad.supermatchmaker.velocity.api;

import org.jetbrains.annotations.NotNull;
import xyz.regulad.supermatchmaker.api.CommonAPI;
import xyz.regulad.supermatchmaker.velocity.MatchmakerVelocity;

public class VelocityAPI extends CommonAPI {
    private final MatchmakerVelocity matchmakerVelocity;

    public VelocityAPI(final @NotNull MatchmakerVelocity matchmakerVelocity) {
        this.matchmakerVelocity = matchmakerVelocity;
        CommonAPI.setInstance(this);
    }
}
