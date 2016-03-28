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

import com.intellectualcrafters.json.JSONObject;
import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.StringMan;
import com.plotsquared.general.commands.CommandDeclaration;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

@CommandDeclaration(command = "plugin",
        permission = "plots.use",
        description = "Show plugin information",
        aliases = "version",
        category = CommandCategory.INFO)
public class PluginCmd extends SubCommand {

    @Override
    public boolean onCommand(PlotPlayer plr, String[] args) {
        MainUtil.sendMessage(plr, String.format("$2>> $1&lPlotSquared $2($1Version$2: $1%s$2)", StringMan.join(PS.get().getVersion(), ".")));
        MainUtil.sendMessage(plr, "$2>> $1&lAuthors$2: $1Citymonstret $2& $1Empire92");
        MainUtil.sendMessage(plr, "$2>> $1&lWiki$2: $1https://github.com/IntellectualCrafters/PlotSquared/wiki");
        MainUtil.sendMessage(plr, "$2>> $1&lNewest Version$2: $1" + getNewestVersionString());
        return true;
    }

    public String getNewestVersionString() {
        String str = readUrl("https://api.github.com/repos/IntellectualSites/PlotSquared/releases/latest");
        JSONObject release = new JSONObject(str);
        return release.getString("name");
    }

    private static String readUrl(String urlString) {
        BufferedReader reader = null;
        try {
            URL url = new URL(urlString);
            reader = new BufferedReader(new InputStreamReader(url.openStream()));
            StringBuilder buffer = new StringBuilder();
            int read;
            char[] chars = new char[1024];
            while ((read = reader.read(chars)) != -1) {
                buffer.append(chars, 0, read);
            }

            return buffer.toString();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
