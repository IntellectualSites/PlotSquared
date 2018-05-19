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
import java.util.Set;
import java.util.UUID;

@CommandDeclaration(command = "create",
        permission = "plots.cluster.create",
        category = CommandCategory.ADMINISTRATION,
        requiredType = RequiredType.NONE,
        description = "Create a new plot cluster",
        aliases = {"c"},
        usage = "/plot cluster create <name> <id-bot> <id-top>",
        confirmation = true)
public class ClusterCreate extends SubCommand {

    public ClusterCreate(Command parent, boolean isStatic) { super(parent, isStatic); }

    @Override
    public boolean onCommand(final PlotPlayer player, String[] args) {
        if (args.length != 3) {
            C.COMMAND_SYNTAX.send(player, getUsage());
            return false;
        }

        PlotArea area = player.getApplicablePlotArea();
        if (area == null) {
            C.NOT_IN_PLOT_WORLD.send(player);
            return false;
        }
        int currentClusters = Settings.Limit.GLOBAL ? player.getClusterCount() : player.getPlotCount(player.getLocation().getWorld());
        if (currentClusters >= player.getAllowedPlots()) {
            return sendMessage(player, C.CANT_CLAIM_MORE_CLUSTERS);
        }
        // check pos1 / pos2
        PlotId pos1 = PlotId.fromString(args[1]);
        PlotId pos2 = PlotId.fromString(args[2]);
        if (pos1 == null || pos2 == null) {
            MainUtil.sendMessage(player, C.NOT_VALID_PLOT_ID);
            return false;
        }
        // check if name is taken
        String name = args[0];
        if (area.getCluster(name) != null) {
            MainUtil.sendMessage(player, C.ALIAS_IS_TAKEN);
            return false;
        }
        if (pos2.x < pos1.x || pos2.y < pos1.y) {
            PlotId tmp = new PlotId(Math.min(pos1.x, pos2.x), Math.min(pos1.y, pos2.y));
            pos2 = new PlotId(Math.max(pos1.x, pos2.x), Math.max(pos1.y, pos2.y));
            pos1 = tmp;
        }
        //check if overlap
        PlotCluster cluster = area.getFirstIntersectingCluster(pos1, pos2);
        if (cluster != null) {
            MainUtil.sendMessage(player, C.CLUSTER_INTERSECTION, cluster.getName());
            return false;
        }
        // Check if it occupies existing plots
        if (!area.contains(pos1) || !area.contains(pos2)) {
            C.CLUSTER_OUTSIDE.send(player, area);
            return false;
        }
        Set<Plot> plots = area.getPlotSelectionOwned(pos1, pos2);
        if (!plots.isEmpty()) {
            if (!Permissions.hasPermission(player, C.PERMISSION_CLUSTER_CREATE_OTHER)) {
                UUID uuid = player.getUUID();
                for (Plot plot : plots) {
                    if (!plot.isOwner(uuid)) {
                        MainUtil.sendMessage(player, C.NO_PERMISSION, C.PERMISSION_CLUSTER_CREATE_OTHER);
                        return false;
                    }
                }
            }
        }
        // Check allowed cluster size
        cluster = new PlotCluster(area, pos1, pos2, player.getUUID());
        int current;
        if (Settings.Limit.GLOBAL) {
            current = player.getPlayerClusterCount();
        } else {
            current = player.getPlayerClusterCount(player.getLocation().getWorld());
        }
        int allowed = Permissions.hasPermissionRange(player, C.PERMISSION_CLUSTER_SIZE, Settings.Limit.MAX_PLOTS);
        if (current + cluster.getArea() > allowed) {
            MainUtil.sendMessage(player, C.NO_PERMISSION, C.PERMISSION_CLUSTER_SIZE + "." + (current + cluster.getArea()));
            return false;
        }
        // create cluster
        cluster.settings.setAlias(name);
        area.addCluster(cluster);
        DBFunc.createCluster(cluster);
        // Add any existing plots to the current cluster
        for (Plot plot : plots) {
            if (plot.hasOwner()) {
                if (!cluster.isAdded(plot.owner)) {
                    cluster.invited.add(plot.owner);
                    DBFunc.setInvited(cluster, plot.owner);
                }
            }
        }
        MainUtil.sendMessage(player, C.CLUSTER_ADDED);
        return true;
    }
}
