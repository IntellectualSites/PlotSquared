package com.github.intellectualsites.plotsquared.plot.commands;

import com.github.intellectualsites.plotsquared.commands.Command;
import com.github.intellectualsites.plotsquared.commands.CommandDeclaration;
import com.github.intellectualsites.plotsquared.plot.config.C;
import com.github.intellectualsites.plotsquared.plot.flag.Flags;
import com.github.intellectualsites.plotsquared.plot.object.Plot;
import com.github.intellectualsites.plotsquared.plot.object.PlotPlayer;
import com.github.intellectualsites.plotsquared.plot.object.RunnableVal2;
import com.github.intellectualsites.plotsquared.plot.object.RunnableVal3;
import com.github.intellectualsites.plotsquared.plot.util.EconHandler;
import com.github.intellectualsites.plotsquared.plot.util.MainUtil;
import com.github.intellectualsites.plotsquared.plot.util.UUIDHandler;
import com.google.common.base.Optional;

import java.util.Set;

@CommandDeclaration(command = "buy", description = "Buy the plot you are standing on",
    usage = "/plot buy", permission = "plots.buy", category = CommandCategory.CLAIMING,
    requiredType = RequiredType.NONE) public class Buy extends Command {

    public Buy() {
        super(MainCommand.getInstance(), true);
    }

    @Override public void execute(final PlotPlayer player, String[] args,
        RunnableVal3<Command, Runnable, Runnable> confirm,
        final RunnableVal2<Command, CommandResult> whenDone) {
        check(EconHandler.manager, C.ECON_DISABLED);
        final Plot plot;
        if (args.length != 0) {
            checkTrue(args.length == 1, C.COMMAND_SYNTAX, getUsage());
            plot = check(MainUtil.getPlotFromString(player, args[0], true), null);
        } else {
            plot = check(player.getCurrentPlot(), C.NOT_IN_PLOT);
        }
        checkTrue(plot.hasOwner(), C.PLOT_UNOWNED);
        checkTrue(!plot.isOwner(player.getUUID()), C.CANNOT_BUY_OWN);
        Set<Plot> plots = plot.getConnectedPlots();
        checkTrue(player.getPlotCount() + plots.size() <= player.getAllowedPlots(),
            C.CANT_CLAIM_MORE_PLOTS);
        Optional<Double> flag = plot.getFlag(Flags.PRICE);
        if (!flag.isPresent()) {
            throw new CommandException(C.NOT_FOR_SALE);
        }
        final double price = flag.get();
        checkTrue(player.getMoney() >= price, C.CANNOT_AFFORD_PLOT);
        player.withdraw(price);
        // Failure
        // Success
        confirm.run(this, () -> {
            C.REMOVED_BALANCE.send(player, price);
            EconHandler.manager
                .depositMoney(UUIDHandler.getUUIDWrapper().getOfflinePlayer(plot.guessOwner()),
                    price);
            PlotPlayer owner = UUIDHandler.getPlayer(plot.guessOwner());
            if (owner != null) {
                C.PLOT_SOLD.send(owner, plot.getId(), player.getName(), price);
            }
            plot.removeFlag(Flags.PRICE);
            plot.setOwner(player.getUUID());
            C.CLAIMED.send(player);
            whenDone.run(Buy.this, CommandResult.SUCCESS);
        }, () -> {
            player.deposit(price);
            whenDone.run(Buy.this, CommandResult.FAILURE);
        });
    }
}
