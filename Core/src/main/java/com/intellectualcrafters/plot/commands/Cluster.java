package com.intellectualcrafters.plot.commands;

import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.config.Settings;
import com.intellectualcrafters.plot.database.DBFunc;
import com.intellectualcrafters.plot.object.BlockLoc;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotArea;
import com.intellectualcrafters.plot.object.PlotCluster;
import com.intellectualcrafters.plot.object.PlotId;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.Permissions;
import com.intellectualcrafters.plot.util.UUIDHandler;
import com.plotsquared.general.commands.CommandDeclaration;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@CommandDeclaration(command = "cluster",
        aliases = "clusters",
        category = CommandCategory.ADMINISTRATION,
        requiredType = RequiredType.NONE,
        permission = "plots.cluster",
        description = "Manage a plot cluster")
public class Cluster extends SubCommand {

    @Override
    public boolean onCommand(PlotPlayer plr, String[] args) {

        // list, create, delete, resize, invite, kick, leave, helpers, tp, sethome
        if (args.length == 0) {
            // return arguments
            MainUtil.sendMessage(plr, C.CLUSTER_AVAILABLE_ARGS);
            return false;
        }
        String sub = args[0].toLowerCase();
        switch (sub) {
            case "l":
            case "list": {
                if (!Permissions.hasPermission(plr, "plots.cluster.list")) {
                    MainUtil.sendMessage(plr, C.NO_PERMISSION, "plots.cluster.list");
                    return false;
                }
                if (args.length != 1) {
                    MainUtil.sendMessage(plr, C.COMMAND_SYNTAX, "/plot cluster list");
                    return false;
                }
                PlotArea area = plr.getApplicablePlotArea();
                if (area == null) {
                    C.NOT_IN_PLOT_WORLD.send(plr);
                    return false;
                }
                Set<PlotCluster> clusters = area.getClusters();
                MainUtil.sendMessage(plr, C.CLUSTER_LIST_HEADING, clusters.size() + "");
                for (PlotCluster cluster : clusters) {
                    // Ignore unmanaged clusters
                    String name = "'" + cluster.getName() + "' : " + cluster.toString();
                    if (plr.getUUID().equals(cluster.owner)) {
                        MainUtil.sendMessage(plr, C.CLUSTER_LIST_ELEMENT, "&a" + name);
                    } else if (cluster.helpers.contains(plr.getUUID())) {
                        MainUtil.sendMessage(plr, C.CLUSTER_LIST_ELEMENT, "&3" + name);
                    } else if (cluster.invited.contains(plr.getUUID())) {
                        MainUtil.sendMessage(plr, C.CLUSTER_LIST_ELEMENT, "&9" + name);
                    } else {
                        MainUtil.sendMessage(plr, C.CLUSTER_LIST_ELEMENT, cluster.toString());
                    }
                }
                return true;
            }
            case "c":
            case "create": {
                if (!Permissions.hasPermission(plr, "plots.cluster.create")) {
                    MainUtil.sendMessage(plr, C.NO_PERMISSION, "plots.cluster.create");
                    return false;
                }
                PlotArea area = plr.getApplicablePlotArea();
                if (area == null) {
                    C.NOT_IN_PLOT_WORLD.send(plr);
                    return false;
                }
                if (args.length != 4) {
                    MainUtil.sendMessage(plr, C.COMMAND_SYNTAX, "/plot cluster create <name> <id-bot> <id-top>");
                    return false;
                }
                // check pos1 / pos2
                PlotId pos1 = PlotId.fromString(args[2]);
                PlotId pos2 = PlotId.fromString(args[3]);
                if (pos1 == null || pos2 == null) {
                    MainUtil.sendMessage(plr, C.NOT_VALID_PLOT_ID);
                    return false;
                }
                // check if name is taken
                String name = args[1];
                if (area.getCluster(name) != null) {
                    MainUtil.sendMessage(plr, C.ALIAS_IS_TAKEN);
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
                    MainUtil.sendMessage(plr, C.CLUSTER_INTERSECTION, cluster.getName());
                    return false;
                }
                // Check if it occupies existing plots
                Set<Plot> plots = area.getPlotSelectionOwned(pos1, pos2);
                if (!plots.isEmpty()) {
                    if (!Permissions.hasPermission(plr, "plots.cluster.create.other")) {
                        UUID uuid = plr.getUUID();
                        for (Plot plot : plots) {
                            if (!plot.isOwner(uuid)) {
                                MainUtil.sendMessage(plr, C.NO_PERMISSION, "plots.cluster.create.other");
                                return false;
                            }
                        }
                    }
                }
                // Check allowed cluster size
                cluster = new PlotCluster(area, pos1, pos2, plr.getUUID());
                int current;
                if (Settings.GLOBAL_LIMIT) {
                    current = plr.getPlayerClusterCount();
                } else {
                    current = plr.getPlayerClusterCount(plr.getLocation().getWorld());
                }
                int allowed = Permissions.hasPermissionRange(plr, "plots.cluster", Settings.MAX_PLOTS);
                if (current + cluster.getArea() > allowed) {
                    MainUtil.sendMessage(plr, C.NO_PERMISSION, "plots.cluster." + (current + cluster.getArea()));
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
                MainUtil.sendMessage(plr, C.CLUSTER_ADDED);
                return true;
            }
            case "disband":
            case "del":
            case "delete": {
                if (!Permissions.hasPermission(plr, "plots.cluster.delete")) {
                    MainUtil.sendMessage(plr, C.NO_PERMISSION, "plots.cluster.delete");
                    return false;
                }
                if (args.length != 1 && args.length != 2) {
                    MainUtil.sendMessage(plr, C.COMMAND_SYNTAX, "/plot cluster delete [name]");
                    return false;
                }
                PlotArea area = plr.getApplicablePlotArea();
                if (area == null) {
                    C.NOT_IN_PLOT_WORLD.send(plr);
                    return false;
                }
                PlotCluster cluster;
                if (args.length == 2) {
                    cluster = area.getCluster(args[1]);
                    if (cluster == null) {
                        MainUtil.sendMessage(plr, C.INVALID_CLUSTER, args[1]);
                        return false;
                    }
                } else {
                    cluster = area.getCluster(plr.getLocation());
                    if (cluster == null) {
                        MainUtil.sendMessage(plr, C.NOT_IN_CLUSTER);
                        return false;
                    }
                }
                if (!cluster.owner.equals(plr.getUUID())) {
                    if (!Permissions.hasPermission(plr, "plots.cluster.delete.other")) {
                        MainUtil.sendMessage(plr, C.NO_PERMISSION, "plots.cluster.delete.other");
                        return false;
                    }
                }
                DBFunc.delete(cluster);
                MainUtil.sendMessage(plr, C.CLUSTER_DELETED);
                return true;
            }
            case "res":
            case "resize": {
                if (!Permissions.hasPermission(plr, "plots.cluster.resize")) {
                    MainUtil.sendMessage(plr, C.NO_PERMISSION, "plots.cluster.resize");
                    return false;
                }
                if (args.length != 3) {
                    MainUtil.sendMessage(plr, C.COMMAND_SYNTAX, "/plot cluster resize <pos1> <pos2>");
                    return false;
                }
                // check pos1 / pos2
                PlotId pos1 = PlotId.fromString(args[1]);
                PlotId pos2 = PlotId.fromString(args[2]);
                if (pos1 == null || pos2 == null) {
                    MainUtil.sendMessage(plr, C.NOT_VALID_PLOT_ID);
                    return false;
                }
                if (pos2.x < pos1.x || pos2.y < pos1.y) {
                    pos1 = new PlotId(Math.min(pos1.x, pos2.x), Math.min(pos1.y, pos2.y));
                    pos2 = new PlotId(Math.max(pos1.x, pos2.x), Math.max(pos1.y, pos2.y));
                }
                // check if in cluster
                PlotArea area = plr.getApplicablePlotArea();
                if (area == null) {
                    C.NOT_IN_PLOT_WORLD.send(plr);
                    return false;
                }
                PlotCluster cluster = area.getCluster(plr.getLocation());
                if (cluster == null) {
                    MainUtil.sendMessage(plr, C.NOT_IN_CLUSTER);
                    return false;
                }
                if (!cluster.hasHelperRights(plr.getUUID())) {
                    if (!Permissions.hasPermission(plr, "plots.cluster.resize.other")) {
                        MainUtil.sendMessage(plr, C.NO_PERMISSION, "plots.cluster.resize.other");
                        return false;
                    }
                }
                //check if overlap
                PlotCluster intersect = area.getFirstIntersectingCluster(pos1, pos2);
                if (intersect != null) {
                    MainUtil.sendMessage(plr, C.CLUSTER_INTERSECTION, intersect.getName());
                    return false;
                }
                HashSet<Plot> existing = area.getPlotSelectionOwned(cluster.getP1(), cluster.getP2());
                HashSet<Plot> newPlots = area.getPlotSelectionOwned(pos1, pos2);
                HashSet<Plot> removed = (HashSet<Plot>) existing.clone();
                removed.removeAll(newPlots);
                // Check expand / shrink
                if (!removed.isEmpty()) {
                    if (!Permissions.hasPermission(plr, "plots.cluster.resize.shrink")) {
                        MainUtil.sendMessage(plr, C.NO_PERMISSION, "plots.cluster.resize.shrink");
                        return false;
                    }
                }
                newPlots.removeAll(existing);
                if (!newPlots.isEmpty()) {
                    if (!Permissions.hasPermission(plr, "plots.cluster.resize.expand")) {
                        MainUtil.sendMessage(plr, C.NO_PERMISSION, "plots.cluster.resize.expand");
                        return false;
                    }
                }
                // Check allowed cluster size
                int current;
                if (Settings.GLOBAL_LIMIT) {
                    current = plr.getPlayerClusterCount();
                } else {
                    current = plr.getPlayerClusterCount(plr.getLocation().getWorld());
                }
                current -= cluster.getArea() + (1 + pos2.x - pos1.x) * (1 + pos2.y - pos1.y);
                int allowed = Permissions.hasPermissionRange(plr, "plots.cluster", Settings.MAX_PLOTS);
                if (current + cluster.getArea() > allowed) {
                    MainUtil.sendMessage(plr, C.NO_PERMISSION, "plots.cluster." + (current + cluster.getArea()));
                    return false;
                }
                // resize cluster
                DBFunc.resizeCluster(cluster, pos1, pos2);
                MainUtil.sendMessage(plr, C.CLUSTER_RESIZED);
                return true;
            }
            case "add":
            case "inv":
            case "invite": {
                if (!Permissions.hasPermission(plr, "plots.cluster.invite")) {
                    MainUtil.sendMessage(plr, C.NO_PERMISSION, "plots.cluster.invite");
                    return false;
                }
                if (args.length != 2) {
                    MainUtil.sendMessage(plr, C.COMMAND_SYNTAX, "/plot cluster invite <player>");
                    return false;
                }
                // check if in cluster
                PlotArea area = plr.getApplicablePlotArea();
                if (area == null) {
                    C.NOT_IN_PLOT_WORLD.send(plr);
                }
                PlotCluster cluster = area.getCluster(plr.getLocation());
                if (cluster == null) {
                    MainUtil.sendMessage(plr, C.NOT_IN_CLUSTER);
                    return false;
                }
                if (!cluster.hasHelperRights(plr.getUUID())) {
                    if (!Permissions.hasPermission(plr, "plots.cluster.invite.other")) {
                        MainUtil.sendMessage(plr, C.NO_PERMISSION, "plots.cluster.invite.other");
                        return false;
                    }
                }
                // check uuid
                UUID uuid = UUIDHandler.getUUID(args[1], null);
                if (uuid == null) {
                    MainUtil.sendMessage(plr, C.INVALID_PLAYER, args[2]);
                    return false;
                }
                if (!cluster.isAdded(uuid)) {
                    // add the user if not added
                    cluster.invited.add(uuid);
                    DBFunc.setInvited(cluster, uuid);
                    PlotPlayer player = UUIDHandler.getPlayer(uuid);
                    if (player != null) {
                        MainUtil.sendMessage(player, C.CLUSTER_INVITED, cluster.getName());
                    }
                }
                MainUtil.sendMessage(plr, C.CLUSTER_ADDED_USER);
                return true;
            }
            case "k":
            case "remove":
            case "kick": {
                if (!Permissions.hasPermission(plr, "plots.cluster.kick")) {
                    MainUtil.sendMessage(plr, C.NO_PERMISSION, "plots.cluster.kick");
                    return false;
                }
                if (args.length != 2) {
                    MainUtil.sendMessage(plr, C.COMMAND_SYNTAX, "/plot cluster kick <player>");
                    return false;
                }
                PlotArea area = plr.getApplicablePlotArea();
                if (area == null) {
                    C.NOT_IN_PLOT_WORLD.send(plr);
                }
                PlotCluster cluster = area.getCluster(plr.getLocation());
                if (cluster == null) {
                    MainUtil.sendMessage(plr, C.NOT_IN_CLUSTER);
                    return false;
                }
                if (!cluster.hasHelperRights(plr.getUUID())) {
                    if (!Permissions.hasPermission(plr, "plots.cluster.kick.other")) {
                        MainUtil.sendMessage(plr, C.NO_PERMISSION, "plots.cluster.kick.other");
                        return false;
                    }
                }
                // check uuid
                UUID uuid = UUIDHandler.getUUID(args[1], null);
                if (uuid == null) {
                    MainUtil.sendMessage(plr, C.INVALID_PLAYER, args[1]);
                    return false;
                }
                // Can't kick if the player is yourself, the owner, or not added to the cluster
                if (uuid.equals(plr.getUUID()) || uuid.equals(cluster.owner) || !cluster.isAdded(uuid)) {
                    MainUtil.sendMessage(plr, C.CANNOT_KICK_PLAYER, cluster.getName());
                    return false;
                }
                if (cluster.helpers.contains(uuid)) {
                    cluster.helpers.remove(uuid);
                    DBFunc.removeHelper(cluster, uuid);
                }
                cluster.invited.remove(uuid);
                DBFunc.removeInvited(cluster, uuid);
                PlotPlayer player = UUIDHandler.getPlayer(uuid);
                if (player != null) {
                    MainUtil.sendMessage(player, C.CLUSTER_REMOVED, cluster.getName());
                }
                for (Plot plot : new ArrayList<>(PS.get().getPlots(plr.getLocation().getWorld(), uuid))) {
                    PlotCluster current = plot.getCluster();
                    if (current != null && current.equals(cluster)) {
                        plot.unclaim();
                    }
                }
                MainUtil.sendMessage(plr, C.CLUSTER_KICKED_USER);
                return true;
            }
            case "quit":
            case "leave": {
                if (!Permissions.hasPermission(plr, "plots.cluster.leave")) {
                    MainUtil.sendMessage(plr, C.NO_PERMISSION, "plots.cluster.leave");
                    return false;
                }
                if (args.length != 1 && args.length != 2) {
                    MainUtil.sendMessage(plr, C.COMMAND_SYNTAX, "/plot cluster leave [name]");
                    return false;
                }
                PlotArea area = plr.getApplicablePlotArea();
                if (area == null) {
                    C.NOT_IN_PLOT_WORLD.send(plr);
                }
                PlotCluster cluster;
                if (args.length == 2) {
                    cluster = area.getCluster(args[1]);
                    if (cluster == null) {
                        MainUtil.sendMessage(plr, C.INVALID_CLUSTER, args[1]);
                        return false;
                    }
                } else {
                    cluster = area.getCluster(plr.getLocation());
                    if (cluster == null) {
                        MainUtil.sendMessage(plr, C.NOT_IN_CLUSTER);
                        return false;
                    }
                }
                UUID uuid = plr.getUUID();
                if (!cluster.isAdded(uuid)) {
                    MainUtil.sendMessage(plr, C.CLUSTER_NOT_ADDED);
                    return false;
                }
                if (uuid.equals(cluster.owner)) {
                    MainUtil.sendMessage(plr, C.CLUSTER_CANNOT_LEAVE);
                    return false;
                }
                if (cluster.helpers.contains(uuid)) {
                    cluster.helpers.remove(uuid);
                    DBFunc.removeHelper(cluster, uuid);
                }
                cluster.invited.remove(uuid);
                DBFunc.removeInvited(cluster, uuid);
                MainUtil.sendMessage(plr, C.CLUSTER_REMOVED, cluster.getName());
                for (Plot plot : new ArrayList<>(PS.get().getPlots(plr.getLocation().getWorld(), uuid))) {
                    PlotCluster current = plot.getCluster();
                    if (current != null && current.equals(cluster)) {
                        plr.getLocation().getWorld();
                        plot.unclaim();
                    }
                }
                return true;
            }
            case "admin":
            case "helper":
            case "helpers": {
                if (!Permissions.hasPermission(plr, "plots.cluster.helpers")) {
                    MainUtil.sendMessage(plr, C.NO_PERMISSION, "plots.cluster.helpers");
                    return false;
                }
                if (args.length != 3) {
                    MainUtil.sendMessage(plr, C.COMMAND_SYNTAX, "/plot cluster helpers <add|remove> <player>");
                    return false;
                }
                PlotArea area = plr.getApplicablePlotArea();
                if (area == null) {
                    C.NOT_IN_PLOT_WORLD.send(plr);
                }
                PlotCluster cluster = area.getCluster(plr.getLocation());
                if (cluster == null) {
                    MainUtil.sendMessage(plr, C.NOT_IN_CLUSTER);
                    return false;
                }
                UUID uuid = UUIDHandler.getUUID(args[2], null);
                if (uuid == null) {
                    MainUtil.sendMessage(plr, C.INVALID_PLAYER, args[2]);
                    return false;
                }
                if (args[1].equalsIgnoreCase("add")) {
                    cluster.helpers.add(uuid);
                    DBFunc.setHelper(cluster, uuid);
                    return MainUtil.sendMessage(plr, C.CLUSTER_ADDED_HELPER);
                }
                if (args[1].equalsIgnoreCase("remove")) {
                    cluster.helpers.remove(uuid);
                    DBFunc.removeHelper(cluster, uuid);
                    return MainUtil.sendMessage(plr, C.CLUSTER_REMOVED_HELPER);
                }
                MainUtil.sendMessage(plr, C.COMMAND_SYNTAX, "/plot cluster helpers <add|remove> <player>");
                return false;
            }
            case "spawn":
            case "home":
            case "tp": {
                if (!Permissions.hasPermission(plr, "plots.cluster.tp")) {
                    MainUtil.sendMessage(plr, C.NO_PERMISSION, "plots.cluster.tp");
                    return false;
                }
                if (args.length != 2) {
                    MainUtil.sendMessage(plr, C.COMMAND_SYNTAX, "/plot cluster tp <name>");
                    return false;
                }
                PlotArea area = plr.getApplicablePlotArea();
                if (area == null) {
                    C.NOT_IN_PLOT_WORLD.send(plr);
                    return false;
                }
                PlotCluster cluster = area.getCluster(args[1]);
                if (cluster == null) {
                    MainUtil.sendMessage(plr, C.INVALID_CLUSTER, args[1]);
                    return false;
                }
                UUID uuid = plr.getUUID();
                if (!cluster.isAdded(uuid)) {
                    if (!Permissions.hasPermission(plr, "plots.cluster.tp.other")) {
                        MainUtil.sendMessage(plr, C.NO_PERMISSION, "plots.cluster.tp.other");
                        return false;
                    }
                }
                plr.teleport(cluster.getHome());
                return MainUtil.sendMessage(plr, C.CLUSTER_TELEPORTING);
            }
            case "i":
            case "info":
            case "show":
            case "information": {
                if (!Permissions.hasPermission(plr, "plots.cluster.info")) {
                    MainUtil.sendMessage(plr, C.NO_PERMISSION, "plots.cluster.info");
                    return false;
                }
                if (args.length != 1 && args.length != 2) {
                    MainUtil.sendMessage(plr, C.COMMAND_SYNTAX, "/plot cluster info [name]");
                    return false;
                }
                PlotArea area = plr.getApplicablePlotArea();
                if (area == null) {
                    C.NOT_IN_PLOT_WORLD.send(plr);
                }
                PlotCluster cluster;
                if (args.length == 2) {
                    cluster = area.getCluster(args[1]);
                    if (cluster == null) {
                        MainUtil.sendMessage(plr, C.INVALID_CLUSTER, args[1]);
                        return false;
                    }
                } else {
                    cluster = area.getCluster(plr.getLocation());
                    if (cluster == null) {
                        MainUtil.sendMessage(plr, C.NOT_IN_CLUSTER);
                        return false;
                    }
                }
                String id = cluster.toString();
                String owner = UUIDHandler.getName(cluster.owner);
                if (owner == null) {
                    owner = "unknown";
                }
                String name = cluster.getName();
                String size = (cluster.getP2().x - cluster.getP1().x + 1) + "x" + (cluster.getP2().y - cluster.getP1().y + 1);
                String rights = cluster.isAdded(plr.getUUID()) + "";
                String message = C.CLUSTER_INFO.s();
                message = message.replaceAll("%id%", id);
                message = message.replaceAll("%owner%", owner);
                message = message.replaceAll("%name%", name);
                message = message.replaceAll("%size%", size);
                message = message.replaceAll("%rights%", rights);
                MainUtil.sendMessage(plr, message);
                return true;
            }
            case "sh":
            case "setspawn":
            case "sethome": {
                if (!Permissions.hasPermission(plr, "plots.cluster.sethome")) {
                    MainUtil.sendMessage(plr, C.NO_PERMISSION, "plots.cluster.sethome");
                    return false;
                }
                if (args.length != 1 && args.length != 2) {
                    MainUtil.sendMessage(plr, C.COMMAND_SYNTAX, "/plot cluster sethome");
                    return false;
                }
                PlotArea area = plr.getApplicablePlotArea();
                if (area == null) {
                    C.NOT_IN_PLOT_WORLD.send(plr);
                }
                PlotCluster cluster = area.getCluster(plr.getLocation());
                if (cluster == null) {
                    MainUtil.sendMessage(plr, C.NOT_IN_CLUSTER);
                    return false;
                }
                if (!cluster.hasHelperRights(plr.getUUID())) {
                    if (!Permissions.hasPermission(plr, "plots.cluster.sethome.other")) {
                        MainUtil.sendMessage(plr, C.NO_PERMISSION, "plots.cluster.sethome.other");
                        return false;
                    }
                }
                Location base = cluster.getClusterBottom();
                Location relative = plr.getLocation().subtract(base.getX(), 0, base.getZ());
                BlockLoc blockloc = new BlockLoc(relative.getX(), relative.getY(), relative.getZ());
                cluster.settings.setPosition(blockloc);
                DBFunc.setPosition(cluster, relative.getX() + "," + relative.getY() + "," + relative.getZ());
                return MainUtil.sendMessage(plr, C.POSITION_SET);
            }
        }
        MainUtil.sendMessage(plr, C.CLUSTER_AVAILABLE_ARGS);
        return false;
    }
}
