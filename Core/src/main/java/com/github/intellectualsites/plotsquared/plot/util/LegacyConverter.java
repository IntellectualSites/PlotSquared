package com.github.intellectualsites.plotsquared.plot.util;

import com.github.intellectualsites.plotsquared.configuration.ConfigurationSection;
import com.github.intellectualsites.plotsquared.plot.object.BlockBucket;
import com.github.intellectualsites.plotsquared.plot.object.PlotBlock;
import lombok.NonNull;

import java.util.*;

/**
 * Converts legacy configurations into the new (BlockBucket) format
 */
@SuppressWarnings("unused")
public final class LegacyConverter {

    private enum ConfigurationType {
        BLOCK, BLOCK_LIST
    }

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

    public LegacyConverter(@NonNull final ConfigurationSection configuration) {
        this.configuration = configuration;
    }

    private BlockBucket blockToBucket(@NonNull final String block) {
        final PlotBlock plotBlock = WorldUtil.IMP.getClosestBlock(block).best;
        return BlockBucket.withSingle(plotBlock);
    }

    private void setString(@NonNull final ConfigurationSection section, @NonNull final String string, @NonNull final BlockBucket blocks) {
        if (!this.configuration.contains(string)) {
            throw new IllegalArgumentException(String.format("No such key: %s", string));
        }
        section.set(string, blocks.toString());
    }

    private BlockBucket blockListToBucket(@NonNull final PlotBlock[] blocks) {
        final Map<PlotBlock, Integer> counts = new HashMap<>();
        for (final PlotBlock block : blocks) {
            if (!counts.containsKey(block)) {
                counts.put(block, 0);
            }
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
            final double ratio = 100D / blocks.length;
            for (final Map.Entry<PlotBlock, Integer> count : counts.entrySet()) {
                bucket.addBlock(count.getKey(), (int) (count.getValue() * ratio));
            }
        } else {
            counts.keySet().forEach(bucket::addBlock);
        }
        return bucket;
    }

    private PlotBlock[] splitBlockList(@NonNull final List<String> list) {
        final PlotBlock[] entries = new PlotBlock[list.size()];
        for (int i = 0; i < list.size(); i++) {
            entries[i] = WorldUtil.IMP.getClosestBlock(list.get(i)).best;
        }
        return entries;
    }

    private void convertBlock(@NonNull final ConfigurationSection section, @NonNull final String key,
        @NonNull final String block) {
        final BlockBucket bucket = this.blockToBucket(block);
        this.setString(section, key, bucket);
    }

    private void convertBlockList(@NonNull final ConfigurationSection section, @NonNull final String key,
        @NonNull final List<String> blockList) {
        final PlotBlock[] blocks = this.splitBlockList(blockList);
        final BlockBucket bucket = this.blockListToBucket(blocks);
        this.setString(section, key, bucket);
    }

    public void convert() {
        // Section is the "worlds" section
        final Collection<String> worlds = this.configuration.getKeys(false);
        for (final String world : worlds) {
            final ConfigurationSection worldSection = configuration.getConfigurationSection(world);
            for (final Map.Entry<String, ConfigurationType> entry : TYPE_MAP.entrySet()) {
                 if (entry.getValue() == ConfigurationType.BLOCK) {
                     this.convertBlock(worldSection, entry.getKey(), worldSection.getString(entry.getKey()));
                 } else {
                     this.convertBlockList(worldSection, entry.getKey(), worldSection.getStringList(entry.getKey()));
                 }
            }
        }
    }

}
