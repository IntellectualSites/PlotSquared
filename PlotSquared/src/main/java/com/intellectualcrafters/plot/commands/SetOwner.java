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
import com.intellectualcrafters.plot.object.PlotId;
import com.intellectualcrafters.plot.util.PlayerFunctions;
import com.intellectualcrafters.plot.util.PlotHelper;
import com.intellectualcrafters.plot.util.UUIDHandler;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.UUID;

@SuppressWarnings("deprecation") public class SetOwner extends SubCommand {

    public SetOwner() {
        super("setowner", "plots.admin.command.setowner", "Set the plot owner", "setowner {player}", "so", CommandCategory.ACTIONS, true);
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
    public boolean execute(final Player plr, final String... args) {
        Plot plot = PlayerFunctions.getCurrentPlot(plr);
        if (plot == null || plot.owner == null) {
            PlayerFunctions.sendMessage(plr, C.NOT_IN_PLOT);
            return true;
        }
        if (args.length < 1) {
            PlayerFunctions.sendMessage(plr, C.NEED_USER);
            return true;
        }

        final World world = plr.getWorld();
        final PlotId bot = PlayerFunctions.getBottomPlot(world, plot).id;
        final PlotId top = PlayerFunctions.getTopPlot(world, plot).id;

        final ArrayList<PlotId> plots = PlayerFunctions.getPlotSelectionIds(world, bot, top);

        for (final PlotId id : plots) {
            final Plot current = PlotMain.getPlots(world).get(id);
            
            UUID uuid = getUUID(args[0]);
            
            if (uuid == null) {
                PlayerFunctions.sendMessage(plr, C.INVALID_PLAYER, args[0]);
                return false;
            }
            
            current.owner = uuid;
            PlotMain.updatePlot(current);
            DBFunc.setOwner(current, current.owner);

            if (PlotMain.worldGuardListener != null) {
                PlotMain.worldGuardListener.changeOwner(plr, current.owner, plr.getWorld(), current);
            }
        }
        PlotHelper.setSign(world, args[0], plot);

        PlayerFunctions.sendMessage(plr, C.SET_OWNER);

        return true;
    }
}
