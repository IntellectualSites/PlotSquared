package com.intellectualcrafters.plot.commands;

import java.net.URL;

import com.intellectualcrafters.jnbt.CompoundTag;
import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.object.PlotWorld;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.SchematicHandler;
import com.intellectualcrafters.plot.util.TaskManager;

public class Download extends SubCommand {
    public Download() {
        super(Command.DOWNLOAD, "Download your plot", "dl", CommandCategory.ACTIONS, true);
    }

    @Override
    public boolean execute(final PlotPlayer plr, String... args) {
        final String world = plr.getLocation().getWorld();
        if (!PS.get().isPlotWorld(world)) {
            return !sendMessage(plr, C.NOT_IN_PLOT_WORLD);
        }
        final Plot plot = MainUtil.getPlot(plr.getLocation());
        if (plot == null) {
            return !sendMessage(plr, C.NOT_IN_PLOT);
        }
        if (MainUtil.runners.containsKey(plot)) {
            MainUtil.sendMessage(plr, C.WAIT_FOR_TIMER);
            return false;
        }
        MainUtil.runners.put(plot, 1);
        MainUtil.sendMessage(plr, C.GENERATING_LINK);
        final CompoundTag tag = SchematicHandler.manager.getCompoundTag(plot.world, plot.id);
        TaskManager.runTaskAsync(new Runnable() {
            @Override
            public void run() {
                URL url = SchematicHandler.manager.upload(tag);
                if (url == null) {
                    MainUtil.sendMessage(plr, C.GENERATING_LINK_FAILED);
                    return;
                }
                MainUtil.sendMessage(plr, url.toString());
                MainUtil.runners.remove(plot);
            }
        });
        return true;
    }
}
