package com.intellectualcrafters.plot.commands;

import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.database.DBFunc;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.EventUtil;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.Permissions;
import com.intellectualcrafters.plot.util.PlotGameMode;
import com.intellectualcrafters.plot.util.UUIDHandler;
import com.intellectualcrafters.plot.util.WorldUtil;
import com.plotsquared.general.commands.Argument;
import com.plotsquared.general.commands.CommandDeclaration;

import java.util.*;
import java.util.Set;

@CommandDeclaration(command = "deny",
        aliases = {"d", "ban"},
        description = "Deny a user from a plot",
        usage = "/plot deny <player>",
        category = CommandCategory.SETTINGS,
        requiredType = RequiredType.PLAYER)
public class Deny extends SubCommand {

    public Deny() {
        super(Argument.PlayerName);
    }

    @Override
    public boolean onCommand(PlotPlayer plr, String[] args) {

        Location location = plr.getLocation();
        Plot plot = location.getPlotAbs();
        if (plot == null) {
            return !sendMessage(plr, C.NOT_IN_PLOT);
        }
        if (!plot.hasOwner()) {
            MainUtil.sendMessage(plr, C.PLOT_UNOWNED);
            return false;
        }
        if (!plot.isOwner(plr.getUUID()) && !Permissions.hasPermission(plr, "plots.admin.command.deny")) {
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
            if (uuid == DBFunc.everyone && !(Permissions.hasPermission(plr, "plots.deny.everyone") || Permissions.hasPermission(plr, "plots.admin.command.deny"))) {
                MainUtil.sendMessage(plr, C.INVALID_PLAYER, MainUtil.getName(uuid));
                continue;
            }
            if (plot.isOwner(uuid)) {
                MainUtil.sendMessage(plr, C.ALREADY_OWNER, MainUtil.getName(uuid));
                return false;
            }

            if (plot.getDenied().contains(uuid)) {
                MainUtil.sendMessage(plr, C.ALREADY_ADDED, MainUtil.getName(uuid));
                return false;
            }
            plot.removeMember(uuid);
            plot.removeTrusted(uuid);
            plot.addDenied(uuid);
            EventUtil.manager.callDenied(plr, plot, uuid, true);
            if (!uuid.equals(DBFunc.everyone)) {
                handleKick(UUIDHandler.getPlayer(uuid), plot);
            } else {
                for (PlotPlayer pp : plot.getPlayersInPlot()) {
                    handleKick(pp, plot);
                }
            }
        }
        if (!uuids.isEmpty()) {
            MainUtil.sendMessage(plr, C.DENIED_ADDED);
        }
        return true;
    }

    private void handleKick(PlotPlayer pp, Plot plot) {
        if (pp == null) {
            return;
        }
        if (!plot.equals(pp.getCurrentPlot())) {
            return;
        }
        if (pp.hasPermission("plots.admin.entry.denied")) {
            return;
        }
        if (pp.getGameMode() == PlotGameMode.SPECTATOR) {
            pp.stopSpectating();
        }
        pp.teleport(WorldUtil.IMP.getSpawn(pp.getLocation().getWorld()));
        MainUtil.sendMessage(pp, C.YOU_GOT_DENIED);
    }
}
