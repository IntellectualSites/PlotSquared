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
import com.intellectualsites.commands.CommandDeclaration;
import com.intellectualsites.commands.callers.CommandCaller;
import com.plotsquared.bukkit.util.UUIDHandler;

@CommandDeclaration(
        command = "kick",
        aliases = {"k"},
        description = "Kick a player from your plot",
        permission = "plots.kick",
        category = CommandCategory.ACTIONS,
        requiredType = PlotPlayer.class
)
public class Kick extends SubCommand {

    @Override
    public boolean onCommand(final CommandCaller caller, final String[] args) {
        final PlotPlayer plr = (PlotPlayer) caller.getSuperCaller();
        final Location loc = plr.getLocation();
        final Plot plot = MainUtil.getPlot(loc);
        if (plot == null) {
            return !sendMessage(plr, C.NOT_IN_PLOT);
        }
        if (plot == null || ((!plot.hasOwner() || !plot.isOwner(plr.getUUID())) && !Permissions.hasPermission(plr, "plots.admin.command.kick"))) {
            MainUtil.sendMessage(plr, C.NO_PLOT_PERMS);
            return false;
        }
        if (args.length != 1) {
            MainUtil.sendMessage(plr, "&c/plot kick <player>");
            return false;
        }
        final PlotPlayer player = UUIDHandler.getPlayer(args[0]);
        if (player == null) {
            MainUtil.sendMessage(plr, C.INVALID_PLAYER, args[0]);
            return false;
        }
        Location otherLoc = player.getLocation();
        if (!plr.getLocation().getWorld().equals(otherLoc.getWorld()) || !plot.equals(MainUtil.getPlot(otherLoc))) {
            MainUtil.sendMessage(plr, C.INVALID_PLAYER, args[0]);
            return false;
        }
        player.teleport(BlockManager.manager.getSpawn(loc.getWorld()));
        return true;
    }
}
