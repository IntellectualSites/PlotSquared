package com.intellectualcrafters.plot.util;

import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.config.Settings;
import com.intellectualcrafters.plot.flag.FlagManager;
import com.intellectualcrafters.plot.generator.ClassicPlotWorld;
import com.intellectualcrafters.plot.object.BO3;
import com.intellectualcrafters.plot.object.ChunkLoc;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotArea;
import com.intellectualcrafters.plot.object.PlotBlock;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.object.RegionWrapper;
import com.intellectualcrafters.plot.object.RunnableVal;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class BO3Handler {

    /**
     * @see #saveBO3(PlotPlayer, Plot, RunnableVal)
     * @param plot
     * @return if successfully exported
     */
    public static boolean saveBO3(Plot plot) {
        return saveBO3(null, plot);
    }

    public static boolean saveBO3(PlotPlayer player, final Plot plot) {
        return saveBO3(player, plot, new RunnableVal<BO3>() {
            @Override
            public void run(BO3 bo3) {
                save(plot, bo3);
            }
        });
    }

    public static boolean contains(PlotBlock[] blocks, PlotBlock block) {
        for (PlotBlock item : blocks) {
            if (item.equals(block)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Save a plot as a BO3 file.
     *  - Use null for the player object if no player is applicable
     * @param player
     * @param plot
     * @return
     */
    public static boolean saveBO3(PlotPlayer player, Plot plot, RunnableVal<BO3> saveTask) {
        if (saveTask == null) {
            throw new IllegalArgumentException("Save task cannot be null!");
        }
        PlotArea plotworld = plot.getArea();
        if (!(plotworld instanceof ClassicPlotWorld) || plotworld.TYPE != 0) {
            MainUtil.sendMessage(player, "BO3 exporting only supports type 0 classic generation.");
            return false;
        }
        String alias = plot.toString();
        Location[] corners = plot.getCorners();
        Location bot = corners[0];
        Location top = corners[1];
        ClassicPlotWorld cpw = (ClassicPlotWorld) plotworld;
        int height = cpw.PLOT_HEIGHT;

        int cx = (bot.getX() + top.getX()) / 2;
        int cz = (bot.getZ() + top.getZ()) / 2;

        HashMap<ChunkLoc, BO3> map = new HashMap<>();

        HashSet<RegionWrapper> regions = plot.getRegions();
        ArrayList<ChunkLoc> chunks = new ArrayList<>();
        for (RegionWrapper region : regions) {
            for (int x = region.minX >> 4; x <= region.maxX >> 4; x++) {
                for (int z = region.minZ >> 4; z <= region.maxZ >> 4; z++) {
                    chunks.add(new ChunkLoc(x, z));
                }
            }
        }
        for (ChunkLoc loc : chunks) {
            ChunkManager.manager.loadChunk(plot.getArea().worldname, loc, false);
        }

        boolean content = false;
        for (RegionWrapper region : regions) {
            Location pos1 = new Location(plotworld.worldname, region.minX, region.minY, region.minZ);
            Location pos2 = new Location(plotworld.worldname, region.maxX, region.maxY, region.maxZ);
            for (int x = pos1.getX(); x <= pos2.getX(); x++) {
                int X = x + 7 - cx >> 4;
                int xx = (x - cx) % 16;
                for (int z = pos1.getZ(); z <= pos2.getZ(); z++) {
                    int Z = z + 7 - cz >> 4;
                    int zz = (z - cz) % 16;
                    ChunkLoc loc = new ChunkLoc(X, Z);
                    BO3 bo3 = map.get(loc);
                    for (int y = 1; y < height; y++) {
                        PlotBlock block = WorldUtil.IMP.getBlock(new Location(plot.getArea().worldname, x, y, z));
                        if (!contains(cpw.MAIN_BLOCK, block)) {
                            if (bo3 == null) {
                                bo3 = new BO3(alias, plotworld.worldname, loc);
                                map.put(loc, bo3);
                                content = true;
                            }
                            bo3.addBlock(xx, y - height - 1, zz, block);
                        }
                    }
                    PlotBlock floor = WorldUtil.IMP.getBlock(new Location(plot.getArea().worldname, x, height, z));
                    if (!contains(cpw.TOP_BLOCK, floor)) {
                        if (bo3 == null) {
                            bo3 = new BO3(alias, plotworld.worldname, loc);
                            map.put(loc, bo3);
                            content = true;
                        }
                        bo3.addBlock(xx, -1, zz, floor);
                    }
                    for (int y = height + 1; y < 256; y++) {
                        PlotBlock block = WorldUtil.IMP.getBlock(new Location(plot.getArea().worldname, x, y, z));
                        if (block.id != 0) {
                            if (bo3 == null) {
                                bo3 = new BO3(alias, plotworld.worldname, loc);
                                map.put(loc, bo3);
                                content = true;
                            }
                            bo3.addBlock(xx, y - height - 1, zz, block);
                        }
                    }
                }
            }
        }

        if (!content) {
            MainUtil.sendMessage(player, "No content found!");
            return false;
        }

        for (Entry<ChunkLoc, BO3> entry : map.entrySet()) {
            ChunkLoc chunk = entry.getKey();
            BO3 bo3 = entry.getValue();
            if (chunk.x == 0 && chunk.z == 0) {
                continue;
            }
            int x = chunk.x;
            int z = chunk.z;
            if (Math.abs(chunk.x) > Math.abs(chunk.z)) {
                x += chunk.x > 0 ? -1 : 1;
            } else {
                z += chunk.z > 0 ? -1 : 1;
            }
            ChunkLoc parentLoc = new ChunkLoc(x, z);
            if (!map.containsKey(parentLoc)) {
                parentLoc = null;
                for (Entry<ChunkLoc, BO3> entry2 : map.entrySet()) {
                    ChunkLoc other = entry2.getKey();
                    if (other.x == chunk.x - 1 && other.z == chunk.z || other.z == chunk.z - 1 && other.x == chunk.x) {
                        parentLoc = other;
                    }
                }
                if (parentLoc == null) {
                    MainUtil.sendMessage(player,
                            "Exporting BO3 cancelled due to detached chunk: " + chunk + " - Make sure you only have one object per plot");
                    return false;
                }
            }
            map.get(parentLoc).addChild(bo3);
        }

        for (Entry<ChunkLoc, BO3> entry : map.entrySet()) {
            saveTask.run(entry.getValue());
        }

        MainUtil.sendMessage(player, "BO3 exporting was successful!");
        return true;
    }

    public static void upload(final Plot plot, UUID uuid, String file, RunnableVal<URL> whenDone) {
        if (plot == null) {
            throw new IllegalArgumentException("Arguments may not be null!");
        }
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (final ZipOutputStream zos = new ZipOutputStream(baos)) {
            saveBO3(null, plot, new RunnableVal<BO3>() {
                @Override
                public void run(BO3 bo3) {
                    try {
                        ZipEntry ze = new ZipEntry(bo3.getFilename());
                        zos.putNextEntry(ze);
                        write(zos, plot, bo3);
                        zos.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
            whenDone.run();
            return;
        }
        MainUtil.upload(uuid, file, "zip", new RunnableVal<OutputStream>() {
            @Override
            public void run(OutputStream output) {
                try {
                    output.write(baos.toByteArray());
                    baos.flush();
                    baos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }, whenDone);
    }

    public static void write(OutputStream stream, Plot plot, BO3 bo3) throws IOException {
        File base = getBaseFile(bo3.getWorld());
        List<String> lines = Files.readAllLines(base.toPath(), StandardCharsets.UTF_8);
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            String result = StringMan
                    .replaceAll(line, "%owner%", MainUtil.getName(plot.owner), "%alias%", plot.toString(), "%blocks%", bo3.getBlocks(), "%branches%",
                            bo3.getChildren(),
                            "%flags%", StringMan.join(FlagManager.getPlotFlags(plot).values(), ","));
            if (!StringMan.isEqual(result, line)) {
                lines.set(i, result);
            }
        }
        stream.write(StringMan.join(lines, System.getProperty("line.separator")).getBytes());
    }

    public static boolean save(Plot plot, BO3 bo3) {
        try {
            File bo3File = bo3.getFile();
            bo3File.createNewFile();
            bo3File.getParentFile().mkdirs();
            try (FileOutputStream fos = new FileOutputStream(bo3File)) {
                write(fos, plot, bo3);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        File base = getBaseFile(plot.getArea().worldname);
        try {
            List<String> lines = Files.readAllLines(base.toPath(), StandardCharsets.UTF_8);
            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i).trim();
                String result = StringMan
                        .replaceAll(line, "%owner%", MainUtil.getName(plot.owner), "%alias%", plot.toString(), "%blocks%", bo3.getBlocks(),
                                "%branches%", bo3.getChildren(),
                                "%flags%", StringMan.join(FlagManager.getPlotFlags(plot).values(), ","));
                if (!StringMan.isEqual(result, line)) {
                    lines.set(i, result);
                }
            }
            File bo3File;
            if (bo3.getLoc().x == 0 && bo3.getLoc().z == 0) {
                bo3File = MainUtil.getFile(base.getParentFile(), bo3.getName() + ".bo3");
            } else {
                bo3File = MainUtil.getFile(base.getParentFile(), bo3.getName() + '_' + bo3.getLoc().x + '_' + bo3.getLoc().z + ".bo3");
            }
            bo3File.createNewFile();
            Files.write(bo3File.toPath(), StringMan.join(lines, System.getProperty("line.separator")).getBytes(), StandardOpenOption.WRITE);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static File getBaseFile(String category) {
        File base = MainUtil.getFile(PS.get().IMP.getDirectory(), Settings.Paths.BO3 + File.separator + category + File.separator + "base.yml");
        if (!base.exists()) {
            PS.get().copyFile("base.yml", Settings.Paths.BO3 + File.separator + category);
        }
        return base;
    }
}
