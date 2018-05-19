package com.intellectualcrafters.plot.commands;

import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.database.DBFunc;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotArea;
import com.intellectualcrafters.plot.object.PlotCluster;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.Permissions;
import com.intellectualcrafters.plot.util.UUIDHandler;
import com.plotsquared.general.commands.Command;
import com.plotsquared.general.commands.CommandDeclaration;
import java.util.ArrayList;
import java.util.UUID;

@CommandDeclaration(command = "kick",
        aliases = {"k","remove"},
        category = CommandCategory.ADMINISTRATION,
        requiredType = RequiredType.NONE,
        permission = "plots.cluster.kick",
        usage = "/plot cluster kick <player>",
        description = "Kick player from plot cluster")
public class ClusterKick extends SubCommand {

    public ClusterKick(Command parent, boolean isStatic) { super(parent, isStatic); }

    @Override
    public boolean onCommand(final PlotPlayer player, String[] args) {
        if (args.length != 1) {
            MainUtil.sendMessage(player, C.COMMAND_SYNTAX, getUsage());
            return false;
        }
        PlotArea area = player.getApplicablePlotArea();
        if (area == null) {
            C.NOT_IN_PLOT_WORLD.send(player);
            return false;
        }
        PlotCluster cluster = area.getCluster(player.getLocation());
        if (cluster == null) {
            MainUtil.sendMessage(player, C.NOT_IN_CLUSTER);
            return false;
        }
        if (!cluster.hasHelperRights(player.getUUID())) {
            if (!Permissions.hasPermission(player, C.PERMISSION_CLUSTER_KICK_OTHER)) {
                MainUtil.sendMessage(player, C.NO_PERMISSION, C.PERMISSION_CLUSTER_KICK_OTHER);
                return false;
            }
        }
        // check uuid
        UUID uuid = UUIDHandler.getUUID(args[0], null);
        if (uuid == null) {
            MainUtil.sendMessage(player, C.INVALID_PLAYER, args[0]);
            return false;
        }
        // Can't kick if the player is yourself, the owner, or not added to the cluster
        if (uuid.equals(player.getUUID()) || uuid.equals(cluster.owner) || !cluster.isAdded(uuid)) {
            MainUtil.sendMessage(player, C.CANNOT_KICK_PLAYER, cluster.getName());
            return false;
        }
        if (cluster.helpers.contains(uuid)) {
            cluster.helpers.remove(uuid);
            DBFunc.removeHelper(cluster, uuid);
        }
        cluster.invited.remove(uuid);
        DBFunc.removeInvited(cluster, uuid);
        PlotPlayer player2 = UUIDHandler.getPlayer(uuid);
        if (player2 != null) {
            MainUtil.sendMessage(player2, C.CLUSTER_REMOVED, cluster.getName());
        }
        for (Plot plot : new ArrayList<>(PS.get().getPlots(player2.getLocation().getWorld(), uuid))) {
            PlotCluster current = plot.getCluster();
            if (current != null && current.equals(cluster)) {
                plot.unclaim();
            }
        }
        MainUtil.sendMessage(player2, C.CLUSTER_KICKED_USER);
        return true;
    }
}