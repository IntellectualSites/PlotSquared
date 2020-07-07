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
import com.plotsquared.core.location.Location;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.expiration.ExpireManager;
import com.plotsquared.core.queue.GlobalBlockQueue;
import com.plotsquared.core.queue.LocalBlockQueue;
import com.plotsquared.core.util.MainUtil;
import com.plotsquared.core.util.RegionManager;
import com.plotsquared.core.util.RegionUtil;
import com.plotsquared.core.util.WorldUtil;
import com.plotsquared.core.util.task.RunnableVal;
import com.plotsquared.core.util.task.RunnableVal2;
import com.plotsquared.core.util.task.TaskManager;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.regions.CuboidRegion;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

@CommandDeclaration(command = "trim",
    permission = "plots.admin",
    description = "Delete unmodified portions of your plotworld",
    usage = "/plot trim <world> [regenerate]",
    requiredType = RequiredType.CONSOLE,
    category = CommandCategory.ADMINISTRATION)
public class Trim extends SubCommand {

    public static ArrayList<Plot> expired = null;
    private static volatile boolean TASK = false;

    public static boolean getBulkRegions(final ArrayList<BlockVector2> empty, final String world,
        final Runnable whenDone) {
        if (Trim.TASK) {
            return false;
        }
        TaskManager.runTaskAsync(new Runnable() {
            @Override public void run() {
                String directory = world + File.separator + "region";
                File folder = new File(PlotSquared.platform().getWorldContainer(), directory);
                File[] regionFiles = folder.listFiles();
                for (File file : regionFiles) {
                    String name = file.getName();
                    if (name.endsWith("mca")) {
                        if (file.getTotalSpace() <= 8192) {
                            checkMca(name);
                        } else {
                            Path path = Paths.get(file.getPath());
                            try {
                                BasicFileAttributes attr =
                                    Files.readAttributes(path, BasicFileAttributes.class);
                                long creation = attr.creationTime().toMillis();
                                long modification = file.lastModified();
                                long diff = Math.abs(creation - modification);
                                if (diff < 10000) {
                                    checkMca(name);
                                }
                            } catch (IOException ignored) {
                            }
                        }
                    }
                }
                Trim.TASK = false;
                TaskManager.runTaskAsync(whenDone);
            }

            private void checkMca(String name) {
                try {
                    String[] split = name.split("\\.");
                    int x = Integer.parseInt(split[1]);
                    int z = Integer.parseInt(split[2]);
                    BlockVector2 loc = BlockVector2.at(x, z);
                    empty.add(loc);
                } catch (NumberFormatException ignored) {
                    PlotSquared.debug("INVALID MCA: " + name);
                }
            }
        });
        Trim.TASK = true;
        return true;
    }

    /**
     * Runs the result task with the parameters (viable, nonViable).
     *
     * @param world  The world
     * @param result (viable = .mcr to trim, nonViable = .mcr keep)
     * @return
     */
    public static boolean getTrimRegions(String world,
        final RunnableVal2<Set<BlockVector2>, Set<BlockVector2>> result) {
        if (result == null) {
            return false;
        }
        MainUtil.sendMessage(null, "Collecting region data...");
        ArrayList<Plot> plots = new ArrayList<>(PlotSquared.get().getPlots(world));
        if (ExpireManager.IMP != null) {
            plots.removeAll(ExpireManager.IMP.getPendingExpired());
        }
        result.value1 = new HashSet<>(RegionManager.manager.getChunkChunks(world));
        result.value2 = new HashSet<>();
        MainUtil.sendMessage(null, " - MCA #: " + result.value1.size());
        MainUtil.sendMessage(null, " - CHUNKS: " + (result.value1.size() * 1024) + " (max)");
        MainUtil.sendMessage(null, " - TIME ESTIMATE: 12 Parsecs");
        TaskManager.objectTask(plots, new RunnableVal<Plot>() {
            @Override public void run(Plot plot) {
                Location pos1 = plot.getCorners()[0];
                Location pos2 = plot.getCorners()[1];
                int ccx1 = pos1.getX() >> 9;
                int ccz1 = pos1.getZ() >> 9;
                int ccx2 = pos2.getX() >> 9;
                int ccz2 = pos2.getZ() >> 9;
                for (int x = ccx1; x <= ccx2; x++) {
                    for (int z = ccz1; z <= ccz2; z++) {
                        BlockVector2 loc = BlockVector2.at(x, z);
                        if (result.value1.remove(loc)) {
                            result.value2.add(loc);
                        }
                    }
                }
            }
        }, result);
        return true;
    }

