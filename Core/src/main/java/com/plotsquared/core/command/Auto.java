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

import com.google.common.reflect.TypeToken;
import com.google.inject.Inject;
import com.intellectualsites.services.ServicePipeline;
import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.configuration.Settings;
import com.plotsquared.core.configuration.caption.TranslatableCaption;
import com.plotsquared.core.database.DBFunc;
import com.plotsquared.core.events.PlayerAutoPlotEvent;
import com.plotsquared.core.events.PlotAutoMergeEvent;
import com.plotsquared.core.events.Result;
import com.plotsquared.core.permissions.Permission;
import com.plotsquared.core.permissions.PermissionHandler;
import com.plotsquared.core.player.MetaDataAccess;
import com.plotsquared.core.player.PlayerMetaDataKeys;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.PlotArea;
import com.plotsquared.core.plot.world.PlotAreaManager;
import com.plotsquared.core.services.plots.AutoService;
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
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

@CommandDeclaration(command = "auto",
    permission = "plots.auto",
    category = CommandCategory.CLAIMING,
    requiredType = RequiredType.NONE,
    aliases = "a",
    usage = "/plot auto [length, width]")
public class Auto extends SubCommand {

    private final PlotAreaManager plotAreaManager;
    private final EventDispatcher eventDispatcher;
    private final EconHandler econHandler;
    private final ServicePipeline servicePipeline;

    @Inject public Auto(@Nonnull final PlotAreaManager plotAreaManager,
                        @Nonnull final EventDispatcher eventDispatcher,
                        @Nonnull final EconHandler econHandler,
                        @Nonnull final ServicePipeline servicePipeline) {
        this.plotAreaManager = plotAreaManager;
        this.eventDispatcher = eventDispatcher;
        this.econHandler = econHandler;
        this.servicePipeline = servicePipeline;
        this.servicePipeline.registerServiceType(TypeToken.of(AutoService.class), new AutoService.DefaultAutoService());
        final AutoService.MultiPlotService multiPlotService = new AutoService.MultiPlotService();
        this.servicePipeline.registerServiceImplementation(AutoService.class, multiPlotService,
            Collections.singletonList(multiPlotService));
        final AutoService.SinglePlotService singlePlotService = new AutoService.SinglePlotService();
        this.servicePipeline.registerServiceImplementation(AutoService.class, singlePlotService,
            Collections.singletonList(singlePlotService));
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

    private void claimSingle(@Nonnull final PlotPlayer<?> player, @Nonnull final Plot plot,
        @Nonnull final PlotArea plotArea, @Nullable final String schematic) {
        try (final MetaDataAccess<Boolean> metaDataAccess =
            player.accessTemporaryMetaData(PlayerMetaDataKeys.TEMPORARY_AUTO)) {
            metaDataAccess.set(true);
        }
        plot.setOwnerAbs(player.getUUID());

        final RunnableVal<Plot> runnableVal = new RunnableVal<Plot>() {
            {
                this.value = plot;
            }

            @Override public void run(final Plot plot) {
                try {
                    TaskManager.getPlatformImplementation().sync(
                        new AutoClaimFinishTask(player, plot, plotArea, schematic,
                            PlotSquared.get().getEventDispatcher()));
                } catch (final Exception e) {
                    e.printStackTrace();
                }
            }
        };

        DBFunc.createPlotSafe(plot, runnableVal, () -> claimSingle(player, plot, plotArea, schematic));

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
        if (!force && mega && !Permissions.hasPermission(player, Permission.PERMISSION_AUTO_MEGA)) {
            player.sendMessage(TranslatableCaption.of("permission.no_permission"),
                    Template.of("node", Permission.PERMISSION_AUTO_MEGA));
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

        final List<Plot> plots = this.servicePipeline
            .pump(new AutoService.AutoQuery(player, null, size_x, size_z, plotarea))
            .through(AutoService.class)
            .getResult();

        if (plots.isEmpty()) {
            player.sendMessage(TranslatableCaption.of("errors.no_free_plots"));
            return false;
        } else if (plots.size() == 1) {
            this.claimSingle(player, plots.get(0), plotarea, schematic);
        } else {
            final Iterator<Plot> plotIterator = plots.iterator();
            while (plotIterator.hasNext()) {
                plotIterator.next().claim(player, !plotIterator.hasNext(), null);
            }
            final PlotAutoMergeEvent mergeEvent = this.eventDispatcher.callAutoMerge(plots.get(0),
                plots.stream().map(Plot::getId).collect(Collectors.toList()));
            if (!force && mergeEvent.getEventResult() == Result.DENY) {
                player.sendMessage(
                    TranslatableCaption.of("events.event_denied"),
                    Template.of("value", "Auto merge")
                );
                return false;
            }
            return plotarea.mergePlots(mergeEvent.getPlots(), true);
        }
        return true;
    }
}
