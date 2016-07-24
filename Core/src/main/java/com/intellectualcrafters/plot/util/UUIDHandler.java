package com.intellectualcrafters.plot.util;

import com.google.common.collect.BiMap;
import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.object.OfflinePlotPlayer;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.object.RunnableVal;
import com.intellectualcrafters.plot.object.StringWrapper;
import com.intellectualcrafters.plot.uuid.UUIDWrapper;

import java.util.HashSet;
import java.util.Map;
import java.util.UUID;

public class UUIDHandler {

    public static UUIDHandlerImplementation implementation;

    public static void add(StringWrapper name, UUID uuid) {
        implementation.add(name, uuid);
    }

    /**
     * Get the map containing all names/uuids.
     *
     * @return map with names + uuids
     *
     * @see BiMap
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
     * @see BiMap#containsValue(Object)
     */
    public static boolean uuidExists(UUID uuid) {
        return implementation.uuidExists(uuid);
    }

    /**
     * Check if a name is cached
     *
     * @param name to check
     *
     * @return true of the name is cached
     *
     * @see BiMap#containsKey(Object)
     */
    public static boolean nameExists(StringWrapper name) {
        return implementation.nameExists(name);
    }

    public static HashSet<UUID> getAllUUIDS() {
        final HashSet<UUID> uuids = new HashSet<>();
        PS.get().foreachPlotRaw(new RunnableVal<Plot>() {
            @Override
            public void run(Plot plot) {
                if (plot.hasOwner()) {
                    uuids.add(plot.owner);
                    uuids.addAll(plot.getTrusted());
                    uuids.addAll(plot.getMembers());
                    uuids.addAll(plot.getDenied());
                }
            }
        });
        return uuids;
    }

    public static UUIDWrapper getUUIDWrapper() {
        return implementation.getUUIDWrapper();
    }

    public static void setUUIDWrapper(UUIDWrapper wrapper) {
        implementation.setUUIDWrapper(wrapper);
    }

    public static void startCaching(Runnable whenDone) {
        implementation.startCaching(whenDone);
    }

    public static void cache(BiMap<StringWrapper, UUID> toAdd) {
        implementation.add(toAdd);
    }

    public static UUID getUUID(PlotPlayer player) {
        return implementation.getUUID(player);
    }

    public static UUID getUUID(OfflinePlotPlayer player) {
        return implementation.getUUID(player);
    }

    public static String getName(UUID uuid) {
        return implementation.getName(uuid);
    }

    public static PlotPlayer getPlayer(UUID uuid) {
        return implementation.getPlayer(uuid);
    }

    public static PlotPlayer getPlayer(String name) {
        return implementation.getPlayer(name);
    }

    public static UUID getUUIDFromString(String nameOrUUIDString) {
        if (nameOrUUIDString.length() > 16) {
            return UUID.fromString(nameOrUUIDString);
        }
        return UUIDHandler.getUUID(nameOrUUIDString, null);
    }

    public static UUID getUUID(String name, RunnableVal<UUID> ifFetch) {
        return implementation.getUUID(name, ifFetch);
    }

    public static UUID getCachedUUID(String name, RunnableVal<UUID> ifFetch) {
        return implementation.getUUIDMap().get(new StringWrapper(name));
    }

    public static Map<String, PlotPlayer> getPlayers() {
        return implementation.getPlayers();
    }

    public static void handleShutdown() {
        implementation.handleShutdown();
    }
}
