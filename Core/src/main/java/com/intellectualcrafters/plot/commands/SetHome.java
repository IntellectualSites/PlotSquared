package com.intellectualcrafters.plot.commands;

import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.object.BlockLoc;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.MainUtil;
import com.plotsquared.general.commands.CommandDeclaration;

@CommandDeclaration(command = "sethome", permission = "plots.set.home", description = "Set the plot home", usage = "/plot sethome [none]", aliases = {
    "sh", "seth"}, category = CommandCategory.SETTINGS, requiredType = RequiredType.NONE)
public class SetHome extends SetCommand {

    @Override public boolean set(PlotPlayer player, Plot plot, String value) {
        switch (value.toLowerCase()) {
            case "unset":
            case "remove":
            case "none": {
                Plot base = plot.getBasePlot(false);
                base.setHome(null);
                return MainUtil.sendMessage(player, C.POSITION_UNSET);
            }
            case "":
                Plot base = plot.getBasePlot(false);
                Location bot = base.getBottomAbs();
                Location loc = player.getLocationFull();
                BlockLoc rel =
                    new BlockLoc(loc.getX() - bot.getX(), loc.getY(), loc.getZ() - bot.getZ(),
                        loc.getYaw(), loc.getPitch());
                base.setHome(rel);
                return MainUtil.sendMessage(player, C.POSITION_SET);
            default:
                MainUtil.sendMessage(player, C.HOME_ARGUMENT);
                return false;
        }
    }
}
