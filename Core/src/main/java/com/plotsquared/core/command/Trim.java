/*
 * PlotSquared, a land and world management plugin for Minecraft.
 * Copyright (C) IntellectualSites <https://intellectualsites.com>
 * Copyright (C) IntellectualSites team and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.plotsquared.core.command;

import com.google.inject.Inject;
import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.configuration.caption.StaticCaption;
import com.plotsquared.core.configuration.caption.TranslatableCaption;
import com.plotsquared.core.location.Location;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.world.PlotAreaManager;
import com.plotsquared.core.queue.GlobalBlockQueue;
import com.plotsquared.core.queue.QueueCoordinator;
import com.plotsquared.core.util.RegionManager;
import com.plotsquared.core.util.RegionUtil;
import com.plotsquared.core.util.WorldUtil;
import com.plotsquared.core.util.query.PlotQuery;
import com.plotsquared.core.util.task.RunnableVal;
import com.plotsquared.core.util.task.RunnableVal2;
import com.plotsquared.core.util.task.TaskManager;
import com.plotsquared.core.util.task.TaskTime;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.regions.CuboidRegion;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

@CommandDeclaration(command = "trim",
        permission = "plots.admin",
        usage = "/plot trim <world> [regenerate]",
        requiredType = RequiredType.CONSOLE,
        category = CommandCategory.ADMINISTRATION)
public class Trim extends SubCommand {

    private static final Logger LOGGER = LogManager.getLogger("PlotSquared/" + Trim.class.getSimpleName());
    private static volatile boolean TASK = false;

    private final PlotAreaManager plotAreaManager;
    private final WorldUtil worldUtil;
    private final GlobalBlockQueue blockQueue;
    private final RegionManager regionManager;

    @Inject
    public Trim(
            final @NonNull PlotAreaManager plotAreaManager,
            final @NonNull WorldUtil worldUtil,
            final @NonNull GlobalBlockQueue blockQueue,
            final @NonNull RegionManager regionManager
    ) {
        this.plotAreaManager = plotAreaManager;
        this.worldUtil = worldUtil;
        this.blockQueue = blockQueue;
        this.regionManager = regionManager;
    }

    /**
     * Runs the result task with the parameters (viable, nonViable).
     *
     * @param world  The world
     * @param result (viable = .mcr to trim, nonViable = .mcr keep)
     * @return success or not
     */
    public static boolean getTrimRegions(
            String world,
            final RunnableVal2<Set<BlockVector2>, Set<BlockVector2>> result
    ) {
        if (result == null) {
            return false;
        }
        TranslatableCaption.of("trim.trim_starting");
        final List<Plot> plots = PlotQuery.newQuery().inWorld(world).asList();
        if (PlotSquared.platform().expireManager() != null) {
            plots.removeAll(PlotSquared.platform().expireManager().getPendingExpired());
        }
        result.value1 = new HashSet<>(PlotSquared.platform().worldUtil().getChunkChunks(world));
        result.value2 = new HashSet<>();
        StaticCaption.of(" - MCA #: " + result.value1.size());
        StaticCaption.of(" - CHUNKS: " + (result.value1.size() * 1024) + " (max)");
        StaticCaption.of(" - TIME ESTIMATE: 12 Parsecs");
        TaskManager.getPlatformImplementation().objectTask(plots, new RunnableVal<>() {
            @Override
            public void run(Plot plot) {
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
        }).thenAccept(ignore ->
                TaskManager.getPlatformImplementation().taskLater(result, TaskTime.ticks(1L)));
        return true;
    }

    @Override
    public boolean onCommand(final PlotPlayer<?> player, String[] args) {
        if (args.length == 0) {
            sendUsage(player);
            return false;
        }
        final String world = args[0];
        if (!this.worldUtil.isWorld(world) || !this.plotAreaManager.hasPlotArea(world)) {
            player.sendMessage(TranslatableCaption.of("errors.not_valid_world"));
            return false;
        }
        if (Trim.TASK) {
            player.sendMessage(TranslatableCaption.of("trim.trim_in_progress"));
            return false;
        }
        Trim.TASK = true;
        final boolean regen = args.length == 2 && Boolean.parseBoolean(args[1]);
        getTrimRegions(world, new RunnableVal2<>() {
            @Override
            public void run(Set<BlockVector2> viable, final Set<BlockVector2> nonViable) {
                Runnable regenTask;
                if (regen) {
                    LOGGER.info("Starting regen task");
                    LOGGER.info(" - This is a VERY slow command");
                    LOGGER.info(" - It will say 'Trim done!' when complete");
                    regenTask = new Runnable() {
                        @Override
                        public void run() {
                            if (nonViable.isEmpty()) {
                                Trim.TASK = false;
                                player.sendMessage(TranslatableCaption.of("trim.trim_done"));
                                LOGGER.info("Trim done!");
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
                                    RegionUtil.createRegion(bx, bx + 511, 0, 0, bz, bz + 511);
                            for (Plot plot : PlotQuery.newQuery().inWorld(world)) {
                                Location bot = plot.getBottomAbs();
                                Location top = plot.getExtendedTopAbs();
                                CuboidRegion plotReg = RegionUtil
                                        .createRegion(bot.getX(), top.getX(), 0, 0, bot.getZ(), top.getZ());
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
                            final QueueCoordinator queue = blockQueue.getNewQueue(worldUtil.getWeWorld(world));
                            TaskManager.getPlatformImplementation().objectTask(chunks, new RunnableVal<>() {
                                @Override
                                public void run(BlockVector2 value) {
                                    queue.regenChunk(value.getX(), value.getZ());
                                }
                            }).thenAccept(ignore -> TaskManager.getPlatformImplementation()
                                    .taskLater(this, TaskTime.ticks(1L)));
                        }
                    };
                } else {
                    regenTask = () -> {
                        Trim.TASK = false;
                        player.sendMessage(TranslatableCaption.of("trim.trim_done"));
                        LOGGER.info("Trim done!");
                    };
                }
                regionManager.deleteRegionFiles(world, viable, regenTask);

            }
        });
        return true;
    }

}
