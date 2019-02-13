package com.github.intellectualsites.plotsquared.bukkit.util;

import com.github.intellectualsites.plotsquared.plot.object.LegacyPlotBlock;
import com.github.intellectualsites.plotsquared.plot.object.PlotBlock;
import com.github.intellectualsites.plotsquared.plot.object.StringPlotBlock;
import com.github.intellectualsites.plotsquared.plot.util.LegacyMappings;
import com.github.intellectualsites.plotsquared.plot.util.StringComparison;
import lombok.*;
import org.bukkit.Material;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Borrowed from https://github.com/Phoenix616/IDConverter/blob/master/mappings/src/main/java/de/themoep/idconverter/IdMappings.java
 * Original License:
 * <p>
 * Minecraft ID mappings
 * Copyright (C) 2017  Max Lee (https://github.com/Phoenix616)
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
public final class BukkitLegacyMappings extends LegacyMappings {

    private static final LegacyBlock[] BLOCKS =
        new LegacyBlock[] {new LegacyBlock(0, "air"), new LegacyBlock(1, "stone"),
            new LegacyBlock(1, 1, "stone", "granite"),
            new LegacyBlock(1, 2, "stone", "polished_granite"),
            new LegacyBlock(1, 3, "stone", "diorite"),
            new LegacyBlock(1, 4, "stone", "polished_diorite"),
            new LegacyBlock(1, 5, "stone", "andesite"),
            new LegacyBlock(1, 6, "stone", "polished_andesite"),
            new LegacyBlock(2, "grass", "grass_block"), new LegacyBlock(3, "dirt"),
            new LegacyBlock(3, 1, "dirt", "coarse_dirt"), new LegacyBlock(3, 2, "dirt", "podzol"),
            new LegacyBlock(4, "cobblestone"), new LegacyBlock(5, "wood", "oak_planks"),
            new LegacyBlock(5, 1, "wood", "spruce_planks"),
            new LegacyBlock(5, 2, "wood", "birch_planks"),
            new LegacyBlock(5, 3, "wood", "jungle_planks"),
            new LegacyBlock(5, 4, "wood", "acacia_planks"),
            new LegacyBlock(5, 5, "wood", "dark_oak_planks"),
            new LegacyBlock(6, "sapling", "oak_sapling"),
            new LegacyBlock(6, 1, "sapling", "spruce_sapling"),
            new LegacyBlock(6, 2, "sapling", "birch_sapling"),
            new LegacyBlock(6, 3, "sapling", "jungle_sapling"),
            new LegacyBlock(6, 4, "sapling", "acacia_sapling"),
            new LegacyBlock(6, 5, "sapling", "dark_oak_sapling"), new LegacyBlock(7, "bedrock"),
            new LegacyBlock(8, "water", "flowing_water"),
            new LegacyBlock(9, "stationary_water", "water"),
            new LegacyBlock(10, "lava", "flowing_lava"),
            new LegacyBlock(11, "stationary_lava", "lava"), new LegacyBlock(12, "sand"),
            new LegacyBlock(12, 1, "sand", "red_sand"), new LegacyBlock(13, "gravel"),
            new LegacyBlock(14, "gold_ore"), new LegacyBlock(15, "iron_ore"),
            new LegacyBlock(16, "coal_ore"), new LegacyBlock(17, "log", "oak_log"),
            new LegacyBlock(17, 1, "log", "oak_log"), new LegacyBlock(17, 2, "log", "spruce_log"),
            new LegacyBlock(17, 3, "log", "birch_log"), new LegacyBlock(17, 4, "log", "jungle_log"),
            new LegacyBlock(17, 5, "log", "oak_bark"), new LegacyBlock(17, 6, "log", "spruce_bark"),
            new LegacyBlock(17, 7, "log", "birch_bark"),
            new LegacyBlock(17, 8, "log", "jungle_bark"),
            new LegacyBlock(18, "leaves", "oak_leaves"),
            new LegacyBlock(18, 1, "leaves", "spruce_leaves"),
            new LegacyBlock(18, 2, "leaves", "birch_leaves"),
            new LegacyBlock(18, 3, "leaves", "jungle_leaves"), new LegacyBlock(19, "sponge"),
            new LegacyBlock(19, 1, "sponge", "wet_sponge"), new LegacyBlock(20, "glass"),
            new LegacyBlock(21, "lapis_ore"), new LegacyBlock(22, "lapis_block"),
            new LegacyBlock(23, "dispenser"), new LegacyBlock(24, "sandstone"),
            new LegacyBlock(24, 1, "sandstone", "chisled_sandstone"),
            new LegacyBlock(24, 2, "sandstone", "cut_sandstone"), new LegacyBlock(25, "note_block"),
            new LegacyBlock(26, "bed_block"), new LegacyBlock(27, "powered_rail"),
            new LegacyBlock(28, "detector_rail"),
            new LegacyBlock(29, "piston_sticky_base", "sticky_piston"),
            new LegacyBlock(30, "web", "cobweb"), new LegacyBlock(31, "long_grass", "dead_bush"),
            new LegacyBlock(31, 1, "long_grass", "grass"),
            new LegacyBlock(31, 2, "long_grass", "fern"), new LegacyBlock(32, "dead_bush"),
            new LegacyBlock(33, "piston_base", "piston"),
            new LegacyBlock(34, "piston_extension", "piston_head"),
            new LegacyBlock(35, "wool", "white_wool"),
            new LegacyBlock(35, 1, "wool", "orange_wool"),
            new LegacyBlock(35, 2, "wool", "magenta_wool"),
            new LegacyBlock(35, 3, "wool", "light_blue_wool"),
            new LegacyBlock(35, 4, "wool", "yellow_wool"),
            new LegacyBlock(35, 5, "wool", "lime_wool"),
            new LegacyBlock(35, 6, "wool", "pink_wool"),
            new LegacyBlock(35, 7, "wool", "gray_wool"),
            new LegacyBlock(35, 8, "wool", "light_gray_wool"),
            new LegacyBlock(35, 9, "wool", "cyan_wool"),
            new LegacyBlock(35, 10, "wool", "purple_wool"),
            new LegacyBlock(35, 11, "wool", "blue_wool"),
            new LegacyBlock(35, 12, "wool", "brown_wool"),
            new LegacyBlock(35, 13, "wool", "green_wool"),
            new LegacyBlock(35, 14, "wool", "red_wool"),
            new LegacyBlock(35, 15, "wool", "black_wool"),
            new LegacyBlock(36, "piston_moving_piece", "moving_piston"),
            new LegacyBlock(37, "yellow_flower", "dandelion"),
            new LegacyBlock(38, "red_rose", "poppy"),
            new LegacyBlock(38, 1, "red_rose", "blue_orchid"),
            new LegacyBlock(38, 2, "red_rose", "allium"),
            new LegacyBlock(38, 3, "red_rose", "azure_bluet"),
            new LegacyBlock(38, 4, "red_rose", "red_tulip"),
            new LegacyBlock(38, 5, "red_rose", "orange_tulip"),
            new LegacyBlock(38, 6, "red_rose", "white_tulip"),
            new LegacyBlock(38, 7, "red_rose", "pink_tulip"),
            new LegacyBlock(38, 8, "red_rose", "oxeye_daisy"),
            new LegacyBlock(39, "brown_mushroom"), new LegacyBlock(40, "red_mushroom"),
            new LegacyBlock(41, "gold_block"), new LegacyBlock(42, "iron_block"),
            new LegacyBlock(43, "double_step"),
            new LegacyBlock(43, 6, "double_step", "smooth_quartz"),
            new LegacyBlock(43, 8, "double_step", "smooth_stone"),
            new LegacyBlock(43, 9, "double_step", "smooth_sandstone"),
            new LegacyBlock(44, "step", "stone_slab"),
            new LegacyBlock(44, 1, "step", "sandstone_slab"),
            new LegacyBlock(44, 2, "step", "petrified_oak_slab"),
            new LegacyBlock(44, 3, "step", "cobblestone_slab"),
            new LegacyBlock(44, 4, "step", "brick_slab"),
            new LegacyBlock(44, 5, "step", "stone_brick_slab"),
            new LegacyBlock(44, 6, "step", "nether_brick_slab"),
            new LegacyBlock(44, 7, "step", "quartz_slab"), new LegacyBlock(45, "brick", "bricks"),
            new LegacyBlock(46, "tnt"), new LegacyBlock(47, "bookshelf"),
            new LegacyBlock(48, "mossy_cobblestone"), new LegacyBlock(49, "obsidian"),
            new LegacyBlock(50, "torch"), new LegacyBlock(50, 1, "torch", "wall_torch"),
            new LegacyBlock(50, 2, "torch", "wall_torch"),
            new LegacyBlock(50, 3, "torch", "wall_torch"),
            new LegacyBlock(50, 4, "torch", "wall_torch"), new LegacyBlock(50, 5, "torch"),
            new LegacyBlock(51, "fire"), new LegacyBlock(52, "mob_spawner"),
            new LegacyBlock(53, "wood_stairs", "oak_stairs"), new LegacyBlock(54, "chest", "chest"),
            new LegacyBlock(55, "redstone_wire"), new LegacyBlock(56, "diamond_ore"),
            new LegacyBlock(57, "diamond_block"),
            new LegacyBlock(58, "workbench", "crafting_table"),
            new LegacyBlock(59, "crops", "wheat"), new LegacyBlock(60, "soil", "farmland"),
            new LegacyBlock(61, "furnace"), new LegacyBlock(62, "burning_furnace"),
            new LegacyBlock(63, "sign_post", "sign"),
            new LegacyBlock(64, "wooden_door", "oak_door"), new LegacyBlock(65, "ladder"),
            new LegacyBlock(66, "rails", "rail"), new LegacyBlock(67, "cobblestone_stairs"),
            new LegacyBlock(68, "wall_sign"), new LegacyBlock(69, "lever"),
            new LegacyBlock(70, "stone_plate", "stone_pressure_plate"),
            new LegacyBlock(71, "iron_door_block", "iron_door"),
            new LegacyBlock(72, "wood_plate", "oak_pressure_plate"),
            new LegacyBlock(73, "redstone_ore"), new LegacyBlock(74, "glowing_redstone_ore"),
            new LegacyBlock(75, "redstone_torch_off"),
            new LegacyBlock(76, "redstone_torch_on", "redstone_torch"),
            new LegacyBlock(76, 1, "redstone_torch_on", "redstone_wall_torch"),
            new LegacyBlock(76, 2, "redstone_torch_on", "redstone_wall_torch"),
            new LegacyBlock(76, 3, "redstone_torch_on", "redstone_wall_torch"),
            new LegacyBlock(76, 4, "redstone_torch_on", "redstone_wall_torch"),
            new LegacyBlock(76, 5, "redstone_torch_on", "redstone_torch"),
            new LegacyBlock(77, "stone_button"), new LegacyBlock(78, "snow"),
            new LegacyBlock(79, "ice"), new LegacyBlock(80, "snow_block"),
            new LegacyBlock(81, "cactus"), new LegacyBlock(82, "clay"),
            new LegacyBlock(83, "sugar_cane_block", "sugar_cane"), new LegacyBlock(84, "jukebox"),
            new LegacyBlock(85, "fence", "oak_fence"),
            new LegacyBlock(86, "pumpkin", "carved_pumpkin"), new LegacyBlock(87, "netherrack"),
            new LegacyBlock(88, "soul_sand"), new LegacyBlock(89, "glowstone"),
            new LegacyBlock(90, "portal"), new LegacyBlock(91, "jack_o_lantern"),
            new LegacyBlock(92, "cake_block", "cake"), new LegacyBlock(93, "diode_block_off"),
            new LegacyBlock(94, "diode_block_on", "repeater"),
            new LegacyBlock(95, "stained_glass", "white_stained_glass"),
            new LegacyBlock(95, 1, "stained_glass", "orange_stained_glass"),
            new LegacyBlock(95, 2, "stained_glass", "magenta_stained_glass"),
            new LegacyBlock(95, 3, "stained_glass", "light_blue_stained_glass"),
            new LegacyBlock(95, 4, "stained_glass", "yellow_stained_glass"),
            new LegacyBlock(95, 5, "stained_glass", "lime_stained_glass"),
            new LegacyBlock(95, 6, "stained_glass", "pink_stained_glass"),
            new LegacyBlock(95, 7, "stained_glass", "gray_stained_glass"),
            new LegacyBlock(95, 8, "stained_glass", "light_gray_stained_glass"),
            new LegacyBlock(95, 9, "stained_glass", "cyan_stained_glass"),
            new LegacyBlock(95, 10, "stained_glass", "purple_stained_glass"),
            new LegacyBlock(95, 11, "stained_glass", "blue_stained_glass"),
            new LegacyBlock(95, 12, "stained_glass", "brown_stained_glass"),
            new LegacyBlock(95, 13, "stained_glass", "green_stained_glass"),
            new LegacyBlock(95, 14, "stained_glass", "red_stained_glass"),
            new LegacyBlock(95, 15, "stained_glass", "black_stained_glass"),
            new LegacyBlock(96, "trap_door", "oak_trapdoor"),
            new LegacyBlock(97, "monster_eggs", "infested_stone"),
            new LegacyBlock(97, 1, "monster_eggs", "infested_coblestone"),
            new LegacyBlock(97, 2, "monster_eggs", "infested_stone_bricks"),
            new LegacyBlock(97, 3, "monster_eggs", "infested_mossy_stone_bricks"),
            new LegacyBlock(97, 4, "monster_eggs", "infested_crcked_stone_bricks"),
            new LegacyBlock(97, 5, "monster_eggs", "infested_chiseled_stone_bricks"),
            new LegacyBlock(98, "smooth_brick", "stone_bricks"),
            new LegacyBlock(98, 1, "smooth_brick", "mossy_stone_bricks"),
            new LegacyBlock(98, 2, "smooth_brick", "cracked_stone_bricks"),
            new LegacyBlock(98, 3, "smooth_brick", "chiseled_bricks"),
            new LegacyBlock(99, "huge_mushroom_1", "brown_mushroom_block"),
            new LegacyBlock(99, 1, "huge_mushroom_1"), new LegacyBlock(99, 2, "huge_mushroom_1"),
            new LegacyBlock(99, 3, "huge_mushroom_1"), new LegacyBlock(99, 4, "huge_mushroom_1"),
            new LegacyBlock(99, 5, "huge_mushroom_1"), new LegacyBlock(99, 6, "huge_mushroom_1"),
            new LegacyBlock(99, 7, "huge_mushroom_1"), new LegacyBlock(99, 8, "huge_mushroom_1"),
            new LegacyBlock(99, 9, "huge_mushroom_1"),
            new LegacyBlock(99, 10, "huge_mushroom_1", "mushroom_stem"),
            new LegacyBlock(99, 14, "huge_mushroom_1"), new LegacyBlock(99, 15, "huge_mushroom_1"),
            new LegacyBlock(100, "huge_mushroom_2", "red_mushroom_block"),
            new LegacyBlock(100, 1, "huge_mushroom_2"), new LegacyBlock(100, 2, "huge_mushroom_2"),
            new LegacyBlock(100, 3, "huge_mushroom_2"), new LegacyBlock(100, 4, "huge_mushroom_2"),
            new LegacyBlock(100, 5, "huge_mushroom_2"), new LegacyBlock(100, 6, "huge_mushroom_2"),
            new LegacyBlock(100, 7, "huge_mushroom_2"), new LegacyBlock(100, 8, "huge_mushroom_2"),
            new LegacyBlock(100, 9, "huge_mushroom_2"),
            new LegacyBlock(100, 10, "huge_mushroom_2", "mushroom_stem"),
            new LegacyBlock(100, 14, "huge_mushroom_2"),
            new LegacyBlock(100, 15, "huge_mushroom_2"),
            new LegacyBlock(101, "iron_fence", "ironbars"),
            new LegacyBlock(102, "thin_glass", "glass_pane"), new LegacyBlock(103, "melon_block"),
            new LegacyBlock(104, "pumpkin_stem"), new LegacyBlock(105, "melon_stem"),
            new LegacyBlock(106, "vine"), new LegacyBlock(107, "fence_gate", "oak_fence_gate"),
            new LegacyBlock(108, "brick_stairs"),
            new LegacyBlock(109, "smooth_stairs", "stone_brick_stairs"),
            new LegacyBlock(110, "mycel", "mycelium"),
            new LegacyBlock(111, "water_lily", "lily_pad"),
            new LegacyBlock(112, "nether_brick", "nether_bricks"),
            new LegacyBlock(113, "nether_fence", "nether_brick_fence"),
            new LegacyBlock(114, "nether_brick_stairs"),
            new LegacyBlock(115, "nether_warts", "nether_wart"),
            new LegacyBlock(116, "enchantment_table", "enchanting_table"),
            new LegacyBlock(117, "brewing_stand"), new LegacyBlock(118, "cauldron"),
            new LegacyBlock(119, "ender_portal", "end_portal"),
            new LegacyBlock(120, "ender_portal_frame", "end_portal_frame"),
            new LegacyBlock(121, "ender_stone", "end_stone"), new LegacyBlock(122, "dragon_egg"),
            new LegacyBlock(123, "redstone_lamp_off"),
            new LegacyBlock(124, "redstone_lamp_on", "redstone_lamp"),
            new LegacyBlock(125, "wood_double_step"), new LegacyBlock(125, 1, "wood_double_step"),
            new LegacyBlock(125, 2, "wood_double_step"),
            new LegacyBlock(125, 3, "wood_double_step"),
            new LegacyBlock(125, 4, "wood_double_step"),
            new LegacyBlock(125, 5, "wood_double_step"),
            new LegacyBlock(126, "wood_step", "oak_slab"),
            new LegacyBlock(126, 1, "wood_step", "spruce_slab"),
            new LegacyBlock(126, 2, "wood_step", "birch_slab"),
            new LegacyBlock(126, 3, "wood_step", "jungle_slab"),
            new LegacyBlock(126, 4, "wood_step", "acacia_slab"),
            new LegacyBlock(126, 5, "wood_step", "dark_oak_slab"), new LegacyBlock(127, "cocoa"),
            new LegacyBlock(128, "sandstone_stairs"), new LegacyBlock(129, "emerald_ore"),
            new LegacyBlock(130, "ender_chest"), new LegacyBlock(131, "tripwire_hook"),
            new LegacyBlock(132, "tripwire"), new LegacyBlock(133, "emerald_block"),
            new LegacyBlock(134, "spruce_wood_stairs", "spruce_stairs"),
            new LegacyBlock(135, "birch_wood_stairs", "birch_stairs"),
            new LegacyBlock(136, "jungle_wood_stairs", "jungle_stairs"),
            new LegacyBlock(137, "command", "command_block"), new LegacyBlock(138, "beacon"),
            new LegacyBlock(139, "cobble_wall", "cobblestone_wall"),
            new LegacyBlock(139, 1, "cobble_wall", "mossy_cobblestone_wall"),
            new LegacyBlock(140, "flower_pot"), new LegacyBlock(141, "carrot", "carrots"),
            new LegacyBlock(142, "potato", "potatoes"),
            new LegacyBlock(143, "wood_button", "oak_button"),
            new LegacyBlock(144, "skull", "skeleton_skull"),
            new LegacyBlock(144, 1, "skull", "skeleton_wall_skull"),
            new LegacyBlock(144, 2, "skull", "skeleton_wall_skull"),
            new LegacyBlock(144, 3, "skull", "skeleton_wall_skull"),
            new LegacyBlock(144, 4, "skull", "skeleton_wall_skull"),
            new LegacyBlock(144, 5, "skull", "skeleton_wall_skull"), new LegacyBlock(145, "anvil"),
            new LegacyBlock(145, 1, "anvil", "chipped_anvil"),
            new LegacyBlock(145, 2, "anvil", "damaged_anvil"),
            new LegacyBlock(146, "trapped_chest"),
            new LegacyBlock(147, "gold_plate", "light_weighted_pressure_plate"),
            new LegacyBlock(148, "iron_plate", "heavy_weighted_pressure_plate"),
            new LegacyBlock(149, "redstone_comparator_off"),
            new LegacyBlock(150, "redstone_comparator_on", "comparator"),
            new LegacyBlock(151, "daylight_detector"), new LegacyBlock(152, "redstone_block"),
            new LegacyBlock(153, "quartz_ore", "nether_quartz_ore"), new LegacyBlock(154, "hopper"),
            new LegacyBlock(155, "quartz_block"), new LegacyBlock(156, "quartz_stairs"),
            new LegacyBlock(157, "activator_rail"), new LegacyBlock(158, "dropper"),
            new LegacyBlock(159, "stained_clay", "white_terracotta"),
            new LegacyBlock(159, 1, "stained_clay", "orange_terracotta"),
            new LegacyBlock(159, 2, "stained_clay", "magenta_terracotta"),
            new LegacyBlock(159, 3, "stained_clay", "light_blue_terracotta"),
            new LegacyBlock(159, 4, "stained_clay", "yellow_terracotta"),
            new LegacyBlock(159, 5, "stained_clay", "lime_terracotta"),
            new LegacyBlock(159, 6, "stained_clay", "pink_terracotta"),
            new LegacyBlock(159, 7, "stained_clay", "gray_terracotta"),
            new LegacyBlock(159, 8, "stained_clay", "light_gray_terracotta"),
            new LegacyBlock(159, 9, "stained_clay", "cyan_terracotta"),
            new LegacyBlock(159, 10, "stained_clay", "purple_terracotta"),
            new LegacyBlock(159, 11, "stained_clay", "blue_terracotta"),
            new LegacyBlock(159, 12, "stained_clay", "brown_terracotta"),
            new LegacyBlock(159, 13, "stained_clay", "green_terracotta"),
            new LegacyBlock(159, 14, "stained_clay", "red_terracotta"),
            new LegacyBlock(159, 15, "stained_clay", "black_terracotta"),
            new LegacyBlock(160, "stained_glass_pane", "white_stained_glass_pane"),
            new LegacyBlock(160, 1, "stained_glass_pane", "orange_stained_glass_pane"),
            new LegacyBlock(160, 2, "stained_glass_pane", "magenta_stained_glass_pane"),
            new LegacyBlock(160, 3, "stained_glass_pane", "light_blue_stained_glass_pane"),
            new LegacyBlock(160, 4, "stained_glass_pane", "yellow_stained_glass_pane"),
            new LegacyBlock(160, 5, "stained_glass_pane", "lime_stained_glass_pane"),
            new LegacyBlock(160, 6, "stained_glass_pane", "pink_stained_glass_pane"),
            new LegacyBlock(160, 7, "stained_glass_pane", "gray_stained_glass_pane"),
            new LegacyBlock(160, 8, "stained_glass_pane", "light_gray_stained_glass_pane"),
            new LegacyBlock(160, 9, "stained_glass_pane", "cyan_stained_glass_pane"),
            new LegacyBlock(160, 10, "stained_glass_pane", "purple_stained_glass_pane"),
            new LegacyBlock(160, 11, "stained_glass_pane", "blue_stained_glass_pane"),
            new LegacyBlock(160, 12, "stained_glass_pane", "brown_stained_glass_pane"),
            new LegacyBlock(160, 13, "stained_glass_pane", "green_stained_glass_pane"),
            new LegacyBlock(160, 14, "stained_glass_pane", "red_stained_glass_pane"),
            new LegacyBlock(160, 15, "stained_glass_pane", "black_stained_glass_pane"),
            new LegacyBlock(161, "leaves_2", "acacia_leaves"),
            new LegacyBlock(161, 1, "leaves_2", "dark_oak_leaves"),
            new LegacyBlock(162, "log_2", "acacia_log"),
            new LegacyBlock(162, 1, "log_2", "spruce_log"),
            new LegacyBlock(162, 2, "log_2", "birch_log"),
            new LegacyBlock(162, 3, "log_2", "jungle_log"),
            new LegacyBlock(163, "acacia_stairs", "acacia_stairs"),
            new LegacyBlock(164, "dark_oak_stairs", "dark_oak_stairs"),
            new LegacyBlock(165, "slime_block", "slime_block"),
            new LegacyBlock(166, "barrier", "barrier"),
            new LegacyBlock(167, "iron_trapdoor", "iron_trapdoor"),
            new LegacyBlock(168, "prismarine"),
            new LegacyBlock(168, 1, "prismarine", "prismarine_bricks"),
            new LegacyBlock(168, 2, "prismarine", "dark_prismarine"),
            new LegacyBlock(169, "sea_lantern"), new LegacyBlock(170, "hay_block"),
            new LegacyBlock(171, "carpet", "white_carpet"),
            new LegacyBlock(171, 1, "carpet", "orange_carpet"),
            new LegacyBlock(171, 2, "carpet", "magenta_carpet"),
            new LegacyBlock(171, 3, "carpet", "light_blue_carpet"),
            new LegacyBlock(171, 4, "carpet", "yellow_carpet"),
            new LegacyBlock(171, 5, "carpet", "lime_carpet"),
            new LegacyBlock(171, 6, "carpet", "pink_carpet"),
            new LegacyBlock(171, 7, "carpet", "gray_carpet"),
            new LegacyBlock(171, 8, "carpet", "light_gray_carpet"),
            new LegacyBlock(171, 9, "carpet", "cyan_carpet"),
            new LegacyBlock(171, 10, "carpet", "purple_carpet"),
            new LegacyBlock(171, 11, "carpet", "blue_carpet"),
            new LegacyBlock(171, 12, "carpet", "brown_carpet"),
            new LegacyBlock(171, 13, "carpet", "green_carpet"),
            new LegacyBlock(171, 14, "carpet", "red_carpet"),
            new LegacyBlock(171, 15, "carpet", "black_carpet"),
            new LegacyBlock(172, "hard_clay", "terracotta"), new LegacyBlock(173, "coal_block"),
            new LegacyBlock(174, "packed_ice"), new LegacyBlock(175, "double_plant", "sunflower"),
            new LegacyBlock(175, 1, "double_plant", "lilac"),
            new LegacyBlock(175, 2, "double_plant", "tall_grass"),
            new LegacyBlock(175, 3, "double_plant", "large_fern"),
            new LegacyBlock(175, 4, "double_plant", "rose_bush"),
            new LegacyBlock(175, 5, "double_plant", "peony"),
            new LegacyBlock(176, "standing_banner"), new LegacyBlock(177, "wall_banner"),
            new LegacyBlock(178, "daylight_detector_inverted"),
            new LegacyBlock(179, "red_sandstone", "red_sandstone"),
            new LegacyBlock(179, 1, "red_sandstone", "chiseled_red_sandstone"),
            new LegacyBlock(179, 2, "red_sandstone", "cut_red_sandstone"),
            new LegacyBlock(180, "red_sandstone_stairs"),
            new LegacyBlock(181, "double_stone_slab2"),
            new LegacyBlock(181, 8, "double_stone_slab2", "smooth_red_sandstone"),
            new LegacyBlock(182, "stone_slab2", "red_sandstone_slab"),
            new LegacyBlock(183, "spruce_fence_gate"), new LegacyBlock(184, "birch_fence_gate"),
            new LegacyBlock(185, "jungle_fence_gate"), new LegacyBlock(186, "dark_oak_fence_gate"),
            new LegacyBlock(187, "acacia_fence_gate"), new LegacyBlock(188, "spruce_fence"),
            new LegacyBlock(189, "birch_fence"), new LegacyBlock(190, "jungle_fence"),
            new LegacyBlock(191, "dark_oak_fence"), new LegacyBlock(192, "acacia_fence"),
            new LegacyBlock(193, "spruce_door"), new LegacyBlock(194, "birch_door"),
            new LegacyBlock(195, "jungle_door"), new LegacyBlock(196, "acacia_door"),
            new LegacyBlock(197, "dark_oak_door"), new LegacyBlock(198, "end_rod"),
            new LegacyBlock(199, "chorus_plant"), new LegacyBlock(200, "chorus_flower"),
            new LegacyBlock(201, "purpur_block"), new LegacyBlock(202, "purpur_pillar"),
            new LegacyBlock(203, "purpur_stairs"), new LegacyBlock(204, "purpur_double_slab"),
            new LegacyBlock(205, "purpur_slab"),
            new LegacyBlock(206, "end_bricks", "end_stone_bricks"),
            new LegacyBlock(207, "beetroot_block", "beetroots"), new LegacyBlock(208, "grass_path"),
            new LegacyBlock(209, "end_gateway"),
            new LegacyBlock(210, "command_repeating", "repeating_command_block"),
            new LegacyBlock(211, "command_chain", "chain_command_block"),
            new LegacyBlock(212, "frosted_ice"), new LegacyBlock(213, "magma", "magma_block"),
            new LegacyBlock(214, "nether_wart_block"),
            new LegacyBlock(215, "red_nether_brick", "red_nether_bricks"),
            new LegacyBlock(216, "bone_block"), new LegacyBlock(217, "structure_void"),
            new LegacyBlock(218, "observer"), new LegacyBlock(219, "white_shulker_box"),
            new LegacyBlock(220, "orange_shulker_box"), new LegacyBlock(221, "magenta_shulker_box"),
            new LegacyBlock(222, "light_blue_shulker_box"),
            new LegacyBlock(223, "yellow_shulker_box"), new LegacyBlock(224, "lime_shulker_box"),
            new LegacyBlock(225, "pink_shulker_box"), new LegacyBlock(226, "gray_shulker_box"),
            new LegacyBlock(227, "silver_shulker_box", "light_gray_shulker_box"),
            new LegacyBlock(228, "cyan_shulker_box"), new LegacyBlock(229, "purple_shulker_box"),
            new LegacyBlock(230, "blue_shulker_box"), new LegacyBlock(231, "brown_shulker_box"),
            new LegacyBlock(232, "green_shulker_box"), new LegacyBlock(233, "red_shulker_box"),
            new LegacyBlock(234, "black_shulker_box"),
            new LegacyBlock(235, "white_glazed_terracotta"),
            new LegacyBlock(236, "orange_glazed_terracotta"),
            new LegacyBlock(237, "magenta_glazed_terracotta"),
            new LegacyBlock(238, "light_blue_glazed_terracotta"),
            new LegacyBlock(239, "yellow_glazed_terracotta"),
            new LegacyBlock(240, "lime_glazed_terracotta"),
            new LegacyBlock(241, "pink_glazed_terracotta"),
            new LegacyBlock(242, "gray_glazed_terracotta"),
            new LegacyBlock(243, "silver_glazed_terracotta", "light_gray_glazed_terracotta"),
            new LegacyBlock(244, "cyan_glazed_terracotta"),
            new LegacyBlock(245, "purple_glazed_terracotta"),
            new LegacyBlock(246, "blue_glazed_terracotta"),
            new LegacyBlock(247, "brown_glazed_terracotta"),
            new LegacyBlock(248, "green_glazed_terracotta"),
            new LegacyBlock(249, "red_glazed_terracotta"),
            new LegacyBlock(250, "black_glazed_terracotta"),
            new LegacyBlock(251, "concrete", "white_concrete"),
            new LegacyBlock(251, 1, "concrete", "orange_concrete"),
            new LegacyBlock(251, 2, "concrete", "magenta_concrete"),
            new LegacyBlock(251, 3, "concrete", "light_blue_concrete"),
            new LegacyBlock(251, 4, "concrete", "yellow_concrete"),
            new LegacyBlock(251, 5, "concrete", "lime_concrete"),
            new LegacyBlock(251, 6, "concrete", "pink_concrete"),
            new LegacyBlock(251, 7, "concrete", "gray_concrete"),
            new LegacyBlock(251, 8, "concrete", "light_gray_concrete"),
            new LegacyBlock(251, 9, "concrete", "cyan_concrete"),
            new LegacyBlock(251, 10, "concrete", "purple_concrete"),
            new LegacyBlock(251, 11, "concrete", "blue_concrete"),
            new LegacyBlock(251, 12, "concrete", "brown_concrete"),
            new LegacyBlock(251, 13, "concrete", "green_concrete"),
            new LegacyBlock(251, 14, "concrete", "red_concrete"),
            new LegacyBlock(251, 15, "concrete", "black_concrete"),
            new LegacyBlock(252, "concrete_powder", "white_concrete_powder"),
            new LegacyBlock(252, 1, "concrete_powder", "orange_concrete_powder"),
            new LegacyBlock(252, 2, "concrete_powder", "magenta_concrete_powder"),
            new LegacyBlock(252, 3, "concrete_powder", "light_blue_concrete_powder"),
            new LegacyBlock(252, 4, "concrete_powder", "yellow_concrete_powder"),
            new LegacyBlock(252, 5, "concrete_powder", "lime_concrete_powder"),
            new LegacyBlock(252, 6, "concrete_powder", "pink_concrete_powder"),
            new LegacyBlock(252, 7, "concrete_powder", "gray_concrete_powder"),
            new LegacyBlock(252, 8, "concrete_powder", "light_gray_concrete_powder"),
            new LegacyBlock(252, 9, "concrete_powder", "cyan_concrete_powder"),
            new LegacyBlock(252, 10, "concrete_powder", "purple_concrete_powder"),
            new LegacyBlock(252, 11, "concrete_powder", "blue_concrete_powder"),
            new LegacyBlock(252, 12, "concrete_powder", "brown_concrete_powder"),
            new LegacyBlock(252, 13, "concrete_powder", "green_concrete_powder"),
            new LegacyBlock(252, 14, "concrete_powder", "red_concrete_powder"),
            new LegacyBlock(252, 15, "concrete_powder", "black_concrete_powder"),
            new LegacyBlock(255, "structure_block"),
            new LegacyBlock(256, "iron_spade", "iron_shovel"), new LegacyBlock(257, "iron_pickaxe"),
            new LegacyBlock(258, "iron_axe"), new LegacyBlock(259, "flint_and_steel"),
            new LegacyBlock(260, "apple"), new LegacyBlock(261, "bow"),
            new LegacyBlock(262, "arrow"), new LegacyBlock(263, "coal"),
            new LegacyBlock(263, 1, "coal", "charcoal"), new LegacyBlock(264, "diamond"),
            new LegacyBlock(265, "iron_ingot"), new LegacyBlock(266, "gold_ingot"),
            new LegacyBlock(267, "iron_sword"), new LegacyBlock(268, "wood_sword", "wooden_sword"),
            new LegacyBlock(269, "wood_spade", "wooden_shovel"),
            new LegacyBlock(270, "wood_pickaxe", "wooden_pickaxe"),
            new LegacyBlock(271, "wood_axe", "wooden_axe"), new LegacyBlock(272, "stone_sword"),
            new LegacyBlock(273, "stone_spade", "stone_shovel"),
            new LegacyBlock(274, "stone_pickaxe"), new LegacyBlock(275, "stone_axe"),
            new LegacyBlock(276, "diamond_sword"),
            new LegacyBlock(277, "diamond_spade", "diamond_shovel"),
            new LegacyBlock(278, "diamond_pickaxe"), new LegacyBlock(279, "diamond_axe"),
            new LegacyBlock(280, "stick"), new LegacyBlock(281, "bowl"),
            new LegacyBlock(282, "mushroom_soup", "mushroom_stew"),
            new LegacyBlock(283, "gold_sword", "golden_sword"),
            new LegacyBlock(284, "gold_spade", "golden_shovel"),
            new LegacyBlock(285, "gold_pickaxe", "golden_pickaxe"),
            new LegacyBlock(286, "gold_axe", "golden_axe"), new LegacyBlock(287, "string"),
            new LegacyBlock(288, "feather"), new LegacyBlock(289, "sulphur", "gunpowder"),
            new LegacyBlock(290, "wood_hoe", "wooden_hoe"), new LegacyBlock(291, "stone_hoe"),
            new LegacyBlock(292, "iron_hoe"), new LegacyBlock(293, "diamond_hoe"),
            new LegacyBlock(294, "gold_hoe", "golden_hoe"),
            new LegacyBlock(295, "seeds", "wheat_seeds"), new LegacyBlock(296, "wheat"),
            new LegacyBlock(297, "bread"), new LegacyBlock(298, "leather_helmet"),
            new LegacyBlock(299, "leather_chestplate"), new LegacyBlock(300, "leather_leggings"),
            new LegacyBlock(301, "leather_boots"), new LegacyBlock(302, "chainmail_helmet"),
            new LegacyBlock(303, "chainmail_chestplate"),
            new LegacyBlock(304, "chainmail_leggings"), new LegacyBlock(305, "chainmail_boots"),
            new LegacyBlock(306, "iron_helmet"), new LegacyBlock(307, "iron_chestplate"),
            new LegacyBlock(308, "iron_leggings"), new LegacyBlock(309, "iron_boots"),
            new LegacyBlock(310, "diamond_helmet"), new LegacyBlock(311, "diamond_chestplate"),
            new LegacyBlock(312, "diamond_leggings"), new LegacyBlock(313, "diamond_boots"),
            new LegacyBlock(314, "gold_helmet", "golden_helmet"),
            new LegacyBlock(315, "gold_chestplate", "golden_chestplate"),
            new LegacyBlock(316, "gold_leggings", "golden_leggings"),
            new LegacyBlock(317, "gold_boots", "golden_boots"), new LegacyBlock(318, "flint"),
            new LegacyBlock(319, "pork", "porkchop"),
            new LegacyBlock(320, "grilled_pork", "cooked_porkchop"),
            new LegacyBlock(321, "painting"), new LegacyBlock(322, "golden_apple", "golden_apple"),
            new LegacyBlock(322, 1, "golden_apple", "enchanted_golden_apple"),
            new LegacyBlock(323, "sign"), new LegacyBlock(324, "wood_door", "oak_door"),
            new LegacyBlock(325, "bucket"), new LegacyBlock(326, "water_bucket"),
            new LegacyBlock(327, "lava_bucket"), new LegacyBlock(328, "minecart"),
            new LegacyBlock(329, "saddle"), new LegacyBlock(330, "iron_door"),
            new LegacyBlock(331, "redstone"), new LegacyBlock(332, "snow_ball", "snowball"),
            new LegacyBlock(333, "boat", "oak_boat"), new LegacyBlock(334, "leather"),
            new LegacyBlock(335, "milk_bucket"), new LegacyBlock(336, "clay_brick", "brick"),
            new LegacyBlock(337, "clay_ball"), new LegacyBlock(338, "sugar_cane"),
            new LegacyBlock(339, "paper"), new LegacyBlock(340, "book"),
            new LegacyBlock(341, "slime_ball"),
            new LegacyBlock(342, "storage_minecart", "chest_minecart"),
            new LegacyBlock(343, "powered_minecart", "furnace_minecart"),
            new LegacyBlock(344, "egg"), new LegacyBlock(345, "compass"),
            new LegacyBlock(346, "fishing_rod"), new LegacyBlock(347, "watch", "clock"),
            new LegacyBlock(348, "glowstone_dust"), new LegacyBlock(349, "raw_fish", "cod"),
            new LegacyBlock(349, 1, "raw_fish", "salmon"),
            new LegacyBlock(349, 2, "raw_fish", "tropical_fish"),
            new LegacyBlock(349, 3, "raw_fish", "pufferfish"),
            new LegacyBlock(350, "cooked_fish", "cooked_cod"),
            new LegacyBlock(350, 1, "cooked_fish", "cooked_salmon"),
            new LegacyBlock(351, "ink_sack", "ink_sac"),
            new LegacyBlock(351, 1, "ink_sack", "rose_red"),
            new LegacyBlock(351, 2, "ink_sack", "cactus_green"),
            new LegacyBlock(351, 3, "ink_sack", "cocoa_beans"),
            new LegacyBlock(351, 4, "ink_sack", "lapis_lazuli"),
            new LegacyBlock(351, 5, "ink_sack", "purple_dye"),
            new LegacyBlock(351, 6, "ink_sack", "cyan_dye"),
            new LegacyBlock(351, 7, "ink_sack", "light_gray_dye"),
            new LegacyBlock(351, 8, "ink_sack", "gray_dye"),
            new LegacyBlock(351, 9, "ink_sack", "pink_dye"),
            new LegacyBlock(351, 10, "ink_sack", "lime_dye"),
            new LegacyBlock(351, 11, "ink_sack", "dandelion_yellow"),
            new LegacyBlock(351, 12, "ink_sack", "light_blue_dye"),
            new LegacyBlock(351, 13, "ink_sack", "magenta_dye"),
            new LegacyBlock(351, 14, "ink_sack", "orange_dye"),
            new LegacyBlock(351, 15, "ink_sack", "bone_meal"), new LegacyBlock(352, "bone"),
            new LegacyBlock(353, "sugar", "sugar"), new LegacyBlock(354, "cake", "cake"),
            new LegacyBlock(355, "bed", "white_bed"), new LegacyBlock(355, 1, "bed", "orange_bed"),
            new LegacyBlock(355, 2, "bed", "magenta_bed"),
            new LegacyBlock(355, 3, "bed", "light_blue_bed"),
            new LegacyBlock(355, 4, "bed", "yellow_bed"),
            new LegacyBlock(355, 5, "bed", "lime_bed"), new LegacyBlock(355, 6, "bed", "pink_bed"),
            new LegacyBlock(355, 7, "bed", "gray_bed"),
            new LegacyBlock(355, 8, "bed", "light_gray_bed"),
            new LegacyBlock(355, 9, "bed", "cyan_bed"),
            new LegacyBlock(355, 10, "bed", "purple_bed"),
            new LegacyBlock(355, 11, "bed", "blue_bed"),
            new LegacyBlock(355, 12, "bed", "brown_bed"),
            new LegacyBlock(355, 13, "bed", "green_bed"),
            new LegacyBlock(355, 14, "bed", "red_bed"),
            new LegacyBlock(355, 15, "bed", "black_bed"), new LegacyBlock(356, "diode", "repeater"),
            new LegacyBlock(357, "cookie", "cookie"), new LegacyBlock(358, "map"),
            new LegacyBlock(359, "shears"), new LegacyBlock(360, "melon", "melon"),
            new LegacyBlock(361, "pumpkin_seeds", "pumpkin_seeds"),
            new LegacyBlock(362, "melon_seeds", "melon_seeds"),
            new LegacyBlock(363, "raw_beef", "beef"), new LegacyBlock(364, "cooked_beef"),
            new LegacyBlock(365, "raw_chicken", "chicken"), new LegacyBlock(366, "cooked_chicken"),
            new LegacyBlock(367, "rotten_flesh"), new LegacyBlock(368, "ender_pearl"),
            new LegacyBlock(369, "blaze_rod"), new LegacyBlock(370, "ghast_tear"),
            new LegacyBlock(371, "gold_nugget"),
            new LegacyBlock(372, "nether_stalk", "nether_wart"),
            new LegacyBlock(373, "potion", "potion"), new LegacyBlock(374, "glass_bottle"),
            new LegacyBlock(375, "spider_eye"), new LegacyBlock(376, "fermented_spider_eye"),
            new LegacyBlock(377, "blaze_powder"), new LegacyBlock(378, "magma_cream"),
            new LegacyBlock(379, "brewing_stand_item", "brewing_stand"),
            new LegacyBlock(380, "cauldron_item", "cauldron"),
            new LegacyBlock(381, "eye_of_ender", "ender_eye"),
            new LegacyBlock(382, "speckled_melon"), new LegacyBlock(383, "monster_egg"),
            new LegacyBlock(383, 4, "monster_egg", "elder_guardian_spawn_egg"),
            new LegacyBlock(383, 5, "monster_egg", "wither_skeleton_spawn_egg"),
            new LegacyBlock(383, 6, "monster_egg", "stray_spawn_egg"),
            new LegacyBlock(383, 23, "monster_egg", "husk_spawn_egg"),
            new LegacyBlock(383, 27, "monster_egg", "zombe_villager_spawn_egg"),
            new LegacyBlock(383, 28, "monster_egg", "skeleton_horse_spawn_egg"),
            new LegacyBlock(383, 29, "monster_egg", "zombie_horse_spawn_egg"),
            new LegacyBlock(383, 31, "monster_egg", "donkey_spawn_egg"),
            new LegacyBlock(383, 32, "monster_egg", "mule_spawn_egg"),
            new LegacyBlock(383, 34, "monster_egg", "evocation_illager_spawn_egg"),
            new LegacyBlock(383, 35, "monster_egg", "vex_spawn_egg"),
            new LegacyBlock(383, 36, "monster_egg", "vindication_illager_spawn_egg"),
            new LegacyBlock(383, 50, "monster_egg", "creeper_spawn_egg"),
            new LegacyBlock(383, 51, "monster_egg", "skeleton_spawn_egg"),
            new LegacyBlock(383, 52, "monster_egg", "spider_spawn_egg"),
            new LegacyBlock(383, 54, "monster_egg", "zombie_spawn_egg"),
            new LegacyBlock(383, 55, "monster_egg", "slime_spawn_egg"),
            new LegacyBlock(383, 56, "monster_egg", "ghast_spawn_egg"),
            new LegacyBlock(383, 57, "monster_egg", "zombie_pigman_spawn_egg"),
            new LegacyBlock(383, 58, "monster_egg", "enderman_spawn_egg"),
            new LegacyBlock(383, 59, "monster_egg", "cave_spider_spawn_egg"),
            new LegacyBlock(383, 60, "monster_egg", "silverfish_spawn_egg"),
            new LegacyBlock(383, 61, "monster_egg", "blaze_spawn_egg"),
            new LegacyBlock(383, 62, "monster_egg", "magma_cube_spawn_egg"),
            new LegacyBlock(383, 65, "monster_egg", "bat_spawn_egg"),
            new LegacyBlock(383, 66, "monster_egg", "witch_spawn_egg"),
            new LegacyBlock(383, 67, "monster_egg", "endermite_spawn_egg"),
            new LegacyBlock(383, 68, "monster_egg", "guardian_spawn_egg"),
            new LegacyBlock(383, 69, "monster_egg", "shulker_spawn_egg"),
            new LegacyBlock(383, 90, "monster_egg", "pig_spawn_egg"),
            new LegacyBlock(383, 91, "monster_egg", "sheep_spawn_egg"),
            new LegacyBlock(383, 92, "monster_egg", "cow_spawn_egg"),
            new LegacyBlock(383, 93, "monster_egg", "chicken_spawn_egg"),
            new LegacyBlock(383, 94, "monster_egg", "squid_spawn_egg"),
            new LegacyBlock(383, 95, "monster_egg", "wolf_spawn_egg"),
            new LegacyBlock(383, 96, "monster_egg", "mooshroom_spawn_egg"),
            new LegacyBlock(383, 98, "monster_egg", "ocelot_spawn_egg"),
            new LegacyBlock(383, 100, "monster_egg", "horse_spawn_egg"),
            new LegacyBlock(383, 101, "monster_egg", "rabbit_spawn_egg"),
            new LegacyBlock(383, 102, "monster_egg", "polar_bear_spawn_egg"),
            new LegacyBlock(383, 103, "monster_egg", "llama_spawn_egg"),
            new LegacyBlock(383, 120, "monster_egg", "villager_spawn_egg"),
            new LegacyBlock(384, "exp_bottle", "experience_bottle"),
            new LegacyBlock(385, "fireball", "fire_charge"),
            new LegacyBlock(386, "book_and_quill", "writable_book"),
            new LegacyBlock(387, "written_book"), new LegacyBlock(388, "emerald"),
            new LegacyBlock(389, "item_frame"),
            new LegacyBlock(390, "flower_pot_item", "flower_pot"),
            new LegacyBlock(391, "carrot_item", "carrot"),
            new LegacyBlock(392, "potato_item", "potato"), new LegacyBlock(393, "baked_potato"),
            new LegacyBlock(394, "poisonous_potato"), new LegacyBlock(395, "empty_map", "map"),
            new LegacyBlock(396, "golden_carrot"),
            new LegacyBlock(397, "skull_item", "skeleton_skull"),
            new LegacyBlock(397, 1, "skull_item", "wither_skeleton_skull"),
            new LegacyBlock(397, 2, "skull_item", "zombie_head"),
            new LegacyBlock(397, 3, "skull_item", "player_head"),
            new LegacyBlock(397, 4, "skull_item", "creeper_head"),
            new LegacyBlock(397, 5, "skull_item", "dragon_head"),
            new LegacyBlock(398, "carrot_stick"), new LegacyBlock(399, "nether_star"),
            new LegacyBlock(400, "pumpkin_pie"),
            new LegacyBlock(401, "firework", "firework_rocket"),
            new LegacyBlock(402, "firework_charge", "firework_star"),
            new LegacyBlock(403, "enchanted_book"),
            new LegacyBlock(404, "redstone_comparator", "comparator"),
            new LegacyBlock(405, "nether_brick_item", "nether_brick"),
            new LegacyBlock(406, "quartz"),
            new LegacyBlock(407, "explosive_minecart", "tnt_minecart"),
            new LegacyBlock(408, "hopper_minecart"), new LegacyBlock(409, "prismarine_shard"),
            new LegacyBlock(410, "prismarine_crystals"), new LegacyBlock(411, "rabbit"),
            new LegacyBlock(412, "cooked_rabbit"), new LegacyBlock(413, "rabbit_stew"),
            new LegacyBlock(414, "rabbit_foot"), new LegacyBlock(415, "rabbit_hide"),
            new LegacyBlock(416, "armor_stand"),
            new LegacyBlock(417, "iron_barding", "iron_horse_armor"),
            new LegacyBlock(418, "gold_barding", "gold_horse_armor"),
            new LegacyBlock(419, "diamond_barding", "diamond_horse_armor"),
            new LegacyBlock(420, "leash", "lead"), new LegacyBlock(421, "name_tag"),
            new LegacyBlock(422, "command_minecart", "command_block_minecart"),
            new LegacyBlock(423, "mutton"), new LegacyBlock(424, "cooked_mutton"),
            new LegacyBlock(425, "banner", "white_banner"),
            new LegacyBlock(425, 1, "banner", "orange_banner"),
            new LegacyBlock(425, 2, "banner", "magenta_banner"),
            new LegacyBlock(425, 3, "banner", "light_blue_banner"),
            new LegacyBlock(425, 4, "banner", "yellow_banner"),
            new LegacyBlock(425, 5, "banner", "lime_banner"),
            new LegacyBlock(425, 6, "banner", "pink_banner"),
            new LegacyBlock(425, 7, "banner", "gray_banner"),
            new LegacyBlock(425, 8, "banner", "light_gray_banner"),
            new LegacyBlock(425, 9, "banner", "cyan_banner"),
            new LegacyBlock(425, 10, "banner", "purple_banner"),
            new LegacyBlock(425, 11, "banner", "blue_banner"),
            new LegacyBlock(425, 12, "banner", "brown_banner"),
            new LegacyBlock(425, 13, "banner", "green_banner"),
            new LegacyBlock(425, 14, "banner", "red_banner"),
            new LegacyBlock(425, 15, "banner", "black_banner"), new LegacyBlock(426, "end_crystal"),
            new LegacyBlock(427, "spruce_door_item", "spruce_door"),
            new LegacyBlock(428, "birch_door_item", "birch_door"),
            new LegacyBlock(429, "jungle_door_item", "jungle_door"),
            new LegacyBlock(430, "acacia_door_item", "acacia_door"),
            new LegacyBlock(431, "dark_oak_door_item", "dark_oak_door"),
            new LegacyBlock(432, "chorus_fruit"), new LegacyBlock(433, "chorus_fruit_popped"),
            new LegacyBlock(434, "beetroot"), new LegacyBlock(435, "beetroot_seeds"),
            new LegacyBlock(436, "beetroot_soup"),
            new LegacyBlock(437, "dragons_breath", "dragon_breath"),
            new LegacyBlock(438, "splash_potion"), new LegacyBlock(439, "spectral_arrow"),
            new LegacyBlock(440, "tipped_arrow"), new LegacyBlock(441, "lingering_potion"),
            new LegacyBlock(442, "shield"), new LegacyBlock(443, "elytra"),
            new LegacyBlock(444, "boat_spruce", "spruce_boat"),
            new LegacyBlock(445, "boat_birch", "birch_boat"),
            new LegacyBlock(446, "boat_jungle", "jungle_boat"),
            new LegacyBlock(447, "boat_acacia", "acacia_boat"),
            new LegacyBlock(448, "boat_dark_oak", "dark_oak_boat"),
            new LegacyBlock(449, "totem", "totem_of_undying"),
            new LegacyBlock(450, "shulker_shell"), new LegacyBlock(452, "iron_nugget"),
            new LegacyBlock(453, "knowledge_book"),
            new LegacyBlock(2256, "gold_record", "music_disc_13"),
            new LegacyBlock(2257, "green_record", "music_disc_cat"),
            new LegacyBlock(2258, "record_3", "music_disc_blocks"),
            new LegacyBlock(2259, "record_4", "music_disc_chirp"),
            new LegacyBlock(2260, "record_5", "music_disc_far"),
            new LegacyBlock(2261, "record_6", "music_disc_mall"),
            new LegacyBlock(2262, "record_7", "music_disc_mellohi"),
            new LegacyBlock(2263, "record_8", "music_disc_stal"),
            new LegacyBlock(2264, "record_9", "music_disc_strad"),
            new LegacyBlock(2265, "record_10", "music_disc_ward"),
            new LegacyBlock(2266, "record_11", "music_disc_11"),
            new LegacyBlock(2267, "record_12", "music_disc_wait")};

    // private static final Map<Integer, PlotBlock> LEGACY_ID_TO_STRING_PLOT_BLOCK = new HashMap<>();
    private static final Map<IdDataPair, PlotBlock> LEGACY_ID_AND_DATA_TO_STRING_PLOT_BLOCK =
        new HashMap<>();
    private static final Map<String, PlotBlock> NEW_STRING_TO_LEGACY_PLOT_BLOCK = new HashMap<>();
    private static final Map<String, PlotBlock> OLD_STRING_TO_STRING_PLOT_BLOCK = new HashMap<>();

    @SuppressWarnings("deprecation") public BukkitLegacyMappings() {
        this.addAll(Arrays.asList(BLOCKS));
        // Make sure to add new blocks as well
        final List<LegacyBlock> missing = new ArrayList<>();
        for (final Material material : Material.values()) {
            final String materialName = material.name().toLowerCase(Locale.ENGLISH);
            if (OLD_STRING_TO_STRING_PLOT_BLOCK.get(materialName) == null) {
                final LegacyBlock missingBlock =
                    new LegacyBlock(material.getId(), materialName, materialName);
                missing.add(missingBlock);
            }
        }
        addAll(missing);
    }

    private void addAll(@NonNull final Collection<LegacyBlock> blocks) {
        for (final LegacyBlock legacyBlock : blocks) {
            // LEGACY_ID_TO_STRING_PLOT_BLOCK
            //     .put(legacyBlock.getNumericalId(), legacyBlock.toStringPlotBlock());
            /*if (legacyBlock.getDataValue() != 0) {
                LEGACY_ID_AND_DATA_TO_STRING_PLOT_BLOCK
                    .put(new IdDataPair(legacyBlock.getNumericalId(), legacyBlock.getDataValue()),
                        legacyBlock.toStringPlotBlock());
            } */
            LEGACY_ID_AND_DATA_TO_STRING_PLOT_BLOCK
                .put(new IdDataPair(legacyBlock.getNumericalId(), legacyBlock.getDataValue()),
                    legacyBlock.toStringPlotBlock());
            NEW_STRING_TO_LEGACY_PLOT_BLOCK
                .put(legacyBlock.getNewName(), legacyBlock.toLegacyPlotBlock());
            OLD_STRING_TO_STRING_PLOT_BLOCK
                .put(legacyBlock.getLegacyName(), legacyBlock.toStringPlotBlock());
            Material material;
            try {
                material = Material.valueOf(legacyBlock.getNewName());
            } catch (final Exception e) {
                material = Material.getMaterial(legacyBlock.getLegacyName(), true);
            }
            legacyBlock.material = material;
        }
    }

    public Collection<PlotBlock> getPlotBlocks() {
        return Arrays.stream(BLOCKS).map(block -> PlotBlock.get(block.getNewName()))
            .collect(Collectors.toList());
    }

    public StringComparison<PlotBlock>.ComparisonResult getClosestsMatch(
        @NonNull final String string) {
        final StringComparison<PlotBlock> comparison =
            new StringComparison<>(string, getPlotBlocks());
        return comparison.getBestMatchAdvanced();
    }

    /**
     * Try to find a legacy plot block by any means possible.
     * Strategy:
     * - Check if the name contains a namespace, if so, strip it
     * - Check if there's a (new) material matching the name
     * - Check if there's a legacy material matching the name
     * - Check if there's a numerical ID matching the name
     * - Return null if everything else fails
     *
     * @param string String ID
     * @return LegacyBlock if found, else null
     */
    public PlotBlock fromAny(@NonNull final String string) {
        if (string.isEmpty()) {
            return StringPlotBlock.EVERYTHING;
        }
        String workingString = string;
        String[] parts = null;
        if (string.contains(":")) {
            parts = string.split(":");
            if (parts.length > 1) {
                if (parts[0].equalsIgnoreCase("minecraft")) {
                    workingString = parts[1];
                } else {
                    workingString = parts[0];
                }
            }
        }
        PlotBlock plotBlock;
        if (NEW_STRING_TO_LEGACY_PLOT_BLOCK.keySet().contains(workingString.toLowerCase())) {
            return PlotBlock.get(workingString);
        } else if ((plotBlock = fromLegacyToString(workingString)) != null) {
            return plotBlock;
        } else {
            try {
                if (parts != null && parts.length > 1) {
                    final int id = Integer.parseInt(parts[0]);
                    final int data = Integer.parseInt(parts[1]);
                    return fromLegacyToString(id, data);
                } else {
                    return fromLegacyToString(Integer.parseInt(workingString), 0);
                }
            } catch (final Throwable exception) {
                return null;
            }
        }
    }

    public PlotBlock fromLegacyToString(final int id, final int data) {
        return LEGACY_ID_AND_DATA_TO_STRING_PLOT_BLOCK.get(new IdDataPair(id, data));
    }

    public PlotBlock fromLegacyToString(final String id) {
        return OLD_STRING_TO_STRING_PLOT_BLOCK.get(id);
    }

    public PlotBlock fromStringToLegacy(final String id) {
        return NEW_STRING_TO_LEGACY_PLOT_BLOCK.get(id.toLowerCase(Locale.ENGLISH));
    }

    @Getter @EqualsAndHashCode @ToString @RequiredArgsConstructor
    private static final class IdDataPair {
        private final int id;
        private final int data;
    }


    @Getter @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class LegacyBlock {

        private final int numericalId;
        private final int dataValue;
        private final String legacyName;
        private final String newName;

        private Material material;

        LegacyBlock(final int numericalId, final int dataValue, @NonNull final String legacyName) {
            this(numericalId, dataValue, legacyName, legacyName);
        }

        LegacyBlock(final int numericalId, @NonNull final String legacyName,
            @NonNull final String newName) {
            this(numericalId, 0, legacyName, newName);
        }

        LegacyBlock(final int numericalId, @NonNull final String legacyName) {
            this(numericalId, legacyName, legacyName);
        }

        PlotBlock toStringPlotBlock() {
            return StringPlotBlock.get(newName);
        }

        PlotBlock toLegacyPlotBlock() {
            return LegacyPlotBlock.get(numericalId, dataValue);
        }

        @Override public String toString() {
            return this.newName;
        }
    }

}
