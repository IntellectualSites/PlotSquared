package com.plotsquared.bukkit.uuid;

import com.google.common.collect.HashBiMap;
import com.google.common.io.ByteSource;
import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.config.Settings;
import com.intellectualcrafters.plot.database.DBFunc;
import com.intellectualcrafters.plot.object.OfflinePlotPlayer;
import com.intellectualcrafters.plot.object.RunnableVal;
import com.intellectualcrafters.plot.object.StringWrapper;
import com.intellectualcrafters.plot.util.ExpireManager;
import com.intellectualcrafters.plot.util.StringMan;
import com.intellectualcrafters.plot.util.TaskManager;
import com.intellectualcrafters.plot.util.UUIDHandler;
import com.intellectualcrafters.plot.util.UUIDHandlerImplementation;
import com.intellectualcrafters.plot.uuid.UUIDWrapper;
import com.plotsquared.bukkit.util.NbtFactory;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

public class FileUUIDHandler extends UUIDHandlerImplementation {
    
    public FileUUIDHandler(final UUIDWrapper wrapper) {
        super(wrapper);
    }
    
    @Override
    public boolean startCaching(final Runnable whenDone) {
        return super.startCaching(whenDone) && cache(whenDone);
    }
    
    public boolean cache(final Runnable whenDone) {
        final File container = Bukkit.getWorldContainer();
        final List<World> worlds = Bukkit.getWorlds();
        final String world;
        if (worlds.isEmpty()) {
            world = "world";
        } else {
            world = worlds.get(0).getName();
        }
        TaskManager.runTaskAsync(new Runnable() {
            @Override
            public void run() {
                PS.debug(C.PREFIX.s() + "&6Starting player data caching for: " + world);
                final File uuidfile = new File(PS.get().IMP.getDirectory(), "uuids.txt");
                if (uuidfile.exists()) {
                    try {
                        final List<String> lines = Files.readAllLines(uuidfile.toPath(), StandardCharsets.UTF_8);
                        for (String line : lines) {
                            try {
                                line = line.trim();
                                if (line.isEmpty()) {
                                    continue;
                                }
                                line = line.replaceAll("[\\|][0-9]+[\\|][0-9]+[\\|]", "");
                                final String[] split = line.split("\\|");
                                final String name = split[0];
                                if ((name.isEmpty()) || (name.length() > 16) || !StringMan.isAlphanumericUnd(name)) {
                                    continue;
                                }
                                final UUID uuid = uuidWrapper.getUUID(name);
                                if (uuid == null) {
                                    continue;
                                }
                                UUIDHandler.add(new StringWrapper(name), uuid);
                            } catch (final Exception e2) {
                                e2.printStackTrace();
                            }
                        }
                    } catch (final IOException e) {
                        e.printStackTrace();
                    }
                }
                if (Settings.TWIN_MODE_UUID) {
                    final HashBiMap<StringWrapper, UUID> toAdd = HashBiMap.create(new HashMap<StringWrapper, UUID>());
                    toAdd.put(new StringWrapper("*"), DBFunc.everyone);
                    final HashSet<UUID> all = UUIDHandler.getAllUUIDS();
                    PS.debug("&aFast mode UUID caching enabled!");
                    final File playerdataFolder = new File(container, world + File.separator + "playerdata");
                    final String[] dat = playerdataFolder.list(new FilenameFilter() {
                        @Override
                        public boolean accept(final File f, final String s) {
                            return s.endsWith(".dat");
                        }
                    });
                    final boolean check = all.isEmpty();
                    if (dat != null) {
                        for (final String current : dat) {
                            final String s = current.replaceAll(".dat$", "");
                            try {
                                final UUID uuid = UUID.fromString(s);
                                if (check || all.remove(uuid)) {
                                    final File file = new File(playerdataFolder + File.separator + current);
                                    final ByteSource is = com.google.common.io.Files.asByteSource(file);
                                    final NbtFactory.NbtCompound compound = NbtFactory.fromStream(is, NbtFactory.StreamOptions.GZIP_COMPRESSION);
                                    final NbtFactory.NbtCompound bukkit = (NbtFactory.NbtCompound) compound.get("bukkit");
                                    final String name = (String) bukkit.get("lastKnownName");
                                    final long last = (long) bukkit.get("lastPlayed");
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
                    if (all.isEmpty()) {
                        if (whenDone != null) {
                            whenDone.run();
                        }
                        return;
                    } else {
                        PS.debug("Failed to cache: " + all.size() + " uuids - slowly processing all files");
                    }
                }
                final HashBiMap<StringWrapper, UUID> toAdd = HashBiMap.create(new HashMap<StringWrapper, UUID>());
                toAdd.put(new StringWrapper("*"), DBFunc.everyone);
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
                    if ((dat != null) && (dat.length != 0)) {
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
                    if ((dat != null) && (dat.length != 0)) {
                        for (final String current : dat) {
                            names.add(current.replaceAll(".dat$", ""));
                        }
                        break;
                    }
                }
                for (UUID uuid : uuids) {
                    try {
                        final File file = new File(playerdataFolder + File.separator + uuid.toString() + ".dat");
                        if (!file.exists()) {
                            continue;
                        }
                        final ByteSource is = com.google.common.io.Files.asByteSource(file);
                        final NbtFactory.NbtCompound compound = NbtFactory.fromStream(is, NbtFactory.StreamOptions.GZIP_COMPRESSION);
                        final NbtFactory.NbtCompound bukkit = (NbtFactory.NbtCompound) compound.get("bukkit");
                        final String name = (String) bukkit.get("lastKnownName");
                        final long last = (long) bukkit.get("lastPlayed");
                        if (Settings.OFFLINE_MODE) {
                            if (Settings.UUID_LOWERCASE && !name.toLowerCase().equals(name)) {
                                uuid = uuidWrapper.getUUID(name);
                            } else {
                                final long most = (long) compound.get("UUIDMost");
                                final long least = (long) compound.get("UUIDLeast");
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

                if (getUUIDMap().isEmpty()) {
                    for (final OfflinePlotPlayer op : uuidWrapper.getOfflinePlayers()) {
                        final long last = op.getLastPlayed();
                        if (last != 0) {
                            final String name = op.getName();
                            final StringWrapper wrap = new StringWrapper(name);
                            final UUID uuid = uuidWrapper.getUUID(op);
                            toAdd.put(wrap, uuid);
                            ExpireManager.dates.put(uuid, last);
                        }
                    }
                }
                add(toAdd);
                if (whenDone != null) {
                    whenDone.run();
                }
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
