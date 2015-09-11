package com.intellectualcrafters.plot.commands;

import java.net.URL;
import java.util.List;
import java.util.UUID;

import com.intellectualcrafters.jnbt.CompoundTag;
import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.config.Settings;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotId;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.object.RunnableVal;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.Permissions;
import com.intellectualcrafters.plot.util.SchematicHandler;
import com.intellectualcrafters.plot.util.TaskManager;
import com.plotsquared.general.commands.CommandDeclaration;

@CommandDeclaration(
command = "save",
aliases = { "backup" },
description = "Save your plot",
category = CommandCategory.ACTIONS,
requiredType = RequiredType.NONE,
permission = "plots.save")
public class Save extends SubCommand
{

    @Override
    public boolean onCommand(final PlotPlayer plr, final String[] args)
    {

        if (!Settings.METRICS)
        {
            MainUtil.sendMessage(plr, "&cPlease enable metrics in order to use this command.\n&7 - Or host it yourself if you don't like the free service");
            return false;
        }
        final String world = plr.getLocation().getWorld();
        if (!PS.get().isPlotWorld(world)) { return !sendMessage(plr, C.NOT_IN_PLOT_WORLD); }
        final Plot plot = MainUtil.getPlot(plr.getLocation());
        if (plot == null) { return !sendMessage(plr, C.NOT_IN_PLOT); }
        if (!plot.hasOwner())
        {
            MainUtil.sendMessage(plr, C.PLOT_UNOWNED);
            return false;
        }
        if (!plot.isOwner(plr.getUUID()) && !Permissions.hasPermission(plr, "plots.admin.command.save"))
        {
            MainUtil.sendMessage(plr, C.NO_PLOT_PERMS);
            return false;
        }
        if (MainUtil.runners.containsKey(plot))
        {
            MainUtil.sendMessage(plr, C.WAIT_FOR_TIMER);
            return false;
        }
        MainUtil.runners.put(plot, 1);
        SchematicHandler.manager.getCompoundTag(plot.world, plot.id, new RunnableVal<CompoundTag>()
        {
            @Override
            public void run()
            {
                TaskManager.runTaskAsync(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        final String time = (System.currentTimeMillis() / 1000) + "";
                        final String name = PS.get().IMP.getServerName().replaceAll("[^A-Za-z0-9]", "");
                        final int size = (plot.getTop().getX() - plot.getBottom().getX()) + 1;
                        final PlotId id = plot.id;
                        final String world = plot.world.replaceAll("[^A-Za-z0-9]", "");
                        final String file = time + "_" + world + "_" + id.x + "_" + id.y + "_" + size + "_" + name;
                        final UUID uuid = plr.getUUID();

                        final URL url = SchematicHandler.manager.upload(value, uuid, file);
                        if (url == null)
                        {
                            MainUtil.sendMessage(plr, C.SAVE_FAILED);
                            MainUtil.runners.remove(plot);
                            return;
                        }
                        MainUtil.sendMessage(plr, C.SAVE_SUCCESS);
                        final List<String> schematics = (List<String>) plr.getMeta("plot_schematics");
                        if (schematics != null)
                        {
                            schematics.add(file);
                        }
                        MainUtil.runners.remove(plot);
                    }
                });
            }
        });
        return true;
    }
}
