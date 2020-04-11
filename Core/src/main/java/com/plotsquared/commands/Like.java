package com.plotsquared.commands;

import com.plotsquared.PlotSquared;
import com.plotsquared.config.Captions;
import com.plotsquared.config.Settings;
import com.plotsquared.database.DBFunc;
import com.plotsquared.events.PlotRateEvent;
import com.plotsquared.plot.flags.implementations.DoneFlag;
import com.plotsquared.plot.Plot;
import com.plotsquared.player.PlotPlayer;
import com.plotsquared.plot.Rating;
import com.plotsquared.events.TeleportCause;
import com.plotsquared.util.MainUtil;
import com.plotsquared.util.Permissions;
import com.plotsquared.util.tasks.TaskManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

@CommandDeclaration(command = "like",
    permission = "plots.like",
    description = "Like the plot",
    usage = "/plot like [next|purge]",
    category = CommandCategory.INFO,
    requiredType = RequiredType.PLAYER)
public class Like extends SubCommand {

    protected static boolean handleLike(final PlotPlayer player, String[] args,
        final boolean like) {
        final UUID uuid = player.getUUID();
        if (args.length == 1) {
            switch (args[0].toLowerCase()) {
                case "next": {
                    final List<Plot> plots = new ArrayList<>(PlotSquared.get().getBasePlots());
                    plots.sort((p1, p2) -> {
                        double v1 = getLikesPercentage(p1);
                        double v2 = getLikesPercentage(p2);
                        if (v1 == v2) {
                            return -0;
                        }
                        return v2 > v1 ? 1 : -1;
                    });
                    for (final Plot plot : plots) {
                        if ((!Settings.Done.REQUIRED_FOR_RATINGS || DoneFlag.isDone(plot)) && plot
                            .isBasePlot() && (!plot.getLikes().containsKey(uuid))) {
                            plot.teleportPlayer(player, TeleportCause.COMMAND, result -> {});
                            MainUtil.sendMessage(player, Captions.RATE_THIS);
                            return true;
                        }
                    }
                    MainUtil.sendMessage(player, Captions.FOUND_NO_PLOTS);
                    return true;
                }
                case "purge": {
                    final Plot plot = player.getCurrentPlot();
                    if (plot == null) {
                        return !sendMessage(player, Captions.NOT_IN_PLOT);
                    }
                    if (!Permissions
                        .hasPermission(player, Captions.PERMISSION_ADMIN_COMMAND_RATE, true)) {
                        return false;
                    }
                    plot.clearRatings();
                    Captions.RATINGS_PURGED.send(player);
                    return true;
                }
            }
        }
        final Plot plot = player.getCurrentPlot();
        if (plot == null) {
            return !sendMessage(player, Captions.NOT_IN_PLOT);
        }
        if (!plot.hasOwner()) {
            sendMessage(player, Captions.RATING_NOT_OWNED);
            return false;
        }
        if (plot.isOwner(player.getUUID())) {
            sendMessage(player, Captions.RATING_NOT_YOUR_OWN);
            return false;
        }
        if (Settings.Done.REQUIRED_FOR_RATINGS && !DoneFlag.isDone(plot)) {
            sendMessage(player, Captions.RATING_NOT_DONE);
            return false;
        }
        final Runnable run = () -> {
            final Boolean oldRating = plot.getLikes().get(uuid);
            if (oldRating != null) {
                sendMessage(player, Captions.RATING_ALREADY_EXISTS, plot.getId().toString());
                return;
            }
            final int rating;
            if (like) {
                rating = 10;
            } else {
                rating = 1;
            }
            plot.addRating(uuid, new Rating(rating));
            final PlotRateEvent
                event = PlotSquared.get().getEventDispatcher().callRating(player, plot, new Rating(rating));
            if (event.getRating() != null) {
                plot.addRating(uuid, event.getRating());
                if (like) {
                    sendMessage(player, Captions.RATING_LIKED, plot.getId().toString());
                } else {
                    sendMessage(player, Captions.RATING_DISLIKED, plot.getId().toString());
                }
            }
        };
        if (plot.getSettings().getRatings() == null) {
            if (!Settings.Enabled_Components.RATING_CACHE) {
                TaskManager.runTaskAsync(() -> {
                    plot.getSettings().setRatings(DBFunc.getRatings(plot));
                    run.run();
                });
                return true;
            }
            plot.getSettings().setRatings(new HashMap<>());
        }
        run.run();
        return true;
    }

    /**
     * Get the likes to dislike ratio of a plot as a percentage (in decimal form)
     *
     * @return likes to dislike ratio, returns zero if the plot has no likes
     */
    public static double getLikesPercentage(final Plot plot) {
        if (!plot.hasRatings()) {
            return 0;
        }
        final Collection<Boolean> reactions = plot.getLikes().values();
        double numLikes = 0, numDislikes = 0;
        for (final boolean reaction : reactions) {
            if (reaction) {
                numLikes += 1;
            } else {
                numDislikes += 1;
            }
        }
        if (numLikes == 0 && numDislikes == 0) {
            return 0D;
        } else if (numDislikes == 0) {
            return 1.0D;
        }
        return numLikes / (numLikes + numDislikes);
    }

    @Override public boolean onCommand(PlotPlayer player, String[] args) {
        return handleLike(player, args, true);
    }

}
