package com.github.intellectualsites.plotsquared.plot.flags.types;

import com.google.common.base.Preconditions;
import com.sk89q.worldedit.world.block.BlockCategory;
import com.sk89q.worldedit.world.block.BlockType;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * Container that class either contains a {@link BlockType}
 * or a {@link BlockCategory}
 */
@EqualsAndHashCode public class BlockTypeWrapper {

    @Nullable @Getter private final BlockType blockType;
    @Nullable @Getter private final BlockCategory blockCategory;

    private BlockTypeWrapper(@NotNull final BlockType blockType) {
        this.blockType = Preconditions.checkNotNull(blockType);
        this.blockCategory = null;
    }

    private BlockTypeWrapper(@NotNull final BlockCategory blockCategory) {
        this.blockType = null;
        this.blockCategory = Preconditions.checkNotNull(blockCategory);
    }

    @Override public String toString() {
        if (this.blockType != null) {
            final String key = this.blockType.toString();
            if (key.startsWith("minecraft:")) {
                return key.substring(10);
            } else {
                return key;
            }
        } else if(this.blockCategory != null) {
            final String key = this.blockCategory.toString();
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

    private static final Map<BlockType, BlockTypeWrapper> blockTypes = new HashMap<>();
    private static final Map<BlockCategory, BlockTypeWrapper> blockCategories = new HashMap<>();

    public static BlockTypeWrapper get(final BlockType blockType) {
        return blockTypes.computeIfAbsent(blockType, BlockTypeWrapper::new);
    }

    public static BlockTypeWrapper get(final BlockCategory blockCategory) {
        return blockCategories.computeIfAbsent(blockCategory, BlockTypeWrapper::new);
    }

}
