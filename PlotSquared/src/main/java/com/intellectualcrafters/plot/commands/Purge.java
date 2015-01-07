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

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import com.intellectualcrafters.plot.PlotMain;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.database.DBFunc;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotId;
import com.intellectualcrafters.plot.util.PlayerFunctions;
import com.intellectualcrafters.plot.util.UUIDHandler;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

@SuppressWarnings({"unused", "deprecated", "javadoc"}) public class Purge extends SubCommand {

    public Purge() {
        super("purge", "plots.admin", "Purge all plots for a world", "purge", "", CommandCategory.DEBUG, false);
    }

    public PlotId getId(String id) {
        try {
            String[] split = id.split(";");
            return new PlotId(Integer.parseInt(split[0]), Integer.parseInt(split[1]));
        }
        catch (Exception e) {
            return null;
        }
    }
    
    @Override
    public boolean execute(final Player plr, final String... args) {
        if (plr != null) {
            PlayerFunctions.sendMessage(plr, (C.NOT_CONSOLE));
            return false;
        }
        if (args.length == 1) {
            String arg = args[0].toLowerCase();
            PlotId id = getId(arg);
            if (id != null) {
                PlayerFunctions.sendMessage(plr, "/plot purge x;z &l<world>");
                return false;
            }
            UUID uuid = UUIDHandler.getUUID(args[0]);
            if (uuid != null) {
                PlayerFunctions.sendMessage(plr, "/plot purge "+args[0]+" &l<world>");
                return false;
            }
            if (arg.equals("player")) {
                PlayerFunctions.sendMessage(plr, "/plot purge &l<player> <world>");
                return false;
            }
            if (arg.equals("unowned")) {
                PlayerFunctions.sendMessage(plr, "/plot purge unowned &l<world>");
                return false;
            }
            if (arg.equals("unknown")) {
                PlayerFunctions.sendMessage(plr, "/plot purge unknown &l<world>");
                return false;
            }
            if (arg.equals("all")) {
                PlayerFunctions.sendMessage(plr, "/plot purge all &l<world>");
                return false;
            }
            PlayerFunctions.sendMessage(plr, C.PURGE_SYNTAX);
            return false;
        }
        if (args.length != 2) {
            PlayerFunctions.sendMessage(plr, C.PURGE_SYNTAX);
            return false;
        }
        World world = Bukkit.getWorld(args[1]);
        if (world == null || !PlotMain.isPlotWorld(world)) {
            PlayerFunctions.sendMessage(null, C.NOT_VALID_PLOT_WORLD);
            return false;
        }
        String worldname = world.getName();
        String arg = args[0].toLowerCase();
        PlotId id = getId(arg);
        if (id != null) {
            HashSet<Integer> ids = new HashSet<Integer>();
            int DBid = DBFunc.getId(worldname, id);
            if (DBid != Integer.MAX_VALUE) {
                ids.add(DBid);
            }
            DBFunc.purgeIds(worldname, ids);
            return finishPurge(DBid == Integer.MAX_VALUE ? 1 : 0);
        }
        UUID uuid = UUIDHandler.getUUID(args[0]);
        if (uuid != null) {
            Set<Plot> plots = PlotMain.getPlots(world,uuid);
            Set<PlotId> ids = new HashSet<>();
            for (Plot plot : plots) {
                ids.add(plot.id);
            }
            DBFunc.purge(worldname, ids);
            return finishPurge(ids.size());
        }
        if (arg.equals("all")) {
            Set<PlotId> ids = PlotMain.getPlots(world).keySet();
            DBFunc.purge(worldname, ids);
            return finishPurge(ids.size());
        }
        if (arg.equals("unknown")) {
            Collection<Plot> plots = PlotMain.getPlots(world).values();
            Set<PlotId> ids = new HashSet<>();
            for (Plot plot : plots) {
                if (plot.owner != null) {
                    String name = UUIDHandler.getName(plot.owner);  
                    if (name == null) {
                        ids.add(plot.id);
                    }
                }
            }
            DBFunc.purge(worldname, ids);
            return finishPurge(ids.size());
        }
        if (arg.equals("unowned")) {
            Collection<Plot> plots = PlotMain.getPlots(world).values();
            Set<PlotId> ids = new HashSet<>();
            for (Plot plot : plots) {
                if (plot.owner == null) {
                    ids.add(plot.id);
                }
            }
            DBFunc.purge(worldname, ids);
            return finishPurge(ids.size());
        }
        PlayerFunctions.sendMessage(plr, C.PURGE_SYNTAX);
        return false;
    }

    private boolean finishPurge(int amount) {
        PlayerFunctions.sendMessage(null, C.PURGE_SUCCESS, amount + "");
        return false;
    }

}
