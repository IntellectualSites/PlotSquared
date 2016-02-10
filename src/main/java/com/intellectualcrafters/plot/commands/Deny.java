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

import java.util.UUID;

import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.database.DBFunc;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.EventUtil;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.Permissions;
import com.intellectualcrafters.plot.util.UUIDHandler;
import com.intellectualcrafters.plot.util.WorldUtil;
import com.plotsquared.general.commands.Argument;
import com.plotsquared.general.commands.CommandDeclaration;

@CommandDeclaration(command = "deny", aliases = { "d" }, description = "Deny a user from a plot", usage = "/plot deny <player>", category = CommandCategory.ACTIONS, requiredType = RequiredType.NONE)
public class Deny extends SubCommand {
    
    public Deny() {
        requiredArguments = new Argument[] { Argument.PlayerName };
    }
    
    @Override
    public boolean onCommand(final PlotPlayer plr, final String[] args) {
        
        final Location loc = plr.getLocation();
        final Plot plot = loc.getPlotAbs();
        if (plot == null) {
            return !sendMessage(plr, C.NOT_IN_PLOT);
        }
        if ((plot == null) || !plot.hasOwner()) {
            MainUtil.sendMessage(plr, C.PLOT_UNOWNED);
            return false;
        }
        if (!plot.isOwner(plr.getUUID()) && !Permissions.hasPermission(plr, "plots.admin.command.deny")) {
            MainUtil.sendMessage(plr, C.NO_PLOT_PERMS);
            return true;
        }
        UUID uuid;
        if (args[0].equalsIgnoreCase("*")) {
            uuid = DBFunc.everyone;
        } else {
            uuid = UUIDHandler.getUUID(args[0], null);
        }
        if (uuid == null) {
            MainUtil.sendMessage(plr, C.INVALID_PLAYER, args[0]);
            return false;
        }
        if (plot.isOwner(uuid)) {
            MainUtil.sendMessage(plr, C.ALREADY_OWNER);
            return false;
        }
        
        if (plot.getDenied().contains(uuid)) {
            MainUtil.sendMessage(plr, C.ALREADY_ADDED);
            return false;
        }
        plot.removeMember(uuid);
        plot.removeTrusted(uuid);
        plot.addDenied(uuid);
        EventUtil.manager.callDenied(plr, plot, uuid, true);
        MainUtil.sendMessage(plr, C.DENIED_ADDED);
        if (!uuid.equals(DBFunc.everyone)) {
            handleKick(UUIDHandler.getPlayer(uuid), plot);
        } else {
            for (PlotPlayer pp : plot.getPlayersInPlot()) {
                handleKick(pp, plot);
            }
        }
        return true;
    }
    
    private void handleKick(final PlotPlayer pp, final Plot plot) {
        if (pp == null) {
            return;
        }
        if (!plot.equals(pp.getCurrentPlot())) {
            return;
        }
        if (pp.hasPermission("plots.admin.entry.denied")) {
            return;
        }
        pp.teleport(WorldUtil.IMP.getSpawn(pp.getLocation().getWorld()));
        MainUtil.sendMessage(pp, C.YOU_GOT_DENIED);
    }
}
