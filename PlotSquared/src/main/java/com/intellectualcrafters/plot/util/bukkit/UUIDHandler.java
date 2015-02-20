package com.intellectualcrafters.plot.util.bukkit;

import java.io.File;
import java.io.FilenameFilter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.intellectualcrafters.plot.PlotSquared;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.config.Settings;
import com.intellectualcrafters.plot.database.DBFunc;
import com.intellectualcrafters.plot.object.StringWrapper;
import com.intellectualcrafters.plot.uuid.DefaultUUIDWrapper;
import com.intellectualcrafters.plot.uuid.OfflineUUIDWrapper;
import com.intellectualcrafters.plot.uuid.UUIDWrapper;

public class UUIDHandler {
    public static boolean CACHED = false;
    public static UUIDWrapper uuidWrapper = null;
    /**
     * Map containing names and UUIDs
     *
     * @see com.google.common.collect.BiMap
     */
    private final static BiMap<StringWrapper, UUID> uuidMap = HashBiMap.create(new HashMap<StringWrapper, UUID>());
    
    public static void add(final StringWrapper name, final UUID uuid) {
        if ((uuid == null) || (name == null)) {
            return;
        }
        if (!uuidMap.containsKey(name) && !uuidMap.inverse().containsKey(uuid)) {
            uuidMap.put(name, uuid);
        }
    }
    
    /**
     * Get the map containing all names/uuids
     *
     * @return map with names + uuids
     *
     * @see com.google.common.collect.BiMap
     */
    public static BiMap<StringWrapper, UUID> getUuidMap() {
        return uuidMap;
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
        return uuidMap.containsValue(uuid);
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
        return uuidMap.containsKey(name);
    }
    
    public static void cacheAll() {
        if (CACHED) {
            return;
        }
        PlotSquared.log(C.PREFIX.s() + "&6Starting player data caching");
        UUIDHandler.CACHED = true;
        final HashSet<String> worlds = new HashSet<>();
        worlds.add(Bukkit.getWorlds().get(0).getName());
        worlds.add("world");
        final HashSet<UUID> uuids = new HashSet<>();
        final HashSet<String> names = new HashSet<>();
        for (final String worldname : worlds) {
            // Getting UUIDs
            final File playerdataFolder = new File(worldname + File.separator + "playerdata");
            String[] dat = playerdataFolder.list(new FilenameFilter() {
                @Override
                public boolean accept(final File f, final String s) {
                    return s.endsWith(".dat");
                }
            });
            if (dat != null) {
                for (final String current : dat) {
                    final String s = current.replaceAll(".dat$", "");
                    try {
                        final UUID uuid = UUID.fromString(s);
                        uuids.add(uuid);
                    } catch (final Exception e) {
                        PlotSquared.log(C.PREFIX.s() + "Invalid playerdata: " + current);
                    }
                }
            }
            // Getting names
            final File playersFolder = new File(worldname + File.separator + "players");
            dat = playersFolder.list(new FilenameFilter() {
                @Override
                public boolean accept(final File f, final String s) {
                    return s.endsWith(".dat");
                }
            });
            if (dat != null) {
                for (final String current : dat) {
                    names.add(current.replaceAll(".dat$", ""));
                }
            }
        }
        final UUIDWrapper wrapper = new DefaultUUIDWrapper();
        for (UUID uuid : uuids) {
            try {
                final OfflinePlayer player = wrapper.getOfflinePlayer(uuid);
                uuid = UUIDHandler.uuidWrapper.getUUID(player);
                final StringWrapper name = new StringWrapper(player.getName());
                add(name, uuid);
            } catch (final Throwable e) {
                PlotSquared.log(C.PREFIX.s() + "&6Invalid playerdata: " + uuid.toString() + ".dat");
            }
        }
        for (final String name : names) {
            final OfflinePlayer player = Bukkit.getOfflinePlayer(name);
            final UUID uuid = UUIDHandler.uuidWrapper.getUUID(player);
            final StringWrapper nameWrap = new StringWrapper(name);
            add(nameWrap, uuid);
        }
        // add the Everyone '*' UUID
        add(new StringWrapper("*"), DBFunc.everyone);
        PlotSquared.log(C.PREFIX.s() + "&6Cached a total of: " + UUIDHandler.uuidMap.size() + " UUIDs");
    }
    
    public static UUID getUUID(final Player player) {
        return UUIDHandler.uuidWrapper.getUUID(player);
    }
    
    public static UUID getUUID(final OfflinePlayer player) {
        return UUIDHandler.uuidWrapper.getUUID(player);
    }
    
    public static String getName(final UUID uuid) {
        if (uuid == null) {
            return null;
        }
        // check online
        final Player player = uuidWrapper.getPlayer(uuid);
        if (player != null) {
            return player.getName();
        }
        // check cache
        final StringWrapper name = UUIDHandler.uuidMap.inverse().get(uuid);
        if (name != null) {
            return name.value;
        }
        return null;
    }
    
    public static UUID getUUID(final String name) {
        if ((name == null) || (name.length() == 0)) {
            return null;
        }
        // check online
        final Player player = Bukkit.getPlayer(name);
        if (player != null) {
            final UUID uuid = UUIDHandler.uuidWrapper.getUUID(player);
            add(new StringWrapper(name), uuid);
            return uuid;
        }
        // check cache
        final StringWrapper wrap = new StringWrapper(name);
        UUID uuid = UUIDHandler.uuidMap.get(wrap);
        if (uuid != null) {
            return uuid;
        }
        // Read from disk OR convert directly to offline UUID
        if (Settings.UUID_FROM_DISK || (uuidWrapper instanceof OfflineUUIDWrapper)) {
            uuid = UUIDHandler.uuidWrapper.getUUID(name);
            add(new StringWrapper(name), uuid);
            return uuid;
        }
        return null;
    }
}
