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
package com.plotsquared.core.plot.flag.types;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.plotsquared.core.configuration.Settings;
import com.sk89q.worldedit.world.block.BlockCategory;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import com.sk89q.worldedit.world.block.BlockType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * Container that class either contains a {@link BlockType}
 * or a {@link BlockCategory}
 */
public class BlockTypeWrapper {

    private static final Logger LOGGER = LogManager.getLogger("PlotSquared/" + BlockTypeWrapper.class.getSimpleName());

    private static final Map<BlockType, BlockTypeWrapper> blockTypes = new HashMap<>();
    private static final Map<String, BlockTypeWrapper> blockCategories = new HashMap<>();
    private static final String minecraftNamespace = "minecraft";
    @Nullable
    private final BlockType blockType;
    @Nullable
    private final String blockCategoryId;
    @Nullable
    private BlockCategory blockCategory;

    private BlockTypeWrapper(final @NonNull BlockType blockType) {
        this.blockType = Preconditions.checkNotNull(blockType);
        this.blockCategory = null;
        this.blockCategoryId = null;
    }

    private BlockTypeWrapper(final @NonNull BlockCategory blockCategory) {
        this.blockType = null;
        this.blockCategory = Preconditions.checkNotNull(blockCategory);
        this.blockCategoryId = blockCategory.getId(); // used in toString()/equals()/hashCode()
    }

    private BlockTypeWrapper(final @NonNull String blockCategoryId) {
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
        // use minecraft as default namespace
        String id;
        if (blockCategoryId.indexOf(':') == -1) {
            id = minecraftNamespace + ":" + blockCategoryId;
        } else {
            id = blockCategoryId;
        }
        return blockCategories.computeIfAbsent(id, BlockTypeWrapper::new);
    }

    @Override
    public String toString() {
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
    public @Nullable BlockCategory getBlockCategory() {
        if (this.blockCategory == null
                && this.blockCategoryId != null) { // only if name is available
            this.blockCategory = BlockCategory.REGISTRY.get(this.blockCategoryId);
            if (this.blockCategory == null && !BlockCategory.REGISTRY.values().isEmpty()) {
                if (Settings.DEBUG) {
                    LOGGER.info("- Block category #{} does not exist", this.blockCategoryId);
                }
                this.blockCategory = new NullBlockCategory(this.blockCategoryId);
            }
        }
        return this.blockCategory;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BlockTypeWrapper that = (BlockTypeWrapper) o;
        return Objects.equal(this.blockType, that.blockType) && Objects
                .equal(this.blockCategoryId, that.blockCategoryId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.blockType, this.blockCategoryId);
    }

    public @Nullable BlockType getBlockType() {
        return this.blockType;
    }


    /**
     * Prevents exceptions when loading/saving block categories
     */
    private static class NullBlockCategory extends BlockCategory {

        public NullBlockCategory(String id) {
            super(id);
        }

        @Override
        public <B extends BlockStateHolder<B>> boolean contains(B blockStateHolder) {
            return false;
        }

    }

}
