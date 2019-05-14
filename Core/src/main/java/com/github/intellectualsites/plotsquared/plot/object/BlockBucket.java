package com.github.intellectualsites.plotsquared.plot.object;

import com.github.intellectualsites.plotsquared.configuration.serialization.ConfigurationSerializable;
import com.github.intellectualsites.plotsquared.plot.config.Configuration;
import com.github.intellectualsites.plotsquared.plot.object.collection.RandomCollection;
import com.google.common.collect.ImmutableMap;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.*;
import java.util.Map.Entry;

/**
 * A block bucket is a container of block types, where each block
 * has a specified chance of being randomly picked
 */
@EqualsAndHashCode @SuppressWarnings({"unused", "WeakerAccess"}) public final class BlockBucket
    implements Iterable<PlotBlock>, ConfigurationSerializable {

    private final Random random = new Random();
    private final Map<PlotBlock, Double> blocks;

    private final BucketIterator bucketIterator = new BucketIterator();
    private boolean compiled, singleItem;
    private PlotBlock head;

    private RandomCollection<PlotBlock> randomBlocks;
    private PlotBlock single;

    public BlockBucket() {
        this.blocks = new HashMap<>();
    }

    public static BlockBucket withSingle(@NonNull final PlotBlock block) {
        final BlockBucket blockBucket = new BlockBucket();
        blockBucket.addBlock(block, 100);
        return blockBucket;
    }

    public static BlockBucket deserialize(@NonNull final Map<String, Object> map) {
        if (!map.containsKey("blocks")) {
            return null;
        }
        return Configuration.BLOCK_BUCKET.parseString(map.get("blocks").toString());
    }

    public void addBlock(@NonNull final PlotBlock block) {
        this.addBlock(block, -1);
    }

    public void addBlock(@NonNull final PlotBlock block, final int chance) {
        addBlock(block, (double) chance);
    }

    private void addBlock(@NonNull final PlotBlock block, double chance) {
        if (chance == -1)
            chance = 1;
        this.blocks.put(block, chance);
        this.compiled = false;
        if (head == null) {
            head = block;
        }
    }

    public boolean isEmpty() {
        return blocks.isEmpty();
    }

    /**
     * Get all blocks that are configured in the bucket
     *
     * @return Immutable collection containing all blocks that can
     * be found in the bucket
     */
    public Collection<PlotBlock> getBlocks() {
        if (!isCompiled()) {
            this.compile();
        }
        return Collections.unmodifiableCollection(this.blocks.keySet());
    }

    /**
     * Get a collection containing a specified amount of randomly selected blocks
     *
     * @param count Number of blocks
     * @return Immutable collection containing randomly selected blocks
     */
    public Collection<PlotBlock> getBlocks(final int count) {
        return Arrays.asList(getBlockArray(count));
    }

    /**
     * Get an array containing a specified amount of randomly selected blocks
     *
     * @param count Number of blocks
     * @return Immutable collection containing randomly selected blocks
     */
    public PlotBlock[] getBlockArray(final int count) {
        final PlotBlock[] blocks = new PlotBlock[count];
        if (this.singleItem) {
            Arrays.fill(blocks, 0, count, getBlock());
        } else {
            for (int i = 0; i < count; i++) {
                blocks[i] = getBlock();
            }
        }
        return blocks;
    }

    public boolean hasSingleItem() {
        return this.singleItem;
    }

    public void compile() {
        if (isCompiled()) {
            return;
        }

        this.compiled = true;
        switch (blocks.size()) {
            case 0:
                single = null;
                this.randomBlocks = null;
                break;
            case 1:
                single = blocks.keySet().iterator().next();
                this.randomBlocks = null;
                break;
            default:
                single = null;
                this.randomBlocks = RandomCollection.of(blocks, random);
                break;
        }
    }

    @Override public Iterator<PlotBlock> iterator() {
        return this.bucketIterator;
    }

    public boolean isCompiled() {
        return this.compiled;
    }

    /**
     * Get a random block out of the bucket
     *
     * @return Randomly picked block (cased on specified rates)
     */
    public PlotBlock getBlock() {
        if (!isCompiled()) {
            this.compile();
        }
        if (single != null) {
            return single;
        }
        if (randomBlocks != null) {
            return randomBlocks.next();
        }
        return StringPlotBlock.EVERYTHING;
    }

    @Override public String toString() {
        if (!isCompiled()) {
            compile();
        }
        final StringBuilder builder = new StringBuilder();
        Iterator<Entry<PlotBlock, Double>> iterator = blocks.entrySet().iterator();
        while (iterator.hasNext()) {
            Entry<PlotBlock, Double> entry = iterator.next();
            PlotBlock block = entry.getKey();
            builder.append(block.getRawId());
            Double weight = entry.getValue();
            if (weight != 1) {
                builder.append(":").append(weight.intValue());
            }
            if (iterator.hasNext()) {
                builder.append(",");
            }
        }
        return builder.toString();
    }

    @Override public Map<String, Object> serialize() {
        if (!isCompiled()) {
            compile();
        }
        return ImmutableMap.of("blocks", this.toString());
    }


    @Getter @EqualsAndHashCode @RequiredArgsConstructor private final static class Range {

        private final int min;
        private final int max;
        @Getter private final boolean automatic;

        public int getWeight() {
            return max - min;
        }

        public boolean isInRange(final int num) {
            return num <= max && num >= min;
        }
    }


    private final class BucketIterator implements Iterator<PlotBlock> {

        @Override public boolean hasNext() {
            return true;
        }

        @Override public PlotBlock next() {
            return getBlock();
        }
    }
}
