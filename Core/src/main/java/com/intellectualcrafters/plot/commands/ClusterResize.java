package com.intellectualcrafters.plot.commands;

import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.config.Settings;
import com.intellectualcrafters.plot.database.DBFunc;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotArea;
import com.intellectualcrafters.plot.object.PlotCluster;
import com.intellectualcrafters.plot.object.PlotId;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.Permissions;
import com.plotsquared.general.commands.Command;
import com.plotsquared.general.commands.CommandDeclaration;
import java.util.HashSet;

@CommandDeclaration(command = "resize",
        aliases = {"res"},
        category = CommandCategory.ADMINISTRATION,
        requiredType = RequiredType.NONE,
        permission = "plots.cluster.resize",
        usage = "/plot cluster resize <pos1> <pos2>",
        description = "Resize a plot cluster")
public class ClusterResize extends SubCommand {

    public ClusterResize(Command parent, boolean isStatic) { super(parent, isStatic); }

    @Override
    public boolean onCommand(final PlotPlayer player, String[] args) {
        if (args.length != 2) {
            MainUtil.sendMessage(player, C.COMMAND_SYNTAX, getUsage());
            return false;
        }
        // check pos1 / pos2
        PlotId pos1 = PlotId.fromString(args[0]);
        PlotId pos2 = PlotId.fromString(args[1]);
        if (pos1 == null || pos2 == null) {
            MainUtil.sendMessage(player, C.NOT_VALID_PLOT_ID);
            return false;
        }
        if (pos2.x < pos1.x || pos2.y < pos1.y) {
            pos1 = new PlotId(Math.min(pos1.x, pos2.x), Math.min(pos1.y, pos2.y));
            pos2 = new PlotId(Math.max(pos1.x, pos2.x), Math.max(pos1.y, pos2.y));
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
            if (!Permissions.hasPermission(player, C.PERMISSION_CLUSTER_RESIZE_OTHER)) {
                MainUtil.sendMessage(player, C.NO_PERMISSION, C.PERMISSION_CLUSTER_RESIZE_OTHER);
                return false;
            }
        }
        //check if overlap
        PlotCluster intersect = area.getFirstIntersectingCluster(pos1, pos2);
        if (intersect != null) {
            MainUtil.sendMessage(player, C.CLUSTER_INTERSECTION, intersect.getName());
            return false;
        }
        HashSet<Plot> existing = area.getPlotSelectionOwned(cluster.getP1(), cluster.getP2());
        HashSet<Plot> newPlots = area.getPlotSelectionOwned(pos1, pos2);
        HashSet<Plot> removed = (HashSet<Plot>) existing.clone();
        removed.removeAll(newPlots);
        // Check expand / shrink
        if (!removed.isEmpty()) {
            if (!Permissions.hasPermission(player, C.PERMISSION_CLUSTER_RESIZE_SHRINK)) {
                MainUtil.sendMessage(player, C.NO_PERMISSION, C.PERMISSION_CLUSTER_RESIZE_SHRINK);
                return false;
            }
        }
        newPlots.removeAll(existing);
        if (!newPlots.isEmpty()) {
            if (!Permissions.hasPermission(player, C.PERMISSION_CLUSTER_RESIZE_EXPAND)) {
                MainUtil.sendMessage(player, C.NO_PERMISSION, C.PERMISSION_CLUSTER_RESIZE_EXPAND);
                return false;
            }
        }
        // Check allowed cluster size
        int current;
        if (Settings.Limit.GLOBAL) {
            current = player.getPlayerClusterCount();
        } else {
            current = player.getPlayerClusterCount(player.getLocation().getWorld());
        }
        current -= cluster.getArea() + (1 + pos2.x - pos1.x) * (1 + pos2.y - pos1.y);
        int allowed = Permissions.hasPermissionRange(player, C.PERMISSION_CLUSTER, Settings.Limit.MAX_PLOTS);
        if (current + cluster.getArea() > allowed) {
            MainUtil.sendMessage(player, C.NO_PERMISSION, C.PERMISSION_CLUSTER.s() + "." + (current + cluster.getArea()));
            return false;
        }
        // resize cluster
        DBFunc.resizeCluster(cluster, pos1, pos2);
        MainUtil.sendMessage(player, C.CLUSTER_RESIZED);
        return true;
    }
}
