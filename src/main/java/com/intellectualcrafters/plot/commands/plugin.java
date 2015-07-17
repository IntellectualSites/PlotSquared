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

import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.StringMan;
import com.intellectualcrafters.plot.util.TaskManager;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

public class plugin extends SubCommand {

    public plugin() {
        super("plugin", "plots.use", "Show plugin information", "plugin", "version", CommandCategory.INFO, false);
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
    public boolean execute(final PlotPlayer plr, final String... args) {
        TaskManager.runTaskAsync(new Runnable() {
            @Override
            public void run() {
                final ArrayList<String> strings = new ArrayList<String>() {
                    // $2>> $1%id$2:$1%world $2- $1%owner
                    {
                        add(String.format("$2>> $1&lPlotSquared $2($1Version$2: $1%s$2)", PS.get().IMP.getVersion()));
                        add(String.format("$2>> $1&lAuthors$2: $1Citymonstret $2& $1Empire92"));
                        add(String.format("$2>> $1&lWiki$2: $1https://github.com/IntellectualCrafters/PlotSquared/wiki"));
                        add(String.format("$2>> $1&lWebsite$2: $1http://plotsquared.com"));
                        add(String.format("$2>> $1&lNewest Version$2: $1" + (PS.get().update == null ? PS.get().IMP.getVersion() : PS.get().update)));
                    }
                };
                for (final String s : strings) {
                    MainUtil.sendMessage(plr, StringMan.replaceFromMap(s, C.replacements), false);
                }
            }
        });
        return true;
    }
}
