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
import com.plotsquared.core.configuration.caption.TranslatableCaption;
import com.plotsquared.core.events.PlotUnlinkEvent;
import com.plotsquared.core.events.Result;
import com.plotsquared.core.location.Location;
import com.plotsquared.core.permissions.Permission;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.util.EventDispatcher;
import com.plotsquared.core.util.Permissions;
import com.plotsquared.core.util.StringMan;
import com.plotsquared.core.util.task.TaskManager;
import net.kyori.adventure.text.minimessage.placeholder.Placeholder;
import org.checkerframework.checker.nullness.qual.NonNull;

@CommandDeclaration(command = "unlink",
        aliases = {"u", "unmerge"},
        usage = "/plot unlink [createroads]",
        requiredType = RequiredType.PLAYER,
        category = CommandCategory.SETTINGS,
        confirmation = true)
public class Unlink extends SubCommand {

    private final EventDispatcher eventDispatcher;

    @Inject
    public Unlink(final @NonNull EventDispatcher eventDispatcher) {
        this.eventDispatcher = eventDispatcher;
    }

    @Override
    public boolean onCommand(final PlotPlayer<?> player, String[] args) {
        Location location = player.getLocation();
        final Plot plot = location.getPlotAbs();
        if (plot == null) {
            player.sendMessage(TranslatableCaption.miniMessage("errors.not_in_plot"));
            return false;
        }
        if (!plot.hasOwner()) {
            player.sendMessage(TranslatableCaption.miniMessage("info.plot_unowned"));
            return false;
        }
        if (plot.getVolume() > Integer.MAX_VALUE) {
            player.sendMessage(TranslatableCaption.miniMessage("schematics.schematic_too_large"));
            return false;
        }
        if (!plot.isMerged()) {
            player.sendMessage(TranslatableCaption.miniMessage("merge.unlink_impossible"));
            return false;
        }
        final boolean createRoad;
        if (args.length != 0) {
            if (args.length != 1 || !StringMan.isEqualIgnoreCaseToAny(args[0], "true", "false")) {
                sendUsage(player);
                return false;
            }
            createRoad = Boolean.parseBoolean(args[0]);
        } else {
            createRoad = true;
        }

        PlotUnlinkEvent event = this.eventDispatcher
                .callUnlink(plot.getArea(), plot, createRoad, createRoad,
                        PlotUnlinkEvent.REASON.PLAYER_COMMAND
                );
        if (event.getEventResult() == Result.DENY) {
            player.sendMessage(
                    TranslatableCaption.miniMessage("events.event_denied"),
                    Placeholder.miniMessage("value", "Unlink")
            );
            return true;
        }
        boolean force = event.getEventResult() == Result.FORCE;
        if (!force && !plot.isOwner(player.getUUID()) && !Permissions
                .hasPermission(player, Permission.PERMISSION_ADMIN_COMMAND_UNLINK)) {
            player.sendMessage(TranslatableCaption.miniMessage("permission.no_plot_perms"));
            return true;
        }
        Runnable runnable = () -> {
            if (!plot.getPlotModificationManager().unlinkPlot(createRoad, createRoad)) {
                player.sendMessage(TranslatableCaption.miniMessage("merge.unmerge_cancelled"));
                return;
            }
            player.sendMessage(TranslatableCaption.miniMessage("merge.unlink_success"));
            eventDispatcher.callPostUnlink(plot, PlotUnlinkEvent.REASON.PLAYER_COMMAND);
        };
        if (hasConfirmation(player)) {
            CmdConfirm.addPending(player, "/plot unlink " + plot.getId(), runnable);
        } else {
            TaskManager.runTask(runnable);
        }
        return true;
    }

}
