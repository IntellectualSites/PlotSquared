package com.github.intellectualsites.plotsquared.plot.listener;

import com.github.intellectualsites.plotsquared.plot.PlotSquared;
import com.github.intellectualsites.plotsquared.plot.config.Captions;
import com.github.intellectualsites.plotsquared.plot.config.Settings;
import com.github.intellectualsites.plotsquared.plot.object.RegionWrapper;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.entity.BaseEntity;
import com.sk89q.worldedit.entity.Entity;
import com.sk89q.worldedit.extent.AbstractDelegateExtent;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.extent.NullExtent;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockStateHolder;

import java.lang.reflect.Field;
import java.util.HashSet;

public class ProcessedWEExtent extends AbstractDelegateExtent {

    private final HashSet<RegionWrapper> mask;
    private final String world;
    private final int max;
    int BScount = 0;
    int Ecount = 0;
    boolean BSblocked = false;
    boolean Eblocked = false;
    private int count;
    private Extent parent;

    public ProcessedWEExtent(String world, HashSet<RegionWrapper> mask, int max, Extent child,
        Extent parent) {
        super(child);
        this.mask = mask;
        this.world = world;
        if (max == -1) {
            max = Integer.MAX_VALUE;
        }
        this.max = max;
        this.count = 0;
        this.parent = parent;
    }

    @Override public BlockState getBlock(BlockVector3 position) {
        if (WEManager.maskContains(this.mask, position.getX(), position.getY(), position.getZ())) {
            return super.getBlock(position);
        }
        return WEExtent.AIRSTATE;
    }

    @Override public BaseBlock getFullBlock(BlockVector3 position) {
        if (WEManager.maskContains(this.mask, position.getX(), position.getY(), position.getZ())) {
            return super.getFullBlock(position);
        }
        return WEExtent.AIRBASE;
    }

    @Override
    public <T extends BlockStateHolder<T>> boolean setBlock(BlockVector3 location, T block)
        throws WorldEditException {
        String id = block.getBlockType().getId();
        switch (id) {
            case "54":
            case "130":
            case "142":
            case "27":
            case "137":
            case "52":
            case "154":
            case "84":
            case "25":
            case "144":
            case "138":
            case "176":
            case "177":
            case "63":
            case "68":
            case "323":
            case "117":
            case "116":
            case "28":
            case "66":
            case "157":
            case "61":
            case "62":
            case "140":
            case "146":
            case "149":
            case "150":
            case "158":
            case "23":
            case "123":
            case "124":
            case "29":
            case "33":
            case "151":
            case "178":
                if (this.BSblocked) {
                    return false;
                }
                this.BScount++;
                if (this.BScount > Settings.Chunk_Processor.MAX_TILES) {
                    this.BSblocked = true;
                    PlotSquared.debug(
                        Captions.PREFIX + "&cdetected unsafe WorldEdit: " + location.getX() + ","
                            + location.getZ());
                }
                if (WEManager
                    .maskContains(this.mask, location.getX(), location.getY(), location.getZ())) {
                    if (this.count++ > this.max) {
                        if (this.parent != null) {
                            try {
                                Field field =
                                    AbstractDelegateExtent.class.getDeclaredField("extent");
                                field.setAccessible(true);
                                field.set(this.parent, new NullExtent());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            this.parent = null;
                        }
                        return false;
                    }
                    return super.setBlock(location, block);
                }
                break;
            default:
                if (WEManager
                    .maskContains(this.mask, location.getX(), location.getY(), location.getZ())) {
                    if (this.count++ > this.max) {
                        if (this.parent != null) {
                            try {
                                Field field =
                                    AbstractDelegateExtent.class.getDeclaredField("extent");
                                field.setAccessible(true);
                                field.set(this.parent, new NullExtent());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            this.parent = null;
                        }
                        return false;
                    }
                    super.setBlock(location, block);
                }
                return true;

        }
        return false;
    }

    @Override public Entity createEntity(Location location, BaseEntity entity) {
        if (this.Eblocked) {
            return null;
        }
        this.Ecount++;
        if (this.Ecount > Settings.Chunk_Processor.MAX_ENTITIES) {
            this.Eblocked = true;
            PlotSquared.debug(
                Captions.PREFIX + "&cdetected unsafe WorldEdit: " + location.getBlockX() + ","
                    + location.getBlockZ());
        }
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
}
