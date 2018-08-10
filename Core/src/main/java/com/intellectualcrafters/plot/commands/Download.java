package com.intellectualcrafters.plot.commands;

import com.intellectualcrafters.jnbt.CompoundTag;
import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.config.Settings;
import com.intellectualcrafters.plot.flag.Flags;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.object.RunnableVal;
import com.intellectualcrafters.plot.util.*;
import com.plotsquared.general.commands.CommandDeclaration;

import java.net.URL;

@CommandDeclaration(usage = "/plot download [schematic|bo3|world]", command = "download", aliases = {
    "dl"}, category = CommandCategory.SCHEMATIC, requiredType = RequiredType.NONE, description = "Download your plot", permission = "plots.download")
public class Download extends SubCommand {

    @Override public boolean onCommand(final PlotPlayer player, String[] args) {
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
        if ((Settings.Done.REQUIRED_FOR_DOWNLOAD && (!plot.getFlag(Flags.DONE).isPresent()))
            && !Permissions.hasPermission(player, C.PERMISSION_ADMIN_COMMAND_DOWNLOAD)) {
            MainUtil.sendMessage(player, C.DONE_NOT_DONE);
            return false;
        }
        if ((!plot.isOwner(player.getUUID())) && !Permissions
            .hasPermission(player, C.PERMISSION_ADMIN.s())) {
            MainUtil.sendMessage(player, C.NO_PLOT_PERMS);
            return false;
        }
        if (plot.getRunning() > 0) {
            MainUtil.sendMessage(player, C.WAIT_FOR_TIMER);
            return false;
        }
        if (args.length == 0 || (args.length == 1 && StringMan
            .isEqualIgnoreCaseToAny(args[0], "sch", "schem", "schematic"))) {
            if (plot.getVolume() > Integer.MAX_VALUE) {
                C.SCHEMATIC_TOO_LARGE.send(player);
                return false;
            }
            plot.addRunning();
            SchematicHandler.manager.getCompoundTag(plot, new RunnableVal<CompoundTag>() {
                @Override public void run(CompoundTag value) {
                    plot.removeRunning();
                    SchematicHandler.manager.upload(value, null, null, new RunnableVal<URL>() {
                        @Override public void run(URL url) {
                            if (url == null) {
                                MainUtil.sendMessage(player, C.GENERATING_LINK_FAILED);
                                return;
                            }
                            MainUtil.sendMessage(player, url.toString());
                        }
                    });
                }
            });
        } else if (args.length == 1 && StringMan
            .isEqualIgnoreCaseToAny(args[0], "bo3", "bo2", "b03", "b02")) {
            if (!Permissions.hasPermission(player, C.PERMISSION_DOWNLOAD_BO3)) {
                C.NO_PERMISSION.send(player, C.PERMISSION_DOWNLOAD_BO3);
                return false;
            }
            if (plot.getVolume() > 128d * 128d * 256) {
                C.SCHEMATIC_TOO_LARGE.send(player);
                return false;
            }
            plot.addRunning();
            BO3Handler.upload(plot, null, null, new RunnableVal<URL>() {
                @Override public void run(URL url) {
                    plot.removeRunning();
                    if (url == null) {
                        MainUtil.sendMessage(player, C.GENERATING_LINK_FAILED);
                        return;
                    }
                    MainUtil.sendMessage(player, url.toString());
                }
            });
        } else if (args.length == 1 && StringMan
            .isEqualIgnoreCaseToAny(args[0], "mcr", "world", "mca")) {
            if (!Permissions.hasPermission(player, C.PERMISSION_DOWNLOAD_WORLD)) {
                C.NO_PERMISSION.send(player, C.PERMISSION_DOWNLOAD_WORLD);
                return false;
            }
            MainUtil.sendMessage(player, "&cNote: The `.mca` files are 512x512");
            plot.addRunning();
            WorldUtil.IMP.saveWorld(world);
            WorldUtil.IMP.upload(plot, null, null, new RunnableVal<URL>() {
                @Override public void run(URL url) {
                    plot.removeRunning();
                    if (url == null) {
                        MainUtil.sendMessage(player, C.GENERATING_LINK_FAILED);
                        return;
                    }
                    MainUtil.sendMessage(player, url.toString());
                }
            });
        } else {
            C.COMMAND_SYNTAX.send(player, getUsage());
            return false;
        }
        MainUtil.sendMessage(player, C.GENERATING_LINK);
        return true;
    }
}
