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
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotId;
import com.intellectualcrafters.plot.object.PlotSelection;
import com.intellectualcrafters.plot.util.PlayerFunctions;
import com.intellectualcrafters.plot.util.PlotHelper;
import com.intellectualcrafters.plot.util.SetBlockFast;
import com.intellectualcrafters.plot.util.UUIDHandler;

import org.bukkit.World;
import org.bukkit.entity.Player;

/**
 * Created 2014-08-01 for PlotSquared
 *
 * @author Empire92
 */
public class Swap extends SubCommand {

    public Swap() {
        super(Command.SWAP, "Swap two plots", "copy", CommandCategory.ACTIONS, true);
    }

    @Override
    public boolean execute(final Player plr, final String... args) {
        if (args.length < 1) {
            PlayerFunctions.sendMessage(plr, C.NEED_PLOT_ID);
            PlayerFunctions.sendMessage(plr, C.SWAP_SYNTAX);
            return false;
        }
        if (!PlayerFunctions.isInPlot(plr)) {
            PlayerFunctions.sendMessage(plr, C.NOT_IN_PLOT);
            return false;
        }
        final Plot plot = PlayerFunctions.getCurrentPlot(plr);
        if (((plot == null) || !plot.hasOwner() || !plot.getOwner().equals(UUIDHandler.getUUID(plr))) && !PlotMain.hasPermission(plr, "plots.admin")) {
            PlayerFunctions.sendMessage(plr, C.NO_PLOT_PERMS);
            return false;
        }
        if ((plot != null) && plot.settings.isMerged()) {
            PlayerFunctions.sendMessage(plr, C.UNLINK_REQUIRED);
            return false;
        }
        final String id = args[0];
        PlotId plotid;
        final World world = plr.getWorld();
        try {
            plotid = new PlotId(Integer.parseInt(id.split(";")[0]), Integer.parseInt(id.split(";")[1]));
            final Plot plot2 = PlotMain.getPlots(world).get(plotid);
            if (((plot2 == null) || !plot2.hasOwner() || (plot2.owner != UUIDHandler.getUUID(plr))) && !PlotMain.hasPermission(plr, "plots.admin")) {
                PlayerFunctions.sendMessage(plr, C.NO_PERM_MERGE, plotid.toString());
                return false;
            }
        } catch (final Exception e) {
            PlayerFunctions.sendMessage(plr, C.NOT_VALID_PLOT_ID);
            PlayerFunctions.sendMessage(plr, C.SWAP_SYNTAX);
            return false;
        }
        assert plot != null;
        if (plot.id.equals(plotid)) {
            PlayerFunctions.sendMessage(plr, C.NOT_VALID_PLOT_ID);
            PlayerFunctions.sendMessage(plr, C.SWAP_SYNTAX);
            return false;
        }
        PlotSelection.swap(world, plot.id, plotid);
        PlayerFunctions.sendMessage(plr, C.SWAP_SUCCESS);

        if (PlotHelper.canSetFast) {
            SetBlockFast.update(plr);
        }

        return true;
    }
}
