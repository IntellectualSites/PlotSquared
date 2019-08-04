package com.github.intellectualsites.plotsquared.plot.commands;

import com.github.intellectualsites.plotsquared.commands.CommandDeclaration;
import com.github.intellectualsites.plotsquared.plot.PlotSquared;
import com.github.intellectualsites.plotsquared.plot.config.Captions;
import com.github.intellectualsites.plotsquared.plot.config.Settings;
import com.github.intellectualsites.plotsquared.plot.database.DBFunc;
import com.github.intellectualsites.plotsquared.plot.flag.Flags;
import com.github.intellectualsites.plotsquared.plot.object.Plot;
import com.github.intellectualsites.plotsquared.plot.object.PlotPlayer;
import com.github.intellectualsites.plotsquared.plot.object.Rating;
import com.github.intellectualsites.plotsquared.plot.util.EventUtil;
import com.github.intellectualsites.plotsquared.plot.util.MainUtil;
import com.github.intellectualsites.plotsquared.plot.util.Permissions;
import com.github.intellectualsites.plotsquared.plot.util.TaskManager;

import java.util.*;

@CommandDeclaration(command = "like", permission = "plots.like", description = "Like the plot",
    usage = "/plot like [next|purge]", category = CommandCategory.INFO,
    requiredType = RequiredType.PLAYER) public class Like extends SubCommand {

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
                        if ((!Settings.Done.REQUIRED_FOR_RATINGS || plot.hasFlag(Flags.DONE))
                            && plot.isBasePlot() && (!plot.getLikes().containsKey(uuid))) {
                            plot.teleportPlayer(player);
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
        if (Settings.Done.REQUIRED_FOR_RATINGS && !plot.hasFlag(Flags.DONE)) {
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
            final Rating result = EventUtil.manager.callRating(player, plot, new Rating(rating));
            if (result != null) {
                plot.addRating(uuid, result);
                if (like) {
                    sendMessage(player, Captions.RATING_LIKED, plot.getId().toString());
                } else {
                    sendMessage(player, Captions.RATING_DISLIKED, plot.getId().toString());
                }
            }
        };
        if (plot.getSettings().ratings == null) {
            if (!Settings.Enabled_Components.RATING_CACHE) {
                TaskManager.runTaskAsync(() -> {
                    plot.getSettings().ratings = DBFunc.getRatings(plot);
                    run.run();
                });
                return true;
            }
            plot.getSettings().ratings = new HashMap<>();
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
