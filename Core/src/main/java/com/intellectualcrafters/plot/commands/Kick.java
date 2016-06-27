package com.intellectualcrafters.plot.commands;

import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.database.DBFunc;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.Permissions;
import com.intellectualcrafters.plot.util.UUIDHandler;
import com.intellectualcrafters.plot.util.WorldUtil;
import com.plotsquared.general.commands.Argument;
import com.plotsquared.general.commands.CommandDeclaration;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@CommandDeclaration(command = "kick",
        aliases = {"k"},
        description = "Kick a player from your plot",
        permission = "plots.kick",
        usage = "<player>",
        category = CommandCategory.TELEPORT,
        requiredType = RequiredType.NONE)
public class Kick extends SubCommand {

    public Kick() {
        super(Argument.PlayerName);
    }

    @Override
    public boolean onCommand(PlotPlayer player, String[] args) {
        Location location = player.getLocation();
        Plot plot = location.getPlot();
        if (plot == null) {
            return !sendMessage(player, C.NOT_IN_PLOT);
        }
        if ((!plot.hasOwner() || !plot.isOwner(player.getUUID())) && !Permissions.hasPermission(player, "plots.admin.command.kick")) {
            MainUtil.sendMessage(player, C.NO_PLOT_PERMS);
            return false;
        }
        Set<UUID> uuids = MainUtil.getUUIDsFromString(args[0]);
        if (uuids.isEmpty()) {
            MainUtil.sendMessage(player, C.INVALID_PLAYER, args[0]);
            return false;
        }
        Set<PlotPlayer> players = new HashSet<>();
        for (UUID uuid : uuids) {
            if (uuid == DBFunc.everyone) {
                for (PlotPlayer pp : plot.getPlayersInPlot()) {
                    if (pp == player || pp.hasPermission("plots.admin.entry.denied")) {
                        continue;
                    }
                    players.add(pp);
                }
                continue;
            }
            PlotPlayer pp = UUIDHandler.getPlayer(uuid);
            if (pp != null) {
                players.add(pp);
            }
        }
        players.remove(player); // Don't ever kick the calling player
        if (players.isEmpty()) {
            MainUtil.sendMessage(player, C.INVALID_PLAYER, args[0]);
            return false;
        }
        for (PlotPlayer player2 : players) {
            if (!plot.equals(player2.getCurrentPlot())) {
                MainUtil.sendMessage(player, C.INVALID_PLAYER, args[0]);
                return false;
            }
            if (player2.hasPermission("plots.admin.entry.denied")) {
                C.CANNOT_KICK_PLAYER.send(player, player2.getName());
                return false;
            }
            Location spawn = WorldUtil.IMP.getSpawn(location.getWorld());
            C.YOU_GOT_KICKED.send(player2);
            if (plot.equals(spawn.getPlot())) {
                Location newSpawn = WorldUtil.IMP.getSpawn(player2);
                if (plot.equals(newSpawn.getPlot())) {
                    // Kick from server if you can't be teleported to spawn
                    player2.kick(C.YOU_GOT_KICKED.s());
                } else {
                    player2.teleport(newSpawn);
                }
            } else {
                player2.teleport(spawn);
            }
        }
        return true;
    }
}
