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
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.StringMan;
import com.intellectualcrafters.plot.util.WorldUtil;
import com.plotsquared.general.commands.CommandDeclaration;

@CommandDeclaration(
        command = "setbiome",
        permission = "plots.set.biome",
        description = "Set the plot biome",
        usage = "/plot biome [biome]",
        aliases = {"biome", "sb", "setb", "b"},
        category = CommandCategory.APPEARANCE,
        requiredType = RequiredType.NONE)
public class Biome extends SetCommand {

    @Override
    public boolean set(final PlotPlayer plr, final Plot plot, final String value) {
        int biome = WorldUtil.IMP.getBiomeFromString(value);
        if (biome == -1) {
            String biomes = StringMan.join(WorldUtil.IMP.getBiomeList(), C.BLOCK_LIST_SEPARATER.s());
            C.NEED_BIOME.send(plr);
            MainUtil.sendMessage(plr, C.SUBCOMMAND_SET_OPTIONS_HEADER.s() + biomes);
            return false;
        }
        if (plot.getRunning() > 0) {
            MainUtil.sendMessage(plr, C.WAIT_FOR_TIMER);
            return false;
        }
        plot.addRunning();
        plot.setBiome(value.toUpperCase(), new Runnable() {
            @Override
            public void run() {
                plot.removeRunning();
                MainUtil.sendMessage(plr, C.BIOME_SET_TO.s() + value.toLowerCase());
            }
        });
        return true;
    }
}
