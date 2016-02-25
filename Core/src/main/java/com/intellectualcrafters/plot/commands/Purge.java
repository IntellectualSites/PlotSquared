////////////////////////////////////////////////////////////////////////////////////////////////////
// PlotSquared - A plot manager and world generator for the Bukkit API                             /
// Copyright (c) 2014 IntellectualSites/IntellectualCrafters                                       /
//                                                                                                 /
// This program is free software; you can redistribute it and/or modify                            /
// it under the terms of the GNU General Public License as published by                            /
// the Free Software Foundation; either version 3 of the License, or                               /
// (at your option) any later version.                                                             /
//                                                                                                 /
// This program is distributed in the hope that it will be useful,                                 /
// but WITHOUT ANY WARRANTY; without even the implied warranty of                                  /
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the                                   /
// GNU General Public License for more details.                                                    /
//                                                                                                 /
// You should have received a copy of the GNU General Public License                               /
// along with this program; if not, write to the Free Software Foundation,                         /
// Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301  USA                               /
//                                                                                                 /
// You can contact us via: support@intellectualsites.com                                           /
////////////////////////////////////////////////////////////////////////////////////////////////////
package com.intellectualcrafters.plot.commands;

import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.database.DBFunc;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotArea;
import com.intellectualcrafters.plot.object.PlotId;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.CmdConfirm;
import com.intellectualcrafters.plot.util.StringMan;
import com.intellectualcrafters.plot.util.UUIDHandler;
import com.plotsquared.general.commands.CommandDeclaration;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.UUID;

@CommandDeclaration(
usage = "/plot purge world:<world> area:<area> id:<id> owner:<owner> shared:<shared> unknown:[true|false]",
command = "purge",
permission = "plots.admin",
description = "Purge all plots for a world",
category = CommandCategory.ADMINISTRATION,
requiredType = RequiredType.CONSOLE)
public class Purge extends SubCommand {
    @Override
    public boolean onCommand(final PlotPlayer plr, final String[] args) {
        if (args.length == 0) {
            return false;
        }

        String world = null;
        PlotArea area = null;
        PlotId id = null;
        UUID owner = null;
        UUID added = null;
        boolean unknown = false;
        for (String arg : args) {
            String[] split = arg.split(":");
            if (split.length != 2) {
                C.COMMAND_SYNTAX.send(plr, getUsage());
                return false;
            }
            switch (split[0].toLowerCase()) {
                case "world":
                case "w": {
                    world = split[1];
                    break;
                }
                case "area":
                case "a": {
                    area = PS.get().getPlotAreaByString(split[1]);
                    if (area == null) {
                        C.NOT_VALID_PLOT_WORLD.send(plr, split[1]);
                        return false;
                    }
                    break;
                }
                case "plotid":
                case "id": {
                    id = PlotId.fromString(split[1]);
                    if (id == null) {
                        C.NOT_VALID_PLOT_ID.send(plr, split[1]);
                        return false;
                    }
                    break;
                }
                case "owner":
                case "o": {
                    owner = UUIDHandler.getUUID(split[1], null);
                    if (owner == null) {
                        C.INVALID_PLAYER.send(plr, split[1]);
                        return false;
                    }
                    break;
                }
                case "shared":
                case "s": {
                    added = UUIDHandler.getUUID(split[1], null);
                    if (added == null) {
                        C.INVALID_PLAYER.send(plr, split[1]);
                        return false;
                    }
                    break;
                }
                case "unknown":
                case "?":
                case "u": {
                    unknown = Boolean.parseBoolean(split[1]);
                    break;
                }
            }
        }
        final HashSet<Plot> toDelete = new HashSet<>();
        for (Plot plot : PS.get().getBasePlots()) {
            if (world != null && !plot.getArea().worldname.equalsIgnoreCase(world)) {
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
            if (unknown && UUIDHandler.getName(plot.owner) != null) {
                continue;
            }
            for (Plot current : plot.getConnectedPlots()) {
                toDelete.add(current);
            }
        }
        if (PS.get().plots_tmp != null) {
            for (Entry<String, HashMap<PlotId, Plot>> entry : PS.get().plots_tmp.entrySet()) {
                String worldname = entry.getKey();
                if (world != null && !world.equalsIgnoreCase(worldname)) {
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
                    if (unknown && UUIDHandler.getName(plot.owner) != null) {
                        continue;
                    }
                    toDelete.add(plot);
                }
            }
        }
        if (toDelete.isEmpty()) {
            C.FOUND_NO_PLOTS.send(plr);
            return false;
        }
        String cmd = "/plot purge " + StringMan.join(args, " ") + " (" + toDelete.size() + " plots)";
        CmdConfirm.addPending(plr, cmd, new Runnable() {
            @Override
            public void run() {
                HashSet<Integer> ids = new HashSet<Integer>();
                for (Plot plot : toDelete) {
                    if (plot.temp != Integer.MAX_VALUE) {
                        ids.add(plot.temp);
                        PlotArea area = plot.getArea();
                        plot.getArea().removePlot(plot.getId());
                    }
                }
                DBFunc.purgeIds(ids);
                C.PURGE_SUCCESS.send(plr, ids.size() + "/" + (toDelete.size()));
            }
        });
        return true;
    }
}
