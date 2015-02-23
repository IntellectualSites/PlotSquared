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

import java.util.ArrayList;

import org.apache.commons.lang.StringUtils;

import com.intellectualcrafters.plot.PlotSquared;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotId;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.object.PlotWorld;
import com.intellectualcrafters.plot.util.EconHandler;
import com.intellectualcrafters.plot.util.EventUtil;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.Permissions;
import com.intellectualcrafters.plot.util.bukkit.UUIDHandler;

/**
 * @author Citymonstret
 */
public class Merge extends SubCommand {
    public final static String[] values = new String[] { "north", "east", "south", "west" };
    public final static String[] aliases = new String[] { "n", "e", "s", "w" };

    public Merge() {
        super(Command.MERGE, "Merge the plot you are standing on with another plot.", "merge", CommandCategory.ACTIONS, true);
    }

    public static String direction(float yaw) {
        yaw = yaw / 90;
        final int i = Math.round(yaw);
        switch (i) {
            case -4:
            case 0:
            case 4:
                return "SOUTH";
            case -1:
            case 3:
                return "EAST";
            case -2:
            case 2:
                return "NORTH";
            case -3:
            case 1:
                return "WEST";
            default:
                return "";
        }
    }

    @Override
    public boolean execute(final PlotPlayer plr, final String... args) {
        final Location loc = plr.getLocation();
        final Plot plot = MainUtil.getPlot(loc);
        if (plot == null) {
            return !sendMessage(plr, C.NOT_IN_PLOT);
        }
        if ((plot == null) || !plot.hasOwner()) {
            MainUtil.sendMessage(plr, C.PLOT_UNOWNED);
            return false;
        }
        final boolean admin = Permissions.hasPermission(plr, "plots.admin.command.merge");
        if (!plot.getOwner().equals(UUIDHandler.getUUID(plr)) && !admin) {
            MainUtil.sendMessage(plr, C.NO_PLOT_PERMS);
            return false;
        }
        if (args.length < 1) {
            MainUtil.sendMessage(plr, C.SUBCOMMAND_SET_OPTIONS_HEADER.s() + StringUtils.join(values, C.BLOCK_LIST_SEPARATER.s()));
            MainUtil.sendMessage(plr, C.DIRECTION.s().replaceAll("%dir%", direction(plr.getLocation().getYaw())));
            return false;
        }
        int direction = -1;
        for (int i = 0; i < values.length; i++) {
            if (args[0].equalsIgnoreCase(values[i]) || args[0].equalsIgnoreCase(aliases[i])) {
                direction = i;
                break;
            }
        }
        if (direction == -1) {
            MainUtil.sendMessage(plr, C.SUBCOMMAND_SET_OPTIONS_HEADER.s() + StringUtils.join(values, C.BLOCK_LIST_SEPARATER.s()));
            MainUtil.sendMessage(plr, C.DIRECTION.s().replaceAll("%dir%", direction(plr.getLocation().getYaw())));
            return false;
        }
        PlotId bot = MainUtil.getBottomPlot(plot).id;
        PlotId top = MainUtil.getTopPlot(plot).id;
        ArrayList<PlotId> plots;
        final String world = plr.getLocation().getWorld();
        switch (direction) {
            case 0: // north = -y
                plots = MainUtil.getMaxPlotSelectionIds(world, new PlotId(bot.x, bot.y - 1), new PlotId(top.x, top.y));
                break;
            case 1: // east = +x
                plots = MainUtil.getMaxPlotSelectionIds(world, new PlotId(bot.x, bot.y), new PlotId(top.x + 1, top.y));
                break;
            case 2: // south = +y
                plots = MainUtil.getMaxPlotSelectionIds(world, new PlotId(bot.x, bot.y), new PlotId(top.x, top.y + 1));
                break;
            case 3: // west = -x
                plots = MainUtil.getMaxPlotSelectionIds(world, new PlotId(bot.x - 1, bot.y), new PlotId(top.x, top.y));
                break;
            default:
                return false;
        }
        final PlotId botId = plots.get(0);
        final PlotId topId = plots.get(plots.size() - 1);
        final PlotId bot1 = MainUtil.getBottomPlot(MainUtil.getPlot(world, botId)).id;
        final PlotId bot2 = MainUtil.getBottomPlot(MainUtil.getPlot(world, topId)).id;
        final PlotId top1 = MainUtil.getTopPlot(MainUtil.getPlot(world, topId)).id;
        final PlotId top2 = MainUtil.getTopPlot(MainUtil.getPlot(world, botId)).id;
        bot = new PlotId(Math.min(bot1.x, bot2.x), Math.min(bot1.y, bot2.y));
        top = new PlotId(Math.max(top1.x, top2.x), Math.max(top1.y, top2.y));
        plots = MainUtil.getMaxPlotSelectionIds(world, bot, top);
        for (final PlotId myid : plots) {
            final Plot myplot = PlotSquared.getPlots(world).get(myid);
            if ((myplot == null) || !myplot.hasOwner() || !(myplot.getOwner().equals(UUIDHandler.getUUID(plr)) || admin)) {
                MainUtil.sendMessage(plr, C.NO_PERM_MERGE.s().replaceAll("%plot%", myid.toString()));
                return false;
            }
        }
        final PlotWorld plotWorld = PlotSquared.getPlotWorld(world);
        if ((PlotSquared.economy != null) && plotWorld.USE_ECONOMY) {
            double cost = plotWorld.MERGE_PRICE;
            cost = plots.size() * cost;
            if (cost > 0d) {
                if (EconHandler.getBalance(plr) < cost) {
                    sendMessage(plr, C.CANNOT_AFFORD_MERGE, cost + "");
                    return false;
                }
                EconHandler.withdrawPlayer(plr, cost);
                sendMessage(plr, C.REMOVED_BALANCE, cost + "");
            }
        }
        final boolean result = EventUtil.manager.callMerge(world, plot, plots);
        if (!result) {
            MainUtil.sendMessage(plr, "&cMerge has been cancelled");
            return false;
        }
        MainUtil.sendMessage(plr, "&cPlots have been merged");
        MainUtil.mergePlots(world, plots, true);
        MainUtil.setSign(UUIDHandler.getName(plot.owner), plot);
        MainUtil.update(plr.getLocation());
        return true;
    }
}
