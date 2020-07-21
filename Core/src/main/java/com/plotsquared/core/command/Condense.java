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

import com.google.inject.Inject;
import com.plotsquared.core.configuration.caption.TranslatableCaption;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.PlotArea;
import com.plotsquared.core.plot.PlotId;
import com.plotsquared.core.plot.world.PlotAreaManager;
import com.plotsquared.core.util.MathMan;
import com.plotsquared.core.util.WorldUtil;
import com.plotsquared.core.util.task.TaskManager;
import com.plotsquared.core.util.task.TaskTime;
import net.kyori.adventure.text.minimessage.Template;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

@CommandDeclaration(command = "condense",
    permission = "plots.admin",
    usage = "/plot condense <area> <start|stop|info> [radius]",
    description = "Condense a plotworld",
    category = CommandCategory.ADMINISTRATION,
    requiredType = RequiredType.CONSOLE)
public class Condense extends SubCommand {

    public static boolean TASK = false;

    private final PlotAreaManager plotAreaManager;
    private final WorldUtil worldUtil;

    @Inject public Condense(@Nonnull final PlotAreaManager plotAreaManager,
                            @Nonnull final WorldUtil worldUtil) {
        this.plotAreaManager = plotAreaManager;
        this.worldUtil = worldUtil;
    }

