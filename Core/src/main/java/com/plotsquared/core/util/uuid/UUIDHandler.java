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
package com.plotsquared.core.util.uuid;

import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.config.Captions;
import com.plotsquared.core.database.DBFunc;
import com.plotsquared.core.player.OfflinePlotPlayer;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.util.task.RunnableVal;
import com.plotsquared.core.util.StringWrapper;
import com.google.common.collect.BiMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
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
     * @see BiMap
     */
    public static BiMap<StringWrapper, UUID> getUuidMap() {
        return implementation.getUUIDMap();
    }

    /**
     * Check if a uuid is cached
     *
     * @param uuid to check
     * @return true of the uuid is cached
     * @see BiMap#containsValue(Object)
     */
    public static boolean uuidExists(UUID uuid) {
        return implementation.uuidExists(uuid);
    }

    /**
     * Check if a name is cached
     *
     * @param name to check
     * @return true of the name is cached
     * @see BiMap#containsKey(Object)
     */
    public static boolean nameExists(StringWrapper name) {
        return implementation.nameExists(name);
    }

    public static HashSet<UUID> getAllUUIDS() {
        final HashSet<UUID> uuids = new HashSet<>();
        PlotSquared.get().forEachPlotRaw(plot -> {
            if (plot.hasOwner()) {
                uuids.add(plot.owner);
                uuids.addAll(plot.getTrusted());
                uuids.addAll(plot.getMembers());
                uuids.addAll(plot.getDenied());
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

    @NotNull public static UUID getUUID(PlotPlayer player) {
        return implementation.getUUID(player);
    }

    public static UUID getUUID(OfflinePlotPlayer player) {
        if (implementation == null) {
            return null;
        }
        return implementation.getUUID(player);
    }

    @Nullable public static String getName(UUID uuid) {
        if (implementation == null) {
            return null;
        }
        if (uuid != null && uuid.equals(DBFunc.SERVER)) {
            return Captions.SERVER.getTranslated();
        }
        return implementation.getName(uuid);
    }

    public static PlotPlayer getPlayer(UUID uuid) {
        if (implementation == null) {
            return null;
        }
        return check(implementation.getPlayer(uuid));
    }

    public static PlotPlayer getPlayer(String name) {
        if (implementation == null) {
            return null;
        }
        return check(implementation.getPlayer(name));
    }

    private static PlotPlayer check(@Nullable PlotPlayer player) {
        if (player != null && !player.isOnline()) {
            UUIDHandler.getPlayers().remove(player.getName());
            PlotSquared.get().IMP.unregister(player);
            player = null;
        }
        return player;
    }

    public static UUID getUUIDFromString(String nameOrUUIDString) {
        if (implementation == null) {
            return null;
        }
        if (nameOrUUIDString.length() > 16) {
            try {
                return UUID.fromString(nameOrUUIDString);
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
        return UUIDHandler.getUUID(nameOrUUIDString, null);
    }

    public static UUID getUUID(String name, RunnableVal<UUID> ifFetch) {
        if (implementation == null) {
            return null;
        }
        return implementation.getUUID(name, ifFetch);
    }

    public static UUID getCachedUUID(String name, RunnableVal<UUID> ifFetch) {
        if (implementation == null) {
            return null;
        }
        return implementation.getUUIDMap().get(new StringWrapper(name));
    }

    public static Map<String, PlotPlayer> getPlayers() {
        if (implementation == null) {
            return new HashMap<>();
        }
        return implementation.getPlayers();
    }

    public static void handleShutdown() {
        if (implementation == null) {
            return;
        }
        implementation.handleShutdown();
    }
}
