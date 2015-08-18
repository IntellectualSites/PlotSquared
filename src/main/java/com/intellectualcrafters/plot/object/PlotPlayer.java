package com.intellectualcrafters.plot.object;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.commands.RequiredType;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.PlotGamemode;
import com.intellectualcrafters.plot.util.PlotWeather;
import com.intellectualcrafters.plot.util.UUIDHandler;
import com.plotsquared.general.commands.CommandCaller;

/**
 * Created 2015-02-20 for PlotSquared
 *
 * @author Citymonstret
 */
public abstract class PlotPlayer implements CommandCaller {
    
    /**
     * The metadata map
     */
    private ConcurrentHashMap<String, Object> meta;

    /**
     * Wrap a Player object to get a PlotPlayer<br>
     *  - This will usually be cached, so no new object creation
     * @param obj
     * @return
     */
    public static PlotPlayer wrap(Object obj) {
        return PS.get().IMP.wrapPlayer(obj);
    }
    
    /**
     * Get the cached PlotPlayer from a username<br>
     *  - This will return null if the player has just logged in or is not online
     * @param name
     * @return
     */
    public static PlotPlayer get(String name) {
        return UUIDHandler.getPlayer(name);
    }
    
    /**
     * Set some session only metadata for the player
     * @param key
     * @param value
     */
    public void setMeta(String key, Object value) {
        if (this.meta == null) {
            this.meta = new ConcurrentHashMap<String, Object>();
        }
        this.meta.put(key, value);
    }

    /**
     * Get the metadata for a key
     * @param key
     * @return
     */
    public Object getMeta(String key) {
        if (this.meta != null) {
            return this.meta.get(key);
        }
        return null;
    }

    /**
     * Delete the metadata for a key<br>
     *  - metadata is session only
     *  - deleting other plugin's metadata may cause issues
     * @param key
     */
    public void deleteMeta(String key) {
        if (this.meta != null) {
            this.meta.remove(key);
        }
    }
    
    /**
     * Returns the player's name
     * @see #getName()
     */
    public String toString() {
        return getName();
    }
    
    /**
     * Get the player's current plot<br>
     *  - This is cached
     * @return
     */
    public Plot getCurrentPlot() {
        return (Plot) getMeta("lastplot");
    }
    
    /**
     * Get the total number of allowed plots
     * @return
     */
    public int getAllowedPlots() {
        return MainUtil.getAllowedPlots(this);
    }
    
    /**
     * Get the number of plots the player owns
     * @return
     */
    public int getPlotCount() {
        return MainUtil.getPlayerPlotCount(this);
    }
    
    /**
     * Get the number of plots the player owns in the world
     * @param world
     * @return
     */
    public int getPlotCount(String world) {
        return MainUtil.getPlayerPlotCount(world, this);
    }
    
    /**
     * Get the plots the player owns
     * @see #PS.java for more searching functions
     * @return Set of plots
     */
    public Set<Plot> getPlots() {
        return PS.get().getPlots(this);
    }
    
    /**
     * Return the PlotWorld the player is currently in, or null
     * @return
     */
    public PlotWorld getPlotWorld() {
        return PS.get().getPlotWorld(getLocation().getWorld());
    }
    
    @Override
    public RequiredType getSuperCaller() {
        return RequiredType.PLAYER;
    }

    /////////////// PLAYER META ///////////////
    
    ////////////// PARTIALLY IMPLEMENTED ///////////
    /**
     * Get the player's last recorded location
     * @return
     */
    public Location getLocation() {
        Location loc = (Location) getMeta("location");
        if (loc != null) {
            return loc;
        }
        return null;
    }
    
    ////////////////////////////////////////////////
    
    /**
     * Get the previous time the player logged in
     * @return
     */
    public abstract long getPreviousLogin();
    
    
    
    /**
     * Get the player's full location (including yaw/pitch)
     * @return
     */
    public abstract Location getLocationFull();
    
    /**
     * Get the player's UUID<br>
     *  === !IMPORTANT ===<br> 
     *  The UUID is dependent on the mode chosen in the settings.yml and may not be the same as Bukkit has 
     *  (especially if using an old version of Bukkit that does not support UUIDs)
     *  
     * @return UUID
     */
    public abstract UUID getUUID();
    
    /**
     * Check the player's permissions<br>
     *  - Will be cached if permission caching is enabled
     */
    public abstract boolean hasPermission(final String perm);
    
    /**
     * Send the player a message
     */
    public abstract void sendMessage(final String message);

    /**
     * Teleport the player to a location
     * @param loc
     */
    public abstract void teleport(final Location loc);

    /**
     * Is the player online
     * @return
     */
    public abstract boolean isOnline();

    /**
     * Get the player's name
     * @return
     */
    public abstract String getName();
    
    /**
     * Set the compass target
     * @param loc
     */
    public abstract void setCompassTarget(Location loc);
    
    /**
     * Load the player data from disk (if applicable)
     */
    public abstract void loadData();
    
    /**
     * Save the player data from disk (if applicable)
     */
    public abstract void saveData();
    
    /**
     * Set player data that will persist restarts
     *  - Please note that this is not intended to store large values
     *  - For session only data use meta
     * @param key
     */
    public abstract void setAttribute(String key);
    
    /**
     * The attribute will be either true or false
     * @param key
     */
    public abstract boolean getAttribute(String key);
    
    /**
     * Remove an attribute from a player
     * @param key
     */
    public abstract void removeAttribute(String key);
    
    /**
     * Set the player's local weather
     * @param weather
     */
    public abstract void setWeather(PlotWeather weather);
    
    /**
     * Get the player's gamemode
     * @return
     */
    public abstract PlotGamemode getGamemode();
    
    /**
     * Set the player's gamemode
     * @param gamemode
     */
    public abstract void setGamemode(PlotGamemode gamemode);
    
    /**
     * Set the player's local time
     * @param time
     */
    public abstract void setTime(long time);
    
    /**
     * Set the player's fly mode
     * @param fly
     */
    public abstract void setFlight(boolean fly);
    
    /**
     * Play music at a location for the player
     * @param loc
     * @param id
     */
    public abstract void playMusic(Location loc, int id);
    
    /**
     * Kick the player from the game
     * @param message
     */
    public abstract void kick(String message);
}
