package com.intellectualcrafters.plot.object;

import java.util.UUID;

import com.intellectualcrafters.plot.util.PlotGamemode;
import com.intellectualcrafters.plot.util.PlotWeather;
import com.plotsquared.general.commands.CommandCaller;

/**
 * Created 2015-02-20 for PlotSquared
 *
 * @author Citymonstret
 */
public interface PlotPlayer extends CommandCaller {
    
    long getPreviousLogin();
    
    Location getLocation();
    
    Location getLocationFull();
    
    UUID getUUID();
    
    boolean hasPermission(final String perm);
    
    void sendMessage(final String message);

    void teleport(final Location loc);
    
    boolean isOp();

    boolean isOnline();

    String getName();
    
    void setCompassTarget(Location loc);
    
    void loadData();
    
    void saveData();
    
    /**
     * Set player data that will persist restarts
     *  - Please note that this is not intended to store large values
     *  - For session only data use meta
     * @param key
     */
    void setAttribute(String key);
    
    /**
     * The attribute will be either true or false
     * @param key
     */
    boolean getAttribute(String key);
    
    /**
     * Remove an attribute from a player
     * @param key
     */
    void removeAttribute(String key);
    
    void setMeta(String key, Object value);
    Object getMeta(String key);
    void deleteMeta(String key);
    
    void setWeather(PlotWeather weather);
    
    PlotGamemode getGamemode();
    
    void setGamemode(PlotGamemode gamemode);
    
    void setTime(long time);
    
    void setFlight(boolean fly);
    
    void playMusic(Location loc, int id);
    
    void kick(String message);
}
