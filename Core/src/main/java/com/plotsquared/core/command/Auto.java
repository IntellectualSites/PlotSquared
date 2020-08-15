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
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.plotsquared.core.command;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.configuration.Settings;
import com.plotsquared.core.configuration.caption.TranslatableCaption;
import com.plotsquared.core.database.DBFunc;
import com.plotsquared.core.events.PlayerAutoPlotEvent;
import com.plotsquared.core.events.PlotAutoMergeEvent;
import com.plotsquared.core.events.Result;
import com.plotsquared.core.events.TeleportCause;
import com.plotsquared.core.permissions.Permission;
import com.plotsquared.core.permissions.PermissionHandler;
import com.plotsquared.core.player.MetaDataAccess;
import com.plotsquared.core.player.PlayerMetaDataKeys;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.PlotArea;
import com.plotsquared.core.plot.PlotAreaType;
import com.plotsquared.core.plot.PlotId;
import com.plotsquared.core.plot.world.PlotAreaManager;
import com.plotsquared.core.util.EconHandler;
import com.plotsquared.core.util.EventDispatcher;
import com.plotsquared.core.util.Expression;
import com.plotsquared.core.util.Permissions;
import com.plotsquared.core.util.task.AutoClaimFinishTask;
import com.plotsquared.core.util.task.RunnableVal;
import com.plotsquared.core.util.task.TaskManager;
import net.kyori.adventure.text.minimessage.Template;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;

@CommandDeclaration(command = "auto",
    permission = "plots.auto",
    category = CommandCategory.CLAIMING,
    requiredType = RequiredType.NONE,
    description = "Claim the nearest plot",
    aliases = "a",
    usage = "/plot auto [length, width]")
public class Auto extends SubCommand {

    private final PlotAreaManager plotAreaManager;
    private final EventDispatcher eventDispatcher;
    private final EconHandler econHandler;

    @Inject public Auto(@Nonnull final PlotAreaManager plotAreaManager,
                        @Nonnull final EventDispatcher eventDispatcher,
                        @Nullable final EconHandler econHandler) {
        this.plotAreaManager = plotAreaManager;
        this.eventDispatcher = eventDispatcher;
        this.econHandler = econHandler;
    }

