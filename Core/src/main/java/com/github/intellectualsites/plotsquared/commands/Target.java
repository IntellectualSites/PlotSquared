package com.github.intellectualsites.plotsquared.commands;

import com.github.intellectualsites.plotsquared.PlotSquared;
import com.github.intellectualsites.plotsquared.plot.config.Captions;
import com.github.intellectualsites.plotsquared.plot.object.Location;
import com.github.intellectualsites.plotsquared.plot.object.Plot;
import com.github.intellectualsites.plotsquared.player.PlotPlayer;
import com.github.intellectualsites.plotsquared.util.MainUtil;
import com.github.intellectualsites.plotsquared.util.StringMan;

@CommandDeclaration(command = "target",
    usage = "/plot target <<plot>|nearest>",
    description = "Target a plot with your compass",
    permission = "plots.target",
    requiredType = RequiredType.PLAYER,
    category = CommandCategory.INFO)
public class Target extends SubCommand {

    public Target() {
        super(Argument.PlotID);
    }

    @Override public boolean onCommand(PlotPlayer player, String[] args) {
        Location location = player.getLocation();
        if (!location.isPlotArea()) {
            MainUtil.sendMessage(player, Captions.NOT_IN_PLOT_WORLD);
            return false;
        }
        Plot target = null;
        if (StringMan.isEqualIgnoreCaseToAny(args[0], "near", "nearest")) {
            int distance = Integer.MAX_VALUE;
            for (Plot plot : PlotSquared.get().getPlots(location.getWorld())) {
                double current = plot.getCenterSynchronous().getEuclideanDistanceSquared(location);
                if (current < distance) {
                    distance = (int) current;
                    target = plot;
                }
            }
            if (target == null) {
                MainUtil.sendMessage(player, Captions.FOUND_NO_PLOTS);
                return false;
            }
        } else if ((target = MainUtil.getPlotFromString(player, args[0], true)) == null) {
            return false;
        }
        target.getCenter(player::setCompassTarget);
        MainUtil.sendMessage(player, Captions.COMPASS_TARGET);
        return true;
    }
}
