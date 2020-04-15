package com.plotsquared.core.command;

import com.plotsquared.core.player.PlotPlayer;

@CommandDeclaration(command = "dislike",
    permission = "plots.dislike",
    description = "Dislike the plot",
    usage = "/plot dislike [next|purge]",
    category = CommandCategory.INFO,
    requiredType = RequiredType.PLAYER)
public class Dislike extends SubCommand {

    @Override public boolean onCommand(PlotPlayer player, String[] args) {
        return Like.handleLike(player, args, false);
    }

}
