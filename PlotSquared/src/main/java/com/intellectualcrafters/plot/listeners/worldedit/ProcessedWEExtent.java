package com.intellectualcrafters.plot.listeners.worldedit;

import java.util.HashSet;

import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.config.Settings;
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

public class ProcessedWEExtent extends AbstractDelegateExtent {
    private final HashSet<RegionWrapper> mask;
    int BScount = 0;
    int Ecount = 0;
    boolean BSblocked = false;
    boolean Eblocked = false;
 
    public ProcessedWEExtent(HashSet<RegionWrapper> mask, Extent extent) {
        super(extent);
        this.mask = mask;
    }
    
    @Override
    public boolean setBlock(Vector location, BaseBlock block) throws WorldEditException {
        switch (block.getType()) {
            case 54:
            case 130:
            case 142:
            case 27:
            case 137:
            case 52:
            case 154:
            case 84:
            case 25:
            case 144:
            case 138:
            case 176:
            case 177:
            case 63:
            case 68:
            case 323:
            case 117:
            case 116:
            case 28:
            case 66:
            case 157:
            case 61:
            case 62:
            case 140:
            case 146:
            case 149:
            case 150:
            case 158:
            case 23:
            case 123:
            case 124:
            case 29:
            case 33:
            case 151:
            case 178: {
                if (BSblocked) {
                    return false;
                }
                BScount++;
                if (BScount > Settings.CHUNK_PROCESSOR_MAX_BLOCKSTATES) {
                    BSblocked = true;
                    PS.log("&cPlotSquared detected unsafe WorldEdit: " + (location.getBlockX()) + "," + (location.getBlockZ()));
                }
            }
        }
        if (WEManager.maskContains(mask, location.getBlockX(), location.getBlockZ())) {
            return super.setBlock(location, block);
        }
        return false;
    }
    
    @Override
    public Entity createEntity(Location location, BaseEntity entity) {
        if (Eblocked) {
            return null;
        }
        Ecount++;
        if (Ecount > Settings.CHUNK_PROCESSOR_MAX_ENTITIES) {
            Eblocked = true;
            PS.log("&cPlotSquared detected unsafe WorldEdit: " + (location.getBlockX()) + "," + (location.getBlockZ()));
        }
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