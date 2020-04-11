package com.plotsquared.commands;

import com.plotsquared.config.CaptionUtility;
import com.plotsquared.config.Captions;
import com.plotsquared.location.Location;
import com.plotsquared.plot.Plot;
import com.plotsquared.player.PlotPlayer;
import com.plotsquared.util.MainUtil;
import com.plotsquared.util.Permissions;
import com.plotsquared.util.StringMan;

public abstract class SetCommand extends SubCommand {

    @Override public boolean onCommand(PlotPlayer player, String[] args) {
        Location location = player.getLocation();
        Plot plot = location.getPlotAbs();
        if (plot == null) {
            return !sendMessage(player, Captions.NOT_IN_PLOT);
        }
        if (!plot.hasOwner()) {
            if (!Permissions.hasPermission(player, CaptionUtility
                .format(player, Captions.PERMISSION_ADMIN_COMMAND.getTranslated(), getFullId()))) {
                MainUtil.sendMessage(player, Captions.NO_PERMISSION, CaptionUtility
                    .format(player, Captions.PERMISSION_ADMIN_COMMAND.getTranslated(),
                        getFullId()));
                MainUtil.sendMessage(player, Captions.PLOT_NOT_CLAIMED);
                return false;
            }
        }
        if (!plot.isOwner(player.getUUID()) && !plot.getTrusted().contains(player.getUUID())) {
            if (!Permissions.hasPermission(player, CaptionUtility
                .format(player, Captions.PERMISSION_ADMIN_COMMAND.getTranslated(), getFullId()))) {
                MainUtil.sendMessage(player, Captions.NO_PERMISSION, CaptionUtility
                    .format(player, Captions.PERMISSION_ADMIN_COMMAND.getTranslated(),
                        getFullId()));
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
