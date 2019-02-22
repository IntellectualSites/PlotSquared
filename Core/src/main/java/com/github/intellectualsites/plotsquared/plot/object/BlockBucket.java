package com.github.intellectualsites.plotsquared.plot.object;

import com.github.intellectualsites.plotsquared.configuration.serialization.ConfigurationSerializable;
import com.github.intellectualsites.plotsquared.plot.config.Captions;
import com.github.intellectualsites.plotsquared.plot.config.Configuration;
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
    private final Map<Range, PlotBlock> ranges = new HashMap<>();
    private final Map<PlotBlock, Integer> blocks;
    private final BucketIterator bucketIterator = new BucketIterator();
    private boolean compiled, singleItem;
    private PlotBlock head;

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
        this.blocks.put(block, chance);
        this.compiled = false;
        if (head == null) {
            head = block;
        }
    }

    public boolean isEmpty() {
        if (isCompiled()) {
            return ranges.isEmpty();
        }
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
        return Collections.unmodifiableCollection(this.ranges.values());
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

        if (blocks.size() == 0) {
            this.compiled = true;
            return;
        }

        if (blocks.size() == 1) {
            this.ranges.put(new Range(0, 100, true), blocks.keySet().toArray(new PlotBlock[1])[0]);
            this.compiled = true;
            this.singleItem = true;
            return;
        }

        final Map<PlotBlock, Integer> temp = new HashMap<>(blocks.size());
        final List<PlotBlock> unassigned = new ArrayList<>(blocks.size());

        int sum = 0;
        for (final Map.Entry<PlotBlock, Integer> entry : blocks.entrySet()) {
            if (entry.getValue() == -1) {
                unassigned.add(entry.getKey());
            } else {
                sum += entry.getValue();
                temp.put(entry.getKey(), entry.getValue());
            }
        }
        //
        // If this doesn't amount to 100 add it up to exactly 100.
        //
        if (sum < 100) {
            final int remaining = 100 - sum;
            if (unassigned.isEmpty()) {
                // If there are no unassigned values, we just add it to the first value
                final Entry<PlotBlock, Integer> entry = temp.entrySet().iterator().next();
                temp.put(entry.getKey(), (entry.getValue() + 1 + remaining));
            } else {
                final int perUnassigned = remaining / unassigned.size();
                for (final PlotBlock block : unassigned) {
                    temp.put(block, perUnassigned);
                    sum += perUnassigned;
                }
                // Make sure there isn't a tiny difference remaining
                if (sum < 100) {
                    final int difference = 100 - sum;
                    temp.put(unassigned.get(0), perUnassigned + difference);
                    sum = 100;
                }
            }
        } else if (!unassigned.isEmpty()) {
            Captions.BUCKET_ENTRIES_IGNORED.send(ConsolePlayer.getConsole());
        }
        //
        // If the sum adds up to more than 100, divide all values
        //
        if (sum > 100) {
            final double ratio = 100D / sum;
            for (final Map.Entry<PlotBlock, Integer> entry : blocks.entrySet()) {
                if (entry.getValue() == -1) {
                    continue;
                }
                temp.put(entry.getKey(), (int) (entry.getValue() * ratio));
            }
        } else {
            temp.forEach(temp::put);
        }
        int start = 0;
        for (final Map.Entry<PlotBlock, Integer> entry : temp.entrySet()) {
            final int rangeStart = start;
            final int rangeEnd = rangeStart + entry.getValue();
            start = rangeEnd + 1;
            final Range range =
                new Range(rangeStart, rangeEnd, unassigned.contains(entry.getKey()));
            this.ranges.put(range, entry.getKey());
        }
        this.blocks.clear();
        this.compiled = true;
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
        if (this.isEmpty()) {
            return StringPlotBlock.EVERYTHING;
        } else if (this.hasSingleItem()) {
            return this.head;
        }
        final int number = random.nextInt(101);
        for (final Map.Entry<Range, PlotBlock> entry : ranges.entrySet()) {
            if (entry.getKey().isInRange(number)) {
                return entry.getValue();
            }
        }
        // Didn't find a block? Try again
        return getBlock();
    }

    @Override public String toString() {
        if (!isCompiled()) {
            compile();
        }
        final StringBuilder builder = new StringBuilder();
        final Iterator<Entry<Range, PlotBlock>> iterator = this.ranges.entrySet().iterator();
        while (iterator.hasNext()) {
            final Entry<Range, PlotBlock> entry = iterator.next();
            builder.append(entry.getValue().getRawId());
            if (!entry.getKey().isAutomatic()) {
                builder.append(":").append(entry.getKey().getWeight());
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
