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
import com.intellectualcrafters.plot.database.DBFunc;
import com.intellectualcrafters.plot.util.PlayerFunctions;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;

/**
 * @author Citymonstret
 */
public class DebugLoadTest extends SubCommand {

    public DebugLoadTest() {
        super(Command.DEBUGCLAIMTEST, "This debug command will force the reload of all plots in the DB", "debugloadtest", CommandCategory.DEBUG, false);
    }

    @Override
    public boolean execute(final Player plr, final String... args) {
        if (plr == null) {
            try {
                final Field fPlots = PlotMain.class.getDeclaredField("plots");
                fPlots.setAccessible(true);
                fPlots.set(null, DBFunc.getPlots());
            } catch (final Exception e) {
                PlotMain.sendConsoleSenderMessage("&3===FAILED&3===");
                e.printStackTrace();
                PlotMain.sendConsoleSenderMessage("&3===END OF STACKTRACE===");
            }
        } else {
            PlayerFunctions.sendMessage(plr, "This debug command can only be executed by console as it has been deemed unsafe if abused.");
        }
        return true;
    }
}
