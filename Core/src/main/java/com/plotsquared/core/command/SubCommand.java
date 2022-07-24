/*
 * PlotSquared, a land and world management plugin for Minecraft.
 * Copyright (C) IntellectualSites <https://intellectualsites.com>
 * Copyright (C) IntellectualSites team and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.plotsquared.core.command;

import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.util.task.RunnableVal2;
import com.plotsquared.core.util.task.RunnableVal3;

import java.util.concurrent.CompletableFuture;

/**
 * SubCommand class
 *
 * @see Command#Command(Command, boolean)
 * @deprecated In favor of normal Command class
 */
public abstract class SubCommand extends Command {

    public SubCommand() {
        super(MainCommand.getInstance(), true);
    }

    public SubCommand(Argument<?>... arguments) {
        this();
        setRequiredArguments(arguments);
    }

    @Override
    public CompletableFuture<Boolean> execute(
            PlotPlayer<?> player, String[] args,
            RunnableVal3<Command, Runnable, Runnable> confirm,
            RunnableVal2<Command, CommandResult> whenDone
    ) {
        return CompletableFuture.completedFuture(onCommand(player, args));
    }

    public abstract boolean onCommand(PlotPlayer<?> player, String[] args);

}
