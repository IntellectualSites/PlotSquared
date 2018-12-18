package com.github.intellectualsites.plotsquared.plot.object;

import com.github.intellectualsites.plotsquared.plot.config.C;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * A block bucket is a container of block types, where each block
 * has a specified chance of being randomly picked
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public final class BlockBucket implements Iterable<PlotBlock> {

  private final Random random = new Random();
  private final Map<Range, PlotBlock> ranges = new HashMap<>();
  private final Map<PlotBlock, Integer> blocks;
  private final BucketIterator bucketIterator = new BucketIterator();
  private boolean compiled;

  public BlockBucket() {
    this.blocks = new HashMap<>();
  }

  public void addBlock(@NonNull final PlotBlock block) {
    this.addBlock(block, -1);
  }

  public void addBlock(@NonNull final PlotBlock block, final int chance) {
    this.blocks.put(block, chance);
    this.compiled = false;
  }

  public void compile() {
    if (isCompiled()) {
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
      }
    }
    //
    // If this doesn't amount to 100 add it up to exactly 100.
    //
    if (sum < 100) {
      final int remaining = 100 - sum;
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
    } else if (!unassigned.isEmpty()) {
      C.BUCKET_ENTRIES_IGNORED.send(ConsolePlayer.getConsole());
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
        temp.put(entry.getKey(), (int)(entry.getValue() * ratio));
      }
    } else {
      temp.forEach(temp::put);
    }
    int start = 0;
    for (final Map.Entry<PlotBlock, Integer> entry : temp.entrySet()) {
      final int rangeStart = start;
      final int rangeEnd = rangeStart + entry.getValue();
      start = rangeEnd + 1;
      final Range range = new Range(rangeStart, rangeEnd);
      this.ranges.put(range, entry.getKey());
    }
    this.blocks.clear();
    this.compiled = true;
  }

  @Override
  public Iterator<PlotBlock> iterator() {
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
    final int number = random.nextInt(101);
    for (final Map.Entry<Range, PlotBlock> entry : ranges.entrySet()) {
      if (entry.getKey().isInRange(number)) {
        return entry.getValue();
      }
    }
    // Didn't find a block? Try again
    return getBlock();
  }

  private final class BucketIterator implements Iterator<PlotBlock> {

    @Override
    public boolean hasNext() {
      return true;
    }

    @Override
    public PlotBlock next() {
      return getBlock();
    }
  }

  @Getter
  @EqualsAndHashCode
  @RequiredArgsConstructor
  private final static class Range {

    private final int min;
    private final int max;

    public boolean isInRange(final int num) {
      return num <= max && num >= min;
    }
  }
}
