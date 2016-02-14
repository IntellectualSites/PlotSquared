package com.intellectualcrafters.plot.util;

import com.google.common.base.Charsets;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.config.Settings;
import com.intellectualcrafters.plot.database.DBFunc;
import com.intellectualcrafters.plot.object.ConsolePlayer;
import com.intellectualcrafters.plot.object.OfflinePlotPlayer;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.object.RunnableVal;
import com.intellectualcrafters.plot.object.StringWrapper;
import com.intellectualcrafters.plot.uuid.UUIDWrapper;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public abstract class UUIDHandlerImplementation {

    public final ConcurrentHashMap<String, PlotPlayer> players;
    public boolean CACHED = false;
    public UUIDWrapper uuidWrapper = null;
    public HashSet<UUID> unknown = new HashSet<>();
    private BiMap<StringWrapper, UUID> uuidMap = HashBiMap.create(new HashMap<StringWrapper, UUID>());
    
    public UUIDHandlerImplementation(final UUIDWrapper wrapper) {
        uuidWrapper = wrapper;
        players = new ConcurrentHashMap<>();
    }
    
    /**
     * If the UUID is not found, some commands can request to fetch the UUID when possible
     * @param name
     * @param ifFetch
     */
    public abstract void fetchUUID(final String name, final RunnableVal<UUID> ifFetch);
    
    /**
     * Start UUID caching (this should be an async task)
     * Recommended to override this is you want to cache offline players
     */
    public boolean startCaching(final Runnable whenDone) {
        if (CACHED) {
            return false;
        }
        return CACHED = true;
    }
    
    public UUIDWrapper getUUIDWrapper() {
        return uuidWrapper;
    }
    
    public void setUUIDWrapper(final UUIDWrapper wrapper) {
        uuidWrapper = wrapper;
    }
    
    public void rename(final UUID uuid, final StringWrapper name) {
        uuidMap.inverse().remove(uuid);
        uuidMap.put(name, uuid);
    }
    
    public void add(final BiMap<StringWrapper, UUID> toAdd) {
        if (uuidMap.isEmpty()) {
            uuidMap = toAdd;
        }
        for (final Map.Entry<StringWrapper, UUID> entry : toAdd.entrySet()) {
            final UUID uuid = entry.getValue();
            final StringWrapper name = entry.getKey();
            if ((uuid == null) || (name == null)) {
                continue;
            }
            final BiMap<UUID, StringWrapper> inverse = uuidMap.inverse();
            if (inverse.containsKey(uuid)) {
                if (uuidMap.containsKey(name)) {
                    continue;
                }
                rename(uuid, name);
                continue;
            }
            uuidMap.put(name, uuid);
        }
        PS.debug(C.PREFIX.s() + "&6Cached a total of: " + uuidMap.size() + " UUIDs");
    }
    
    public boolean add(final StringWrapper name, final UUID uuid) {
        if ((uuid == null)) {
            return false;
        }
        if (name == null) {
            try {
                unknown.add(uuid);
            } catch (final Exception e) {
                ConsolePlayer.getConsole().sendMessage("&c(minor) Invalid UUID mapping: " + uuid);
                e.printStackTrace();
            }
            return false;
        }
        
        /*
         * lazy UUID conversion:
         *  - Useful if the person misconfigured the database, or settings before PlotMe conversion
         */
        if (!Settings.OFFLINE_MODE && !unknown.isEmpty()) {
            TaskManager.runTaskAsync(new Runnable() {
                @Override
                public void run() {
                    UUID offline = UUID.nameUUIDFromBytes(("OfflinePlayer:" + name.value).getBytes(Charsets.UTF_8));
                    if (!unknown.contains(offline) && !name.value.equals(name.value.toLowerCase())) {
                        offline = UUID.nameUUIDFromBytes(("OfflinePlayer:" + name.value.toLowerCase()).getBytes(Charsets.UTF_8));
                        if (!unknown.contains(offline)) {
                            offline = null;
                        }
                    }
                    if (offline != null && !offline.equals(uuid)) {
                        unknown.remove(offline);
                        final Set<Plot> plots = PS.get().getPlotsAbs(offline);
                        if (!plots.isEmpty()) {
                            for (final Plot plot : plots) {
                                plot.owner = uuid;
                            }
                            DBFunc.replaceUUID(offline, uuid);
                            PS.debug("&cDetected invalid UUID stored for: " + name.value);
                            PS.debug("&7 - Did you recently switch to online-mode storage without running `uuidconvert`?");
                            PS.debug("&6PlotSquared will update incorrect entries when the user logs in, or you can reconstruct your database.");
                        }
                    }
                }
            });
        }
        try {
            final UUID offline = uuidMap.put(name, uuid);
            if (offline != null) {
                if (!offline.equals(uuid)) {
                    final Set<Plot> plots = PS.get().getPlots(offline);
                    if (!plots.isEmpty()) {
                        for (final Plot plot : plots) {
                            plot.owner = uuid;
                        }
                        DBFunc.replaceUUID(offline, uuid);
                        PS.debug("&cDetected invalid UUID stored for (1): " + name.value);
                        PS.debug("&7 - Did you recently switch to online-mode storage without running `uuidconvert`?");
                        PS.debug("&6PlotSquared will update incorrect entries when the user logs in, or you can reconstruct your database.");
                    }
                    return true;
                }
                return false;
            }
        } catch (final Exception e) {
            final BiMap<UUID, StringWrapper> inverse = uuidMap.inverse();
            if (inverse.containsKey(uuid)) {
                if (uuidMap.containsKey(name)) {
                    return false;
                }
                rename(uuid, name);
                return false;
            }
            uuidMap.put(name, uuid);
        }
        return true;
    }
    
    public boolean uuidExists(final UUID uuid) {
        return uuidMap.containsValue(uuid);
    }
    
    public BiMap<StringWrapper, UUID> getUUIDMap() {
        return uuidMap;
    }
    
    public boolean nameExists(final StringWrapper wrapper) {
        return uuidMap.containsKey(wrapper);
    }
    
    public void handleShutdown() {
        players.clear();
        uuidMap.clear();
        uuidWrapper = null;
    }
    
    public String getName(final UUID uuid) {
        if (uuid == null) {
            return null;
        }
        //        // check online
        //        final PlotPlayer player = getPlayer(uuid);
        //        if (player != null) {
        //            return player.getName();
        //        }
        // check cache
        final StringWrapper name = uuidMap.inverse().get(uuid);
        if (name != null) {
            return name.value;
        }
        return null;
    }
    
    public UUID getUUID(final String name, final RunnableVal<UUID> ifFetch) {
        if ((name == null) || (name.isEmpty())) {
            return null;
        }
        // check online
        final PlotPlayer player = getPlayer(name);
        if (player != null) {
            return player.getUUID();
        }
        // check cache
        final StringWrapper wrap = new StringWrapper(name);
        UUID uuid = uuidMap.get(wrap);
        if (uuid != null) {
            return uuid;
        }
        // Read from disk OR convert directly to offline UUID
        if (Settings.OFFLINE_MODE) {
            uuid = uuidWrapper.getUUID(name);
            add(new StringWrapper(name), uuid);
            return uuid;
        }
        if (Settings.UUID_FROM_DISK && (ifFetch != null)) {
            fetchUUID(name, ifFetch);
            return null;
        }
        return null;
    }
    
    public UUID getUUID(final PlotPlayer player) {
        return uuidWrapper.getUUID(player);
    }
    
    public UUID getUUID(final OfflinePlotPlayer player) {
        return uuidWrapper.getUUID(player);
    }
    
    public PlotPlayer getPlayer(final UUID uuid) {
        String name = getName(uuid);
        if (name != null) {
            return getPlayer(name);
        }
        return null;
    }
    
    public PlotPlayer getPlayer(final String name) {
        return players.get(name);
    }
    
    public Map<String, PlotPlayer> getPlayers() {
        return players;
    }
    
}
