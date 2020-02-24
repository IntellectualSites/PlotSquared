package com.github.intellectualsites.plotsquared.plot.flags.implementations;

import com.github.intellectualsites.plotsquared.plot.config.Captions;
import com.github.intellectualsites.plotsquared.plot.flags.types.BooleanFlag;
import org.jetbrains.annotations.NotNull;

public class VehiclePlaceFlag extends BooleanFlag<VehiclePlaceFlag> {

    public static final VehiclePlaceFlag VEHICLE_PLACE_TRUE = new VehiclePlaceFlag(true);
    public static final VehiclePlaceFlag VEHICLE_PLACE_FALSE = new VehiclePlaceFlag(false);

    private VehiclePlaceFlag(boolean value) {
        super(value, Captions.FLAG_DESCRIPTION_VEHICLE_PLACE);
    }

    @Override protected VehiclePlaceFlag flagOf(@NotNull Boolean value) {
        return value ? VEHICLE_PLACE_TRUE : VEHICLE_PLACE_FALSE;
    }

}
