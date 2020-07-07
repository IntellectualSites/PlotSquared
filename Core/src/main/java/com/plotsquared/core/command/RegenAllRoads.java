/*
 *       _____  _       _    _____                                _
 *      |  __ \| |     | |  / ____|                              | |
 *      | |__) | | ___ | |_| (___   __ _ _   _  __ _ _ __ ___  __| |
 *      |  ___/| |/ _ \| __|\___ \ / _` | | | |/ _` | '__/ _ \/ _` |
 *      | |    | | (_) | |_ ____) | (_| | |_| | (_| | | |  __/ (_| |
 *      |_|    |_|\___/ \__|_____/ \__, |\__,_|\__,_|_|  \___|\__,_|
 *                                    | |
 *                                    |_|
 *            PlotSquared plot management system for Minecraft
 *                  Copyright (C) 2020 IntellectualSites
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.plotsquared.core.command;

import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.configuration.Captions;
import com.plotsquared.core.generator.HybridPlotManager;
import com.plotsquared.core.generator.HybridUtils;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.PlotArea;
import com.plotsquared.core.plot.PlotManager;
import com.plotsquared.core.util.MainUtil;

@CommandDeclaration(command = "regenallroads",
    description = "Regenerate all roads in the map using the set road schematic",
    aliases = {"rgar"},
    usage = "/plot regenallroads <world> [height]",
    category = CommandCategory.ADMINISTRATION,
    requiredType = RequiredType.CONSOLE,
    permission = "plots.regenallroads")
public class RegenAllRoads extends SubCommand {

    @Override public boolean onCommand(PlotPlayer<?> player, String[] args) {
        int height = 0;
        if (args.length == 2) {
            try {
                height = Integer.parseInt(args[1]);
            } catch (NumberFormatException ignored) {
                MainUtil.sendMessage(player, Captions.NOT_VALID_NUMBER, "(0, 256)");
                MainUtil.sendMessage(player, Captions.COMMAND_SYNTAX,
                    "/plot regenallroads <world> [height]");
                return false;
            }
        } else if (args.length != 1) {
            MainUtil.sendMessage(player, Captions.COMMAND_SYNTAX,
                "/plot regenallroads <world> [height]");
            return false;
        }
        PlotArea area = PlotSquared.get().getPlotAreaManager().getPlotAreaByString(args[0]);
        if (area == null) {
            Captions.NOT_VALID_PLOT_WORLD.send(player, args[0]);
            return false;
        }
        String name = args[0];
        PlotManager manager = area.getPlotManager();
        if (!(manager instanceof HybridPlotManager)) {
            MainUtil.sendMessage(player, Captions.NOT_VALID_PLOT_WORLD);
            return false;
        }
        //Set<BlockVector2> chunks = ChunkManager.manager.getChunkChunks(name);
        MainUtil
            .sendMessage(player, "&cIf no schematic is set, the following will not do anything");
        MainUtil.sendMessage(player,
            "&7 - To set a schematic, stand in a plot and use &c/plot createroadschematic");
        //MainUtil.sendMessage(player, "&6Potential chunks to update: &7" + (chunks.size() * 1024));
        //MainUtil.sendMessage(player, "&6Estimated time: &7" + chunks.size() + " seconds");
        boolean result = HybridUtils.manager.scheduleRoadUpdate(area, height);
        if (!result) {
            MainUtil.sendMessage(player,
                "&cCannot schedule mass schematic update! (Is one already in progress?)");
            return false;
        }
        return true;
    }
}
