/*
 * PlotSquared, a land and world management plugin for Minecraft.
 * Copyright (C) IntellectualSites <https://intellectualsites.com>
 * Copyright (C) IntellectualSites team and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.plotsquared.core.command;

import cloud.commandframework.services.ServicePipeline;
import com.google.inject.Inject;
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
import com.plotsquared.core.services.plots.AutoQuery;
import com.plotsquared.core.services.plots.AutoService;
import com.plotsquared.core.util.EconHandler;
import com.plotsquared.core.util.EventDispatcher;
import com.plotsquared.core.util.PlotExpression;
import com.plotsquared.core.util.task.AutoClaimFinishTask;
import com.plotsquared.core.util.task.RunnableVal;
import com.plotsquared.core.util.task.TaskManager;
import io.leangen.geantyref.TypeToken;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

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

    @Inject
    public Auto(
            final @NonNull PlotAreaManager plotAreaManager,
            final @NonNull EventDispatcher eventDispatcher,
            final @NonNull EconHandler econHandler,
            final @NonNull ServicePipeline servicePipeline
    ) {
        this.plotAreaManager = plotAreaManager;
        this.eventDispatcher = eventDispatcher;
        this.econHandler = econHandler;
        this.servicePipeline = servicePipeline;
        this.servicePipeline.registerServiceType(TypeToken.get(AutoService.class), new AutoService.DefaultAutoService());
        final AutoService.MultiPlotService multiPlotService = new AutoService.MultiPlotService();
        this.servicePipeline.registerServiceImplementation(AutoService.class, multiPlotService,
                Collections.singletonList(multiPlotService)
        );
        final AutoService.SinglePlotService singlePlotService = new AutoService.SinglePlotService();
        this.servicePipeline.registerServiceImplementation(AutoService.class, singlePlotService,
                Collections.singletonList(singlePlotService)
        );
    }

    public static boolean checkAllowedPlots(
            PlotPlayer<?> player, PlotArea plotarea,
            @Nullable Integer allowedPlots, int sizeX, int sizeZ
    ) {
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
                        player.sendMessage(
                                TranslatableCaption.of("permission.cant_claim_more_plots"),
                                TagResolver.resolver("amount", Tag.inserting(Component.text(diff + grantedPlots)))
                        );
                        return false;
                    } else if (diff >= 0 && grantedPlots + diff < sizeX * sizeZ) {
                        player.sendMessage(
                                TranslatableCaption.of("permission.cant_claim_more_plots"),
                                TagResolver.resolver("amount", Tag.inserting(Component.text(diff + grantedPlots)))
                        );
                        return false;
                    } else {
                        int left = grantedPlots + diff < 0 ? 0 : diff - sizeX * sizeZ;
                        if (left == 0) {
                            metaDataAccess.remove();
                        } else {
                            metaDataAccess.set(left);
                        }
                        player.sendMessage(
                                TranslatableCaption.of("economy.removed_granted_plot"),
                                TagResolver.builder()
                                        .tag("used_grants", Tag.inserting(Component.text(grantedPlots - left)))
                                        .tag("remaining_grants", Tag.inserting(Component.text(left)))
                                        .build()
                        );
                    }
                } else {
                    player.sendMessage(
                            TranslatableCaption.of("permission.cant_claim_more_plots"),
                            TagResolver.resolver("amount", Tag.inserting(Component.text(player.getAllowedPlots())))
                    );
                    return false;
                }
            }
        }
        return true;
    }

    private void claimSingle(
            final @NonNull PlotPlayer<?> player, final @NonNull Plot plot,
            final @NonNull PlotArea plotArea, final @Nullable String schematic
    ) {
        try (final MetaDataAccess<Boolean> metaDataAccess =
                     player.accessTemporaryMetaData(PlayerMetaDataKeys.TEMPORARY_AUTO)) {
            metaDataAccess.set(true);
        }
        plot.setOwnerAbs(player.getUUID());

        final RunnableVal<Plot> runnableVal = new RunnableVal<>() {
            {
                this.value = plot;
            }

            @Override
            public void run(final Plot plot) {
                try {
                    TaskManager.getPlatformImplementation().sync(
                            new AutoClaimFinishTask(player, plot, plotArea, schematic,
                                    PlotSquared.get().getEventDispatcher()
                            ));
                } catch (final Exception e) {
                    e.printStackTrace();
                }
            }
        };

        DBFunc.createPlotSafe(plot, runnableVal, () -> claimSingle(player, plot, plotArea, schematic));

    }

    @Override
    public boolean onCommand(final PlotPlayer<?> player, String[] args) {
        PlotArea plotarea = player.getApplicablePlotArea();
        if (plotarea == null) {
            final PermissionHandler permissionHandler = PlotSquared.platform().permissionHandler();
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
        int sizeX = 1;
        int sizeZ = 1;
        String schematic = null;
        boolean mega = false;
        if (args.length > 0) {
            try {
                String[] split = args[0].split("[,;]");
                if (split.length == 2) {
                    sizeX = Integer.parseInt(split[0]);
                    sizeZ = Integer.parseInt(split[1]);
                } else {
                    player.sendMessage(
                            TranslatableCaption.of("commandconfig.command_syntax"),
                            TagResolver.resolver("value", Tag.inserting(Component.text(getUsage())))
                    );
                    return true;
                }
                if (sizeX < 1 || sizeZ < 1) {
                    player.sendMessage(TranslatableCaption.of("error.plot_size_negative"));
                    return true;
                }
                if (args.length > 1) {
                    schematic = args[1];
                }
                mega = true;
            } catch (NumberFormatException ignored) {
                sizeX = 1;
                sizeZ = 1;
                schematic = args[0];
            }
        }
        PlayerAutoPlotEvent event = this.eventDispatcher
                .callAuto(player, plotarea, schematic, sizeX, sizeZ);
        if (event.getEventResult() == Result.DENY) {
            player.sendMessage(
                    TranslatableCaption.of("events.event_denied"),
                    TagResolver.resolver("value", Tag.inserting(Component.text("Auto claim")))
            );
            return true;
        }
        boolean force = event.getEventResult() == Result.FORCE;
        sizeX = event.getSizeX();
        sizeZ = event.getSizeZ();
        schematic = event.getSchematic();
        if (!force && mega && !player.hasPermission(Permission.PERMISSION_AUTO_MEGA)) {
            player.sendMessage(
                    TranslatableCaption.of("permission.no_permission"),
                    TagResolver.resolver("node", Tag.inserting(Permission.PERMISSION_AUTO_MEGA))
            );
            return false;
        }
        if (!force && sizeX * sizeZ > Settings.Claim.MAX_AUTO_AREA) {
            player.sendMessage(
                    TranslatableCaption.of("permission.cant_claim_more_plots_num"),
                    TagResolver.resolver("amount", Tag.inserting(Component.text(Settings.Claim.MAX_AUTO_AREA)))
            );
            return false;
        }
        final int allowed_plots = player.getAllowedPlots();
        try (final MetaDataAccess<Boolean> metaDataAccess =
                     player.accessTemporaryMetaData(PlayerMetaDataKeys.TEMPORARY_AUTO)) {
            if (!force && (metaDataAccess.get().orElse(false) || !checkAllowedPlots(player,
                    plotarea, allowed_plots, sizeX, sizeZ
            ))) {
                return false;
            }
        }

        if (schematic != null && !schematic.isEmpty()) {
            if (!plotarea.hasSchematic(schematic)) {
                player.sendMessage(
                        TranslatableCaption.of("schematics.schematic_invalid_named"),
                        TagResolver.builder()
                                .tag("schemname", Tag.inserting(Component.text(schematic)))
                                .tag("reason", Tag.inserting(Component.text("non-existent")))
                                .build()
                );
                return true;
            }
            if (!force && !player.hasPermission(
                    Permission.PERMISSION_CLAIM_SCHEMATIC.format(schematic)
            ) && !player.hasPermission("plots.admin.command.schematic")) {
                player.sendMessage(
                        TranslatableCaption.of("permission.no_permission"),
                        TagResolver.resolver("node", Tag.inserting(Component.text("plots.claim.%s0")))
                );
                return true;
            }
        }
        if (this.econHandler != null && plotarea.useEconomy() && !player.hasPermission(Permission.PERMISSION_ADMIN_BYPASS_ECON)) {
            PlotExpression costExp = plotarea.getPrices().get("claim");
            PlotExpression mergeCostExp = plotarea.getPrices().get("merge");
            int size = sizeX * sizeZ;
            double mergeCost = size <= 1 || mergeCostExp == null ? 0d : mergeCostExp.evaluate(size);
            double cost = costExp.evaluate(Settings.Limit.GLOBAL ?
                    player.getPlotCount() :
                    player.getPlotCount(plotarea.getWorldName()));
            cost = size * cost + mergeCost;
            if (cost > 0d) {
                if (!this.econHandler.isSupported()) {
                    player.sendMessage(TranslatableCaption.of("economy.vault_or_consumer_null"));
                    return false;
                }
                if (!force && this.econHandler.getMoney(player) < cost) {
                    player.sendMessage(
                            TranslatableCaption.of("economy.cannot_afford_plot"),
                            TagResolver.builder()
                                    .tag("money", Tag.inserting(Component.text(this.econHandler.format(cost))))
                                    .tag(
                                            "balance",
                                            Tag.inserting(Component.text(this.econHandler.format(this.econHandler.getMoney(player))))
                                    )
                                    .build()
                    );
                    return false;
                }
                this.econHandler.withdrawMoney(player, cost);
                player.sendMessage(
                        TranslatableCaption.of("economy.removed_balance"),
                        TagResolver.resolver("money", Tag.inserting(Component.text(this.econHandler.format(cost))))
                );
            }
        }

        List<Plot> plots = this.servicePipeline
                .pump(new AutoQuery(player, null, sizeX, sizeZ, plotarea))
                .through(AutoService.class)
                .getResult();

        plots = this.eventDispatcher.callAutoPlotsChosen(player, plots).getPlots();

        if (plots.isEmpty()) {
            player.sendMessage(TranslatableCaption.of("errors.no_free_plots"));
            return false;
        } else if (plots.size() == 1) {
            this.claimSingle(player, plots.get(0), plotarea, schematic);
        } else {
            final Iterator<Plot> plotIterator = plots.iterator();
            while (plotIterator.hasNext()) {
                Plot plot = plotIterator.next();
                if (!plot.canClaim(player)) {
                    continue;
                }
                plot.claim(player, !plotIterator.hasNext(), null, true, true);
                eventDispatcher.callPostAuto(player, plot);
            }
            final PlotAutoMergeEvent mergeEvent = this.eventDispatcher.callAutoMerge(
                    plots.get(0),
                    plots.stream().map(Plot::getId).collect(Collectors.toList())
            );
            if (!force && mergeEvent.getEventResult() == Result.DENY) {
                player.sendMessage(
                        TranslatableCaption.of("events.event_denied"),
                        TagResolver.resolver("value", Tag.inserting(Component.text("Auto merge")))
                );
                return false;
            }
            return plotarea.mergePlots(mergeEvent.getPlots(), true);
        }
        return true;
    }

}
