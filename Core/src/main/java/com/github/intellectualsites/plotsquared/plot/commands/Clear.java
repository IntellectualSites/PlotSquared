package com.github.intellectualsites.plotsquared.plot.commands;

import com.github.intellectualsites.plotsquared.commands.Command;
import com.github.intellectualsites.plotsquared.commands.CommandDeclaration;
import com.github.intellectualsites.plotsquared.plot.config.Captions;
import com.github.intellectualsites.plotsquared.plot.config.Settings;
import com.github.intellectualsites.plotsquared.plot.flag.FlagManager;
import com.github.intellectualsites.plotsquared.plot.flag.Flags;
import com.github.intellectualsites.plotsquared.plot.object.Plot;
import com.github.intellectualsites.plotsquared.plot.object.PlotPlayer;
import com.github.intellectualsites.plotsquared.plot.object.RunnableVal2;
import com.github.intellectualsites.plotsquared.plot.object.RunnableVal3;
import com.github.intellectualsites.plotsquared.plot.util.MainUtil;
import com.github.intellectualsites.plotsquared.plot.util.Permissions;
import com.github.intellectualsites.plotsquared.plot.util.block.GlobalBlockQueue;

@CommandDeclaration(command = "clear", description = "Clear the plot you stand on",
    permission = "plots.clear", category = CommandCategory.APPEARANCE, usage = "/plot clear",
    aliases = "reset", confirmation = true) public class Clear extends Command {

    // Note: To clear a specific plot use /plot <plot> clear
    // The syntax also works with any command: /plot <plot> <command>

    public Clear() {
        super(MainCommand.getInstance(), true);
    }

    @Override public void execute(final PlotPlayer player, String[] args,
        RunnableVal3<Command, Runnable, Runnable> confirm,
        RunnableVal2<Command, CommandResult> whenDone) throws CommandException {
        checkTrue(args.length == 0, Captions.COMMAND_SYNTAX, getUsage());
        final Plot plot = check(player.getCurrentPlot(), Captions.NOT_IN_PLOT);
        checkTrue(plot.isOwner(player.getUUID()) || Permissions
                .hasPermission(player, Captions.PERMISSION_ADMIN_COMMAND_CLEAR),
            Captions.NO_PLOT_PERMS);
        checkTrue(plot.getRunning() == 0, Captions.WAIT_FOR_TIMER);
        checkTrue(!Settings.Done.RESTRICT_BUILDING || !Flags.DONE.isSet(plot) || Permissions
            .hasPermission(player, Captions.PERMISSION_CONTINUE), Captions.DONE_ALREADY_DONE);
        confirm.run(this, () -> {
            final long start = System.currentTimeMillis();
            boolean result = plot.clear(true, false, () -> {
                plot.unlink();
                GlobalBlockQueue.IMP.addTask(() -> {
                    plot.removeRunning();
                    // If the state changes, then mark it as no longer done
                    if (plot.getFlag(Flags.DONE).isPresent()) {
                        FlagManager.removePlotFlag(plot, Flags.DONE);
                    }
                    if (plot.getFlag(Flags.ANALYSIS).isPresent()) {
                        FlagManager.removePlotFlag(plot, Flags.ANALYSIS);
                    }
                    MainUtil.sendMessage(player, Captions.CLEARING_DONE,
                        "" + (System.currentTimeMillis() - start));
                });
            });
            if (!result) {
                MainUtil.sendMessage(player, Captions.WAIT_FOR_TIMER);
            } else {
                plot.addRunning();
            }
        }, null);
    }
}
