package com.plotsquared.core.plot.flag.implementations;

import com.plotsquared.core.config.Captions;
import com.plotsquared.core.plot.flag.types.NonNegativeIntegerFlag;
import org.jetbrains.annotations.NotNull;

public class VehicleCapFlag extends NonNegativeIntegerFlag<VehicleCapFlag> {
    public static final VehicleCapFlag VEHICLE_CAP_UNLIMITED =
        new VehicleCapFlag(Integer.MAX_VALUE);

    protected VehicleCapFlag(int value) {
        super(value, Captions.FLAG_DESCRIPTION_VEHICLE_CAP);
    }

    @Override protected VehicleCapFlag flagOf(@NotNull Integer value) {
        return new VehicleCapFlag(value);
    }
}
