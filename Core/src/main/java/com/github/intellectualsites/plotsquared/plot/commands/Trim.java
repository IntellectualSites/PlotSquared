package com.github.intellectualsites.plotsquared.plot.commands;

import com.github.intellectualsites.plotsquared.commands.CommandDeclaration;
import com.github.intellectualsites.plotsquared.plot.PlotSquared;
import com.github.intellectualsites.plotsquared.plot.config.Captions;
import com.github.intellectualsites.plotsquared.plot.object.*;
import com.github.intellectualsites.plotsquared.plot.util.ChunkManager;
import com.github.intellectualsites.plotsquared.plot.util.MainUtil;
import com.github.intellectualsites.plotsquared.plot.util.TaskManager;
import com.github.intellectualsites.plotsquared.plot.util.WorldUtil;
import com.github.intellectualsites.plotsquared.plot.util.block.GlobalBlockQueue;
import com.github.intellectualsites.plotsquared.plot.util.block.LocalBlockQueue;
import com.github.intellectualsites.plotsquared.plot.util.expiry.ExpireManager;

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

@CommandDeclaration(command = "trim", permission = "plots.admin",
    description = "Delete unmodified portions of your plotworld",
    usage = "/plot trim <world> [regenerate]", requiredType = RequiredType.CONSOLE,
    category = CommandCategory.ADMINISTRATION) public class Trim extends SubCommand {

    public static ArrayList<Plot> expired = null;
    private static volatile boolean TASK = false;

    public static boolean getBulkRegions(final ArrayList<ChunkLoc> empty, final String world,
        final Runnable whenDone) {
        if (Trim.TASK) {
            return false;
        }
        TaskManager.runTaskAsync(new Runnable() {
            @Override public void run() {
                String directory = world + File.separator + "region";
                File folder = new File(PlotSquared.get().IMP.getWorldContainer(), directory);
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
                    ChunkLoc loc = new ChunkLoc(x, z);
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
        final RunnableVal2<Set<ChunkLoc>, Set<ChunkLoc>> result) {
        if (result == null) {
            return false;
        }
        MainUtil.sendMessage(null, "Collecting region data...");
        ArrayList<Plot> plots = new ArrayList<>();
        plots.addAll(PlotSquared.get().getPlots(world));
        if (ExpireManager.IMP != null) {
            plots.removeAll(ExpireManager.IMP.getPendingExpired());
        }
        result.value1 = new HashSet<>(ChunkManager.manager.getChunkChunks(world));
        result.value2 = new HashSet<>();
        MainUtil.sendMessage(null, " - MCA #: " + result.value1.size());
        MainUtil.sendMessage(null, " - CHUNKS: " + (result.value1.size() * 1024) + " (max)");
        MainUtil.sendMessage(null, " - TIME ESTIMATE: 12 Parsecs");
        TaskManager.objectTask(plots, new RunnableVal<Plot>() {
            @Override public void run(Plot plot) {
                Location pos1 = plot.getBottom();
                Location pos2 = plot.getTop();
                int ccx1 = pos1.getX() >> 9;
                int ccz1 = pos1.getZ() >> 9;
                int ccx2 = pos2.getX() >> 9;
                int ccz2 = pos2.getZ() >> 9;
                for (int x = ccx1; x <= ccx2; x++) {
                    for (int z = ccz1; z <= ccz2; z++) {
                        ChunkLoc loc = new ChunkLoc(x, z);
                        if (result.value1.remove(loc)) {
                            result.value2.add(loc);
                        }
                    }
                }
            }
        }, result);
        return true;
    }

    @Override public boolean onCommand(final PlotPlayer player, String[] args) {
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
        getTrimRegions(world, new RunnableVal2<Set<ChunkLoc>, Set<ChunkLoc>>() {
            @Override public void run(Set<ChunkLoc> viable, final Set<ChunkLoc> nonViable) {
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
                            Iterator<ChunkLoc> iterator = nonViable.iterator();
                            ChunkLoc mcr = iterator.next();
                            iterator.remove();
                            int cbx = mcr.x << 5;
                            int cbz = mcr.z << 5;
                            // get all 1024 chunks
                            HashSet<ChunkLoc> chunks = new HashSet<>();
                            for (int x = cbx; x < cbx + 32; x++) {
                                for (int z = cbz; z < cbz + 32; z++) {
                                    ChunkLoc loc = new ChunkLoc(x, z);
                                    chunks.add(loc);
                                }
                            }
                            int bx = cbx << 4;
                            int bz = cbz << 4;
                            RegionWrapper region = new RegionWrapper(bx, bx + 511, bz, bz + 511);
                            for (Plot plot : PlotSquared.get().getPlots(world)) {
                                Location bot = plot.getBottomAbs();
                                Location top = plot.getExtendedTopAbs();
                                RegionWrapper plotReg =
                                    new RegionWrapper(bot.getX(), top.getX(), bot.getZ(),
                                        top.getZ());
                                if (!region.intersects(plotReg)) {
                                    continue;
                                }
                                for (int x = plotReg.minX >> 4; x <= plotReg.maxX >> 4; x++) {
                                    for (int z = plotReg.minZ >> 4; z <= plotReg.maxZ >> 4; z++) {
                                        ChunkLoc loc = new ChunkLoc(x, z);
                                        chunks.remove(loc);
                                    }
                                }
                            }
                            final LocalBlockQueue queue =
                                GlobalBlockQueue.IMP.getNewQueue(world, false);
                            TaskManager.objectTask(chunks, new RunnableVal<ChunkLoc>() {
                                @Override public void run(ChunkLoc value) {
                                    queue.regenChunk(value.x, value.z);
                                }
                            }, this);
                        }
                    };
                } else {
                    regenTask = new Runnable() {
                        @Override public void run() {
                            Trim.TASK = false;
                            player.sendMessage("Trim done!");
                        }
                    };
                }
                ChunkManager.manager.deleteRegionFiles(world, viable, regenTask);

            }
        });
        return true;
    }
}
