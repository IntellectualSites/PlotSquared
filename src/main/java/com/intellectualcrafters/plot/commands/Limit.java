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
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.UUIDHandler;
import com.plotsquared.general.commands.Argument;
import com.plotsquared.general.commands.CommandDeclaration;

@CommandDeclaration(
command = "limit",
permission = "plots.limit",
description = "Set or increment player plot claim limits",
aliases = { "setlimit" },
usage = "/plot limit <player> <expression>",
category = CommandCategory.DEBUG)
public class Limit extends SubCommand
{

    public Limit()
    {
        requiredArguments = new Argument[] {
        Argument.String,
        Argument.String
        };
    }

    @Override
    public boolean onCommand(final PlotPlayer plr, final String[] args)
    {
        final UUID uuid = UUIDHandler.getUUID(args[0], null);
        if (uuid == null)
        {
            MainUtil.sendMessage(plr, C.INVALID_PLAYER, args[0]);
            return false;
        }
        UUIDHandler.getUUIDWrapper().getOfflinePlayer(uuid);

        // get current plot limit
        // increase

        //        EconHandler.manager.setPermission(op, perm, value);
        plr.sendMessage("TODO");

        return true;
    }
}
