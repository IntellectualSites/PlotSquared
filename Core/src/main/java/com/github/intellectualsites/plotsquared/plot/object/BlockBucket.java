package com.github.intellectualsites.plotsquared.plot.object;

import com.github.intellectualsites.plotsquared.configuration.serialization.ConfigurationSerializable;
import com.github.intellectualsites.plotsquared.plot.config.Configuration;
import com.github.intellectualsites.plotsquared.plot.util.MathMan;
import com.github.intellectualsites.plotsquared.plot.util.StringMan;
import com.github.intellectualsites.plotsquared.plot.util.world.BlockUtil;
import com.github.intellectualsites.plotsquared.plot.util.world.PatternUtil;
import com.google.common.collect.ImmutableMap;
import com.sk89q.worldedit.function.pattern.BlockPattern;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockType;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Map;
import java.util.regex.Matcher;

/**
 * A block bucket is a container of block types, where each block
 * has a specified chance of being randomly picked
 */
@EqualsAndHashCode(of = {"input"}) @SuppressWarnings({"unused", "WeakerAccess"})
public final class BlockBucket implements ConfigurationSerializable {
    private boolean compiled;

    private StringBuilder input;
    private BlockState single;
    private Pattern pattern;

    public BlockBucket(BlockType type) {
        this(type.getId());
        this.single = type.getDefaultState();
        this.pattern = new BlockPattern(this.single);
        this.compiled = true;
    }

    public BlockBucket(BlockState state) {
        this(state.getAsString());
        this.single = state;
        this.pattern = new BlockPattern(this.single);
        this.compiled = true;
    }

    public BlockBucket(String input) {
        this.input = new StringBuilder(input);
    }

    public BlockBucket() {
        this.input = new StringBuilder();
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
        if (chance == -1) chance = 1;
        String prefix = input.length() == 0 ? "" : ",";
        input.append(prefix).append(chance).append("%").append(prefix);
        this.compiled = false;
    }

    public boolean isEmpty() {
        return input == null || input.length() == 0;
    }

    private static java.util.regex.Pattern regex = java.util.regex.Pattern.compile("((?<namespace>[A-Za-z_]+):)?(?<block>([A-Za-z_]+(\\[?[\\S\\s]+\\])?))(:(?<chance>[0-9]{1,3}))?");

    public void compile() {
        if (isCompiled()) {
            return;
        }
        this.compiled = true;
        String string = this.input.toString();
        if (string.isEmpty()) {
            this.single = null;
            this.pattern = null;
            return;
        }
        // Convert legacy format
        boolean legacy = false;
        String[] blocksStr = string.split(",(?![^\\(\\[]*[\\]\\)])");
        if (blocksStr.length == 1) {
            try {
                Matcher matcher = regex.matcher(string);
                if (matcher.find()) {
                    String chanceStr = matcher.group("chance");
                    String block = matcher.group("block");
                    //noinspection PointlessNullCheck
                    if (chanceStr != null && block != null && !MathMan.isInteger(block) && MathMan.isInteger(chanceStr)) {
                        String namespace = matcher.group("namespace");
                        string = (namespace == null ? "" : namespace + ":") + block;
                    }
                }
                this.single = BlockUtil.get(string);
                this.pattern = new BlockPattern(single);
                return;
            } catch (Exception ignore) { }
        }
        for (int i = 0; i < blocksStr.length; i++) {
            String entry = blocksStr[i];
            Matcher matcher = regex.matcher(entry);
            if (matcher.find()) {
                String chanceStr = matcher.group("chance");
                //noinspection PointlessNullCheck
                if (chanceStr != null && MathMan.isInteger(chanceStr)) {
                    String[] parts = entry.split(":");
                    parts = Arrays.copyOf(parts, parts.length - 1);
                    entry = chanceStr + "%" + StringMan.join(parts, ":");
                    blocksStr[i] = entry;
                    legacy = true;
                }
            }
        }
        if (legacy) {
            string = StringMan.join(blocksStr, ",");
        }
        pattern = PatternUtil.parse(null, string);
    }

    public boolean isCompiled() {
        return this.compiled;
    }

    public Pattern toPattern() {
        this.compile();
        return this.pattern;
    }

    @Override public String toString() {
        return input.toString();
    }

    public boolean isAir() {
        compile();
        return isEmpty() || (single != null && single.getBlockType().getMaterial().isAir());
    }

    @Override public Map<String, Object> serialize() {
        if (!isCompiled()) {
            compile();
        }
        return ImmutableMap.of("blocks", this.toString());
    }

    @Getter @EqualsAndHashCode @RequiredArgsConstructor private static final class Range {

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
}
