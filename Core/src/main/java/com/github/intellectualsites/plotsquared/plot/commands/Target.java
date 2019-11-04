package com.github.intellectualsites.plotsquared.plot.commands;

import com.github.intellectualsites.plotsquared.plot.util.block.BlockUtil;

import com.github.intellectualsites.plotsquared.commands.Argument;
import com.github.intellectualsites.plotsquared.commands.CommandDeclaration;
import com.github.intellectualsites.plotsquared.plot.PlotSquared;
import com.github.intellectualsites.plotsquared.plot.config.Captions;
import com.github.intellectualsites.plotsquared.plot.object.Location;
import com.github.intellectualsites.plotsquared.plot.object.Plot;
import com.github.intellectualsites.plotsquared.plot.object.PlotPlayer;
import com.github.intellectualsites.plotsquared.plot.util.MainUtil;
import com.github.intellectualsites.plotsquared.plot.util.StringMan;

@CommandDeclaration(command = "target", usage = "/plot target <<plot>|nearest>",
    description = "Target a plot with your compass", permission = "plots.target",
    requiredType = RequiredType.PLAYER, category = CommandCategory.INFO) public class Target
    extends SubCommand {

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
                double current = plot.getCenter().getEuclideanDistanceSquared(location);
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
        player.setCompassTarget(target.getCenter());
        MainUtil.sendMessage(player, Captions.COMPASS_TARGET);
        return true;
    }
}
