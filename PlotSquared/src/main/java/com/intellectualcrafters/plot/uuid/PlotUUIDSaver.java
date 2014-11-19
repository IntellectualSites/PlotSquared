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

package com.intellectualcrafters.plot.uuid;

import com.google.common.collect.BiMap;
import com.intellectualcrafters.json.JSONObject;
import com.intellectualcrafters.json.JSONTokener;
import com.intellectualcrafters.plot.PlotMain;
import com.intellectualcrafters.plot.config.Settings;
import com.intellectualcrafters.plot.object.StringWrapper;
import com.intellectualcrafters.plot.util.UUIDHandler;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.java.JavaPlugin;

import java.net.URL;
import java.net.URLConnection;
import java.util.UUID;

/**
 * Plot UUID Saver/Fetcher
 *
 * @author Citymonstret
 * @author Empire92
 */
public class PlotUUIDSaver implements UUIDSaver {

    @Override
    public void globalPopulate() {
        JavaPlugin.getPlugin(PlotMain.class).getServer().getScheduler().runTaskAsynchronously(JavaPlugin.getPlugin(PlotMain.class), new Runnable() {
            @Override
            public void run() {
                final OfflinePlayer[] offlinePlayers = Bukkit.getOfflinePlayers();
                final int length = offlinePlayers.length;
                final long start = System.currentTimeMillis();

                String name;
                UUID uuid;
                for (final OfflinePlayer player : offlinePlayers) {
                    uuid = player.getUniqueId();
                    if (!UUIDHandler.uuidExists(uuid)) {
                        name = player.getName();
                        UUIDHandler.add(new StringWrapper(name), uuid);
                    }
                }

                final long time = System.currentTimeMillis() - start;
                final int size = UUIDHandler.getUuidMap().size();
                double ups;
                if ((time == 0l) || (size == 0)) {
                    ups = size;
                } else {
                    ups = size / time;
                }

                // Plot Squared Only...
                PlotMain.sendConsoleSenderMessage("&cFinished caching of offline player UUIDs! Took &6" + time + "&cms (&6" + ups + "&c per millisecond), &6" + length + " &cUUIDs were cached" + " and there is now a grand total of &6" + size + " &ccached.");
            }
        });
    }

    @Override
    public void globalSave(final BiMap<StringWrapper, UUID> map) {

    }

    @Override
    public void save(final UUIDSet set) {

    }

    @Override
    public UUID mojangUUID(final String name) throws Exception {
        final URLConnection connection = new URL(Settings.API_URL + "?user=" + name).openConnection();
        connection.addRequestProperty("User-Agent", "Mozilla/4.0");
        final JSONTokener tokener = new JSONTokener(connection.getInputStream());
        final JSONObject root = new JSONObject(tokener);
        final String uuid = root.getJSONObject(name).getString("dashed");
        return UUID.fromString(uuid);
    }

    @Override
    public String mojangName(final UUID uuid) throws Exception {
        final URLConnection connection = new URL(Settings.API_URL + "?user=" + uuid.toString().replace("-", "")).openConnection();
        connection.addRequestProperty("User-Agent", "Mozilla/4.0");
        final JSONTokener tokener = new JSONTokener(connection.getInputStream());
        final JSONObject root = new JSONObject(tokener);
        return root.getJSONObject(uuid.toString().replace("-", "")).getString("username");
    }

    @Override
    public UUIDSet get(final String name) {
        return null;
    }

    @Override
    public UUIDSet get(final UUID uuid) {
        return null;
    }
}
