package com.intellectualcrafters.plot.commands;

import java.net.URL;

import com.intellectualcrafters.jnbt.CompoundTag;
import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.config.Settings;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.object.RunnableVal;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.Permissions;
import com.intellectualcrafters.plot.util.SchematicHandler;
import com.intellectualcrafters.plot.util.TaskManager;
import com.intellectualsites.commands.CommandDeclaration;
import com.intellectualsites.commands.callers.CommandCaller;

@CommandDeclaration(
        command = "download",
        aliases = {"dl"},
        category = CommandCategory.ACTIONS,
        requiredType = PlotPlayer.class,
        description = "Download your plot",
        permission = "plots.download"
)
public class Download extends SubCommand {

    @Override
    public boolean onCommand(final CommandCaller caller, final String[] args) {
        final PlotPlayer plr = (PlotPlayer) caller.getSuperCaller();
        if (!Settings.METRICS) {
            MainUtil.sendMessage(plr, "&cPlease enable metrics in order to use this command.\n&7 - Or host it yourself if you don't like the free service");
            return false;
        }
        final String world = plr.getLocation().getWorld();
        if (!PS.get().isPlotWorld(world)) {
            return !sendMessage(plr, C.NOT_IN_PLOT_WORLD);
        }
        final Plot plot = MainUtil.getPlot(plr.getLocation());
        if (plot == null) {
            return !sendMessage(plr, C.NOT_IN_PLOT);
        }
        if (!plot.hasOwner()) {
            MainUtil.sendMessage(plr, C.PLOT_UNOWNED);
            return false;
        }
        if (!plot.isOwner(plr.getUUID()) && !Permissions.hasPermission(plr, "plots.admin.command.download")) {
            MainUtil.sendMessage(plr, C.NO_PLOT_PERMS);
            return false;
        }
        if (MainUtil.runners.containsKey(plot)) {
            MainUtil.sendMessage(plr, C.WAIT_FOR_TIMER);
            return false;
        }
        MainUtil.runners.put(plot, 1);
        MainUtil.sendMessage(plr, C.GENERATING_LINK);
        SchematicHandler.manager.getCompoundTag(plot.world, plot.id, new RunnableVal<CompoundTag>() {
            @Override
            public void run() {
                TaskManager.runTaskAsync(new Runnable() {
                    @Override
                    public void run() {
                        URL url = SchematicHandler.manager.upload(value, null, null);
                        if (url == null) {
                            MainUtil.sendMessage(plr, C.GENERATING_LINK_FAILED);
                            MainUtil.runners.remove(plot);
                            return;
                        }
                        MainUtil.sendMessage(plr, url.toString());
                        MainUtil.runners.remove(plot);
                    }
                });
            }
        });
        return true;
    }
}
