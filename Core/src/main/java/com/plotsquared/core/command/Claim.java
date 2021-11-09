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
 *                  Copyright (C) 2021 IntellectualSites
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

import com.google.inject.Inject;
import com.plotsquared.core.configuration.Settings;
import com.plotsquared.core.configuration.caption.TranslatableCaption;
import com.plotsquared.core.database.DBFunc;
import com.plotsquared.core.events.PlayerClaimPlotEvent;
import com.plotsquared.core.events.PlotMergeEvent;
import com.plotsquared.core.events.Result;
import com.plotsquared.core.location.Direction;
import com.plotsquared.core.location.Location;
import com.plotsquared.core.permissions.Permission;
import com.plotsquared.core.player.MetaDataAccess;
import com.plotsquared.core.player.PlayerMetaDataKeys;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.PlotArea;
import com.plotsquared.core.util.EconHandler;
import com.plotsquared.core.util.EventDispatcher;
import com.plotsquared.core.util.Permissions;
import com.plotsquared.core.util.PlotExpression;
import com.plotsquared.core.util.task.TaskManager;
import net.kyori.adventure.text.minimessage.Template;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.nullness.qual.NonNull;

@CommandDeclaration(
        command = "claim",
        aliases = "c",
        category = CommandCategory.CLAIMING,
        requiredType = RequiredType.PLAYER, permission = "plots.claim",
        usage = "/plot claim")
public class Claim extends SubCommand {

    private static final Logger LOGGER = LogManager.getLogger("PlotSquared/" + Claim.class.getSimpleName());

    private final EventDispatcher eventDispatcher;
    private final EconHandler econHandler;

    @Inject
    public Claim(
            final @NonNull EventDispatcher eventDispatcher,
            final @NonNull EconHandler econHandler
    ) {
        this.eventDispatcher = eventDispatcher;
        this.econHandler = econHandler;
    }

    @Override
    public boolean onCommand(final PlotPlayer<?> player, String[] args) {
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
                    Template.of("value", "Claim")
            );
            return true;
        }
        boolean force = event.getEventResult() == Result.FORCE;
        int currentPlots = Settings.Limit.GLOBAL ?
                player.getPlotCount() :
                player.getPlotCount(location.getWorldName());

        final PlotArea area = plot.getArea();

        try (final MetaDataAccess<Integer> metaDataAccess = player.accessPersistentMetaData(PlayerMetaDataKeys.PERSISTENT_GRANTED_PLOTS)) {
            int grants = 0;
            if (currentPlots >= player.getAllowedPlots() && !force) {
                if (metaDataAccess.isPresent()) {
                    grants = metaDataAccess.get().orElse(0);
                    if (grants <= 0) {
                        player.sendMessage(
                                TranslatableCaption.of("permission.cant_claim_more_plots"),
                                Template.of("amount", String.valueOf(grants))
                        );
                        metaDataAccess.remove();
                    }
                } else {
                    player.sendMessage(
                            TranslatableCaption.of("permission.cant_claim_more_plots"),
                            Template.of("amount", String.valueOf(player.getAllowedPlots()))
                    );
                    return false;
                }
            }

            if (!plot.canClaim(player)) {
                player.sendMessage(TranslatableCaption.of("working.plot_is_claimed"));
                return false;
            }
            if (schematic != null && !schematic.isEmpty()) {
                if (area.isSchematicClaimSpecify()) {
                    if (!area.hasSchematic(schematic)) {
                        player.sendMessage(
                                TranslatableCaption.of("schematics.schematic_invalid_named"),
                                Template.of("schemname", schematic),
                                Template.of("reason", "non-existent")
                        );
                    }
                    if (!Permissions.hasPermission(player, Permission.PERMISSION_CLAIM_SCHEMATIC
                            .format(schematic)) && !Permissions.hasPermission(
                            player,
                            "plots.admin.command.schematic"
                    ) && !force) {
                        player.sendMessage(
                                TranslatableCaption.of("permission.no_schematic_permission"),
                                Template.of("value", schematic)
                        );
                    }
                }
            }
            if (this.econHandler.isEnabled(area) && !force) {
                PlotExpression costExr = area.getPrices().get("claim");
                double cost = costExr.evaluate(currentPlots);
                if (cost > 0d) {
                    if (!this.econHandler.isSupported()) {
                        player.sendMessage(TranslatableCaption.of("economy.vault_or_consumer_null"));
                        return false;
                    }
                    if (this.econHandler.getMoney(player) < cost) {
                        player.sendMessage(
                                TranslatableCaption.of("economy.cannot_afford_plot"),
                                Template.of("money", this.econHandler.format(cost)),
                                Template.of("balance", this.econHandler.format(this.econHandler.getMoney(player)))
                        );
                        return false;
                    }
                    this.econHandler.withdrawMoney(player, cost);
                    player.sendMessage(
                            TranslatableCaption.of("economy.removed_balance"),
                            Template.of("money", this.econHandler.format(cost)),
                            Template.of("balance", this.econHandler.format(this.econHandler.getMoney(player)))
                    );
                }
            }
            if (grants > 0) {
                if (grants == 1) {
                    metaDataAccess.remove();
                } else {
                    metaDataAccess.set(grants - 1);
                }
                player.sendMessage(
                        TranslatableCaption.of("economy.removed_granted_plot"),
                        Template.of("usedGrants", String.valueOf((grants - 1))),
                        Template.of("remainingGrants", String.valueOf(grants))
                );
            }
        }
        if (!Permissions.hasPermission(player, Permission.PERMISSION_ADMIN_BYPASS_BORDER)) {
            int border = area.getBorder();
            if (border != Integer.MAX_VALUE && plot.getDistanceFromOrigin() > border && !force) {
                player.sendMessage(TranslatableCaption.of("border.denied"));
                return false;
            }
        }
        plot.setOwnerAbs(player.getUUID());
        final String finalSchematic = schematic;
        DBFunc.createPlotSafe(plot, () -> {
            try {
                TaskManager.getPlatformImplementation().sync(() -> {
                    if (!plot.claim(player, true, finalSchematic, false, false)) {
                        LOGGER.info("Failed to claim plot {}", plot.getId().toCommaSeparatedString());
                        player.sendMessage(TranslatableCaption.of("working.plot_not_claimed"));
                        plot.setOwnerAbs(null);
                    } else if (area.isAutoMerge()) {
                        PlotMergeEvent mergeEvent = Claim.this.eventDispatcher
                                .callMerge(plot, Direction.ALL, Integer.MAX_VALUE, player);
                        if (mergeEvent.getEventResult() == Result.DENY) {
                            player.sendMessage(
                                    TranslatableCaption.of("events.event_denied"),
                                    Template.of("value", "Auto merge on claim")
                            );
                        } else {
                            plot.getPlotModificationManager().autoMerge(
                                    mergeEvent.getDir(),
                                    mergeEvent.getMax(),
                                    player.getUUID(),
                                    player,
                                    true
                            );
                        }
                    }
                    return null;
                });
            } catch (final Exception e) {
                e.printStackTrace();
            }
        }, () -> {
            LOGGER.info("Failed to add plot to database: {}", plot.getId().toCommaSeparatedString());
            player.sendMessage(TranslatableCaption.of("working.plot_not_claimed"));
            plot.setOwnerAbs(null);
        });
        return true;
    }

}
