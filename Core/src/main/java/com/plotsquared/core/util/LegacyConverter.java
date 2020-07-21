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
package com.plotsquared.core.util;

import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.configuration.caption.CaptionUtility;
import com.plotsquared.core.configuration.Captions;
import com.plotsquared.core.configuration.ConfigurationSection;
import com.plotsquared.core.player.ConsolePlayer;
import com.plotsquared.core.plot.BlockBucket;
import com.sk89q.worldedit.world.block.BlockState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Converts legacy configurations into the new (BlockBucket) format
 */
@SuppressWarnings("unused")
public final class LegacyConverter {

    private static final Logger logger = LoggerFactory.getLogger("P2/" + LegacyConverter.class.getSimpleName());
    public static final String CONFIGURATION_VERSION = "post_flattening";
    private static final HashMap<String, ConfigurationType> TYPE_MAP = new HashMap<>();

    static {
        TYPE_MAP.put("plot.filling", ConfigurationType.BLOCK_LIST);
        TYPE_MAP.put("plot.floor", ConfigurationType.BLOCK_LIST);
        TYPE_MAP.put("wall.filling", ConfigurationType.BLOCK);
        TYPE_MAP.put("wall.block_claimed", ConfigurationType.BLOCK);
        TYPE_MAP.put("wall.block", ConfigurationType.BLOCK);
        TYPE_MAP.put("road.block", ConfigurationType.BLOCK);
    }

    private final ConfigurationSection configuration;

    public LegacyConverter(@Nonnull final ConfigurationSection configuration) {
        this.configuration = configuration;
    }

    private BlockBucket blockToBucket(@Nonnull final String block) {
        final BlockState plotBlock = PlotSquared.platform().getWorldUtil().getClosestBlock(block).best;
        return BlockBucket.withSingle(plotBlock);
    }

    private void setString(@Nonnull final ConfigurationSection section,
        @Nonnull final String string, @Nonnull final BlockBucket blocks) {
        if (!section.contains(string)) {
            throw new IllegalArgumentException(String.format("No such key: %s", string));
        }
        section.set(string, blocks.toString());
    }

    private BlockBucket blockListToBucket(@Nonnull final BlockState[] blocks) {
        final Map<BlockState, Integer> counts = new HashMap<>();
        for (final BlockState block : blocks) {
            counts.putIfAbsent(block, 0);
            counts.put(block, counts.get(block) + 1);
        }
        boolean includeRatios = false;
        for (final Integer integer : counts.values()) {
            if (integer > 1) {
                includeRatios = true;
                break;
            }
        }
        final BlockBucket bucket = new BlockBucket();
        if (includeRatios) {
            for (final Map.Entry<BlockState, Integer> count : counts.entrySet()) {
                bucket.addBlock(count.getKey(), count.getValue());
            }
        } else {
            counts.keySet().forEach(bucket::addBlock);
        }
        return bucket;
    }

    private BlockState[] splitBlockList(@Nonnull final List<String> list) {
        return list.stream().map(s -> PlotSquared.platform().getWorldUtil().getClosestBlock(s).best)
            .toArray(BlockState[]::new);
    }

    private void convertBlock(@Nonnull final ConfigurationSection section,
        @Nonnull final String key, @Nonnull final String block) {
        final BlockBucket bucket = this.blockToBucket(block);
        this.setString(section, key, bucket);
        logger.info(CaptionUtility
            .format(ConsolePlayer.getConsole(), Captions.LEGACY_CONFIG_REPLACED.getTranslated(),
                block, bucket.toString()));
    }

    private void convertBlockList(@Nonnull final ConfigurationSection section,
        @Nonnull final String key, @Nonnull final List<String> blockList) {
        final BlockState[] blocks = this.splitBlockList(blockList);
        final BlockBucket bucket = this.blockListToBucket(blocks);
        this.setString(section, key, bucket);
        logger.info(CaptionUtility
            .format(ConsolePlayer.getConsole(), Captions.LEGACY_CONFIG_REPLACED.getTranslated(),
                plotBlockArrayString(blocks), bucket.toString()));
    }

    private String plotBlockArrayString(@Nonnull final BlockState[] blocks) {
        final StringBuilder builder = new StringBuilder();
        for (int i = 0; i < blocks.length; i++) {
            builder.append(blocks[i].toString());
            if ((i + 1) < blocks.length) {
                builder.append(",");
            }
        }
        return builder.toString();
    }

    public void convert() {
        // Section is the "worlds" section
        final Collection<String> worlds = this.configuration.getKeys(false);
        for (final String world : worlds) {
            final ConfigurationSection worldSection = configuration.getConfigurationSection(world);
            for (final Map.Entry<String, ConfigurationType> entry : TYPE_MAP.entrySet()) {
                if (worldSection.contains(entry.getKey())) {
                    if (entry.getValue() == ConfigurationType.BLOCK) {
                        this.convertBlock(worldSection, entry.getKey(),
                            worldSection.getString(entry.getKey()));
                    } else {
                        this.convertBlockList(worldSection, entry.getKey(),
                            worldSection.getStringList(entry.getKey()));
                    }
                }
            }
        }
    }

    private enum ConfigurationType {
        BLOCK, BLOCK_LIST
    }

}
