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

import org.bukkit.entity.Player;

import com.intellectualcrafters.plot.BukkitMain;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotSelection;
import com.intellectualcrafters.plot.util.PlayerFunctions;
import com.intellectualcrafters.plot.util.PlotHelper;
import com.intellectualcrafters.plot.util.UUIDHandler;

public class Copy extends SubCommand {

    public Copy() {
        super(Command.COPY, "Copy a plot", "copy", CommandCategory.ACTIONS, true);
    }

    @Override
    public boolean execute(final Player plr, final String... args) {
        if (!PlayerFunctions.isInPlot(plr)) {
            PlayerFunctions.sendMessage(plr, C.NOT_IN_PLOT);
            return false;
        }
        final Plot plot = PlayerFunctions.getCurrentPlot(plr);
        if (((plot == null) || !plot.hasOwner() || !plot.getOwner().equals(UUIDHandler.getUUID(plr))) && !BukkitMain.hasPermission(plr, "plots.admin.command.copy")) {
            PlayerFunctions.sendMessage(plr, C.NO_PLOT_PERMS);
            return false;
        }
        assert plot != null;
        if (plot.settings.isMerged()) {
            PlayerFunctions.sendMessage(plr, C.UNLINK_REQUIRED);
            return false;
        }
        final int size = (PlotHelper.getPlotTopLocAbs(plr.getWorld(), plot.getId()).getBlockX() - PlotHelper.getPlotBottomLocAbs(plr.getWorld(), plot.getId()).getBlockX());
        final PlotSelection selection = new PlotSelection(size, plr.getWorld(), plot);
        if (PlotSelection.currentSelection.containsKey(plr.getName())) {
            PlotSelection.currentSelection.remove(plr.getName());
        }
        PlotSelection.currentSelection.put(plr.getName(), selection);
        sendMessage(plr, C.CLIPBOARD_SET);
        return true;
    }
}
