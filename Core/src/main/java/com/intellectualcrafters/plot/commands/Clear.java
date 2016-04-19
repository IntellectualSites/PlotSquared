package com.intellectualcrafters.plot.commands;

import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.config.Settings;
import com.intellectualcrafters.plot.flag.FlagManager;
import com.intellectualcrafters.plot.flag.Flags;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.CmdConfirm;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.Permissions;
import com.intellectualcrafters.plot.util.SetQueue;
import com.intellectualcrafters.plot.util.TaskManager;
import com.plotsquared.general.commands.CommandDeclaration;

import java.util.Set;

@CommandDeclaration(command = "clear",
        description = "Clear a plot",
        permission = "plots.clear",
        category = CommandCategory.APPEARANCE,
        usage = "/plot clear [id]",
        aliases = "reset",
        confirmation = true)
public class Clear extends SubCommand {

    @Override
    public boolean onCommand(final PlotPlayer plr, String[] args) {
        Location loc = plr.getLocation();
        final Plot plot;
        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("mine")) {
                Set<Plot> plots = plr.getPlots();
                if (!plots.isEmpty()) {
                    plot = plots.iterator().next();
                } else {
                    MainUtil.sendMessage(plr, C.NO_PLOTS);
                    return false;
                }
            } else {
                plot = MainUtil.getPlotFromString(plr, args[0], true);
            }
            if (plot == null) {
                MainUtil.sendMessage(plr, C.COMMAND_SYNTAX, "/plot clear [X;Z|mine]");
                return false;
            }
        } else if (args.length == 0) {
            plot = loc.getPlotAbs();
            if (plot == null) {
                MainUtil.sendMessage(plr, C.COMMAND_SYNTAX, "/plot clear [X;Z|mine]");
                C.NOT_IN_PLOT.send(plr);
                return false;
            }
        } else {
            MainUtil.sendMessage(plr, C.COMMAND_SYNTAX, "/plot clear [X;Z|mine]");
            return false;
        }
        if ((!plot.hasOwner() || !plot.isOwner(plr.getUUID())) && !Permissions.hasPermission(plr, "plots.admin.command.clear")) {
            return sendMessage(plr, C.NO_PLOT_PERMS);
        }
        if (plot.getRunning() != 0) {
            MainUtil.sendMessage(plr, C.WAIT_FOR_TIMER);
            return false;
        }
        if (plot.getFlag(Flags.DONE).isPresent()
                && (!Permissions.hasPermission(plr, "plots.continue") || (Settings.DONE_COUNTS_TOWARDS_LIMIT && (plr.getAllowedPlots() >= plr
                .getPlotCount())))) {
            MainUtil.sendMessage(plr, C.DONE_ALREADY_DONE);
            return false;
        }
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                final long start = System.currentTimeMillis();
                boolean result = plot.clear(true, false, new Runnable() {
                    @Override
                    public void run() {
                        plot.unlink();
                        SetQueue.IMP.addTask(new Runnable() {
                            @Override
                            public void run() {
                                plot.removeRunning();
                                // If the state changes, then mark it as no longer done
                                if (plot.getFlag(Flags.DONE).isPresent()) {
                                    FlagManager.removePlotFlag(plot, Flags.DONE);
                                }
                                if (plot.getFlag(Flags.ANALYSIS).isPresent()) {
                                    FlagManager.removePlotFlag(plot, Flags.ANALYSIS);
                                }
                                MainUtil.sendMessage(plr, C.CLEARING_DONE, "" + (System.currentTimeMillis() - start));
                            }
                        });
                    }
                });
                if (!result) {
                    MainUtil.sendMessage(plr, C.WAIT_FOR_TIMER);
                } else {
                    plot.addRunning();
                }
            }
        };
        if (hasConfirmation(plr)) {
            CmdConfirm.addPending(plr, "/plot clear " + plot.getId(), runnable);
        } else {
            TaskManager.runTask(runnable);
        }
        return true;
    }
}
