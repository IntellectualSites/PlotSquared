package com.intellectualcrafters.plot.uuid;

import com.google.common.collect.BiMap;
import com.intellectualcrafters.plot.PlotMain;
import com.intellectualcrafters.plot.StringWrapper;
import com.intellectualcrafters.plot.UUIDHandler;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

/**
 * Created by Citymonstret on 2014-10-13.
 */
public class PlotUUIDSaver extends UUIDSaver {

    public void globalPopulate() {
        JavaPlugin.getPlugin(PlotMain.class).getServer().getScheduler().runTaskAsynchronously(JavaPlugin.getPlugin(PlotMain.class), new Runnable() {
            @Override
            public void run() {
                OfflinePlayer[] offlinePlayers = Bukkit.getOfflinePlayers();
                int length = offlinePlayers.length;
                long start = System.currentTimeMillis();

                String name;
                UUID uuid;
                for (OfflinePlayer player : offlinePlayers) {
                    uuid = player.getUniqueId();
                    if (!UUIDHandler.uuidExists(uuid)) {
                        name = player.getName();
                        UUIDHandler.add(new StringWrapper(name), uuid);
                    }
                }

                long time = System.currentTimeMillis() - start;
                int size = UUIDHandler.getUuidMap().size();
                double ups;
                if(time == 0l || size == 0) {
                    ups = size;
                } else {
                    ups = size / time;
                }

                //Plot Squared Only...
                PlotMain.sendConsoleSenderMessage("&cFinished caching of offline player UUIDs! Took &6" + time + "&cms (&6" + ups + "&c per millisecond), &6"
                        + length + " &cUUIDs were cached" + " and there is now a grand total of &6" + size
                        + " &ccached.");
            }
        });
    }

    public void globalSave(BiMap<StringWrapper, UUID> map) {

    }

    public void save(UUIDSet set) {

    }

    public UUIDSet get(String name) {
        return null;
    }

    public UUIDSet get(UUID uuid) {
        return null;
    }
}