package com.intellectualcrafters.plot.object;

import java.util.UUID;

/**
 * Created 2015-02-20 for PlotSquared
 *
 * @author Citymonstret
 */
public interface PlotPlayer {
    
    public void setTmpData(String key, Object value);
    
    public Object getTmpData(String key);
    
    public long getPreviousLogin();
    
    public Location getLocation();
    
    public Location getLocationFull();
    
    public UUID getUUID();
    
    public boolean hasPermission(final String perm);
    
    public void sendMessage(final String message);

    public void teleport(final Location loc);

    public boolean isOp();

    public boolean isOnline();

    public String getName();
    
    public void setCompassTarget(Location loc);
}
