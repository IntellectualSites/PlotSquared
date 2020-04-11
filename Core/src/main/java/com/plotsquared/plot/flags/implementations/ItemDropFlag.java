package com.plotsquared.plot.flags.implementations;

import com.plotsquared.config.Captions;
import com.plotsquared.plot.flags.types.BooleanFlag;
import org.jetbrains.annotations.NotNull;

public class ItemDropFlag extends BooleanFlag<ItemDropFlag> {

    public static final ItemDropFlag ITEM_DROP_TRUE = new ItemDropFlag(true);
    public static final ItemDropFlag ITEM_DROP_FALSE = new ItemDropFlag(false);

    private ItemDropFlag(boolean value) {
        super(value, Captions.FLAG_DESCRIPTION_ITEM_DROP);
    }

    @Override protected ItemDropFlag flagOf(@NotNull Boolean value) {
        return value ? ITEM_DROP_TRUE : ITEM_DROP_FALSE;
    }

}
