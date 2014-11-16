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

package com.intellectualcrafters.plot;

import org.bukkit.*;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;
import org.bukkit.util.ChatPaginator;

import java.util.*;

/**
 * Functions involving players, plots and locations.
 *
 * @author Citymonstret
 */
@SuppressWarnings("javadoc")
public class PlayerFunctions {

    /**
     * @param player player
     * @return
     */
    public static boolean isInPlot(final Player player) {
        return getCurrentPlot(player) != null;
    }

    /**
     * @param plot plot
     * @return
     */
    public static boolean hasExpired(final Plot plot) {
        final OfflinePlayer player = Bukkit.getOfflinePlayer(plot.owner);
        final long lp = player.getLastPlayed();
        final long cu = System.currentTimeMillis();
        return (lp - cu) > 30l;
    }

    public static ArrayList<PlotId> getPlotSelectionIds(@SuppressWarnings("unused") final World world, final PlotId pos1, final PlotId pos2) {
        final ArrayList<PlotId> myplots = new ArrayList<>();
        for (int x = pos1.x; x <= pos2.x; x++) {
            for (int y = pos1.y; y <= pos2.y; y++) {
                myplots.add(new PlotId(x, y));
            }
        }
        return myplots;
    }

    public static ArrayList<PlotId> getMaxPlotSelectionIds(final World world, PlotId pos1, PlotId pos2) {

        final Plot plot1 = PlotMain.getPlots(world).get(pos1);
        final Plot plot2 = PlotMain.getPlots(world).get(pos2);

        if (plot1 != null) {
            pos1 = getBottomPlot(world, plot1).id;
        }

        if (plot2 != null) {
            pos2 = getTopPlot(world, plot2).id;
        }

        final ArrayList<PlotId> myplots = new ArrayList<>();
        for (int x = pos1.x; x <= pos2.x; x++) {
            for (int y = pos1.y; y <= pos2.y; y++) {
                myplots.add(new PlotId(x, y));
            }
        }
        return myplots;
    }

    public static Plot getBottomPlot(final World world, final Plot plot) {
        if (plot.settings.getMerged(0)) {
            final Plot p = PlotMain.getPlots(world).get(new PlotId(plot.id.x, plot.id.y - 1));
            if (p == null) {
                return plot;
            }
            return getBottomPlot(world, p);
        }
        if (plot.settings.getMerged(3)) {
            final Plot p = PlotMain.getPlots(world).get(new PlotId(plot.id.x - 1, plot.id.y));
            if (p == null) {
                return plot;
            }
            return getBottomPlot(world, p);
        }
        return plot;
    }

    public static Plot getTopPlot(final World world, final Plot plot) {
        if (plot.settings.getMerged(2)) {
            return getTopPlot(world, PlotMain.getPlots(world).get(new PlotId(plot.id.x, plot.id.y + 1)));
        }
        if (plot.settings.getMerged(1)) {
            return getTopPlot(world, PlotMain.getPlots(world).get(new PlotId(plot.id.x + 1, plot.id.y)));
        }
        return plot;
    }

    /**
     * Returns the plot at a location (mega plots are not considered, all plots
     * are treated as small plots)
     *
     * @param loc
     * @return
     */
    public static PlotId getPlotAbs(final Location loc) {
        final String world = loc.getWorld().getName();
        final PlotManager manager = PlotMain.getPlotManager(world);
        if (manager == null) {
            return null;
        }
        final PlotWorld plotworld = PlotMain.getWorldSettings(world);
        return manager.getPlotIdAbs(plotworld, loc);
    }

    /**
     * Returns the plot id at a location (mega plots are considered)
     *
     * @param loc
     * @return
     */
    public static PlotId getPlot(final Location loc) {
        final String world = loc.getWorld().getName();
        final PlotManager manager = PlotMain.getPlotManager(world);
        if (manager == null) {
            return null;
        }
        final PlotWorld plotworld = PlotMain.getWorldSettings(world);
        return manager.getPlotId(plotworld, loc);
    }

    /**
     * Returns the plot a player is currently in.
     *
     * @param player
     * @return
     */
    public static Plot getCurrentPlot(final Player player) {
        if (!PlotMain.isPlotWorld(player.getWorld())) {
            return null;
        }
        final PlotId id = getPlot(player.getLocation());
        final World world = player.getWorld();
        if (id == null) {
            return null;
        }
        final HashMap<PlotId, Plot> plots = PlotMain.getPlots(world);
        if (plots != null) {
            if (plots.containsKey(id)) {
                return plots.get(id);
            }
        }
        return new Plot(id, null, Biome.FOREST, new ArrayList<UUID>(), new ArrayList<UUID>(), world.getName());

    }

