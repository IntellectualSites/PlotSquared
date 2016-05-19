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
        requiredType = RequiredType.PLAYER)
public class Kick extends SubCommand {

    public Kick() {
        super(Argument.PlayerName);
    }

    @Override
    public boolean onCommand(PlotPlayer plr, String[] args) {
        Location location = plr.getLocation();
        Plot plot = location.getPlot();
        if (plot == null) {
            return !sendMessage(plr, C.NOT_IN_PLOT);
        }
        if ((!plot.hasOwner() || !plot.isOwner(plr.getUUID())) && !Permissions.hasPermission(plr, "plots.admin.command.kick")) {
            MainUtil.sendMessage(plr, C.NO_PLOT_PERMS);
            return false;
        }
        Set<UUID> uuids = MainUtil.getUUIDsFromString(args[0]);
        if (uuids == null) {
            MainUtil.sendMessage(plr, C.INVALID_PLAYER, args[0]);
            return false;
        }
        Set<PlotPlayer> players = new HashSet<>();
        for (UUID uuid : uuids) {
            if (uuid == DBFunc.everyone) {
                players.addAll(plot.getPlayersInPlot());
                break;
            }
            PlotPlayer pp = UUIDHandler.getPlayer(uuid);
            if (pp != null) {
                players.add(pp);
            }
        }
        players.remove(plr); // Don't ever kick the calling player
        if (players.isEmpty()) {
            MainUtil.sendMessage(plr, C.INVALID_PLAYER, args[0]);
            return false;
        }
        for (PlotPlayer player : players) {
            Location location2 = player.getLocation();
            if (!plr.getLocation().getWorld().equals(location2.getWorld()) || !plot.equals(location2.getPlot())) {
                MainUtil.sendMessage(plr, C.INVALID_PLAYER, args[0]);
                return false;
            }
            if (player.hasPermission("plots.admin.entry.denied")) {
                C.CANNOT_KICK_PLAYER.send(plr, player.getName());
                return false;
            }
            Location spawn = WorldUtil.IMP.getSpawn(location.getWorld());
            C.YOU_GOT_KICKED.send(player);
            if (plot.equals(spawn.getPlot())) {
                Location newSpawn = WorldUtil.IMP.getSpawn(player);
                if (plot.equals(newSpawn.getPlot())) {
                    // Kick from server if you can't be teleported to spawn
                    player.kick(C.YOU_GOT_KICKED.s());
                } else {
                    player.teleport(newSpawn);
                }
            } else {
                player.teleport(spawn);
            }
        }
        return true;
    }
}
