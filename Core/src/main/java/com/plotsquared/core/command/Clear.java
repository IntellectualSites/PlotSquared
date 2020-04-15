package com.plotsquared.core.command;

import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.config.Captions;
import com.plotsquared.core.config.Settings;
import com.plotsquared.core.events.PlotFlagRemoveEvent;
import com.plotsquared.core.events.Result;
import com.plotsquared.core.plot.flag.PlotFlag;
import com.plotsquared.core.plot.flag.implementations.AnalysisFlag;
import com.plotsquared.core.plot.flag.implementations.DoneFlag;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.util.task.RunnableVal2;
import com.plotsquared.core.util.task.RunnableVal3;
import com.plotsquared.core.util.MainUtil;
import com.plotsquared.core.util.Permissions;
import com.plotsquared.core.queue.GlobalBlockQueue;

import java.util.concurrent.CompletableFuture;

import static com.plotsquared.core.command.SubCommand.sendMessage;

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
            sendMessage(player, Captions.EVENT_DENIED, "Clear");
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
                        PlotFlag<?, ?> plotFlag = plot.getFlagContainer().getFlag(DoneFlag.class);
                        PlotFlagRemoveEvent event =
                            PlotSquared.get().getEventDispatcher().callFlagRemove(plotFlag, plot);
                        if (event.getEventResult() != Result.DENY) {
                            plot.removeFlag(event.getFlag());
                        }
                    }
                    if (!plot.getFlag(AnalysisFlag.class).isEmpty()) {
                        PlotFlag<?, ?> plotFlag =
                            plot.getFlagContainer().getFlag(AnalysisFlag.class);
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
