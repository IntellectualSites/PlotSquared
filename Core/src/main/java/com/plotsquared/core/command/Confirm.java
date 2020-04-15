package com.plotsquared.core.command;

import com.plotsquared.core.config.Captions;
import com.plotsquared.core.config.Settings;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.util.MainUtil;
import com.plotsquared.core.util.task.TaskManager;

@CommandDeclaration(command = "confirm",
    permission = "plots.use",
    description = "Confirm an action",
    category = CommandCategory.INFO)
public class Confirm extends SubCommand {

    @Override public boolean onCommand(PlotPlayer player, String[] args) {
        CmdInstance command = CmdConfirm.getPending(player);
        if (command == null) {
            MainUtil.sendMessage(player, Captions.FAILED_CONFIRM);
            return false;
        }
        CmdConfirm.removePending(player);
        if ((System.currentTimeMillis() - command.timestamp)
            > Settings.Confirmation.CONFIRMATION_TIMEOUT_SECONDS * 1000) {
            MainUtil.sendMessage(player, Captions.EXPIRED_CONFIRM);
            return false;
        }
        TaskManager.runTask(command.command);
        return true;
    }
}
