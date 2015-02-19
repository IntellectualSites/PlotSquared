////////////////////////////////////////////////////////////////////////////////////////////////////
// PlotSquared - A plot manager and world generator for the Bukkit API                             /
// Copyright (c) 2014 IntellectualSites/IntellectualCrafters                                       /
//                                                                                                 /
// This program is free software; you can redistribute it and/or modify                            /
// it under the terms of the GNU General Public License as published by                            /
// the Free Software Foundation; either version 3 of the License, or                               /
// (at your option) any later version.                                                             /
//                                                                                                 /
// This program is distributed in the hope that it will be useful,                                 /
// but WITHOUT ANY WARRANTY; without even the implied warranty of                                  /
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the                                   /
// GNU General Public License for more details.                                                    /
//                                                                                                 /
// You should have received a copy of the GNU General Public License                               /
// along with this program; if not, write to the Free Software Foundation,                         /
// Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301  USA                               /
//                                                                                                 /
// You can contact us via: support@intellectualsites.com                                           /
////////////////////////////////////////////////////////////////////////////////////////////////////

package com.intellectualcrafters.plot.commands;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.generator.BlockPopulator;

import com.intellectualcrafters.plot.BukkitMain;
import com.intellectualcrafters.plot.PlotSquared;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.database.DBFunc;
import com.intellectualcrafters.plot.generator.AugmentedPopulator;
import com.intellectualcrafters.plot.object.BlockLoc;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotCluster;
import com.intellectualcrafters.plot.object.PlotClusterId;
import com.intellectualcrafters.plot.object.PlotId;
import com.intellectualcrafters.plot.object.PlotWorld;
import com.intellectualcrafters.plot.util.ClusterManager;
import com.intellectualcrafters.plot.util.PlayerFunctions;
import com.intellectualcrafters.plot.util.PlotHelper;
import com.intellectualcrafters.plot.util.UUIDHandler;

public class Cluster extends SubCommand {

    public Cluster() {
        super(Command.CLUSTER, "Manage a plot cluster", "cluster", CommandCategory.ACTIONS, true);
    }

