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

import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.object.CmdInstance;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.CmdConfirm;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.TaskManager;

/**
 * @author Citymonstret
 */
public class Confirm extends SubCommand {
    public Confirm() {
        super(Command.CONFIRM, "Confirm command execution", "confirm", CommandCategory.ACTIONS, true);
    }

    @Override
    public boolean execute(final PlotPlayer plr, final String... args) {
    	CmdInstance command = CmdConfirm.getPending(plr);
    	if (command == null) {
    		MainUtil.sendMessage(plr, C.FAILED_CONFIRM);
    		return false;
    	}
    	CmdConfirm.removePending(plr);
    	if (System.currentTimeMillis() - command.timestamp > 10000) {
    	    MainUtil.sendMessage(plr, C.FAILED_CONFIRM);
    	    return false;
    	}
    	TaskManager.runTask(command.command);
    	return true;
    }
}
