package com.plotsquared.core.plot.flag.implementations;

import com.plotsquared.core.config.Captions;
import com.plotsquared.core.plot.flag.types.BooleanFlag;
import org.jetbrains.annotations.NotNull;

public class LiquidFlowFlag extends BooleanFlag<LiquidFlowFlag> {

    public static final LiquidFlowFlag LIQUID_FLOW_TRUE = new LiquidFlowFlag(true);
    public static final LiquidFlowFlag LIQUID_FLOW_FALSE = new LiquidFlowFlag(false);

    private LiquidFlowFlag(boolean value) {
        super(value, Captions.FLAG_DESCRIPTION_LIQUID_FLOW);
    }

    @Override protected LiquidFlowFlag flagOf(@NotNull Boolean value) {
        return value ? LIQUID_FLOW_TRUE : LIQUID_FLOW_FALSE;
    }

}
