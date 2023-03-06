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
package com.plotsquared.core.util;

import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.configuration.ConfigurationSection;
import com.plotsquared.core.configuration.caption.TranslatableCaption;
import com.plotsquared.core.player.ConsolePlayer;
import com.plotsquared.core.plot.BlockBucket;
import com.sk89q.worldedit.world.block.BlockState;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Converts legacy configurations into the new (BlockBucket) format
 */
@SuppressWarnings("unused")
public final class LegacyConverter {

    public static final String CONFIGURATION_VERSION = "post_flattening";
    private static final Logger LOGGER = LogManager.getLogger("PlotSquared/" + LegacyConverter.class.getSimpleName());
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

    public LegacyConverter(final @NonNull ConfigurationSection configuration) {
        this.configuration = configuration;
    }

    private BlockBucket blockToBucket(final @NonNull String block) {
        final BlockState plotBlock = PlotSquared.platform().worldUtil().getClosestBlock(block).best;
        return BlockBucket.withSingle(plotBlock);
    }

    private void setString(
            final @NonNull ConfigurationSection section,
            final @NonNull String string, final @NonNull BlockBucket blocks
    ) {
        if (!section.contains(string)) {
            throw new IllegalArgumentException(String.format("No such key: %s", string));
        }
        section.set(string, blocks.toString());
    }

    private BlockBucket blockListToBucket(final @NonNull BlockState[] blocks) {
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

    private BlockState[] splitBlockList(final @NonNull List<String> list) {
        return list.stream().map(s -> PlotSquared.platform().worldUtil().getClosestBlock(s).best)
                .toArray(BlockState[]::new);
    }

    private void convertBlock(
            final @NonNull ConfigurationSection section,
            final @NonNull String key,
            final @NonNull String block
    ) {
        final BlockBucket bucket = this.blockToBucket(block);
        this.setString(section, key, bucket);
        ConsolePlayer.getConsole().sendMessage(
                TranslatableCaption.of("legacyconfig.legacy_config_replaced"),
                TagResolver.builder()
                        .tag("value1", Tag.inserting(Component.text(block)))
                        .tag("value2", Tag.inserting(Component.text(bucket.toString())))
                        .build()
        );
    }

    private void convertBlockList(
            final @NonNull ConfigurationSection section,
            final @NonNull String key,
            final @NonNull List<String> blockList
    ) {
        final BlockState[] blocks = this.splitBlockList(blockList);
        final BlockBucket bucket = this.blockListToBucket(blocks);
        this.setString(section, key, bucket);
        ConsolePlayer.getConsole()
                .sendMessage(
                        TranslatableCaption.of("legacyconfig.legacy_config_replaced"),
                        TagResolver.builder()
                                .tag("value1", Tag.inserting(Component.text(plotBlockArrayString(blocks))))
                                .tag("value2", Tag.inserting(Component.text(bucket.toString())))
                                .build()
                );
    }

    private String plotBlockArrayString(final @NonNull BlockState[] blocks) {
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
                                worldSection.getString(entry.getKey())
                        );
                    } else {
                        this.convertBlockList(worldSection, entry.getKey(),
                                worldSection.getStringList(entry.getKey())
                        );
                    }
                }
            }
        }
    }

    private enum ConfigurationType {
        BLOCK,
        BLOCK_LIST
    }

}
