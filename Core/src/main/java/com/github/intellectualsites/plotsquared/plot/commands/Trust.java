package com.github.intellectualsites.plotsquared.plot.commands;

import com.github.intellectualsites.plotsquared.commands.Command;
import com.github.intellectualsites.plotsquared.commands.CommandDeclaration;
import com.github.intellectualsites.plotsquared.plot.config.C;
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

@CommandDeclaration(command = "trust", aliases = {
    "t"}, requiredType = RequiredType.NONE, usage = "/plot trust <player>", description = "Allow a player to build in a plot", category = CommandCategory.SETTINGS)
public class Trust extends Command {

    public Trust() {
        super(MainCommand.getInstance(), true);
    }

    @Override public void execute(final PlotPlayer player, String[] args,
        RunnableVal3<Command, Runnable, Runnable> confirm,
        RunnableVal2<Command, CommandResult> whenDone) throws CommandException {
        final Plot plot = check(player.getCurrentPlot(), C.NOT_IN_PLOT);
        checkTrue(plot.hasOwner(), C.PLOT_UNOWNED);
        checkTrue(plot.isOwner(player.getUUID()) || Permissions
            .hasPermission(player, C.PERMISSION_ADMIN_COMMAND_TRUST), C.NO_PLOT_PERMS);
        checkTrue(args.length == 1, C.COMMAND_SYNTAX, getUsage());
        final Set<UUID> uuids = MainUtil.getUUIDsFromString(args[0]);
        checkTrue(!uuids.isEmpty(), C.INVALID_PLAYER, args[0]);
        Iterator<UUID> iter = uuids.iterator();
        int size = plot.getTrusted().size() + plot.getMembers().size();
        while (iter.hasNext()) {
            UUID uuid = iter.next();
            if (uuid == DBFunc.everyone && !(
                Permissions.hasPermission(player, C.PERMISSION_TRUST_EVERYONE) || Permissions
                    .hasPermission(player, C.PERMISSION_ADMIN_COMMAND_TRUST))) {
                MainUtil.sendMessage(player, C.INVALID_PLAYER, MainUtil.getName(uuid));
                iter.remove();
                continue;
            }
            if (plot.isOwner(uuid)) {
                MainUtil.sendMessage(player, C.ALREADY_OWNER, MainUtil.getName(uuid));
                iter.remove();
                continue;
            }
            if (plot.getTrusted().contains(uuid)) {
                MainUtil.sendMessage(player, C.ALREADY_ADDED, MainUtil.getName(uuid));
                iter.remove();
                continue;
            }
            size += plot.getMembers().contains(uuid) ? 0 : 1;
        }
        checkTrue(!uuids.isEmpty(), null);
        checkTrue(size <= plot.getArea().MAX_PLOT_MEMBERS || Permissions
            .hasPermission(player, C.PERMISSION_ADMIN_COMMAND_TRUST), C.PLOT_MAX_MEMBERS);
        confirm.run(this, new Runnable() {
            @Override // Success
            public void run() {
                for (UUID uuid : uuids) {
                    if (uuid != DBFunc.everyone) {
                        if (!plot.removeMember(uuid)) {
                            if (plot.getDenied().contains(uuid)) {
                                plot.removeDenied(uuid);
                            }
                        }
                    }
                    plot.addTrusted(uuid);
                    EventUtil.manager.callTrusted(player, plot, uuid, true);
                    MainUtil.sendMessage(player, C.TRUSTED_ADDED);
                }
            }
        }, null);
    }
}
