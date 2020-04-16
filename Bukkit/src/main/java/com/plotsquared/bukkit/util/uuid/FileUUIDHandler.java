/*
 *       _____  _       _    _____                                _
 *      |  __ \| |     | |  / ____|                              | |
 *      | |__) | | ___ | |_| (___   __ _ _   _  __ _ _ __ ___  __| |
 *      |  ___/| |/ _ \| __|\___ \ / _` | | | |/ _` | '__/ _ \/ _` |
 *      | |    | | (_) | |_ ____) | (_| | |_| | (_| | | |  __/ (_| |
 *      |_|    |_|\___/ \__|_____/ \__, |\__,_|\__,_|_|  \___|\__,_|
 *                                    | |
 *                                    |_|
 *            PlotSquared plot management system for Minecraft
 *                  Copyright (C) 2020 IntellectualSites
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.plotsquared.bukkit.util.uuid;

import com.google.common.collect.HashBiMap;
import com.google.common.collect.Sets;
import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.configuration.Captions;
import com.plotsquared.core.configuration.Settings;
import com.plotsquared.core.player.OfflinePlotPlayer;
import com.plotsquared.core.plot.expiration.ExpireManager;
import com.plotsquared.core.util.StringMan;
import com.plotsquared.core.util.StringWrapper;
import com.plotsquared.core.util.task.RunnableVal;
import com.plotsquared.core.util.task.TaskManager;
import com.plotsquared.core.util.uuid.UUIDHandler;
import com.plotsquared.core.util.uuid.UUIDHandlerImplementation;
import com.plotsquared.core.util.uuid.UUIDWrapper;
import com.sk89q.jnbt.CompoundTag;
import com.sk89q.jnbt.NBTInputStream;
import com.sk89q.jnbt.Tag;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.zip.GZIPInputStream;

public class FileUUIDHandler extends UUIDHandlerImplementation {

    public FileUUIDHandler(UUIDWrapper wrapper) {
        super(wrapper);
    }

    @Override public boolean startCaching(Runnable whenDone) {
        return super.startCaching(whenDone) && cache(whenDone);
    }

    private Tag readTag(File file) throws IOException {
        // Don't chain the creation of the GZIP stream and the NBT stream, because their
        // constructors may throw an IOException.
        try (BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(file));
            GZIPInputStream gzipInputStream = new GZIPInputStream(inputStream);
            NBTInputStream nbtInputStream = new NBTInputStream(gzipInputStream)) {
            return nbtInputStream.readNamedTag().getTag();
        }
    }

    public boolean cache(final Runnable whenDone) {
        final File container = Bukkit.getWorldContainer();
        List<World> worlds = Bukkit.getWorlds();
        final String world;
        if (worlds.isEmpty()) {
            world = "world";
        } else {
            world = worlds.get(0).getName();
        }
        TaskManager.runTaskAsync(() -> {
            PlotSquared.debug(Captions.PREFIX + "Starting player data caching for: " + world);
            File uuidFile = new File(PlotSquared.get().IMP.getDirectory(), "uuids.txt");
            if (uuidFile.exists()) {
                try {
                    List<String> lines =
                        Files.readAllLines(uuidFile.toPath(), StandardCharsets.UTF_8);
                    for (String line : lines) {
                        try {
                            line = line.trim();
                            if (line.isEmpty()) {
                                continue;
                            }
                            line = line.replaceAll("[\\|][0-9]+[\\|][0-9]+[\\|]", "");
                            String[] split = line.split("\\|");
                            String name = split[0];
                            if (name.isEmpty() || (name.length() > 16) || !StringMan
                                .isAlphanumericUnd(name)) {
                                continue;
                            }
                            UUID uuid = FileUUIDHandler.this.uuidWrapper.getUUID(name);
                            if (uuid == null) {
                                continue;
                            }
                            UUIDHandler.add(new StringWrapper(name), uuid);
                        } catch (Exception e2) {
                            e2.printStackTrace();
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            HashBiMap<StringWrapper, UUID> toAdd = HashBiMap.create(new HashMap<>());
            if (Settings.UUID.NATIVE_UUID_PROVIDER) {
                HashSet<UUID> all = UUIDHandler.getAllUUIDS();
                PlotSquared.debug("Fast mode UUID caching enabled!");
                File playerDataFolder = new File(container, world + File.separator + "playerdata");
                String[] dat = playerDataFolder.list(new DatFileFilter());
                boolean check = all.isEmpty();
                if (dat != null) {
                    for (String current : dat) {
                        String s = current.replaceAll(".dat$", "");
                        try {
                            UUID uuid = UUID.fromString(s);
                            if (check || all.remove(uuid)) {
                                File file = new File(playerDataFolder, current);
                                CompoundTag compound = (CompoundTag) readTag(file);
                                if (!compound.containsKey("bukkit")) {
                                    PlotSquared.debug("ERROR: Player data (" + uuid.toString()
                                        + ".dat) does not contain the the key \"bukkit\"");
                                } else {
                                    Map<String, Tag> compoundMap = compound.getValue();
                                    CompoundTag bukkit = (CompoundTag) compoundMap.get("bukkit");
                                    Map<String, Tag> bukkitMap = bukkit.getValue();
                                    String name =
                                        (String) bukkitMap.get("lastKnownName").getValue();
                                    long last = (long) bukkitMap.get("lastPlayed").getValue();
                                    long first = (long) bukkitMap.get("firstPlayed").getValue();
                                    if (ExpireManager.IMP != null) {
                                        ExpireManager.IMP.storeDate(uuid, last);
                                        ExpireManager.IMP.storeAccountAge(uuid, last - first);
                                    }
                                    toAdd.put(new StringWrapper(name), uuid);
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            PlotSquared.debug(Captions.PREFIX + "Invalid playerdata: " + current);
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
                    PlotSquared.debug(
                        "Failed to cache: " + all.size() + " uuids - slowly processing all files");
                }
            }
            HashSet<String> worlds1 = Sets.newHashSet(world, "world");
            HashSet<UUID> uuids = new HashSet<>();
            HashSet<String> names = new HashSet<>();
            File playerDataFolder = null;
            for (String worldName : worlds1) {
                // Getting UUIDs
                playerDataFolder = new File(container, worldName + File.separator + "playerdata");
                String[] dat = playerDataFolder.list(new DatFileFilter());
                if ((dat != null) && (dat.length != 0)) {
                    for (String current : dat) {
                        String s = current.replaceAll(".dat$", "");
                        try {
                            UUID uuid = UUID.fromString(s);
                            uuids.add(uuid);
                        } catch (Exception ignored) {
                            PlotSquared.debug(Captions.PREFIX + "Invalid PlayerData: " + current);
                        }
                    }
                    break;
                }
                // Getting names
                File playersFolder = new File(worldName + File.separator + "players");
                dat = playersFolder.list(new DatFileFilter());
                if ((dat != null) && (dat.length != 0)) {
                    for (String current : dat) {
                        names.add(current.replaceAll(".dat$", ""));
                    }
                    break;
                }
            }
            for (UUID uuid : uuids) {
                try {
                    File file =
                        new File(playerDataFolder + File.separator + uuid.toString() + ".dat");
                    if (!file.exists()) {
                        continue;
                    }
                    CompoundTag compound = (CompoundTag) readTag(file);
                    if (!compound.containsKey("bukkit")) {
                        PlotSquared.debug("ERROR: Player data (" + uuid.toString()
                            + ".dat) does not contain the the key \"bukkit\"");
                    } else {
                        Map<String, Tag> compoundMap = compound.getValue();
                        CompoundTag bukkit = (CompoundTag) compoundMap.get("bukkit");
                        Map<String, Tag> bukkitMap = bukkit.getValue();
                        String name = (String) bukkitMap.get("lastKnownName").getValue();
                        StringWrapper wrap = new StringWrapper(name);
                        if (!toAdd.containsKey(wrap)) {
                            long last = (long) bukkitMap.get("lastPlayed").getValue();
                            long first = (long) bukkitMap.get("firstPlayed").getValue();
                            if (Settings.UUID.OFFLINE) {
                                if (Settings.UUID.FORCE_LOWERCASE && !name.toLowerCase()
                                    .equals(name)) {
                                    uuid = FileUUIDHandler.this.uuidWrapper.getUUID(name);
                                } else {
                                    long most = (long) compoundMap.get("UUIDMost").getValue();
                                    long least = (long) compoundMap.get("UUIDLeast").getValue();
                                    uuid = new UUID(most, least);
                                }
                            }
                            if (ExpireManager.IMP != null) {
                                ExpireManager.IMP.storeDate(uuid, last);
                                ExpireManager.IMP.storeAccountAge(uuid, last - first);
                            }
                            toAdd.put(wrap, uuid);
                        }
                    }
                } catch (Exception ignored) {
                    PlotSquared.debug(
                        Captions.PREFIX + "&6Invalid PlayerData: " + uuid.toString() + ".dat");
                }
            }
            for (String name : names) {
                UUID uuid = FileUUIDHandler.this.uuidWrapper.getUUID(name);
                StringWrapper nameWrap = new StringWrapper(name);
                toAdd.put(nameWrap, uuid);
            }

            if (getUUIDMap().isEmpty()) {
                for (OfflinePlotPlayer offlinePlotPlayer : FileUUIDHandler.this.uuidWrapper
                    .getOfflinePlayers()) {
                    long last = offlinePlotPlayer.getLastPlayed();
                    if (last != 0) {
                        String name = offlinePlotPlayer.getName();
                        StringWrapper wrap = new StringWrapper(name);
                        if (!toAdd.containsKey(wrap)) {
                            UUID uuid = FileUUIDHandler.this.uuidWrapper.getUUID(offlinePlotPlayer);
                            if (toAdd.containsValue(uuid)) {
                                StringWrapper duplicate = toAdd.inverse().get(uuid);
                                PlotSquared.debug(
                                    "The UUID: " + uuid.toString() + " is already mapped to "
                                        + duplicate
                                        + "\n It cannot be added to the Map with a key of " + wrap);
                            }
                            toAdd.putIfAbsent(wrap, uuid);
                            if (ExpireManager.IMP != null) {
                                ExpireManager.IMP.storeDate(uuid, last);
                            }
                        }
                    }
                }
            }
            add(toAdd);
            if (whenDone != null) {
                whenDone.run();
            }
        });
        return true;
    }

    @Override public void fetchUUID(final String name, final RunnableVal<UUID> ifFetch) {
        TaskManager.runTaskAsync(() -> {
            ifFetch.value = FileUUIDHandler.this.uuidWrapper.getUUID(name);
            TaskManager.runTask(ifFetch);
        });
    }
}
