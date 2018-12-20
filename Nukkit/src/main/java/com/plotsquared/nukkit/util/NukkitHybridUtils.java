package com.plotsquared.nukkit.util;

import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.generator.HybridUtils;
import com.intellectualcrafters.plot.object.RegionWrapper;
import com.intellectualcrafters.plot.object.RunnableVal;
import com.intellectualcrafters.plot.util.expiry.PlotAnalysis;

public class NukkitHybridUtils extends HybridUtils {

    public NukkitHybridUtils() {
        PS.debug("Not implemented: NukkitHybridUtils");
    }

    @Override
    public void analyzeRegion(final String world, final RegionWrapper region, final RunnableVal<PlotAnalysis> whenDone) {
        throw new UnsupportedOperationException("NOT IMPLEMENTED YET");
    }
}
