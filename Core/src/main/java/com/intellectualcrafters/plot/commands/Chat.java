package com.intellectualcrafters.plot.commands;

import com.intellectualcrafters.plot.object.PlotPlayer;
import com.plotsquared.general.commands.CommandDeclaration;

@CommandDeclaration(
        command = "chat",
        description = "Toggle plot chat on or off",
        usage = "/plot chat",
        permission = "plots.chat",
        category = CommandCategory.CHAT,
        requiredType = RequiredType.NONE)
public class Chat extends ToggleChat {

    public Chat() { super(MainCommand.getInstance(), true); }

    @Override
    public boolean onCommand(PlotPlayer player, String[] args) {
        return super.onCommand(player, args);
    }
}
