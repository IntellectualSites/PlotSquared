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
import java.util.HashSet;
import java.util.UUID;

import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotId;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.object.PlotWorld;
import com.intellectualcrafters.plot.util.CmdConfirm;
import com.intellectualcrafters.plot.util.EconHandler;
import com.intellectualcrafters.plot.util.EventUtil;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.Permissions;
import com.intellectualcrafters.plot.util.StringMan;
import com.intellectualcrafters.plot.util.UUIDHandler;
import com.plotsquared.general.commands.CommandDeclaration;

@CommandDeclaration(
        command = "merge",
        aliases = {"m"},
        description = "Merge the plot you are standing on, with another plot",
        permission = "plots.merge",
        usage = "/plot merge [direction]",
        category = CommandCategory.ACTIONS,
        requiredType = RequiredType.NONE
)
public class Merge extends SubCommand {
    public final static String[] values = new String[] { "north", "east", "south", "west" };
    public final static String[] aliases = new String[] { "n", "e", "s", "w" };

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
    public boolean onCommand(final PlotPlayer plr, final String[] args) {
        
        final Location loc = plr.getLocationFull();
        final Plot plot = MainUtil.getPlot(loc);
        if (plot == null) {
            return !sendMessage(plr, C.NOT_IN_PLOT);
        }
        if ((plot == null) || !plot.hasOwner()) {
            MainUtil.sendMessage(plr, C.PLOT_UNOWNED);
            return false;
        }
        final boolean admin = Permissions.hasPermission(plr, "plots.admin.command.merge");
        if (!plot.isOwner(plr.getUUID()) && !admin) {
            MainUtil.sendMessage(plr, C.NO_PLOT_PERMS);
            return false;
        }
        int direction = -1;
        if (args.length == 0) {
            switch (direction(plr.getLocationFull().getYaw())) {
                case "NORTH":
                    direction = 0;
                    break;
                case "EAST":
                    direction = 1;
                    break;
                case "SOUTH":
                    direction = 2;
                    break;
                case "WEST":
                    direction = 3;
                    break;
            }
        }
        else {
            for (int i = 0; i < values.length; i++) {
                if (args[0].equalsIgnoreCase(values[i]) || args[0].equalsIgnoreCase(aliases[i])) {
                    direction = i;
                    break;
                }
            }
        }
        if (direction == -1) {
            MainUtil.sendMessage(plr, C.SUBCOMMAND_SET_OPTIONS_HEADER.s() + StringMan.join(values, C.BLOCK_LIST_SEPARATER.s()));
            MainUtil.sendMessage(plr, C.DIRECTION.s().replaceAll("%dir%", direction(loc.getYaw())));
            return false;
        }
        PlotId bot = MainUtil.getBottomPlot(plot).id;
        PlotId top = MainUtil.getTopPlot(plot).id;
        ArrayList<PlotId> selPlots;
        final String world = loc.getWorld();
        switch (direction) {
            case 0: // north = -y
                selPlots = MainUtil.getMaxPlotSelectionIds(world, new PlotId(bot.x, bot.y - 1), new PlotId(top.x, top.y));
                break;
            case 1: // east = +x
                selPlots = MainUtil.getMaxPlotSelectionIds(world, new PlotId(bot.x, bot.y), new PlotId(top.x + 1, top.y));
                break;
            case 2: // south = +y
                selPlots = MainUtil.getMaxPlotSelectionIds(world, new PlotId(bot.x, bot.y), new PlotId(top.x, top.y + 1));
                break;
            case 3: // west = -x
                selPlots = MainUtil.getMaxPlotSelectionIds(world, new PlotId(bot.x - 1, bot.y), new PlotId(top.x, top.y));
                break;
            default:
                return false;
        }
        final PlotId botId = selPlots.get(0);
        final PlotId topId = selPlots.get(selPlots.size() - 1);
        final PlotId bot1 = MainUtil.getBottomPlot(MainUtil.getPlot(world, botId)).id;
        final PlotId bot2 = MainUtil.getBottomPlot(MainUtil.getPlot(world, topId)).id;
        final PlotId top1 = MainUtil.getTopPlot(MainUtil.getPlot(world, topId)).id;
        final PlotId top2 = MainUtil.getTopPlot(MainUtil.getPlot(world, botId)).id;
        bot = new PlotId(Math.min(bot1.x, bot2.x), Math.min(bot1.y, bot2.y));
        top = new PlotId(Math.max(top1.x, top2.x), Math.max(top1.y, top2.y));
        final ArrayList<PlotId> plots = MainUtil.getMaxPlotSelectionIds(world, bot, top);
        boolean multiMerge = false;
        final HashSet<UUID> multiUUID = new HashSet<UUID>();
        HashSet<PlotId> multiPlots = new HashSet<>();
        final UUID u1 = plot.owner;
        for (final PlotId myid : plots) {
            final Plot myplot = PS.get().getPlot(world, myid);
            if (myplot == null || myplot.owner == null) {
                MainUtil.sendMessage(plr, C.NO_PERM_MERGE.s().replaceAll("%plot%", myid.toString()));
                return false;
            }
            UUID u2 = myplot.owner;
            if (u2.equals(u1)) {
                continue;
            }
            PlotPlayer p2 = UUIDHandler.getPlayer(u2);
            if (p2 == null) {
                MainUtil.sendMessage(plr, C.NO_PERM_MERGE.s().replaceAll("%plot%", myid.toString()));
                return false;
            }
            multiMerge = true;
            multiPlots.add(myid);
            multiUUID.add(u2);
        }
        if (multiMerge) {
            if (!Permissions.hasPermission(plr, Permissions.MERGE_OTHER)) {
                MainUtil.sendMessage(plr, C.NO_PERMISSION, Permissions.MERGE_OTHER.s);
                return false;
            }
            for (final UUID uuid : multiUUID) {
                PlotPlayer accepter = UUIDHandler.getPlayer(uuid);
                CmdConfirm.addPending(accepter, C.MERGE_REQUEST_CONFIRM.s().replaceAll("%s", plr.getName()), new Runnable() {
                    @Override
                    public void run() {
                        PlotPlayer accepter = UUIDHandler.getPlayer(uuid);
                        multiUUID.remove(uuid);
                        if (multiUUID.size() == 0) {
                            PlotPlayer pp = UUIDHandler.getPlayer(u1);
                            if (pp == null) {
                                sendMessage(accepter, C.MERGE_NOT_VALID);
                                return;
                            }
                            final PlotWorld plotWorld = PS.get().getPlotWorld(world);
                            if ((EconHandler.manager != null) && plotWorld.USE_ECONOMY) {
                                double cost = plotWorld.MERGE_PRICE;
                                cost = plots.size() * cost;
                                if (cost > 0d) {
                                    if (EconHandler.manager.getMoney(plr) < cost) {
                                        sendMessage(plr, C.CANNOT_AFFORD_MERGE, cost + "");
                                        return;
                                    }
                                    EconHandler.manager.withdrawMoney(plr, cost);
                                    sendMessage(plr, C.REMOVED_BALANCE, cost + "");
                                }
                            }
                            final boolean result = EventUtil.manager.callMerge(world, plot, plots);
                            if (!result) {
                                MainUtil.sendMessage(plr, "&cMerge has been cancelled");
                                return;
                            }
                            MainUtil.sendMessage(plr, C.SUCCESS_MERGE);
                            MainUtil.mergePlots(world, plots, true, true);
                            MainUtil.setSign(UUIDHandler.getName(plot.owner), plot);
                        }
                        MainUtil.sendMessage(accepter, C.MERGE_ACCEPTED);
                    }
                });
            }
            MainUtil.sendMessage(plr, C.MERGE_REQUESTED);
            return true;
        }
        final PlotWorld plotWorld = PS.get().getPlotWorld(world);
        if ((EconHandler.manager != null) && plotWorld.USE_ECONOMY) {
            double cost = plotWorld.MERGE_PRICE;
            cost = plots.size() * cost;
            if (cost > 0d) {
                if (EconHandler.manager.getMoney(plr) < cost) {
                    sendMessage(plr, C.CANNOT_AFFORD_MERGE, cost + "");
                    return false;
                }
                EconHandler.manager.withdrawMoney(plr, cost);
                sendMessage(plr, C.REMOVED_BALANCE, cost + "");
            }
        }
        final boolean result = EventUtil.manager.callMerge(world, plot, plots);
        if (!result) {
            MainUtil.sendMessage(plr, "&cMerge has been cancelled");
            return false;
        }
        MainUtil.sendMessage(plr, C.SUCCESS_MERGE);
        MainUtil.mergePlots(world, plots, true, true);
        MainUtil.setSign(UUIDHandler.getName(plot.owner), plot);
        return true;
    }
}
