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
import com.plotsquared.general.commands.CommandDeclaration;
import java.net.MalformedURLException;
import java.net.URL;

@CommandDeclaration(
command = "update",
permission = "plots.admin.command.update",
description = "Update PlotSquared",
usage = "/plot update",
requiredType = RequiredType.NONE,
aliases = { "updateplugin" },
category = CommandCategory.ADMINISTRATION)
public class Update extends SubCommand {

    @Override
    public boolean onCommand(final PlotPlayer plr, final String[] args) {
        URL url;
        if (args.length == 0) {
            url = PS.get().update;
        } else if (args.length == 1) {
            try {
                url = new URL(args[0]);
            } catch (final MalformedURLException e) {
                MainUtil.sendMessage(plr, "&cInvalid URL: " + args[0]);
                MainUtil.sendMessage(plr, C.COMMAND_SYNTAX, "/plot update [url]");
                return false;
            }
        } else {
            MainUtil.sendMessage(plr, C.COMMAND_SYNTAX, getUsage().replaceAll("\\{label\\}", "plot"));
            return false;
        }
        if (url == null) {
            MainUtil.sendMessage(plr, "&cNo update found!");
            MainUtil.sendMessage(plr, "&cTo manually specify an update URL: /plot update <url>");
            return false;
        }
        if (PS.get().update(null, url) && (url == PS.get().update)) {
            PS.get().update = null;
        }
        return true;
    }

}
