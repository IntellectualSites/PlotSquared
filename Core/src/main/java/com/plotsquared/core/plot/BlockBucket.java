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
package com.plotsquared.core.plot;

import com.google.common.collect.ImmutableMap;
import com.plotsquared.core.configuration.ConfigurationUtil;
import com.plotsquared.core.configuration.serialization.ConfigurationSerializable;
import com.plotsquared.core.util.BlockUtil;
import com.plotsquared.core.util.MathMan;
import com.plotsquared.core.util.PatternUtil;
import com.plotsquared.core.util.StringMan;
import com.sk89q.worldedit.function.pattern.BlockPattern;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockType;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;

/**
 * A block bucket is a container of block types, where each block
 * has a specified chance of being randomly picked
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public final class BlockBucket implements ConfigurationSerializable {

    private static final java.util.regex.Pattern regex = java.util.regex.Pattern.compile(
            "((?<namespace>[A-Za-z_]+):)?(?<block>([A-Za-z_]+(\\[?[\\S\\s]+\\])?))(:(?<chance>[0-9]{1,3}))?");
    private final StringBuilder input;
    private boolean compiled;
    private BlockState single;
    private Pattern pattern;

    public BlockBucket(final @NonNull BlockType type) {
        this(type.getId());
        this.single = type.getDefaultState();
        this.pattern = new BlockPattern(this.single);
        this.compiled = true;
    }

    public BlockBucket(final @NonNull BlockState state) {
        this(state.getAsString());
        this.single = state;
        this.pattern = new BlockPattern(this.single);
        this.compiled = true;
    }

    public BlockBucket(final @NonNull String input) {
        this.input = new StringBuilder(input);
    }

    public BlockBucket() {
        this.input = new StringBuilder();
    }

    public static BlockBucket withSingle(final @NonNull BlockState block) {
        final BlockBucket blockBucket = new BlockBucket();
        blockBucket.addBlock(block, 100);
        return blockBucket;
    }

    public static BlockBucket deserialize(final @NonNull Map<String, Object> map) {
        if (!map.containsKey("blocks")) {
            return null;
        }
        return ConfigurationUtil.BLOCK_BUCKET.parseString(map.get("blocks").toString());
    }

    public void addBlock(final @NonNull BlockState block) {
        this.addBlock(block, -1);
    }

    public void addBlock(final @NonNull BlockState block, final int chance) {
        addBlock(block, (double) chance);
    }

    private void addBlock(final @NonNull BlockState block, double chance) {
        if (chance == -1) {
            chance = 1;
        }
        String prefix = input.length() == 0 ? "" : ",";
        input.append(prefix).append(block).append(":").append(chance);
        this.compiled = false;
    }

    public boolean isEmpty() {
        return input == null || input.length() == 0;
    }

    public void compile() {
        if (isCompiled()) {
            return;
        }
        // Synchronized as BlockBuckets may require compilation asynchronously due to async chunk generation on Paper servers
        synchronized (this) {
            if (isCompiled()) {
                return;
            }
            String string = this.input.toString();
            if (string.isEmpty()) {
                this.single = null;
                this.pattern = null;
                this.compiled = true;
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
                        if (chanceStr != null && block != null && !MathMan.isInteger(block) && MathMan
                                .isInteger(chanceStr)) {
                            String namespace = matcher.group("namespace");
                            string = (namespace == null ? "" : namespace + ":") + block;
                        }
                    }
                    this.single = BlockUtil.get(string);
                    this.pattern = new BlockPattern(single);
                    this.compiled = true;
                    return;
                } catch (Exception ignore) {
                }
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
            this.compiled = true;
        }
    }

    public boolean isCompiled() {
        return this.compiled;
    }

    public Pattern toPattern() {
        this.compile();
        return this.pattern;
    }

    @Override
    public String toString() {
        return input.toString();
    }

    public boolean isAir() {
        compile();
        return isEmpty() || (single != null && single.getBlockType().getMaterial().isAir());
    }

    @Override
    public Map<String, Object> serialize() {
        if (!isCompiled()) {
            compile();
        }
        return ImmutableMap.of("blocks", this.toString());
    }

    public boolean equals(final Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof final BlockBucket other)) {
            return false;
        }
        final Object this$input = this.input;
        final Object other$input = other.input;
        return Objects.equals(this$input, other$input);
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $input = this.input;
        result = result * PRIME + ($input == null ? 43 : $input.hashCode());
        return result;
    }

    private record Range(
            int min,
            int max,
            boolean automatic
    ) {

        public int getWeight() {
            return max - min;
        }

        public boolean isInRange(final int num) {
            return num <= max && num >= min;
        }

    }

}
