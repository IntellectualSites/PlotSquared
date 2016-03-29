package com.intellectualcrafters.plot.commands;

import com.intellectualcrafters.plot.object.PlotPlayer;
import com.plotsquared.general.commands.CommandDeclaration;

@CommandDeclaration(
        command = "chat",
        description = "Toggle plot chat on or off",
        usage = "/plot chat [on|off]",
        permission = "plots.chat",
        category = CommandCategory.CHAT,
        requiredType = RequiredType.NONE)
public class Chat extends SubCommand {

    @Override
    public boolean onCommand(PlotPlayer player, String[] args) {
        MainCommand.getInstance().toggle.chat(this, player, new String[0], null, null);
        return true;
    }
}
