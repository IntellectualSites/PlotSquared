package com.github.intellectualsites.plotsquared.plot.commands;

import com.github.intellectualsites.plotsquared.commands.Argument;
import com.github.intellectualsites.plotsquared.commands.CommandDeclaration;
import com.github.intellectualsites.plotsquared.plot.PlotSquared;
import com.github.intellectualsites.plotsquared.plot.config.Captions;
import com.github.intellectualsites.plotsquared.plot.database.DBFunc;
import com.github.intellectualsites.plotsquared.plot.object.Location;
import com.github.intellectualsites.plotsquared.plot.object.Plot;
import com.github.intellectualsites.plotsquared.plot.object.PlotPlayer;
import com.github.intellectualsites.plotsquared.plot.util.EventUtil;
import com.github.intellectualsites.plotsquared.plot.util.MainUtil;
import com.github.intellectualsites.plotsquared.plot.util.Permissions;
import com.github.intellectualsites.plotsquared.plot.util.UUIDHandler;
import com.github.intellectualsites.plotsquared.plot.util.WorldUtil;
import com.sk89q.worldedit.world.gamemode.GameModes;

import java.util.Set;
import java.util.UUID;

@CommandDeclaration(command = "deny", aliases = {"d", "ban"},
    description = "Deny a user from entering a plot", usage = "/plot deny <player|*>",
    category = CommandCategory.SETTINGS, requiredType = RequiredType.PLAYER) public class Deny
    extends SubCommand {

    public Deny() {
        super(Argument.PlayerName);
    }

    @Override public boolean onCommand(PlotPlayer player, String[] args) {

        Location location = player.getLocation();
        Plot plot = location.getPlotAbs();
        if (plot == null) {
            return !sendMessage(player, Captions.NOT_IN_PLOT);
        }
        if (!plot.hasOwner()) {
            MainUtil.sendMessage(player, Captions.PLOT_UNOWNED);
            return false;
        }
        if (!plot.isOwner(player.getUUID()) && !Permissions
            .hasPermission(player, Captions.PERMISSION_ADMIN_COMMAND_DENY)) {
            MainUtil.sendMessage(player, Captions.NO_PLOT_PERMS);
            return true;
        }
        Set<UUID> uuids = MainUtil.getUUIDsFromString(args[0]);
        if (uuids.isEmpty()) {
            MainUtil.sendMessage(player, Captions.INVALID_PLAYER, args[0]);
            return false;
        }
        for (UUID uuid : uuids) {
            if (uuid == DBFunc.EVERYONE && !(
                Permissions.hasPermission(player, Captions.PERMISSION_DENY_EVERYONE) || Permissions
                    .hasPermission(player, Captions.PERMISSION_ADMIN_COMMAND_DENY))) {
                MainUtil.sendMessage(player, Captions.INVALID_PLAYER, MainUtil.getName(uuid));
                continue;
            }
            if (plot.isOwner(uuid)) {
                MainUtil.sendMessage(player, Captions.CANT_REMOVE_OWNER, MainUtil.getName(uuid));
                return false;
            }

            if (plot.getDenied().contains(uuid)) {
                MainUtil.sendMessage(player, Captions.ALREADY_ADDED, MainUtil.getName(uuid));
                return false;
            }
            if (uuid != DBFunc.EVERYONE) {
                plot.removeMember(uuid);
                plot.removeTrusted(uuid);
            }
            plot.addDenied(uuid);
            EventUtil.manager.callDenied(player, plot, uuid, true);
            if (!uuid.equals(DBFunc.EVERYONE)) {
                handleKick(UUIDHandler.getPlayer(uuid), plot);
            } else {
                for (PlotPlayer plotPlayer : plot.getPlayersInPlot()) {
                    // Ignore plot-owners
                    if (plot.isOwner(plotPlayer.getUUID())) {
                        continue;
                    }
                    handleKick(plotPlayer, plot);
                }
            }
        }
        if (!uuids.isEmpty()) {
            MainUtil.sendMessage(player, Captions.DENIED_ADDED);
        }
        return true;
    }

    private void handleKick(PlotPlayer player, Plot plot) {
        if (player == null) {
            return;
        }
        if (!plot.equals(player.getCurrentPlot())) {
            return;
        }
        if (player.hasPermission("plots.admin.entry.denied")) {
            return;
        }
        if (player.getGameMode() == GameModes.SPECTATOR) {
            player.stopSpectating();
        }
        Location location = player.getLocation();
        Location spawn = WorldUtil.IMP.getSpawn(location.getWorld());
        MainUtil.sendMessage(player, Captions.YOU_GOT_DENIED);
        if (plot.equals(spawn.getPlot())) {
            Location newSpawn =
                WorldUtil.IMP.getSpawn(PlotSquared.get().getPlotAreaManager().getAllWorlds()[0]);
            if (plot.equals(newSpawn.getPlot())) {
                // Kick from server if you can't be teleported to spawn
                player.kick(Captions.YOU_GOT_DENIED.getTranslated());
            } else {
                player.teleport(newSpawn);
            }
        } else {
            player.teleport(spawn);
        }
    }
}
