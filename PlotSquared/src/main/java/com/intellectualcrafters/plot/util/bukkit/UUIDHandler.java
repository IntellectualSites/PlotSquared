package com.intellectualcrafters.plot.util.bukkit;

import java.io.File;
import java.io.FilenameFilter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.intellectualcrafters.plot.PlotSquared;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.config.Settings;
import com.intellectualcrafters.plot.database.DBFunc;
import com.intellectualcrafters.plot.object.BukkitOfflinePlayer;
import com.intellectualcrafters.plot.object.OfflinePlotPlayer;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.object.StringWrapper;
import com.intellectualcrafters.plot.uuid.DefaultUUIDWrapper;
import com.intellectualcrafters.plot.uuid.OfflineUUIDWrapper;
import com.intellectualcrafters.plot.uuid.UUIDWrapper;

public class UUIDHandler {
    public static boolean CACHED = false;
    public static UUIDWrapper uuidWrapper = null;
    public static HashMap<String, PlotPlayer> players = new HashMap<>();

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
        BiMap<UUID, StringWrapper> inverse = uuidMap.inverse();
        if (inverse.containsKey(uuid)) {
            if (uuidMap.containsKey(name)) {
                return;
            }
            inverse.remove(uuid);
        }
        uuidMap.put(name, uuid);
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
    
    public static HashSet<UUID> getAllUUIDS() {
        HashSet<UUID> uuids = new HashSet<UUID>();
        for (Plot plot : PlotSquared.getPlotsRaw()) {
            for (UUID uuid : plot.helpers) {
                uuids.add(uuid);
            }
            for (UUID uuid : plot.trusted) {
                uuids.add(uuid);
            }
            for (UUID uuid : plot.denied) {
                uuids.add(uuid);
            }
            if (plot.owner != null) {
                uuids.add(plot.owner);
            }
        }
        return uuids;
    }

    public static void cacheAll(final String world) {
        if (CACHED) {
            return;
        }
        PlotSquared.log(C.PREFIX.s() + "&6Starting player data caching: " + world);
        UUIDHandler.CACHED = true;
        add(new StringWrapper("*"), DBFunc.everyone);
        if (Settings.TWIN_MODE_UUID) {
            HashSet<UUID> all = getAllUUIDS();
            final File playerdataFolder = new File(Bukkit.getWorldContainer(), world + File.separator + "playerdata");
            String[] dat = playerdataFolder.list(new FilenameFilter() {
                @Override
                public boolean accept(final File f, final String s) {
                    return s.endsWith(".dat");
                }
            });
            boolean check = all.size() == 0;
            if (dat != null) {
                for (final String current : dat) {
                    final String s = current.replaceAll(".dat$", "");
                    try {
                        final UUID uuid = UUID.fromString(s);
                        if (check || all.contains(uuid)) {
                            OfflinePlayer op = Bukkit.getOfflinePlayer(uuid);
                            add(new StringWrapper(op.getName()), uuid);
                        }
                    } catch (final Exception e) {
                        PlotSquared.log(C.PREFIX.s() + "Invalid playerdata: " + current);
                    }
                }
            }
            PlotSquared.log(C.PREFIX.s() + "&6Cached a total of: " + UUIDHandler.uuidMap.size() + " UUIDs");
            return;
        }
        final HashSet<String> worlds = new HashSet<>();
        worlds.add(world);
        worlds.add("world");
        final HashSet<UUID> uuids = new HashSet<>();
        final HashSet<String> names = new HashSet<>();
        for (final String worldname : worlds) {
            // Getting UUIDs
            final File playerdataFolder = new File(Bukkit.getWorldContainer(), worldname + File.separator + "playerdata");
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
                final OfflinePlotPlayer player = wrapper.getOfflinePlayer(uuid);
                uuid = UUIDHandler.uuidWrapper.getUUID(player);
                final StringWrapper name = new StringWrapper(player.getName());
                add(name, uuid);
            } catch (final Throwable e) {
                PlotSquared.log(C.PREFIX.s() + "&6Invalid playerdata: " + uuid.toString() + ".dat");
            }
        }
        for (final String name : names) {
            final UUID uuid = uuidWrapper.getUUID(name);
            final StringWrapper nameWrap = new StringWrapper(name);
            add(nameWrap, uuid);
        }
        
        
        if (uuidMap.size() == 0) {
            for (OfflinePlotPlayer op : uuidWrapper.getOfflinePlayers()) {
                if (op.getLastPlayed() != 0) {
                    String name = op.getName();
                    StringWrapper wrap = new StringWrapper(name);
                    UUID uuid = uuidWrapper.getUUID(op);
                    add(wrap, uuid);
                }
            }
        }
        PlotSquared.log(C.PREFIX.s() + "&6Cached a total of: " + UUIDHandler.uuidMap.size() + " UUIDs");
    }

    public static UUID getUUID(final PlotPlayer player) {
        return UUIDHandler.uuidWrapper.getUUID(player);
    }

    public static UUID getUUID(final BukkitOfflinePlayer player) {
        return UUIDHandler.uuidWrapper.getUUID(player);
    }

    public static String getName(final UUID uuid) {
        if (uuid == null) {
            return null;
        }
        // check online
        final PlotPlayer player = UUIDHandler.getPlayer(uuid);
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

    public static PlotPlayer getPlayer(final UUID uuid) {
        for (final PlotPlayer player : players.values()) {
            if (player.getUUID().equals(uuid)) {
                return player;
            }
        }
        return null;
    }

    public static PlotPlayer getPlayer(final String name) {
        return players.get(name);
    }
    
    public static UUID getUUID(final String name) {
        if ((name == null) || (name.length() == 0)) {
            return null;
        }
        // check online
        final PlotPlayer player = getPlayer(name);
        if (player != null) {
            return player.getUUID();
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
