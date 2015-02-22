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
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.object.PlotWorld;
import com.intellectualcrafters.plot.util.EconHandler;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.Permissions;
import com.intellectualcrafters.plot.util.bukkit.BukkitPlayerFunctions;
import com.intellectualcrafters.plot.util.bukkit.UUIDHandler;

public class Delete extends SubCommand {
    public Delete() {
        super(Command.DELETE, "Delete a plot", "delete", CommandCategory.ACTIONS, true);
    }
    
    @Override
    public boolean execute(final PlotPlayer plr, final String... args) {
        Location loc = plr.getLocation();
        final Plot plot = MainUtil.getPlot(loc);
        if (plot == null) {
            return !sendMessage(plr, C.NOT_IN_PLOT);
        }
        if (!MainUtil.getTopPlot(plot).equals(MainUtil.getBottomPlot(plot))) {
            return !sendMessage(plr, C.UNLINK_REQUIRED);
        }
        if ((((plot == null) || !plot.hasOwner() || !plot.getOwner().equals(UUIDHandler.uuidWrapper.getUUID(plr)))) && !Permissions.hasPermission(plr, "plots.admin.command.delete")) {
            return !sendMessage(plr, C.NO_PLOT_PERMS);
        }
        assert plot != null;
        final PlotWorld pWorld = PlotSquared.getPlotWorld(plot.world);
        if (PlotSquared.economy != null && pWorld.USE_ECONOMY && (plot != null) && plot.hasOwner() && plot.getOwner().equals(UUIDHandler.getUUID(plr))) {
            final double c = pWorld.SELL_PRICE;
            if (c > 0d) {
                final Economy economy = PlotSquared.economy;
                EconHandler.depositPlayer(plr, c);
                sendMessage(plr, C.ADDED_BALANCE, c + "");
            }
        }
        if (MainUtil.runners.containsKey(plot)) {
            MainUtil.sendMessage(plr, C.WAIT_FOR_TIMER);
            return false;
        }
        final boolean result = PlotSquared.removePlot(loc.getWorld(), plot.id, true);
        final long start = System.currentTimeMillis();
        if (result) {
            boolean result2 = MainUtil.clearAsPlayer(plot, false, new Runnable() {
                @Override
                public void run() {
                    MainUtil.sendMessage(plr, C.CLEARING_DONE, "" + (System.currentTimeMillis() - start));
                }
            });
            if (!result2) {
                MainUtil.sendMessage(plr, C.WAIT_FOR_TIMER);
            }
            DBFunc.delete(loc.getWorld(), plot);
        } else {
            MainUtil.sendMessage(plr, "Plot deletion has been denied.");
        }
        return true;
    }
}
