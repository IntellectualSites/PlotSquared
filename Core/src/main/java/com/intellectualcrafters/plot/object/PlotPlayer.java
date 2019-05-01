package com.intellectualcrafters.plot.object;

import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.commands.RequiredType;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.config.Settings;
import com.intellectualcrafters.plot.database.DBFunc;
import com.intellectualcrafters.plot.flag.Flags;
import com.intellectualcrafters.plot.object.worlds.PlotAreaManager;
import com.intellectualcrafters.plot.object.worlds.SinglePlotArea;
import com.intellectualcrafters.plot.object.worlds.SinglePlotAreaManager;
import com.intellectualcrafters.plot.util.*;
import com.intellectualcrafters.plot.util.expiry.ExpireManager;
import com.plotsquared.general.commands.CommandCaller;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The abstract class supporting {@code BukkitPlayer} and {@code SpongePlayer}.
 */
public abstract class PlotPlayer implements CommandCaller, OfflinePlotPlayer {
    private Map<String, byte[]> metaMap = new HashMap<>();

    /** The metadata map.*/
    private ConcurrentHashMap<String, Object> meta;

    /**
     * Efficiently wrap a Player, or OfflinePlayer object to get a PlotPlayer (or fetch if it's already cached)<br>
     *  - Accepts sponge/bukkit Player (online)
     *  - Accepts player name (online)
     *  - Accepts UUID
     *  - Accepts bukkit OfflinePlayer (offline)
     * @param player
     * @return
     */
    public static PlotPlayer wrap(Object player) {
        return PS.get().IMP.wrapPlayer(player);
    }

    /**
     * Get the cached PlotPlayer from a username<br>
     *  - This will return null if the player has not finished logging in or is not online
     * @param name
     * @return
     */
    public static PlotPlayer get(String name) {
        return UUIDHandler.getPlayer(name);
    }

    /**
     * Set some session only metadata for this player.
     * @param key
     * @param value
     */
    public void setMeta(String key, Object value) {
        if (value == null) {
            deleteMeta(key);
        } else {
            if (this.meta == null) {
                this.meta = new ConcurrentHashMap<>();
            }
            this.meta.put(key, value);
        }
    }

    /**
     * Get the session metadata for a key.
     * @param key the name of the metadata key
     * @param <T> the object type to return
     * @return the value assigned to the key or null if it does not exist
     */
    public <T> T getMeta(String key) {
        if (this.meta != null) {
            return (T) this.meta.get(key);
        }
        return null;
    }

    public <T> T getMeta(String key, T defaultValue) {
        T meta = getMeta(key);
        if (meta == null) {
            return defaultValue;
        }
        return meta;
    }

    /**
     * Delete the metadata for a key.
     *  - metadata is session only
     *  - deleting other plugin's metadata may cause issues
     * @param key
     */
    public Object deleteMeta(String key) {
        return this.meta == null ? null : this.meta.remove(key);
    }

    /**
     * This player's name.
     *
     * @return the name of the player
     */
    @Override
    public String toString() {
        return getName();
    }

    /**
     * Get this player's current plot.
     * @return the plot the player is standing on or null if standing on a road or not in a {@link PlotArea}
     */
    public Plot getCurrentPlot() {
        Plot value = getMeta("lastplot");
        if (value == null && !Settings.Enabled_Components.EVENTS) {
            return getLocation().getPlot();
        }
        return value;
    }

    /**
     * Get the total number of allowed plots
     * Possibly relevant: (To increment the player's allowed plots, see the example script on the wiki)
     * @return number of allowed plots within the scope (globally, or in the player's current world as defined in the settings.yml)
     */
    public int getAllowedPlots() {
        return Permissions.hasPermissionRange(this, "plots.plot", Settings.Limit.MAX_PLOTS);
    }

    /**
     * Get the total number of allowed clusters
     * @return number of allowed clusters within the scope (globally, or in the player's current world as defined in the settings.yml)
     */
    public int getAllowedClusters() {
        return Permissions.hasPermissionRange(this, "plots.cluster", Settings.Limit.MAX_PLOTS);
    }

    public int hasPermissionRange(String stub, int range) {
        if (hasPermission(C.PERMISSION_ADMIN.s())) {
            return Integer.MAX_VALUE;
        }
        String[] nodes = stub.split("\\.");
        StringBuilder n = new StringBuilder();
        for (int i = 0; i < (nodes.length - 1); i++) {
            n.append(nodes[i]).append(".");
            if (!stub.equals(n + C.PERMISSION_STAR.s())) {
                if (hasPermission(n + C.PERMISSION_STAR.s())) {
                    return Integer.MAX_VALUE;
                }
            }
        }
        if (hasPermission(stub + ".*")) {
            return Integer.MAX_VALUE;
        }
        for (int i = range; i > 0; i--) {
            if (hasPermission(stub + "." + i)) {
                return i;
            }
        }
        return 0;
    }

