package com.github.intellectualsites.plotsquared.plot.commands;

import com.github.intellectualsites.plotsquared.commands.CommandDeclaration;
import com.github.intellectualsites.plotsquared.plot.PlotSquared;
import com.github.intellectualsites.plotsquared.plot.config.Captions;
import com.github.intellectualsites.plotsquared.plot.object.*;
import com.github.intellectualsites.plotsquared.plot.util.MainUtil;
import com.github.intellectualsites.plotsquared.plot.util.Permissions;
import com.github.intellectualsites.plotsquared.plot.util.SchematicHandler;
import com.github.intellectualsites.plotsquared.plot.util.TaskManager;
import com.sk89q.jnbt.CompoundTag;

import java.net.URL;
import java.util.List;
import java.util.UUID;

@CommandDeclaration(command = "save", aliases = {"backup"}, description = "Save your plot",
    category = CommandCategory.SCHEMATIC, requiredType = RequiredType.NONE,
    permission = "plots.save") public class Save extends SubCommand {

    @Override public boolean onCommand(final PlotPlayer player, String[] args) {
        String world = player.getLocation().getWorld();
        if (!PlotSquared.get().hasPlotArea(world)) {
            return !sendMessage(player, Captions.NOT_IN_PLOT_WORLD);
        }
        final Plot plot = player.getCurrentPlot();
        if (plot == null) {
            return !sendMessage(player, Captions.NOT_IN_PLOT);
        }
        if (!plot.hasOwner()) {
            MainUtil.sendMessage(player, Captions.PLOT_UNOWNED);
            return false;
        }
        if (!plot.isOwner(player.getUUID()) && !Permissions
            .hasPermission(player, Captions.PERMISSION_ADMIN_COMMAND_SAVE)) {
            MainUtil.sendMessage(player, Captions.NO_PLOT_PERMS);
            return false;
        }
        if (plot.getRunning() > 0) {
            MainUtil.sendMessage(player, Captions.WAIT_FOR_TIMER);
            return false;
        }
        plot.addRunning();
        SchematicHandler.manager.getCompoundTag(plot, new RunnableVal<CompoundTag>() {
            @Override public void run(final CompoundTag value) {
                TaskManager.runTaskAsync(() -> {
                    String time = (System.currentTimeMillis() / 1000) + "";
                    Location[] corners = plot.getCorners();
                    corners[0].setY(0);
                    corners[1].setY(255);
                    int size = (corners[1].getX() - corners[0].getX()) + 1;
                    PlotId id = plot.getId();
                    String world1 = plot.getArea().toString().replaceAll(";", "-")
                        .replaceAll("[^A-Za-z0-9]", "");
                    final String file = time + '_' + world1 + '_' + id.x + '_' + id.y + '_' + size;
                    UUID uuid = player.getUUID();
                    SchematicHandler.manager.upload(value, uuid, file, new RunnableVal<URL>() {
                        @Override public void run(URL url) {
                            plot.removeRunning();
                            if (url == null) {
                                MainUtil.sendMessage(player, Captions.SAVE_FAILED);
                                return;
                            }
                            MainUtil.sendMessage(player, Captions.SAVE_SUCCESS);
                            List<String> schematics = player.getMeta("plot_schematics");
                            if (schematics != null) {
                                schematics.add(file + ".schematic");
                            }
                        }
                    });
                });
            }
        });
        return true;
    }
}
