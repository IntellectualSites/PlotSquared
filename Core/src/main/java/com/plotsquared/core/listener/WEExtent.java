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
package com.plotsquared.core.listener;

import com.plotsquared.core.util.WEManager;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.entity.BaseEntity;
import com.sk89q.worldedit.entity.Entity;
import com.sk89q.worldedit.extent.AbstractDelegateExtent;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import com.sk89q.worldedit.world.block.BlockTypes;

import java.util.Set;

public class WEExtent extends AbstractDelegateExtent {

    public static BlockState AIRSTATE = BlockTypes.AIR.getDefaultState();
    public static BaseBlock AIRBASE = BlockTypes.AIR.getDefaultState().toBaseBlock();
    private final Set<CuboidRegion> mask;

    public WEExtent(Set<CuboidRegion> mask, Extent extent) {
        super(extent);
        this.mask = mask;
    }

    @Override public boolean setBlock(BlockVector3 location, BlockStateHolder block)
        throws WorldEditException {
        return WEManager.maskContains(this.mask, location.getX(), location.getY(), location.getZ())
            && super.setBlock(location, block);
    }

    @Override public Entity createEntity(Location location, BaseEntity entity) {
        if (WEManager.maskContains(this.mask, location.getBlockX(), location.getBlockY(),
            location.getBlockZ())) {
            return super.createEntity(location, entity);
        }
        return null;
    }

    @Override public boolean setBiome(BlockVector2 position, BiomeType biome) {
        return WEManager.maskContains(this.mask, position.getX(), position.getZ()) && super
            .setBiome(position, biome);
    }

    @Override public BlockState getBlock(BlockVector3 location) {
        if (WEManager.maskContains(this.mask, location.getX(), location.getY(), location.getZ())) {
            return super.getBlock(location);
        }
        return AIRSTATE;
    }

    @Override public BaseBlock getFullBlock(BlockVector3 location) {
        if (WEManager.maskContains(this.mask, location.getX(), location.getY(), location.getZ())) {
            return super.getFullBlock(location);
        }
        return AIRBASE;
    }
}
