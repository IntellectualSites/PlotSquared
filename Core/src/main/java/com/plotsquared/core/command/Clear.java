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
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.plotsquared.core.command;

import com.google.inject.Inject;
import com.plotsquared.core.backup.BackupManager;
import com.plotsquared.core.configuration.Settings;
import com.plotsquared.core.configuration.caption.TranslatableCaption;
import com.plotsquared.core.events.PlotFlagRemoveEvent;
import com.plotsquared.core.events.Result;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.flag.PlotFlag;
import com.plotsquared.core.plot.flag.implementations.AnalysisFlag;
import com.plotsquared.core.plot.flag.implementations.DoneFlag;
import com.plotsquared.core.queue.GlobalBlockQueue;
import com.plotsquared.core.util.EventDispatcher;
import com.plotsquared.core.util.Permissions;
import com.plotsquared.core.util.task.RunnableVal2;
import com.plotsquared.core.util.task.RunnableVal3;
import net.kyori.adventure.text.minimessage.Template;
import com.plotsquared.core.util.task.TaskManager;
import javax.annotation.Nonnull;
import java.util.concurrent.CompletableFuture;

@CommandDeclaration(command = "clear",
    description = "Clear the plot you stand on",
    requiredType = RequiredType.NONE,
    permission = "plots.clear",
    category = CommandCategory.APPEARANCE,
    usage = "/plot clear",
    aliases = "reset",
    confirmation = true)
public class Clear extends Command {

    private final EventDispatcher eventDispatcher;
    private final GlobalBlockQueue blockQueue;

    @Inject public Clear(@Nonnull final EventDispatcher eventDispatcher,
                         @Nonnull final GlobalBlockQueue blockQueue) {
        super(MainCommand.getInstance(), true);
        this.eventDispatcher = eventDispatcher;
        this.blockQueue = blockQueue;
    }

    @Override
    public CompletableFuture<Boolean> execute(final PlotPlayer<?> player, String[] args,
        RunnableVal3<Command, Runnable, Runnable> confirm,
        RunnableVal2<Command, CommandResult> whenDone) throws CommandException {
        if (args.length != 0) {
            sendUsage(player);
            return CompletableFuture.completedFuture(false);
        }
        final Plot plot = check(player.getCurrentPlot(), TranslatableCaption.of("errors.not_in_plot"));
        Result eventResult = this.eventDispatcher.callClear(plot).getEventResult();
        if (eventResult == Result.DENY) {
            player.sendMessage(
                    TranslatableCaption.of("events.event_denied"),
                    Template.of("value", "Clear"));
            return CompletableFuture.completedFuture(true);
        }
        boolean force = eventResult == Result.FORCE;
        checkTrue(force || plot.isOwner(player.getUUID()) || Permissions
                .hasPermission(player, "plots.admin.command.clear"),
            TranslatableCaption.of("permission.no_plot_perms"));
        checkTrue(plot.getRunning() == 0, TranslatableCaption.of("errors.wait_for_timer"));
        checkTrue(force || !Settings.Done.RESTRICT_BUILDING || !DoneFlag.isDone(plot) || Permissions
            .hasPermission(player, "plots.continue"), TranslatableCaption.of("done.done_already_done"));
        confirm.run(this, () -> {
            BackupManager.backup(player, plot, () -> {
                final long start = System.currentTimeMillis();
                boolean result = plot.clear(true, false, () -> {
                    plot.unlink();
                    TaskManager.runTask(() -> {
                        plot.removeRunning();
                        // If the state changes, then mark it as no longer done
                        if (DoneFlag.isDone(plot)) {
                            PlotFlag<?, ?> plotFlag =
                                plot.getFlagContainer().getFlag(DoneFlag.class);
                            PlotFlagRemoveEvent event = this.eventDispatcher
                                .callFlagRemove(plotFlag, plot);
                            if (event.getEventResult() != Result.DENY) {
                                plot.removeFlag(event.getFlag());
                            }
                        }
                        if (!plot.getFlag(AnalysisFlag.class).isEmpty()) {
                            PlotFlag<?, ?> plotFlag =
                                plot.getFlagContainer().getFlag(AnalysisFlag.class);
                            PlotFlagRemoveEvent event = this.eventDispatcher
                                .callFlagRemove(plotFlag, plot);
                            if (event.getEventResult() != Result.DENY) {
                                plot.removeFlag(event.getFlag());
                            }
                        }
                        player.sendMessage(
                                TranslatableCaption.of("working.clearing_done"),
                                Template.of("amount", String.valueOf(System.currentTimeMillis() - start))
                        );
                    });
                });
                if (!result) {
                    player.sendMessage(TranslatableCaption.of("errors.wait_for_timer"));
                } else {
                    plot.addRunning();
                }
            });
        }, null);
        return CompletableFuture.completedFuture(true);
    }
}
