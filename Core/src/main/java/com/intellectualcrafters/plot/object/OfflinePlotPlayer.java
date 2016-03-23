package com.intellectualcrafters.plot.object;

import java.util.UUID;

/**
 * Created 2015-02-20 for PlotSquared
 *

 */
public interface OfflinePlotPlayer {

    UUID getUUID();

    long getLastPlayed();

    boolean isOnline();

    String getName();
}
