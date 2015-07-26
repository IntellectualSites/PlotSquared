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
package com.plotsquared.bukkit.util.bukkit;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.bukkit.entity.Player;

import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotId;
import com.intellectualcrafters.plot.util.MainUtil;
import com.plotsquared.bukkit.util.UUIDHandler;

/**
 * Functions involving players, plots and locations.
 */
public class BukkitPlayerFunctions {

    /*
     * =========== NOTICE ================
     *  - We will try to move as many functions as we can out of this class and into the MainUtil class
     */

    /**
     * Clear a plot. Use null player if no player is present
     * @param player
     * @param world
     * @param plot
     * @param isDelete
     */
    public static void clear(final Player player, final String world, final Plot plot, final boolean isDelete) {
        final long start = System.currentTimeMillis();
        final Runnable whenDone = new Runnable() {
            @Override
            public void run() {
                if ((player != null) && player.isOnline()) {
                    MainUtil.sendMessage(BukkitUtil.getPlayer(player), C.CLEARING_DONE, "" + (System.currentTimeMillis() - start));
                }
            }
        };
        if (!MainUtil.clearAsPlayer(plot, isDelete, whenDone)) {
            MainUtil.sendMessage(null, C.WAIT_FOR_TIMER);
        }
    }

    public static String getPlayerName(final UUID uuid) {
        if (uuid == null) {
            return "unknown";
        }
        final String name = UUIDHandler.getName(uuid);
        if (name == null) {
            return "unknown";
        }
        return name;
    }

    /**
     * @param player player
     *
     * @return boolean
     */
    public static boolean isInPlot(final Player player) {
        return getCurrentPlot(player) != null;
    }

    public static ArrayList<PlotId> getMaxPlotSelectionIds(final String world, PlotId pos1, PlotId pos2) {
        final Plot plot1 = PS.get().getPlots(world).get(pos1);
        final Plot plot2 = PS.get().getPlots(world).get(pos2);
        if (plot1 != null) {
            pos1 = MainUtil.getBottomPlot(plot1).id;
        }
        if (plot2 != null) {
            pos2 = MainUtil.getTopPlot(plot2).id;
        }
        final ArrayList<PlotId> myplots = new ArrayList<>();
        for (int x = pos1.x; x <= pos2.x; x++) {
            for (int y = pos1.y; y <= pos2.y; y++) {
                myplots.add(new PlotId(x, y));
            }
        }
        return myplots;
    }

    /**
     * Returns the plot a player is currently in.
     *
     * @param player
     *
     * @return boolean
     */
    public static Plot getCurrentPlot(final Player player) {
        if (!PS.get().isPlotWorld(player.getWorld().getName())) {
            return null;
        }
        final PlotId id = MainUtil.getPlotId(BukkitUtil.getLocation(player));
        final String world = player.getWorld().getName();
        if (id == null) {
            return null;
        }
        if (PS.get().getPlots(world).containsKey(id)) {
            return PS.get().getPlots(world).get(id);
        }
        return new Plot(world, id, null);
    }

    /**
     * Get the plots for a player
     *
     * @param plr
     *
     * @return boolean
     */
    public static Set<Plot> getPlayerPlots(final String world, final Player plr) {
        final Set<Plot> p = PS.get().getPlots(world, plr.getName());
        if (p == null) {
            return new HashSet<>();
        }
        return p;
    }
}
