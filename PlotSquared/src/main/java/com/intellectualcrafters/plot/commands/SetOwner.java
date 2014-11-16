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

import com.intellectualcrafters.plot.PlotMain;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.database.DBFunc;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.util.PlayerFunctions;
import com.intellectualcrafters.plot.util.UUIDHandler;
import org.bukkit.entity.Player;

import java.util.UUID;

@SuppressWarnings("deprecation")
public class SetOwner extends SubCommand {

    public SetOwner() {
        super("setowner", "plots.admin", "Set the plot owner", "setowner {player}", "so", CommandCategory.ACTIONS, true);
    }

    /*
     * private UUID getUUID(String string) { OfflinePlayer player =
     * Bukkit.getOfflinePlayer(string); return ((player != null) &&
     * player.hasPlayedBefore()) ? player.getUniqueId() : null; }
     */

    private UUID getUUID(final String string) {
        return UUIDHandler.getUUID(string);
    }

    @Override
    public boolean execute(final Player plr, final String... args) {
        if (!PlayerFunctions.isInPlot(plr)) {
            PlayerFunctions.sendMessage(plr, C.NOT_IN_PLOT);
            return true;
        }
        final Plot plot = PlayerFunctions.getCurrentPlot(plr);
        if (args.length < 1) {
            PlayerFunctions.sendMessage(plr, C.NEED_USER);
            return true;
        }
        plot.owner = getUUID(args[0]);
        PlotMain.updatePlot(plot);
        DBFunc.setOwner(plot, plot.owner);
        PlayerFunctions.sendMessage(plr, C.SET_OWNER);

        if (PlotMain.worldGuardListener != null) {
            PlotMain.worldGuardListener.changeOwner(plr, plot.owner, plr.getWorld(), plot);
        }

        return true;
    }
}
