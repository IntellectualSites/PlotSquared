package com.intellectualcrafters.plot.commands;

import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.config.Settings;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotArea;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.object.RunnableVal2;
import com.intellectualcrafters.plot.object.RunnableVal3;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.MathMan;
import com.intellectualcrafters.plot.util.Permissions;
import com.intellectualcrafters.plot.util.UUIDHandler;
import com.plotsquared.general.commands.Command;
import com.plotsquared.general.commands.CommandDeclaration;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

@CommandDeclaration(
        command = "visit",
        permission = "plots.visit",
        description = "Visit someones plot",
        usage = "/plot visit [<player>|<alias>|<world>|<id>] [#]",
        aliases = {"v", "tp", "teleport", "goto", "home", "h"},
        requiredType = RequiredType.NONE,
        category = CommandCategory.TELEPORT)
public class Visit extends Command {

    public Visit() {
        super(MainCommand.getInstance(), true);
    }

    @Override
    public Collection<Command> tab(PlotPlayer player, String[] args, boolean space) {
        return tabOf(player, args, space, getUsage());
    }

    @Override
    public void execute(final PlotPlayer player, String[] args, RunnableVal3<Command, Runnable, Runnable> confirm, final RunnableVal2<Command, CommandResult> whenDone) throws CommandException {
        if (args.length == 1 && args[0].contains(":")) {
            args = args[0].split(":");
        }
        int page = Integer.MIN_VALUE;
        Collection<Plot> unsorted = null;
        PlotArea sortByArea = player.getApplicablePlotArea();
        boolean shouldSortByArea = Settings.Teleport.PER_WORLD_VISIT;
        switch (args.length) {
            case 3:
                if (!MathMan.isInteger(args[2])) {
                    C.NOT_VALID_NUMBER.send(player, "(1, ∞)");
                    C.COMMAND_SYNTAX.send(player, getUsage());
                    return;
                }
                page = Integer.parseInt(args[2]);
            case 2:
                if (page != Integer.MIN_VALUE || !MathMan.isInteger(args[1])) {
                    sortByArea = PS.get().getPlotAreaByString(args[1]);
                    if (sortByArea == null) {
                        C.NOT_VALID_NUMBER.send(player, "(1, ∞)");
                        C.COMMAND_SYNTAX.send(player, getUsage());
                        return;
                    }
                    UUID user = UUIDHandler.getUUIDFromString(args[0]);
                    if (user == null) {
                        C.COMMAND_SYNTAX.send(player, getUsage());
                        return;
                    }
                    unsorted = PS.get().getBasePlots(user);
                    shouldSortByArea = true;
                    break;
                }
                page = Integer.parseInt(args[1]);
            case 1:
                UUID user = args[0].length() >= 2 ? UUIDHandler.getUUIDFromString(args[0]) : null;
                if (user != null && !PS.get().hasPlot(user)) user = null;
                if (page == Integer.MIN_VALUE && user == null && MathMan.isInteger(args[0])) {
                    page = Integer.parseInt(args[0]);
                    unsorted = PS.get().getBasePlots(player);
                    break;
                }
                if (user != null) {
                    unsorted = PS.get().getBasePlots(user);
                } else {
                    Plot plot = MainUtil.getPlotFromString(player, args[0], true);
                    if (plot != null) {
                        unsorted = Collections.singletonList(plot.getBasePlot(false));
                    }
                }
                break;
            case 0:
                page = 1;
                unsorted = PS.get().getPlots(player);
                break;
            default:

        }
        if (page == Integer.MIN_VALUE) {
            page = 1;
        }
        if (unsorted == null || unsorted.isEmpty()) {
            C.FOUND_NO_PLOTS.send(player);
            return;
        }
        Iterator<Plot> iterator = unsorted.iterator();
        while (iterator.hasNext()) {
            if (!iterator.next().isBasePlot()) {
                iterator.remove();
            }
        }
        if (page < 1 || page > unsorted.size()) {
            C.NOT_VALID_NUMBER.send(player, "(1, " + unsorted.size() + ")");
            return;
        }
        List<Plot> plots;
        if (shouldSortByArea) {
            plots = PS.get().sortPlots(unsorted, PS.SortType.CREATION_DATE, sortByArea);
        }  else {
            plots = PS.get().sortPlotsByTemp(unsorted);
        }
        final Plot plot = plots.get(page - 1);
        if (!plot.hasOwner()) {
            if (!Permissions.hasPermission(player, C.PERMISSION_VISIT_UNOWNED)) {
                C.NO_PERMISSION.send(player, C.PERMISSION_VISIT_UNOWNED);
                return;
            }
        } else if (plot.isOwner(player.getUUID())) {
            if (!Permissions.hasPermission(player, C.PERMISSION_VISIT_OWNED) && !Permissions.hasPermission(player, C.PERMISSION_HOME)) {
                C.NO_PERMISSION.send(player, C.PERMISSION_VISIT_OWNED);
                return;
            }
        } else if (plot.isAdded(player.getUUID())) {
            if (!Permissions.hasPermission(player, C.PERMISSION_SHARED)) {
                C.NO_PERMISSION.send(player, C.PERMISSION_SHARED);
                return;
            }
        } else {
            if (!Permissions.hasPermission(player, C.PERMISSION_VISIT_OTHER)) {
                C.NO_PERMISSION.send(player, C.PERMISSION_VISIT_OTHER);
                return;
            }
        }
        confirm.run(this, new Runnable() {
            @Override
            public void run() {
                if (plot.teleportPlayer(player)) {
                    whenDone.run(Visit.this, CommandResult.SUCCESS);
                } else {
                    whenDone.run(Visit.this, CommandResult.FAILURE);
                }
            }
        }, new Runnable() {
            @Override
            public void run() {
                whenDone.run(Visit.this, CommandResult.FAILURE);
            }
        });
    }

}
