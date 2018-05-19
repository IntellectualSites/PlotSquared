package com.intellectualcrafters.plot.commands;

import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.database.DBFunc;
import com.intellectualcrafters.plot.object.PlotArea;
import com.intellectualcrafters.plot.object.PlotCluster;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.Permissions;
import com.plotsquared.general.commands.Command;
import com.plotsquared.general.commands.CommandDeclaration;

@CommandDeclaration(command = "delete",
        aliases = {"del","disband"},
        category = CommandCategory.ADMINISTRATION,
        requiredType = RequiredType.NONE,
        permission = "plots.cluster.delete",
        usage = "/plot cluster delete [name]",
        description = "Delete a plot cluster")
public class ClusterDelete extends SubCommand {

    public ClusterDelete(Command parent, boolean isStatic) { super(parent, isStatic); }

    @Override
    public boolean onCommand(final PlotPlayer player, String[] args) {
        if (args.length > 1) {
            MainUtil.sendMessage(player, C.COMMAND_SYNTAX, getUsage());
            return false;
        }
        PlotArea area = player.getApplicablePlotArea();
        if (area == null) {
            C.NOT_IN_PLOT_WORLD.send(player);
            return false;
        }
        PlotCluster cluster;
        if (args.length == 1) {
            cluster = area.getCluster(args[0]);
            if (cluster == null) {
                MainUtil.sendMessage(player, C.INVALID_CLUSTER, args[0]);
                return false;
            }
        } else {
            cluster = area.getCluster(player.getLocation());
            if (cluster == null) {
                MainUtil.sendMessage(player, C.NOT_IN_CLUSTER);
                return false;
            }
        }
        if (!cluster.owner.equals(player.getUUID())) {
            if (!Permissions.hasPermission(player, C.PERMISSION_CLUSTER_DELETE_OTHER)) {
                MainUtil.sendMessage(player, C.NO_PERMISSION, C.PERMISSION_CLUSTER_DELETE_OTHER);
                return false;
            }
        }
        DBFunc.delete(cluster);
        MainUtil.sendMessage(player, C.CLUSTER_DELETED);
        return true;
    }
}

