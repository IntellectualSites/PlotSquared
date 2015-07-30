package com.plotsquared.bukkit.listeners.worldedit;

import java.util.HashSet;

import com.intellectualcrafters.plot.object.RegionWrapper;
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

public class WEExtent extends AbstractDelegateExtent {
    private final HashSet<RegionWrapper> mask;
 
    public WEExtent(HashSet<RegionWrapper> mask, Extent extent) {
        super(extent);
        this.mask = mask;
    }
    
    @Override
    public boolean setBlock(Vector location, BaseBlock block) throws WorldEditException {
        if (WEManager.maskContains(mask, location.getBlockX(), location.getBlockZ())) {
            return super.setBlock(location, block);
        }
        return false;
    }
    
    @Override
    public Entity createEntity(Location location, BaseEntity entity) {
        if (WEManager.maskContains(mask, location.getBlockX(), location.getBlockZ())) {
            return super.createEntity(location, entity);
        }
        return null;
    }
    
    @Override
    public boolean setBiome(Vector2D position, BaseBiome biome) {
        if (WEManager.maskContains(mask, position.getBlockX(), position.getBlockZ())) {
            return super.setBiome(position, biome);
        }
        return false;
    }
}