package com.github.intellectualsites.plotsquared.plot.commands;

import com.github.intellectualsites.plotsquared.commands.CommandDeclaration;
import com.github.intellectualsites.plotsquared.plot.config.Captions;
import com.github.intellectualsites.plotsquared.plot.object.BlockLoc;
import com.github.intellectualsites.plotsquared.plot.object.Location;
import com.github.intellectualsites.plotsquared.plot.object.Plot;
import com.github.intellectualsites.plotsquared.plot.object.PlotPlayer;
import com.github.intellectualsites.plotsquared.plot.util.MainUtil;

@CommandDeclaration(command = "sethome", permission = "plots.set.home",
    description = "Set the plot home to your current position", usage = "/plot sethome [none]",
    aliases = {"sh", "seth"}, category = CommandCategory.SETTINGS, requiredType = RequiredType.PLAYER)
public class SetHome extends SetCommand {

    @Override public boolean set(PlotPlayer player, Plot plot, String value) {
        switch (value.toLowerCase()) {
            case "unset":
            case "reset":
            case "remove":
            case "none": {
                Plot base = plot.getBasePlot(false);
                base.setHome(null);
                return MainUtil.sendMessage(player, Captions.POSITION_UNSET);
            }
            case "":
                Plot base = plot.getBasePlot(false);
                Location bot = base.getBottomAbs();
                Location loc = player.getLocationFull();
                BlockLoc rel =
                    new BlockLoc(loc.getX() - bot.getX(), loc.getY(), loc.getZ() - bot.getZ(),
                        loc.getYaw(), loc.getPitch());
                base.setHome(rel);
                return MainUtil.sendMessage(player, Captions.POSITION_SET);
            default:
                MainUtil.sendMessage(player, Captions.HOME_ARGUMENT);
                return false;
        }
    }
}
