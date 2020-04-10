package com.github.intellectualsites.plotsquared.commands;

import com.github.intellectualsites.plotsquared.PlotSquared;
import com.github.intellectualsites.plotsquared.plot.config.Captions;
import com.github.intellectualsites.plotsquared.events.PlotFlagAddEvent;
import com.github.intellectualsites.plotsquared.events.PlotFlagRemoveEvent;
import com.github.intellectualsites.plotsquared.events.Result;
import com.github.intellectualsites.plotsquared.plot.flags.implementations.DescriptionFlag;
import com.github.intellectualsites.plotsquared.plot.object.Plot;
import com.github.intellectualsites.plotsquared.player.PlotPlayer;
import com.github.intellectualsites.plotsquared.util.MainUtil;

@CommandDeclaration(command = "setdescription",
    permission = "plots.set.desc",
    description = "Set the plot description",
    usage = "/plot desc <description>",
    aliases = {"desc", "setdesc", "setd", "description"},
    category = CommandCategory.SETTINGS,
    requiredType = RequiredType.PLAYER)
public class Desc extends SetCommand {

    @Override public boolean set(PlotPlayer player, Plot plot, String desc) {
        if (desc.isEmpty()) {
            PlotFlagRemoveEvent event = PlotSquared.get().getEventDispatcher()
                .callFlagRemove(plot.getFlagContainer().getFlag(DescriptionFlag.class), plot);
            if (event.getEventResult() == Result.DENY) {
                sendMessage(player, Captions.EVENT_DENIED, "Description removal");
                return false;
            }
            plot.removeFlag(event.getFlag());
            MainUtil.sendMessage(player, Captions.DESC_UNSET);
            return true;
        }
        PlotFlagAddEvent event = PlotSquared.get().getEventDispatcher().callFlagAdd(
            plot.getFlagContainer().getFlag(DescriptionFlag.class).createFlagInstance(desc), plot);
        if (event.getEventResult() == Result.DENY) {
            sendMessage(player, Captions.EVENT_DENIED, "Description set");
            return false;
        }
        boolean result = plot.setFlag(event.getFlag());
        if (!result) {
            MainUtil.sendMessage(player, Captions.FLAG_NOT_ADDED);
            return false;
        }
        MainUtil.sendMessage(player, Captions.DESC_SET);
        return true;
    }
}
