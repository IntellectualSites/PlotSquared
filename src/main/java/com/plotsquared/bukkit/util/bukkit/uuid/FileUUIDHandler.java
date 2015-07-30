package com.plotsquared.bukkit.util.bukkit.uuid;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.World;

import com.google.common.collect.HashBiMap;
import com.google.common.io.Files;
import com.google.common.io.InputSupplier;
import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.config.Settings;
import com.intellectualcrafters.plot.database.DBFunc;
import com.intellectualcrafters.plot.object.OfflinePlotPlayer;
import com.intellectualcrafters.plot.object.RunnableVal;
import com.intellectualcrafters.plot.object.StringWrapper;
import com.intellectualcrafters.plot.util.ExpireManager;
import com.intellectualcrafters.plot.util.NbtFactory;
import com.intellectualcrafters.plot.util.TaskManager;
import com.intellectualcrafters.plot.util.UUIDHandler;
import com.intellectualcrafters.plot.util.UUIDHandlerImplementation;
import com.intellectualcrafters.plot.uuid.UUIDWrapper;

public class FileUUIDHandler extends UUIDHandlerImplementation {

    public FileUUIDHandler(UUIDWrapper wrapper) {
        super(wrapper);
    }
    
    @Override
    public boolean startCaching(Runnable whenDone) {
        if (!super.startCaching(whenDone)) {
            return false;
        }
        return cache(whenDone);
    }
    
    public boolean cache(final Runnable whenDone) {
        final File container = Bukkit.getWorldContainer();
        List<World> worlds = Bukkit.getWorlds();
        final String world;
        if (worlds.size() == 0) {
            world = "world";
        }
        else {
            world = worlds.get(0).getName();
        }
        TaskManager.runTaskAsync(new Runnable() {
            @Override
            public void run() {
                PS.debug(C.PREFIX.s() + "&6Starting player data caching for: " + world);
                final HashBiMap<StringWrapper, UUID> toAdd = HashBiMap.create(new HashMap<StringWrapper, UUID>());
                toAdd.put(new StringWrapper("*"), DBFunc.everyone);
                if (Settings.TWIN_MODE_UUID) {
                    HashSet<UUID> all = UUIDHandler.getAllUUIDS();
                    PS.debug("&aFast mode UUID caching enabled!");
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
                                    NbtFactory.NbtCompound compound = NbtFactory.fromStream(is, NbtFactory.StreamOptions.GZIP_COMPRESSION);
                                    NbtFactory.NbtCompound bukkit = (NbtFactory.NbtCompound) compound.get("bukkit");
                                    String name = (String) bukkit.get("lastKnownName");
                                    long last = (long) bukkit.get("lastPlayed");
                                    ExpireManager.dates.put(uuid, last);
                                    toAdd.put(new StringWrapper(name), uuid);
                                }
                            } catch (final Exception e) {
                                e.printStackTrace();
                                PS.debug(C.PREFIX.s() + "Invalid playerdata: " + current);
                            }
                        }
                    }
                    add(toAdd);
                    if (whenDone != null) whenDone.run();
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
                                PS.debug(C.PREFIX.s() + "Invalid playerdata: " + current);
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
                        NbtFactory.NbtCompound compound = NbtFactory.fromStream(is, NbtFactory.StreamOptions.GZIP_COMPRESSION);
                        NbtFactory.NbtCompound bukkit = (NbtFactory.NbtCompound) compound.get("bukkit");
                        String name = (String) bukkit.get("lastKnownName");
                        long last = (long) bukkit.get("lastPlayed");
                        if (Settings.OFFLINE_MODE) {
                            if (Settings.UUID_LOWERCASE && !name.toLowerCase().equals(name)) {
                                uuid = uuidWrapper.getUUID(name);
                            } else {
                                long most = (long) compound.get("UUIDMost");
                                long least = (long) compound.get("UUIDLeast");
                                uuid = new UUID(most, least);
                            }
                        }
                        ExpireManager.dates.put(uuid, last);
                        toAdd.put(new StringWrapper(name), uuid);
                    } catch (final Throwable e) {
                        PS.debug(C.PREFIX.s() + "&6Invalid playerdata: " + uuid.toString() + ".dat");
                    }
                }
                for (final String name : names) {
                    final UUID uuid = uuidWrapper.getUUID(name);
                    final StringWrapper nameWrap = new StringWrapper(name);
                    toAdd.put(nameWrap, uuid);
                }

                if (getUUIDMap().size() == 0) {
                    for (OfflinePlotPlayer op : uuidWrapper.getOfflinePlayers()) {
                        if (op.getLastPlayed() != 0) {
                            String name = op.getName();
                            StringWrapper wrap = new StringWrapper(name);
                            UUID uuid = uuidWrapper.getUUID(op);
                            toAdd.put(wrap, uuid);
                        }
                    }
                }
                add(toAdd);
                if (whenDone != null) whenDone.run();
            }
        });
        return true;
    }

    @Override
    public void fetchUUID(final String name, final RunnableVal<UUID> ifFetch) {
        TaskManager.runTaskAsync(new Runnable() {
            @Override
            public void run() {
                ifFetch.value = uuidWrapper.getUUID(name);
                TaskManager.runTask(ifFetch);
            }
        });
    }
}
