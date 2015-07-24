package com.intellectualcrafters.plot.util.bukkit;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import com.intellectualcrafters.plot.util.bukkit.uuid.FileUUIDHandler;
import com.intellectualcrafters.plot.util.bukkit.uuid.SQLUUIDHandler;
import com.intellectualcrafters.plot.util.bukkit.uuid.UUIDHandlerImplementation;
import org.bukkit.Bukkit;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.io.Files;
import com.google.common.io.InputSupplier;
import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.config.Settings;
import com.intellectualcrafters.plot.database.DBFunc;
import com.intellectualcrafters.plot.object.BukkitOfflinePlayer;
import com.intellectualcrafters.plot.object.OfflinePlotPlayer;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.object.StringWrapper;
import com.intellectualcrafters.plot.util.ExpireManager;
import com.intellectualcrafters.plot.util.NbtFactory;
import com.intellectualcrafters.plot.util.NbtFactory.NbtCompound;
import com.intellectualcrafters.plot.util.NbtFactory.StreamOptions;
import com.intellectualcrafters.plot.util.TaskManager;
import com.intellectualcrafters.plot.uuid.OfflineUUIDWrapper;
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

    public static void startCaching() {
        implementation.startCaching();
    }

    public static void cache(final Map<StringWrapper, UUID> toAdd) {
        implementation.cache(toAdd);
    }

    public static UUID getUUID(final PlotPlayer player) {
        return implementation.getUUID(player);
    }

    public static UUID getUUID(final BukkitOfflinePlayer player) {
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
    
    public static UUID getUUID(final String name) {
        return implementation.getUUID(name);
    }

    public static Map<String, PlotPlayer> getPlayers() {
        return implementation.getPlayers();
    }

    public static void cacheWorld(String world) {
        implementation.cacheWorld(world);
    }

    public static void handleShutdown() {
        implementation.handleShutdown();
    }
}
