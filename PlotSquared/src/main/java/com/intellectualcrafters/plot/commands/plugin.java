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

package com.intellectualcrafters.plot.commands;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.intellectualcrafters.plot.PlotSquared;
import com.intellectualcrafters.plot.util.PlayerFunctions;

public class plugin extends SubCommand {

    public static String downloads, version;

    public plugin() {
        super("plugin", "plots.use", "Show plugin information", "plugin", "version", CommandCategory.INFO, false);
    }

    public static void setup(final JavaPlugin plugin) {
        plugin.getServer().getScheduler().runTaskLaterAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                try {
                    downloads = convertToNumericString(getInfo("https://intellectualsites.com/spigot_api.php?method=downloads&url=http://www.spigotmc.org/resources/plotsquared.1177/"), false);
                } catch (final Exception e) {
                    downloads = "unknown";
                }
            }
        }, 1l);
        plugin.getServer().getScheduler().runTaskLaterAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                try {
                    version = convertToNumericString(getInfo("https://intellectualsites.com/spigot_api.php?method=version&resource=1177"), true);
                } catch (final Exception e) {
                    // Let's just ignore this, most likely error 500...
                    version = "unknown";
                }
            }
        }, 200l);
    }

    private static String convertToNumericString(final String str, final boolean dividers) {
        final StringBuilder builder = new StringBuilder();
        for (final char c : str.toCharArray()) {
            if (Character.isDigit(c)) {
                builder.append(c);
            } else if (dividers && ((c == ',') || (c == '.') || (c == '-') || (c == '_'))) {
                builder.append(c);
            }
        }
        return builder.toString();
    }

    private static String getInfo(final String link) throws Exception {
        final URLConnection connection = new URL(link).openConnection();
        connection.addRequestProperty("User-Agent", "Mozilla/4.0");
        final BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String document = "", line;
        while ((line = reader.readLine()) != null) {
            document += (line + "\n");
        }
        reader.close();
        return document;
    }

    @Override
    public boolean execute(final Player plr, final String... args) {
        Bukkit.getScheduler().runTaskAsynchronously(PlotSquared.getMain(), new Runnable() {
            @Override
            public void run() {
                final ArrayList<String> strings = new ArrayList<String>() {
                    {
                        add(String.format("&c>> &6PlotSquared (Version: %s)", PlotSquared.getMain().getDescription().getVersion()));
                        add(String.format("&c>> &6Made by Citymonstret and Empire92"));
                        add(String.format("&c>> &6Download at &lhttp://www.spigotmc.org/resources/1177"));
                        add(String.format("&c>> &cNewest Version (Spigot): %s", version));
                        add(String.format("&c>> &cTotal Downloads (Spigot): %s", downloads));
                    }
                };
                for (final String s : strings) {
                    PlayerFunctions.sendMessage(plr, s);
                }
            }
        });
        return true;
    }

}
