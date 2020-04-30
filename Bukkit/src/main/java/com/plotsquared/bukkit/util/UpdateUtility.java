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

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.PlotVersion;
import com.plotsquared.core.configuration.Captions;
import com.plotsquared.core.configuration.Settings;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

public class UpdateUtility implements Listener {

    public static PlotVersion internalVersion;
    public static String spigotVersion;
    public static boolean hasUpdate;
    private static BukkitTask task;
    public final JavaPlugin javaPlugin;
    private boolean notify = true;

    public UpdateUtility(final JavaPlugin javaPlugin) {
        this.javaPlugin = javaPlugin;
        internalVersion = PlotSquared.get().getVersion();
    }

    public void updateChecker() {
        task = Bukkit.getScheduler().runTaskTimerAsynchronously(this.javaPlugin, () -> {
            try {
                HttpsURLConnection connection = (HttpsURLConnection) new URL(
                    "https://api.spigotmc.org/simple/0.1/index.php?action=getResource&id=77506")
                    .openConnection();
                connection.setRequestMethod("GET");
                JsonObject result = (new JsonParser())
                    .parse(new JsonReader(new InputStreamReader(connection.getInputStream())))
                    .getAsJsonObject();
                spigotVersion = result.get("current_version").getAsString();
            } catch (IOException e) {
                PlotSquared.log(Captions.PREFIX + "&cUnable to check for updates because: " + e);
                return;
            }

            if (internalVersion.isLaterVersion(spigotVersion)) {
                PlotSquared
                    .log(Captions.PREFIX + "&6There appears to be a PlotSquared update available!");
                PlotSquared.log(
                    Captions.PREFIX + "&6You are running version " + internalVersion.versionString()
                        + ", &6latest version is " + spigotVersion);
                PlotSquared
                    .log(Captions.PREFIX + "&6https://www.spigotmc.org/resources/77506/updates");
                hasUpdate = true;
                if (Settings.UpdateChecker.NOTIFY_ONCE) {
                    cancelTask();
                }
            } else if (notify) {
                notify = false;
                PlotSquared.log(Captions.PREFIX
                    + "Congratulations! You are running the latest PlotSquared version.");
            }
        }, 0L, Settings.UpdateChecker.POLL_RATE * 60 * 20);
    }

    private void cancelTask() {
        Bukkit.getScheduler().runTaskLater(javaPlugin, () -> task.cancel(), 20L);
    }
}
