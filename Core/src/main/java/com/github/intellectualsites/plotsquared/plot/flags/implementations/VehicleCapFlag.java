package com.github.intellectualsites.plotsquared.plot.flags.implementations;

import com.github.intellectualsites.plotsquared.plot.config.Captions;
import com.github.intellectualsites.plotsquared.plot.flags.types.NonNegativeIntegerFlag;
import org.jetbrains.annotations.NotNull;

public class VehicleCapFlag extends NonNegativeIntegerFlag<VehicleCapFlag> {
    public static final VehicleCapFlag VEHICLE_CAP_UNLIMITED = new VehicleCapFlag(Integer.MAX_VALUE);

    protected VehicleCapFlag(int value) {
        super(value, Captions.FLAG_DESCRIPTION_VEHICLE_CAP);
    }

    @Override protected VehicleCapFlag flagOf(@NotNull Integer value) {
        return new VehicleCapFlag(value);
    }
}
