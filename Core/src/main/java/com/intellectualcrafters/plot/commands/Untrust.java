package com.intellectualcrafters.plot.commands;

import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.Permissions;
import com.intellectualcrafters.plot.util.UUIDHandler;
import com.plotsquared.general.commands.Argument;
import com.plotsquared.general.commands.CommandDeclaration;

import java.util.ArrayList;
import java.util.UUID;

//     UNTRUST("untrust", "ut"),

@CommandDeclaration(
        command = "untrust",
        aliases = {"ut"},
        permission = "plots.untrust",
        description = "Remove a trusted user from a plot",
        usage = "/plot untrust <player>",
        requiredType = RequiredType.PLAYER,
        category = CommandCategory.SETTINGS)
public class Untrust extends SubCommand {

    public Untrust() {
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
        if (!plot.isOwner(plr.getUUID()) && !Permissions.hasPermission(plr, "plots.admin.command.untrust")) {
            MainUtil.sendMessage(plr, C.NO_PLOT_PERMS);
            return true;
        }
        int count = 0;
        switch (args[0]) {
            case "unknown":
                ArrayList<UUID> toRemove = new ArrayList<>();
                for (UUID uuid : plot.getTrusted()) {
                    if (UUIDHandler.getName(uuid) == null) {
                        toRemove.add(uuid);
                    }
                }
                for (UUID uuid : toRemove) {
                    plot.removeTrusted(uuid);
                    count++;
                }
                break;
            case "*":
                for (UUID uuid : new ArrayList<>(plot.getTrusted())) {
                    plot.removeTrusted(uuid);
                    count++;
                }
                break;
            default:
                UUID uuid = UUIDHandler.getUUID(args[0], null);
                if (uuid != null) {
                    if (plot.removeTrusted(uuid)) {
                        count++;
                    }
                }
                break;
        }
        if (count == 0) {
            MainUtil.sendMessage(plr, C.INVALID_PLAYER, args[0]);
            return false;
        } else {
            MainUtil.sendMessage(plr, C.REMOVED_PLAYERS, count + "");
        }
        return true;
    }
}
