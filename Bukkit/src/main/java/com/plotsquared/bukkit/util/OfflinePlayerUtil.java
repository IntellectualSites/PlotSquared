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
package com.plotsquared.bukkit.util;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.UUID;

import static com.plotsquared.core.util.ReflectionUtils.callConstructor;
import static com.plotsquared.core.util.ReflectionUtils.callMethod;
import static com.plotsquared.core.util.ReflectionUtils.getCbClass;
import static com.plotsquared.core.util.ReflectionUtils.getField;
import static com.plotsquared.core.util.ReflectionUtils.getNmsClass;
import static com.plotsquared.core.util.ReflectionUtils.getUtilClass;
import static com.plotsquared.core.util.ReflectionUtils.makeConstructor;
import static com.plotsquared.core.util.ReflectionUtils.makeField;
import static com.plotsquared.core.util.ReflectionUtils.makeMethod;

public class OfflinePlayerUtil {

    public static Player loadPlayer(OfflinePlayer player) {
        if (player == null) {
            return null;
        }
        if (player instanceof Player) {
            return (Player) player;
        }
        return loadPlayer(player.getUniqueId(), player.getName());
    }

    private static Player loadPlayer(UUID id, String name) {
        Object server = getMinecraftServer();
        Object interactManager = newPlayerInteractManager();
        Object worldServer = getWorldServer();
        Object profile = newGameProfile(id, name);
        Class<?> entityPlayerClass = getNmsClass("EntityPlayer");
        Constructor entityPlayerConstructor =
            makeConstructor(entityPlayerClass, getNmsClass("MinecraftServer"),
                getNmsClass("WorldServer"), getUtilClass("com.mojang.authlib.GameProfile"),
                getNmsClass("PlayerInteractManager"));
        Object entityPlayer =
            callConstructor(entityPlayerConstructor, server, worldServer, profile, interactManager);
        return (Player) getBukkitEntity(entityPlayer);
    }

    private static Object newGameProfile(UUID id, String name) {
        Class<?> gameProfileClass = getUtilClass("com.mojang.authlib.GameProfile");
        if (gameProfileClass == null) { //Before uuids
            return name;
        }
        Constructor gameProfileConstructor =
            makeConstructor(gameProfileClass, UUID.class, String.class);
        if (gameProfileConstructor == null) { //Version has string constructor
            gameProfileConstructor = makeConstructor(gameProfileClass, String.class, String.class);
            return callConstructor(gameProfileConstructor, id.toString(), name);
        } else { //Version has uuid constructor
            return callConstructor(gameProfileConstructor, id, name);
        }
    }

    private static Object newPlayerInteractManager() {
        Object worldServer = getWorldServer();
        Class<?> playerInteractClass = getNmsClass("PlayerInteractManager");
        Class<?> worldClass = getNmsClass("World");
        Constructor c = makeConstructor(playerInteractClass, worldClass);
        return callConstructor(c, worldServer);
    }

    public static Object getWorldServerNew() {
        Object server = getMinecraftServer();
        Class<?> minecraftServerClass = getNmsClass("MinecraftServer");
        Class<?> dimensionManager = getNmsClass("DimensionManager");
        Object overworld = getField(makeField(dimensionManager, "OVERWORLD"), null);
        Method getWorldServer = makeMethod(minecraftServerClass, "getWorldServer", dimensionManager);
        return callMethod(getWorldServer, server, overworld);
    }

    private static Object getWorldServer() {
        Object server = getMinecraftServer();
        Class<?> minecraftServerClass = getNmsClass("MinecraftServer");
        Method getWorldServer = makeMethod(minecraftServerClass, "getWorldServer", int.class);
        Object o;
        try {
            o = callMethod(getWorldServer, server, 0);
        } catch (final RuntimeException e) {
            o = getWorldServerNew();
        }
        return o;
    }

    //NMS Utils

    private static Object getMinecraftServer() {
        return callMethod(makeMethod(getCbClass("CraftServer"), "getServer"), Bukkit.getServer());
    }

    private static Entity getBukkitEntity(Object o) {
        Method getBukkitEntity = makeMethod(o.getClass(), "getBukkitEntity");
        return callMethod(getBukkitEntity, o);
    }
}
