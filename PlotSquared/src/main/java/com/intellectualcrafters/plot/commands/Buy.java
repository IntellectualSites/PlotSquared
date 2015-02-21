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

import net.milkbowl.vault.economy.Economy;

import com.intellectualcrafters.plot.PlotSquared;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.database.DBFunc;
import com.intellectualcrafters.plot.flag.Flag;
import com.intellectualcrafters.plot.flag.FlagManager;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotId;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.object.PlotWorld;
import com.intellectualcrafters.plot.util.EconHandler;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.bukkit.BukkitPlayerFunctions;
import com.intellectualcrafters.plot.util.bukkit.UUIDHandler;

/**
 * @author Citymonstret
 */
public class Buy extends SubCommand {
    public Buy() {
        super(Command.BUY, "Buy the plot you are standing on", "b", CommandCategory.CLAIMING, true);
    }
    
    @Override
    public boolean execute(final PlotPlayer plr, final String... args) {
        if (PlotSquared.economy == null) {
            return sendMessage(plr, C.ECON_DISABLED);
        }
        Location loc = plr.getLocation();
        final String world = loc.getWorld();
        if (!PlotSquared.isPlotWorld(world)) {
            return sendMessage(plr, C.NOT_IN_PLOT_WORLD);
        }
        Plot plot;
        if (args.length > 0) {
            try {
                final String[] split = args[0].split(";");
                final PlotId id = new PlotId(Integer.parseInt(split[0]), Integer.parseInt(split[1]));
                plot = MainUtil.getPlot(world, id);
            } catch (final Exception e) {
                return sendMessage(plr, C.NOT_VALID_PLOT_ID);
            }
        } else {
            plot = MainUtil.getPlot(loc);
        }
        if (plot == null) {
            return sendMessage(plr, C.NOT_IN_PLOT);
        }
        int currentPlots = MainUtil.getPlayerPlotCount(world, plr);
        if (currentPlots >= MainUtil.getAllowedPlots(plr, currentPlots)) {
            return sendMessage(plr, C.CANT_CLAIM_MORE_PLOTS);
        }
        if (!plot.hasOwner()) {
            return sendMessage(plr, C.PLOT_UNOWNED);
        }
        if (plot.owner.equals(plr.getUUID())) {
            return sendMessage(plr, C.CANNOT_BUY_OWN);
        }
        final Flag flag = FlagManager.getPlotFlag(plot, "price");
        if (flag == null) {
            return sendMessage(plr, C.NOT_FOR_SALE);
        }
        double initPrice = (double) flag.getValue();
        double price = initPrice;
        final PlotId id = plot.id;
        final PlotId id2 = MainUtil.getTopPlot(world, plot).id;
        final int size = MainUtil.getPlotSelectionIds(id, id2).size();
        final PlotWorld plotworld = PlotSquared.getPlotWorld(world);
        if (plotworld.USE_ECONOMY) {
            price += plotworld.PLOT_PRICE * size;
            initPrice += plotworld.SELL_PRICE * size;
        }
        if (PlotSquared.economy != null && price > 0d) {
            final Economy economy = PlotSquared.economy;
            if (EconHandler.getBalance(plr) < price) {
                return sendMessage(plr, C.CANNOT_AFFORD_PLOT, "" + price);
            }
            EconHandler.withdrawPlayer(plr, price);
            sendMessage(plr, C.REMOVED_BALANCE, price + "");
            EconHandler.depositPlayer(UUIDHandler.uuidWrapper.getOfflinePlayer(plot.owner), initPrice);
            final Player owner = UUIDHandler.uuidWrapper.getPlayer(plot.owner);
            if (owner != null) {
                sendMessage(plr, C.PLOT_SOLD, plot.id + "", plr.getName(), initPrice + "");
            }
            FlagManager.removePlotFlag(plot, "price");
        }
        plot.owner = plr.getUUID();
        DBFunc.setOwner(plot, plot.owner);
        MainUtil.sendMessage(plr, C.CLAIMED);
        return true;
    }
}