    @Override public boolean onCommand(final PlotPlayer<?> player, String[] args) {
        if (args.length != 2 && args.length != 3) {
            player.sendMessage(
                    TranslatableCaption.of("commandconfig.command_syntax"),
                    Template.of("value", "/plot condense <area> <start|stop|info> [radius]")
            );
            return false;
        }
        PlotArea area = this.plotAreaManager.getPlotAreaByString(args[0]);
        if (area == null || !this.worldUtil.isWorld(area.getWorldName())) {
            player.sendMessage(TranslatableCaption.of("invalid.invalid_area"));
            return false;
        }
        switch (args[1].toLowerCase()) {
            case "start": {
                if (args.length == 2) {
                    player.sendMessage(
                            TranslatableCaption.of("commandconfig.command_syntax"),
                            Template.of("value", "/plot condense" + area.toString() + " start <radius>")
                    );
                    return false;
                }
                if (Condense.TASK) {
                    player.sendMessage(TranslatableCaption.of("condense.task_already_started"));
                    return false;
                }
                if (!MathMan.isInteger(args[2])) {
                    player.sendMessage(TranslatableCaption.of("condense.invalid_radius"));
                    return false;
                }
                int radius = Integer.parseInt(args[2]);

                final List<Plot> plots = new ArrayList<>(area.getPlots());
                // remove non base plots
                Iterator<Plot> iterator = plots.iterator();
                int maxSize = 0;
                ArrayList<Integer> sizes = new ArrayList<>();
                while (iterator.hasNext()) {
                    Plot plot = iterator.next();
                    if (!plot.isBasePlot()) {
                        iterator.remove();
                        continue;
                    }
                    int size = plot.getConnectedPlots().size();
                    if (size > maxSize) {
                        maxSize = size;
                    }
                    sizes.add(size - 1);
                }
                // Sort plots by size (buckets?)]
                ArrayList<Plot>[] buckets = new ArrayList[maxSize];
                for (int i = 0; i < plots.size(); i++) {
                    Plot plot = plots.get(i);
                    int size = sizes.get(i);
                    ArrayList<Plot> array = buckets[size];
                    if (array == null) {
                        array = new ArrayList<>();
                        buckets[size] = array;
                    }
                    array.add(plot);
                }
                final ArrayList<Plot> allPlots = new ArrayList<>(plots.size());
                for (int i = buckets.length - 1; i >= 0; i--) {
                    ArrayList<Plot> array = buckets[i];
                    if (array != null) {
                        allPlots.addAll(array);
                    }
                }
                int size = allPlots.size();
                int minimumRadius = (int) Math.ceil(Math.sqrt(size) / 2 + 1);
                if (radius < minimumRadius) {
                    player.sendMessage(TranslatableCaption.of("condense.radius_too_small"));
                    return false;
                }
                List<PlotId> toMove = new ArrayList<>(getPlots(allPlots, radius));
                final List<PlotId> free = new ArrayList<>();
                PlotId start = PlotId.of(0, 0);
                while (start.getX() <= minimumRadius && start.getY() <= minimumRadius) {
                    Plot plot = area.getPlotAbs(start);
                    if (plot != null && !plot.hasOwner()) {
                        free.add(plot.getId());
                    }
                    start = start.getNextId();
                }
                if (free.isEmpty() || toMove.isEmpty()) {
                    player.sendMessage(TranslatableCaption.of("condense.no_free_plots_found"));
                    return false;
                }
                player.sendMessage(TranslatableCaption.of("condense.task_started"));
                Condense.TASK = true;
                Runnable run = new Runnable() {
                    @Override public void run() {
                        if (!Condense.TASK) {
                            player.sendMessage(TranslatableCaption.of("condense.task_cancelled"));
                        }
                        if (allPlots.isEmpty()) {
                            Condense.TASK = false;
                            player.sendMessage(TranslatableCaption.of("condense.task_complete"));
                            return;
                        }
                        final Runnable task = this;
                        final Plot origin = allPlots.remove(0);
                        int i = 0;
                        while (free.size() > i) {
                            final Plot possible = origin.getArea().getPlotAbs(free.get(i));
                            if (possible.hasOwner()) {
                                free.remove(i);
                                continue;
                            }
                            i++;
                            final AtomicBoolean result = new AtomicBoolean(false);
                            try {
                                result.set(origin.move(possible, () -> {
                                    if (result.get()) {
                                        player.sendMessage(
                                                TranslatableCaption.of("condense.moving"),
                                                Template.of("origin", String.valueOf(origin)),
                                                Template.of("possible", String.valueOf(possible))
                                        );
                                        TaskManager.runTaskLater(task, TaskTime.ticks(1L));
                                    }
                                }, false).get());
                            } catch (InterruptedException | ExecutionException e) {
                                e.printStackTrace();
                            }
                            if (result.get()) {
                                break;
                            }
                        }
                        if (free.isEmpty()) {
                            Condense.TASK = false;
                            player.sendMessage(TranslatableCaption.of("condense.task_failed"));
                            return;
                        }
                        if (i >= free.size()) {
                            player.sendMessage(
                                    TranslatableCaption.of("condense.skipping"),
                                    Template.of("plot", String.valueOf(origin))
                            );
                        }
                    }
                };
                TaskManager.runTaskAsync(run);
                return true;
            }
            case "stop":
                if (!Condense.TASK) {
                    player.sendMessage(TranslatableCaption.of("condense.task_already_stopped"));
                    return false;
                }
                Condense.TASK = false;
                player.sendMessage(TranslatableCaption.of("condense.task_stopped"));
                return true;
            case "info":
                if (args.length == 2) {
                    player.sendMessage(
                            TranslatableCaption.of("commandconfig.command_syntax"),
                            Template.of("value", "/plot condense " + area.toString() + " info <radius>")
                    );
                    return false;
                }
                if (!MathMan.isInteger(args[2])) {
                    player.sendMessage(TranslatableCaption.of("condense.invalid_radius"));
                    return false;
                }
                int radius = Integer.parseInt(args[2]);
                Collection<Plot> plots = area.getPlots();
                int size = plots.size();
                int minimumRadius = (int) Math.ceil(Math.sqrt(size) / 2 + 1);
                if (radius < minimumRadius) {
                    player.sendMessage(TranslatableCaption.of("condense.radius_too_small"));
                    return false;
                }
                int maxMove = getPlots(plots, minimumRadius).size();
                int userMove = getPlots(plots, radius).size();
                player.sendMessage(TranslatableCaption.of("condense.default_eval"));
                player.sendMessage(
                        TranslatableCaption.of("condense.minimum_radius"),
                        Template.of("minimumRadius", String.valueOf(minimumRadius))
                );
                player.sendMessage(
                        TranslatableCaption.of("condense.minimum_radius"),
                        Template.of("maxMove", String.valueOf(maxMove))
                );
                player.sendMessage(TranslatableCaption.of("condense.input_eval"));
                player.sendMessage(
                        TranslatableCaption.of("condense.input_radius"),
                        Template.of("radius", String.valueOf(radius))
                );
                player.sendMessage(
                        TranslatableCaption.of("condense.estimated_moves"),
                        Template.of("userMove", String.valueOf(userMove))
                );
                player.sendMessage(TranslatableCaption.of("condense.eta"));
                player.sendMessage(TranslatableCaption.of("condense.radius_measured"));
                return true;
        }
        player.sendMessage(
                TranslatableCaption.of("commandconfig.command_syntax"),
                Template.of("value","/plot condense " + area.getWorldName() + " <start|stop|info> [radius]")
        );
        return false;
    }

    public Set<PlotId> getPlots(Collection<Plot> plots, int radius) {
        HashSet<PlotId> outside = new HashSet<>();
        for (Plot plot : plots) {
            if (plot.getId().getX() > radius || plot.getId().getX() < -radius || plot.getId().getY() > radius
                || plot.getId().getY() < -radius) {
                outside.add(plot.getId());
            }
        }
        return outside;
    }
}
