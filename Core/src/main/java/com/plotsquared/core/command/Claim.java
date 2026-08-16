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

import com.google.inject.Inject;
import com.plotsquared.core.configuration.Settings;
import com.plotsquared.core.configuration.caption.TranslatableCaption;
import com.plotsquared.core.database.DBFunc;
import com.plotsquared.core.events.PlayerClaimPlotEvent;
import com.plotsquared.core.events.PlotMergeEvent;
import com.plotsquared.core.events.Result;
import com.plotsquared.core.location.Direction;
import com.plotsquared.core.permissions.Permission;
import com.plotsquared.core.player.MetaDataAccess;
import com.plotsquared.core.player.PlayerMetaDataKeys;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.PlotArea;
import com.plotsquared.core.util.EconHandler;
import com.plotsquared.core.util.EventDispatcher;
import com.plotsquared.core.util.PlotExpression;
import com.plotsquared.core.util.task.TaskManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
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
        Plot plot = player.getCurrentPlot();
        if (plot == null) {
            player.sendMessage(TranslatableCaption.of("errors.not_in_plot"));
            return false;
        }
        final PlayerClaimPlotEvent event = this.eventDispatcher.callClaim(player, plot, schematic);
        schematic = event.getSchematic();
        if (event.getEventResult() == Result.DENY) {
            player.sendMessage(
                    TranslatableCaption.of("events.event_denied"),
                    TagResolver.resolver("value", Tag.inserting(Component.text("Claim")))
            );
            return true;
        }
        boolean force = event.getEventResult() == Result.FORCE;
        int currentPlots = Settings.Limit.GLOBAL ?
                player.getPlotCount() :
                player.getPlotCount(plot.getWorldName());

        final PlotArea area = plot.getArea();

        try (final MetaDataAccess<Integer> metaDataAccess = player.accessPersistentMetaData(PlayerMetaDataKeys.PERSISTENT_GRANTED_PLOTS)) {
            int grants = 0;
            if (currentPlots >= player.getAllowedPlots() && !force) {
                if (metaDataAccess.isPresent()) {
                    grants = metaDataAccess.get().orElse(0);
                    if (grants <= 0) {
                        player.sendMessage(
                                TranslatableCaption.of("permission.cant_claim_more_plots"),
                                TagResolver.resolver("amount", Tag.inserting(Component.text(grants)))
                        );
                        metaDataAccess.remove();
                    }
                } else {
                    player.sendMessage(
                            TranslatableCaption.of("permission.cant_claim_more_plots"),
                            TagResolver.resolver("amount", Tag.inserting(Component.text(player.getAllowedPlots())))
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
                                TagResolver.builder()
                                        .tag("schemname", Tag.inserting(Component.text(schematic)))
                                        .tag("reason", Tag.inserting(Component.text("non-existent")))
                                        .build()
                        );
                    }
                    if (!player.hasPermission(Permission.PERMISSION_CLAIM_SCHEMATIC
                            .format(schematic)) && !player.hasPermission(
                            "plots.admin.command.schematic"
                    ) && !force) {
                        player.sendMessage(
                                TranslatableCaption.of("permission.no_schematic_permission"),
                                TagResolver.resolver("value", Tag.inserting(Component.text(schematic)))
                        );
                    }
                }
            }
            if (this.econHandler.isEnabled(area) && !force && !player.hasPermission(Permission.PERMISSION_ADMIN_BYPASS_ECON)) {
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
                                TagResolver.builder()
                                        .tag("money", Tag.inserting(Component.text(this.econHandler.format(cost))))
                                        .tag(
                                                "balance",
                                                Tag.inserting(Component.text(this.econHandler.format(this.econHandler.getMoney(
                                                        player))))
                                        )
                                        .build()
                        );
                        return false;
                    }
                    this.econHandler.withdrawMoney(player, cost);
                    player.sendMessage(
                            TranslatableCaption.of("economy.removed_balance"),
                            TagResolver.builder()
                                    .tag("money", Tag.inserting(Component.text(this.econHandler.format(cost))))
                                    .tag(
                                            "balance",
                                            Tag.inserting(Component.text(this.econHandler.format(this.econHandler.getMoney(
                                                    player))))
                                    )
                                    .build()
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
                        TagResolver.builder()
                                .tag("used_grants", Tag.inserting(Component.text(grants - 1)))
                                .tag("remaining_grants", Tag.inserting(Component.text(grants)))
                                .build()
                );
            }
        }
        if (!player.hasPermission(Permission.PERMISSION_ADMIN_BYPASS_BORDER)) {
            int border = area.getBorder(false);
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
                                    TagResolver.resolver("value", Tag.inserting(Component.text("Auto merge on claim")))
                            );
                        } else {
                            if (plot.getPlotModificationManager().autoMerge(
                                    mergeEvent.getDir(),
                                    mergeEvent.getMax(),
                                    player.getUUID(),
                                    player,
                                    true
                            )) {
                                eventDispatcher.callPostMerge(player, plot);
                            }
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
