package com.intellectualcrafters.plot.commands;

import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.database.DBFunc;
import com.intellectualcrafters.plot.object.PlotArea;
import com.intellectualcrafters.plot.object.PlotCluster;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.Permissions;
import com.intellectualcrafters.plot.util.UUIDHandler;
import com.plotsquared.general.commands.Command;
import com.plotsquared.general.commands.CommandDeclaration;
import java.util.UUID;

@CommandDeclaration(command = "invite",
        aliases = {"add","inv"},
        category = CommandCategory.ADMINISTRATION,
        requiredType = RequiredType.NONE,
        permission = "plots.cluster.invite",
        usage = "/plot cluster invite <player>",
        description = "Invite player to a plot cluster")
public class ClusterInvite extends SubCommand {

    public ClusterInvite(Command parent, boolean isStatic) { super(parent, isStatic); }

    @Override
    public boolean onCommand(final PlotPlayer player, String[] args) {
        if (args.length != 1) {
            MainUtil.sendMessage(player, C.COMMAND_SYNTAX, getUsage());
            return false;
        }
        // check if in cluster
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
            if (!Permissions.hasPermission(player, C.PERMISSION_CLUSTER_INVITE_OTHER)) {
                MainUtil.sendMessage(player, C.NO_PERMISSION, C.PERMISSION_CLUSTER_INVITE_OTHER);
                return false;
            }
        }
        // check uuid
        UUID uuid = UUIDHandler.getUUID(args[0], null);
        if (uuid == null) {
            MainUtil.sendMessage(player, C.INVALID_PLAYER, args[0]);
            return false;
        }
        if (!cluster.isAdded(uuid)) {
            // add the user if not added
            cluster.invited.add(uuid);
            DBFunc.setInvited(cluster, uuid);
            PlotPlayer player2 = UUIDHandler.getPlayer(uuid);
            if (player2 != null) {
                MainUtil.sendMessage(player2, C.CLUSTER_INVITED, cluster.getName());
            }
        }
        MainUtil.sendMessage(player, C.CLUSTER_ADDED_USER);
        return true;
    }
}