    @Override
    public boolean execute(final Player plr, final String... args) {
        // list, create, delete, resize, invite, kick, leave, helpers, tp, sethome
        
        if (args.length == 0) {
            // return arguments
            PlayerFunctions.sendMessage(plr, C.CLUSTER_AVAILABLE_ARGS);
            return false;
        }
        String sub = args[0].toLowerCase();
        switch (sub) {
            case "l":
            case "list": {
                if (!BukkitMain.hasPermission(plr, "plots.cluster.list")) {
                    PlayerFunctions.sendMessage(plr, C.NO_PERMISSION, "plots.cluster.list");
                    return false;
                }
                if (args.length != 1) {
                    PlayerFunctions.sendMessage(plr, C.COMMAND_SYNTAX, "/plot cluster list");
                    return false;
                }
                HashSet<PlotCluster> clusters = ClusterManager.getClusters(plr.getWorld());
                PlayerFunctions.sendMessage(plr, C.CLUSTER_LIST_HEADING, clusters.size() + "");
                for (PlotCluster cluster : clusters) {
                    // Ignore unmanaged clusters
                    String name = "'" + cluster.getName() + "' : " + cluster.toString();
                    if (UUIDHandler.getUUID(plr).equals(cluster.owner)) {
                        PlayerFunctions.sendMessage(plr, C.CLUSTER_LIST_ELEMENT, "&a" + name);
                    }
                    else if (cluster.helpers.contains(UUIDHandler.getUUID(plr))) {
                        PlayerFunctions.sendMessage(plr, C.CLUSTER_LIST_ELEMENT, "&3" + name);
                    }
                    else if (cluster.invited.contains(UUIDHandler.getUUID(plr))) {
                        PlayerFunctions.sendMessage(plr, C.CLUSTER_LIST_ELEMENT, "&9" + name);
                    }
                    else {
                        PlayerFunctions.sendMessage(plr, C.CLUSTER_LIST_ELEMENT, cluster.toString());
                    }
                }
                return true;
            }
            case "c":
            case "create": {
                if (!BukkitMain.hasPermission(plr, "plots.cluster.create")) {
                    PlayerFunctions.sendMessage(plr, C.NO_PERMISSION, "plots.cluster.create");
                    return false;
                }
                if (args.length != 4) {
                    PlotId id = ClusterManager.estimatePlotId(plr.getLocation());
                    PlayerFunctions.sendMessage(plr, C.COMMAND_SYNTAX, "/plot cluster create <name> <id-bot> <id-top>");
                    PlayerFunctions.sendMessage(plr, C.CLUSTER_CURRENT_PLOTID, "" + id);
                    return false;
                }
                // check pos1 / pos2
                PlotId pos1 = PlotHelper.parseId(args[2]);
                PlotId pos2 = PlotHelper.parseId(args[3]);
                if (pos1 == null || pos2 == null) {
                    PlayerFunctions.sendMessage(plr, C.NOT_VALID_PLOT_ID);
                    return false;
                }
                // check if name is taken
                String name = args[1];
                for (PlotCluster cluster : ClusterManager.getClusters(plr.getWorld())) {
                    if (name.equals(cluster.getName())) {
                        PlayerFunctions.sendMessage(plr, C.ALIAS_IS_TAKEN);
                        return false; 
                    }
                }
                //check if overlap
                PlotClusterId id = new PlotClusterId(pos1, pos2);
                HashSet<PlotCluster> intersects = ClusterManager.getIntersects(plr.getWorld().getName(), id);
                if (intersects.size() > 0 || pos2.x < pos1.x || pos2.y < pos1.y) {
                    PlayerFunctions.sendMessage(plr, C.CLUSTER_INTERSECTION, intersects.size() + "");
                    return false; 
                }
                // create cluster
                String world = plr.getWorld().getName();
                PlotCluster cluster = new PlotCluster(world, pos1, pos2, UUIDHandler.getUUID(plr));
                cluster.settings.setAlias(name);
                DBFunc.createCluster(world, cluster);
                if (!ClusterManager.clusters.containsKey(world)) {
                    ClusterManager.clusters.put(world, new HashSet<PlotCluster>());
                }
                ClusterManager.clusters.get(world).add(cluster);
                // Add any existing plots to the current cluster
                for (Plot plot : PlotSquared.getPlots(plr.getWorld()).values()) {
                    PlotCluster current = ClusterManager.getCluster(plot);
                    if (cluster.equals(current) && !cluster.hasRights(plot.owner)) {
                        cluster.invited.add(plot.owner);
                        DBFunc.setInvited(world, cluster, plot.owner);
                    }
                }
                if (!PlotSquared.isPlotWorld(world)) {
                    PlotSquared.config.createSection("worlds." + world);
                    PlotSquared.loadWorld(plr.getWorld());
                }
                PlayerFunctions.sendMessage(plr, C.CLUSTER_ADDED);
                return true;
            }
            case "disband":
            case "del":
            case "delete": {
                if (!BukkitMain.hasPermission(plr, "plots.cluster.delete")) {
                    PlayerFunctions.sendMessage(plr, C.NO_PERMISSION, "plots.cluster.delete");
                    return false;
                }
                if (args.length != 1 && args.length != 2) {
                    PlayerFunctions.sendMessage(plr, C.COMMAND_SYNTAX, "/plot cluster delete [name]");
                    return false;
                }
                PlotCluster cluster;
                if (args.length == 2) {
                    cluster = ClusterManager.getCluster(plr.getWorld().getName(), args[1]);
                    if (cluster == null) {
                        PlayerFunctions.sendMessage(plr, C.INVALID_CLUSTER, args[1]);
                        return false;
                    }
                }
                else {
                    cluster = ClusterManager.getCluster(plr.getLocation());
                    if (cluster == null) {
                        PlayerFunctions.sendMessage(plr, C.NOT_IN_CLUSTER);
                        return false;
                    }
                }
                if (!cluster.owner.equals(UUIDHandler.getUUID(plr))) {
                    if (!BukkitMain.hasPermission(plr, "plots.cluster.delete.other")) {
                        PlayerFunctions.sendMessage(plr, C.NO_PERMISSION, "plots.cluster.delete.other");
                        return false;
                    }
                }
                PlotWorld plotworld = PlotSquared.getWorldSettings(plr.getWorld());
                if (plotworld.TYPE == 2) {
                    ArrayList<Plot> toRemove = new ArrayList<>();
                    for (Plot plot : PlotSquared.getPlots(plr.getWorld()).values()) {
                        PlotCluster other = ClusterManager.getCluster(plot);
                        if (cluster.equals(other)) {
                            toRemove.add(plot);
                        }
                    }
                    for (Plot plot : toRemove) {
                        DBFunc.delete(plot.world, plot);
                    }
                }
                DBFunc.delete(cluster);
                if (plotworld.TYPE == 2) {
                    for (Iterator<BlockPopulator> iterator = plr.getWorld().getPopulators().iterator(); iterator.hasNext();) {
                        BlockPopulator populator = iterator.next();
                        if (populator instanceof AugmentedPopulator) {
                            if (((AugmentedPopulator) populator).cluster.equals(cluster)) {
                                iterator.remove();
                            }
                        }
                    }
                }
                for (String set : ClusterManager.clusters.keySet()) {
                }
                ClusterManager.last = null;
                ClusterManager.clusters.get(cluster.world).remove(cluster);
                ClusterManager.regenCluster(cluster);
                PlayerFunctions.sendMessage(plr, C.CLUSTER_DELETED);
                return true;
            }
            case "res":
            case "resize": {
                if (!BukkitMain.hasPermission(plr, "plots.cluster.resize")) {
                    PlayerFunctions.sendMessage(plr, C.NO_PERMISSION, "plots.cluster.resize");
                    return false;
                }
                if (args.length != 3) {
                    PlayerFunctions.sendMessage(plr, C.COMMAND_SYNTAX, "/plot cluster resize <pos1> <pos2>");
                    return false;
                }
                // check pos1 / pos2
                PlotId pos1 = PlotHelper.parseId(args[1]);
                PlotId pos2 = PlotHelper.parseId(args[2]);
                if (pos1 == null || pos2 == null) {
                    PlayerFunctions.sendMessage(plr, C.NOT_VALID_PLOT_ID);
                    return false;
                }
                // check if in cluster
                PlotCluster cluster = ClusterManager.getCluster(plr.getLocation());
                if (cluster == null) {
                    PlayerFunctions.sendMessage(plr, C.NOT_IN_CLUSTER);
                    return false;
                }
                if (!cluster.hasHelperRights(UUIDHandler.getUUID(plr))) {
                    if (!BukkitMain.hasPermission(plr, "plots.cluster.resize.other")) {
                        PlayerFunctions.sendMessage(plr, C.NO_PERMISSION, "plots.cluster.resize.other");
                        return false;
                    }
                }
                //check if overlap
                PlotClusterId id = new PlotClusterId(pos1, pos2);
                HashSet<PlotCluster> intersects = ClusterManager.getIntersects(plr.getWorld().getName(), id);
                if (intersects.size() > 1) {
                    PlayerFunctions.sendMessage(plr, C.CLUSTER_INTERSECTION, (intersects.size() - 1) + "");
                    return false; 
                }
                // resize cluster
                DBFunc.resizeCluster(cluster, id);
                PlayerFunctions.sendMessage(plr, C.CLUSTER_RESIZED);
                return true;
            }
            case "reg":
            case "regenerate":
            case "regen": {
                if (!BukkitMain.hasPermission(plr, "plots.cluster.delete")) {
                    PlayerFunctions.sendMessage(plr, C.NO_PERMISSION, "plots.cluster.regen");
                    return false;
                }
                if (args.length != 1 && args.length != 2) {
                    PlayerFunctions.sendMessage(plr, C.COMMAND_SYNTAX, "/plot cluster regen [name]");
                    return false;
                }
                PlotCluster cluster;
                if (args.length == 2) {
                    cluster = ClusterManager.getCluster(plr.getWorld().getName(), args[1]);
                    if (cluster == null) {
                        PlayerFunctions.sendMessage(plr, C.INVALID_CLUSTER, args[1]);
                        return false;
                    }
                }
                else {
                    cluster = ClusterManager.getCluster(plr.getLocation());
                    if (cluster == null) {
                        PlayerFunctions.sendMessage(plr, C.NOT_IN_CLUSTER);
                        return false;
                    }
                }
                if (!cluster.owner.equals(UUIDHandler.getUUID(plr))) {
                    if (!BukkitMain.hasPermission(plr, "plots.cluster.regen.other")) {
                        PlayerFunctions.sendMessage(plr, C.NO_PERMISSION, "plots.cluster.regen.other");
                        return false;
                    }
                }
                ClusterManager.regenCluster(cluster);
                PlayerFunctions.sendMessage(plr, C.CLUSTER_REGENERATED);
                return true;
            }
            case "add": 
            case "inv": 
            case "invite": {
                if (!BukkitMain.hasPermission(plr, "plots.cluster.invite")) {
                    PlayerFunctions.sendMessage(plr, C.NO_PERMISSION, "plots.cluster.invite");
                    return false;
                }
                if (args.length != 2) {
                    PlayerFunctions.sendMessage(plr, C.COMMAND_SYNTAX, "/plot cluster invite <player>");
                    return false;
                }
                // check if in cluster
                PlotCluster cluster = ClusterManager.getCluster(plr.getLocation());
                if (cluster == null) {
                    PlayerFunctions.sendMessage(plr, C.NOT_IN_CLUSTER);
                    return false;
                }
                if (!cluster.hasHelperRights(UUIDHandler.getUUID(plr))) {
                    if (!BukkitMain.hasPermission(plr, "plots.cluster.invite.other")) {
                        PlayerFunctions.sendMessage(plr, C.NO_PERMISSION, "plots.cluster.invite.other");
                        return false;
                    }
                }
                // check uuid
                UUID uuid = UUIDHandler.getUUID(args[1]);
                if (uuid == null) {
                    PlayerFunctions.sendMessage(plr, C.INVALID_PLAYER, args[1]);
                    return false;
                }
                if (!cluster.hasRights(uuid)) {
                    // add the user if not added
                    cluster.invited.add(uuid);
                    String world = plr.getWorld().getName();
                    DBFunc.setInvited(world, cluster, uuid);
                    Player player = UUIDHandler.uuidWrapper.getPlayer(uuid);
                    if (player != null) {
                        PlayerFunctions.sendMessage(player, C.CLUSTER_INVITED, cluster.getName());
                    }
                }
                PlayerFunctions.sendMessage(plr, C.CLUSTER_ADDED_USER);
                return true;
            }
            case "k":
            case "remove":
            case "kick": {
                if (!BukkitMain.hasPermission(plr, "plots.cluster.kick")) {
                    PlayerFunctions.sendMessage(plr, C.NO_PERMISSION, "plots.cluster.kick");
                    return false;
                }
                if (args.length != 2) {
                    PlayerFunctions.sendMessage(plr, C.COMMAND_SYNTAX, "/plot cluster kick <player>");
                    return false;
                }
                PlotCluster cluster = ClusterManager.getCluster(plr.getLocation());
                if (cluster == null) {
                    PlayerFunctions.sendMessage(plr, C.NOT_IN_CLUSTER);
                    return false;
                }
                if (!cluster.hasHelperRights(UUIDHandler.getUUID(plr))) {
                    if (!BukkitMain.hasPermission(plr, "plots.cluster.kick.other")) {
                        PlayerFunctions.sendMessage(plr, C.NO_PERMISSION, "plots.cluster.kick.other");
                        return false;
                    }
                }
                // check uuid
                UUID uuid = UUIDHandler.getUUID(args[1]);
                if (uuid == null) {
                    PlayerFunctions.sendMessage(plr, C.INVALID_PLAYER, args[1]);
                    return false;
                }
                // Can't kick if the player is yourself, the owner, or not added to the cluster
                if (uuid.equals(UUIDHandler.getUUID(plr)) || uuid.equals(cluster.owner) || !cluster.hasRights(uuid)) {
                    PlayerFunctions.sendMessage(plr, C.CANNOT_KICK_PLAYER, cluster.getName());
                    return false;
                }
                if (cluster.helpers.contains(uuid)) {
                    cluster.helpers.remove(uuid);
                    DBFunc.removeHelper(cluster, uuid);
                }
                cluster.invited.remove(uuid);
                DBFunc.removeInvited(cluster, uuid);
                Player player = UUIDHandler.uuidWrapper.getPlayer(uuid);
                if (player != null) {
                    PlayerFunctions.sendMessage(player, C.CLUSTER_REMOVED, cluster.getName());
                }
                for (Plot plot : PlotSquared.getPlots(plr.getWorld(), uuid)) {
                    PlotCluster current = ClusterManager.getCluster(plot);
                    if (current != null && current.equals(cluster)) {
                        String world = plr.getWorld().getName();
                        DBFunc.delete(world, plot);
                    }
                }
                PlayerFunctions.sendMessage(plr, C.CLUSTER_KICKED_USER);
                return true;
            }
            case "quit":
            case "leave": {
                if (!BukkitMain.hasPermission(plr, "plots.cluster.leave")) {
                    PlayerFunctions.sendMessage(plr, C.NO_PERMISSION, "plots.cluster.leave");
                    return false;
                }
                if (args.length != 1 && args.length != 2) {
                    PlayerFunctions.sendMessage(plr, C.COMMAND_SYNTAX, "/plot cluster leave [name]");
                    return false;
                }
                PlotCluster cluster;
                if (args.length == 2) {
                    cluster = ClusterManager.getCluster(plr.getWorld().getName(), args[1]);
                    if (cluster == null) {
                        PlayerFunctions.sendMessage(plr, C.INVALID_CLUSTER, args[1]);
                        return false;
                    }
                }
                else {
                    cluster = ClusterManager.getCluster(plr.getLocation());
                    if (cluster == null) {
                        PlayerFunctions.sendMessage(plr, C.NOT_IN_CLUSTER);
                        return false;
                    }
                }
                UUID uuid = UUIDHandler.getUUID(plr);
                if (!cluster.hasRights(uuid)) {
                    PlayerFunctions.sendMessage(plr, C.CLUSTER_NOT_ADDED);
                    return false;
                }
                if (uuid.equals(cluster.owner)) {
                    PlayerFunctions.sendMessage(plr, C.CLUSTER_CANNOT_LEAVE);
                    return false;
                }
                if (cluster.helpers.contains(uuid)) {
                    cluster.helpers.remove(uuid);
                    DBFunc.removeHelper(cluster, uuid);
                }
                cluster.invited.remove(uuid);
                DBFunc.removeInvited(cluster, uuid);
                PlayerFunctions.sendMessage(plr, C.CLUSTER_REMOVED, cluster.getName());
                for (Plot plot : PlotSquared.getPlots(plr.getWorld(), uuid)) {
                    PlotCluster current = ClusterManager.getCluster(plot);
                    if (current != null && current.equals(cluster)) {
                        String world = plr.getWorld().getName();
                        DBFunc.delete(world, plot);
                    }
                }
                return true;
            }
            case "admin":
            case "helper":
            case "helpers": {
                if (!BukkitMain.hasPermission(plr, "plots.cluster.helpers")) {
                    PlayerFunctions.sendMessage(plr, C.NO_PERMISSION, "plots.cluster.helpers");
                    return false;
                }
                if (args.length != 3) {
                    PlayerFunctions.sendMessage(plr, C.COMMAND_SYNTAX, "/plot cluster helpers <add|remove> <player>");
                    return false;
                }
                PlotCluster cluster = ClusterManager.getCluster(plr.getLocation());
                if (cluster == null) {
                    PlayerFunctions.sendMessage(plr, C.NOT_IN_CLUSTER);
                    return false;
                }
                UUID uuid = UUIDHandler.getUUID(args[2]);
                if (uuid == null) {
                    PlayerFunctions.sendMessage(plr, C.INVALID_PLAYER, args[1]);
                    return false;
                }
                if (args[1].toLowerCase().equals("add")) {
                    cluster.helpers.add(uuid);
                    return PlayerFunctions.sendMessage(plr, C.CLUSTER_ADDED_HELPER);
                }
                if (args[1].toLowerCase().equals("remove")) {
                    cluster.helpers.remove(uuid);
                    return PlayerFunctions.sendMessage(plr, C.CLUSTER_REMOVED_HELPER);
                }
                PlayerFunctions.sendMessage(plr, C.COMMAND_SYNTAX, "/plot cluster helpers <add|remove> <player>");
                return false;
            }
            case "spawn":
            case "home":
            case "tp": {
                if (!BukkitMain.hasPermission(plr, "plots.cluster.tp")) {
                    PlayerFunctions.sendMessage(plr, C.NO_PERMISSION, "plots.cluster.tp");
                    return false;
                }
                if (args.length != 2) {
                    PlayerFunctions.sendMessage(plr, C.COMMAND_SYNTAX, "/plot cluster tp <name>");
                    return false;
                }
                PlotCluster cluster = ClusterManager.getCluster(plr.getWorld().getName(), args[1]);
                if (cluster == null) {
                    PlayerFunctions.sendMessage(plr, C.INVALID_CLUSTER, args[1]);
                    return false;
                }
                UUID uuid = UUIDHandler.getUUID(plr);
                if (!cluster.hasRights(uuid)) {
                    if (!BukkitMain.hasPermission(plr, "plots.cluster.tp.other")) {
                        PlayerFunctions.sendMessage(plr, C.NO_PERMISSION, "plots.cluster.tp.other");
                        return false;
                    }
                }
                plr.teleport(ClusterManager.getHome(cluster));
                return PlayerFunctions.sendMessage(plr, C.CLUSTER_TELEPORTING);
            }
            case "i":
            case "info":
            case "show":
            case "information": {
                if (!BukkitMain.hasPermission(plr, "plots.cluster.info")) {
                    PlayerFunctions.sendMessage(plr, C.NO_PERMISSION, "plots.cluster.info");
                    return false;
                }
                if (args.length != 1 && args.length != 2) {
                    PlayerFunctions.sendMessage(plr, C.COMMAND_SYNTAX, "/plot cluster info [name]");
                    return false;
                }
                PlotCluster cluster;
                if (args.length == 2) {
                    cluster = ClusterManager.getCluster(plr.getWorld().getName(), args[1]);
                    if (cluster == null) {
                        PlayerFunctions.sendMessage(plr, C.INVALID_CLUSTER, args[1]);
                        return false;
                    }
                }
                else {
                    cluster = ClusterManager.getCluster(plr.getLocation());
                    if (cluster == null) {
                        PlayerFunctions.sendMessage(plr, C.NOT_IN_CLUSTER);
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
                String rights = cluster.hasRights(UUIDHandler.getUUID(plr)) + "";
                
                String message = C.CLUSTER_INFO.s();
                message = message.replaceAll("%id%", id);
                message = message.replaceAll("%owner%", owner);
                message = message.replaceAll("%name%", name);
                message = message.replaceAll("%size%", size);
                message = message.replaceAll("%rights%", rights);
                
                PlayerFunctions.sendMessage(plr, message);
                return true;
            }
            case "sh":
            case "setspawn":
            case "sethome": {
                if (!BukkitMain.hasPermission(plr, "plots.cluster.sethome")) {
                    PlayerFunctions.sendMessage(plr, C.NO_PERMISSION, "plots.cluster.sethome");
                    return false;
                }
                if (args.length != 1 && args.length != 2) {
                    PlayerFunctions.sendMessage(plr, C.COMMAND_SYNTAX, "/plot cluster sethome");
                    return false;
                }
                PlotCluster cluster = ClusterManager.getCluster(plr.getLocation());
                if (cluster == null) {
                    PlayerFunctions.sendMessage(plr, C.NOT_IN_CLUSTER);
                    return false;
                }
                if (!cluster.hasHelperRights(UUIDHandler.getUUID(plr))) {
                    if (!BukkitMain.hasPermission(plr, "plots.cluster.sethome.other")) {
                        PlayerFunctions.sendMessage(plr, C.NO_PERMISSION, "plots.cluster.sethome.other");
                        return false;
                    }
                }
                Location base = ClusterManager.getClusterBottom(cluster);
                base.setY(0);
                Location relative = plr.getLocation().subtract(base);
                BlockLoc blockloc = new BlockLoc(relative.getBlockX(), relative.getBlockY(), relative.getBlockZ());
                cluster.settings.setPosition(blockloc);
                DBFunc.setPosition(cluster, relative.getBlockX() + "," + relative.getBlockY() + "," + relative.getBlockZ());
                return PlayerFunctions.sendMessage(plr, C.POSITION_SET);
            }
        }
        PlayerFunctions.sendMessage(plr, C.CLUSTER_AVAILABLE_ARGS);
        return false;
    }
}
