package com.intellectualcrafters.plot.commands;

import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.database.DBFunc;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.EventUtil;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.Permissions;
import com.plotsquared.general.commands.Argument;
import com.plotsquared.general.commands.CommandDeclaration;
import java.util.*;
import java.util.Set;

@CommandDeclaration(
        command = "trust",
        aliases = {"t"},
        requiredType = RequiredType.PLAYER,
        usage = "/plot trust <player>",
        description = "Allow a player to build in a plot",
        category = CommandCategory.SETTINGS)
public class Trust extends SubCommand {

    public Trust() {
        super(Argument.PlayerName);
    }

    @Override
    public boolean onCommand(PlotPlayer plr, String[] args) {
        Location loc = plr.getLocation();
        Plot plot = loc.getPlotAbs();
        if (plot == null) {
            return !sendMessage(plr, C.NOT_IN_PLOT);
        }
        if (!plot.hasOwner()) {
            MainUtil.sendMessage(plr, C.PLOT_UNOWNED);
            return false;
        }
        if (!plot.isOwner(plr.getUUID()) && !Permissions.hasPermission(plr, "plots.admin.command.trust")) {
            MainUtil.sendMessage(plr, C.NO_PLOT_PERMS);
            return true;
        }
        Set<UUID> uuids = MainUtil.getUUIDsFromString(args[0]);
        if (uuids == null || uuids.isEmpty()) {
            MainUtil.sendMessage(plr, C.INVALID_PLAYER, args[0]);
            return false;
        }
        Iterator<UUID> iter = uuids.iterator();
        while (iter.hasNext()) {
            UUID uuid = iter.next();
            if (uuid == DBFunc.everyone && !(Permissions.hasPermission(plr, "plots.trust.everyone") || Permissions.hasPermission(plr, "plots.admin.command.trust"))) {
                MainUtil.sendMessage(plr, C.INVALID_PLAYER, MainUtil.getName(uuid));
                continue;
            }
            if (plot.isOwner(uuid)) {
                MainUtil.sendMessage(plr, C.ALREADY_OWNER, MainUtil.getName(uuid));
                continue;
            }
            if (plot.getTrusted().contains(uuid)) {
                MainUtil.sendMessage(plr, C.ALREADY_ADDED, MainUtil.getName(uuid));
                continue;
            }
            if (plot.removeMember(uuid)) {
                plot.addTrusted(uuid);
            } else {
                if ((plot.getMembers().size() + plot.getTrusted().size()) >= plot.getArea().MAX_PLOT_MEMBERS) {
                    MainUtil.sendMessage(plr, C.PLOT_MAX_MEMBERS);
                    continue;
                }
                if (plot.getDenied().contains(uuid)) {
                    plot.removeDenied(uuid);
                }
                plot.addTrusted(uuid);
            }
            EventUtil.manager.callTrusted(plr, plot, uuid, true);
        }
        if (!uuids.isEmpty()) {
            MainUtil.sendMessage(plr, C.TRUSTED_ADDED);
        }
        return true;
    }
}
