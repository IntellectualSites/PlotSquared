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

import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.configuration.Captions;
import com.plotsquared.core.configuration.Settings;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

public class UpdateUtility implements Listener {

    public static String internalVersion;
    public static String spigotVersion;
    public final JavaPlugin javaPlugin;

    public UpdateUtility(final JavaPlugin javaPlugin) {
        this.javaPlugin = javaPlugin;
        internalVersion = javaPlugin.getDescription().getVersion();
    }

    public void updateChecker() {
        new BukkitRunnable() {
            public void run() {
                Bukkit.getScheduler().runTaskAsynchronously(UpdateUtility.this.javaPlugin, () -> {
                    if (Settings.Enabled_Components.UPDATE_NOTIFICATIONS) {
                        try {
                            HttpsURLConnection connection = (HttpsURLConnection) new URL("https://api.spigotmc.org/legacy/update.php?resource=1177").openConnection();
                            connection.setRequestMethod("GET");
                            spigotVersion = (new BufferedReader(new InputStreamReader(connection.getInputStream()))).readLine();
                        } catch (IOException e) {
                            PlotSquared.log(
                                Captions.PREFIX + "&cUnable to check for updates because: " + e);
                            this.cancel();
                            return;
                        }

                        if (!internalVersion.equals(spigotVersion)) {
                            PlotSquared.log(Captions.PREFIX + "&6There appears to be a PlotSquared update available!");
                            PlotSquared.log(Captions.PREFIX + "&6You are running version " + internalVersion + ", &6latest version is " + spigotVersion);
                            PlotSquared.log(Captions.PREFIX + "&6https://www.spigotmc.org/resources/1177/updates");
                        } else {
                            PlotSquared.log(Captions.PREFIX + "Congratulations! You are running the latest PlotSquared version.");
                        }
                    }
                    this.cancel();
                });
            }
        }.runTaskTimer(this.javaPlugin, 0L, 12000L);
    }
}
