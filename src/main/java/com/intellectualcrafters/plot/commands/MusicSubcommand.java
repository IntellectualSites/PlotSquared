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

import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.flag.Flag;
import com.intellectualcrafters.plot.flag.FlagManager;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotBlock;
import com.intellectualcrafters.plot.object.PlotInventory;
import com.intellectualcrafters.plot.object.PlotItemStack;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.BlockManager;
import com.intellectualcrafters.plot.util.MainUtil;
import com.plotsquared.general.commands.CommandDeclaration;

@CommandDeclaration(
        command = "music",
        permission = "plots.music",
        description = "Player music in a plot",
        usage = "/plot music",
        category = CommandCategory.ACTIONS,
        requiredType = RequiredType.NONE
)
public class MusicSubcommand extends SubCommand {

    @Override
    public boolean onCommand(final PlotPlayer player, final String[] args) {
        final Location loc = player.getLocation();
        final Plot plot = MainUtil.getPlot(loc);
        if (plot == null) {
            return !sendMessage(player, C.NOT_IN_PLOT);
        }
        if (!plot.isAdded(player.getUUID())) {
            sendMessage(player, C.NO_PLOT_PERMS);
            return true;
        }
        PlotInventory inv = new PlotInventory(player, 2, "Plot Jukebox") {
            public boolean onClick(int index) {
                PlotItemStack item = getItem(index);
                if (item == null) {
                    return true;
                }
                int id = item.id == 7 ? 0 : item.id;
                if (id == 0) {
                    FlagManager.removePlotFlag(plot, "music");
                }
                else {
                    FlagManager.addPlotFlag(plot, new Flag(FlagManager.getFlag("music"), id));
                }
                return false;
            }
        };
        int index = 0;
        for (int i = 2256; i < 2268; i++) {
            String name = "&r&6" + BlockManager.manager.getClosestMatchingName(new PlotBlock((short) i, (byte) 0));
            String[] lore = {"&r&aClick to play!"};
            PlotItemStack item = new PlotItemStack(i, (byte) 0, 1, name, lore);
            inv.setItem(index, item);
            index++;
        }
        if (player.getMeta("music") != null) {
            String name = "&r&6Cancel music";
            String[] lore = {"&r&cClick to cancel!"};
            inv.setItem(index, new PlotItemStack(7, (short) 0, 1, name, lore));
        }
        inv.openInventory();
        return true;
    }
}
