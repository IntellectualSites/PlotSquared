package com.intellectualcrafters.plot.commands;

import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.object.PlotArea;
import com.intellectualcrafters.plot.object.PlotCluster;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.MainUtil;
import com.plotsquared.general.commands.Command;
import com.plotsquared.general.commands.CommandDeclaration;
import java.util.Set;

@CommandDeclaration(command = "list",
        aliases = {"l"},
        category = CommandCategory.ADMINISTRATION,
        requiredType = RequiredType.NONE,
        permission = "plots.cluster.list",
        usage = "/plot cluster list",
        description = "List all plot clusters")
public class ClusterList extends SubCommand {

    public ClusterList(Command parent, boolean isStatic) { super(parent, isStatic); }

    @Override
    public boolean onCommand(final PlotPlayer player, String[] args) {
        PlotArea area = player.getApplicablePlotArea();
        if (area == null) {
            C.NOT_IN_PLOT_WORLD.send(player);
            return false;
        }
        Set<PlotCluster> clusters = area.getClusters();
        MainUtil.sendMessage(player, C.CLUSTER_LIST_HEADING, clusters.size() + "");
        for (PlotCluster cluster : clusters) {
            // Ignore unmanaged clusters
            String name = "'" + cluster.getName() + "' : " + cluster.toString();
            if (player.getUUID().equals(cluster.owner)) {
                MainUtil.sendMessage(player, C.CLUSTER_LIST_ELEMENT, "&a" + name);
            } else if (cluster.helpers.contains(player.getUUID())) {
                MainUtil.sendMessage(player, C.CLUSTER_LIST_ELEMENT, "&3" + name);
            } else if (cluster.invited.contains(player.getUUID())) {
                MainUtil.sendMessage(player, C.CLUSTER_LIST_ELEMENT, "&9" + name);
            } else {
                MainUtil.sendMessage(player, C.CLUSTER_LIST_ELEMENT, cluster.toString());
            }
        }
        return true;
    }
}
