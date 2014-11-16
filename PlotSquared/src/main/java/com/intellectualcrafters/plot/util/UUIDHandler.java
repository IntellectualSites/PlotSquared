////////////////////////////////////////////////////////////////////////////////////////////////////
// PlotSquared - A plot manager and world generator for the Bukkit API                             /
// Copyright (c) 2014 IntellectualSites/IntellectualCrafters                                       /
//                                                                                                 /
// This program is free software; you can redistribute it and/or modify                            /
// it under the terms of the GNU General Public License as published by                            /
// the Free Software Foundation; either version 3 of the License, or                               /
// (at your option) any later version.                                                             /
//                                                                                                 /
// This program is distributed in the hope that it will be useful,                                 /
// but WITHOUT ANY WARRANTY; without even the implied warranty of                                  /
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the                                   /
// GNU General Public License for more details.                                                    /
//                                                                                                 /
// You should have received a copy of the GNU General Public License                               /
// along with this program; if not, write to the Free Software Foundation,                         /
// Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301  USA                               /
//                                                                                                 /
// You can contact us via: support@intellectualsites.com                                           /
////////////////////////////////////////////////////////////////////////////////////////////////////

package com.intellectualcrafters.plot.util;

import com.google.common.base.Charsets;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.intellectualcrafters.plot.PlotMain;
import com.intellectualcrafters.plot.config.Settings;
import com.intellectualcrafters.plot.object.StringWrapper;
import com.intellectualcrafters.plot.uuid.NameFetcher;
import com.intellectualcrafters.plot.uuid.UUIDFetcher;
import com.intellectualcrafters.plot.uuid.UUIDSaver;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.HashMap;
import java.util.UUID;

/**
 * This class can be used to efficiently translate UUIDs and names back and
 * forth.
 * It uses three primary methods of achieving this:
 * - Read From Cache
 * - Read from OfflinePlayer objects
 * - Read from (if onlinemode: mojang api) (else: playername hashing)
 * All UUIDs/Usernames will be stored in a map (cache) until the server is
 * restarted.
 * <p/>
 * You can use getUuidMap() to save the uuids/names to a file (SQLite db for
 * example).
 * Primary methods: getUUID(String name) & getName(UUID uuid) <-- You should
 * ONLY use these.
 * Call startFetch(JavaPlugin plugin) in your onEnable().
 * <p/>
 * Originally created by:
 *
 * @author Citymonstret
 * @author Empire92
 *         for PlotSquared.
 */
@SuppressWarnings("unused")
public class UUIDHandler {

    /**
     * Online mode
     *
     * @see org.bukkit.Server#getOnlineMode()
     */
    private static boolean online = Bukkit.getServer().getOnlineMode();

    /**
     * Map containing names and UUID's
     */
    private static BiMap<StringWrapper, UUID> uuidMap = HashBiMap.create(new HashMap<StringWrapper, UUID>());

    /**
     * Get the map containing all names/uuids
     *
     * @return map with names + uuids
     */
    public static BiMap<StringWrapper, UUID> getUuidMap() {
        return uuidMap;
    }

    /**
     * Check if a uuid is cached
     *
     * @param uuid to check
     * @return true of the uuid is cached
     */
    public static boolean uuidExists(final UUID uuid) {
        return uuidMap.containsValue(uuid);
    }

    /**
     * Check if a name is cached
     *
     * @param name to check
     * @return true of the name is cached
     */
    public static boolean nameExists(final StringWrapper name) {
        return uuidMap.containsKey(name);
    }

    /**
     * Add a set to the cache
     *
     * @param name to cache
     * @param uuid to cache
     */
    public static void add(final StringWrapper name, final UUID uuid) {
        if (!uuidMap.containsKey(name) && !uuidMap.inverse().containsKey(uuid)) {
            uuidMap.put(name, uuid);
        }
    }

