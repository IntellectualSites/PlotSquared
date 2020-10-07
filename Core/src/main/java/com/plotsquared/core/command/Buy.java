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
import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.configuration.caption.TranslatableCaption;
import com.plotsquared.core.events.PlotFlagRemoveEvent;
import com.plotsquared.core.events.Result;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.PlotArea;
import com.plotsquared.core.plot.flag.PlotFlag;
import com.plotsquared.core.plot.flag.implementations.PriceFlag;
import com.plotsquared.core.synchronization.LockKey;
import com.plotsquared.core.synchronization.LockRepository;
import com.plotsquared.core.util.EconHandler;
import com.plotsquared.core.util.EventDispatcher;
import com.plotsquared.core.util.task.RunnableVal2;
import com.plotsquared.core.util.task.RunnableVal3;
import net.kyori.adventure.text.minimessage.Template;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

@CommandDeclaration(command = "buy",
    usage = "/plot buy",
    permission = "plots.buy",
    category = CommandCategory.CLAIMING,
    requiredType = RequiredType.NONE)
public class Buy extends Command {

    private final EventDispatcher eventDispatcher;
    private final EconHandler econHandler;

    @Inject public Buy(@Nonnull final EventDispatcher eventDispatcher,
                       @Nonnull final EconHandler econHandler) {
        super(MainCommand.getInstance(), true);
        this.eventDispatcher = eventDispatcher;
        this.econHandler = econHandler;
    }

    @Override
    public CompletableFuture<Boolean> execute(final PlotPlayer<?> player, String[] args,
        RunnableVal3<Command, Runnable, Runnable> confirm,
        final RunnableVal2<Command, CommandResult> whenDone) {

        PlotArea area = player.getPlotAreaAbs();
        check(area, TranslatableCaption.of("errors.not_in_plot_world"));
        check(this.econHandler.isEnabled(area), TranslatableCaption.of("economy.econ_disabled"));
        final Plot plot;
        if (args.length != 0) {
            if (args.length != 1) {
                sendUsage(player);
                return CompletableFuture.completedFuture(false);
            }
            plot = check(Plot.getPlotFromString(player, args[0], true), null);
        } else {
            plot = check(player.getCurrentPlot(), TranslatableCaption.of("errors.not_in_plot"));
        }
        checkTrue(plot.hasOwner(), TranslatableCaption.of("info.plot_unowned"));
        checkTrue(!plot.isOwner(player.getUUID()), TranslatableCaption.of("economy.cannot_buy_own"));
        Set<Plot> plots = plot.getConnectedPlots();
        checkTrue(player.getPlotCount() + plots.size() <= player.getAllowedPlots(),
            TranslatableCaption.of("permission.cant_claim_more_plots"));
        double price = plot.getFlag(PriceFlag.class);
        if (price <= 0) {
            throw new CommandException(TranslatableCaption.of("economy.not_for_sale"));
        }
        checkTrue(this.econHandler.getMoney(player) >= price,
                TranslatableCaption.of("economy.cannot_afford_plot"));
        this.econHandler.withdrawMoney(player, price);
        // Failure
        // Success
        confirm.run(this, () -> {
            player.sendMessage(
                    TranslatableCaption.of("economy.removed_balance"),
                    Template.of("money", String.valueOf(price))
            );

            this.econHandler.depositMoney(PlotSquared.platform().getPlayerManager().getOfflinePlayer(plot.getOwnerAbs()), price);

            PlotPlayer<?> owner = PlotSquared.platform().getPlayerManager().getPlayerIfExists(plot.getOwnerAbs());
            if (owner != null) {
                owner.sendMessage(
                        TranslatableCaption.of("economy.plot_sold"),
                        Template.of("plot", plot.getId().toString()),
                        Template.of("player", player.getName()),
                        Template.of("price", String.valueOf(price))
                );
            }
            PlotFlag<?, ?> plotFlag = plot.getFlagContainer().getFlag(PriceFlag.class);
            PlotFlagRemoveEvent event = this.eventDispatcher.callFlagRemove(plotFlag, plot);
            if (event.getEventResult() != Result.DENY) {
                plot.removeFlag(event.getFlag());
            }
            plot.setOwner(player.getUUID());
            player.sendMessage(TranslatableCaption.of("working.claimed"));
            whenDone.run(Buy.this, CommandResult.SUCCESS);
        }, () -> {
            this.econHandler.depositMoney(player, price);
            whenDone.run(Buy.this, CommandResult.FAILURE);
        });
        return CompletableFuture.completedFuture(true);
    }
}
