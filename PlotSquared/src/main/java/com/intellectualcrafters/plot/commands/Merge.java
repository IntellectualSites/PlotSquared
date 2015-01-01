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

import com.intellectualcrafters.plot.PlotMain;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.events.PlotMergeEvent;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotId;
import com.intellectualcrafters.plot.object.PlotWorld;
import com.intellectualcrafters.plot.util.PlayerFunctions;
import com.intellectualcrafters.plot.util.PlotHelper;
import com.intellectualcrafters.plot.util.SetBlockFast;
import com.intellectualcrafters.plot.util.UUIDHandler;

import net.milkbowl.vault.economy.Economy;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.ArrayList;

/**
 * @author Citymonstret
 */
public class Merge extends SubCommand {

    public final static String[] values = new String[]{"north", "east", "south", "west"};
    public final static String[] aliases = new String[]{"n", "e", "s", "w"};

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
    public boolean execute(final Player plr, final String... args) {
        if (!PlayerFunctions.isInPlot(plr)) {
            PlayerFunctions.sendMessage(plr, C.NOT_IN_PLOT);
            return true;
        }
        final Plot plot = PlayerFunctions.getCurrentPlot(plr);
        if ((plot == null) || !plot.hasOwner()) {
            PlayerFunctions.sendMessage(plr, C.PLOT_UNOWNED);
            return false;
        }
        boolean admin = PlotMain.hasPermission(plr, "plots.admin");
        if (!plot.getOwner().equals(UUIDHandler.getUUID(    plr)) && !admin) {
            PlayerFunctions.sendMessage(plr, C.NO_PLOT_PERMS);
            return false;
        }
        if (args.length < 1) {
            PlayerFunctions.sendMessage(plr, C.SUBCOMMAND_SET_OPTIONS_HEADER.s() + StringUtils.join(values, C.BLOCK_LIST_SEPARATER.s()));
            PlayerFunctions.sendMessage(plr, C.DIRECTION.s().replaceAll("%dir%", direction(plr.getLocation().getYaw())));
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
            PlayerFunctions.sendMessage(plr, C.SUBCOMMAND_SET_OPTIONS_HEADER.s() + StringUtils.join(values, C.BLOCK_LIST_SEPARATER.s()));
            PlayerFunctions.sendMessage(plr, C.DIRECTION.s().replaceAll("%dir%", direction(plr.getLocation().getYaw())));
            return false;
        }
        final World world = plr.getWorld();
        final PlotId bot = PlayerFunctions.getBottomPlot(world, plot).id;
        final PlotId top = PlayerFunctions.getTopPlot(world, plot).id;
        ArrayList<PlotId> plots;
        switch (direction) {
            case 0: // north = -y
                plots = PlayerFunctions.getMaxPlotSelectionIds(plr.getWorld(), new PlotId(bot.x, bot.y - 1), new PlotId(top.x, top.y));
                break;
            case 1: // east = +x
                plots = PlayerFunctions.getMaxPlotSelectionIds(plr.getWorld(), new PlotId(bot.x, bot.y), new PlotId(top.x + 1, top.y));
                break;
            case 2: // south = +y
                plots = PlayerFunctions.getMaxPlotSelectionIds(plr.getWorld(), new PlotId(bot.x, bot.y), new PlotId(top.x, top.y + 1));
                break;
            case 3: // west = -x
                plots = PlayerFunctions.getMaxPlotSelectionIds(plr.getWorld(), new PlotId(bot.x - 1, bot.y), new PlotId(top.x, top.y));
                break;
            default:
                return false;
        }
        for (final PlotId myid : plots) {
            final Plot myplot = PlotMain.getPlots(world).get(myid);
            if ((myplot == null) || !myplot.hasOwner() || !(myplot.getOwner().equals(UUIDHandler.getUUID(plr)) || admin)) {
                PlayerFunctions.sendMessage(plr, C.NO_PERM_MERGE.s().replaceAll("%plot%", myid.toString()));
                return false;
            }
        }

        final PlotWorld plotWorld = PlotMain.getWorldSettings(world);
        if (PlotMain.useEconomy && plotWorld.USE_ECONOMY) {
            double cost = plotWorld.MERGE_PRICE;
            cost = plots.size() * cost;
            if (cost > 0d) {
                final Economy economy = PlotMain.economy;
                if (economy.getBalance(plr) < cost) {
                    sendMessage(plr, C.CANNOT_AFFORD_MERGE, cost + "");
                    return false;
                }
                economy.withdrawPlayer(plr, cost);
                sendMessage(plr, C.REMOVED_BALANCE, cost + "");
            }
        }

        final PlotMergeEvent event = new PlotMergeEvent(world, plot, plots);

        Bukkit.getServer().getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            event.setCancelled(true);
            PlayerFunctions.sendMessage(plr, "&cMerge has been cancelled");
            return false;
        }
        PlayerFunctions.sendMessage(plr, "&cPlots have been merged");
        PlotHelper.mergePlots(world, plots);

        PlotHelper.setSign(world, UUIDHandler.getName(plot.owner), plot);

        if (PlotHelper.canSetFast) {
            SetBlockFast.update(plr);
        }
        return true;
    }
}
