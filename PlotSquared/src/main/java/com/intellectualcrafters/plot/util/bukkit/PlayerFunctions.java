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
package com.intellectualcrafters.plot.util.bukkit;

import com.intellectualcrafters.plot.BukkitMain;
import com.intellectualcrafters.plot.PlotSquared;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.config.Settings;
import com.intellectualcrafters.plot.object.BukkitPlayer;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotId;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.object.PlotWorld;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.Permissions;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.util.ChatPaginator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Functions involving players, plots and locations.
 */
public class PlayerFunctions {
    /**
     * Clear a plot. Use null player if no player is present
     * @param player
     * @param world
     * @param plot
     * @param isDelete
     */
    public static void clear(final Player player, final String world, final Plot plot, final boolean isDelete) {
        final long start = System.currentTimeMillis();
        Runnable whenDone = new Runnable() {
            @Override
            public void run() {
                if ((player != null) && player.isOnline()) {
                    MainUtil.sendMessage(new BukkitPlayer(player), C.CLEARING_DONE, "" + (System.currentTimeMillis() - start));
                }
            }
        };
        if (!MainUtil.clearAsPlayer(plot, isDelete, whenDone)) {
            MainUtil.sendMessage(null, C.WAIT_FOR_TIMER);
        }
    }
    
    /**
     * Merges all plots in the arraylist (with cost)
     *
     * @param plr
     * @param world
     * @param plotIds
     *
     * @return
     */
    public static boolean mergePlots(final Player plr, final String world, final ArrayList<PlotId> plotIds) {
        final PlotWorld plotworld = PlotSquared.getPlotWorld(world);
        if ((PlotSquared.economy != null) && plotworld.USE_ECONOMY) {
            final double cost = plotIds.size() * plotworld.MERGE_PRICE;
            if (cost > 0d) {
                final Economy economy = PlotSquared.economy;
                if (economy.getBalance(plr) < cost) {
                    MainUtil.sendMessage(new BukkitPlayer(plr), C.CANNOT_AFFORD_MERGE, "" + cost);
                    return false;
                }
                economy.withdrawPlayer(plr, cost);
                MainUtil.sendMessage(new BukkitPlayer(plr), C.REMOVED_BALANCE, cost + "");
            }
        }
        return MainUtil.mergePlots(world, plotIds, true);
    }
    
    public static String getPlayerName(final UUID uuid) {
        if (uuid == null) {
            return "unknown";
        }
        final OfflinePlayer plr = UUIDHandler.uuidWrapper.getOfflinePlayer(uuid);
        if (!plr.hasPlayedBefore()) {
            return "unknown";
        }
        return plr.getName();
    }
    
    /**
     * @param player player
     *
     * @return
     */
    public static boolean isInPlot(final Player player) {
        return getCurrentPlot(player) != null;
    }
    
    public static ArrayList<PlotId> getMaxPlotSelectionIds(final String world, PlotId pos1, PlotId pos2) {
        final Plot plot1 = PlotSquared.getPlots(world).get(pos1);
        final Plot plot2 = PlotSquared.getPlots(world).get(pos2);
        if (plot1 != null) {
            pos1 = MainUtil.getBottomPlot(plot1).id;
        }
        if (plot2 != null) {
            pos2 = MainUtil.getTopPlot(world, plot2).id;
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
     * @return
     */
    public static Plot getCurrentPlot(final Player player) {
        if (!PlotSquared.isPlotWorld(player.getWorld().getName())) {
            return null;
        }
        final PlotId id = MainUtil.getPlotId(BukkitUtil.getLocation(player));
        final String world = player.getWorld().getName();
        if (id == null) {
            return null;
        }
        if (PlotSquared.getPlots(world).containsKey(id)) {
            return PlotSquared.getPlots(world).get(id);
        }
        return new Plot(id, null, new ArrayList<UUID>(), new ArrayList<UUID>(), world);
    }
    
    /**
     * Get the plots for a player
     *
     * @param plr
     *
     * @return
     */
    public static Set<Plot> getPlayerPlots(final String world, final Player plr) {
        final Set<Plot> p = PlotSquared.getPlots(world, plr.getName());
        if (p == null) {
            return new HashSet<>();
        }
        return p;
    }
    
    /**
     * Get the number of plots for a player
     *
     * @param plr
     *
     * @return
     */
    public static int getPlayerPlotCount(final String world, final Player plr) {
        final UUID uuid = UUIDHandler.getUUID(plr);
        int count = 0;
        for (final Plot plot : PlotSquared.getPlots(world).values()) {
            if (plot.hasOwner() && plot.owner.equals(uuid) && plot.countsTowardsMax) {
                count++;
            }
        }
        return count;
    }
}
