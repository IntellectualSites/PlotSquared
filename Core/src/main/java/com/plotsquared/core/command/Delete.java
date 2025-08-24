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
import com.plotsquared.core.events.Result;
import com.plotsquared.core.events.TeleportCause;
import com.plotsquared.core.permissions.Permission;
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
import org.checkerframework.checker.nullness.qual.NonNull;


@CommandDeclaration(command = "delete",
        permission = "plots.delete",
        usage = "/plot delete",
        aliases = {"dispose", "del"},
        category = CommandCategory.CLAIMING,
        requiredType = RequiredType.NONE,
        confirmation = true)
public class Delete extends SubCommand {

    private final EventDispatcher eventDispatcher;
    private final EconHandler econHandler;

    @Inject
    public Delete(
            final @NonNull EventDispatcher eventDispatcher,
            final @NonNull EconHandler econHandler
    ) {
        this.eventDispatcher = eventDispatcher;
        this.econHandler = econHandler;
    }

    @Override
    public boolean onCommand(final PlotPlayer<?> player, String[] args) {
        final Plot plot = player.getCurrentPlot();
        if (plot == null) {
            player.sendMessage(TranslatableCaption.of("errors.not_in_plot"));
            return false;
        }
        if (!plot.hasOwner()) {
            player.sendMessage(TranslatableCaption.of("info.plot_unowned"));
            return false;
        }
        if (plot.getVolume() > Integer.MAX_VALUE) {
            player.sendMessage(TranslatableCaption.of("schematics.schematic_too_large"));
            return false;
        }
        Result eventResult = this.eventDispatcher.callDelete(plot).getEventResult();
        if (eventResult == Result.DENY) {
            player.sendMessage(
                    TranslatableCaption.of("events.event_denied"),
                    TagResolver.resolver("value", Tag.inserting(Component.text("Delete")))
            );
            return true;
        }
        boolean force = eventResult == Result.FORCE;
        if (!force && !plot.isOwner(player.getUUID()) && !player.hasPermission(Permission.PERMISSION_ADMIN_COMMAND_DELETE)) {
            player.sendMessage(TranslatableCaption.of("permission.no_plot_perms"));
            return false;
        }
        final PlotArea plotArea = plot.getArea();
        final java.util.Set<Plot> plots = plot.getConnectedPlots();
        final int currentPlots = Settings.Limit.GLOBAL ?
                player.getPlotCount() :
                player.getPlotCount(plot.getWorldName());
        Runnable run = () -> {
            if (plot.getRunning() > 0) {
                player.sendMessage(TranslatableCaption.of("errors.wait_for_timer"));
                return;
            }
            final long start = System.currentTimeMillis();
            if (Settings.Teleport.ON_DELETE) {
                plot.getPlayersInPlot().forEach(playerInPlot -> plot.teleportPlayer(playerInPlot, TeleportCause.COMMAND_DELETE,
                        result -> {
                        }
                ));
            }
            boolean result = plot.getPlotModificationManager().deletePlot(player, () -> {
                plot.removeRunning();
                if (this.econHandler.isEnabled(plotArea)) {
                    PlotExpression valueExr = plotArea.getPrices().get("sell");
                    double value = plots.size() * valueExr.evaluate(currentPlots);
                    if (value > 0d) {
                        this.econHandler.depositMoney(player, value);
                        player.sendMessage(
                                TranslatableCaption.of("economy.added_balance"),
                                TagResolver.resolver("money", Tag.inserting(Component.text(this.econHandler.format(value))))
                        );
                    }
                }
                player.sendMessage(
                        TranslatableCaption.of("working.deleting_done"),
                        TagResolver.resolver(
                                "amount",
                                Tag.inserting(Component.text(String.valueOf(System.currentTimeMillis() - start)))
                        ),
                        TagResolver.resolver("world", Tag.inserting(Component.text(plotArea.getWorldName()))),
                        TagResolver.resolver("plot", Tag.inserting(Component.text(plot.getId().toString())))
                );
                eventDispatcher.callPostDelete(plot);
            });
            if (result) {
                plot.addRunning();
            } else {
                player.sendMessage(TranslatableCaption.of("errors.wait_for_timer"));
            }
        };
        if (hasConfirmation(player)) {
            CmdConfirm.addPending(player, getCommandString() + ' ' + plot.getId(), run);
        } else {
            TaskManager.runTask(run);
        }
        return true;
    }

}
