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
package com.github.intellectualsites.plotsquared.plot.commands;

import com.github.intellectualsites.plotsquared.commands.Command;
import com.github.intellectualsites.plotsquared.commands.CommandDeclaration;
import com.github.intellectualsites.plotsquared.plot.PlotSquared;
import com.github.intellectualsites.plotsquared.plot.config.Captions;
import com.github.intellectualsites.plotsquared.plot.object.Plot;
import com.github.intellectualsites.plotsquared.plot.object.PlotPlayer;
import com.github.intellectualsites.plotsquared.plot.object.RunnableVal2;
import com.github.intellectualsites.plotsquared.plot.object.RunnableVal3;
import com.github.intellectualsites.plotsquared.plot.util.MainUtil;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@CommandDeclaration(command = "leave",
    description = "Removes self from being trusted or a member of the plot",
    permission = "plots.leave",
    usage = "/plot leave",
    category = CommandCategory.CLAIMING,
    requiredType = RequiredType.PLAYER)
public class Leave extends Command {
    public Leave() {
        super(MainCommand.getInstance(), true);
    }

    @Override public CompletableFuture<Boolean> execute(PlotPlayer player, String[] args,
        RunnableVal3<Command, Runnable, Runnable> confirm,
        RunnableVal2<Command, CommandResult> whenDone) throws CommandException {
        final Plot plot = check(player.getCurrentPlot(), Captions.NOT_IN_PLOT);
        checkTrue(plot.hasOwner(), Captions.PLOT_UNOWNED);
        checkTrue(plot.isAdded(player.getUUID()), Captions.NOT_ADDED_TRUSTED);
        checkTrue(args.length == 0, Captions.COMMAND_SYNTAX, getUsage());
        if (plot.isOwner(player.getUUID())) {
            checkTrue(plot.hasOwner(), Captions.ALREADY_OWNER);
            // TODO setowner, other
        } else {
            UUID uuid = player.getUUID();
            if (plot.isAdded(uuid)) {
                if (plot.removeTrusted(uuid)) {
                    PlotSquared.get().getEventDispatcher().callTrusted(player, plot, uuid, false);
                }
                if (plot.removeMember(uuid)) {
                    PlotSquared.get().getEventDispatcher().callMember(player, plot, uuid, false);
                }
                MainUtil.sendMessage(player, Captions.PLOT_LEFT, player.getName());
            } else {
                MainUtil.sendMessage(player, Captions.INVALID_PLAYER, 1);
            }
        }
        return CompletableFuture.completedFuture(true);
    }
}
