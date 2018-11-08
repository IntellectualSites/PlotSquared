package com.plotsquared.listener;

import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.config.Settings;
import com.intellectualcrafters.plot.object.RegionWrapper;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.entity.BaseEntity;
import com.sk89q.worldedit.entity.Entity;
import com.sk89q.worldedit.extent.AbstractDelegateExtent;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.extent.NullExtent;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.world.biome.BaseBiome;
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
    public BlockState getBlock(BlockVector3 location) {
        if (WEManager.maskContains(this.mask, location.getBlockX(), location.getBlockY(), location.getBlockZ())) {
            return super.getBlock(location);
        }
        return WEManager.AIR;
    }

    @Override
    public boolean setBlock(BlockVector3 location, BlockStateHolder block) throws WorldEditException {
        String id = block.getBlockType().getId();
        switch (id) {
            case "minecraft:chest":
            case "minecraft:ender_chest":
            case "minecraft:potatoes":
            case "minecraft:powered_rail":
            case "minecraft:command_block" :
            case "minecraft:mob_spawner":
            case "minecraft:hopper":
            case "minecraft:jukebox":
            case "minecraft:note_block":
            case "minecraft:skull":
            case "minecraft:beacon":
            case "minecraft:standing_banner":
            case "minecraft:wall_banner":
            case "minecraft:standing_sign":
            case "minecraft:wall_sign":
            case "minecraft:sign":
            case "minecraft:brewing_stand":
            case "minecraft:enchanting_table":
            case "minecraft:detector_rail":
            case "minecraft:rail":
            case "minecraft:activator_rail":
            case "minecraft:furnace":
            case "minecraft:flower_pot ":
            case "minecraft:trapped_chest":
            case "minecraft:powered_comparator":
            case "minecraft:dropper":
            case "minecraft:dispenser":
            case "minecraft:redstone_lamp":
            case "minecraft:sticky_piston":
            case "minecraft:piston":
            case "minecraft:daylight_detector":
                if (this.BSblocked) {
                    return false;
                }
                this.BScount++;
                if (this.BScount > Settings.Chunk_Processor.MAX_TILES) {
                    this.BSblocked = true;
                    PS.debug(C.PREFIX + "&cdetected unsafe WorldEdit: " + location.getBlockX() + "," + location.getBlockZ());
                }
                if (WEManager.maskContains(this.mask, location.getBlockX(), location.getBlockY(), location.getBlockZ())) {
                    if (this.count++ > this.max) {
                        if (this.parent != null) {
                            try {
                                Field field = AbstractDelegateExtent.class.getDeclaredField("extent");
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
                int x = location.getBlockX();
                int y = location.getBlockY();
                int z = location.getBlockZ();
                if (WEManager.maskContains(this.mask, location.getBlockX(), location.getBlockY(), location.getBlockZ())) {
                    if (this.count++ > this.max) {
                        if (this.parent != null) {
                            try {
                                Field field = AbstractDelegateExtent.class.getDeclaredField("extent");
                                field.setAccessible(true);
                                field.set(this.parent, new NullExtent());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            this.parent = null;
                        }
                        return false;
                    }
                    switch (id) {
                        case "minecraft:air":
                        case "minecraft:grass_block":
                        case "minecraft:cobblestone":
                        case "minecraft:gravel":
                        case "minecraft:gold_ore":
                        case "minecraft:iron_ore":
                        case "minecraft:glass":
                        case "minecraft:white_stained_glass":
                        case "minecraft:orange_stained_glass":
                        case "minecraft:red_stained_glass":
                        case "minecraft:light_blue_stained_glass":
                        case "minecraft:yellow_stained_glass":
                        case "minecraft:pink_stained_glass":
                        case "minecraft:blue_stained_glass":
                        case "minecraft:green_stained_glass":
                        case "minecraft:lime_stained_glass":
                        case "minecraft:black_stained_glass":
                        case "minecraft:grey_stained_glass":
                        case "minecraft:brown_stained_glass":
                        case "minecraft:cyan_stained_glass":
                        case "minecraft:purple_stained_glass":
                        case "minecraft:magneta_stained_glass":
                        case "minecraft:lapis_ore":
                        case "minecraft:lapis_block":
                        case "minecraft:sandstone":
                        case "minecraft:cobweb":
                        case "minecraft:dead_bush":
                        case "minecraft:dandelion":
                        case "minecraft:brown_mushroom":
                        case "minecraft:red_mushroom":
                        case "minecraft:gold_block":
                        case "minecraft:iron_block":
                        case "minecraft:bricks":
                        case "minecraft:tnt":
                        case "minecraft:bookshelf":
                        case "minecraft:mossy_cobblestone":
                        case "minecraft:obsidian":
                        case "minecraft:fire":
                        case "minecraft:redstone_wire":
                        case "minecraft:diamond_ore":
                        case "minecraft:diamond_block":
                        case "minecraft:crafting_table":
                        case "minecraft:farmland":
                        case "minecraft:bedrock":
                        case "minecraft:flowing_water":
                        case "minecraft:water":
                        case "minecraft:flowing_lava":
                        case "minecraft:lava":
                        case "minecraft:redstone_ore":
                        case "minecraft:unlit_redstone_torch":
                        case "minecraft:redstone_torch":
                        case "minecraft:ice":
                        case "minecraft:snow_block":
                        case "minecraft:cactus":
                        case "minecraft:clay_ball":
                        case "minecraft:reeds":
                        case "minecraft:oak_fence ":
                        case "minecraft:netherrack":
                        case "minecraft:soul_sand":
                        case "minecraft:iron_bars":
                        case "minecraft:glass_pane":
                        case "minecraft:white_glass_pane":
                        case "minecraft:orange_glass_pane":
                        case "minecraft:red_glass_pane":
                        case "minecraft:light_blue_glass_pane":
                        case "minecraft:yellow_glass_pane":
                        case "minecraft:pink_glass_pane":
                        case "minecraft:blue_glass_pane":
                        case "minecraft:green_glass_pane":
                        case "minecraft:lime_glass_pane":
                        case "minecraft:black_glass_pane":
                        case "minecraft:grey_glass_pane":
                        case "minecraft:brown_glass_pane":
                        case "minecraft:cyan_glass_pane":
                        case "minecraft:purple_glass_pane":
                        case "minecraft:magneta_glass_pane":
                        case "minecraft:melon":
                        case "minecraft:mycelium":
                        case "minecraft:nether_bricks":
                        case "minecraft:nether_brick_fence":
                        case "minecraft:nether_brick_stairs":
                        case "minecraft:end_stone":
                        case "minecraft:dragon_egg":
                        case "minecraft:emerald_ore":
                        case "minecraft:emerald_block":
                        case "minecraft:flower_pot":
                        case "minecraft:slime_block ":
                        case "minecraft:barrier":
                        case "minecraft:sea_lantern":
                        case "minecraft:hay_block":
                        case "minecraft:terracotta":
                        case "minecraft:white_terracotta":
                        case "minecraft:orange_terracotta":
                        case "minecraft:red_terracotta":
                        case "minecraft:light_blue_terracotta":
                        case "minecraft:yellow_terracotta":
                        case "minecraft:pink_terracotta":
                        case "minecraft:blue_terracotta":
                        case "minecraft:green_terracotta":
                        case "minecraft:lime_terracotta":
                        case "minecraft:black_terracotta":
                        case "minecraft:grey_terracotta":
                        case "minecraft:brown_terracotta":
                        case "minecraft:cyan_terracotta":
                        case "minecraft:purple_terracotta":
                        case "minecraft:magneta_terracotta":
                        case "minecraft:coal_block":
                        case "minecraft:packed_ice":
                        case "minecraft:double_stone_slab2 ":
                        case "minecraft:red_sandstone_slab":
                        case "minecraft:spruce_fence":
                        case "minecraft:birch_fence":
                        case "minecraft:oak_fence":
                        case "minecraft:jungle_fence":
                        case "minecraft:dark_oak_fence":
                        case "minecraft:acacia_fence":
                            {
                                super.setBlock(location, block);
                            }
                            break;
                        default:
                            {
                                super.setBlock(location, block);
                            }
                            break;
                    }
                    return true;
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
        if (this.Ecount > Settings.Chunk_Processor.MAX_ENTITIES) {
            this.Eblocked = true;
            PS.debug(C.PREFIX + "&cdetected unsafe WorldEdit: " + location.getBlockX() + "," + location.getBlockZ());
        }
        if (WEManager.maskContains(this.mask, location.getBlockX(), location.getBlockY(), location.getBlockZ())) {
            return super.createEntity(location, entity);
        }
        return null;
    }

    @Override
    public boolean setBiome(BlockVector2 position, BaseBiome biome) {
        return WEManager.maskContains(this.mask, position.getBlockX(), position.getBlockZ()) && super.setBiome(position, biome);
    }
}
