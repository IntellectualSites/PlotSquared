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
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;

import org.bukkit.Bukkit;

import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.TaskManager;

public class Update extends SubCommand {
    public static String downloads, version;

    public Update() {
        super("update", "plots.admin", "Update PlotSquared", "update", "updateplugin", CommandCategory.DEBUG, false);
    }

    @Override
    public boolean execute(final PlotPlayer plr, final String... args) {
        URL url;
        if (args.length == 0) {
            url = PS.get().update;
        }
        else if (args.length == 1) {
            try {
                url = new URL(args[0]);
            } catch (MalformedURLException e) {
                MainUtil.sendMessage(plr, "&cInvalid url: " + args[0]);
                MainUtil.sendMessage(plr, C.COMMAND_SYNTAX, "/plot update [url]");
                return false;
            }
        }
        else {
            MainUtil.sendMessage(plr, C.COMMAND_SYNTAX, "/plot update");
            return false;
        }
        if (url == null) {
            MainUtil.sendMessage(plr, "&cNo update found!");
            MainUtil.sendMessage(plr, "&cTo manually specify an update URL: /plot update <url>");
            return false;
        }
        if (PS.get().update(plr, url) && url == PS.get().update) {
            PS.get().update = null;
        }
        return true;
    }
}
