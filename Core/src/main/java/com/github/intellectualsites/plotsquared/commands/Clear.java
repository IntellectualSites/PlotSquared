package com.github.intellectualsites.plotsquared.commands;

import com.github.intellectualsites.plotsquared.PlotSquared;
import com.github.intellectualsites.plotsquared.plot.config.Captions;
import com.github.intellectualsites.plotsquared.plot.config.Settings;
import com.github.intellectualsites.plotsquared.events.PlotFlagRemoveEvent;
import com.github.intellectualsites.plotsquared.events.Result;
import com.github.intellectualsites.plotsquared.plot.flags.PlotFlag;
import com.github.intellectualsites.plotsquared.plot.flags.implementations.AnalysisFlag;
import com.github.intellectualsites.plotsquared.plot.flags.implementations.DoneFlag;
import com.github.intellectualsites.plotsquared.plot.object.Plot;
import com.github.intellectualsites.plotsquared.player.PlotPlayer;
import com.github.intellectualsites.plotsquared.util.tasks.RunnableVal2;
import com.github.intellectualsites.plotsquared.util.tasks.RunnableVal3;
import com.github.intellectualsites.plotsquared.util.MainUtil;
import com.github.intellectualsites.plotsquared.util.Permissions;
import com.github.intellectualsites.plotsquared.queue.GlobalBlockQueue;

import java.util.concurrent.CompletableFuture;

import static com.github.intellectualsites.plotsquared.commands.SubCommand.sendMessage;

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
