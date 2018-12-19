package com.github.intellectualsites.plotsquared.plot.util;

import com.github.intellectualsites.plotsquared.plot.object.PlotBlock;

public abstract class LegacyMappings {

    public abstract PlotBlock fromAny(final String string);

    public abstract PlotBlock fromLegacyToString(final int id);

    public abstract PlotBlock fromLegacyToString(final int id, final int data);

    public abstract PlotBlock fromLegacyToString(final String id);

    public abstract PlotBlock fromStringToLegacy(final String id);

}
