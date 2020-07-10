/*
 *       _____  _       _    _____                                _
 *      |  __ \| |     | |  / ____|                              | |
 *      | |__) | | ___ | |_| (___   __ _ _   _  __ _ _ __ ___  __| |
 *      |  ___/| |/ _ \| __|\___ \ / _` | | | |/ _` | '__/ _ \/ _` |
 *      | |    | | (_) | |_ ____) | (_| | |_| | (_| | | |  __/ (_| |
 *      |_|    |_|\___/ \__|_____/ \__, |\__,_|\__,_|_|  \___|\__,_|
 *                                    | |
 *                                    |_|
 *            PlotSquared plot management system for Minecraft
 *                  Copyright (C) 2020 IntellectualSites
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.plotsquared.core.generator;

import com.plotsquared.core.inject.annotations.WorldConfig;
import com.plotsquared.core.configuration.ConfigurationNode;
import com.plotsquared.core.configuration.ConfigurationSection;
import com.plotsquared.core.configuration.ConfigurationUtil;
import com.plotsquared.core.configuration.file.YamlConfiguration;
import com.plotsquared.core.listener.PlotListener;
import com.plotsquared.core.plot.BlockBucket;
import com.plotsquared.core.plot.PlotId;
import com.plotsquared.core.util.EventDispatcher;
import com.sk89q.worldedit.world.block.BlockTypes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("WeakerAccess")
public abstract class ClassicPlotWorld extends SquarePlotWorld {

    public int ROAD_HEIGHT = 62;
    public int PLOT_HEIGHT = 62;
    public int WALL_HEIGHT = 62;
    public BlockBucket MAIN_BLOCK = new BlockBucket(BlockTypes.STONE);
    // new BlockState[] {BlockUtil.get("stone")};
    public BlockBucket TOP_BLOCK = new BlockBucket(BlockTypes.GRASS_BLOCK);
    //new BlockState[] {BlockUtil.get("grass")};
    public BlockBucket WALL_BLOCK = new BlockBucket(BlockTypes.STONE_SLAB);
    // BlockUtil.get((short) 44, (byte) 0);
    public BlockBucket CLAIMED_WALL_BLOCK = new BlockBucket(BlockTypes.SANDSTONE_SLAB);
    // BlockUtil.get((short) 44, (byte) 1);
    public BlockBucket WALL_FILLING = new BlockBucket(BlockTypes.STONE);
    //BlockUtil.get((short) 1, (byte) 0);
    public BlockBucket ROAD_BLOCK = new BlockBucket(BlockTypes.QUARTZ_BLOCK);
    // BlockUtil.get((short) 155, (byte) 0);
    public boolean PLOT_BEDROCK = true;

    public ClassicPlotWorld(@NotNull final String worldName,
                            @Nullable final String id,
                            @NotNull final IndependentPlotGenerator generator,
                            @NotNull final PlotId min,
                            @NotNull final PlotId max,
                            @NotNull final EventDispatcher eventDispatcher,
                            @NotNull final PlotListener plotListener,
                            @WorldConfig @NotNull final YamlConfiguration worldConfiguration) {
        super(worldName, id, generator, min, max, eventDispatcher, plotListener, worldConfiguration);
    }

    /**
     * CONFIG NODE | DEFAULT VALUE | DESCRIPTION | CONFIGURATION TYPE | REQUIRED FOR INITIAL SETUP.
     * <p>
     * <p>Set the last boolean to false if you do not check a specific config node to be set while using the setup
     * command - this may be useful if a config value can be changed at a later date, and has no impact on the actual
     * world generation</p>
     */
    @NotNull @Override public ConfigurationNode[] getSettingNodes() {
        return new ConfigurationNode[] {
            new ConfigurationNode("plot.height", this.PLOT_HEIGHT, "Plot height",
                ConfigurationUtil.INTEGER),
            new ConfigurationNode("plot.size", this.PLOT_WIDTH, "Plot width",
                ConfigurationUtil.INTEGER),
            new ConfigurationNode("plot.filling", this.MAIN_BLOCK, "Plot block",
                ConfigurationUtil.BLOCK_BUCKET),
            new ConfigurationNode("plot.floor", this.TOP_BLOCK, "Plot floor block",
                ConfigurationUtil.BLOCK_BUCKET),
            new ConfigurationNode("wall.block", this.WALL_BLOCK, "Top wall block",
                ConfigurationUtil.BLOCK_BUCKET),
            new ConfigurationNode("wall.block_claimed", this.CLAIMED_WALL_BLOCK,
                "Wall block (claimed)", ConfigurationUtil.BLOCK_BUCKET),
            new ConfigurationNode("road.width", this.ROAD_WIDTH, "Road width",
                ConfigurationUtil.INTEGER),
            new ConfigurationNode("road.height", this.ROAD_HEIGHT, "Road height",
                ConfigurationUtil.INTEGER),
            new ConfigurationNode("road.block", this.ROAD_BLOCK, "Road block",
                ConfigurationUtil.BLOCK_BUCKET),
            new ConfigurationNode("wall.filling", this.WALL_FILLING, "Wall filling block",
                ConfigurationUtil.BLOCK_BUCKET),
            new ConfigurationNode("wall.height", this.WALL_HEIGHT, "Wall height",
                ConfigurationUtil.INTEGER),
            new ConfigurationNode("plot.bedrock", this.PLOT_BEDROCK, "Plot bedrock generation",
                ConfigurationUtil.BOOLEAN)};
    }

    /**
     * This method is called when a world loads. Make sure you set all your constants here. You are provided with the
     * configuration section for that specific world.
     */
    @Override public void loadConfiguration(ConfigurationSection config) {
        super.loadConfiguration(config);
        this.PLOT_BEDROCK = config.getBoolean("plot.bedrock");
        this.PLOT_HEIGHT = Math.min(255, config.getInt("plot.height"));
        this.MAIN_BLOCK = new BlockBucket(config.getString("plot.filling"));
        this.TOP_BLOCK = new BlockBucket(config.getString("plot.floor"));
        this.WALL_BLOCK = new BlockBucket(config.getString("wall.block"));
        this.ROAD_HEIGHT = Math.min(255, config.getInt("road.height"));
        this.ROAD_BLOCK = new BlockBucket(config.getString("road.block"));
        this.WALL_FILLING = new BlockBucket(config.getString("wall.filling"));
        this.WALL_HEIGHT = Math.min(254, config.getInt("wall.height"));
        this.CLAIMED_WALL_BLOCK = new BlockBucket(config.getString("wall.block_claimed"));
    }
}
