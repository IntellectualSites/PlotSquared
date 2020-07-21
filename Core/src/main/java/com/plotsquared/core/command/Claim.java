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

import com.google.common.primitives.Ints;
import com.google.inject.Inject;
import com.plotsquared.core.configuration.caption.CaptionUtility;
import com.plotsquared.core.configuration.Captions;
import com.plotsquared.core.configuration.Settings;
import com.plotsquared.core.configuration.caption.TranslatableCaption;
import com.plotsquared.core.database.DBFunc;
import com.plotsquared.core.events.PlayerClaimPlotEvent;
import com.plotsquared.core.events.PlotMergeEvent;
import com.plotsquared.core.events.Result;
import com.plotsquared.core.location.Direction;
import com.plotsquared.core.location.Location;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.PlotArea;
import com.plotsquared.core.util.EconHandler;
import com.plotsquared.core.util.EventDispatcher;
import com.plotsquared.core.util.Expression;
import com.plotsquared.core.util.Permissions;
import com.plotsquared.core.util.task.TaskManager;
import net.kyori.adventure.text.minimessage.Template;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@CommandDeclaration(command = "claim", aliases = "c", description = "Claim the current plot you're standing on", category = CommandCategory.CLAIMING, requiredType = RequiredType.PLAYER, permission = "plots.claim", usage = "/plot claim")
public class Claim extends SubCommand {

    private static final Logger logger =
        LoggerFactory.getLogger("P2/" + Claim.class.getSimpleName());

    private final EventDispatcher eventDispatcher;
    private final EconHandler econHandler;

    @Inject public Claim(@Nonnull final EventDispatcher eventDispatcher,
        @Nullable final EconHandler econHandler) {
        this.eventDispatcher = eventDispatcher;
        this.econHandler = econHandler;
    }

    @Override public boolean onCommand(final PlotPlayer<?> player, String[] args) {
        String schematic = null;
        if (args.length >= 1) {
            schematic = args[0];
        }
        Location location = player.getLocation();
        Plot plot = location.getPlotAbs();
        if (plot == null) {
            player.sendMessage(TranslatableCaption.of("errors.not_in_plot"));
            return false;
        }
        final PlayerClaimPlotEvent event = this.eventDispatcher.callClaim(player, plot, schematic);
        schematic = event.getSchematic();
        if (event.getEventResult() == Result.DENY) {
            player.sendMessage(
                    TranslatableCaption.of("events.event_denied"),
                    Template.of("value", "Claim"));
            return true;
        }
        boolean force = event.getEventResult() == Result.FORCE;
        int currentPlots = Settings.Limit.GLOBAL ?
            player.getPlotCount() :
            player.getPlotCount(location.getWorldName());
        int grants = 0;
        if (currentPlots >= player.getAllowedPlots() && !force) {
            if (player.hasPersistentMeta("grantedPlots")) {
                grants = Ints.fromByteArray(player.getPersistentMeta("grantedPlots"));
                if (grants <= 0) {
                    player.removePersistentMeta("grantedPlots");
                    return sendMessage(player, Captions.CANT_CLAIM_MORE_PLOTS);
                }
            } else {
                return sendMessage(player, Captions.CANT_CLAIM_MORE_PLOTS);
            }
        }
        if (!plot.canClaim(player)) {
            return sendMessage(player, Captions.PLOT_IS_CLAIMED);
        }
        final PlotArea area = plot.getArea();
        if (schematic != null && !schematic.isEmpty()) {
            if (area.isSchematicClaimSpecify()) {
                if (!area.hasSchematic(schematic)) {
                    return sendMessage(player, Captions.SCHEMATIC_INVALID,
                        "non-existent: " + schematic);
                }
                if (!Permissions.hasPermission(player, CaptionUtility
                    .format(player, Captions.PERMISSION_CLAIM_SCHEMATIC.getTranslated(), schematic))
                    && !Permissions
                    .hasPermission(player, Captions.PERMISSION_ADMIN_COMMAND_SCHEMATIC) && !force) {
                    return sendMessage(player, Captions.NO_SCHEMATIC_PERMISSION, schematic);
                }
            }
        }
        if ((this.econHandler != null) && area.useEconomy() && !force) {
            Expression<Double> costExr = area.getPrices().get("claim");
            double cost = costExr.evaluate((double) currentPlots);
            if (cost > 0d) {
                if (this.econHandler.getMoney(player) < cost) {
                    return sendMessage(player, Captions.CANNOT_AFFORD_PLOT, "" + cost);
                }
                this.econHandler.withdrawMoney(player, cost);
                sendMessage(player, Captions.REMOVED_BALANCE, cost + "");
            }
        }
        if (grants > 0) {
            if (grants == 1) {
                player.removePersistentMeta("grantedPlots");
            } else {
                player.setPersistentMeta("grantedPlots", Ints.toByteArray(grants - 1));
            }
            sendMessage(player, Captions.REMOVED_GRANTED_PLOT, "1", (grants - 1));
        }
        int border = area.getBorder();
        if (border != Integer.MAX_VALUE && plot.getDistanceFromOrigin() > border && !force) {
            return !sendMessage(player, Captions.BORDER);
        }
        plot.setOwnerAbs(player.getUUID());
        final String finalSchematic = schematic;
        DBFunc.createPlotSafe(plot, () -> {
            try {
                TaskManager.getPlatformImplementation().sync(() -> {
                    if (!plot.claim(player, true, finalSchematic, false)) {
                        logger.info(Captions.PREFIX.getTranslated() + String
                            .format("Failed to claim plot %s", plot.getId().toCommaSeparatedString()));
                        sendMessage(player, Captions.PLOT_NOT_CLAIMED);
                        plot.setOwnerAbs(null);
                    } else if (area.isAutoMerge()) {
                        PlotMergeEvent mergeEvent = Claim.this.eventDispatcher
                            .callMerge(plot, Direction.ALL, Integer.MAX_VALUE, player);
                        if (mergeEvent.getEventResult() == Result.DENY) {
                            player.sendMessage(
                    TranslatableCaption.of("events.event_denied"),
                    Template.of("value", "Auto merge on claim"));
                        } else {
                            plot.autoMerge(mergeEvent.getDir(), mergeEvent.getMax(), player.getUUID(), true);
                        }
                    }
                    return null;
                });
            } catch (final Exception e) {
                e.printStackTrace();
            }
        }, () -> {
            logger.info(Captions.PREFIX.getTranslated() + String
                .format("Failed to add plot %s to the database",
                    plot.getId().toCommaSeparatedString()));
            sendMessage(player, Captions.PLOT_NOT_CLAIMED);
            plot.setOwnerAbs(null);
        });
        return true;
    }
}
