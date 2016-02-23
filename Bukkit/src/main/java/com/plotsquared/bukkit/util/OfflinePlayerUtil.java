package com.plotsquared.bukkit.util;

import static com.intellectualcrafters.plot.util.ReflectionUtils.callConstructor;
import static com.intellectualcrafters.plot.util.ReflectionUtils.callMethod;
import static com.intellectualcrafters.plot.util.ReflectionUtils.getCbClass;
import static com.intellectualcrafters.plot.util.ReflectionUtils.getNmsClass;
import static com.intellectualcrafters.plot.util.ReflectionUtils.getUtilClass;
import static com.intellectualcrafters.plot.util.ReflectionUtils.makeConstructor;
import static com.intellectualcrafters.plot.util.ReflectionUtils.makeMethod;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.UUID;

public class OfflinePlayerUtil {

    public static Player loadPlayer(final String name) {
        return loadPlayer(Bukkit.getOfflinePlayer(name));
    }

    public static Player loadPlayer(final UUID id) {
        return loadPlayer(Bukkit.getOfflinePlayer(id));
    }

    public static Player loadPlayer(final OfflinePlayer player) {
        if (player == null) {
            return null;
        }
        if (player instanceof Player) {
            return (Player) player;
        }
        return loadPlayer(player.getUniqueId(), player.getName());
    }

    private static Player loadPlayer(final UUID id, final String name) {
        final Object server = getMinecraftServer();
        final Object interactManager = newPlayerInteractManager();
        final Object worldServer = getWorldServer();
        final Object profile = newGameProfile(id, name);
        final Class<?> entityPlayerClass = getNmsClass("EntityPlayer");
        final Constructor entityPlayerConstructor = makeConstructor(entityPlayerClass, getNmsClass("MinecraftServer"), getNmsClass("WorldServer"),
                getUtilClass("com.mojang.authlib.GameProfile"),
                getNmsClass("PlayerInteractManager"));
        final Object entityPlayer = callConstructor(entityPlayerConstructor, server, worldServer, profile, interactManager);
        return (Player) getBukkitEntity(entityPlayer);
    }

    private static Object newGameProfile(final UUID id, final String name) {
        final Class<?> gameProfileClass = getUtilClass("com.mojang.authlib.GameProfile");
        if (gameProfileClass == null) { //Before uuids
            return name;
        }
        Constructor gameProfileConstructor = makeConstructor(gameProfileClass, UUID.class, String.class);
        if (gameProfileConstructor == null) { //Verson has string constructor
            gameProfileConstructor = makeConstructor(gameProfileClass, String.class, String.class);
            return callConstructor(gameProfileConstructor, id.toString(), name);
        } else { //Version has uuid constructor
            return callConstructor(gameProfileConstructor, id, name);
        }
    }

    private static Object newPlayerInteractManager() {
        final Object worldServer = getWorldServer();
        final Class<?> playerInteractClass = getNmsClass("PlayerInteractManager");
        final Class<?> worldClass = getNmsClass("World");
        final Constructor c = makeConstructor(playerInteractClass, worldClass);
        return callConstructor(c, worldServer);
    }

    private static Object getWorldServer() {
        final Object server = getMinecraftServer();
        final Class<?> minecraftServerClass = getNmsClass("MinecraftServer");
        final Method getWorldServer = makeMethod(minecraftServerClass, "getWorldServer", int.class);
        return callMethod(getWorldServer, server, 0);
    }

    //NMS Utils

    private static Object getMinecraftServer() {
        return callMethod(makeMethod(getCbClass("CraftServer"), "getServer"), Bukkit.getServer());
    }

    private static Entity getBukkitEntity(final Object o) {
        final Method getBukkitEntity = makeMethod(o.getClass(), "getBukkitEntity");
        return callMethod(getBukkitEntity, o);
    }
}
