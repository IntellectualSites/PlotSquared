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

import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.BlockManager;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.Permissions;
import com.intellectualcrafters.plot.util.bukkit.UUIDHandler;

public class Toggle extends SubCommand {
    public Toggle() {
        super(Command.TOGGLE, "Toggle per user settings", "toggle <setting>", CommandCategory.ACTIONS, true);
    }
    
    public void noArgs(PlotPlayer player) {
        MainUtil.sendMessage(player, C.COMMAND_SYNTAX, "/plot toggle <setting>");
        MainUtil.sendMessage(player, C.SUBCOMMAND_SET_OPTIONS_HEADER.s() + "titles");
    }

    @Override
    public boolean execute(final PlotPlayer player, final String... args) {
        if (args.length == 0) {
            noArgs(player);
            return false;
        }
        switch (args[0].toLowerCase()) {
            case "titles": {
                if (toggle(player, "disabletitles")) {
                    MainUtil.sendMessage(player, C.TOGGLE_ENABLED, args[0]);
                }
                else {
                    MainUtil.sendMessage(player, C.TOGGLE_DISABLED, args[0]);
                }
                return true;
            }
            default: {
                return false;
            }
        }
    }
    
    public boolean toggle(PlotPlayer player, String key) {
        if (player.getAttribute(key)) {
            player.removeAttribute(key);
            return true;
        }
        else {
            player.setAttribute(key);
            return false;
        }
    }
}
