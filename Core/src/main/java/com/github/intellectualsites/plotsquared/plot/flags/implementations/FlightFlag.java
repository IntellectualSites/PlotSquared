package com.github.intellectualsites.plotsquared.plot.flags.implementations;

import com.github.intellectualsites.plotsquared.plot.config.Captions;
import com.github.intellectualsites.plotsquared.plot.flags.types.BooleanFlag;
import org.jetbrains.annotations.NotNull;

public class FlightFlag extends BooleanFlag<FlightFlag> {

    public static final FlightFlag FLIGHT_FLAG_FALSE = new FlightFlag(false);

    protected FlightFlag(final boolean value) {
        super(value, Captions.FLAG_DESCRIPTION_FLIGHT);
    }

    @Override protected FlightFlag flagOf(@NotNull Boolean value) {
        return new FlightFlag(value);
    }

}
