package com.intellectualcrafters.plot.commands;

import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.config.Settings;
import com.intellectualcrafters.plot.object.CmdInstance;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.CmdConfirm;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.TaskManager;
import com.plotsquared.general.commands.CommandDeclaration;

@CommandDeclaration(command = "confirm",
        permission = "plots.use",
        description = "Confirm an action",
        category = CommandCategory.INFO)
public class Confirm extends SubCommand {

    @Override
    public boolean onCommand(PlotPlayer player, String[] args) {
        CmdInstance command = CmdConfirm.getPending(player);
        if (command == null) {
            MainUtil.sendMessage(player, C.FAILED_CONFIRM);
            return false;
        }
        CmdConfirm.removePending(player);
        if ((System.currentTimeMillis() - command.timestamp) > Settings.Confirmation.CONFIRMATION_TIMEOUT_SECONDS * 1000) {
            MainUtil.sendMessage(player, C.EXPIRED_CONFIRM);
            return false;
        }
        TaskManager.runTask(command.command);
        return true;
    }
}
