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

import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.database.DBFunc;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualsites.commands.CommandDeclaration;
import com.intellectualcrafters.plot.object.PlotPlayer;

@CommandDeclaration(
        command = "debugsavetest",
        permission = "plots.debugsavetest",
        category = CommandCategory.DEBUG,
        requiredType = RequiredType.CONSOLE,
        usage = "/plot debugsavetest",
        description = "This command will force the recreation of all plots in the DB"
)
public class DebugSaveTest extends SubCommand {

    @Override
    public boolean onCommand(PlotPlayer plr, String[] args) {
        final ArrayList<Plot> plots = new ArrayList<Plot>();
        plots.addAll(PS.get().getPlots());
        MainUtil.sendMessage(null, "&6Starting `DEBUGSAVETEST`");
        DBFunc.createPlotsAndData(plots, new Runnable() {
            @Override
            public void run() {
                MainUtil.sendMessage(null, "&6Database sync finished!");
            }
        });
        return true;
    }
}
