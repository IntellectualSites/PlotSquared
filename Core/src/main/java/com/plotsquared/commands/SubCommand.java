package com.plotsquared.commands;

import com.plotsquared.config.Captions;
import com.plotsquared.player.PlotPlayer;
import com.plotsquared.util.tasks.RunnableVal2;
import com.plotsquared.util.tasks.RunnableVal3;

import java.util.concurrent.CompletableFuture;

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

    public static boolean sendMessage(PlotPlayer player, Captions message, Object... args) {
        message.send(player, args);
        return true;
    }

    @Override public CompletableFuture<Boolean> execute(PlotPlayer player, String[] args,
        RunnableVal3<Command, Runnable, Runnable> confirm,
        RunnableVal2<Command, CommandResult> whenDone) {
        return CompletableFuture.completedFuture(onCommand(player, args));
    }

    public abstract boolean onCommand(PlotPlayer player, String[] args);
}
