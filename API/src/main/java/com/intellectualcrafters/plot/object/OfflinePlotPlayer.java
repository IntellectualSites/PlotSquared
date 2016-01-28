package com.intellectualcrafters.plot.object;

import java.util.UUID;

/**
 * Created 2015-02-20 for PlotSquared
 *
 */
public interface OfflinePlotPlayer {
    public UUID getUUID();
    
    public long getLastPlayed();
    
    public boolean isOnline();
    
    public String getName();
}