    /**
     * Updates a given plot with another instance
     *
     * @param plot
     * @deprecated
     */
    @Deprecated
    public static void set(final Plot plot) {
        PlotMain.updatePlot(plot);
    }

    /**
     * Get the plots for a player
     *
     * @param plr
     * @return
     */
    public static Set<Plot> getPlayerPlots(final World world, final Player plr) {
        final Set<Plot> p = PlotMain.getPlots(world, plr);
        if (p == null) {
            return new HashSet<>();
        }
        return p;
    }

    /**
     * Get the number of plots for a player
     *
     * @param plr
     * @return
     */
    public static int getPlayerPlotCount(final World world, final Player plr) {
        final UUID uuid = plr.getUniqueId();
        int count = 0;
        for (final Plot plot : PlotMain.getPlots(world).values()) {
            if (plot.hasOwner() && plot.owner.equals(uuid) && plot.countsTowardsMax) {
                count++;
            }
        }
        return count;
    }

    /**
     * Get the maximum number of plots a player is allowed
     *
     * @param p
     * @return
     */
    @SuppressWarnings("SuspiciousNameCombination")
    public static int getAllowedPlots(final Player p) {
        return PlotMain.hasPermissionRange(p, "plots.plot", Settings.MAX_PLOTS);
    }

    /**
     * @return PlotMain.getPlots();
     * @deprecated
     */
    @Deprecated
    public static Set<Plot> getPlots() {
        return PlotMain.getPlots();
    }

    /**
     * \\previous\\
     *
     * @param plr
     * @param msg Was used to wrap the chat client length (Packets out--)
     */
    public static void sendMessageWrapped(final Player plr, String msg) {
        if (msg.length() > ChatPaginator.AVERAGE_CHAT_PAGE_WIDTH) {
            String[] ss = ChatPaginator.wordWrap(msg, ChatPaginator.AVERAGE_CHAT_PAGE_WIDTH);
            StringBuilder b = new StringBuilder();
            for (String p : ss) {
                b.append(p).append(p.equals(ss[ss.length - 1]) ? "" : "\n ");
            }
            msg = b.toString();
        }
        if (msg.endsWith("\n")) {
            msg = msg.substring(0, msg.length() - 2);
        }
        plr.sendMessage(msg);
    }

    /**
     * Send a message to the player
     *
     * @param plr Player to recieve message
     * @param msg Message to send
     * @return true
     * Can be used in things such as commands (return PlayerFunctions.sendMessage(...))
     */
    public static boolean sendMessage(final Player plr, final String msg) {
        if (msg.length() > 0 && !msg.equals("")) {
            if (plr == null) {
                PlotMain.sendConsoleSenderMessage(C.PREFIX.s() + msg);
            } else {
                sendMessageWrapped(plr, ChatColor.translateAlternateColorCodes('&', C.PREFIX.s() + msg));
            }
        }
        return true;
        /*
        if ((msg.length() == 0) || msg.equalsIgnoreCase("")) {
            return ;
        }

        if (plr == null) {
            PlotMain.sendConsoleSenderMessage(C.PREFIX.s() + msg);
            return;
        }

        sendMessageWrapped(plr, ChatColor.translateAlternateColorCodes('&', C.PREFIX.s() + msg));
        */
    }

    /**
     * Send a message to the player
     *
     * @param plr Player to recieve message
     * @param c   Caption to send
     * @return 
     */
    public static boolean sendMessage(final Player plr, final C c, final String... args) {
        if (c.s().length() > 1) {
            if (plr == null)
                PlotMain.sendConsoleSenderMessage(c);
            else {
                String msg = c.s();
                if ((args != null) && (args.length > 0)) {
                    for (final String str : args) {
                        System.out.print(str);
                        if (msg.contains("%s")) {
                            msg = msg.replaceFirst("%s", str);
                        }
                    }
                }
                sendMessage(plr, msg);
            }
        }
        return true;
        /*
        if (plr == null) {
            PlotMain.sendConsoleSenderMessage(c);
            return;
        }

>>>>>>> origin/master
        if (c.s().length() < 1) {
            return;
        }
        String msg = c.s();
        if ((args != null) && (args.length > 0)) {
            for (final String str : args) {
                msg = msg.replaceFirst("%s", str);
            }
        }
        if (plr == null) {
            PlotMain.sendConsoleSenderMessage(c);
            return;
        }
        sendMessage(plr, msg);
        */
    }
}
