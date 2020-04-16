/*
 *
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
import com.plotsquared.core.config.Captions;
import com.plotsquared.core.location.Location;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.PlotArea;
import com.plotsquared.core.plot.PlotId;
import com.plotsquared.core.plot.PlotManager;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.util.StringWrapper;
import com.plotsquared.core.util.ChunkManager;
import com.plotsquared.core.util.MainUtil;
import com.plotsquared.core.util.uuid.UUIDHandler;
import com.plotsquared.core.util.WorldUtil;
import com.google.common.collect.BiMap;
import com.sk89q.worldedit.math.BlockVector2;

import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@CommandDeclaration(command = "debugclaimtest",
    description =
        "If you accidentally delete your database, this command will attempt to restore all plots based on the data from plot signs. "
            + "Execution time may vary",
    category = CommandCategory.DEBUG,
    requiredType = RequiredType.CONSOLE,
    permission = "plots.debugclaimtest")
public class DebugClaimTest extends SubCommand {

    @Override public boolean onCommand(final PlotPlayer player, String[] args) {
        if (args.length < 3) {
            return !MainUtil.sendMessage(null,
                "If you accidentally delete your database, this command will attempt to restore all plots based on the data from the "
                    + "plot signs. \n\n&cMissing world arg /plot debugclaimtest {world} {PlotId min} {PlotId max}");
        }
        PlotArea area = PlotSquared.get().getPlotAreaByString(args[0]);
        if (area == null || !WorldUtil.IMP.isWorld(area.getWorldName())) {
            Captions.NOT_VALID_PLOT_WORLD.send(player, args[0]);
            return false;
        }
        PlotId min, max;
        try {
            args[1].split(";");
            args[2].split(";");
            min = PlotId.fromString(args[1]);
            max = PlotId.fromString(args[2]);
        } catch (Exception ignored) {
            return !MainUtil.sendMessage(player,
                "&cInvalid min/max values. &7The values are to Plot IDs in the format &cX;Y &7where X;Y are the plot coords\nThe conversion "
                    + "will only check the plots in the selected area.");
        }
        MainUtil.sendMessage(player,
            "&3Sign Block&8->&3Plot&8: &7Beginning sign to plot conversion. This may take a while...");
        MainUtil.sendMessage(player,
            "&3Sign Block&8->&3Plot&8: Found an excess of 250,000 chunks. Limiting search radius... (~3.8 min)");
        PlotManager manager = area.getPlotManager();
        CompletableFuture.runAsync(() -> {
            ArrayList<PlotId> ids = MainUtil.getPlotSelectionIds(min, max);
            MainUtil.sendMessage(player,
                "&3Sign Block&8->&3Plot&8: " + ids.size() + " plot ids to check!");
            for (PlotId id : ids) {
                Plot plot = area.getPlotAbs(id);
                if (plot.hasOwner()) {
                    MainUtil.sendMessage(player, " - &cDB Already contains: " + plot.getId());
                    continue;
                }
                Location location = manager.getSignLoc(plot);
                BlockVector2 chunk = BlockVector2.at(location.getX() >> 4, location.getZ() >> 4);
                ChunkManager.manager.loadChunk(area.getWorldName(), chunk, false).thenRun(() -> {
                    String[] lines = WorldUtil.IMP.getSignSynchronous(location);
                    if (lines != null) {
                        String line = lines[2];
                        if (line != null && line.length() > 2) {
                            line = line.substring(2);
                            BiMap<StringWrapper, UUID> map = UUIDHandler.getUuidMap();
                            UUID uuid = map.get(new StringWrapper(line));
                            if (uuid == null) {
                                for (Map.Entry<StringWrapper, UUID> stringWrapperUUIDEntry : map
                                    .entrySet()) {
                                    if (stringWrapperUUIDEntry.getKey().value.toLowerCase()
                                        .startsWith(line.toLowerCase())) {
                                        uuid = stringWrapperUUIDEntry.getValue();
                                        break;
                                    }
                                }
                            }
                            if (uuid == null) {
                                uuid = UUIDHandler.getUUID(line, null);
                            }
                            if (uuid != null) {
                                MainUtil.sendMessage(player,
                                    " - &aFound plot: " + plot.getId() + " : " + line);
                                plot.setOwner(uuid);
                                MainUtil.sendMessage(player, " - &8Updated plot: " + plot.getId());
                            } else {
                                MainUtil.sendMessage(player,
                                    " - &cInvalid PlayerName: " + plot.getId() + " : " + line);
                            }
                        }
                    }
                }).join();
            }
        }).thenRun(() -> MainUtil.sendMessage(player, "&3Sign Block&8->&3Plot&8: Finished scan."));
        return true;
    }
}
