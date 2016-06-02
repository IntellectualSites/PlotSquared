package com.intellectualcrafters.plot.commands;

import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.StringMan;
import com.plotsquared.general.commands.Argument;
import com.plotsquared.general.commands.CommandDeclaration;

@CommandDeclaration(
        command = "target",
        usage = "/plot target <<plot>|nearest>",
        description = "Target a plot with your compass",
        permission = "plots.target",
        requiredType = RequiredType.PLAYER,
        category = CommandCategory.INFO)
public class Target extends SubCommand {

    public Target() {
        super(Argument.PlotID);
    }

    @Override
    public boolean onCommand(PlotPlayer player, String[] args) {
        Location location = player.getLocation();
        if (!location.isPlotArea()) {
            MainUtil.sendMessage(player, C.NOT_IN_PLOT_WORLD);
            return false;
        }
        Plot target = null;
        if (StringMan.isEqualIgnoreCaseToAny(args[0], "near", "nearest")) {
            int distance = Integer.MAX_VALUE;
            for (Plot plot : PS.get().getPlots(location.getWorld())) {
                double current = plot.getCenter().getEuclideanDistanceSquared(location);
                if (current < distance) {
                    distance = (int) current;
                    target = plot;
                }
            }
            if (target == null) {
                MainUtil.sendMessage(player, C.FOUND_NO_PLOTS);
                return false;
            }
        } else if ((target = MainUtil.getPlotFromString(player, args[0], true)) == null) {
            return false;
        }
        player.setCompassTarget(target.getCenter());
        MainUtil.sendMessage(player, C.COMPASS_TARGET);
        return true;
    }
}
