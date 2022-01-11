package quest.ender.MatchmakerBukkit.placeholder;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.clip.placeholderapi.expansion.Taskable;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import quest.ender.MatchmakerBukkit.MatchmakerBukkit;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class MatchmakerExpansion extends PlaceholderExpansion implements Taskable {
    private final @NotNull MatchmakerBukkit matchmakerBukkit;
    private final @NotNull HashMap<@NotNull String, @NotNull CompletableFuture<@NotNull Integer>> gameStatFutures = new HashMap<>();
    private @Nullable CompletableFuture<@NotNull String> currentGameFuture = null;
    private @Nullable CompletableFuture<@NotNull List<@NotNull String>> allGamesFuture = null;
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
    public @NotNull String getIdentifier() {
        return this.matchmakerBukkit.getDescription().getName().toLowerCase();
    }

    @Override
    public @NotNull String getAuthor() {
        return String.join(", ", this.matchmakerBukkit.getDescription().getAuthors());
    }

    @Override
    public @NotNull String getVersion() {
        return this.matchmakerBukkit.getDescription().getVersion();
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
                final @NotNull CompletableFuture<@NotNull Integer> statsCompletableFuture = this.gameStatFutures.computeIfAbsent(
                        game, k -> this.matchmakerBukkit.getGamePlayerCount(player, k)
                );

                if (!statsCompletableFuture.isDone()) {
                    return "Waiting...";
                } else {
                    try {
                        return String.valueOf(statsCompletableFuture.get());
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
                    return String.join(", ", this.allGamesFuture.get());
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
        this.bukkitRunnable.runTaskTimer(this.matchmakerBukkit, 2400L, 2400L); // Can't run async, ran into weird issues where the future would become null after the placeholder request checked for it.
    }

    @Override
    public void stop() {
        if (this.matchmakerBukkit.getServer().getScheduler().isCurrentlyRunning(this.bukkitRunnable.getTaskId()))
            this.bukkitRunnable.cancel();
    }
}
