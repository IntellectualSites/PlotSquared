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
import com.plotsquared.core.configuration.Captions;
import com.plotsquared.core.configuration.caption.TranslatableCaption;
import com.plotsquared.core.events.PlotUnlinkEvent;
import com.plotsquared.core.events.Result;
import com.plotsquared.core.location.Location;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.util.EventDispatcher;
import com.plotsquared.core.util.Permissions;
import com.plotsquared.core.util.StringMan;
import com.plotsquared.core.util.task.TaskManager;
import net.kyori.adventure.text.minimessage.Template;

import javax.annotation.Nonnull;

@CommandDeclaration(command = "unlink",
    aliases = {"u", "unmerge"},
    description = "Unlink a mega-plot",
    usage = "/plot unlink [createroads]",
    requiredType = RequiredType.PLAYER,
    category = CommandCategory.SETTINGS,
    confirmation = true)
public class Unlink extends SubCommand {

    private final EventDispatcher eventDispatcher;
    
    @Inject public Unlink(@Nonnull final EventDispatcher eventDispatcher) {
        this.eventDispatcher = eventDispatcher;
    }
    
    @Override public boolean onCommand(final PlotPlayer<?> player, String[] args) {
        Location location = player.getLocation();
        final Plot plot = location.getPlotAbs();
        if (plot == null) {
            player.sendMessage(TranslatableCaption.of("errors.not_in_plot"));
            return false;
        }
        if (!plot.hasOwner()) {
            return !sendMessage(player, Captions.PLOT_UNOWNED);
        }
        if (!plot.isMerged()) {
            return sendMessage(player, Captions.UNLINK_IMPOSSIBLE);
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
                PlotUnlinkEvent.REASON.PLAYER_COMMAND);
        if (event.getEventResult() == Result.DENY) {
            player.sendMessage(
                    TranslatableCaption.of("events.event_denied"),
                    Template.of("value", "Unlink"));
            return true;
        }
        boolean force = event.getEventResult() == Result.FORCE;
        if (!force && !plot.isOwner(player.getUUID()) && !Permissions
            .hasPermission(player, Captions.PERMISSION_ADMIN_COMMAND_UNLINK)) {
            return sendMessage(player, Captions.NO_PLOT_PERMS);
        }
        Runnable runnable = () -> {
            if (!plot.unlinkPlot(createRoad, createRoad)) {
                MainUtil.sendMessage(player, Captions.UNMERGE_CANCELLED);
                return;
            }
            MainUtil.sendMessage(player, Captions.UNLINK_SUCCESS);
        };
        if (hasConfirmation(player)) {
            CmdConfirm.addPending(player, "/plot unlink " + plot.getId(), runnable);
        } else {
            TaskManager.runTask(runnable);
        }
        return true;
    }
}
