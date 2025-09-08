/*
 * PlotSquared, a land and world management plugin for Minecraft.
 * Copyright (C) IntellectualSites <https://intellectualsites.com>
 * Copyright (C) IntellectualSites team and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.plotsquared.core.generator;

import com.plotsquared.core.configuration.ConfigurationNode;
import com.plotsquared.core.configuration.ConfigurationSection;
import com.plotsquared.core.configuration.ConfigurationUtil;
import com.plotsquared.core.configuration.Settings;
import com.plotsquared.core.configuration.caption.TranslatableCaption;
import com.plotsquared.core.configuration.file.YamlConfiguration;
import com.plotsquared.core.inject.annotations.WorldConfig;
import com.plotsquared.core.plot.BlockBucket;
import com.plotsquared.core.plot.PlotId;
import com.plotsquared.core.queue.GlobalBlockQueue;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.world.block.BlockTypes;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.nullness.qual.NonNull;

import javax.annotation.Nullable;

@SuppressWarnings("WeakerAccess")
public abstract class ClassicPlotWorld extends SquarePlotWorld {

    private static final Logger LOGGER = LogManager.getLogger("PlotSquared/" + ClassicPlotWorld.class.getSimpleName());

    public int ROAD_HEIGHT = 62;
    public int PLOT_HEIGHT = 62;
    public int WALL_HEIGHT = 62;
    public BlockBucket MAIN_BLOCK = new BlockBucket(BlockTypes.STONE);
    public BlockBucket TOP_BLOCK = new BlockBucket(BlockTypes.GRASS_BLOCK);
    public BlockBucket WALL_BLOCK = new BlockBucket(BlockTypes.STONE_SLAB);
    public BlockBucket CLAIMED_WALL_BLOCK = new BlockBucket(BlockTypes.SANDSTONE_SLAB);
    public BlockBucket WALL_FILLING = new BlockBucket(BlockTypes.STONE);
    public BlockBucket ROAD_BLOCK = new BlockBucket(BlockTypes.QUARTZ_BLOCK);
    public boolean PLOT_BEDROCK = true;
    public boolean PLACE_TOP_BLOCK = true;
    public boolean COMPONENT_BELOW_BEDROCK = false;

    public ClassicPlotWorld(
            final @NonNull String worldName,
            final @Nullable String id,
            final @NonNull IndependentPlotGenerator generator,
            final @Nullable PlotId min,
            final @Nullable PlotId max,
            @WorldConfig final @NonNull YamlConfiguration worldConfiguration,
            final @NonNull GlobalBlockQueue blockQueue
    ) {
        super(worldName, id, generator, min, max, worldConfiguration, blockQueue);
    }

    private static BlockBucket createCheckedBlockBucket(String input, BlockBucket def) {
        final BlockBucket bucket = new BlockBucket(input);
        Pattern pattern = null;
        try {
            pattern = bucket.toPattern();
        } catch (Exception ignore) {
        }
        if (pattern == null) {
            LOGGER.error("Failed to parse pattern '{}', check your worlds.yml", input);
            LOGGER.error("Falling back to {}", def);
            return def;
        }
        return bucket;
    }

    /**
     * CONFIG NODE | DEFAULT VALUE | DESCRIPTION | CONFIGURATION TYPE | REQUIRED FOR INITIAL SETUP.
     *
     * <p>Set the last boolean to false if you do not check a specific config node to be set while using the setup
     * command - this may be useful if a config value can be changed at a later date, and has no impact on the actual
     * world generation</p>
     */
    @NonNull
    @Override
    public ConfigurationNode[] getSettingNodes() {
        return new ConfigurationNode[]{
                new ConfigurationNode("plot.height", this.PLOT_HEIGHT, TranslatableCaption.of("setup.plot_height"),
                        ConfigurationUtil.INTEGER
                ),
                new ConfigurationNode("plot.size", this.PLOT_WIDTH, TranslatableCaption.of("setup.plot_width"),
                        ConfigurationUtil.INTEGER
                ),
                new ConfigurationNode("plot.filling", this.MAIN_BLOCK, TranslatableCaption.of("setup.plot_block"),
                        ConfigurationUtil.BLOCK_BUCKET
                ),
                new ConfigurationNode("wall.place_top_block", this.PLACE_TOP_BLOCK,
                        TranslatableCaption.of("setup.top_block_boolean"), ConfigurationUtil.BOOLEAN
                ),
                new ConfigurationNode("plot.floor", this.TOP_BLOCK, TranslatableCaption.of("setup.plot_block_floor"),
                        ConfigurationUtil.BLOCK_BUCKET
                ),
                new ConfigurationNode("wall.block", this.WALL_BLOCK, TranslatableCaption.of("setup.top_wall_block"),
                        ConfigurationUtil.BLOCK_BUCKET
                ),
                new ConfigurationNode("wall.block_claimed", this.CLAIMED_WALL_BLOCK,
                        TranslatableCaption.of("setup.wall_block_claimed"), ConfigurationUtil.BLOCK_BUCKET
                ),
                new ConfigurationNode("road.width", this.ROAD_WIDTH, TranslatableCaption.of("setup.road_width"),
                        ConfigurationUtil.INTEGER
                ),
                new ConfigurationNode("road.height", this.ROAD_HEIGHT, TranslatableCaption.of("setup.road_height"),
                        ConfigurationUtil.INTEGER
                ),
                new ConfigurationNode("road.block", this.ROAD_BLOCK, TranslatableCaption.of("setup.road_block"),
                        ConfigurationUtil.BLOCK_BUCKET
                ),
                new ConfigurationNode("wall.filling", this.WALL_FILLING, TranslatableCaption.of("setup.wall_filling_block"),
                        ConfigurationUtil.BLOCK_BUCKET
                ),
                new ConfigurationNode("wall.height", this.WALL_HEIGHT, TranslatableCaption.of("setup.wall_height"),
                        ConfigurationUtil.INTEGER
                ),
                new ConfigurationNode("plot.bedrock", this.PLOT_BEDROCK, TranslatableCaption.of("setup.bedrock_boolean"),
                        ConfigurationUtil.BOOLEAN
                ),
                new ConfigurationNode("world.component_below_bedrock", this.COMPONENT_BELOW_BEDROCK, TranslatableCaption.of(
                        "setup.component_below_bedrock_boolean"), ConfigurationUtil.BOOLEAN
                )};
    }

    /**
     * This method is called when a world loads. Make sure you set all your constants here. You are provided with the
     * configuration section for that specific world.
     */
    @Override
    public void loadConfiguration(ConfigurationSection config) {
        super.loadConfiguration(config);
        this.PLOT_BEDROCK = config.getBoolean("plot.bedrock");
        this.PLOT_HEIGHT = Math.min(getMaxGenHeight(), config.getInt("plot.height"));
        this.MAIN_BLOCK = createCheckedBlockBucket(config.getString("plot.filling"), MAIN_BLOCK);
        this.TOP_BLOCK = createCheckedBlockBucket(config.getString("plot.floor"), TOP_BLOCK);
        this.WALL_BLOCK = createCheckedBlockBucket(config.getString("wall.block"), WALL_BLOCK);
        this.ROAD_HEIGHT = Math.min(getMaxGenHeight(), config.getInt("road.height"));
        this.ROAD_BLOCK = createCheckedBlockBucket(config.getString("road.block"), ROAD_BLOCK);
        this.WALL_FILLING = createCheckedBlockBucket(config.getString("wall.filling"), WALL_FILLING);
        this.PLACE_TOP_BLOCK = config.getBoolean("wall.place_top_block");
        this.WALL_HEIGHT = Math.min(getMaxGenHeight() - (PLACE_TOP_BLOCK ? 1 : 0), config.getInt("wall.height"));
        this.CLAIMED_WALL_BLOCK = createCheckedBlockBucket(config.getString("wall.block_claimed"), CLAIMED_WALL_BLOCK);
        this.COMPONENT_BELOW_BEDROCK = config.getBoolean("world.component_below_bedrock");
    }

    @Override
    public int getMinComponentHeight() {
        return COMPONENT_BELOW_BEDROCK && getMinGenHeight() >= getMinBuildHeight()
                ? getMinGenHeight() + (PLOT_BEDROCK ? 1 : 0)
                : getMinBuildHeight();
    }

    int schematicStartHeight() {
        int plotRoadMin = Math.min(PLOT_HEIGHT, ROAD_HEIGHT);
        if (!Settings.Schematics.USE_WALL_IN_ROAD_SCHEM_HEIGHT) {
            return plotRoadMin;
        }
        return Math.min(WALL_HEIGHT, plotRoadMin);
    }

}
