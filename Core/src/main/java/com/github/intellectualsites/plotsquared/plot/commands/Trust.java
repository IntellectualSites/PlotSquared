package com.github.intellectualsites.plotsquared.plot.commands;

import com.github.intellectualsites.plotsquared.commands.Command;
import com.github.intellectualsites.plotsquared.commands.CommandDeclaration;
import com.github.intellectualsites.plotsquared.plot.config.Captions;
import com.github.intellectualsites.plotsquared.plot.database.DBFunc;
import com.github.intellectualsites.plotsquared.plot.object.Plot;
import com.github.intellectualsites.plotsquared.plot.object.PlotPlayer;
import com.github.intellectualsites.plotsquared.plot.object.RunnableVal2;
import com.github.intellectualsites.plotsquared.plot.object.RunnableVal3;
import com.github.intellectualsites.plotsquared.plot.util.EventUtil;
import com.github.intellectualsites.plotsquared.plot.util.MainUtil;
import com.github.intellectualsites.plotsquared.plot.util.Permissions;

import java.util.Iterator;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@CommandDeclaration(command = "trust", aliases = {"t"}, requiredType = RequiredType.PLAYER,
    usage = "/plot trust <player|*>",
    description = "Allow a user to build in a plot and use WorldEdit while the plot owner is offline.",
    category = CommandCategory.SETTINGS) public class Trust extends Command {

    public Trust() {
        super(MainCommand.getInstance(), true);
    }

    @Override public CompletableFuture<Boolean> execute(final PlotPlayer player, String[] args,
        RunnableVal3<Command, Runnable, Runnable> confirm,
        RunnableVal2<Command, CommandResult> whenDone) throws CommandException {
        final Plot currentPlot = player.getCurrentPlot();
        if (currentPlot == null) {
            throw new CommandException(Captions.NOT_IN_PLOT);
        }
        checkTrue(currentPlot.hasOwner(), Captions.PLOT_UNOWNED);
        checkTrue(currentPlot.isOwner(player.getUUID()) || Permissions
                .hasPermission(player, Captions.PERMISSION_ADMIN_COMMAND_TRUST),
            Captions.NO_PLOT_PERMS);
        checkTrue(args.length == 1, Captions.COMMAND_SYNTAX, getUsage());
        final Set<UUID> uuids = MainUtil.getUUIDsFromString(args[0]);
        checkTrue(!uuids.isEmpty(), Captions.INVALID_PLAYER, args[0]);
        Iterator<UUID> iterator = uuids.iterator();
        int size = currentPlot.getTrusted().size() + currentPlot.getMembers().size();
        while (iterator.hasNext()) {
            UUID uuid = iterator.next();
            if (uuid == DBFunc.EVERYONE && !(
                Permissions.hasPermission(player, Captions.PERMISSION_TRUST_EVERYONE) || Permissions
                    .hasPermission(player, Captions.PERMISSION_ADMIN_COMMAND_TRUST))) {
                MainUtil.sendMessage(player, Captions.INVALID_PLAYER, MainUtil.getName(uuid));
                iterator.remove();
                continue;
            }
            if (currentPlot.isOwner(uuid)) {
                MainUtil.sendMessage(player, Captions.ALREADY_ADDED, MainUtil.getName(uuid));
                iterator.remove();
                continue;
            }
            if (currentPlot.getTrusted().contains(uuid)) {
                MainUtil.sendMessage(player, Captions.ALREADY_ADDED, MainUtil.getName(uuid));
                iterator.remove();
                continue;
            }
            size += currentPlot.getMembers().contains(uuid) ? 0 : 1;
        }
        checkTrue(!uuids.isEmpty(), null);
        checkTrue(size <= currentPlot.getArea().MAX_PLOT_MEMBERS || Permissions
                .hasPermission(player, Captions.PERMISSION_ADMIN_COMMAND_TRUST),
            Captions.PLOT_MAX_MEMBERS);
        // Success
        confirm.run(this, () -> {
            for (UUID uuid : uuids) {
                if (uuid != DBFunc.EVERYONE) {
                    if (!currentPlot.removeMember(uuid)) {
                        if (currentPlot.getDenied().contains(uuid)) {
                            currentPlot.removeDenied(uuid);
                        }
                    }
                }
                currentPlot.addTrusted(uuid);
                EventUtil.manager.callTrusted(player, currentPlot, uuid, true);
                MainUtil.sendMessage(player, Captions.TRUSTED_ADDED);
            }
        }, null);

        return CompletableFuture.completedFuture(true);
    }
}
