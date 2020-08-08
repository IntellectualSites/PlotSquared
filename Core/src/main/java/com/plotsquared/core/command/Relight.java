/*
 *       _____  _       _    _____                                _
 *      |  __ \| |     | |  / ____|                              | |
 *      | |__) | | ___ | |_| (___   __ _ _   _  __ _ _ __ ___  __| |
 *      |  ___/| |/ _ \| __|\___ \ / _` | | | |/ _` | '__/ _ \/ _` |
 *      | |    | | (_) | |_ ____) | (_| | |_| | (_| | | |  __/ (_| |
 *      |_|    |_|\___/ \__|_____/ \__, |\__,_|\__,_|_|  \___|\__,_|
 *                                    | |
 *                                    |_|
 *            PlotSquared plot management system for Minecraft
 *                  Copyright (C) 2020 IntellectualSites
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.plotsquared.core.command;

import com.plotsquared.core.configuration.caption.StaticCaption;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.util.task.RunnableVal2;
import com.plotsquared.core.util.task.RunnableVal3;

import java.util.concurrent.CompletableFuture;

@CommandDeclaration(command = "relight",
    description = "Relight your plot",
    usage = "/plot relight",
    category = CommandCategory.DEBUG,
    requiredType = RequiredType.PLAYER)
public class Relight extends Command {
    public Relight() {
        super(MainCommand.getInstance(), true);
    }

    @Override
    public CompletableFuture<Boolean> execute(final PlotPlayer<?> player, String[] args,
        RunnableVal3<Command, Runnable, Runnable> confirm,
        RunnableVal2<Command, CommandResult> whenDone) {
        player.sendMessage(StaticCaption.of("Not implemented."));
/*        final Plot plot = player.getCurrentPlot();
        if (plot == null) {
            player.sendMessage(TranslatableCaption.of("errors.not_in_plot"));
            return CompletableFuture.completedFuture(false);
        }
        final QueueCoordinator queue = plot.getArea().getQueue(false);
        ChunkManager.chunkTask(plot, new RunnableVal<int[]>() {
            @Override public void run(int[] value) {
                queue.fixChunkLighting(value[0], value[1]);
            }
        }, () -> {
            plot.refreshChunks();
            player.sendMessage(TranslatableCaption.of("setblock.set_block_action_finished"));
        }, 5);*/

        return CompletableFuture.completedFuture(true);
    }
}
