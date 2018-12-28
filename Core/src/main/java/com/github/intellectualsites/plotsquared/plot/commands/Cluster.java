package com.github.intellectualsites.plotsquared.plot.commands;

import com.github.intellectualsites.plotsquared.commands.CommandDeclaration;
import com.github.intellectualsites.plotsquared.plot.PlotSquared;
import com.github.intellectualsites.plotsquared.plot.config.C;
import com.github.intellectualsites.plotsquared.plot.config.Settings;
import com.github.intellectualsites.plotsquared.plot.database.DBFunc;
import com.github.intellectualsites.plotsquared.plot.object.*;
import com.github.intellectualsites.plotsquared.plot.util.MainUtil;
import com.github.intellectualsites.plotsquared.plot.util.Permissions;
import com.github.intellectualsites.plotsquared.plot.util.UUIDHandler;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@CommandDeclaration(command = "cluster", aliases = "clusters",
    category = CommandCategory.ADMINISTRATION, requiredType = RequiredType.NONE,
    permission = "plots.cluster", description = "Manage a plot cluster") public class Cluster
    extends SubCommand {

    @Override public boolean onCommand(PlotPlayer player, String[] args) {

        // list, create, delete, resize, invite, kick, leave, helpers, tp, sethome
        if (args.length == 0) {
            // return arguments
            MainUtil.sendMessage(player, C.CLUSTER_AVAILABLE_ARGS);
            return false;
        }
        String sub = args[0].toLowerCase();
        switch (sub) {
            case "l":
            case "list": {
                if (!Permissions.hasPermission(player, C.PERMISSION_CLUSTER_LIST)) {
                    MainUtil.sendMessage(player, C.NO_PERMISSION, C.PERMISSION_CLUSTER_LIST);
                    return false;
                }
                if (args.length != 1) {
                    MainUtil.sendMessage(player, C.COMMAND_SYNTAX, "/plot cluster list");
                    return false;
                }
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
            case "c":
            case "create": {
                if (!Permissions.hasPermission(player, C.PERMISSION_CLUSTER_CREATE)) {
                    MainUtil.sendMessage(player, C.NO_PERMISSION, C.PERMISSION_CLUSTER_CREATE);
                    return false;
                }
                PlotArea area = player.getApplicablePlotArea();
                if (area == null) {
                    C.NOT_IN_PLOT_WORLD.send(player);
                    return false;
                }
                if (args.length != 4) {
                    MainUtil.sendMessage(player, C.COMMAND_SYNTAX,
                        "/plot cluster create <name> <id-bot> <id-top>");
                    return false;
                }
                int currentClusters = Settings.Limit.GLOBAL ?
                    player.getClusterCount() :
                    player.getPlotCount(player.getLocation().getWorld());
                if (currentClusters >= player.getAllowedPlots()) {
                    return sendMessage(player, C.CANT_CLAIM_MORE_CLUSTERS);
                }
                // check pos1 / pos2
                PlotId pos1 = PlotId.fromString(args[2]);
                PlotId pos2 = PlotId.fromString(args[3]);
                if (pos1 == null || pos2 == null) {
                    MainUtil.sendMessage(player, C.NOT_VALID_PLOT_ID);
                    return false;
                }
                // check if name is taken
                String name = args[1];
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
                                MainUtil.sendMessage(player, C.NO_PERMISSION,
                                    C.PERMISSION_CLUSTER_CREATE_OTHER);
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
                int allowed = Permissions.hasPermissionRange(player, C.PERMISSION_CLUSTER_SIZE,
                    Settings.Limit.MAX_PLOTS);
                if (current + cluster.getArea() > allowed) {
                    MainUtil.sendMessage(player, C.NO_PERMISSION,
                        C.PERMISSION_CLUSTER_SIZE + "." + (current + cluster.getArea()));
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
            case "disband":
            case "del":
            case "delete": {
                if (!Permissions.hasPermission(player, C.PERMISSION_CLUSTER_DELETE)) {
                    MainUtil.sendMessage(player, C.NO_PERMISSION, C.PERMISSION_CLUSTER_DELETE);
                    return false;
                }
                if (args.length != 1 && args.length != 2) {
                    MainUtil.sendMessage(player, C.COMMAND_SYNTAX, "/plot cluster delete [name]");
                    return false;
                }
                PlotArea area = player.getApplicablePlotArea();
                if (area == null) {
                    C.NOT_IN_PLOT_WORLD.send(player);
                    return false;
                }
                PlotCluster cluster;
                if (args.length == 2) {
                    cluster = area.getCluster(args[1]);
                    if (cluster == null) {
                        MainUtil.sendMessage(player, C.INVALID_CLUSTER, args[1]);
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
                        MainUtil.sendMessage(player, C.NO_PERMISSION,
                            C.PERMISSION_CLUSTER_DELETE_OTHER);
                        return false;
                    }
                }
                DBFunc.delete(cluster);
                MainUtil.sendMessage(player, C.CLUSTER_DELETED);
                return true;
            }
            case "res":
            case "resize": {
                if (!Permissions.hasPermission(player, C.PERMISSION_CLUSTER_RESIZE)) {
                    MainUtil.sendMessage(player, C.NO_PERMISSION, C.PERMISSION_CLUSTER_RESIZE);
                    return false;
                }
                if (args.length != 3) {
                    MainUtil.sendMessage(player, C.COMMAND_SYNTAX,
                        "/plot cluster resize <pos1> <pos2>");
                    return false;
                }
                // check pos1 / pos2
                PlotId pos1 = PlotId.fromString(args[1]);
                PlotId pos2 = PlotId.fromString(args[2]);
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
                        MainUtil.sendMessage(player, C.NO_PERMISSION,
                            C.PERMISSION_CLUSTER_RESIZE_OTHER);
                        return false;
                    }
                }
                //check if overlap
                PlotCluster intersect = area.getFirstIntersectingCluster(pos1, pos2);
                if (intersect != null) {
                    MainUtil.sendMessage(player, C.CLUSTER_INTERSECTION, intersect.getName());
                    return false;
                }
                Set<Plot> existing =
                    area.getPlotSelectionOwned(cluster.getP1(), cluster.getP2());
                Set<Plot> newPlots = area.getPlotSelectionOwned(pos1, pos2);
                // Set<Plot> removed = (HashSet<Plot>) existing.clone();
                Set<Plot> removed = new HashSet<>(existing);

                removed.removeAll(newPlots);
                // Check expand / shrink
                if (!removed.isEmpty()) {
                    if (!Permissions.hasPermission(player, C.PERMISSION_CLUSTER_RESIZE_SHRINK)) {
                        MainUtil.sendMessage(player, C.NO_PERMISSION,
                            C.PERMISSION_CLUSTER_RESIZE_SHRINK);
                        return false;
                    }
                }
                newPlots.removeAll(existing);
                if (!newPlots.isEmpty()) {
                    if (!Permissions.hasPermission(player, C.PERMISSION_CLUSTER_RESIZE_EXPAND)) {
                        MainUtil.sendMessage(player, C.NO_PERMISSION,
                            C.PERMISSION_CLUSTER_RESIZE_EXPAND);
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
                int allowed = Permissions
                    .hasPermissionRange(player, C.PERMISSION_CLUSTER, Settings.Limit.MAX_PLOTS);
                if (current + cluster.getArea() > allowed) {
                    MainUtil.sendMessage(player, C.NO_PERMISSION,
                        C.PERMISSION_CLUSTER.s() + "." + (current + cluster.getArea()));
                    return false;
                }
                // resize cluster
                DBFunc.resizeCluster(cluster, pos1, pos2);
                MainUtil.sendMessage(player, C.CLUSTER_RESIZED);
                return true;
            }
            case "add":
            case "inv":
            case "invite": {
                if (!Permissions.hasPermission(player, C.PERMISSION_CLUSTER_INVITE)) {
                    MainUtil.sendMessage(player, C.NO_PERMISSION, C.PERMISSION_CLUSTER_INVITE);
                    return false;
                }
                if (args.length != 2) {
                    MainUtil.sendMessage(player, C.COMMAND_SYNTAX, "/plot cluster invite <player>");
                    return false;
                }
                // check if in cluster
                PlotArea area = player.getApplicablePlotArea();
                if (area == null) {
                    C.NOT_IN_PLOT_WORLD.send(player);
                }
                PlotCluster cluster = area.getCluster(player.getLocation());
                if (cluster == null) {
                    MainUtil.sendMessage(player, C.NOT_IN_CLUSTER);
                    return false;
                }
                if (!cluster.hasHelperRights(player.getUUID())) {
                    if (!Permissions.hasPermission(player, C.PERMISSION_CLUSTER_INVITE_OTHER)) {
                        MainUtil.sendMessage(player, C.NO_PERMISSION,
                            C.PERMISSION_CLUSTER_INVITE_OTHER);
                        return false;
                    }
                }
                // check uuid
                UUID uuid = UUIDHandler.getUUID(args[1], null);
                if (uuid == null) {
                    MainUtil.sendMessage(player, C.INVALID_PLAYER, args[2]);
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
            case "k":
            case "remove":
            case "kick": {
                if (!Permissions.hasPermission(player, C.PERMISSION_CLUSTER_KICK)) {
                    MainUtil.sendMessage(player, C.NO_PERMISSION, C.PERMISSION_CLUSTER_KICK);
                    return false;
                }
                if (args.length != 2) {
                    MainUtil.sendMessage(player, C.COMMAND_SYNTAX, "/plot cluster kick <player>");
                    return false;
                }
                PlotArea area = player.getApplicablePlotArea();
                if (area == null) {
                    C.NOT_IN_PLOT_WORLD.send(player);
                }
                PlotCluster cluster = area.getCluster(player.getLocation());
                if (cluster == null) {
                    MainUtil.sendMessage(player, C.NOT_IN_CLUSTER);
                    return false;
                }
                if (!cluster.hasHelperRights(player.getUUID())) {
                    if (!Permissions.hasPermission(player, C.PERMISSION_CLUSTER_KICK_OTHER)) {
                        MainUtil
                            .sendMessage(player, C.NO_PERMISSION, C.PERMISSION_CLUSTER_KICK_OTHER);
                        return false;
                    }
                }
                // check uuid
                UUID uuid = UUIDHandler.getUUID(args[1], null);
                if (uuid == null) {
                    MainUtil.sendMessage(player, C.INVALID_PLAYER, args[1]);
                    return false;
                }
                // Can't kick if the player is yourself, the owner, or not added to the cluster
                if (uuid.equals(player.getUUID()) || uuid.equals(cluster.owner) || !cluster
                    .isAdded(uuid)) {
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
                for (Plot plot : new ArrayList<>(
                    PlotSquared.get().getPlots(player2.getLocation().getWorld(), uuid))) {
                    PlotCluster current = plot.getCluster();
                    if (current != null && current.equals(cluster)) {
                        plot.unclaim();
                    }
                }
                MainUtil.sendMessage(player2, C.CLUSTER_KICKED_USER);
                return true;
            }
            case "quit":
            case "leave": {
                if (!Permissions.hasPermission(player, C.PERMISSION_CLUSTER_LEAVE)) {
                    MainUtil.sendMessage(player, C.NO_PERMISSION, C.PERMISSION_CLUSTER_LEAVE);
                    return false;
                }
                if (args.length != 1 && args.length != 2) {
                    MainUtil.sendMessage(player, C.COMMAND_SYNTAX, "/plot cluster leave [name]");
                    return false;
                }
                PlotArea area = player.getApplicablePlotArea();
                if (area == null) {
                    C.NOT_IN_PLOT_WORLD.send(player);
                }
                PlotCluster cluster;
                if (args.length == 2) {
                    cluster = area.getCluster(args[1]);
                    if (cluster == null) {
                        MainUtil.sendMessage(player, C.INVALID_CLUSTER, args[1]);
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
                for (Plot plot : new ArrayList<>(
                    PlotSquared.get().getPlots(player.getLocation().getWorld(), uuid))) {
                    PlotCluster current = plot.getCluster();
                    if (current != null && current.equals(cluster)) {
                        player.getLocation().getWorld();
                        plot.unclaim();
                    }
                }
                return true;
            }
            case "members":
            case "admin":
            case "helper":
            case "helpers": {
                if (!Permissions.hasPermission(player, C.PERMISSION_CLUSTER_HELPERS)) {
                    MainUtil.sendMessage(player, C.NO_PERMISSION, C.PERMISSION_CLUSTER_HELPERS);
                    return false;
                }
                if (args.length != 3) {
                    MainUtil.sendMessage(player, C.COMMAND_SYNTAX,
                        "/plot cluster helpers <add|remove> <player>");
                    return false;
                }
                PlotArea area = player.getApplicablePlotArea();
                if (area == null) {
                    C.NOT_IN_PLOT_WORLD.send(player);
                }
                PlotCluster cluster = area.getCluster(player.getLocation());
                if (cluster == null) {
                    MainUtil.sendMessage(player, C.NOT_IN_CLUSTER);
                    return false;
                }
                UUID uuid = UUIDHandler.getUUID(args[2], null);
                if (uuid == null) {
                    MainUtil.sendMessage(player, C.INVALID_PLAYER, args[2]);
                    return false;
                }
                if (args[1].equalsIgnoreCase("add")) {
                    cluster.helpers.add(uuid);
                    DBFunc.setHelper(cluster, uuid);
                    return MainUtil.sendMessage(player, C.CLUSTER_ADDED_HELPER);
                }
                if (args[1].equalsIgnoreCase("remove")) {
                    cluster.helpers.remove(uuid);
                    DBFunc.removeHelper(cluster, uuid);
                    return MainUtil.sendMessage(player, C.CLUSTER_REMOVED_HELPER);
                }
                MainUtil.sendMessage(player, C.COMMAND_SYNTAX,
                    "/plot cluster helpers <add|remove> <player>");
                return false;
            }
            case "spawn":
            case "home":
            case "tp": {
                if (!Permissions.hasPermission(player, C.PERMISSION_CLUSTER_TP)) {
                    MainUtil.sendMessage(player, C.NO_PERMISSION, C.PERMISSION_CLUSTER_TP);
                    return false;
                }
                if (args.length != 2) {
                    MainUtil.sendMessage(player, C.COMMAND_SYNTAX, "/plot cluster tp <name>");
                    return false;
                }
                PlotArea area = player.getApplicablePlotArea();
                if (area == null) {
                    C.NOT_IN_PLOT_WORLD.send(player);
                    return false;
                }
                PlotCluster cluster = area.getCluster(args[1]);
                if (cluster == null) {
                    MainUtil.sendMessage(player, C.INVALID_CLUSTER, args[1]);
                    return false;
                }
                UUID uuid = player.getUUID();
                if (!cluster.isAdded(uuid)) {
                    if (!Permissions.hasPermission(player, C.PERMISSION_CLUSTER_TP_OTHER)) {
                        MainUtil
                            .sendMessage(player, C.NO_PERMISSION, C.PERMISSION_CLUSTER_TP_OTHER);
                        return false;
                    }
                }
                player.teleport(cluster.getHome());
                return MainUtil.sendMessage(player, C.CLUSTER_TELEPORTING);
            }
            case "i":
            case "info":
            case "show":
            case "information": {
                if (!Permissions.hasPermission(player, C.PERMISSION_CLUSTER_INFO)) {
                    MainUtil.sendMessage(player, C.NO_PERMISSION, C.PERMISSION_CLUSTER_INFO);
                    return false;
                }
                if (args.length != 1 && args.length != 2) {
                    MainUtil.sendMessage(player, C.COMMAND_SYNTAX, "/plot cluster info [name]");
                    return false;
                }
                PlotArea area = player.getApplicablePlotArea();
                if (area == null) {
                    C.NOT_IN_PLOT_WORLD.send(player);
                }
                PlotCluster cluster;
                if (args.length == 2) {
                    cluster = area.getCluster(args[1]);
                    if (cluster == null) {
                        MainUtil.sendMessage(player, C.INVALID_CLUSTER, args[1]);
                        return false;
                    }
                } else {
                    cluster = area.getCluster(player.getLocation());
                    if (cluster == null) {
                        MainUtil.sendMessage(player, C.NOT_IN_CLUSTER);
                        return false;
                    }
                }
                String id = cluster.toString();
                String owner = UUIDHandler.getName(cluster.owner);
                if (owner == null) {
                    owner = "unknown";
                }
                String name = cluster.getName();
                String size = (cluster.getP2().x - cluster.getP1().x + 1) + "x" + (
                    cluster.getP2().y - cluster.getP1().y + 1);
                String rights = cluster.isAdded(player.getUUID()) + "";
                String message = C.CLUSTER_INFO.s();
                message = message.replaceAll("%id%", id);
                message = message.replaceAll("%owner%", owner);
                message = message.replaceAll("%name%", name);
                message = message.replaceAll("%size%", size);
                message = message.replaceAll("%rights%", rights);
                MainUtil.sendMessage(player, message);
                return true;
            }
            case "sh":
            case "setspawn":
            case "sethome":
                if (!Permissions.hasPermission(player, C.PERMISSION_CLUSTER_SETHOME)) {
                    MainUtil.sendMessage(player, C.NO_PERMISSION, C.PERMISSION_CLUSTER_SETHOME);
                    return false;
                }
                if (args.length != 1 && args.length != 2) {
                    MainUtil.sendMessage(player, C.COMMAND_SYNTAX, "/plot cluster sethome");
                    return false;
                }
                PlotArea area = player.getApplicablePlotArea();
                if (area == null) {
                    C.NOT_IN_PLOT_WORLD.send(player);
                }
                PlotCluster cluster = area.getCluster(player.getLocation());
                if (cluster == null) {
                    MainUtil.sendMessage(player, C.NOT_IN_CLUSTER);
                    return false;
                }
                if (!cluster.hasHelperRights(player.getUUID())) {
                    if (!Permissions.hasPermission(player, C.PERMISSION_CLUSTER_SETHOME_OTHER)) {
                        MainUtil.sendMessage(player, C.NO_PERMISSION,
                            C.PERMISSION_CLUSTER_SETHOME_OTHER);
                        return false;
                    }
                }
                Location base = cluster.getClusterBottom();
                Location relative = player.getLocation().subtract(base.getX(), 0, base.getZ());
                BlockLoc blockloc = new BlockLoc(relative.getX(), relative.getY(), relative.getZ());
                cluster.settings.setPosition(blockloc);
                DBFunc.setPosition(cluster,
                    relative.getX() + "," + relative.getY() + "," + relative.getZ());
                return MainUtil.sendMessage(player, C.POSITION_SET);
        }
        MainUtil.sendMessage(player, C.CLUSTER_AVAILABLE_ARGS);
        return false;
    }
}
