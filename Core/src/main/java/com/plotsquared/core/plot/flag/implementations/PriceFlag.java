package com.plotsquared.core.plot.flag.implementations;

import com.plotsquared.core.config.Captions;
import com.plotsquared.core.plot.flag.types.DoubleFlag;
import org.jetbrains.annotations.NotNull;

public class PriceFlag extends DoubleFlag<PriceFlag> {
    public static final PriceFlag PRICE_NOT_BUYABLE = new PriceFlag(0D);

    protected PriceFlag(@NotNull Double value) {
        super(value, Double.MIN_NORMAL, Double.MAX_VALUE, Captions.FLAG_DESCRIPTION_PRICE);
    }

    @Override protected PriceFlag flagOf(@NotNull Double value) {
        return new PriceFlag(value);
    }
}
