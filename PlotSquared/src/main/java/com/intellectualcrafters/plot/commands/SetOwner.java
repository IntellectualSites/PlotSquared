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
import com.intellectualcrafters.plot.config.Settings;
import com.intellectualcrafters.plot.database.DBFunc;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotId;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.Permissions;
import com.intellectualcrafters.plot.util.bukkit.UUIDHandler;

import java.util.ArrayList;
import java.util.UUID;

public class SetOwner extends SubCommand {
    public SetOwner() {
        super("setowner", "plots.set.owner", "Set the plot owner", "setowner <player>", "so", CommandCategory.ACTIONS, true);
    }

    /*
     * private UUID getUUID(String string) { OfflinePlayer player =
     * Bukkit.getOfflinePlayer(string); return ((player != null) &&
     * player.hasPlayedBefore()) ? UUIDHandler.getUUID(player) : null; }
     */
    private UUID getUUID(final String string) {
        return UUIDHandler.getUUID(string);
    }

    @Override
    public boolean execute(final PlotPlayer plr, final String... args) {
        final Location loc = plr.getLocation();
        final Plot plot = MainUtil.getPlot(loc);
        if ((plot == null) || (plot.owner == null)) {
            MainUtil.sendMessage(plr, C.NOT_IN_PLOT);
            return false;
        }
        if (args.length < 1) {
            MainUtil.sendMessage(plr, C.NEED_USER);
            return false;
        }
        
        final PlotId bot = MainUtil.getBottomPlot(plot).id;
        final PlotId top = MainUtil.getTopPlot(plot).id;
        final ArrayList<PlotId> plots = MainUtil.getPlotSelectionIds(bot, top);
        
        PlotPlayer other = UUIDHandler.getPlayer(args[0]);
        if (other == null) {
        	if (!Permissions.hasPermission(plr, "plots.admin.command.setowner")) {
        		MainUtil.sendMessage(plr, C.INVALID_PLAYER, args[0]);
        		return false;
        	}
        }
        else {
        	if (!Permissions.hasPermission(plr, "plots.admin.command.setowner")) {
        		int size = plots.size();
                final int currentPlots = (Settings.GLOBAL_LIMIT ? MainUtil.getPlayerPlotCount(other) : MainUtil.getPlayerPlotCount(loc.getWorld(), other)) + size;
        		if (currentPlots > MainUtil.getAllowedPlots(other)) {
                    sendMessage(plr, C.CANT_TRANSFER_MORE_PLOTS);
                    return false;
                }
        	}
        }
        
        if (!plot.isOwner(plr.getUUID())) {
        	if (!Permissions.hasPermission(plr, "plots.admin.command.setowner")) {
        		MainUtil.sendMessage(plr, C.NO_PERMISSION, "plots.admin.command.setowner");
        		return false;
        	}
        }
        
        final String world = loc.getWorld();
        for (final PlotId id : plots) {
            final Plot current = PS.get().getPlots(world).get(id);
            final UUID uuid = getUUID(args[0]);
            if (uuid == null) {
                MainUtil.sendMessage(plr, C.INVALID_PLAYER, args[0]);
                return false;
            }
            current.owner = uuid;
            PS.get().updatePlot(current);
            DBFunc.setOwner(current, current.owner);
        }
        MainUtil.setSign(args[0], plot);
        MainUtil.sendMessage(plr, C.SET_OWNER);
        if (other != null) {
        	MainUtil.sendMessage(other, C.NOW_OWNER, plot.world + ";" + plot.id);
        }
        return true;
    }
}
