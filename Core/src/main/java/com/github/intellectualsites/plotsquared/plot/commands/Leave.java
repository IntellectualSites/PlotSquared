package com.github.intellectualsites.plotsquared.plot.commands;

import com.github.intellectualsites.plotsquared.commands.Command;
import com.github.intellectualsites.plotsquared.commands.CommandDeclaration;
import com.github.intellectualsites.plotsquared.plot.config.C;
import com.github.intellectualsites.plotsquared.plot.object.Plot;
import com.github.intellectualsites.plotsquared.plot.object.PlotPlayer;
import com.github.intellectualsites.plotsquared.plot.object.RunnableVal2;
import com.github.intellectualsites.plotsquared.plot.object.RunnableVal3;
import com.github.intellectualsites.plotsquared.plot.util.EventUtil;
import com.github.intellectualsites.plotsquared.plot.util.MainUtil;

import java.util.UUID;

@CommandDeclaration(command = "leave", description = "Leave a plot", permission = "plots.leave",
    category = CommandCategory.CLAIMING, requiredType = RequiredType.NONE) public class Leave
    extends Command {
    public Leave() {
        super(MainCommand.getInstance(), true);
    }

    @Override public void execute(PlotPlayer player, String[] args,
        RunnableVal3<Command, Runnable, Runnable> confirm,
        RunnableVal2<Command, CommandResult> whenDone) throws CommandException {
        final Plot plot = check(player.getCurrentPlot(), C.NOT_IN_PLOT);
        checkTrue(plot.hasOwner(), C.PLOT_UNOWNED);
        checkTrue(plot.isAdded(player.getUUID()), C.NO_PLOT_PERMS);
        checkTrue(args.length == 0, C.COMMAND_SYNTAX, getUsage());
        if (plot.isOwner(player.getUUID())) {
            checkTrue(plot.hasOwner(), C.ALREADY_OWNER);
            // TODO setowner, other
        } else {
            UUID uuid = player.getUUID();
            if (plot.isAdded(uuid)) {
                if (plot.removeTrusted(uuid)) {
                    EventUtil.manager.callTrusted(player, plot, uuid, false);
                }
                if (plot.removeMember(uuid)) {
                    EventUtil.manager.callMember(player, plot, uuid, false);
                }
                MainUtil.sendMessage(player, C.INVALID_PLAYER, args[0]);
            } else {
                MainUtil.sendMessage(player, C.REMOVED_PLAYERS, 1);
            }
        }
    }
}
