package com.intellectualcrafters.plot.commands;

import com.intellectualcrafters.jnbt.CompoundTag;
import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.config.Settings;
import com.intellectualcrafters.plot.flag.FlagManager;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.object.RunnableVal;
import com.intellectualcrafters.plot.util.BO3Handler;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.Permissions;
import com.intellectualcrafters.plot.util.SchematicHandler;
import com.intellectualcrafters.plot.util.StringMan;
import com.intellectualcrafters.plot.util.WorldUtil;
import com.plotsquared.general.commands.CommandDeclaration;
import java.net.URL;

@CommandDeclaration(usage = "/plot download [schematic|bo3|world]",
command = "download",
aliases = { "dl" },
category = CommandCategory.SCHEMATIC,
requiredType = RequiredType.NONE,
description = "Download your plot",
permission = "plots.download")
public class Download extends SubCommand {

    @Override
    public boolean onCommand(final PlotPlayer plr, final String[] args) {
        final String world = plr.getLocation().getWorld();
        if (!PS.get().hasPlotArea(world)) {
            return !sendMessage(plr, C.NOT_IN_PLOT_WORLD);
        }
        final Plot plot = plr.getCurrentPlot();
        if (plot == null) {
            return !sendMessage(plr, C.NOT_IN_PLOT);
        }
        if (!plot.hasOwner()) {
            MainUtil.sendMessage(plr, C.PLOT_UNOWNED);
            return false;
        }
        if ((!plot.isOwner(plr.getUUID()) || (Settings.DOWNLOAD_REQUIRES_DONE && (FlagManager.getPlotFlagRaw(plot, "done") != null))) && !Permissions.hasPermission(plr, "plots.admin.command.download")) {
            MainUtil.sendMessage(plr, C.NO_PLOT_PERMS);
            return false;
        }
        if (plot.getRunning() > 0) {
            MainUtil.sendMessage(plr, C.WAIT_FOR_TIMER);
            return false;
        }
        if (args.length == 0 || (args.length == 1 && StringMan.isEqualIgnoreCaseToAny(args[0], "sch", "schem", "schematic"))) {
            plot.addRunning();
            SchematicHandler.manager.getCompoundTag(plot, new RunnableVal<CompoundTag>() {
                @Override
                public void run(final CompoundTag value) {
                    plot.removeRunning();
                    SchematicHandler.manager.upload(value, null, null, new RunnableVal<URL>() {
                        @Override
                        public void run(URL url) {
                            if (url == null) {
                                MainUtil.sendMessage(plr, C.GENERATING_LINK_FAILED);
                                return;
                            }
                            MainUtil.sendMessage(plr, url.toString());
                        }
                    });
                }
            });
        } else if (args.length == 1 && StringMan.isEqualIgnoreCaseToAny(args[0], "bo3", "bo2", "b03", "b02")) {
            if (!Permissions.hasPermission(plr, "plots.download.bo3")) {
                C.NO_PERMISSION.send(plr, "plots.download.bo3");
            }
            plot.addRunning();
            BO3Handler.upload(plot, null, null, new RunnableVal<URL>() {
                @Override
                public void run(URL url) {
                    plot.removeRunning();
                    if (url == null) {
                        MainUtil.sendMessage(plr, C.GENERATING_LINK_FAILED);
                        return;
                    }
                    MainUtil.sendMessage(plr, url.toString());
                }
            });
        } else if (args.length == 1 && StringMan.isEqualIgnoreCaseToAny(args[0], "mcr", "world", "mca")) {
            if (!Permissions.hasPermission(plr, "plots.download.world")) {
                C.NO_PERMISSION.send(plr, "plots.download.world");
            }
            MainUtil.sendMessage(plr, "&cNote: The `.mca` files are 512x512");
            plot.addRunning();
            WorldUtil.IMP.upload(plot, null, null, new RunnableVal<URL>() {
                @Override
                public void run(URL url) {
                    plot.removeRunning();
                    if (url == null) {
                        MainUtil.sendMessage(plr, C.GENERATING_LINK_FAILED);
                        return;
                    }
                    MainUtil.sendMessage(plr, url.toString());
                }
            });
        }
        else {
            C.COMMAND_SYNTAX.send(plr, getUsage());
            return false;
        }
        MainUtil.sendMessage(plr, C.GENERATING_LINK);
        return true;
    }
}
