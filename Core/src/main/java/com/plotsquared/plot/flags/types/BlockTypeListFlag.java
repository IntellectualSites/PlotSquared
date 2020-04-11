package com.plotsquared.plot.flags.types;

import com.plotsquared.config.Caption;
import com.plotsquared.config.Captions;
import com.plotsquared.plot.flags.FlagParseException;
import com.plotsquared.util.BlockUtil;
import com.sk89q.worldedit.world.block.BlockCategory;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public abstract class BlockTypeListFlag<F extends ListFlag<BlockTypeWrapper, F>>
    extends ListFlag<BlockTypeWrapper, F> {

    protected BlockTypeListFlag(List<BlockTypeWrapper> blockTypeList, Caption description) {
        super(blockTypeList, Captions.FLAG_CATEGORY_BLOCK_LIST, description);
    }

    @Override public F parse(@NotNull String input) throws FlagParseException {
        final List<BlockTypeWrapper> parsedBlocks = new ArrayList<>();
        final String[] split = input.split(",(?![^\\(\\[]*[\\]\\)])");
        if (split.length == 0) {
            return this.flagOf(parsedBlocks);
        }
        for (final String blockString : split) {
            final BlockTypeWrapper blockTypeWrapper;
            final BlockState blockState = BlockUtil.get(blockString);
            if (blockState == null) {
                // If it's not a block state, we assume it's a block category
                final BlockCategory blockCategory;
                if (!blockString.startsWith("#") || (blockCategory = BlockCategory.REGISTRY.get(blockString.substring(1))) == null) {
                    throw new FlagParseException(this, blockString, Captions.FLAG_ERROR_INVALID_BLOCK);
                } else {
                    blockTypeWrapper = BlockTypeWrapper.get(blockCategory);
                }
            } else {
                blockTypeWrapper = BlockTypeWrapper.get(blockState.getBlockType());
            }
            if (!parsedBlocks.contains(blockTypeWrapper)) {
                parsedBlocks.add(blockTypeWrapper);
            }
        }
        return this.flagOf(parsedBlocks);
    }

    @Override public String getExample() {
        return "air,grass_block";
    }

    @Override public Collection<String> getTabCompletions() {
        final Collection<String> tabCompletions = new ArrayList<>();
        tabCompletions.addAll(BlockType.REGISTRY.keySet().stream().map(val -> val.replace("minecraft:", ""))
            .collect(Collectors.toList()));
        tabCompletions.addAll(BlockCategory.REGISTRY.keySet().stream().map(val -> "#" + val.replace("minecraft:", ""))
            .collect(Collectors.toList()));
        return tabCompletions;
    }

}
