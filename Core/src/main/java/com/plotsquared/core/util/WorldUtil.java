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
package com.plotsquared.core.util;

import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.location.Location;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.util.task.RunnableVal;
import com.sk89q.jnbt.CompoundTag;
import com.sk89q.jnbt.IntTag;
import com.sk89q.jnbt.NBTInputStream;
import com.sk89q.jnbt.NBTOutputStream;
import com.sk89q.jnbt.Tag;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.entity.EntityType;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public abstract class WorldUtil {
    public static WorldUtil IMP;

    public abstract String getMainWorld();

    public abstract boolean isWorld(String worldName);

    public abstract void getSign(Location location, Consumer<String[]> result);

    /**
     * @deprecated May result in synchronous chunk loading
     */
    @Deprecated public abstract String[] getSignSynchronous(Location location);

    public abstract Location getSpawn(String world);

    public abstract void setSpawn(Location location);

    public abstract void saveWorld(String world);

    public abstract String getClosestMatchingName(BlockState plotBlock);

    public abstract boolean isBlockSolid(BlockState block);

    public abstract StringComparison<BlockState>.ComparisonResult getClosestBlock(String name);

    public abstract void getBiome(String world, int x, int z, Consumer<BiomeType> result);

    /**
     * @deprecated May result in synchronous chunk loading
     */
    @Deprecated public abstract BiomeType getBiomeSynchronous(String world, int x, int z);

    public abstract void getBlock(Location location, Consumer<BlockState> result);

    /**
     * @deprecated May result in synchronous chunk loading
     */
    @Deprecated public abstract BlockState getBlockSynchronous(Location location);

    public abstract void getHighestBlock(String world, int x, int z, final IntConsumer result);

    /**
     * @deprecated May result in synchronous chunk loading
     */
    @Deprecated public abstract int getHighestBlockSynchronous(String world, int x, int z);

    public abstract void setSign(String world, int x, int y, int z, String[] lines);

    public abstract void setBiomes(String world, CuboidRegion region, BiomeType biome);

    public abstract com.sk89q.worldedit.world.World getWeWorld(String world);

    public void upload(@NotNull final Plot plot, UUID uuid, String file,
        RunnableVal<URL> whenDone) {
        plot.getHome(home -> MainUtil.upload(uuid, file, "zip", new RunnableVal<OutputStream>() {
            @Override public void run(OutputStream output) {
                try (final ZipOutputStream zos = new ZipOutputStream(output)) {
                    File dat = getDat(plot.getWorldName());
                    Location spawn = getSpawn(plot.getWorldName());
                    if (dat != null) {
                        ZipEntry ze = new ZipEntry("world" + File.separator + dat.getName());
                        zos.putNextEntry(ze);
                        try (NBTInputStream nis = new NBTInputStream(
                            new GZIPInputStream(new FileInputStream(dat)))) {
                            CompoundTag tag = (CompoundTag) nis.readNamedTag().getTag();
                            CompoundTag data = (CompoundTag) tag.getValue().get("Data");
                            Map<String, Tag> map = ReflectionUtils.getMap(data.getValue());
                            map.put("SpawnX", new IntTag(home.getX()));
                            map.put("SpawnY", new IntTag(home.getY()));
                            map.put("SpawnZ", new IntTag(home.getZ()));
                            try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                                try (NBTOutputStream out = new NBTOutputStream(
                                    new GZIPOutputStream(baos, true))) {
                                    //TODO Find what this should be called
                                    out.writeNamedTag("Schematic????", tag);
                                }
                                zos.write(baos.toByteArray());
                            }
                        }
                    }
                    setSpawn(spawn);
                    byte[] buffer = new byte[1024];
                    for (Plot current : plot.getConnectedPlots()) {
                        Location bot = current.getBottomAbs();
                        Location top = current.getTopAbs();
                        int brx = bot.getX() >> 9;
                        int brz = bot.getZ() >> 9;
                        int trx = top.getX() >> 9;
                        int trz = top.getZ() >> 9;
                        Set<BlockVector2> files =
                            RegionManager.manager.getChunkChunks(bot.getWorldName());
                        for (BlockVector2 mca : files) {
                            if (mca.getX() >= brx && mca.getX() <= trx && mca.getZ() >= brz
                                && mca.getZ() <= trz) {
                                final File file =
                                    getMcr(plot.getWorldName(), mca.getX(), mca.getZ());
                                if (file != null) {
                                    //final String name = "r." + (x - cx) + "." + (z - cz) + ".mca";
                                    String name = file.getName();
                                    final ZipEntry ze = new ZipEntry(
                                        "world" + File.separator + "region" + File.separator
                                            + name);
                                    zos.putNextEntry(ze);
                                    try (FileInputStream in = new FileInputStream(file)) {
                                        int len;
                                        while ((len = in.read(buffer)) > 0) {
                                            zos.write(buffer, 0, len);
                                        }
                                    }
                                    zos.closeEntry();
                                }
                            }
                        }
                    }
                    zos.closeEntry();
                    zos.flush();
                    zos.finish();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }, whenDone));
    }

    public File getDat(String world) {
        File file = new File(
            PlotSquared.platform().getWorldContainer() + File.separator + world + File.separator
                + "level.dat");
        if (file.exists()) {
            return file;
        }
        return null;
    }

    public File getMcr(String world, int x, int z) {
        File file = new File(PlotSquared.platform().getWorldContainer(),
            world + File.separator + "region" + File.separator + "r." + x + '.' + z + ".mca");
        if (file.exists()) {
            return file;
        }
        return null;
    }

    public abstract boolean isBlockSame(BlockState block1, BlockState block2);

    public abstract PlotPlayer wrapPlayer(UUID uuid);

    public abstract double getHealth(PlotPlayer player);

    public abstract void setHealth(PlotPlayer player, double health);

    public abstract int getFoodLevel(PlotPlayer player);

    public abstract void setFoodLevel(PlotPlayer player, int foodLevel);

    public abstract Set<EntityType> getTypesInCategory(final String category);

    public abstract Collection<BlockType> getTileEntityTypes();

    public abstract int getTileEntityCount(String world, BlockVector2 chunk);

}
