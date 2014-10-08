package com.intellectualcrafters.plot;

import com.google.common.base.Charsets;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class UUIDHandler {

    private static ConcurrentHashMap<String, UUID> uuidMap = new ConcurrentHashMap<>();

    public static boolean uuidExists(UUID uuid) {
        return uuidMap.containsValue(uuid);
    }

    public static boolean nameExists(String name) {
        return uuidMap.containsKey(name);
    }

    public static void add(String name, UUID uuid) {
        uuidMap.put(name, uuid);
    }

    /**
     *
     * @param plugin
     */
    public static void startFetch(JavaPlugin plugin) {
       plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {
           @Override
           public void run() {
                OfflinePlayer[] offlinePlayers = Bukkit.getOfflinePlayers();
                int lenght = offlinePlayers.length;
                long start = System.currentTimeMillis();

                String name;
                UUID uuid;
                for(OfflinePlayer player : offlinePlayers) {
                    name = player.getName();
                    uuid = player.getUniqueId();
                    if(!uuidExists(uuid))
                        add(name, uuid);
                }

                long time = System.currentTimeMillis() - start;
                PlotMain.sendConsoleSenderMessage("&cFinished caching of offlineplayers! Took &6" + time + "&cms, &6" + lenght + " &cUUID's were cached" +
                        " and there is now a grand total of &6" + uuidMap.size() + " &ccached.");
           }
       });
    }

    /**
     *
     * @param name
     * @return
     */
    public static UUID getUUID(String name) {
        if (nameExists(name)) {
            return uuidMap.get(name);
        }
        UUID uuid;
        if ((uuid = getUuidOnlinePlayer(name)) != null) {
            return uuid;
        }
        if ((uuid = getUuidOfflinePlayer(name)) != null) {
            return uuid;
        }
        if(Bukkit.getOnlineMode()) {
            /* TODO: Add mojang API support */
        } else {
            return getUuidOfflineMode(name);
        }
        return null;
    }

    /**
     *
     * @param uuid
     * @return
     */
    private static String loopSearch(UUID uuid) {
        for(Map.Entry<String, UUID> entry : uuidMap.entrySet()) {
            if(entry.getValue().equals(uuid)) {
                return entry.getKey();
            }
        }
        return "";
    }

    /**
     *
     * @param uuid
     * @return
     */
    public static String getName(UUID uuid) {
        if(uuidExists(uuid)) {
            return loopSearch(uuid);
        }
        String name;
        if ((name = getNameOnlinePlayer(uuid)) != null) {
            return name;
        }
        if ((name = getNameOfflinePlayer(uuid)) != null) {
            return name;
        }
        return null;
    }

    /**
     *
     * @param name
     * @return
     */
    private static UUID getUuidOfflineMode(String name) {
        UUID uuid = UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes(Charsets.UTF_8));
        add(name, uuid);
        return uuid;
    }

    /**
     *
     * @param uuid
     * @return
     */
    private static String getNameOnlinePlayer(UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);
        if(player == null || !player.isOnline()) {
            return null;
        }
        String name = player.getName();
        add(name, uuid);
        return name;
    }

    /**
     *
     * @param uuid
     * @return
     */
    private static String getNameOfflinePlayer(UUID uuid) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
        if (player == null || !player.hasPlayedBefore()) {
            return null;
        }
        String name = player.getName();
        add(name, uuid);
        return name;
    }

    /**
     *
     * @param name
     * @return
     */
    private static UUID getUuidOnlinePlayer(String name) {
        Player player = Bukkit.getPlayer(name);
        if (player == null || !player.isOnline()) {
            return null;
        }
        UUID uuid = player.getUniqueId();
        add(name, uuid);
        return uuid;
    }

    /**
     *
     * @param name
     * @return
     */
    private static UUID getUuidOfflinePlayer(String name) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(name);
        if (player == null || !player.hasPlayedBefore()) {
            return null;
        }
        UUID uuid = player.getUniqueId();
        add(name, uuid);
        return uuid;
    }
}
