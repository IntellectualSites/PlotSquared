package com.github.intellectualsites.plotsquared.plot.object;

import com.github.intellectualsites.plotsquared.configuration.serialization.ConfigurationSerializable;
import com.github.intellectualsites.plotsquared.plot.config.Configuration;
import com.github.intellectualsites.plotsquared.plot.object.collection.RandomCollection;
import com.google.common.collect.ImmutableMap;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockTypes;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

/**
 * A block bucket is a container of block types, where each block
 * has a specified chance of being randomly picked
 */
@EqualsAndHashCode(of={"blocks"}) @SuppressWarnings({"unused", "WeakerAccess"}) public final class BlockBucket
    implements Iterable<BlockState>, ConfigurationSerializable {

    private final Random random = new Random();
    private final Map<BlockState, Double> blocks;

    private final BucketIterator bucketIterator = new BucketIterator();
    private boolean compiled, singleItem;
    private BlockState head;

    private RandomCollection<BlockState> randomBlocks;
    private BlockState single;

    public BlockBucket() {
        this.blocks = new HashMap<>();
    }

    public static BlockBucket withSingle(@NonNull final BlockState block) {
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

    public void addBlock(@NonNull final BlockState block) {
        this.addBlock(block, -1);
    }

    public void addBlock(@NonNull final BlockState block, final int chance) {
        addBlock(block, (double) chance);
    }

    private void addBlock(@NonNull final BlockState block, double chance) {
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
    public Collection<BlockState> getBlocks() {
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
    public Collection<BlockState> getBlocks(final int count) {
        return Arrays.asList(getBlockArray(count));
    }

    /**
     * Get an array containing a specified amount of randomly selected blocks
     *
     * @param count Number of blocks
     * @return Immutable collection containing randomly selected blocks
     */
    public BlockState[] getBlockArray(final int count) {
        final BlockState[] blocks = new BlockState[count];
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

    @NotNull @Override public Iterator<BlockState> iterator() {
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
    public BlockState getBlock() {
        if (!isCompiled()) {
            this.compile();
        }
        if (single != null) {
            return single;
        }
        if (randomBlocks != null) {
            return randomBlocks.next();
        }
        return BlockTypes.AIR.getDefaultState();
    }

    @Override public String toString() {
        if (!isCompiled()) {
            compile();
        }
        if (blocks.size() == 1) {
            return blocks.entrySet().iterator().next().getKey().toString();
        }
        final StringBuilder builder = new StringBuilder();
        Iterator<Entry<BlockState, Double>> iterator = blocks.entrySet().iterator();
        while (iterator.hasNext()) {
            Entry<BlockState, Double> entry = iterator.next();
            BlockState block = entry.getKey();
            builder.append(block);
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

    public boolean isAir() {
        compile();
        return blocks.isEmpty() || (single != null && single.getBlockType().getMaterial().isAir());
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


    private final class BucketIterator implements Iterator<BlockState> {

        @Override public boolean hasNext() {
            return true;
        }

        @Override public BlockState next() {
            return getBlock();
        }
    }
}
