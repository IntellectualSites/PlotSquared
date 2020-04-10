package com.github.intellectualsites.plotsquared.plot.object;

import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.UUID;

public class PlotHandler {

    public static boolean sameOwners(@NotNull final Plot plot1, @NotNull final Plot plot2) {
        if (!(plot1.hasOwner() && plot2.hasOwner())) {
            return false;
        }
        final Set<UUID> owners = plot1.getOwners();
        for (final UUID owner : owners) {
            if (plot2.isOwner(owner)) {
                return true;
            }
        }
        return false;
    }

}