    /**
     * Get the number of plots this player owns.
     *
     * @see #getPlotCount(String);
     * @see #getPlots()
     *
     * @return number of plots within the scope (globally, or in the player's current world as defined in the settings.yml)
     */
    public int getPlotCount() {
        if (!Settings.Limit.GLOBAL) {
            return getPlotCount(getLocation().getWorld());
        }
        final AtomicInteger count = new AtomicInteger(0);
        final UUID uuid = getUUID();
        PS.get().foreachPlotArea(new RunnableVal<PlotArea>() {
            @Override
            public void run(PlotArea value) {
                if (!Settings.Done.COUNTS_TOWARDS_LIMIT) {
                    for (Plot plot : value.getPlotsAbs(uuid)) {
                        if (!plot.hasFlag(Flags.DONE)) {
                            count.incrementAndGet();
                        }
                    }
                } else {
                    count.addAndGet(value.getPlotsAbs(uuid).size());
                }
            }
        });
        return count.get();
    }

    public int getClusterCount() {
        if (!Settings.Limit.GLOBAL) {
            return getClusterCount(getLocation().getWorld());
        }
        final AtomicInteger count = new AtomicInteger(0);
        final UUID uuid = getUUID();
        PS.get().foreachPlotArea(new RunnableVal<PlotArea>() {
            @Override
            public void run(PlotArea value) {
                for (PlotCluster cluster : value.getClusters()) {
                    if (cluster.isOwner(getUUID())) {
                        count.incrementAndGet();
                    }
                }
            }
        });
        return count.get();
    }

    /**
     * Get the number of plots this player owns in the world.
     * @param world the name of the plotworld to check.
     * @return
     */
    public int getPlotCount(String world) {
        UUID uuid = getUUID();
        int count = 0;
        for (PlotArea area : PS.get().getPlotAreas(world)) {
            if (!Settings.Done.COUNTS_TOWARDS_LIMIT) {
                for (Plot plot : area.getPlotsAbs(uuid)) {
                    if (!plot.getFlag(Flags.DONE).isPresent()) {
                        count++;
                    }
                }
            } else {
                count += area.getPlotsAbs(uuid).size();
            }
        }
        return count;
    }

    public int getClusterCount(String world) {
        UUID uuid = getUUID();
        int count = 0;
        for (PlotArea area : PS.get().getPlotAreas(world)) {
            for (PlotCluster cluster : area.getClusters()) {
                if (cluster.isOwner(getUUID())) {
                    count++;
                }
            }
        }
        return count;
    }

    /**
     * Get a {@code Set} of plots owned by this player.
     * @see PS for more searching functions
     * @see #getPlotCount() for the number of plots
     * @return a {@code Set} of plots owned by the player
     */
    public Set<Plot> getPlots() {
        return PS.get().getPlots(this);
    }

    /**
     * Return the PlotArea this player is currently in, or null.
     * @return
     */
    public PlotArea getPlotAreaAbs() {
        return PS.get().getPlotAreaAbs(getLocation());
    }

    public PlotArea getApplicablePlotArea() {
        return PS.get().getApplicablePlotArea(getLocation());
    }

    @Override
    public RequiredType getSuperCaller() {
        return RequiredType.PLAYER;
    }

    /////////////// PLAYER META ///////////////

    ////////////// PARTIALLY IMPLEMENTED ///////////
    /**
     * Get this player's last recorded location or null if they don't any plot relevant location.
     * @return The location
     */
    public Location getLocation() {
        Location location = getMeta("location");
        if (location != null) {
            return location;
        }
        return getLocationFull();
    }

    ////////////////////////////////////////////////

    @Deprecated
    public long getPreviousLogin() {
        return getLastPlayed();
    }

    /**
     * Get this player's full location (including yaw/pitch)
     * @return
     */
    public abstract Location getLocationFull();

    /**
     * Get this player's UUID.
     * === !IMPORTANT ===<br>
     * The UUID is dependent on the mode chosen in the settings.yml and may not be the same as Bukkit has
     * (especially if using an old version of Bukkit that does not support UUIDs)
     *
     * @return UUID
     */
    @Override public abstract UUID getUUID();

