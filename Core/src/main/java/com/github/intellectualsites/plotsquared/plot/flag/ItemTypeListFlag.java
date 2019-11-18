package com.github.intellectualsites.plotsquared.plot.flag;

import com.github.intellectualsites.plotsquared.plot.config.Captions;
import com.github.intellectualsites.plotsquared.plot.object.Plot;
import com.github.intellectualsites.plotsquared.plot.util.StringMan;
import com.github.intellectualsites.plotsquared.plot.util.world.BlockUtil;
import com.github.intellectualsites.plotsquared.plot.util.world.ItemUtil;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.item.ItemType;
import com.sk89q.worldedit.world.item.ItemTypes;

import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class ItemTypeListFlag extends ListFlag<Set<ItemType>> {

    public ItemTypeListFlag(String name) {
        super(Captions.FLAG_CATEGORY_BLOCK_LIST, name);
    }

    @Override public String valueToString(Set<ItemType> value) {
        return StringMan.join(value, ",");
    }

    @Override public Set<ItemType> parseValue(final String value) {
        return Arrays.stream(ItemUtil.parse(value)).filter(Objects::nonNull).collect(Collectors.toSet());
    }

    @Override public String getValueDescription() {
        return Captions.FLAG_ERROR_PLOTBLOCKLIST.getTranslated();
    }
}
