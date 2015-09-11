package com.intellectualcrafters.plot.commands;

import com.intellectualcrafters.plot.object.PlotPlayer;
import com.plotsquared.general.commands.CommandDeclaration;

@CommandDeclaration(
command = "chat",
description = "Toggle plot chat on or off",
usage = "/plot chat [on|off]",
permission = "plots.chat",
category = CommandCategory.ACTIONS,
requiredType = RequiredType.NONE)
public class Chat extends SubCommand
{

    @Override
    public boolean onCommand(final PlotPlayer player, final String... args)
    {
        return MainCommand.onCommand(player, "plot", new String[] { "toggle", "chat" });
    }
}
