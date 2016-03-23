package com.plotsquared.listener;

import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.config.Settings;
import com.intellectualcrafters.plot.object.PlotBlock;
import com.intellectualcrafters.plot.object.RegionWrapper;
import com.intellectualcrafters.plot.util.SetQueue;
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

    public ProcessedWEExtent(String world, HashSet<RegionWrapper> mask, int max, Extent child, Extent parent) {
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

    @Override
    public boolean setBlock(Vector location, BaseBlock block) throws WorldEditException {
        int id = block.getType();
        switch (id) {
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
                if (this.BSblocked) {
                    return false;
                }
                this.BScount++;
                if (this.BScount > Settings.CHUNK_PROCESSOR_MAX_BLOCKSTATES) {
                    this.BSblocked = true;
                    PS.debug("&cPlotSquared detected unsafe WorldEdit: " + location.getBlockX() + "," + location.getBlockZ());
                }
                if (WEManager.maskContains(this.mask, location.getBlockX(), location.getBlockY(), location.getBlockZ())) {
                    if (this.count++ > this.max) {
                        if (this.parent != null) {
                            try {
                                Field field = AbstractDelegateExtent.class.getDeclaredField("extent");
                                field.setAccessible(true);
                                field.set(this.parent, new com.sk89q.worldedit.extent.NullExtent());
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
            }
            default: {
                int x = location.getBlockX();
                int y = location.getBlockY();
                int z = location.getBlockZ();
                if (WEManager.maskContains(this.mask, location.getBlockX(), location.getBlockY(), location.getBlockZ())) {
                    if (this.count++ > this.max) {
                        if (this.parent != null) {
                            try {
                                Field field = AbstractDelegateExtent.class.getDeclaredField("extent");
                                field.setAccessible(true);
                                field.set(this.parent, new com.sk89q.worldedit.extent.NullExtent());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            this.parent = null;
                        }
                        return false;
                    }
                    switch (id) {
                        case 0:
                        case 2:
                        case 4:
                        case 13:
                        case 14:
                        case 15:
                        case 20:
                        case 21:
                        case 22:
                        case 24:
                        case 25:
                        case 30:
                        case 32:
                        case 37:
                        case 39:
                        case 40:
                        case 41:
                        case 42:
                        case 45:
                        case 46:
                        case 47:
                        case 48:
                        case 49:
                        case 51:
                        case 52:
                        case 54:
                        case 55:
                        case 56:
                        case 57:
                        case 58:
                        case 60:
                        case 61:
                        case 62:
                        case 7:
                        case 8:
                        case 9:
                        case 10:
                        case 11:
                        case 73:
                        case 74:
                        case 78:
                        case 79:
                        case 80:
                        case 81:
                        case 82:
                        case 83:
                        case 84:
                        case 85:
                        case 87:
                        case 88:
                        case 101:
                        case 102:
                        case 103:
                        case 110:
                        case 112:
                        case 113:
                        case 117:
                        case 121:
                        case 122:
                        case 123:
                        case 124:
                        case 129:
                        case 133:
                        case 138:
                        case 137:
                        case 140:
                        case 165:
                        case 166:
                        case 169:
                        case 170:
                        case 172:
                        case 173:
                        case 174:
                        case 176:
                        case 177:
                        case 181:
                        case 182:
                        case 188:
                        case 189:
                        case 190:
                        case 191:
                        case 192: {
                            if (Settings.EXPERIMENTAL_FAST_ASYNC_WORLDEDIT) {
                                SetQueue.IMP.setBlock(this.world, x, y, z, id);
                            } else {
                                super.setBlock(location, block);
                            }
                            break;
                        }
                        default: {
                            if (Settings.EXPERIMENTAL_FAST_ASYNC_WORLDEDIT) {
                                SetQueue.IMP.setBlock(this.world, x, y, z, new PlotBlock((short) id, (byte) block.getData()));
                            } else {
                                super.setBlock(location, block);
                            }
                            break;
                        }
                    }
                    return true;
                }
            }

        }
        return false;
    }

    @Override
    public Entity createEntity(Location location, BaseEntity entity) {
        if (this.Eblocked) {
            return null;
        }
        this.Ecount++;
        if (this.Ecount > Settings.CHUNK_PROCESSOR_MAX_ENTITIES) {
            this.Eblocked = true;
            PS.debug("&cPlotSquared detected unsafe WorldEdit: " + location.getBlockX() + "," + location.getBlockZ());
        }
        if (WEManager.maskContains(this.mask, location.getBlockX(), location.getBlockY(), location.getBlockZ())) {
            return super.createEntity(location, entity);
        }
        return null;
    }

    @Override
    public boolean setBiome(Vector2D position, BaseBiome biome) {
        return WEManager.maskContains(this.mask, position.getBlockX(), position.getBlockZ()) && super.setBiome(position, biome);
    }
}
