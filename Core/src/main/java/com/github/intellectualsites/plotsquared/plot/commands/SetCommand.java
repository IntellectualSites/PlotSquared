package com.github.intellectualsites.plotsquared.plot.commands;

import com.github.intellectualsites.plotsquared.plot.config.Captions;
import com.github.intellectualsites.plotsquared.plot.object.Location;
import com.github.intellectualsites.plotsquared.plot.object.Plot;
import com.github.intellectualsites.plotsquared.plot.object.PlotPlayer;
import com.github.intellectualsites.plotsquared.plot.util.MainUtil;
import com.github.intellectualsites.plotsquared.plot.util.Permissions;
import com.github.intellectualsites.plotsquared.plot.util.StringMan;

public abstract class SetCommand extends SubCommand {

    @Override public boolean onCommand(PlotPlayer player, String[] args) {
        Location location = player.getLocation();
        Plot plot = location.getPlotAbs();
        if (plot == null) {
            return !sendMessage(player, Captions.NOT_IN_PLOT);
        }
        if (!plot.hasOwner()) {
            if (!Permissions
                .hasPermission(player, Captions.PERMISSION_ADMIN_COMMAND.f(getFullId()))) {
                MainUtil.sendMessage(player, Captions.NO_PERMISSION,
                    Captions.PERMISSION_ADMIN_COMMAND.f(getFullId()));
                MainUtil.sendMessage(player, Captions.PLOT_NOT_CLAIMED);
                return false;
            }
        }
        if (!plot.isOwner(player.getUUID())) {
            if (!Permissions
                .hasPermission(player, Captions.PERMISSION_ADMIN_COMMAND.f(getFullId()))) {
                MainUtil.sendMessage(player, Captions.NO_PERMISSION,
                    Captions.PERMISSION_ADMIN_COMMAND.f(getFullId()));
                MainUtil.sendMessage(player, Captions.NO_PLOT_PERMS);
                return false;
            }
        }
        if (args.length == 0) {
            return set(player, plot, "");
        }
        return set(player, plot, StringMan.join(args, " "));
    }

    public abstract boolean set(PlotPlayer player, Plot plot, String value);

}
