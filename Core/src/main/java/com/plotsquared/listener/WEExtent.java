package com.plotsquared.listener;

import com.intellectualcrafters.plot.object.RegionWrapper;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.entity.BaseEntity;
import com.sk89q.worldedit.entity.Entity;
import com.sk89q.worldedit.extent.AbstractDelegateExtent;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.world.biome.BaseBiome;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockStateHolder;

import java.util.HashSet;

public class WEExtent extends AbstractDelegateExtent {

    private final HashSet<RegionWrapper> mask;

    public WEExtent(HashSet<RegionWrapper> mask, Extent extent) {
        super(extent);
        this.mask = mask;
    }

    @Override
    public boolean setBlock(BlockVector3 location, BlockStateHolder block) throws WorldEditException {
        return WEManager.maskContains(this.mask, location.getBlockX(), location.getBlockY(), location.getBlockZ()) && super.setBlock(location, block);
    }

    @Override
    public Entity createEntity(Location location, BaseEntity entity) {
        if (WEManager.maskContains(this.mask, location.getBlockX(), location.getBlockY(), location.getBlockZ())) {
            return super.createEntity(location, entity);
        }
        return null;
    }

    @Override
    public boolean setBiome(BlockVector2 position, BaseBiome biome) {
        return WEManager.maskContains(this.mask, position.getBlockX(), position.getBlockZ()) && super.setBiome(position, biome);
    }

    @Override
    public BlockState getBlock(BlockVector3 location) {
        if (WEManager.maskContains(this.mask, location.getBlockX(), location.getBlockY(), location.getBlockZ())) {
            return super.getBlock(location);
        }
        return WEManager.AIR;
    }
}
