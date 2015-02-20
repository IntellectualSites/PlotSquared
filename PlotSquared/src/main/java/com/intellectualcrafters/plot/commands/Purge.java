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
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

import com.intellectualcrafters.plot.PlotSquared;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.database.DBFunc;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotId;
import com.intellectualcrafters.plot.util.bukkit.BukkitPlayerFunctions;
import com.intellectualcrafters.plot.util.bukkit.UUIDHandler;

@SuppressWarnings({ "javadoc" })
public class Purge extends SubCommand {
    public Purge() {
        super("purge", "plots.admin", "Purge all plots for a world", "purge", "", CommandCategory.DEBUG, false);
    }
    
    public PlotId getId(final String id) {
        try {
            final String[] split = id.split(";");
            return new PlotId(Integer.parseInt(split[0]), Integer.parseInt(split[1]));
        } catch (final Exception e) {
            return null;
        }
    }
    
    @Override
    public boolean execute(final Player plr, final String... args) {
        if (plr != null) {
            BukkitPlayerFunctions.sendMessage(plr, (C.NOT_CONSOLE));
            return false;
        }
        if (args.length == 1) {
            final String arg = args[0].toLowerCase();
            final PlotId id = getId(arg);
            if (id != null) {
                BukkitPlayerFunctions.sendMessage(plr, "/plot purge x;z &l<world>");
                return false;
            }
            final UUID uuid = UUIDHandler.getUUID(args[0]);
            if (uuid != null) {
                BukkitPlayerFunctions.sendMessage(plr, "/plot purge " + args[0] + " &l<world>");
                return false;
            }
            if (arg.equals("player")) {
                BukkitPlayerFunctions.sendMessage(plr, "/plot purge &l<player> <world>");
                return false;
            }
            if (arg.equals("unowned")) {
                BukkitPlayerFunctions.sendMessage(plr, "/plot purge unowned &l<world>");
                return false;
            }
            if (arg.equals("unknown")) {
                BukkitPlayerFunctions.sendMessage(plr, "/plot purge unknown &l<world>");
                return false;
            }
            if (arg.equals("all")) {
                BukkitPlayerFunctions.sendMessage(plr, "/plot purge all &l<world>");
                return false;
            }
            BukkitPlayerFunctions.sendMessage(plr, C.PURGE_SYNTAX);
            return false;
        }
        if (args.length != 2) {
            BukkitPlayerFunctions.sendMessage(plr, C.PURGE_SYNTAX);
            return false;
        }
        final World world = Bukkit.getWorld(args[1]);
        if ((world == null) || !PlotSquared.isPlotWorld(world)) {
            BukkitPlayerFunctions.sendMessage(null, C.NOT_VALID_PLOT_WORLD);
            return false;
        }
        final String worldname = world.getName();
        final String arg = args[0].toLowerCase();
        final PlotId id = getId(arg);
        if (id != null) {
            final HashSet<Integer> ids = new HashSet<Integer>();
            final int DBid = DBFunc.getId(worldname, id);
            if (DBid != Integer.MAX_VALUE) {
                ids.add(DBid);
            }
            DBFunc.purgeIds(worldname, ids);
            return finishPurge(DBid == Integer.MAX_VALUE ? 1 : 0);
        }
        final UUID uuid = UUIDHandler.getUUID(args[0]);
        if (uuid != null) {
            final Set<Plot> plots = PlotSquared.getPlots(world, uuid);
            final Set<PlotId> ids = new HashSet<>();
            for (final Plot plot : plots) {
                ids.add(plot.id);
            }
            DBFunc.purge(worldname, ids);
            return finishPurge(ids.size());
        }
        if (arg.equals("all")) {
            final Set<PlotId> ids = PlotSquared.getPlots(world).keySet();
            if (ids.size() == 0) {
                return BukkitPlayerFunctions.sendMessage(null, "&cNo plots found");
            }
            DBFunc.purge(worldname, ids);
            return finishPurge(ids.size());
        }
        if (arg.equals("unknown")) {
            final Collection<Plot> plots = PlotSquared.getPlots(world).values();
            final Set<PlotId> ids = new HashSet<>();
            for (final Plot plot : plots) {
                if (plot.owner != null) {
                    final String name = UUIDHandler.getName(plot.owner);
                    if (name == null) {
                        ids.add(plot.id);
                    }
                }
            }
            if (ids.size() == 0) {
                return BukkitPlayerFunctions.sendMessage(null, "&cNo plots found");
            }
            DBFunc.purge(worldname, ids);
            return finishPurge(ids.size());
        }
        if (arg.equals("unowned")) {
            final Collection<Plot> plots = PlotSquared.getPlots(world).values();
            final Set<PlotId> ids = new HashSet<>();
            for (final Plot plot : plots) {
                if (plot.owner == null) {
                    ids.add(plot.id);
                }
            }
            if (ids.size() == 0) {
                return BukkitPlayerFunctions.sendMessage(null, "&cNo plots found");
            }
            DBFunc.purge(worldname, ids);
            return finishPurge(ids.size());
        }
        BukkitPlayerFunctions.sendMessage(plr, C.PURGE_SYNTAX);
        return false;
    }
    
    private boolean finishPurge(final int amount) {
        BukkitPlayerFunctions.sendMessage(null, C.PURGE_SUCCESS, amount + "");
        return false;
    }
}