    @Override public boolean onCommand(final PlotPlayer<?> player, String[] args) {
        if (args.length == 0) {
            Captions.COMMAND_SYNTAX.send(player, getUsage());
            return false;
        }
        final String world = args[0];
        if (!WorldUtil.IMP.isWorld(world) || !PlotSquared.get().hasPlotArea(world)) {
            MainUtil.sendMessage(player, Captions.NOT_VALID_WORLD);
            return false;
        }
        if (Trim.TASK) {
            Captions.TRIM_IN_PROGRESS.send(player);
            return false;
        }
        Trim.TASK = true;
        final boolean regen = args.length == 2 && Boolean.parseBoolean(args[1]);
        getTrimRegions(world, new RunnableVal2<Set<BlockVector2>, Set<BlockVector2>>() {
            @Override public void run(Set<BlockVector2> viable, final Set<BlockVector2> nonViable) {
                Runnable regenTask;
                if (regen) {
                    PlotSquared.log("Starting regen task:");
                    PlotSquared.log(" - This is a VERY slow command");
                    PlotSquared.log(" - It will say `Trim done!` when complete");
                    regenTask = new Runnable() {
                        @Override public void run() {
                            if (nonViable.isEmpty()) {
                                Trim.TASK = false;
                                player.sendMessage("Trim done!");
                                return;
                            }
                            Iterator<BlockVector2> iterator = nonViable.iterator();
                            BlockVector2 mcr = iterator.next();
                            iterator.remove();
                            int cbx = mcr.getX() << 5;
                            int cbz = mcr.getZ() << 5;
                            // get all 1024 chunks
                            HashSet<BlockVector2> chunks = new HashSet<>();
                            for (int x = cbx; x < cbx + 32; x++) {
                                for (int z = cbz; z < cbz + 32; z++) {
                                    BlockVector2 loc = BlockVector2.at(x, z);
                                    chunks.add(loc);
                                }
                            }
                            int bx = cbx << 4;
                            int bz = cbz << 4;
                            CuboidRegion region =
                                RegionUtil.createRegion(bx, bx + 511, bz, bz + 511);
                            for (Plot plot : PlotSquared.get().getPlots(world)) {
                                Location bot = plot.getBottomAbs();
                                Location top = plot.getExtendedTopAbs();
                                CuboidRegion plotReg = RegionUtil
                                    .createRegion(bot.getX(), top.getX(), bot.getZ(), top.getZ());
                                if (!RegionUtil.intersects(region, plotReg)) {
                                    continue;
                                }
                                for (int x = plotReg.getMinimumPoint().getX() >> 4;
                                     x <= plotReg.getMaximumPoint().getX() >> 4; x++) {
                                    for (int z = plotReg.getMinimumPoint().getZ() >> 4;
                                         z <= plotReg.getMaximumPoint().getZ() >> 4; z++) {
                                        BlockVector2 loc = BlockVector2.at(x, z);
                                        chunks.remove(loc);
                                    }
                                }
                            }
                            final LocalBlockQueue queue =
                                GlobalBlockQueue.IMP.getNewQueue(world, false);
                            TaskManager.objectTask(chunks, new RunnableVal<BlockVector2>() {
                                @Override public void run(BlockVector2 value) {
                                    queue.regenChunk(value.getX(), value.getZ());
                                }
                            }, this);
                        }
                    };
                } else {
                    regenTask = () -> {
                        Trim.TASK = false;
                        player.sendMessage("Trim done!");
                    };
                }
                RegionManager.manager.deleteRegionFiles(world, viable, regenTask);

            }
        });
        return true;
    }
}
