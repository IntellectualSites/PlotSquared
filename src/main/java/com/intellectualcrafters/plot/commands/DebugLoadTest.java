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

import java.lang.reflect.Field;

import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.database.DBFunc;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.plotsquared.general.commands.CommandDeclaration;

@CommandDeclaration(
        command = "debugloadtest",
        permission = "plots.debugloadtest",
        description = "This debug command will force the reload of all plots in the DB",
        usage = "/plot debugloadtest",
        category = CommandCategory.DEBUG,
        requiredType = RequiredType.CONSOLE
)
public class DebugLoadTest extends SubCommand {

    @Override
    public boolean onCommand(PlotPlayer plr, String[] args) {
        try {
            final Field fPlots = PS.class.getDeclaredField("plots");
            fPlots.setAccessible(true);
            fPlots.set(null, DBFunc.getPlots());
        } catch (final Exception e) {
            PS.debug("&3===FAILED&3===");
            e.printStackTrace();
            PS.debug("&3===END OF STACKTRACE===");
        }
        return true;
    }
}
