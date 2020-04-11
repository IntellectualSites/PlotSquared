package com.plotsquared.commands;

import com.plotsquared.config.Captions;
import com.plotsquared.config.Settings;
import com.plotsquared.player.PlotPlayer;
import com.plotsquared.util.MainUtil;
import com.plotsquared.util.tasks.TaskManager;

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
