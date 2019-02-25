package com.intellectualcrafters.plot.commands;

import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.object.RunnableVal2;
import com.intellectualcrafters.plot.object.RunnableVal3;
import com.intellectualcrafters.plot.util.EventUtil;
import com.intellectualcrafters.plot.util.MainUtil;
import com.plotsquared.general.commands.Command;
import com.plotsquared.general.commands.CommandDeclaration;
import java.util.UUID;

@CommandDeclaration(command = "leave",
        description = "Removes self from being trusted or a member of the plot",
        permission = "plots.leave",
        category = CommandCategory.CLAIMING,
        requiredType = RequiredType.NONE)
public class Leave extends Command {
    public Leave() {
        super(MainCommand.getInstance(), true);
    }

    @Override
    public void execute(PlotPlayer player, String[] args, RunnableVal3<Command, Runnable, Runnable> confirm, RunnableVal2<Command, CommandResult> whenDone) throws CommandException {
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