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
import com.intellectualcrafters.plot.config.Settings;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.Permissions;
import com.intellectualcrafters.plot.util.UUIDHandler;
import com.plotsquared.general.commands.CommandDeclaration;

import java.util.HashSet;
import java.util.UUID;

@CommandDeclaration(
command = "setowner",
permission = "plots.set.owner",
description = "Set the plot owner",
usage = "/plot setowner <player>",
aliases = { "owner", "so", "seto" },
category = CommandCategory.CLAIMING,
requiredType = RequiredType.NONE)
public class Owner extends SetCommand {
    
    @Override
    public boolean set(PlotPlayer plr, Plot plot, String value) {
        HashSet<Plot> plots = plot.getConnectedPlots();
        UUID uuid = null;
        String name = null;
        if (value.length() == 36) {
            try {
                uuid = UUID.fromString(value);
                name = MainUtil.getName(uuid);
            } catch (Exception e) {}
        } else {
            uuid = UUIDHandler.getUUID(value, null);
            name = UUIDHandler.getName(uuid);
            name = name == null ? value : name;
        }
        if (uuid == null) {
            if (value.equalsIgnoreCase("none")) {
                HashSet<Plot> connected = plot.getConnectedPlots();
                plot.unlink();
                for (Plot current : connected) {
                    current.unclaim();
                    current.removeSign();
                }
                MainUtil.sendMessage(plr, C.SET_OWNER);
                return true;
            }
            C.INVALID_PLAYER.send(plr, value);
            return false;
        }
        if (plot.isOwner(uuid)) {
            C.ALREADY_OWNER.send(plr);
            return false;
        }
        PlotPlayer other = UUIDHandler.getPlayer(uuid);
        if (!Permissions.hasPermission(plr, "plots.admin.command.setowner")) {
            if (other == null) {
                C.INVALID_PLAYER_OFFLINE.send(plr, value);
                return false;
            }
            final int size = plots.size();
            final int currentPlots = (Settings.GLOBAL_LIMIT ? other.getPlotCount() : other.getPlotCount(plot.getArea().worldname)) + size;
            if (currentPlots > other.getAllowedPlots()) {
                sendMessage(plr, C.CANT_TRANSFER_MORE_PLOTS);
                return false;
            }
        }
        
        plot.setOwner(uuid);
        plot.setSign(name);
        MainUtil.sendMessage(plr, C.SET_OWNER);
        if (other != null) {
            MainUtil.sendMessage(other, C.NOW_OWNER, plot.getArea() + ";" + plot.getId());
        }
        return true;
    }
}
