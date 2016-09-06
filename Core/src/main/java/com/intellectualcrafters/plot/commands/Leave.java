package com.intellectualcrafters.plot.commands;

import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.object.RunnableVal2;
import com.intellectualcrafters.plot.object.RunnableVal3;
import com.plotsquared.general.commands.Command;
import com.plotsquared.general.commands.CommandDeclaration;
import java.util.*;
import java.util.Set;

@CommandDeclaration(command = "leave",
        description = "Leave a plot",
        permission = "plots.leave",
        category = CommandCategory.CLAIMING,
        requiredType = RequiredType.NONE)
public class Leave extends Command {
    public Leave(Command parent, boolean isStatic) {
        super(MainCommand.getInstance(), true);
    }

    @Override
    public void execute(PlotPlayer player, String[] args, RunnableVal3<Command, Runnable, Runnable> confirm, RunnableVal2<Command, CommandResult> whenDone) throws CommandException {
        final Plot plot = check(player.getCurrentPlot(), C.NOT_IN_PLOT);
        checkTrue(plot.hasOwner(), C.PLOT_UNOWNED);
        checkTrue(plot.isAdded(player.getUUID()), C.NO_PLOT_PERMS);
        checkTrue(args.length == 0, C.COMMAND_SYNTAX, getUsage());
        if (plot.isOwner(player.getUUID())) {
            Set<UUID> owners = plot.getOwners();
        } else {

        }
    }
}