    public boolean canTeleport(Location loc) {
        Location current = getLocationFull();
        teleport(loc);
        boolean result = true;
        if (!getLocation().equals(loc)) {
            result = false;
        }
        teleport(current);
        return result;
    }

    /**
     * Teleport this player to a location.
     * @param location the target location
     */
    public abstract void teleport(Location location);
    /**
     * Kick this player to a location
     *
     * @param location the target location
     */
    public void plotkick(Location location) {
        setMeta("kick", true);
        teleport(location);
        deleteMeta("kick");
    }

    /**
     * Set this compass target.
     * @param location the target location
     */
    public abstract void setCompassTarget(Location location);

    /**
     * Set player data that will persist restarts.
     *  - Please note that this is not intended to store large values
     *  - For session only data use meta
     * @param key
     */
    public void setAttribute(String key) {
        setPersistentMeta("attrib_" + key, new byte[]{(byte) 1});
    }


    /**
     * Retrieves the attribute of this player.
     *
     * @param key
     * @return the attribute will be either true or false
     */
    public boolean getAttribute(String key) {
        if (!hasPersistentMeta("attrib_" + key)) {
            return false;
        }
        return getPersistentMeta("attrib_" + key)[0] == 1;
    }

    /**
     * Remove an attribute from a player.
     * @param key
     */
    public void removeAttribute(String key) {
        removePersistentMeta("attrib_" + key);
    }

    /**
     * Sets the local weather for this Player.
     * @param weather the weather visible to the player
     */
    public abstract void setWeather(PlotWeather weather);

    /**
     * Get this player's gamemode.
     * @return the gamemode of the player.
     */
    public abstract PlotGameMode getGameMode();

    /**
     * Set this player's gameMode.
     * @param gameMode the gamemode to set
     */
    public abstract void setGameMode(PlotGameMode gameMode);

    /**
     * Set this player's local time (ticks).
     * @param time the time visible to the player
     */
    public abstract void setTime(long time);

    public abstract  boolean getFlight();

    /**
     * Set this player's fly mode.
     * @param fly if the player can fly
     */
    public abstract void setFlight(boolean fly);

    /**
     * Play music at a location for this player.
     * @param location where to play the music
     * @param id the numerical record item id
     */
    public abstract void playMusic(Location location, int id);

    /**
     * Check if this player is banned.
     * @return true if the player is banned, false otherwise.
     */
    public abstract boolean isBanned();

    /**
     * Kick this player from the game.
     * @param message the reason for the kick
     */
    public abstract void kick(String message);

    /**
     * Called when this player quits.
     */
    public void unregister() {
        Plot plot = getCurrentPlot();
        if (plot != null && Settings.Enabled_Components.PERSISTENT_META && plot.getArea() instanceof SinglePlotArea) {
            PlotId id = plot.getId();
            int x = id.x;
            int z = id.y;
            ByteBuffer buffer = ByteBuffer.allocate(13);
            buffer.putShort((short) x);
            buffer.putShort((short) z);
            Location loc = getLocation();
            buffer.putInt(loc.getX());
            buffer.put((byte) loc.getY());
            buffer.putInt(loc.getZ());
            setPersistentMeta("quitLoc", buffer.array());
        } else if (hasPersistentMeta("quitLoc")) {
            removePersistentMeta("quitLoc");
        }
        if (plot != null) {
            EventUtil.manager.callLeave(this, plot);
        }
        if (Settings.Enabled_Components.BAN_DELETER && isBanned()) {
            for (Plot owned : getPlots()) {
                owned.deletePlot(null);
                PS.debug(String.format("&cPlot &6%s &cwas deleted + cleared due to &6%s&c getting banned", plot.getId(), getName()));
            }
        }
        String name = getName();
        if (ExpireManager.IMP != null) {
            ExpireManager.IMP.storeDate(getUUID(), System.currentTimeMillis());
        }
        UUIDHandler.getPlayers().remove(name);
        PS.get().IMP.unregister(this);
    }

    /**
     * Get the amount of clusters this player owns in the specific world.
     * @param world
     * @return
     */
    public int getPlayerClusterCount(String world) {
        UUID uuid = getUUID();
        int count = 0;
        for (PlotCluster cluster : PS.get().getClusters(world)) {
            if (uuid.equals(cluster.owner)) {
                count += cluster.getArea();
            }
        }
        return count;
    }

