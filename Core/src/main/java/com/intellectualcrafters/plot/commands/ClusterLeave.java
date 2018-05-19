package com.intellectualcrafters.plot.commands;

import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.database.DBFunc;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotArea;
import com.intellectualcrafters.plot.object.PlotCluster;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.MainUtil;
import com.plotsquared.general.commands.Command;
import com.plotsquared.general.commands.CommandDeclaration;
import java.util.ArrayList;
import java.util.UUID;

@CommandDeclaration(command = "leave",
        aliases = {"quit"},
        category = CommandCategory.ADMINISTRATION,
        requiredType = RequiredType.NONE,
        permission = "plots.cluster.leave",
        usage = "/plot cluster leave [name]",
        description = "Leave a plot cluster")
public class ClusterLeave extends SubCommand {

    public ClusterLeave(Command parent, boolean isStatic) { super(parent, isStatic); }

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
        UUID uuid = player.getUUID();
        if (!cluster.isAdded(uuid)) {
            MainUtil.sendMessage(player, C.CLUSTER_NOT_ADDED);
            return false;
        }
        if (uuid.equals(cluster.owner)) {
            MainUtil.sendMessage(player, C.CLUSTER_CANNOT_LEAVE);
            return false;
        }
        if (cluster.helpers.contains(uuid)) {
            cluster.helpers.remove(uuid);
            DBFunc.removeHelper(cluster, uuid);
        }
        cluster.invited.remove(uuid);
        DBFunc.removeInvited(cluster, uuid);
        MainUtil.sendMessage(player, C.CLUSTER_REMOVED, cluster.getName());
        for (Plot plot : new ArrayList<>(PS.get().getPlots(player.getLocation().getWorld(), uuid))) {
            PlotCluster current = plot.getCluster();
            if (current != null && current.equals(cluster)) {
                player.getLocation().getWorld();
                plot.unclaim();
            }
        }
        return true;
    }
}
