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
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.SetBlockQueue;
import com.intellectualcrafters.plot.util.TaskManager;
import com.intellectualcrafters.plot.util.bukkit.UUIDHandler;
import com.intellectualcrafters.plot.uuid.DefaultUUIDWrapper;
import com.intellectualcrafters.plot.uuid.LowerOfflineUUIDWrapper;
import com.intellectualcrafters.plot.uuid.OfflineUUIDWrapper;
import com.intellectualcrafters.plot.uuid.UUIDWrapper;

public class DebugUUID extends SubCommand {
    public DebugUUID() {
        super("uuidconvert", "plots.admin", "Debug uuid conversion", "debuguuid", "debuguuid", CommandCategory.DEBUG, false);
    }

    @Override
    public boolean execute(final PlotPlayer player, final String... args) {
        if (player != null) {
            MainUtil.sendMessage(player, C.NOT_CONSOLE);
            return false;
        }
        if (args.length == 0) {
            MainUtil.sendMessage(player, C.COMMAND_SYNTAX, "/plot uuidconvert <lower|offline|online>");
            return false;
        }
        
        UUIDWrapper currentUUIDWrapper = UUIDHandler.uuidWrapper;
        UUIDWrapper newWrapper = null;
        
        switch (args[0].toLowerCase()) {
            case "lower": {
                newWrapper = new LowerOfflineUUIDWrapper();
                break;
            }
            case "offline": {
                newWrapper = new OfflineUUIDWrapper();
                break;
            }
            case "online": {
                newWrapper = new DefaultUUIDWrapper();
                break;
            }
            default: {
                try {
                    Class<?> clazz = Class.forName(args[0]);
                    newWrapper = (UUIDWrapper) clazz.newInstance();
                }
                catch (Exception e) {
                    MainUtil.sendMessage(player, C.COMMAND_SYNTAX, "/plot uuidconvert <lower|offline|online>");
                    return false;
                }
            }
        }
        
        if (currentUUIDWrapper.getClass().getCanonicalName().equals(newWrapper.getClass().getCanonicalName())) {
            MainUtil.sendMessage(player, "&cUUID mode already in use!");
            return false;
        }
        
        return true;
    }
}
