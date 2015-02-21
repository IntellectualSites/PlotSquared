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

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.intellectualcrafters.plot.BukkitMain;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.util.bukkit.BukkitPlayerFunctions;
import com.intellectualcrafters.plot.util.bukkit.UUIDHandler;

@SuppressWarnings({ "unused", "deprecation", "javadoc" })
public class Kick extends SubCommand {
    public Kick() {
        super(Command.KICK, "Kick a player from your plot", "kick", CommandCategory.ACTIONS, true);
    }
    
    @Override
    public boolean execute(final PlotPlayer plr, final String... args) {
        if (!BukkitPlayerFunctions.isInPlot(plr)) {
            BukkitPlayerFunctions.sendMessage(plr, "You're not in a plot.");
            return false;
        }
        final Plot plot = BukkitPlayerFunctions.getCurrentPlot(plr);
        if (((plot == null) || !plot.hasOwner() || !plot.getOwner().equals(UUIDHandler.getUUID(plr))) && !BukkitMain.hasPermission(plr, "plots.admin.command.kick")) {
            BukkitPlayerFunctions.sendMessage(plr, C.NO_PLOT_PERMS);
            return false;
        }
        if (args.length != 1) {
            BukkitPlayerFunctions.sendMessage(plr, "&c/plot kick <player>");
            return false;
        }
        if (Bukkit.getPlayer(args[0]) == null) {
            BukkitPlayerFunctions.sendMessage(plr, C.INVALID_PLAYER, args[0]);
            return false;
        }
        final Player player = Bukkit.getPlayer(args[0]);
        if (!player.getWorld().equals(plr.getWorld()) || !BukkitPlayerFunctions.isInPlot(player) || (BukkitPlayerFunctions.getCurrentPlot(player) == null) || !BukkitPlayerFunctions.getCurrentPlot(player).equals(plot)) {
            BukkitPlayerFunctions.sendMessage(plr, C.INVALID_PLAYER.s().replaceAll("%player%", args[0]));
            return false;
        }
        player.teleport(player.getWorld().getSpawnLocation());
        return true;
    }
}
