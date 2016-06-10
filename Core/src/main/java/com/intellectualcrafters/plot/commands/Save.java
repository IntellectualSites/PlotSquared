package com.intellectualcrafters.plot.commands;

import com.intellectualcrafters.jnbt.CompoundTag;
import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.config.Settings;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotId;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.object.RunnableVal;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.Permissions;
import com.intellectualcrafters.plot.util.SchematicHandler;
import com.intellectualcrafters.plot.util.TaskManager;
import com.plotsquared.general.commands.CommandDeclaration;

import java.net.URL;
import java.util.List;
import java.util.UUID;

@CommandDeclaration(command = "save",
        aliases = {"backup"},
        description = "Save your plot",
        category = CommandCategory.SCHEMATIC,
        requiredType = RequiredType.NONE,
        permission = "plots.save")
public class Save extends SubCommand {

    @Override
    public boolean onCommand(final PlotPlayer player, String[] args) {

        if (!Settings.Enabled_Components.METRICS) {
            MainUtil.sendMessage(player,
                    "&cPlease enable metrics in order to use this command.\n&7 - Or host it yourself if you don't like the free service");
            return false;
        }
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
        if (!plot.isOwner(player.getUUID()) && !Permissions.hasPermission(player, "plots.admin.command.save")) {
            MainUtil.sendMessage(player, C.NO_PLOT_PERMS);
            return false;
        }
        if (plot.getRunning() > 0) {
            MainUtil.sendMessage(player, C.WAIT_FOR_TIMER);
            return false;
        }
        plot.addRunning();
        SchematicHandler.manager.getCompoundTag(plot, new RunnableVal<CompoundTag>() {
            @Override
            public void run(final CompoundTag value) {
                TaskManager.runTaskAsync(new Runnable() {
                    @Override
                    public void run() {
                        String time = (System.currentTimeMillis() / 1000) + "";
                        String name = PS.get().IMP.getServerName().replaceAll("[^A-Za-z0-9]", "");
                        Location[] corners = plot.getCorners();
                        int size = (corners[1].getX() - corners[0].getX()) + 1;
                        PlotId id = plot.getId();
                        String world = plot.getArea().toString().replaceAll(";", "-").replaceAll("[^A-Za-z0-9]", "");
                        final String file = time + '_' + world + '_' + id.x + '_' + id.y + '_' + size + '_' + name;
                        UUID uuid = player.getUUID();
                        SchematicHandler.manager.upload(value, uuid, file, new RunnableVal<URL>() {
                            @Override
                            public void run(URL url) {
                                plot.removeRunning();
                                if (url == null) {
                                    MainUtil.sendMessage(player, C.SAVE_FAILED);
                                    return;
                                }
                                MainUtil.sendMessage(player, C.SAVE_SUCCESS);
                                List<String> schematics = player.getMeta("plot_schematics");
                                if (schematics != null) {
                                    schematics.add(file);
                                }
                            }
                        });
                    }
                });
            }
        });
        return true;
    }
}
