package com.plotsquared.plot.flags.implementations;

import com.plotsquared.config.Captions;
import com.plotsquared.plot.flags.types.BooleanFlag;
import org.jetbrains.annotations.NotNull;

public class VehicleUseFlag extends BooleanFlag<VehicleUseFlag> {

    public static final VehicleUseFlag VEHICLE_USE_TRUE = new VehicleUseFlag(true);
    public static final VehicleUseFlag VEHICLE_USE_FALSE = new VehicleUseFlag(false);

    private VehicleUseFlag(boolean value) {
        super(value, Captions.FLAG_DESCRIPTION_VEHICLE_USE);
    }

    @Override protected VehicleUseFlag flagOf(@NotNull Boolean value) {
        return value ? VEHICLE_USE_TRUE : VEHICLE_USE_FALSE;
    }

}
