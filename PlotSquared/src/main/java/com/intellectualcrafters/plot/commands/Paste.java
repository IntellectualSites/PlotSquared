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
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.bukkit.BukkitPlayerFunctions;
import com.intellectualcrafters.plot.util.bukkit.UUIDHandler;

public class Paste extends SubCommand {
    public Paste() {
        super(Command.PASTE, "Paste a plot", "paste", CommandCategory.ACTIONS, true);
    }
    
    @Override
    public boolean execute(final Player plr, final String... args) {
        if (!BukkitPlayerFunctions.isInPlot(plr)) {
            BukkitPlayerFunctions.sendMessage(plr, C.NOT_IN_PLOT);
            return false;
        }
        final Plot plot = BukkitPlayerFunctions.getCurrentPlot(plr);
        if (((plot == null) || !plot.hasOwner() || !plot.getOwner().equals(UUIDHandler.getUUID(plr))) && !BukkitMain.hasPermission(plr, "plots.admin.command.paste")) {
            BukkitPlayerFunctions.sendMessage(plr, C.NO_PLOT_PERMS);
            return false;
        }
        if (!BukkitPlayerFunctions.getTopPlot(plr.getWorld(), plot).equals(BukkitPlayerFunctions.getBottomPlot(plr.getWorld(), plot))) {
            BukkitPlayerFunctions.sendMessage(plr, C.UNLINK_REQUIRED);
            return false;
        }
        assert plot != null;
        final int size = (MainUtil.getPlotTopLocAbs(plr.getWorld(), plot.getId()).getBlockX() - MainUtil.getPlotBottomLocAbs(plr.getWorld(), plot.getId()).getBlockX());
        if (PlotSelection.currentSelection.containsKey(plr.getName())) {
            final PlotSelection selection = PlotSelection.currentSelection.get(plr.getName());
            if (size != selection.getWidth()) {
                sendMessage(plr, C.PASTE_FAILED, "The size of the current plot is not the same as the paste");
                return false;
            }
            selection.paste(plr.getWorld(), plot);
            sendMessage(plr, C.PASTED);
        } else {
            sendMessage(plr, C.NO_CLIPBOARD);
            return false;
        }
        PlotSelection.currentSelection.remove(plr.getName());
        return true;
    }
}
