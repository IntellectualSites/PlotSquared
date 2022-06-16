/*
 * PlotSquared, a land and world management plugin for Minecraft.
 * Copyright (C) IntellectualSites <https://intellectualsites.com>
 * Copyright (C) IntellectualSites team and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.plotsquared.core.command;

import com.plotsquared.core.configuration.Settings;
import com.plotsquared.core.configuration.caption.TranslatableCaption;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.util.task.TaskManager;

@CommandDeclaration(command = "confirm",
        permission = "plots.confirm",
        category = CommandCategory.INFO)
public class Confirm extends SubCommand {

    @Override
    public boolean onCommand(PlotPlayer<?> player, String[] args) {
        CmdInstance command = CmdConfirm.getPending(player);
        if (command == null) {
            player.sendMessage(TranslatableCaption.of("confirm.failed_confirm"));
            return false;
        }
        CmdConfirm.removePending(player);
        if ((System.currentTimeMillis() - command.timestamp)
                > Settings.Confirmation.CONFIRMATION_TIMEOUT_SECONDS * 1000) {
            player.sendMessage(TranslatableCaption.of("confirm.expired_confirm"));
            return false;
        }
        TaskManager.runTask(command.command);
        return true;
    }

}
