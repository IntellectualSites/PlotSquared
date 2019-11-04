package com.github.intellectualsites.plotsquared.plot.commands;

import com.github.intellectualsites.plotsquared.commands.CommandDeclaration;
import com.github.intellectualsites.plotsquared.plot.config.Captions;
import com.github.intellectualsites.plotsquared.plot.config.Settings;
import com.github.intellectualsites.plotsquared.plot.object.CmdInstance;
import com.github.intellectualsites.plotsquared.plot.object.PlotPlayer;
import com.github.intellectualsites.plotsquared.plot.util.CmdConfirm;
import com.github.intellectualsites.plotsquared.plot.util.MainUtil;
import com.github.intellectualsites.plotsquared.plot.util.TaskManager;

@CommandDeclaration(command = "confirm", permission = "plots.use",
    description = "Confirm an action", category = CommandCategory.INFO) public class Confirm
    extends SubCommand {

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
