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
package com.plotsquared.core.queue;

import com.plotsquared.core.location.Location;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.util.SchematicHandler;
import com.plotsquared.core.util.StringMan;
import com.plotsquared.core.util.uuid.UUIDHandler;
import com.plotsquared.core.util.WorldUtil;
import com.plotsquared.core.util.PatternUtil;
import com.sk89q.jnbt.CompoundTag;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockState;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public abstract class LocalBlockQueue {

    @Getter @Setter private boolean forceSync = false;
    @Getter @Setter @Nullable private Object chunkObject;

    /**
     * Needed for compatibility with FAWE.
     *
     * @param world unused
     */
    @Deprecated public LocalBlockQueue(String world) {
    }

    public ScopedLocalBlockQueue getForChunk(int x, int z) {
        int bx = x << 4;
        int bz = z << 4;
        return new ScopedLocalBlockQueue(this, new Location(getWorld(), bx, 0, bz),
            new Location(getWorld(), bx + 15, 255, bz + 15));
    }

    public abstract boolean next();

    public abstract void startSet(boolean parallel);

    public abstract void endSet(boolean parallel);

    public abstract int size();

    public abstract void optimize();

    public abstract long getModified();

    public abstract void setModified(long modified);

    /**
     * Sets the block at the coordinates provided to the given id.
     *
     * @param x  the x coordinate from from 0 to 15 inclusive
     * @param y  the y coordinate from from 0 (inclusive) - maxHeight(exclusive)
     * @param z  the z coordinate from 0 to 15 inclusive
     * @param id the id to set the block to
     */
    public abstract boolean setBlock(final int x, final int y, final int z, final BlockState id);

    public abstract boolean setBlock(final int x, final int y, final int z, final BaseBlock id);

    public boolean setBlock(final int x, final int y, final int z, @NotNull final Pattern pattern) {
        return setBlock(x, y, z, PatternUtil.apply(pattern, x, y, z));
    }

    public boolean setTile(int x, int y, int z, CompoundTag tag) {
        SchematicHandler.manager.restoreTile(this, tag, x, y, z);
        return true;
    }

    public abstract BlockState getBlock(int x, int y, int z);

    public abstract boolean setBiome(int x, int z, BiomeType biome);

    public abstract boolean setBiome();

    public abstract String getWorld();

    public abstract void flush();

    public final void setModified() {
        setModified(System.currentTimeMillis());
    }

    public abstract void refreshChunk(int x, int z);

    public abstract void fixChunkLighting(int x, int z);

    public abstract void regenChunk(int x, int z);

    public final void regenChunkSafe(int x, int z) {
        regenChunk(x, z);
        fixChunkLighting(x, z);
        BlockVector2 loc = BlockVector2.at(x, z);
        for (Map.Entry<String, PlotPlayer> entry : UUIDHandler.getPlayers().entrySet()) {
            PlotPlayer pp = entry.getValue();
            Location pLoc = pp.getLocation();
            if (!StringMan.isEqual(getWorld(), pLoc.getWorld()) || !pLoc.getBlockVector2()
                .equals(loc)) {
                continue;
            }
            pLoc.setY(WorldUtil.IMP.getHighestBlockSynchronous(getWorld(), pLoc.getX(), pLoc.getZ()));
            pp.teleport(pLoc);
        }
    }

    public boolean enqueue() {
        return GlobalBlockQueue.IMP.enqueue(this);
    }

    public void setCuboid(Location pos1, Location pos2, BlockState block) {
        int yMin = Math.min(pos1.getY(), pos2.getY());
        int yMax = Math.min(255, Math.max(pos1.getY(), pos2.getY()));
        int xMin = Math.min(pos1.getX(), pos2.getX());
        int xMax = Math.max(pos1.getX(), pos2.getX());
        int zMin = Math.min(pos1.getZ(), pos2.getZ());
        int zMax = Math.max(pos1.getZ(), pos2.getZ());
        for (int y = yMin; y <= yMax; y++) {
            for (int x = xMin; x <= xMax; x++) {
                for (int z = zMin; z <= zMax; z++) {
                    setBlock(x, y, z, block);
                }
            }
        }
    }

    public void setCuboid(Location pos1, Location pos2, Pattern blocks) {
        int yMin = Math.min(pos1.getY(), pos2.getY());
        int yMax = Math.min(255, Math.max(pos1.getY(), pos2.getY()));
        int xMin = Math.min(pos1.getX(), pos2.getX());
        int xMax = Math.max(pos1.getX(), pos2.getX());
        int zMin = Math.min(pos1.getZ(), pos2.getZ());
        int zMax = Math.max(pos1.getZ(), pos2.getZ());
        for (int y = yMin; y <= yMax; y++) {
            for (int x = xMin; x <= xMax; x++) {
                for (int z = zMin; z <= zMax; z++) {
                    setBlock(x, y, z, blocks);
                }
            }
        }
    }
}
