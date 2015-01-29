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

import java.util.HashSet;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.intellectualcrafters.plot.PlotMain;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.database.DBFunc;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotCluster;
import com.intellectualcrafters.plot.object.PlotClusterId;
import com.intellectualcrafters.plot.object.PlotId;
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
        if (!ClusterManager.clusters.containsKey(plr.getWorld().getName())) {
            return false;
        }
        // list, create, delete, resize, invite, kick, leave, helpers, tp
        
        if (args.length == 0) {
            // return arguments
            PlayerFunctions.sendMessage(plr, C.CLUSTER_AVAILABLE_ARGS);
            return false;
        }
        String sub = args[0].toLowerCase();
        switch (sub) {
            case "list": {
                if (!PlotMain.hasPermission(plr, "plots.cluster.list")) {
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
                    if (cluster.settings.getAlias().equals("")) {
                        continue;
                    }
                    if (UUIDHandler.getUUID(plr).equals(cluster.owner)) {
                        PlayerFunctions.sendMessage(plr, C.CLUSTER_LIST_ELEMENT, "&a" + cluster.toString());
                    }
                    else if (cluster.helpers.contains(UUIDHandler.getUUID(plr))) {
                        PlayerFunctions.sendMessage(plr, C.CLUSTER_LIST_ELEMENT, "&3" + cluster.toString());
                    }
                    else if (cluster.invited.contains(UUIDHandler.getUUID(plr))) {
                        PlayerFunctions.sendMessage(plr, C.CLUSTER_LIST_ELEMENT, "&9" + cluster.toString());
                    }
                    else {
                        PlayerFunctions.sendMessage(plr, C.CLUSTER_LIST_ELEMENT, cluster.toString());
                    }
                }
                return true;
            }
            case "create": {
                if (!PlotMain.hasPermission(plr, "plots.cluster.create")) {
                    PlayerFunctions.sendMessage(plr, C.NO_PERMISSION, "plots.cluster.create");
                    return false;
                }
                if (args.length != 4) {
                    PlayerFunctions.sendMessage(plr, C.COMMAND_SYNTAX, "/plot cluster create <name> <id-bot> <id-top>");
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
                if (intersects.size() > 0) {
                    PlayerFunctions.sendMessage(plr, C.CLUSTER_INTERSECTION, intersects.size() + "");
                    return false; 
                }
                // create cluster
                String world = plr.getWorld().getName();
                PlotCluster cluster = new PlotCluster(world, pos1, pos2, UUIDHandler.getUUID(plr));
                cluster.settings.setAlias(name);
                DBFunc.createCluster(world, cluster);
                ClusterManager.clusters.get(world).add(cluster);
                PlayerFunctions.sendMessage(plr, C.CLUSTER_ADDED);
                return true;
            }
            case "delete": {
                if (!PlotMain.hasPermission(plr, "plots.cluster.delete")) {
                    PlayerFunctions.sendMessage(plr, C.NO_PERMISSION, "plots.cluster.delete");
                    return false;
                }
                if (args.length != 1) {
                    PlayerFunctions.sendMessage(plr, C.COMMAND_SYNTAX, "/plot cluster delete");
                    return false;
                }
                PlotCluster toDelete = ClusterManager.getCluster(plr.getLocation());
                if (toDelete == null) {
                    PlayerFunctions.sendMessage(plr, C.NOT_IN_CLUSTER);
                    return false;
                }
                String world_delete = plr.getWorld().getName();
                ClusterManager.clusters.get(world_delete).remove(toDelete);
                DBFunc.delete(toDelete);
                PlayerFunctions.sendMessage(plr, C.CLUSTER_DELETED);
                return true;
            }
            case "resize": {
                if (!PlotMain.hasPermission(plr, "plots.cluster.resize")) {
                    PlayerFunctions.sendMessage(plr, C.NO_PERMISSION, "plots.cluster.resize");
                    return false;
                }
                if (args.length != 3) {
                    PlayerFunctions.sendMessage(plr, C.COMMAND_SYNTAX, "/plot cluster resize <pos1> <pos2>");
                    return false;
                }
                // check pos1 / pos2
                PlotId pos1 = PlotHelper.parseId(args[2]);
                PlotId pos2 = PlotHelper.parseId(args[3]);
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
                //check if overlap
                PlotClusterId id = new PlotClusterId(pos1, pos2);
                HashSet<PlotCluster> intersects = ClusterManager.getIntersects(plr.getWorld().getName(), id);
                if (intersects.size() > 0) {
                    PlayerFunctions.sendMessage(plr, C.CLUSTER_INTERSECTION, intersects.size() + "");
                    return false; 
                }
                // resize cluster
                DBFunc.resizeCluster(cluster, id);
                PlayerFunctions.sendMessage(plr, C.CLUSTER_RESIZED);
                return true;
            }
            case "invite": {
                if (!PlotMain.hasPermission(plr, "plots.cluster.invite")) {
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
                        PlayerFunctions.sendMessage(plr, C.CLUSTER_INVITED, cluster.getName());
                    }
                }
                PlayerFunctions.sendMessage(plr, C.CLUSTER_ADDED_USER);
                return true;
            }
            case "kick": {
                if (!PlotMain.hasPermission(plr, "plots.cluster.kick")) {
                    PlayerFunctions.sendMessage(plr, C.NO_PERMISSION, "plots.cluster.kick");
                    return false;
                }
                if (args.length != 2) {
                    PlayerFunctions.sendMessage(plr, C.COMMAND_SYNTAX, "/plot cluster kick <player>");
                    return false;
                }
                return true;
            }
            case "leave": {
                if (!PlotMain.hasPermission(plr, "plots.cluster.leave")) {
                    PlayerFunctions.sendMessage(plr, C.NO_PERMISSION, "plots.cluster.leave");
                    return false;
                }
                if (args.length != 1 && args.length != 2) {
                    PlayerFunctions.sendMessage(plr, C.COMMAND_SYNTAX, "/plot cluster leave [name]");
                    return false;
                }
                return true;
            }
            case "helpers": {
                if (!PlotMain.hasPermission(plr, "plots.cluster.helpers")) {
                    PlayerFunctions.sendMessage(plr, C.NO_PERMISSION, "plots.cluster.helpers");
                    return false;
                }
                if (args.length != 3) {
                    PlayerFunctions.sendMessage(plr, C.COMMAND_SYNTAX, "/plot cluster helpers <add|remove> <player>");
                    return false;
                }
                return true;
            }
            case "tp": {
                if (!PlotMain.hasPermission(plr, "plots.cluster.tp")) {
                    PlayerFunctions.sendMessage(plr, C.NO_PERMISSION, "plots.cluster.tp");
                    return false;
                }
                if (args.length != 2) {
                    PlayerFunctions.sendMessage(plr, C.COMMAND_SYNTAX, "/plot cluster tp <name>");
                    return false;
                }
                return true;
            }
            case "info": {
                if (!PlotMain.hasPermission(plr, "plots.cluster.info")) {
                    PlayerFunctions.sendMessage(plr, C.NO_PERMISSION, "plots.cluster.info");
                    return false;
                }
                if (args.length != 1 && args.length != 2) {
                    PlayerFunctions.sendMessage(plr, C.COMMAND_SYNTAX, "/plot cluster info [name]");
                    return false;
                }
                return true;
            }
        }
        PlayerFunctions.sendMessage(plr, C.CLUSTER_AVAILABLE_ARGS);
        return false;
    }
    
    
}
