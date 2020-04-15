package com.plotsquared.core.plot.flag.implementations;

import com.plotsquared.core.config.Captions;
import com.plotsquared.core.plot.flag.types.BooleanFlag;
import org.jetbrains.annotations.NotNull;

public class VehicleBreakFlag extends BooleanFlag<VehicleBreakFlag> {

    public static final VehicleBreakFlag VEHICLE_BREAK_TRUE = new VehicleBreakFlag(true);
    public static final VehicleBreakFlag VEHICLE_BREAK_FALSE = new VehicleBreakFlag(false);

    private VehicleBreakFlag(boolean value) {
        super(value, Captions.FLAG_DESCRIPTION_VEHICLE_BREAK);
    }

    @Override protected VehicleBreakFlag flagOf(@NotNull Boolean value) {
        return value ? VEHICLE_BREAK_TRUE : VEHICLE_BREAK_FALSE;
    }

}
