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

import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.object.PlotId;
import com.intellectualcrafters.plot.util.bukkit.BukkitPlayerFunctions;

public class Clipboard extends SubCommand {
    public Clipboard() {
        super(Command.CLIPBOARD, "View information about your current copy", "clipboard", CommandCategory.INFO, true);
    }
    
    @Override
    public boolean execute(final Player plr, final String... args) {
        if (!currentSelection.containsKey(plr.getName())) {
            return sendMessage(plr, C.NO_CLIPBOARD);
        }
        final PlotSelection selection = currentSelection.get(plr.getName());
        final PlotId plotId = selection.getPlot().getId();
        final int width = selection.getWidth();
        final int total = selection.getBlocks().length;
        String message = C.CLIPBOARD_INFO.s();
        message = message.replace("%id", plotId.toString()).replace("%width", width + "").replace("%total", total + "");
        BukkitPlayerFunctions.sendMessage(plr, message);
        return true;
    }
}
