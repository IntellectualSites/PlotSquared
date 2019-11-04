package com.github.intellectualsites.plotsquared.plot.flag;

import com.github.intellectualsites.plotsquared.plot.object.Plot;
import com.github.intellectualsites.plotsquared.plot.util.block.BlockUtil;

import com.github.intellectualsites.plotsquared.plot.config.Captions;
import com.sk89q.worldedit.world.block.BlockState;
import com.github.intellectualsites.plotsquared.plot.util.StringMan;
import com.sk89q.worldedit.world.block.BlockType;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class BlockStateListFlag extends ListFlag<Set<BlockType>> {

    public BlockStateListFlag(String name) {
        super(Captions.FLAG_CATEGORY_BLOCK_LIST, name);
    }

    @Override public String valueToString(Object value) {
        return StringMan.join((Set<BlockType>) value, ",");
    }

    @Override public Set<BlockType> parseValue(final String value) {
        return Arrays.stream(BlockUtil.parse(value)).map(BlockState::getBlockType).collect(Collectors.toSet());
    }

    @Override public String getValueDescription() {
        return "Flag value must be a block list";
    }

    public boolean contains(Plot plot, BlockState value) {
        return contains(plot, value.getBlockType());
    }
}
