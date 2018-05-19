package com.intellectualcrafters.plot.commands;

import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.config.Settings;
import com.intellectualcrafters.plot.flag.Flags;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.Permissions;
import com.plotsquared.general.commands.Command;

public abstract class DownloadCommand extends SubCommand {

    public DownloadCommand() { super(MainCommand.getInstance(), true); }

    public DownloadCommand(Command parent, boolean isStatic) { super(parent, isStatic); }

    @Override
    public boolean onCommand(final PlotPlayer player, String[] args) {
        String world = player.getLocation().getWorld();
        if (!PS.get().hasPlotArea(world)) {
            return !sendMessage(player, C.NOT_IN_PLOT_WORLD);
        }
        final Plot plot = player.getCurrentPlot();
        if (plot == null) {
            return !sendMessage(player, C.NOT_IN_PLOT);
        }
        if (!plot.hasOwner()) {
            MainUtil.sendMessage(player, C.PLOT_UNOWNED);
            return false;
        }
        if ((Settings.Done.REQUIRED_FOR_DOWNLOAD && (!plot.getFlag(Flags.DONE).isPresent())) && !Permissions
                .hasPermission(player, C.PERMISSION_ADMIN_COMMAND_DOWNLOAD)) {
            MainUtil.sendMessage(player, C.DONE_NOT_DONE);
            return false;
        }
        if ((!plot.isOwner(player.getUUID())) && !Permissions.hasPermission(player, C.PERMISSION_ADMIN.s())) {
            MainUtil.sendMessage(player, C.NO_PLOT_PERMS);
            return false;
        }
        if (plot.getRunning() > 0) {
            MainUtil.sendMessage(player, C.WAIT_FOR_TIMER);
            return false;
        }
        if (download(player, plot, world)) {
            MainUtil.sendMessage(player, C.GENERATING_LINK);
            return true;
        } else {
            return false;
        }
    }

    public abstract boolean download(PlotPlayer player, Plot plot, String world);
}

