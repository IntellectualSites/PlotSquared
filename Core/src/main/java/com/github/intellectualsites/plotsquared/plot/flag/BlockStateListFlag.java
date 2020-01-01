package com.github.intellectualsites.plotsquared.plot.flag;

import com.github.intellectualsites.plotsquared.plot.config.Captions;
import com.github.intellectualsites.plotsquared.plot.object.Plot;
import com.github.intellectualsites.plotsquared.plot.util.StringMan;
import com.github.intellectualsites.plotsquared.plot.util.world.BlockUtil;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockType;

import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class BlockStateListFlag extends ListFlag<Set<BlockType>> {

    public BlockStateListFlag(String name) {
        super(Captions.FLAG_CATEGORY_BLOCK_LIST, name);
    }

    @Override public String valueToString(Object value) {
        return StringMan.join((Set<BlockType>) value, ",");
    }

    @Override public Set<BlockType> parseValue(final String value) {
        return Arrays.stream(BlockUtil.parse(value)).filter(Objects::nonNull).map(BlockState::getBlockType).collect(Collectors.toSet());
    }

    @Override public String getValueDescription() {
        return Captions.FLAG_ERROR_PLOTBLOCKLIST.getTranslated();
    }

    public boolean contains(Plot plot, BlockState value) {
        return contains(plot, value.getBlockType());
    }
}
