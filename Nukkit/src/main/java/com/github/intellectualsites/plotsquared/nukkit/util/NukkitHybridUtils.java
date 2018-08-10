package com.github.intellectualsites.plotsquared.nukkit.util;

import com.github.intellectualsites.plotsquared.plot.PS;
import com.github.intellectualsites.plotsquared.plot.generator.HybridUtils;
import com.github.intellectualsites.plotsquared.plot.object.RegionWrapper;
import com.github.intellectualsites.plotsquared.plot.object.RunnableVal;
import com.github.intellectualsites.plotsquared.plot.util.expiry.PlotAnalysis;

public class NukkitHybridUtils extends HybridUtils {

    public NukkitHybridUtils() {
        PS.debug("Not implemented: NukkitHybridUtils");
    }

    @Override public void analyzeRegion(final String world, final RegionWrapper region,
        final RunnableVal<PlotAnalysis> whenDone) {
        throw new UnsupportedOperationException("NOT IMPLEMENTED YET");
    }
}