    /**
     * @param name to use as key
     * @return uuid
     */
    public static UUID getUUID(final String name) {
        final StringWrapper nameWrap = new StringWrapper(name);
        if (uuidMap.containsKey(nameWrap)) {
            return uuidMap.get(nameWrap);
        }
        @SuppressWarnings("deprecation")
        final Player player = Bukkit.getPlayer(name);
        if (player != null) {
            final UUID uuid = player.getUniqueId();
            add(nameWrap, uuid);
            return uuid;
        }
        UUID uuid;
        if (online) {
            if (Settings.CUSTOM_API) {
                if ((uuid = getUuidOnlinePlayer(nameWrap)) != null) {
                    return uuid;
                }
                try {
                    return PlotMain.getUUIDSaver().mojangUUID(name);
                } catch (final Exception e) {
                    try {
                        final UUIDFetcher fetcher = new UUIDFetcher(Arrays.asList(name));
                        uuid = fetcher.call().get(name);
                        add(nameWrap, uuid);
                    } catch (final Exception ex) {
                        ex.printStackTrace();
                    }
                }
            } else {
                try {
                    final UUIDFetcher fetcher = new UUIDFetcher(Arrays.asList(name));
                    uuid = fetcher.call().get(name);
                    add(nameWrap, uuid);
                } catch (final Exception ex) {
                    ex.printStackTrace();
                }
            }
        } else {
            return getUuidOfflineMode(nameWrap);
        }
        return null;
    }

    /**
     * @param uuid to use as key
     * @return name (cache)
     */
    private static StringWrapper loopSearch(final UUID uuid) {
        return uuidMap.inverse().get(uuid);
    }

    /**
     * @param uuid to use as key
     * @return Name
     */
    public static String getName(final UUID uuid) {
        if (uuidExists(uuid)) {
            return loopSearch(uuid).value;
        }
        String name;
        if ((name = getNameOnlinePlayer(uuid)) != null) {
            return name;
        }
        if ((name = getNameOfflinePlayer(uuid)) != null) {
            return name;
        }
        if (online) {
            if (!Settings.CUSTOM_API) {
                try {
                    final NameFetcher fetcher = new NameFetcher(Arrays.asList(uuid));
                    name = fetcher.call().get(uuid);
                    add(new StringWrapper(name), uuid);
                    return name;
                } catch (final Exception ex) {
                    ex.printStackTrace();
                }
            } else {
                try {
                    return PlotMain.getUUIDSaver().mojangName(uuid);
                } catch (final Exception e) {
                    try {
                        final NameFetcher fetcher = new NameFetcher(Arrays.asList(uuid));
                        name = fetcher.call().get(uuid);
                        add(new StringWrapper(name), uuid);
                        return name;
                    } catch (final Exception ex) {
                        e.printStackTrace();
                    }
                }
            }
            try {
                return PlotMain.getUUIDSaver().mojangName(uuid);
            } catch (final Exception e) {
                try {
                    final NameFetcher fetcher = new NameFetcher(Arrays.asList(uuid));
                    name = fetcher.call().get(uuid);
                    add(new StringWrapper(name), uuid);
                    return name;
                } catch (final Exception ex) {
                    ex.printStackTrace();
                }
            }
        } else {
            return "unknown";
        }
        return "";
    }

    /**
     * @param name to use as key
     * @return UUID (name hash)
     */
    private static UUID getUuidOfflineMode(final StringWrapper name) {
        final UUID uuid = UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes(Charsets.UTF_8));
        add(name, uuid);
        return uuid;
    }

    /**
     * @param uuid to use as key
     * @return String - name
     */
    private static String getNameOnlinePlayer(final UUID uuid) {
        final Player player = Bukkit.getPlayer(uuid);
        if ((player == null) || !player.isOnline()) {
            return null;
        }
        final String name = player.getName();
        add(new StringWrapper(name), uuid);
        return name;
    }

    /**
     * @param uuid to use as key
     * @return String - name
     */
    private static String getNameOfflinePlayer(final UUID uuid) {
        final OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
        if ((player == null) || !player.hasPlayedBefore()) {
            return null;
        }
        final String name = player.getName();
        add(new StringWrapper(name), uuid);
        return name;
    }

    /**
     * @param name to use as key
     * @return UUID
     */
    private static UUID getUuidOnlinePlayer(final StringWrapper name) {
        @SuppressWarnings("deprecation")
        final Player player = Bukkit.getPlayer(name.value);
        if (player == null) {
            return null;
        }
        final UUID uuid = player.getUniqueId();
        add(name, uuid);
        return uuid;
    }

    /**
     * Handle saving of uuids
     *
     * @see com.intellectualcrafters.plot.uuid.UUIDSaver#globalSave(com.google.common.collect.BiMap)
     */
    @SuppressWarnings("unused")
    public static void handleSaving() {
        final UUIDSaver saver = PlotMain.getUUIDSaver();
        saver.globalSave(getUuidMap());
    }
}
