package com.intellectualcrafters.plot.object;

import java.util.UUID;

/**
 * Created 2015-02-20 for PlotSquared
 *
 * @author Citymonstret
 */
public interface PlotPlayer {
    
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
    
    public void loadData();
    
    public void saveData();
    
    /**
     * Set player data that will persist restarts
     *  - Please note that this is not intended to store large values
     *  - For session only data use meta
     * @param key
     * @param value
     */
    public void setAttribute(String key);
    
    /**
     * The attribute will be either true or false
     * @param key
     */
    public boolean getAttribute(String key);
    
    /**
     * Remove an attribute from a player
     * @param key
     */
    public void removeAttribute(String key);
    
    public void setMeta(String key, Object value);
    public Object getMeta(String key);
    public void deleteMeta(String key);
}
