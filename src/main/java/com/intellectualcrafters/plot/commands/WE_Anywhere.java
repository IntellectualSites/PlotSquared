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
import com.intellectualsites.commands.CommandDeclaration;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.plotsquared.bukkit.listeners.worldedit.WEManager;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.Permissions;

@CommandDeclaration(
        command = "weanywhere",
        permission = "plots.worldedit.bypass",
        description = "Force bypass of WorldEdit",
        aliases = {"wea"},
        usage = "/plot weanywhere",
        requiredType = RequiredType.PLAYER,
        category = CommandCategory.DEBUG
)
public class WE_Anywhere extends SubCommand {

    @Override
    public boolean onCommand(PlotPlayer player, String[] arguments) {
        if (PS.get().worldEdit == null) {
            MainUtil.sendMessage(player, "&cWorldEdit is not enabled on this server");
            return true;
        }
        if (Permissions.hasPermission(player, "plots.worldedit.bypass")) {
            if (WEManager.bypass.contains(player.getName())) {
                WEManager.bypass.remove(player.getName());
                MainUtil.sendMessage(player, C.WORLDEDIT_RESTRICTED);
            }
            else {
                WEManager.bypass.add(player.getName());
                MainUtil.sendMessage(player, C.WORLDEDIT_UNMASKED);
            }
        }
        return true;
    }

}
