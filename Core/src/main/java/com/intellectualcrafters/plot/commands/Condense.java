package com.intellectualcrafters.plot.commands;

import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotArea;
import com.intellectualcrafters.plot.object.PlotId;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.MathMan;
import com.intellectualcrafters.plot.util.TaskManager;
import com.intellectualcrafters.plot.util.WorldUtil;
import com.plotsquared.general.commands.CommandDeclaration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

@CommandDeclaration(command = "condense",
        permission = "plots.admin",
        description = "Condense a plotworld",
        category = CommandCategory.ADMINISTRATION,
        requiredType = RequiredType.CONSOLE)
public class Condense extends SubCommand {

    public static boolean TASK = false;

    @Override
    public boolean onCommand(final PlotPlayer player, String[] args) {
        if (args.length != 2 && args.length != 3) {
            MainUtil.sendMessage(player, "/plot condense <area> <start|stop|info> [radius]");
            return false;
        }
        PlotArea area = PS.get().getPlotAreaByString(args[0]);
        if (area == null || !WorldUtil.IMP.isWorld(area.worldname)) {
            MainUtil.sendMessage(player, "INVALID AREA");
            return false;
        }
        switch (args[1].toLowerCase()) {
            case "start": {
                if (args.length == 2) {
                    MainUtil.sendMessage(player, "/plot condense " + area.toString() + " start <radius>");
                    return false;
                }
                if (Condense.TASK) {
                    MainUtil.sendMessage(player, "TASK ALREADY STARTED");
                    return false;
                }
                if (!MathMan.isInteger(args[2])) {
                    MainUtil.sendMessage(player, "INVALID RADIUS");
                    return false;
                }
                int radius = Integer.parseInt(args[2]);
                ArrayList<Plot> plots = new ArrayList<>(PS.get().getPlots(area));
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
                int minimumRadius = (int) Math.ceil((Math.sqrt(size) / 2) + 1);
                if (radius < minimumRadius) {
                    MainUtil.sendMessage(player, "RADIUS TOO SMALL");
                    return false;
                }
                List<PlotId> toMove = new ArrayList<>(getPlots(allPlots, radius));
                final List<PlotId> free = new ArrayList<>();
                PlotId start = new PlotId(0, 0);
                while (start.x <= minimumRadius && start.y <= minimumRadius) {
                    Plot plot = area.getPlotAbs(start);
                    if (plot != null && !plot.hasOwner()) {
                        free.add(plot.getId());
                    }
                    start = Auto.getNextPlotId(start, 1);
                }
                if (free.isEmpty() || toMove.isEmpty()) {
                    MainUtil.sendMessage(player, "NO FREE PLOTS FOUND");
                    return false;
                }
                MainUtil.sendMessage(player, "TASK STARTED...");
                Condense.TASK = true;
                Runnable run = new Runnable() {
                    @Override public void run() {
                        if (!Condense.TASK) {
                            MainUtil.sendMessage(player, "TASK CANCELLED.");
                        }
                        if (allPlots.isEmpty()) {
                            Condense.TASK = false;
                            MainUtil.sendMessage(player,
                                "TASK COMPLETE. PLEASE VERIFY THAT NO NEW PLOTS HAVE BEEN CLAIMED DURING TASK.");
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
                            result.set(origin.move(possible, () -> {
                                if (result.get()) {
                                    MainUtil.sendMessage(player,
                                        "Moving: " + origin + " -> " + possible);
                                    TaskManager.runTaskLater(task, 1);
                                }
                            }, false));
                            if (result.get()) {
                                break;
                            }
                        }
                        if (free.isEmpty()) {
                            Condense.TASK = false;
                            MainUtil.sendMessage(player, "TASK FAILED. NO FREE PLOTS FOUND!");
                            return;
                        }
                        if (i >= free.size()) {
                            MainUtil.sendMessage(player, "SKIPPING COMPLEX PLOT: " + origin);
                        }
                    }
                };
                TaskManager.runTaskAsync(run);
                return true;
            }
            case "stop":
                if (!Condense.TASK) {
                    MainUtil.sendMessage(player, "TASK ALREADY STOPPED");
                    return false;
                }
                Condense.TASK = false;
                MainUtil.sendMessage(player, "TASK STOPPED");
                return true;
            case "info":
                if (args.length == 2) {
                    MainUtil.sendMessage(player, "/plot condense " + area.toString() + " info <radius>");
                    return false;
                }
                if (!MathMan.isInteger(args[2])) {
                    MainUtil.sendMessage(player, "INVALID RADIUS");
                    return false;
                }
                int radius = Integer.parseInt(args[2]);
                Collection<Plot> plots = area.getPlots();
                int size = plots.size();
                int minimumRadius = (int) Math.ceil((Math.sqrt(size) / 2) + 1);
                if (radius < minimumRadius) {
                    MainUtil.sendMessage(player, "RADIUS TOO SMALL");
                    return false;
                }
                int maxMove = getPlots(plots, minimumRadius).size();
                int userMove = getPlots(plots, radius).size();
                MainUtil.sendMessage(player, "=== DEFAULT EVAL ===");
                MainUtil.sendMessage(player, "MINIMUM RADIUS: " + minimumRadius);
                MainUtil.sendMessage(player, "MAXIMUM MOVES: " + maxMove);
                MainUtil.sendMessage(player, "=== INPUT EVAL ===");
                MainUtil.sendMessage(player, "INPUT RADIUS: " + radius);
                MainUtil.sendMessage(player, "ESTIMATED MOVES: " + userMove);
                MainUtil.sendMessage(player, "ESTIMATED TIME: No idea, times will drastically change based on the system performance and load");
                MainUtil.sendMessage(player, "&e - Radius is measured in plot width");
                return true;
        }
        MainUtil.sendMessage(player, "/plot condense " + area.worldname + " <start|stop|info> [radius]");
        return false;
    }

    public Set<PlotId> getPlots(Collection<Plot> plots, int radius) {
        HashSet<PlotId> outside = new HashSet<>();
        for (Plot plot : plots) {
            if (plot.getId().x > radius || plot.getId().x < -radius || plot.getId().y > radius || plot.getId().y < -radius) {
                outside.add(plot.getId());
            }
        }
        return outside;
    }
}
