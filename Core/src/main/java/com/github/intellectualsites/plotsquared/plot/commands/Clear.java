package com.github.intellectualsites.plotsquared.plot.commands;

import com.github.intellectualsites.plotsquared.commands.Command;
import com.github.intellectualsites.plotsquared.commands.CommandDeclaration;
import com.github.intellectualsites.plotsquared.plot.PlotSquared;
import com.github.intellectualsites.plotsquared.plot.config.CaptionUtility;
import com.github.intellectualsites.plotsquared.plot.config.Captions;
import com.github.intellectualsites.plotsquared.plot.config.Settings;
import com.github.intellectualsites.plotsquared.plot.events.PlotFlagRemoveEvent;
import com.github.intellectualsites.plotsquared.plot.events.Result;
import com.github.intellectualsites.plotsquared.plot.flags.GlobalFlagContainer;
import com.github.intellectualsites.plotsquared.plot.flags.PlotFlag;
import com.github.intellectualsites.plotsquared.plot.flags.implementations.AnalysisFlag;
import com.github.intellectualsites.plotsquared.plot.flags.implementations.DoneFlag;
import com.github.intellectualsites.plotsquared.plot.object.Plot;
import com.github.intellectualsites.plotsquared.plot.object.PlotPlayer;
import com.github.intellectualsites.plotsquared.plot.object.RunnableVal2;
import com.github.intellectualsites.plotsquared.plot.object.RunnableVal3;
import com.github.intellectualsites.plotsquared.plot.util.MainUtil;
import com.github.intellectualsites.plotsquared.plot.util.Permissions;
import com.github.intellectualsites.plotsquared.plot.util.block.GlobalBlockQueue;

import java.util.concurrent.CompletableFuture;

@CommandDeclaration(command = "clear",
    description = "Clear the plot you stand on",
    requiredType = RequiredType.NONE,
    permission = "plots.clear",
    category = CommandCategory.APPEARANCE,
    usage = "/plot clear",
    aliases = "reset",
    confirmation = true)
public class Clear extends Command {

    // Note: To clear a specific plot use /plot <plot> clear
    // The syntax also works with any command: /plot <plot> <command>

    public Clear() {
        super(MainCommand.getInstance(), true);
    }

    @Override public CompletableFuture<Boolean> execute(final PlotPlayer player, String[] args,
        RunnableVal3<Command, Runnable, Runnable> confirm,
        RunnableVal2<Command, CommandResult> whenDone) throws CommandException {
        checkTrue(args.length == 0, Captions.COMMAND_SYNTAX, getUsage());
        final Plot plot = check(player.getCurrentPlot(), Captions.NOT_IN_PLOT);
        Result eventResult =
            PlotSquared.get().getEventDispatcher().callClear(plot).getEventResult();
        if (eventResult == Result.DENY) {
            player.sendMessage(CaptionUtility.format(player, eventResult.getReason()));
            return CompletableFuture.completedFuture(true);
        }
        boolean force = eventResult == Result.FORCE;
        checkTrue(force || plot.isOwner(player.getUUID()) || Permissions
                .hasPermission(player, Captions.PERMISSION_ADMIN_COMMAND_CLEAR),
            Captions.NO_PLOT_PERMS);
        checkTrue(plot.getRunning() == 0, Captions.WAIT_FOR_TIMER);
        checkTrue(force || !Settings.Done.RESTRICT_BUILDING || !DoneFlag.isDone(plot) || Permissions
            .hasPermission(player, Captions.PERMISSION_CONTINUE), Captions.DONE_ALREADY_DONE);
        confirm.run(this, () -> {
            final long start = System.currentTimeMillis();
            boolean result = plot.clear(true, false, () -> {
                plot.unlink();
                GlobalBlockQueue.IMP.addEmptyTask(() -> {
                    plot.removeRunning();
                    // If the state changes, then mark it as no longer done
                    if (DoneFlag.isDone(plot)) {
                        PlotFlag<?, ?> plotFlag =
                            GlobalFlagContainer.getInstance().getFlag(DoneFlag.class);
                        PlotFlagRemoveEvent event =
                            PlotSquared.get().getEventDispatcher().callFlagRemove(plotFlag, plot);
                        if (event.getEventResult() != Result.DENY) {
                            plot.removeFlag(event.getFlag());
                        }
                    }
                    if (!plot.getFlag(AnalysisFlag.class).isEmpty()) {
                        PlotFlag<?, ?> plotFlag =
                            GlobalFlagContainer.getInstance().getFlag(AnalysisFlag.class);
                        PlotFlagRemoveEvent event =
                            PlotSquared.get().getEventDispatcher().callFlagRemove(plotFlag, plot);
                        if (event.getEventResult() != Result.DENY) {
                            plot.removeFlag(event.getFlag());
                        }
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
        return CompletableFuture.completedFuture(true);
    }
}
