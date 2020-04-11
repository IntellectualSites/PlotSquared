package com.plotsquared.commands;

import com.plotsquared.PlotSquared;
import com.plotsquared.config.Captions;
import com.plotsquared.plot.Plot;
import com.plotsquared.player.PlotPlayer;
import com.plotsquared.util.tasks.RunnableVal2;
import com.plotsquared.util.tasks.RunnableVal3;
import com.plotsquared.util.MainUtil;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@CommandDeclaration(command = "leave",
    description = "Removes self from being trusted or a member of the plot",
    permission = "plots.leave",
    usage = "/plot leave",
    category = CommandCategory.CLAIMING,
    requiredType = RequiredType.PLAYER)
public class Leave extends Command {
    public Leave() {
        super(MainCommand.getInstance(), true);
    }

    @Override public CompletableFuture<Boolean> execute(PlotPlayer player, String[] args,
        RunnableVal3<Command, Runnable, Runnable> confirm,
        RunnableVal2<Command, CommandResult> whenDone) throws CommandException {
        final Plot plot = check(player.getCurrentPlot(), Captions.NOT_IN_PLOT);
        checkTrue(plot.hasOwner(), Captions.PLOT_UNOWNED);
        checkTrue(plot.isAdded(player.getUUID()), Captions.NOT_ADDED_TRUSTED);
        checkTrue(args.length == 0, Captions.COMMAND_SYNTAX, getUsage());
        if (plot.isOwner(player.getUUID())) {
            checkTrue(plot.hasOwner(), Captions.ALREADY_OWNER);
            // TODO setowner, other
        } else {
            UUID uuid = player.getUUID();
            if (plot.isAdded(uuid)) {
                if (plot.removeTrusted(uuid)) {
                    PlotSquared.get().getEventDispatcher().callTrusted(player, plot, uuid, false);
                }
                if (plot.removeMember(uuid)) {
                    PlotSquared.get().getEventDispatcher().callMember(player, plot, uuid, false);
                }
                MainUtil.sendMessage(player, Captions.PLOT_LEFT, player.getName());
            } else {
                MainUtil.sendMessage(player, Captions.INVALID_PLAYER, 1);
            }
        }
        return CompletableFuture.completedFuture(true);
    }
}
