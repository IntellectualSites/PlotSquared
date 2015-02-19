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

import java.util.ArrayList;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

import com.intellectualcrafters.plot.PlotSquared;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.database.DBFunc;
import com.intellectualcrafters.plot.flag.AbstractFlag;
import com.intellectualcrafters.plot.flag.Flag;
import com.intellectualcrafters.plot.flag.FlagManager;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.util.PlayerFunctions;

public class DebugFixFlags extends SubCommand {

    public DebugFixFlags() {
        super(Command.DEBUGFIXFLAGS, "Attempt to fix all flags for a world", "debugclear", CommandCategory.DEBUG, false);
    }

    @Override
    public boolean execute(final Player plr, final String... args) {
        if (plr != null) {
            PlayerFunctions.sendMessage(plr, C.NOT_CONSOLE);
            return false;
        }
        if (args.length != 1) {
            PlayerFunctions.sendMessage(plr, C.COMMAND_SYNTAX, "/plot debugfixflags <world>");
            return false;
        }
        World world = Bukkit.getWorld(args[0]);
        if (world == null || !PlotSquared.isPlotWorld(world)) {
            PlayerFunctions.sendMessage(plr, C.NOT_VALID_PLOT_WORLD, args[0]);
            return false;
        }
        PlayerFunctions.sendMessage(plr, "&8--- &6Starting task &8 ---");
        for (Plot plot : PlotSquared.getPlots(world).values()) {
            Set<Flag> flags = plot.settings.flags;
            ArrayList<Flag> toRemove = new ArrayList<Flag>();
            for (Flag flag : flags) {
                AbstractFlag af = FlagManager.getFlag(flag.getKey());
                if (af == null) {
                    toRemove.add(flag);
                }
            }
            for (Flag flag : toRemove) {
                plot.settings.flags.remove(flag);
            }
            if (toRemove.size() > 0) {
                DBFunc.setFlags(plot.world, plot, plot.settings.flags);
            }
        }
        PlayerFunctions.sendMessage(plr, "&aDone!");
        
        return true;
    }
}
