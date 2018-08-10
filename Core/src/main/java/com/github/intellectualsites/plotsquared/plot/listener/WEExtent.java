package com.github.intellectualsites.plotsquared.plot.listener;

import com.github.intellectualsites.plotsquared.plot.object.RegionWrapper;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.Vector2D;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.entity.BaseEntity;
import com.sk89q.worldedit.entity.Entity;
import com.sk89q.worldedit.extent.AbstractDelegateExtent;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.world.biome.BaseBiome;

import java.util.HashSet;

public class WEExtent extends AbstractDelegateExtent {

    private final HashSet<RegionWrapper> mask;

    public WEExtent(HashSet<RegionWrapper> mask, Extent extent) {
        super(extent);
        this.mask = mask;
    }

    @Override public boolean setBlock(Vector location, BaseBlock block) throws WorldEditException {
        return WEManager.maskContains(this.mask, location.getBlockX(), location.getBlockY(),
            location.getBlockZ()) && super.setBlock(location, block);
    }

    @Override public Entity createEntity(Location location, BaseEntity entity) {
        if (WEManager.maskContains(this.mask, location.getBlockX(), location.getBlockY(),
            location.getBlockZ())) {
            return super.createEntity(location, entity);
        }
        return null;
    }

    @Override public boolean setBiome(Vector2D position, BaseBiome biome) {
        return WEManager.maskContains(this.mask, position.getBlockX(), position.getBlockZ())
            && super.setBiome(position, biome);
    }

    @Override public BaseBlock getBlock(Vector location) {
        if (WEManager.maskContains(this.mask, location.getBlockX(), location.getBlockY(),
            location.getBlockZ())) {
            return super.getBlock(location);
        }
        return WEManager.AIR;
    }
}
