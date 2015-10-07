package com.intellectualcrafters.plot.object;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.commands.RequiredType;
import com.intellectualcrafters.plot.config.Settings;
import com.intellectualcrafters.plot.util.EventUtil;
import com.intellectualcrafters.plot.util.ExpireManager;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.PlotGamemode;
import com.intellectualcrafters.plot.util.PlotWeather;
import com.intellectualcrafters.plot.util.UUIDHandler;
import com.plotsquared.general.commands.CommandCaller;
import com.plotsquared.listener.PlotListener;

/**
 * Created 2015-02-20 for PlotSquared
 *
 */
public abstract class PlotPlayer implements CommandCaller {
    
    /**
     * The metadata map
     */
    private ConcurrentHashMap<String, Object> meta;
    
    /**
     * Efficiently wrap a Player object to get a PlotPlayer (or fetch if it's already cached)<br>
     *  - Accepts sponge/bukkit Player (online)
     *  - Accepts player name (online)
     *  - Accepts UUID
     *  - Accepts bukkit OfflinePlayer (offline)
     * @param obj
     * @return
     */
    public static PlotPlayer wrap(final Object obj) {
        return PS.get().IMP.wrapPlayer(obj);
    }
    
    /**
     * Get the cached PlotPlayer from a username<br>
     *  - This will return null if the player has just logged in or is not online
     * @param name
     * @return
     */
    public static PlotPlayer get(final String name) {
        return UUIDHandler.getPlayer(name);
    }
    
    /**
     * Set some session only metadata for the player
     * @param key
     * @param value
     */
    public void setMeta(final String key, final Object value) {
        if (meta == null) {
            meta = new ConcurrentHashMap<String, Object>();
        }
        meta.put(key, value);
    }
    
    /**
     * Get the metadata for a key
     * @param key
     * @return
     */
    public Object getMeta(final String key) {
        if (meta != null) {
            return meta.get(key);
        }
        return null;
    }
    
    /**
     * Delete the metadata for a key<br>
     *  - metadata is session only
     *  - deleting other plugin's metadata may cause issues
     * @param key
     */
    public void deleteMeta(final String key) {
        if (meta != null) {
            meta.remove(key);
        }
    }
    
    /**
     * Returns the player's name
     * @see #getName()
     */
    @Override
    public String toString() {
        return getName();
    }
    
    /**
     * Get the player's current plot<br>
     *  - This will return null if the player is standing in the road, or not in a plot world/area
     *  - An unowned plot is still a plot, it just doesn't have any settings
     * @return
     */
    public Plot getCurrentPlot() {
        return (Plot) getMeta("lastplot");
    }
    
    /**
     * Get the total number of allowed plots
     * Possibly relevant: (To increment the player's allowed plots, see the example script on the wiki)
     * @return number of allowed plots within the scope (globally, or in the player's current world as defined in the settings.yml)
     */
    public int getAllowedPlots() {
        return MainUtil.getAllowedPlots(this);
    }
    
    /**
     * Get the number of plots the player owns
     *
     * @see #getPlotCount(String);
     * @see #getPlots()
     *
     * @return number of plots within the scope (globally, or in the player's current world as defined in the settings.yml)
     */
    public int getPlotCount() {
        return MainUtil.getPlayerPlotCount(this);
    }
    
    /**
     * Get the number of plots the player owns in the world
     * @param world
     * @return
     */
    public int getPlotCount(final String world) {
        return MainUtil.getPlayerPlotCount(world, this);
    }
    
    /**
     * Get the plots the player owns
     * @see #PS.java for more searching functions
     * @see #getPlotCount() for the number of plots
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
     * Get the player's last recorded location or null if they don't any plot relevant location
     * @return
     */
    public Location getLocation() {
        final Location loc = (Location) getMeta("location");
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
    @Override
    public abstract boolean hasPermission(final String perm);
    
    /**
     * Send the player a message
     */
    @Override
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
    public abstract void setCompassTarget(final Location loc);
    
    /**
     * Load the player data from disk (if applicable)
     * @deprecated hacky
     */
    @Deprecated
    public abstract void loadData();
    
    /**
     * Save the player data from disk (if applicable)
     * @deprecated hacky
     */
    @Deprecated
    public abstract void saveData();
    
    /**
     * Set player data that will persist restarts
     *  - Please note that this is not intended to store large values
     *  - For session only data use meta
     * @param key
     */
    public abstract void setAttribute(final String key);
    
    /**
     * The attribute will be either true or false
     * @param key
     */
    public abstract boolean getAttribute(final String key);
    
    /**
     * Remove an attribute from a player
     * @param key
     */
    public abstract void removeAttribute(final String key);
    
    /**
     * Set the player's local weather
     * @param weather
     */
    public abstract void setWeather(final PlotWeather weather);
    
    /**
     * Get the player's gamemode
     * @return
     */
    public abstract PlotGamemode getGamemode();
    
    /**
     * Set the player's gamemode
     * @param gamemode
     */
    public abstract void setGamemode(final PlotGamemode gamemode);
    
    /**
     * Set the player's local time (ticks)
     * @param time
     */
    public abstract void setTime(final long time);
    
    /**
     * Set the player's fly mode
     * @param fly
     */
    public abstract void setFlight(final boolean fly);
    
    /**
     * Play music at a location for the player
     * @param loc
     * @param id
     */
    public abstract void playMusic(final Location loc, final int id);
    
    /**
     * Check if the player is banned
     * @return
     */
    public abstract boolean isBanned();
    
    /**
     * Kick the player from the game
     * @param message
     */
    public abstract void kick(final String message);
    
    /**
     * Called when the player quits
     */
    public void unregister() {
        final Plot plot = getCurrentPlot();
        if (plot != null) {
            PlotListener.plotExit(this, plot);
        }
        ExpireManager.dates.put(getUUID(), System.currentTimeMillis());
        EventUtil.unregisterPlayer(this);
        if (Settings.DELETE_PLOTS_ON_BAN && isBanned()) {
            for (final Plot owned : PS.get().getPlotsInWorld(getName())) {
                owned.deletePlot(null);
                PS.debug(String.format("&cPlot &6%s &cwas deleted + cleared due to &6%s&c getting banned", plot.getId(), getName()));
            }
        }
        UUIDHandler.getPlayers().remove(getName());
        PS.get().IMP.unregister(this);
    }
}
