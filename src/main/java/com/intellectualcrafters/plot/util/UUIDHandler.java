package com.intellectualcrafters.plot.util;

import java.util.HashSet;
import java.util.Map;
import java.util.UUID;

import com.google.common.collect.BiMap;
import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.object.OfflinePlotPlayer;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.object.RunnableVal;
import com.intellectualcrafters.plot.object.StringWrapper;
import com.intellectualcrafters.plot.uuid.UUIDWrapper;

public class UUIDHandler {

    public static UUIDHandlerImplementation implementation;

    public static void add(final StringWrapper name, final UUID uuid) {
        implementation.add(name, uuid);
    }

    /**
     * Get the map containing all names/uuids
     *
     * @return map with names + uuids
     *
     * @see com.google.common.collect.BiMap
     */
    public static BiMap<StringWrapper, UUID> getUuidMap() {
        return implementation.getUUIDMap();
    }

    /**
     * Check if a uuid is cached
     *
     * @param uuid to check
     *
     * @return true of the uuid is cached
     *
     * @see com.google.common.collect.BiMap#containsValue(Object)
     */
    public static boolean uuidExists(final UUID uuid) {
        return implementation.uuidExists(uuid);
    }

    /**
     * Check if a name is cached
     *
     * @param name to check
     *
     * @return true of the name is cached
     *
     * @see com.google.common.collect.BiMap#containsKey(Object)
     */
    public static boolean nameExists(final StringWrapper name) {
        return implementation.nameExists(name);
    }
    
    public static HashSet<UUID> getAllUUIDS() {
        HashSet<UUID> uuids = new HashSet<>();
        for (Plot plot : PS.get().getPlotsRaw()) {
            if (plot.owner != null) {
                uuids.add(plot.owner);
                uuids.addAll(plot.getTrusted());
                uuids.addAll(plot.getMembers());
                uuids.addAll(plot.getDenied());
            }
        }
        return uuids;
    }

    public static UUIDWrapper getUUIDWrapper() {
        return implementation.getUUIDWrapper();
    }

    public static void setUUIDWrapper(final UUIDWrapper wrapper) {
        implementation.setUUIDWrapper(wrapper);
    }

    public static void startCaching(Runnable whenDone) {
        implementation.startCaching(whenDone);
    }

    public static void cache(final BiMap<StringWrapper, UUID> toAdd) {
        implementation.add(toAdd);
    }

    public static UUID getUUID(final PlotPlayer player) {
        return implementation.getUUID(player);
    }

    public static UUID getUUID(final OfflinePlotPlayer player) {
        return implementation.getUUID(player);
    }

    public static String getName(final UUID uuid) {
        return implementation.getName(uuid);
    }

    public static PlotPlayer getPlayer(final UUID uuid) {
        return implementation.getPlayer(uuid);
    }

    public static PlotPlayer getPlayer(final String name) {
        return implementation.getPlayer(name);
    }
    
    public static UUID getUUID(final String name, RunnableVal<UUID> ifFetch) {
        return implementation.getUUID(name, ifFetch);
    }
    
    public static UUID getCachedUUID(final String name, RunnableVal<UUID> ifFetch) {
        return implementation.getUUIDMap().get(new StringWrapper(name));
    }

    public static Map<String, PlotPlayer> getPlayers() {
        return implementation.getPlayers();
    }

    public static void handleShutdown() {
        implementation.handleShutdown();
    }
}
