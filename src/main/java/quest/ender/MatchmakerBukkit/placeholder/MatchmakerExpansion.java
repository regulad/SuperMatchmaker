package quest.ender.MatchmakerBukkit.placeholder;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.clip.placeholderapi.expansion.Taskable;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import quest.ender.MatchmakerBukkit.MatchmakerBukkit;

import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class MatchmakerExpansion extends PlaceholderExpansion implements Taskable {
    private final @NotNull MatchmakerBukkit matchmakerBukkit;
    private final @NotNull HashMap<@NotNull String, @NotNull CompletableFuture<@NotNull String>> gameStatFutures = new HashMap<>();
    private @Nullable CompletableFuture<@NotNull String> currentGameFuture = null;
    private @Nullable CompletableFuture<@NotNull String> allGamesFuture = null;
    private final @NotNull BukkitRunnable bukkitRunnable = new BukkitRunnable() { // Clears the "cache" so any changes may be recorded.
        @Override
        public void run() {
            if (MatchmakerExpansion.this.currentGameFuture != null) MatchmakerExpansion.this.currentGameFuture = null;
            if (MatchmakerExpansion.this.allGamesFuture != null) MatchmakerExpansion.this.allGamesFuture = null;
            MatchmakerExpansion.this.gameStatFutures.clear();
        }
    };

    public MatchmakerExpansion(@NotNull MatchmakerBukkit matchmakerBukkit) {
        this.matchmakerBukkit = matchmakerBukkit;
    }

    @Override
    public String getIdentifier() {
        return "matchmaker";
    }

    @Override
    public String getAuthor() {
        return "regulad";
    }

    @Override
    public String getVersion() {
        return "${project.version}";
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onPlaceholderRequest(Player player, String placeholder) {
        if (placeholder.startsWith("stats_")) {
            @Nullable String game = placeholder.replaceAll("stats_", "");

            if (game.equals("current")) {
                if (this.currentGameFuture == null) this.currentGameFuture = this.matchmakerBukkit.getGame(player);

                if (!this.currentGameFuture.isDone()) {
                    game = null;
                } else {
                    try {
                        game = this.currentGameFuture.get();
                    } catch (ExecutionException | InterruptedException exception) {
                        exception.printStackTrace();
                        game = null;
                    }
                }
            }

            if (game == null) {
                return "Waiting on something else...";
            } else {
                final @NotNull CompletableFuture<@NotNull String> statsCompletableFuture = this.gameStatFutures.computeIfAbsent(game, k -> this.matchmakerBukkit.getGameStats(player, k));

                if (!statsCompletableFuture.isDone()) {
                    return "Waiting...";
                } else {
                    try {
                        return statsCompletableFuture.get();
                    } catch (ExecutionException | InterruptedException exception) {
                        exception.printStackTrace();
                        return "Error!";
                    }
                }
            }
        } else if (placeholder.equalsIgnoreCase("current")) {
            if (this.currentGameFuture == null) this.currentGameFuture = this.matchmakerBukkit.getGame(player);

            if (!this.currentGameFuture.isDone()) {
                return "Waiting...";
            } else {
                try {
                    return this.currentGameFuture.get();
                } catch (ExecutionException | InterruptedException exception) {
                    exception.printStackTrace();
                    return "Error!";
                }
            }
        } else if (placeholder.equalsIgnoreCase("games")) {
            if (this.allGamesFuture == null) this.allGamesFuture = this.matchmakerBukkit.getGames(player);

            if (!this.allGamesFuture.isDone()) {
                return "Waiting...";
            } else {
                try {
                    return this.allGamesFuture.get();
                } catch (ExecutionException | InterruptedException exception) {
                    exception.printStackTrace();
                    return "Error!";
                }
            }
        } else {
            return null;
        }
    }

    @Override
    public void start() {
        this.bukkitRunnable.runTaskTimerAsynchronously(this.matchmakerBukkit, 12000L, 12000L); // Should be safe to run async. I see no reason why not. It will take like, a microsecond anyway.
    }

    @Override
    public void stop() {
        if (!this.bukkitRunnable.isCancelled()) this.bukkitRunnable.cancel();
    }
}
