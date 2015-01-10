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
import com.intellectualcrafters.plot.database.DBFunc;
import com.intellectualcrafters.plot.events.PlayerClaimPlotEvent;
import com.intellectualcrafters.plot.flag.Flag;
import com.intellectualcrafters.plot.flag.FlagManager;
import com.intellectualcrafters.plot.generator.HybridPlotWorld;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotId;
import com.intellectualcrafters.plot.object.PlotWorld;
import com.intellectualcrafters.plot.util.PlayerFunctions;
import com.intellectualcrafters.plot.util.PlotHelper;
import com.intellectualcrafters.plot.util.SchematicHandler;
import com.intellectualcrafters.plot.util.SetBlockFast;
import com.intellectualcrafters.plot.util.UUIDHandler;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

/**
 * @author Citymonstret
 */
public class Buy extends SubCommand {

    public Buy() {
        super(Command.BUY, "Buy the plot you are standing on", "b", CommandCategory.CLAIMING, true);
    }

    @Override
    public boolean execute(final Player plr, final String... args) {
        if (!PlotMain.useEconomy) {
            return sendMessage(plr, C.ECON_DISABLED);
        }
        World world = plr.getWorld();
        if (!PlotMain.isPlotWorld(world)) {
            return sendMessage(plr, C.NOT_IN_PLOT_WORLD);
        }
        Plot plot;
        if (args.length > 0) {
            try {
                String[] split = args[0].split(";");
                PlotId id = new PlotId(Integer.parseInt(split[0]), Integer.parseInt(split[1]));
                plot = PlotHelper.getPlot(world, id);
            }
            catch (Exception e) {
                return sendMessage(plr, C.NOT_VALID_PLOT_ID);
            }
        }
        else {
            plot = PlayerFunctions.getCurrentPlot(plr);
        }
        if (plot == null) {
            return sendMessage(plr, C.NOT_IN_PLOT);
        }
        if (PlayerFunctions.getPlayerPlotCount(world, plr) >= PlayerFunctions.getAllowedPlots(plr)) {
            return sendMessage(plr, C.CANT_CLAIM_MORE_PLOTS);
        }
        if (!plot.hasOwner()) {
            return sendMessage(plr, C.PLOT_UNOWNED);
        }
        Flag flag = FlagManager.getPlotFlag(plot, "price");
        if (flag == null) {
            return sendMessage(plr, C.NOT_FOR_SALE);
        }
        double price = Double.parseDouble(flag.getValue());
        PlotId id = plot.id;
        PlotId id2 = PlayerFunctions.getTopPlot(world, plot).id;
        int size = PlayerFunctions.getPlotSelectionIds(world, id, id2).size();
        PlotWorld plotworld = PlotMain.getWorldSettings(world);
        if (plotworld.USE_ECONOMY) {
            price += plotworld.PLOT_PRICE * size;
        }
        if (price > 0d) {
            final Economy economy = PlotMain.economy;
            if (economy.getBalance(plr) < price) {
                return sendMessage(plr, C.CANNOT_AFFORD_PLOT, "" + price);
            }
            economy.withdrawPlayer(plr, price);
            sendMessage(plr, C.REMOVED_BALANCE, price + "");
        }
        plot.owner = UUIDHandler.getUUID(plr);
        DBFunc.setOwner(plot, plot.owner);
        PlayerFunctions.sendMessage(plr, C.CLAIMED);
    }
}
