package com.github.intellectualsites.plotsquared.listener;

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
