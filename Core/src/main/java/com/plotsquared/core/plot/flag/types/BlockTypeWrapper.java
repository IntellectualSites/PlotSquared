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
package com.plotsquared.core.plot.flag.types;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.plotsquared.core.PlotSquared;
import com.sk89q.worldedit.world.block.BlockCategory;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import com.sk89q.worldedit.world.block.BlockType;
import lombok.Getter;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * Container that class either contains a {@link BlockType}
 * or a {@link BlockCategory}
 */
public class BlockTypeWrapper {

    private static final Map<BlockType, BlockTypeWrapper> blockTypes = new HashMap<>();
    private static final Map<String, BlockTypeWrapper> blockCategories = new HashMap<>();
    @Nullable @Getter private final BlockType blockType;
    @Nullable private final String blockCategoryId;
    @Nullable private BlockCategory blockCategory;

    private BlockTypeWrapper(@Nonnull final BlockType blockType) {
        this.blockType = Preconditions.checkNotNull(blockType);
        this.blockCategory = null;
        this.blockCategoryId = null;
    }

    private BlockTypeWrapper(@Nonnull final BlockCategory blockCategory) {
        this.blockType = null;
        this.blockCategory = Preconditions.checkNotNull(blockCategory);
        this.blockCategoryId = blockCategory.getId(); // used in toString()/equals()/hashCode()
    }

    private BlockTypeWrapper(@Nonnull final String blockCategoryId) {
        this.blockType = null;
        this.blockCategory = null;
        this.blockCategoryId = Preconditions.checkNotNull(blockCategoryId);
    }

    public static BlockTypeWrapper get(final BlockType blockType) {
        return blockTypes.computeIfAbsent(blockType, BlockTypeWrapper::new);
    }

    public static BlockTypeWrapper get(final BlockCategory blockCategory) {
        return blockCategories
            .computeIfAbsent(blockCategory.getId(), id -> new BlockTypeWrapper(blockCategory));
    }

    public static BlockTypeWrapper get(final String blockCategoryId) {
        return blockCategories.computeIfAbsent(blockCategoryId, BlockTypeWrapper::new);
    }

    @Override public String toString() {
        if (this.blockType != null) {
            final String key = this.blockType.toString();
            if (key.startsWith("minecraft:")) {
                return key.substring(10);
            } else {
                return key;
            }
        } else if (this.blockCategoryId != null) {
            final String key = this.blockCategoryId;
            if (key.startsWith("minecraft:")) {
                return '#' + key.substring(10);
            } else {
                return '#' + key;
            }
        } else {
            return null;
        }
    }

    public boolean accepts(final BlockType blockType) {
        if (this.getBlockType() != null) {
            return this.getBlockType().equals(blockType);
        } else if (this.getBlockCategory() != null) {
            return this.getBlockCategory().contains(blockType);
        } else {
            return false;
        }
    }

    /**
     * Returns the block category associated with this wrapper.
     * <br>
     * Invocation will try to lazily initialize the block category if it's not
     * set yet but the category id is present. If {@link BlockCategory#REGISTRY} is already populated
     * but does not contain a category with the given name, a BlockCategory containing no items
     * is returned.
     * If this wrapper does not wrap a BlockCategory, null is returned.
     * <br>
     * <b>If {@link BlockCategory#REGISTRY} isn't populated yet, null is returned.</b>
     *
     * @return the block category represented by this wrapper.
     */
    @Nullable public BlockCategory getBlockCategory() {
        if (this.blockCategory == null
            && this.blockCategoryId != null) { // only if name is available
            this.blockCategory = BlockCategory.REGISTRY.get(this.blockCategoryId);
            if (this.blockCategory == null && !BlockCategory.REGISTRY.values().isEmpty()) {
                PlotSquared.debug("- Block category #" + this.blockCategoryId + " does not exist");
                this.blockCategory = new NullBlockCategory(this.blockCategoryId);
            }
        }
        return this.blockCategory;
    }

    @Override public boolean equals(final Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        BlockTypeWrapper that = (BlockTypeWrapper) o;
        return Objects.equal(this.blockType, that.blockType) && Objects
            .equal(this.blockCategoryId, that.blockCategoryId);
    }

    @Override public int hashCode() {
        return Objects.hashCode(this.blockType, this.blockCategoryId);
    }


    /**
     * Prevents exceptions when loading/saving block categories
     */
    private static class NullBlockCategory extends BlockCategory {

        public NullBlockCategory(String id) {
            super(id);
        }

        @Override public <B extends BlockStateHolder<B>> boolean contains(B blockStateHolder) {
            return false;
        }
    }

}