    /**
     * Get the amount of clusters this player owns.
     * @return the number of clusters this player owns
     */
    public int getPlayerClusterCount() {
        final AtomicInteger count = new AtomicInteger();
        PS.get().foreachPlotArea(new RunnableVal<PlotArea>() {
            @Override
            public void run(PlotArea value) {
                count.addAndGet(value.getClusters().size());
            }
        });
        return count.get();
    }

    /**
     * Return a {@code Set} of all plots this player owns in a certain world.
     * @param world the world to retrieve plots from
     * @return a {@code Set} of plots this player owns in the provided world
     */
    public Set<Plot> getPlots(String world) {
        UUID uuid = getUUID();
        HashSet<Plot> plots = new HashSet<>();
        for (Plot plot : PS.get().getPlots(world)) {
            if (plot.isOwner(uuid)) {
                plots.add(plot);
            }
        }
        return plots;
    }

    public void populatePersistentMetaMap() {
        if (Settings.Enabled_Components.PERSISTENT_META) {
            DBFunc.getPersistentMeta(getUUID(), new RunnableVal<Map<String, byte[]>>() {
                @Override
                public void run(Map<String, byte[]> value) {
                    try {
                        PlotPlayer.this.metaMap = value;
                        if (!value.isEmpty()) {
                            if (Settings.Teleport.ON_LOGIN) {
                                PlotAreaManager manager = PS.get().getPlotAreaManager();
                                if (manager instanceof SinglePlotAreaManager) {
                                    PlotArea area = ((SinglePlotAreaManager) manager).getArea();
                                    byte[] arr = PlotPlayer.this.getPersistentMeta("quitLoc");
                                    if (arr != null) {
                                        removePersistentMeta("quitLoc");

                                        if (getMeta("teleportOnLogin", true)) {
                                            ByteBuffer quitWorld = ByteBuffer.wrap(arr);
                                            final int plotX = quitWorld.getShort();
                                            final int plotZ = quitWorld.getShort();
                                            PlotId id = new PlotId(plotX, plotZ);
                                            int x = quitWorld.getInt();
                                            int y = quitWorld.get() & 0xFF;
                                            int z = quitWorld.getInt();
                                            Plot plot = area.getOwnedPlot(id);
                                            if (plot != null) {
                                                final Location loc = new Location(plot.getWorldName(), x, y, z);
                                                if (plot.isLoaded()) {
                                                    TaskManager.runTask(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            if (getMeta("teleportOnLogin", true)) {
                                                                teleport(loc);
                                                                sendMessage(C.TELEPORTED_TO_PLOT.f() + " (quitLoc) (" + plotX + "," + plotZ + ")");
                                                            }
                                                        }
                                                    });
                                                } else if (!PS.get().isMainThread(Thread.currentThread())) {
                                                    if (getMeta("teleportOnLogin", true)) {
                                                        if (plot.teleportPlayer(PlotPlayer.this)) {
                                                            TaskManager.runTask(new Runnable() {
                                                                @Override
                                                                public void run() {
                                                                    if (getMeta("teleportOnLogin", true)) {
                                                                        if (plot.isLoaded()) {
                                                                            teleport(loc);
                                                                            sendMessage(C.TELEPORTED_TO_PLOT.f() + " (quitLoc-unloaded) (" + plotX + "," + plotZ + ")");
                                                                        }
                                                                    }
                                                                }
                                                            });
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    public byte[] getPersistentMeta(String key) {
        return this.metaMap.get(key);
    }

    public void removePersistentMeta(String key) {
        if (this.metaMap.containsKey(key)) {
            this.metaMap.remove(key);
        }
        if (Settings.Enabled_Components.PERSISTENT_META) {
            DBFunc.removePersistentMeta(getUUID(), key);
        }
    }

    public void setPersistentMeta(String key, byte[] value) {
        boolean delete = hasPersistentMeta(key);
        this.metaMap.put(key, value);
        if (Settings.Enabled_Components.PERSISTENT_META) {
            DBFunc.addPersistentMeta(getUUID(), key, value, delete);
        }
    }

    public boolean hasPersistentMeta(String key) {
        return this.metaMap.containsKey(key);
    }

    public abstract void stopSpectating();

    /**
     * The amount of money this Player has.
     * @return
     */
    public double getMoney() {
        return EconHandler.manager == null ? 0 : EconHandler.manager.getMoney(this);
    }

    public void withdraw(double amount) {
        if (EconHandler.manager != null) {
            EconHandler.manager.withdrawMoney(this, amount);
        }
    }

    public void deposit(double amount) {
        if (EconHandler.manager != null) {
            EconHandler.manager.depositMoney(this, amount);
        }
    }
}
