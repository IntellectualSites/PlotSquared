package com.github.intellectualsites.plotsquared.plot.object;

import java.util.Set;
import java.util.UUID;

public class PlotHandler {
    public static boolean sameOwners(final Plot plot1, final Plot plot2) {
        if (plot1.owner == null || plot2.owner == null) {
            return false;
        }
        final Set<UUID> owners = plot1.getOwners();
        for (UUID owner : owners) {
            if (plot2.isOwner(owner)) {
                return true;
            }
        }
        return false;
    }
}
