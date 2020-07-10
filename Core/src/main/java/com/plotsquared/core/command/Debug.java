/*
 *       _____  _       _    _____                                _
 *      |  __ \| |     | |  / ____|                              | |
 *      | |__) | | ___ | |_| (___   __ _ _   _  __ _ _ __ ___  __| |
 *      |  ___/| |/ _ \| __|\___ \ / _` | | | |/ _` | '__/ _ \/ _` |
 *      | |    | | (_) | |_ ____) | (_| | |_| | (_| | | |  __/ (_| |
 *      |_|    |_|\___/ \__|_____/ \__, |\__,_|\__,_|_|  \___|\__,_|
 *                                    | |
 *                                    |_|
 *            PlotSquared plot management system for Minecraft
 *                  Copyright (C) 2020 IntellectualSites
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.plotsquared.core.command;

import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.configuration.Captions;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.world.PlotAreaManager;
import com.plotsquared.core.util.MainUtil;
import com.plotsquared.core.util.RegionManager;
import com.plotsquared.core.util.StringMan;
import com.plotsquared.core.util.entity.EntityCategories;
import com.plotsquared.core.util.entity.EntityCategory;
import com.plotsquared.core.util.query.PlotQuery;
import com.plotsquared.core.util.task.TaskManager;
import com.plotsquared.core.uuid.UUIDMapping;
import com.sk89q.worldedit.world.entity.EntityType;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Comparator;
import java.util.Map;

@CommandDeclaration(command = "debug",
    category = CommandCategory.DEBUG,
    description = "Show debug information",
    usage = "/plot debug [msg]",
    permission = "plots.admin")
public class Debug extends SubCommand {

    private final PlotAreaManager plotAreaManager;

    public Debug(@NotNull final PlotAreaManager plotAreaManager) {
        this.plotAreaManager = plotAreaManager;
    }

    @Override public boolean onCommand(PlotPlayer<?> player, String[] args) {
        if (args.length > 0) {
            if ("player".equalsIgnoreCase(args[0])) {
                for (Map.Entry<String, Object> meta : player.getMeta().entrySet()) {
                    MainUtil.sendMessage(player,
                        "Key: " + meta.getKey() + " Value: " + meta.getValue().toString() + " , ");
                }
            }
        }
        if (args.length > 0 && "loadedchunks".equalsIgnoreCase(args[0])) {
            final long start = System.currentTimeMillis();
            MainUtil.sendMessage(player, "Fetching loaded chunks...");
            TaskManager.runTaskAsync(() -> MainUtil.sendMessage(player,
                "Loaded chunks: " + RegionManager.manager
                    .getChunkChunks(player.getLocation().getWorldName()).size() + "(" + (
                    System.currentTimeMillis() - start) + "ms) using thread: " + Thread
                    .currentThread().getName()));
            return true;
        }
        if (args.length > 0 && "uuids".equalsIgnoreCase(args[0])) {
            final Collection<UUIDMapping> mappings = PlotSquared.get().getImpromptuUUIDPipeline().getAllImmediately();
            MainUtil.sendMessage(player, String.format("There are %d cached UUIDs", mappings.size()));
            return true;
        }
        if (args.length > 0 && "debug-players".equalsIgnoreCase(args[0])) {
            MainUtil.sendMessage(player, "Player in debug mode: " );
            for (final PlotPlayer<?> pp : PlotPlayer.getDebugModePlayers()) {
                MainUtil.sendMessage(player, "- " + pp.getName());
            }
            return true;
        }
        if (args.length > 0 && "entitytypes".equalsIgnoreCase(args[0])) {
            EntityCategories.init();
            player.sendMessage(Captions.PREFIX.getTranslated() + "§cEntity Categories: ");
            EntityCategory.REGISTRY.forEach(category -> {
                final StringBuilder builder =
                    new StringBuilder("§7- §6").append(category.getId()).append("§7: §6");
                for (final EntityType entityType : category.getAll()) {
                    builder.append(entityType.getId()).append(" ");
                }
                player.sendMessage(Captions.PREFIX.getTranslated() + builder.toString());
            });
            EntityType.REGISTRY.values().stream().sorted(Comparator.comparing(EntityType::getId))
                .forEach(entityType -> {
                    long categoryCount = EntityCategory.REGISTRY.values().stream()
                        .filter(category -> category.contains(entityType)).count();
                    if (categoryCount > 0) {
                        return;
                    }
                    player.sendMessage(
                        Captions.PREFIX.getTranslated() + entityType.getName() + " is in "
                            + categoryCount + " categories");
                });
            return true;
        }
        if ((args.length > 0) && args[0].equalsIgnoreCase("msg")) {
            StringBuilder msg = new StringBuilder();
            for (Captions caption : Captions.values()) {
                msg.append(caption.getTranslated()).append("\n");
            }
            MainUtil.sendMessage(player, msg.toString());
            return true;
        }
        StringBuilder information = new StringBuilder();
        String header = Captions.DEBUG_HEADER.getTranslated();
        String line = Captions.DEBUG_LINE.getTranslated();
        String section = Captions.DEBUG_SECTION.getTranslated();
        information.append(header);
        information.append(getSection(section, "PlotArea"));
        information.append(
            getLine(line, "Plot Worlds", StringMan.join(this.plotAreaManager.getAllPlotAreas(), ", ")));
        information.append(getLine(line, "Owned Plots", PlotQuery.newQuery().allPlots().count()));
        information.append(getSection(section, "Messages"));
        information.append(getLine(line, "Total Messages", Captions.values().length));
        information.append(getLine(line, "View all captions", "/plot debug msg"));
        MainUtil.sendMessage(player, information.toString());
        return true;
    }

    private String getSection(String line, String val) {
        return line.replaceAll("%val%", val) + "\n";
    }

    private String getLine(String line, String var, Object val) {
        return line.replaceAll("%var%", var).replaceAll("%val%", "" + val) + "\n";
    }
}