    public static boolean checkAllowedPlots(PlotPlayer player, PlotArea plotarea,
        @Nullable Integer allowedPlots, int sizeX, int sizeZ) {
        if (allowedPlots == null) {
            allowedPlots = player.getAllowedPlots();
        }
        int currentPlots;
        if (Settings.Limit.GLOBAL) {
            currentPlots = player.getPlotCount();
        } else {
            currentPlots = player.getPlotCount(plotarea.getWorldName());
        }
        int diff = allowedPlots - currentPlots;
        if (diff - sizeX * sizeZ < 0) {
            try (final MetaDataAccess<Integer> metaDataAccess = player.accessPersistentMetaData(
                PlayerMetaDataKeys.PERSISTENT_GRANTED_PLOTS)) {
                if (metaDataAccess.isPresent()) {
                    int grantedPlots = metaDataAccess.get().orElse(0);
                    if (diff < 0 && grantedPlots < sizeX * sizeZ) {
                        player.sendMessage(TranslatableCaption.of("permission.cant_claim_more_plots"));
                        return false;
                    } else if (diff >= 0 && grantedPlots + diff < sizeX * sizeZ) {
                        player.sendMessage(TranslatableCaption.of("permission.cant_claim_more_plots"));
                        return false;
                    } else {
                        int left = grantedPlots + diff < 0 ? 0 : diff - sizeX * sizeZ;
                        if (left == 0) {
                            metaDataAccess.remove();
                        } else {
                            metaDataAccess.set(left);
                        }
                        player.sendMessage(TranslatableCaption.of("economy.removed_granted_plot"),
                            Template.of("usedGrants", String.valueOf(grantedPlots - left)),
                            Template.of("remainingGrants", String.valueOf(left)));
                    }
                } else {
                    player.sendMessage(TranslatableCaption.of("permission.cant_claim_more_plots"));
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Teleport the player home, or claim a new plot
     *
     * @param player
     * @param area
     * @param start
     * @param schematic
     */
    public static void homeOrAuto(final PlotPlayer player, final PlotArea area, PlotId start,
        final String schematic) {
        Set<Plot> plots = player.getPlots();
        if (!plots.isEmpty()) {
            plots.iterator().next().teleportPlayer(player, TeleportCause.COMMAND, result -> {
            });
        } else {
            autoClaimSafe(player, area, start, schematic);
        }
    }

    /**
     * Claim a new plot for a player
     *
     * @param player
     * @param area
     * @param start
     * @param schematic
     */
    public static void autoClaimSafe(final PlotPlayer<?> player, final PlotArea area, PlotId start,
        final String schematic) {
        try (final MetaDataAccess<Boolean> metaDataAccess =
            player.accessTemporaryMetaData(PlayerMetaDataKeys.TEMPORARY_AUTO)) {
            metaDataAccess.set(true);
        }
        autoClaimFromDatabase(player, area, start, new RunnableVal<Plot>() {
            @Override public void run(final Plot plot) {
                try {
                    TaskManager.getPlatformImplementation().sync(new AutoClaimFinishTask(player, plot, area, schematic,
                        PlotSquared.get().getEventDispatcher()));
                } catch (final Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public static void autoClaimFromDatabase(final PlotPlayer player, final PlotArea area,
        PlotId start, final RunnableVal<Plot> whenDone) {
        final Plot plot = area.getNextFreePlot(player, start);
        if (plot == null) {
            whenDone.run(null);
            return;
        }
        whenDone.value = plot;
        plot.setOwnerAbs(player.getUUID());
        DBFunc.createPlotSafe(plot, whenDone,
            () -> autoClaimFromDatabase(player, area, plot.getId(), whenDone));
    }

    @Override public boolean onCommand(final PlotPlayer<?> player, String[] args) {
        PlotArea plotarea = player.getApplicablePlotArea();
        if (plotarea == null) {
            final PermissionHandler permissionHandler = PlotSquared.platform().getPermissionHandler();
            if (permissionHandler.hasCapability(
                PermissionHandler.PermissionHandlerCapability.PER_WORLD_PERMISSIONS)) {
                for (final PlotArea area : this.plotAreaManager.getAllPlotAreas()) {
                    if (player.hasPermission(area.getWorldName(), "plots.auto")) {
                        if (plotarea != null) {
                            plotarea = null;
                            break;
                        }
                        plotarea = area;
                    }
                }
            }
            if (this.plotAreaManager.getAllPlotAreas().length == 1) {
                plotarea = this.plotAreaManager.getAllPlotAreas()[0];
            }
            if (plotarea == null) {
                player.sendMessage(TranslatableCaption.of("errors.not_in_plot_world"));
                return false;
            }
        }
        int size_x = 1;
        int size_z = 1;
        String schematic = null;
        boolean mega = false;
        if (args.length > 0) {
            try {
                String[] split = args[0].split(",|;");
                switch (split.length) {
                    case 1:
                        size_x = 1;
                        size_z = 1;
                        break;
                    case 2:
                        size_x = Integer.parseInt(split[0]);
                        size_z = Integer.parseInt(split[1]);
                        break;
                    default:
                        player.sendMessage(
                                TranslatableCaption.of("commandconfig.command_syntax"),
                                Template.of("value", getUsage())
                        );
                        return true;
                }
                if (size_x < 1 || size_z < 1) {
                    player.sendMessage(TranslatableCaption.of("error.plot_size"));
                }
                if (args.length > 1) {
                    schematic = args[1];
                }
                mega = true;
            } catch (NumberFormatException ignored) {
                size_x = 1;
                size_z = 1;
                schematic = args[0];
                // PlayerFunctions.sendMessage(plr,
                // "&cError: Invalid size (X,Y)");
                // return false;
            }
        }
        PlayerAutoPlotEvent event = this.eventDispatcher
            .callAuto(player, plotarea, schematic, size_x, size_z);
        if (event.getEventResult() == Result.DENY) {
            player.sendMessage(
                    TranslatableCaption.of("events.event_denied"),
                    Template.of("value", "Auto claim"));
            return true;
        }
        boolean force = event.getEventResult() == Result.FORCE;
        size_x = event.getSize_x();
        size_z = event.getSize_z();
        schematic = event.getSchematic();
        if (!force && mega && !Permissions.hasPermission(player, "plots.auto.mega")) {
            player.sendMessage(TranslatableCaption.of("permission.no_permission"),
                    Template.of("node", "plots.auto.mega"));
        }
        if (!force && size_x * size_z > Settings.Claim.MAX_AUTO_AREA) {
            player.sendMessage(TranslatableCaption.of("permission.cant_claim_more_plots_num"),
                    Template.of("amount", String.valueOf(Settings.Claim.MAX_AUTO_AREA)));
            return false;
        }
        final int allowed_plots = player.getAllowedPlots();
        try (final MetaDataAccess<Boolean> metaDataAccess =
            player.accessTemporaryMetaData(PlayerMetaDataKeys.TEMPORARY_AUTO)) {
            if (!force && (metaDataAccess.get().orElse(false) || !checkAllowedPlots(player,
                plotarea, allowed_plots, size_x, size_z))) {
                return false;
            }
        }

        if (schematic != null && !schematic.isEmpty()) {
            if (!plotarea.hasSchematic(schematic)) {
                player.sendMessage(
                        TranslatableCaption.of("schematics.schematic_invalid_named"),
                        Template.of("schemname", schematic),
                        Template.of("reason", "non-existant")
                );
                return true;
            }
            if (!force && !Permissions.hasPermission(player, Permission.PERMISSION_CLAIM_SCHEMATIC.format(schematic)) && !Permissions
                .hasPermission(player, "plots.admin.command.schematic")) {
                player.sendMessage(
                        TranslatableCaption.of("permission.no_permission"),
                        Template.of("node", "plots.claim.%s0")
                );
                return true;
            }
        }
        if (this.econHandler != null && plotarea.useEconomy()) {
            Expression<Double> costExp = plotarea.getPrices().get("claim");
            double cost = costExp.evaluate((double) (Settings.Limit.GLOBAL ?
                player.getPlotCount() :
                player.getPlotCount(plotarea.getWorldName())));
            cost = (size_x * size_z) * cost;
            if (cost > 0d) {
                if (!force && this.econHandler.getMoney(player) < cost) {
                    player.sendMessage(
                            TranslatableCaption.of("economy.cannot_afford_plot"),
                            Template.of("money", String.valueOf(cost))
                    );
                    return true;
                }
                this.econHandler.withdrawMoney(player, cost);
                player.sendMessage(
                        TranslatableCaption.of("economy.removed_balance"),
                        Template.of("money", String.valueOf(cost))
                );
            }
        }
        // TODO handle type 2 (partial) the same as normal worlds!
        if (size_x == 1 && size_z == 1) {
            autoClaimSafe(player, plotarea, null, schematic);
            return true;
        } else {
            if (plotarea.getType() == PlotAreaType.PARTIAL) {
                player.sendMessage(TranslatableCaption.of("errors.no_free_plots"));
                return false;
            }
            while (true) {
                PlotId start = plotarea.getMeta("lastPlot", PlotId.of(0, 0)).getNextId();
                PlotId end = PlotId.of(start.getX() + size_x - 1, start.getY() + size_z - 1);
                if (plotarea.canClaim(player, start, end)) {
                    plotarea.setMeta("lastPlot", start);

                    for (final PlotId plotId : PlotId.PlotRangeIterator.range(start, end)) {
                        final Plot plot = plotarea.getPlot(plotId);
                        if (plot == null) {
                            return false;
                        }
                        plot.claim(player, plotId.equals(end), null);
                    }

                    final List<PlotId> plotIds = Lists.newArrayList((Iterable<? extends PlotId>)
                        PlotId.PlotRangeIterator.range(start, end));
                    final PlotId pos1 = plotIds.get(0);
                    final PlotAutoMergeEvent mergeEvent = this.eventDispatcher
                        .callAutoMerge(plotarea.getPlotAbs(pos1), plotIds);
                    if (!force && mergeEvent.getEventResult() == Result.DENY) {
                        player.sendMessage(
                                TranslatableCaption.of("events.event_denied"),
                                Template.of("value", "Auto merge")
                        );
                        return false;
                    }
                    if (!plotarea.mergePlots(mergeEvent.getPlots(), true)) {
                        return false;
                    }
                    break;
                }
                plotarea.setMeta("lastPlot", start);
            }
            return true;
        }
    }
}
