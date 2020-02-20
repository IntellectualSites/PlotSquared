package com.github.intellectualsites.plotsquared.plot.util;

import com.github.intellectualsites.plotsquared.configuration.ConfigurationSection;
import com.github.intellectualsites.plotsquared.plot.PlotSquared;
import com.github.intellectualsites.plotsquared.plot.config.Captions;
import com.github.intellectualsites.plotsquared.plot.object.BlockBucket;
import com.github.intellectualsites.plotsquared.plot.object.ConsolePlayer;
import com.sk89q.worldedit.world.block.BlockState;
import lombok.NonNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Converts legacy configurations into the new (BlockBucket) format
 */
@SuppressWarnings("unused") public final class LegacyConverter {

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

    public LegacyConverter(@NonNull final ConfigurationSection configuration) {
        this.configuration = configuration;
    }

    private BlockBucket blockToBucket(@NonNull final String block) {
        final BlockState plotBlock = WorldUtil.IMP.getClosestBlock(block).best;
        return BlockBucket.withSingle(plotBlock);
    }

    private void setString(@NonNull final ConfigurationSection section,
        @NonNull final String string, @NonNull final BlockBucket blocks) {
        if (!section.contains(string)) {
            throw new IllegalArgumentException(String.format("No such key: %s", string));
        }
        section.set(string, blocks.toString());
    }

    private BlockBucket blockListToBucket(@NonNull final BlockState[] blocks) {
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

    private BlockState[] splitBlockList(@NonNull final List<String> list) {
        return list.stream().map(s -> WorldUtil.IMP.getClosestBlock(s).best)
            .toArray(BlockState[]::new);
    }

    private void convertBlock(@NonNull final ConfigurationSection section,
        @NonNull final String key, @NonNull final String block) {
        final BlockBucket bucket = this.blockToBucket(block);
        this.setString(section, key, bucket);
        PlotSquared.log(Captions
            .format(ConsolePlayer.getConsole(), Captions.LEGACY_CONFIG_REPLACED.getTranslated(), block, bucket.toString()));
    }

    private void convertBlockList(@NonNull final ConfigurationSection section,
        @NonNull final String key, @NonNull final List<String> blockList) {
        final BlockState[] blocks = this.splitBlockList(blockList);
        final BlockBucket bucket = this.blockListToBucket(blocks);
        this.setString(section, key, bucket);
        PlotSquared.log(Captions
            .format(ConsolePlayer.getConsole(), Captions.LEGACY_CONFIG_REPLACED.getTranslated(), plotBlockArrayString(blocks),
                bucket.toString()));
    }

    private String plotBlockArrayString(@NonNull final BlockState[] blocks) {
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
