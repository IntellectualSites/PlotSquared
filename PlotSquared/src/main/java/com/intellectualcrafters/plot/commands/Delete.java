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
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotWorld;
import com.intellectualcrafters.plot.util.PlayerFunctions;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.entity.Player;

public class Delete extends SubCommand {

    public Delete() {
        super(Command.DELETE, "Delete a plot", "delete", CommandCategory.ACTIONS, true);
    }

    @Override
    public boolean execute(final Player plr, final String... args) {
        if (!PlayerFunctions.isInPlot(plr)) {
            return !sendMessage(plr, C.NOT_IN_PLOT);
        }
        final Plot plot = PlayerFunctions.getCurrentPlot(plr);
        if (!PlayerFunctions.getTopPlot(plr.getWorld(), plot).equals(PlayerFunctions.getBottomPlot(plr.getWorld(), plot))) {
            return !sendMessage(plr, C.UNLINK_REQUIRED);
        }
        if ((((plot == null) || !plot.hasOwner() || !plot.getOwner().equals(plr.getUniqueId()))) && !PlotMain.hasPermission(plr, "plots.admin")) {
            return !sendMessage(plr, C.NO_PLOT_PERMS);
        }
        assert plot != null;
        final PlotWorld pWorld = PlotMain.getWorldSettings(plot.getWorld());
        if (PlotMain.useEconomy && pWorld.USE_ECONOMY && plot!=null && plot.hasOwner()) {
            final double c = pWorld.SELL_PRICE;
            if (c > 0d) {
                final Economy economy = PlotMain.economy;
                economy.depositPlayer(plr, c);
                sendMessage(plr, C.ADDED_BALANCE, c + "");
            }
        }
        final boolean result = PlotMain.removePlot(plr.getWorld().getName(), plot.id, true);
        if (result) {
            plot.clear(plr);
            DBFunc.delete(plr.getWorld().getName(), plot);
            if ((Math.abs(plot.id.x) <= Math.abs(Auto.lastPlot.x)) && (Math.abs(plot.id.y) <= Math.abs(Auto.lastPlot.y))) {
                Auto.lastPlot = plot.id;
            }
        } else {
            PlayerFunctions.sendMessage(plr, "Plot deletion has been denied.");
        }
        return true;
    }
}
