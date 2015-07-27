package com.plotsquared.bukkit.listeners.worldedit;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.Vector2D;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.entity.BaseEntity;
import com.sk89q.worldedit.entity.Entity;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.world.biome.BaseBiome;

import java.util.ArrayList;
import java.util.List;

public class NullExtent implements Extent {

    @Override
    public BaseBiome getBiome(Vector2D arg0) {
        return null;
    }

    @Override
    public BaseBlock getBlock(Vector arg0) {
        return null;
    }

    @Override
    public BaseBlock getLazyBlock(Vector arg0) {
        return null;
    }

    @Override
    public Operation commit() {
        return null;
    }

    @Override
    public boolean setBiome(Vector2D arg0, BaseBiome arg1) {
        return false;
    }

    @Override
    public boolean setBlock(Vector arg0, BaseBlock arg1) throws WorldEditException {
        return false;
    }

    @Override
    public Entity createEntity(Location arg0, BaseEntity arg1) {
        return null;
    }

    @Override
    public List<? extends Entity> getEntities() {
        return new ArrayList<>();
    }

    @Override
    public List<? extends Entity> getEntities(Region arg0) {
        return new ArrayList<>();
    }

    @Override
    public Vector getMaximumPoint() {
        return new Vector(0, 0, 0);
    }

    @Override
    public Vector getMinimumPoint() {
        return new Vector(0, 0, 0);
    }
    
}
