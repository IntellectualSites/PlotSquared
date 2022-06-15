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

import com.plotsquared.core.configuration.caption.Caption;
import com.plotsquared.core.configuration.caption.TranslatableCaption;
import com.plotsquared.core.plot.flag.FlagParseException;
import com.plotsquared.core.util.BlockUtil;
import com.sk89q.worldedit.world.block.BlockCategory;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockType;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public abstract class BlockTypeListFlag<F extends ListFlag<BlockTypeWrapper, F>>
        extends ListFlag<BlockTypeWrapper, F> {

    public static boolean skipCategoryVerification = false;

    protected BlockTypeListFlag(List<BlockTypeWrapper> blockTypeList, Caption description) {
        super(blockTypeList, TranslatableCaption.of("flags.flag_category_block_list"), description);
    }

    @Override
    public F parse(@NonNull String input) throws FlagParseException {
        final List<BlockTypeWrapper> parsedBlocks = new ArrayList<>();
        final String[] split = input.replaceAll("\\s+", "").split(",(?![^\\(\\[]*[\\]\\)])");
        if (split.length == 0) {
            return this.flagOf(parsedBlocks);
        }
        for (final String blockString : split) {
            final BlockTypeWrapper blockTypeWrapper;
            final BlockState blockState = BlockUtil.get(blockString);
            if (blockState == null) {
                // If it's not a block state, we assume it's a block category
                blockTypeWrapper = getCategory(blockString);
            } else {
                blockTypeWrapper = BlockTypeWrapper.get(blockState.getBlockType());
            }
            if (!parsedBlocks.contains(blockTypeWrapper)) {
                parsedBlocks.add(blockTypeWrapper);
            }
        }
        return this.flagOf(parsedBlocks);
    }

    @Override
    public String getExample() {
        return "air,grass_block";
    }

    @Override
    public Collection<String> getTabCompletions() {
        final Collection<String> tabCompletions = new ArrayList<>();
        tabCompletions.addAll(
                BlockType.REGISTRY.keySet().stream().map(val -> val.replace("minecraft:", ""))
                        .collect(Collectors.toList()));
        tabCompletions.addAll(
                BlockCategory.REGISTRY.keySet().stream().map(val -> "#" + val.replace("minecraft:", ""))
                        .collect(Collectors.toList()));
        return tabCompletions;
    }

    private BlockTypeWrapper getCategory(final String blockString) throws FlagParseException {
        if (!blockString.startsWith("#")) {
            throw new FlagParseException(this, blockString, TranslatableCaption.of("flags.flag_error_invalid_block"));
        }
        String categoryId = blockString.substring(1);
        BlockTypeWrapper blockTypeWrapper;
        if (skipCategoryVerification) {
            blockTypeWrapper = BlockTypeWrapper.get(categoryId);
        } else {
            BlockCategory blockCategory = BlockCategory.REGISTRY.get(categoryId);
            if (blockCategory == null) {
                throw new FlagParseException(this, blockString, TranslatableCaption.of("flags.flag_error_invalid_block"));
            }
            blockTypeWrapper = BlockTypeWrapper.get(blockCategory);
        }
        return blockTypeWrapper;
    }

}
