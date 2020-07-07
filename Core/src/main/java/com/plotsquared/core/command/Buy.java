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

import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.configuration.Captions;
import com.plotsquared.core.events.PlotFlagRemoveEvent;
import com.plotsquared.core.events.Result;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.flag.PlotFlag;
import com.plotsquared.core.plot.flag.implementations.PriceFlag;
import com.plotsquared.core.util.EconHandler;
import com.plotsquared.core.util.MainUtil;
import com.plotsquared.core.util.task.RunnableVal2;
import com.plotsquared.core.util.task.RunnableVal3;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

@CommandDeclaration(command = "buy",
    description = "Buy the plot you are standing on",
    usage = "/plot buy",
    permission = "plots.buy",
    category = CommandCategory.CLAIMING,
    requiredType = RequiredType.NONE)
public class Buy extends Command {

    public Buy() {
        super(MainCommand.getInstance(), true);
    }

    @Override
    public CompletableFuture<Boolean> execute(final PlotPlayer<?> player, String[] args,
        RunnableVal3<Command, Runnable, Runnable> confirm,
        final RunnableVal2<Command, CommandResult> whenDone) {

        check(EconHandler.getEconHandler(), Captions.ECON_DISABLED);
        final Plot plot;
        if (args.length != 0) {
            checkTrue(args.length == 1, Captions.COMMAND_SYNTAX, getUsage());
            plot = check(MainUtil.getPlotFromString(player, args[0], true), null);
        } else {
            plot = check(player.getCurrentPlot(), Captions.NOT_IN_PLOT);
        }
        checkTrue(plot.hasOwner(), Captions.PLOT_UNOWNED);
        checkTrue(!plot.isOwner(player.getUUID()), Captions.CANNOT_BUY_OWN);
        Set<Plot> plots = plot.getConnectedPlots();
        checkTrue(player.getPlotCount() + plots.size() <= player.getAllowedPlots(),
            Captions.CANT_CLAIM_MORE_PLOTS);
        double price = plot.getFlag(PriceFlag.class);
        if (price <= 0) {
            throw new CommandException(Captions.NOT_FOR_SALE);
        }
        checkTrue(player.getMoney() >= price, Captions.CANNOT_AFFORD_PLOT);
        player.withdraw(price);
        // Failure
        // Success
        confirm.run(this, () -> {
            Captions.REMOVED_BALANCE.send(player, price);

            EconHandler.getEconHandler().depositMoney(PlotSquared.platform().getPlayerManager().getOfflinePlayer(plot.getOwnerAbs()), price);

            PlotPlayer owner = PlotSquared.platform().getPlayerManager().getPlayerIfExists(plot.getOwnerAbs());
            if (owner != null) {
                Captions.PLOT_SOLD.send(owner, plot.getId(), player.getName(), price);
            }
            PlotFlag<?, ?> plotFlag = plot.getFlagContainer().getFlag(PriceFlag.class);
            PlotFlagRemoveEvent event =
                PlotSquared.get().getEventDispatcher().callFlagRemove(plotFlag, plot);
            if (event.getEventResult() != Result.DENY) {
                plot.removeFlag(event.getFlag());
            }
            plot.setOwner(player.getUUID());
            Captions.CLAIMED.send(player);
            whenDone.run(Buy.this, CommandResult.SUCCESS);
        }, () -> {
            player.deposit(price);
            whenDone.run(Buy.this, CommandResult.FAILURE);
        });
        return CompletableFuture.completedFuture(true);
    }
}
