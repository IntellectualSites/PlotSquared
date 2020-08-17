/*
 *       _____  _       _    _____                                _
 *      |  __ \| |     | |  / ____|                              | |
 *      | |__) | | ___ | |_| (___   __ _ _   _  __ _ _ __ ___  __| |
 *      |  ___/| |/ _ \| __|\___ \ / _` | | | |/ _` | '__/ _ \/ _` |
 *      | |    | | (_) | |_ ____) | (_| | |_| | (_| | | |  __/ (_| |
 *      |_|    |_|\___/ \__|_____/ \__, |\__,_|\__,_|_|  \___|\__,_|
 *                                    | |
 *                                    |_|
 *            PlotSquared plot management system for Minecraft
 *                  Copyright (C) 2020 IntellectualSites
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.plotsquared.core.command;

import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.permissions.Permission;
import com.plotsquared.core.configuration.Settings;
import com.plotsquared.core.configuration.caption.Caption;
import com.plotsquared.core.configuration.caption.StaticCaption;
import com.plotsquared.core.configuration.caption.TranslatableCaption;
import com.plotsquared.core.database.DBFunc;
import com.plotsquared.core.events.TeleportCause;
import com.plotsquared.core.location.BlockLoc;
import com.plotsquared.core.location.Location;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.PlotArea;
import com.plotsquared.core.plot.PlotCluster;
import com.plotsquared.core.plot.PlotId;
import com.plotsquared.core.util.Permissions;
import com.plotsquared.core.util.query.PlotQuery;
import net.kyori.adventure.text.minimessage.Template;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

@CommandDeclaration(command = "cluster",
    aliases = "clusters",
    category = CommandCategory.ADMINISTRATION,
    requiredType = RequiredType.NONE,
    permission = "plots.cluster",
    description = "Manage a plot cluster")
public class Cluster extends SubCommand {

    @Override public boolean onCommand(PlotPlayer<?> player, String[] args) {

        // list, create, delete, resize, invite, kick, leave, helpers, tp, sethome
        if (args.length == 0) {
            // return arguments
            player.sendMessage(TranslatableCaption.of("cluster.cluster_available_args"));
            return false;
        }
        String sub = args[0].toLowerCase();
        switch (sub) {
            case "l":
            case "list": {
                if (!Permissions.hasPermission(player, "plots.cluster.list")) {
                    player.sendMessage(
                            TranslatableCaption.of("permission.no_permission"),
                            Template.of("node", "plots.cluster.list")
                    );
                    return false;
                }
                if (args.length != 1) {
                    player.sendMessage(
                            TranslatableCaption.of("commandconfig.command_syntax"),
                            Template.of("value", "/plot cluster list")
                    );
                    return false;
                }
                PlotArea area = player.getApplicablePlotArea();
                if (area == null) {
                    player.sendMessage(TranslatableCaption.of("errors.not_in_plot_world"));
                    return false;
                }
                Set<PlotCluster> clusters = area.getClusters();
                player.sendMessage(
                        TranslatableCaption.of("cluster.cluster_list_heading"),
                        Template.of("amount", clusters.size() + "")
                );
                for (PlotCluster cluster : clusters) {
                    // Ignore unmanaged clusters
                    String name = "'" + cluster.getName() + "' : " + cluster.toString();
                    if (player.getUUID().equals(cluster.owner)) {
                        player.sendMessage(
                                TranslatableCaption.of("cluster.cluster_list_element_owner"),
                                Template.of("cluster", name)
                        );
                    } else if (cluster.helpers.contains(player.getUUID())) {
                        player.sendMessage(
                                TranslatableCaption.of("cluster.cluster_list_element_helpers"),
                                Template.of("cluster", name)
                        );
                    } else if (cluster.invited.contains(player.getUUID())) {
                        player.sendMessage(
                                TranslatableCaption.of("cluster.cluster_list_element_invited"),
                                Template.of("cluster", name)
                        );
                    } else {
                        player.sendMessage(
                                TranslatableCaption.of("cluster.cluster_list_element"),
                                Template.of("cluster", cluster.toString())
                        );
                    }
                }
                return true;
            }
            case "c":
            case "create": {
                if (!Permissions.hasPermission(player, "plots.cluster.create")) {
                    player.sendMessage(
                            TranslatableCaption.of("permission.no_permission"),
                            Template.of("node", "plots.cluster.create")
                    );
                    return false;
                }
                PlotArea area = player.getApplicablePlotArea();
                if (area == null) {
                    player.sendMessage(TranslatableCaption.of("errors.not_in_plot_world"));
                    return false;
                }
                if (args.length != 4) {
                    player.sendMessage(
                            TranslatableCaption.of("commandconfig.command_syntax"),
                            Template.of("value", "/plot cluster create <name> <id-bot> <id-top>")
                    );
                    return false;
                }
                int currentClusters = Settings.Limit.GLOBAL ?
                    player.getClusterCount() :
                    player.getPlotCount(player.getLocation().getWorldName());
                if (currentClusters >= player.getAllowedPlots()) {
                    player.sendMessage(TranslatableCaption.of("permission.cant_claim_more_clusters"));
                }
                PlotId pos1;
                PlotId pos2;
                // check pos1 / pos2
                try {
                    pos1 = PlotId.fromString(args[2]);
                    pos2 = PlotId.fromString(args[3]);
                } catch (IllegalArgumentException ignored) {
                    player.sendMessage(TranslatableCaption.of("invalid.not_valid_plot_id"));
                    return false;
                }
                // check if name is taken
                String name = args[1];
                if (area.getCluster(name) != null) {
                    player.sendMessage(TranslatableCaption.of("alias.alias_is_taken"));
                    return false;
                }
                if (pos2.getX() < pos1.getX() || pos2.getY() < pos1.getY()) {
                    PlotId tmp = PlotId.of(Math.min(pos1.getX(), pos2.getX()), Math.min(pos1.getY(), pos2.getY()));
                    pos2 = PlotId.of(Math.max(pos1.getX(), pos2.getX()), Math.max(pos1.getY(), pos2.getY()));
                    pos1 = tmp;
                }
                //check if overlap
                PlotCluster cluster = area.getFirstIntersectingCluster(pos1, pos2);
                if (cluster != null) {
                    player.sendMessage(
                            TranslatableCaption.of("cluster.cluster_intersection"),
                            Template.of("cluster", cluster.getName())
                    );
                    return false;
                }
                // Check if it occupies existing plots
                if (!area.contains(pos1) || !area.contains(pos2)) {
                    player.sendMessage(
                            TranslatableCaption.of("cluster.cluster_outside"),
                            Template.of("area", String.valueOf(area))
                    );
                    return false;
                }
                Set<Plot> plots = area.getPlotSelectionOwned(pos1, pos2);
                if (!plots.isEmpty()) {
                    if (!Permissions
                        .hasPermission(player, "plots.cluster.create.other")) {
                        UUID uuid = player.getUUID();
                        for (Plot plot : plots) {
                            if (!plot.isOwner(uuid)) {
                                player.sendMessage(
                                        TranslatableCaption.of("permission.no_permission"),
                                        Template.of("node", "plots.cluster.create.other")
                                );
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
                    current = player.getPlayerClusterCount(player.getLocation().getWorldName());
                }
                int allowed = Permissions
                    .hasPermissionRange(player, Permission.PERMISSION_CLUSTER_SIZE,
                        Settings.Limit.MAX_PLOTS);
                if (current + cluster.getArea() > allowed) {
                    player.sendMessage(
                            TranslatableCaption.of("permission.no_permission"),
                            Template.of("node", "plots.cluster.size" + "." + (current + cluster.getArea()))
                    );
                    return false;
                }
                // create cluster
                cluster.settings.setAlias(name);
                area.addCluster(cluster);
                DBFunc.createCluster(cluster);
                // Add any existing plots to the current cluster
                for (Plot plot : plots) {
                    if (plot.hasOwner()) {
                        if (!cluster.isAdded(plot.getOwner())) {
                            cluster.invited.add(plot.getOwner());
                            DBFunc.setInvited(cluster, plot.getOwner());
                        }
                    }
                }
                player.sendMessage(TranslatableCaption.of("cluster.cluster_added"));
                return true;
            }
            case "disband":
            case "del":
            case "delete": {
                if (!Permissions.hasPermission(player, Permission.PERMISSION_CLUSTER_DELETE)) {
                    player.sendMessage(
                            TranslatableCaption.of("permission.no_permission"),
                            Template.of("node", "plots.cluster.delete")
                    );
                    return false;
                }
                if (args.length != 1 && args.length != 2) {
                    player.sendMessage(
                            TranslatableCaption.of("commandconfig.command_syntax"),
                            Template.of("value", "/plot cluster delete [name]")
                    );
                    return false;
                }
                PlotArea area = player.getApplicablePlotArea();
                if (area == null) {
                    player.sendMessage(TranslatableCaption.of("errors.not_in_plot_world"));
                    return false;
                }
                PlotCluster cluster;
                if (args.length == 2) {
                    cluster = area.getCluster(args[1]);
                    if (cluster == null) {
                        player.sendMessage(
                                TranslatableCaption.of("cluster.invalid_cluster_name"),
                                Template.of("cluster", args[1])
                        );
                        return false;
                    }
                } else {
                    cluster = area.getCluster(player.getLocation());
                    if (cluster == null) {
                        player.sendMessage(TranslatableCaption.of("errors.not_in_cluster"));
                        return false;
                    }
                }
                if (!cluster.owner.equals(player.getUUID())) {
                    if (!Permissions
                        .hasPermission(player, Permission.PERMISSION_CLUSTER_DELETE_OTHER)) {
                        player.sendMessage(
                                TranslatableCaption.of("permission.no_permission"),
                                Template.of("node", "plots.cluster.delete.other"));
                        return false;
                    }
                }
                DBFunc.delete(cluster);
                player.sendMessage(TranslatableCaption.of("cluster.cluster_deleted"));
                return true;
            }
            case "res":
            case "resize": {
                if (!Permissions.hasPermission(player, Permission.PERMISSION_CLUSTER_RESIZE)) {
                    player.sendMessage(
                            TranslatableCaption.of("permission.no_permission"),
                            Template.of("node", "plots.cluster.resize"));
                    return false;
                }
                if (args.length != 3) {
                    player.sendMessage(
                            TranslatableCaption.of("commandconfig.command_syntax"),
                            Template.of("value", "/plot cluster resize [name]")
                    );
                    return false;
                }
                PlotId pos1;
                PlotId pos2;
                // check pos1 / pos2
                try {
                    pos1 = PlotId.fromString(args[2]);
                    pos2 = PlotId.fromString(args[3]);
                } catch (IllegalArgumentException ignored) {
                    player.sendMessage(TranslatableCaption.of("invalid.not_valid_plot_id"));
                    return false;
                }
                if (pos2.getX() < pos1.getX() || pos2.getY() < pos1.getY()) {
                    pos1 = PlotId.of(Math.min(pos1.getX(), pos2.getX()), Math.min(pos1.getY(), pos2.getY()));
                    pos2 = PlotId.of(Math.max(pos1.getX(), pos2.getX()), Math.max(pos1.getY(), pos2.getY()));
                }
                // check if in cluster
                PlotArea area = player.getApplicablePlotArea();
                if (area == null) {
                    player.sendMessage(TranslatableCaption.of("errors.not_in_plot_world"));
                    return false;
                }
                PlotCluster cluster = area.getCluster(player.getLocation());
                if (cluster == null) {
                    player.sendMessage(TranslatableCaption.of("errors.not_in_cluster"));
                    return false;
                }
                if (!cluster.hasHelperRights(player.getUUID())) {
                    if (!Permissions
                        .hasPermission(player, Permission.PERMISSION_CLUSTER_RESIZE_OTHER)) {
                        player.sendMessage(
                                TranslatableCaption.of("permission.no_permission"),
                                Template.of("node", "plots.cluster.resize.other"));
                        return false;
                    }
                }
                //check if overlap
                PlotCluster intersect = area.getFirstIntersectingCluster(pos1, pos2);
                if (intersect != null) {
                    player.sendMessage(
                            TranslatableCaption.of("cluster.cluster_intersection"),
                            Template.of("cluster", intersect.getName())
                    );
                    return false;
                }
                Set<Plot> existing = area.getPlotSelectionOwned(cluster.getP1(), cluster.getP2());
                Set<Plot> newPlots = area.getPlotSelectionOwned(pos1, pos2);
                // Set<Plot> removed = (HashSet<Plot>) existing.clone();
                Set<Plot> removed = new HashSet<>(existing);

                removed.removeAll(newPlots);
                // Check expand / shrink
                if (!removed.isEmpty()) {
                    if (!Permissions
                        .hasPermission(player, Permission.PERMISSION_CLUSTER_RESIZE_SHRINK)) {
                        player.sendMessage(
                                TranslatableCaption.of("permission.no_permission"),
                                Template.of("node", "plots.cluster.resize.shrink")
                        );
                        return false;
                    }
                }
                newPlots.removeAll(existing);
                if (!newPlots.isEmpty()) {
                    if (!Permissions
                        .hasPermission(player, Permission.PERMISSION_CLUSTER_RESIZE_EXPAND)) {
                        player.sendMessage(
                                TranslatableCaption.of("permission.no_permission"),
                                Template.of("node", "plots.cluster.resize.expand")
                        );
                        return false;
                    }
                }
                // Check allowed cluster size
                int current;
                if (Settings.Limit.GLOBAL) {
                    current = player.getPlayerClusterCount();
                } else {
                    current = player.getPlayerClusterCount(player.getLocation().getWorldName());
                }
                current -= cluster.getArea() + (1 + pos2.getX() - pos1.getX()) * (1 + pos2.getY() - pos1.getY());
                int allowed = Permissions.hasPermissionRange(player, Permission.PERMISSION_CLUSTER,
                    Settings.Limit.MAX_PLOTS);
                if (current + cluster.getArea() > allowed) {
                    player.sendMessage(
                            TranslatableCaption.of("permission.no_permission"),
                            Template.of("node", "plots.cluster" + "." + (current + cluster.getArea()))
                    );
                    return false;
                }
                // resize cluster
                DBFunc.resizeCluster(cluster, pos1, pos2);
                player.sendMessage(TranslatableCaption.of("cluster.cluster_resized"));
                return true;
            }
            case "add":
            case "inv":
            case "invite": {
                if (!Permissions.hasPermission(player, "plots.cluster.invite")) {
                    player.sendMessage(
                            TranslatableCaption.of("permission.no_permission"),
                            Template.of("node", "plots.cluster.invite")
                    );
                    return false;
                }
                if (args.length != 2) {
                    player.sendMessage(
                            TranslatableCaption.of("commandconfig.command_syntax"),
                            Template.of("value", "/plot cluster invite <player>")
                    );
                    return false;
                }
                // check if in cluster
                PlotArea area = player.getApplicablePlotArea();
                if (area == null) {
                    player.sendMessage(TranslatableCaption.of("errors.not_in_plot_world"));
                }
                PlotCluster cluster = area.getCluster(player.getLocation());
                if (cluster == null) {
                    player.sendMessage(TranslatableCaption.of("errors.not_in_cluster"));
                    return false;
                }
                if (!cluster.hasHelperRights(player.getUUID())) {
                    if (!Permissions
                        .hasPermission(player, Permission.PERMISSION_CLUSTER_INVITE_OTHER)) {
                        player.sendMessage(
                                TranslatableCaption.of("permission.no_permission"),
                                Template.of("node", Permission.PERMISSION_CLUSTER_INVITE_OTHER.toString())
                        );
                        return false;
                    }
                }

                PlotSquared.get().getImpromptuUUIDPipeline()
                    .getSingle(args[1], (uuid, throwable) -> {
                        if (throwable instanceof TimeoutException) {
                            player.sendMessage(TranslatableCaption.of("players.fetching_players_timeout"));
                        } else if (throwable != null) {
                            player.sendMessage(
                                    TranslatableCaption.of("errors.invalid_player"),
                                    Template.of("value", args[1])
                            );
                        } else {
                            if (!cluster.isAdded(uuid)) {
                                // add the user if not added
                                cluster.invited.add(uuid);
                                DBFunc.setInvited(cluster, uuid);
                                final PlotPlayer otherPlayer =
                                    PlotSquared.platform().getPlayerManager().getPlayerIfExists(uuid);
                                if (otherPlayer != null) {
                                    player.sendMessage(
                                            TranslatableCaption.of("cluster.cluster_invited"),
                                            Template.of("cluster", cluster.getName())
                                    );
                                }
                            }
                            player.sendMessage(TranslatableCaption.of("cluster.cluster_added_user"));
                        }
                    });
                return true;
            }
            case "k":
            case "remove":
            case "kick": {
                if (!Permissions.hasPermission(player, Permission.PERMISSION_CLUSTER_KICK)) {
                    player.sendMessage(
                            TranslatableCaption.of("permission.no_permission"),
                            Template.of("node", Permission.PERMISSION_CLUSTER_KICK.toString())
                    );
                    return false;
                }
                if (args.length != 2) {
                    player.sendMessage(
                            TranslatableCaption.of("commandconfig.command_syntax"),
                            Template.of("value", "/plot cluster kick <player>")
                    );
                    return false;
                }
                PlotArea area = player.getApplicablePlotArea();
                if (area == null) {
                    player.sendMessage(TranslatableCaption.of("errors.not_in_plot_world"));
                }
                PlotCluster cluster = area.getCluster(player.getLocation());
                if (cluster == null) {
                    player.sendMessage(TranslatableCaption.of("errors.not_in_cluster"));
                    return false;
                }
                if (!cluster.hasHelperRights(player.getUUID())) {
                    if (!Permissions
                        .hasPermission(player, Permission.PERMISSION_CLUSTER_KICK_OTHER)) {
                        player.sendMessage(
                                TranslatableCaption.of("permission.no_permission"),
                                Template.of("node", Permission.PERMISSION_CLUSTER_KICK_OTHER.toString())
                        );
                        return false;
                    }
                }
                // check uuid
                PlotSquared.get().getImpromptuUUIDPipeline()
                    .getSingle(args[1], (uuid, throwable) -> {
                        if (throwable instanceof TimeoutException) {
                            player.sendMessage(TranslatableCaption.of("players.fetching_players_timeout"));
                        } else if (throwable != null) {
                            player.sendMessage(
                                    TranslatableCaption.of("errors.invalid_player"),
                                    Template.of("value", args[1])
                            );
                        } else {
                            // Can't kick if the player is yourself, the owner, or not added to the cluster
                            if (uuid.equals(player.getUUID()) || uuid.equals(cluster.owner)
                                || !cluster.isAdded(uuid)) {
                                player.sendMessage(
                                        TranslatableCaption.of("cluster.cannot_kick_player"),
                                        Template.of("value", cluster.getName())
                                );
                            } else {
                                if (cluster.helpers.contains(uuid)) {
                                    cluster.helpers.remove(uuid);
                                    DBFunc.removeHelper(cluster, uuid);
                                }
                                cluster.invited.remove(uuid);
                                DBFunc.removeInvited(cluster, uuid);

                                final PlotPlayer player2 =
                                    PlotSquared.platform().getPlayerManager().getPlayerIfExists(uuid);
                                if (player2 != null) {
                                    player.sendMessage(
                                            TranslatableCaption.of("cluster.cluster_removed"),
                                            Template.of("cluster", cluster.getName())
                                    );
                                }
                                for (final Plot plot : PlotQuery.newQuery().inWorld(player2.getLocation()
                                    .getWorldName()).ownedBy(uuid)) {
                                    PlotCluster current = plot.getCluster();
                                    if (current != null && current.equals(cluster)) {
                                        plot.unclaim();
                                    }
                                }
                                player.sendMessage(TranslatableCaption.of("cluster.cluster_kicked_user"));
                            }
                        }
                    });
                return true;
            }
            case "quit":
            case "leave": {
                if (!Permissions.hasPermission(player, Permission.PERMISSION_CLUSTER_LEAVE)) {
                    player.sendMessage(
                            TranslatableCaption.of("permission.no_permission"),
                            Template.of("node", Permission.PERMISSION_CLUSTER_LEAVE.toString())
                    );
                    return false;
                }
                if (args.length != 1 && args.length != 2) {
                    player.sendMessage(
                            TranslatableCaption.of("commandconfig.command_syntax"),
                            Template.of("value", "/plot cluster leave [name]")
                    );
                    return false;
                }
                PlotArea area = player.getApplicablePlotArea();
                if (area == null) {
                    player.sendMessage(TranslatableCaption.of("errors.not_in_plot_world"));
                }
                PlotCluster cluster;
                if (args.length == 2) {
                    cluster = area.getCluster(args[1]);
                    if (cluster == null) {
                        player.sendMessage(
                                TranslatableCaption.of("cluster.invalid_cluster_name"),
                                Template.of("cluster", args[1])
                        );
                        return false;
                    }
                } else {
                    cluster = area.getCluster(player.getLocation());
                    if (cluster == null) {
                        player.sendMessage(TranslatableCaption.of("errors.not_in_cluster"));
                        return false;
                    }
                }
                UUID uuid = player.getUUID();
                if (!cluster.isAdded(uuid)) {
                    player.sendMessage(TranslatableCaption.of("cluster.cluster_not_added"));
                    return false;
                }
                if (uuid.equals(cluster.owner)) {
                    player.sendMessage(TranslatableCaption.of("cluster.cluster_cannot_leave"));
                    return false;
                }
                if (cluster.helpers.contains(uuid)) {
                    cluster.helpers.remove(uuid);
                    DBFunc.removeHelper(cluster, uuid);
                }
                cluster.invited.remove(uuid);
                DBFunc.removeInvited(cluster, uuid);
                player.sendMessage(
                        TranslatableCaption.of("cluster.cluster_removed"),
                        Template.of("cluster", cluster.getName())
                );
                for (final Plot plot : PlotQuery.newQuery().inWorld(player.getLocation().getWorldName())
                    .ownedBy(uuid)) {
                    PlotCluster current = plot.getCluster();
                    if (current != null && current.equals(cluster)) {
                        plot.unclaim();
                    }
                }
                return true;
            }
            case "members": {
                if (!Permissions.hasPermission(player, Permission.PERMISSION_CLUSTER_HELPERS)) {
                    player.sendMessage(
                            TranslatableCaption.of("permission.no_permission"),
                            Template.of("node", Permission.PERMISSION_CLUSTER_HELPERS.toString())
                    );
                    return false;
                }
                if (args.length != 3) {
                    player.sendMessage(
                            TranslatableCaption.of("commandconfig.command_syntax"),
                            Template.of("value", "/plot cluster members <add | remove> <player>")
                    );
                    return false;
                }
                PlotArea area = player.getApplicablePlotArea();
                if (area == null) {
                    player.sendMessage(TranslatableCaption.of("errors.not_in_plot_world"));
                }
                PlotCluster cluster = area.getCluster(player.getLocation());
                if (cluster == null) {
                    player.sendMessage(TranslatableCaption.of("errors.not_in_cluster"));
                    return false;
                }

                PlotSquared.get().getImpromptuUUIDPipeline()
                    .getSingle(args[2], (uuid, throwable) -> {
                        if (throwable instanceof TimeoutException) {
                            player.sendMessage(TranslatableCaption.of("players.fetching_players_timeout"));
                        } else if (throwable != null) {
                            player.sendMessage(
                                    TranslatableCaption.of("errors.invalid_player"),
                                    Template.of("value", args[2])
                            );
                        } else {
                            if (args[1].equalsIgnoreCase("add")) {
                                cluster.helpers.add(uuid);
                                DBFunc.setHelper(cluster, uuid);
                                player.sendMessage(TranslatableCaption.of("cluster.cluster_added_helper"));
                            } else if (args[1].equalsIgnoreCase("remove")) {
                                cluster.helpers.remove(uuid);
                                DBFunc.removeHelper(cluster, uuid);
                                player.sendMessage(TranslatableCaption.of("cluster.cluster_removed_helper"));
                            } else {
                                player.sendMessage(
                                        TranslatableCaption.of("commandconfig.command_syntax"),
                                        Template.of("value", "/plot cluster members <add | remove> <player>")
                                );
                            }
                        }
                    });
                return true;
            }
            case "spawn":
            case "home":
            case "tp": {
                if (!Permissions.hasPermission(player, Permission.PERMISSION_CLUSTER_TP)) {
                    player.sendMessage(
                            TranslatableCaption.of("permission.no_permission"),
                            Template.of("node", Permission.PERMISSION_CLUSTER_TP.toString())
                    );
                    return false;
                }
                if (args.length != 2) {
                    player.sendMessage(
                            TranslatableCaption.of("commandconfig.command_syntax"),
                            Template.of("value", "/plot cluster tp <name>")
                    );
                    return false;
                }
                PlotArea area = player.getApplicablePlotArea();
                if (area == null) {
                    player.sendMessage(TranslatableCaption.of("errors.not_in_plot_world"));
                    return false;
                }
                PlotCluster cluster = area.getCluster(args[1]);
                if (cluster == null) {
                    player.sendMessage(
                            TranslatableCaption.of("cluster.invalid_cluster_name"),
                            Template.of("cluster", args[1])
                    );
                    return false;
                }
                UUID uuid = player.getUUID();
                if (!cluster.isAdded(uuid)) {
                    if (!Permissions.hasPermission(player, Permission.PERMISSION_CLUSTER_TP_OTHER)) {
                        player.sendMessage(
                                TranslatableCaption.of("permission.no_permission"),
                                Template.of("node", Permission.PERMISSION_CLUSTER_TP_OTHER.toString())
                        );
                        return false;
                    }
                }
                cluster.getHome(home -> player.teleport(home, TeleportCause.COMMAND));
                player.sendMessage(TranslatableCaption.of("cluster.cluster_teleporting"));
            }
            case "i":
            case "info":
            case "show":
            case "information": {
                if (!Permissions.hasPermission(player, Permission.PERMISSION_CLUSTER_INFO)) {
                    player.sendMessage(
                            TranslatableCaption.of("permission.no_permission"),
                            Template.of("node", Permission.PERMISSION_CLUSTER_TP.toString())
                    );
                    return false;
                }
                if (args.length != 1 && args.length != 2) {
                    player.sendMessage(
                            TranslatableCaption.of("commandconfig.command_syntax"),
                            Template.of("value", "/plot cluster info [name]")
                    );
                }
                PlotArea area = player.getApplicablePlotArea();
                if (area == null) {
                    player.sendMessage(TranslatableCaption.of("errors.not_in_plot_world"));
                }
                PlotCluster cluster;
                if (args.length == 2) {
                    cluster = area.getCluster(args[1]);
                    player.sendMessage(
                            TranslatableCaption.of("cluster.invalid_cluster_name"),
                            Template.of("cluster", args[1])
                    );
                } else {
                    cluster = area.getCluster(player.getLocation());
                    if (cluster == null) {
                        player.sendMessage(TranslatableCaption.of("errors.not_in_cluster"));
                        return false;
                    }
                }
                String id = cluster.toString();

                PlotSquared.get().getImpromptuUUIDPipeline()
                    .getSingle(cluster.owner, (username, throwable) -> {
                        if (throwable instanceof TimeoutException) {
                            player.sendMessage(TranslatableCaption.of("players.fetching_players_timeout"));
                        } else {
                            final String owner;
                            if (username == null) {
                                owner = "unknown";
                            } else {
                                owner = username;
                            }
                            String name = cluster.getName();
                            String size = (cluster.getP2().getX() - cluster.getP1().getX() + 1) + "x" + (
                                cluster.getP2().getY() - cluster.getP1().getY() + 1);
                            String rights = cluster.isAdded(player.getUUID()) + "";
                            Caption message = TranslatableCaption.of("cluster.cluster_info");
                            Template idTemplate = Template.of("id", id);
                            Template ownerTemplate = Template.of("owner", owner);
                            Template nameTemplate = Template.of("name", name);
                            Template sizeTemplate = Template.of("size", size);
                            Template rightsTemplate = Template.of("rights", rights);
                            player.sendMessage(message, idTemplate, ownerTemplate, nameTemplate, sizeTemplate, rightsTemplate);
                        }
                    });
                return true;
            }
            case "sh":
            case "setspawn":
            case "sethome":
                if (!Permissions.hasPermission(player, Permission.PERMISSION_CLUSTER_SETHOME)) {
                    player.sendMessage(
                            TranslatableCaption.of("permission.no_permission"),
                            Template.of("node", Permission.PERMISSION_CLUSTER_SETHOME.toString())
                    );
                    return false;
                }
                if (args.length != 1 && args.length != 2) {
                    player.sendMessage(
                            TranslatableCaption.of("commandconfig.command_syntax"),
                            Template.of("value", "/plot cluster sethome")
                    );
                    return false;
                }
                PlotArea area = player.getApplicablePlotArea();
                if (area == null) {
                    player.sendMessage(TranslatableCaption.of("errors.not_in_plot_world"));
                }
                PlotCluster cluster = area.getCluster(player.getLocation());
                if (cluster == null) {
                    player.sendMessage(TranslatableCaption.of("errors.not_in_cluster"));
                    return false;
                }
                if (!cluster.hasHelperRights(player.getUUID())) {
                    if (!Permissions
                        .hasPermission(player, Permission.PERMISSION_CLUSTER_SETHOME_OTHER)) {
                        player.sendMessage(
                                TranslatableCaption.of("permission.no_permission"),
                                Template.of("node", Permission.PERMISSION_CLUSTER_SETHOME_OTHER.toString())
                        );
                        return false;
                    }
                }
                Location base = cluster.getClusterBottom();
                Location relative = player.getLocation().subtract(base.getX(), 0, base.getZ());
                BlockLoc blockloc = new BlockLoc(relative.getX(), relative.getY(), relative.getZ());
                cluster.settings.setPosition(blockloc);
                DBFunc.setPosition(cluster,
                    relative.getX() + "," + relative.getY() + "," + relative.getZ());
                player.sendMessage(TranslatableCaption.of("position.position_set"));
        }
        player.sendMessage(TranslatableCaption.of("cluster.cluster_available_args"));
        return false;
    }
}
