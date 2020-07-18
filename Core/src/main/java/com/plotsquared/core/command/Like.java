/*
 *       _____  _       _    _____                                _
 *      |  __ \| |     | |  / ____|                              | |
 *      | |__) | | ___ | |_| (___   __ _ _   _  __ _ _ __ ___  __| |
 *      |  ___/| |/ _ \| __|\___ \ / _` | | | |/ _` | '__/ _ \/ _` |
 *      | |    | | (_) | |_ ____) | (_| | |_| | (_| | | |  __/ (_| |
 *      |_|    |_|\___/ \__|_____/ \__, |\__,_|\__,_|_|  \___|\__,_|
 *                                    | |
 *                                    |_|
 *            PlotSquared plot management system for Minecraft
 *                  Copyright (C) 2020 IntellectualSites
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.plotsquared.core.command;

import com.google.inject.Inject;
import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.configuration.Captions;
import com.plotsquared.core.configuration.Settings;
import com.plotsquared.core.configuration.caption.TranslatableCaption;
import com.plotsquared.core.database.DBFunc;
import com.plotsquared.core.events.PlotRateEvent;
import com.plotsquared.core.events.TeleportCause;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.Rating;
import com.plotsquared.core.plot.flag.implementations.DoneFlag;
import com.plotsquared.core.util.EventDispatcher;
import com.plotsquared.core.util.MainUtil;
import com.plotsquared.core.util.Permissions;
import com.plotsquared.core.util.task.TaskManager;

import javax.annotation.Nonnull;
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

    private final EventDispatcher eventDispatcher;
    
    @Inject public Like(@Nonnull final EventDispatcher eventDispatcher) {
        this.eventDispatcher = eventDispatcher;
    }
    
    protected boolean handleLike(final PlotPlayer<?> player, String[] args,
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
                            plot.teleportPlayer(player, TeleportCause.COMMAND, result -> {
                            });
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
                        player.sendMessage(TranslatableCaption.of("errors.not_in_plot"));
                        return false;
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
            player.sendMessage(TranslatableCaption.of("errors.not_in_plot"));
            return false;
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
            final PlotRateEvent event =
                this.eventDispatcher.callRating(player, plot, new Rating(rating));
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

    @Override public boolean onCommand(PlotPlayer<?> player, String[] args) {
        return handleLike(player, args, true);
    }

}
