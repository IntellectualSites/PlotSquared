package com.plotsquared.core.command;

import com.plotsquared.core.player.PlotPlayer;

@CommandDeclaration(command = "chat",
    description = "Toggle plot chat on or off",
    usage = "/plot chat [on|off]",
    permission = "plots.chat",
    category = CommandCategory.CHAT,
    requiredType = RequiredType.PLAYER)
public class Chat extends SubCommand {

    @Override public boolean onCommand(PlotPlayer player, String[] args) {
        MainCommand.getInstance().toggle.chat(this, player, new String[0], null, null);
        return true;
    }
}
