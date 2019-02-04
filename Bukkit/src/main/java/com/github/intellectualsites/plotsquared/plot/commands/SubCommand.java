package com.github.intellectualsites.plotsquared.plot.commands;

import com.github.intellectualsites.plotsquared.commands.Argument;
import com.github.intellectualsites.plotsquared.commands.Command;
import com.github.intellectualsites.plotsquared.plot.config.C;
import com.github.intellectualsites.plotsquared.plot.object.PlotPlayer;
import com.github.intellectualsites.plotsquared.plot.object.RunnableVal2;
import com.github.intellectualsites.plotsquared.plot.object.RunnableVal3;

/**
 * SubCommand class
 *
 * @Deprecated In favor of normal Command class
 * @see Command(Command, boolean)
 */
public abstract class SubCommand extends Command {
    public SubCommand() {
        super(MainCommand.getInstance(), true);
    }

    public SubCommand(Argument... arguments) {
        this();
        setRequiredArguments(arguments);
    }

    @Override public void execute(PlotPlayer player, String[] args,
        RunnableVal3<Command, Runnable, Runnable> confirm,
        RunnableVal2<Command, CommandResult> whenDone) {
        onCommand(player, args);
    }


    public abstract boolean onCommand(PlotPlayer player, String[] args);

    public boolean sendMessage(PlotPlayer player, C message, Object... args) {
        message.send(player, args);
        return true;
    }
}
