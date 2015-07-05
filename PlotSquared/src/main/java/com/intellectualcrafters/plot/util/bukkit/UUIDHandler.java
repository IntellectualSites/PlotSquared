package com.intellectualcrafters.plot.util.bukkit;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.UUID;

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
    /**
     * Map containing names and UUIDs
     *
     * @see com.google.common.collect.BiMap
     */
    private final static BiMap<StringWrapper, UUID> uuidMap = HashBiMap.create(new HashMap<StringWrapper, UUID>());
    public static boolean CACHED = false;
    public static UUIDWrapper uuidWrapper = null;
    public static HashMap<String, PlotPlayer> players = new HashMap<>();

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
        for (Plot plot : PS.get().getPlotsRaw()) {
            for (UUID uuid : plot.trusted) {
                uuids.add(uuid);
            }
            for (UUID uuid : plot.members) {
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
        final File container = Bukkit.getWorldContainer();
        UUIDHandler.CACHED = true;
        TaskManager.runTaskAsync(new Runnable() {
            @Override
            public void run() {
                PS.log(C.PREFIX.s() + "&6Starting player data caching for: " + world);
                final HashMap<StringWrapper, UUID> toAdd = new HashMap<>();
                toAdd.put(new StringWrapper("*"), DBFunc.everyone);
                if (Settings.TWIN_MODE_UUID) {
                    HashSet<UUID> all = getAllUUIDS();
                    PS.log("&aFast mod UUID caching enabled!");
                    final File playerdataFolder = new File(container, world + File.separator + "playerdata");
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
                                UUID uuid = UUID.fromString(s);
                                if (check || all.contains(uuid)) {
                                    File file = new File(playerdataFolder + File.separator + current);
                                    InputSupplier<FileInputStream> is = Files.newInputStreamSupplier(file);
                                    NbtCompound compound = NbtFactory.fromStream(is, StreamOptions.GZIP_COMPRESSION);
                                    NbtCompound bukkit = (NbtCompound) compound.get("bukkit");
                                    String name = (String) bukkit.get("lastKnownName");
                                    long last = (long) bukkit.get("lastPlayed");
                                    ExpireManager.dates.put(uuid, last);
                                    toAdd.put(new StringWrapper(name), uuid);
                                }
                            } catch (final Exception e) {
                                e.printStackTrace();
                                PS.log(C.PREFIX.s() + "Invalid playerdata: " + current);
                            }
                        }
                    }
                    cache(toAdd);
                    return;
                }
                final HashSet<String> worlds = new HashSet<>();
                worlds.add(world);
                worlds.add("world");
                final HashSet<UUID> uuids = new HashSet<>();
                final HashSet<String> names = new HashSet<>();
                File playerdataFolder = null;
                for (final String worldname : worlds) {
                    // Getting UUIDs
                    playerdataFolder = new File(container, worldname + File.separator + "playerdata");
                    String[] dat = playerdataFolder.list(new FilenameFilter() {
                        @Override
                        public boolean accept(final File f, final String s) {
                            return s.endsWith(".dat");
                        }
                    });
                    if (dat != null && dat.length != 0) {
                        for (final String current : dat) {
                            final String s = current.replaceAll(".dat$", "");
                            try {
                                final UUID uuid = UUID.fromString(s);
                                uuids.add(uuid);
                            } catch (final Exception e) {
                                PS.log(C.PREFIX.s() + "Invalid playerdata: " + current);
                            }
                        }
                        break;
                    }
                    // Getting names
                    final File playersFolder = new File(worldname + File.separator + "players");
                    dat = playersFolder.list(new FilenameFilter() {
                        @Override
                        public boolean accept(final File f, final String s) {
                            return s.endsWith(".dat");
                        }
                    });
                    if (dat != null && dat.length != 0) {
                        for (final String current : dat) {
                            names.add(current.replaceAll(".dat$", ""));
                        }
                        break;
                    }
                }
                for (UUID uuid : uuids) {
                    try {
                        File file = new File(playerdataFolder + File.separator + uuid.toString() + ".dat");
                        InputSupplier<FileInputStream> is = Files.newInputStreamSupplier(file);
                        NbtCompound compound = NbtFactory.fromStream(is, StreamOptions.GZIP_COMPRESSION);
                        NbtCompound bukkit = (NbtCompound) compound.get("bukkit");
                        String name = (String) bukkit.get("lastKnownName");
                        long last = (long) bukkit.get("lastPlayed");
                        if (Settings.OFFLINE_MODE) {
                            if (!Settings.UUID_LOWERCASE || !name.toLowerCase().equals(name)) {
                                long most = (long) compound.get("UUIDMost");
                                long least = (long) compound.get("UUIDLeast");
                                uuid = new UUID(most, least);
                            }
                        }
                        ExpireManager.dates.put(uuid, last);
                        toAdd.put(new StringWrapper(name), uuid);
                    } catch (final Throwable e) {
                        PS.log(C.PREFIX.s() + "&6Invalid playerdata: " + uuid.toString() + ".dat");
                    }
                }
                for (final String name : names) {
                    final UUID uuid = uuidWrapper.getUUID(name);
                    final StringWrapper nameWrap = new StringWrapper(name);
                    toAdd.put(nameWrap, uuid);
                }
                
                if (uuidMap.size() == 0) {
                    for (OfflinePlotPlayer op : uuidWrapper.getOfflinePlayers()) {
                        if (op.getLastPlayed() != 0) {
                            String name = op.getName();
                            StringWrapper wrap = new StringWrapper(name);
                            UUID uuid = uuidWrapper.getUUID(op);
                            toAdd.put(wrap, uuid);
                        }
                    }
                }
                cache(toAdd);
            }
        });
    }
    
    public static void cache(final HashMap<StringWrapper, UUID> toAdd) {
        TaskManager.runTask(new Runnable() {
            @Override
            public void run() {
                for (Entry<StringWrapper, UUID> entry : toAdd.entrySet()) {
                    add(entry.getKey(), entry.getValue());
                }
                PS.log(C.PREFIX.s() + "&6Cached a total of: " + UUIDHandler.uuidMap.size() + " UUIDs");
            }
        });
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
