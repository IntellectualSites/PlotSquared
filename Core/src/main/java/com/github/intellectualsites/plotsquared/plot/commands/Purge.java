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

import com.github.intellectualsites.plotsquared.commands.CommandDeclaration;
import com.github.intellectualsites.plotsquared.plot.PlotSquared;
import com.github.intellectualsites.plotsquared.plot.config.Captions;
import com.github.intellectualsites.plotsquared.plot.database.DBFunc;
import com.github.intellectualsites.plotsquared.plot.listener.PlotListener;
import com.github.intellectualsites.plotsquared.plot.object.Plot;
import com.github.intellectualsites.plotsquared.plot.object.PlotArea;
import com.github.intellectualsites.plotsquared.plot.object.PlotId;
import com.github.intellectualsites.plotsquared.plot.object.PlotPlayer;
import com.github.intellectualsites.plotsquared.plot.util.CmdConfirm;
import com.github.intellectualsites.plotsquared.plot.util.StringMan;
import com.github.intellectualsites.plotsquared.plot.util.TaskManager;
import com.github.intellectualsites.plotsquared.plot.util.UUIDHandler;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

@CommandDeclaration(usage = "/plot purge world:<world> area:<area> id:<id> owner:<owner> shared:<shared> unknown:[true|false] clear:[true|false]",
    command = "purge",
    permission = "plots.admin",
    description = "Purge all plots for a world",
    category = CommandCategory.ADMINISTRATION,
    requiredType = RequiredType.CONSOLE,
    confirmation = true)
public class Purge extends SubCommand {

    @Override public boolean onCommand(final PlotPlayer player, String[] args) {
        if (args.length == 0) {
            Captions.COMMAND_SYNTAX.send(player, getUsage());
            return false;
        }

        String world = null;
        PlotArea area = null;
        PlotId id = null;
        UUID owner = null;
        UUID added = null;
        boolean unknown = false;
        boolean clear = false;
        for (String arg : args) {
            String[] split = arg.split(":");
            if (split.length != 2) {
                Captions.COMMAND_SYNTAX.send(player, getUsage());
                return false;
            }
            switch (split[0].toLowerCase()) {
                case "world":
                case "w":
                    world = split[1];
                    break;
                case "area":
                case "a":
                    area = PlotSquared.get().getPlotAreaByString(split[1]);
                    if (area == null) {
                        Captions.NOT_VALID_PLOT_WORLD.send(player, split[1]);
                        return false;
                    }
                    break;
                case "plotid":
                case "id":
                    try {
                        id = PlotId.fromString(split[1]);
                    } catch (IllegalArgumentException ignored) {
                        Captions.NOT_VALID_PLOT_ID.send(player, split[1]);
                        return false;
                    }
                    break;
                case "owner":
                case "o":
                    owner = UUIDHandler.getUUID(split[1], null);
                    if (owner == null) {
                        Captions.INVALID_PLAYER.send(player, split[1]);
                        return false;
                    }
                    break;
                case "shared":
                case "s":
                    added = UUIDHandler.getUUID(split[1], null);
                    if (added == null) {
                        Captions.INVALID_PLAYER.send(player, split[1]);
                        return false;
                    }
                    break;
                case "unknown":
                case "?":
                case "u":
                    unknown = Boolean.parseBoolean(split[1]);
                    break;
                case "clear":
                case "c":
                case "delete":
                case "d":
                case "del":
                    clear = Boolean.parseBoolean(split[1]);
                    break;
                default:
                    Captions.COMMAND_SYNTAX.send(player, getUsage());
                    return false;
            }
        }
        final HashSet<Plot> toDelete = new HashSet<>();
        for (Plot plot : PlotSquared.get().getBasePlots()) {
            if (world != null && !plot.getWorldName().equalsIgnoreCase(world)) {
                continue;
            }
            if (area != null && !plot.getArea().equals(area)) {
                continue;
            }
            if (id != null && !plot.getId().equals(id)) {
                continue;
            }
            if (owner != null && !plot.isOwner(owner)) {
                continue;
            }
            if (added != null && !plot.isAdded(added)) {
                continue;
            }
            if (unknown && UUIDHandler.getName(plot.getOwnerAbs()) != null) {
                continue;
            }
            toDelete.addAll(plot.getConnectedPlots());
        }
        if (PlotSquared.get().plots_tmp != null) {
            for (Entry<String, HashMap<PlotId, Plot>> entry : PlotSquared.get().plots_tmp
                .entrySet()) {
                String worldName = entry.getKey();
                if (world != null && !world.equalsIgnoreCase(worldName)) {
                    continue;
                }
                for (Entry<PlotId, Plot> entry2 : entry.getValue().entrySet()) {
                    Plot plot = entry2.getValue();
                    if (id != null && !plot.getId().equals(id)) {
                        continue;
                    }
                    if (owner != null && !plot.isOwner(owner)) {
                        continue;
                    }
                    if (added != null && !plot.isAdded(added)) {
                        continue;
                    }
                    if (unknown && UUIDHandler.getName(plot.getOwnerAbs()) != null) {
                        continue;
                    }
                    toDelete.add(plot);
                }
            }
        }
        if (toDelete.isEmpty()) {
            Captions.FOUND_NO_PLOTS.send(player);
            return false;
        }
        String cmd =
            "/plot purge " + StringMan.join(args, " ") + " (" + toDelete.size() + " plots)";
        boolean finalClear = clear;
        Runnable run = () -> {
            PlotSquared.debug("Calculating plots to purge, please wait...");
            HashSet<Integer> ids = new HashSet<>();
            Iterator<Plot> iterator = toDelete.iterator();
            AtomicBoolean cleared = new AtomicBoolean(true);
            Runnable runasync = new Runnable() {
                @Override public void run() {
                    while (iterator.hasNext() && cleared.get()) {
                        cleared.set(false);
                        Plot plot = iterator.next();
                        if (plot.temp != Integer.MAX_VALUE) {
                            ids.add(plot.temp);
                            if (finalClear) {
                                plot.clear(false, true, () -> PlotSquared
                                    .debug("Plot " + plot.getId() + " cleared by purge."));
                            } else {
                                plot.removeSign();
                            }
                            plot.getArea().removePlot(plot.getId());
                            for (PlotPlayer pp : plot.getPlayersInPlot()) {
                                PlotListener.plotEntry(pp, plot);
                            }
                        }
                        cleared.set(true);
                    }
                    if (iterator.hasNext()) {
                        TaskManager.runTaskAsync(this);
                    } else {
                        TaskManager.runTask(() -> {
                            DBFunc.purgeIds(ids);
                            Captions.PURGE_SUCCESS.send(player, ids.size() + "/" + toDelete.size());
                        });
                    }
                }
            };
            TaskManager.runTaskAsync(runasync);
        };
        if (hasConfirmation(player)) {
            CmdConfirm.addPending(player, cmd, run);
        } else {
            run.run();
        }
        return true;
    }
}
