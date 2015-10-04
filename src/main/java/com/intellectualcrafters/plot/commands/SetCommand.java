package com.intellectualcrafters.plot.commands;

import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.Permissions;
import com.intellectualcrafters.plot.util.StringMan;

public abstract class SetCommand extends SubCommand {
    
    @Override
    public boolean onCommand(PlotPlayer plr, String[] args) {
        final Location loc = plr.getLocation();
        final Plot plot = MainUtil.getPlotAbs(loc);
        if (plot == null) {
            return !sendMessage(plr, C.NOT_IN_PLOT);
        }
        if (!plot.hasOwner()) {
            if (!Permissions.hasPermission(plr, "plots.admin.command." + getCommand())) {
                MainUtil.sendMessage(plr, C.NO_PERMISSION, "plots.admin.command." + getCommand());
                MainUtil.sendMessage(plr, C.PLOT_NOT_CLAIMED);
                return false;
            }
        }
        if (!plot.isOwner(plr.getUUID())) {
            if (!Permissions.hasPermission(plr, "plots.admin.command." + getCommand())) {
                MainUtil.sendMessage(plr, C.NO_PERMISSION, "plots.admin.command." + getCommand());
                MainUtil.sendMessage(plr, C.NO_PLOT_PERMS);
                return false;
            }
        }
        if (args.length == 0) {
            return set(plr, plot, "");
        }
        return set(plr, plot, StringMan.join(args, " "));
    }
    
    public abstract boolean set(PlotPlayer plr, Plot plot, String value);

}
