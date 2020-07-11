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
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.plotsquared.bukkit.listener;

import com.destroystokyo.paper.MaterialTags;
import com.google.common.base.Charsets;
import com.plotsquared.bukkit.player.BukkitPlayer;
import com.plotsquared.bukkit.util.BukkitEntityUtil;
import com.plotsquared.bukkit.util.BukkitUtil;
import com.plotsquared.bukkit.util.UpdateUtility;
import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.configuration.Captions;
import com.plotsquared.core.configuration.Settings;
import com.plotsquared.core.listener.PlayerBlockEventType;
import com.plotsquared.core.listener.PlotListener;
import com.plotsquared.core.location.Location;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.PlotArea;
import com.plotsquared.core.plot.PlotId;
import com.plotsquared.core.plot.PlotInventory;
import com.plotsquared.core.plot.flag.FlagContainer;
import com.plotsquared.core.plot.flag.implementations.AnimalInteractFlag;
import com.plotsquared.core.plot.flag.implementations.BlockedCmdsFlag;
import com.plotsquared.core.plot.flag.implementations.ChatFlag;
import com.plotsquared.core.plot.flag.implementations.DenyTeleportFlag;
import com.plotsquared.core.plot.flag.implementations.DoneFlag;
import com.plotsquared.core.plot.flag.implementations.DropProtectionFlag;
import com.plotsquared.core.plot.flag.implementations.HangingBreakFlag;
import com.plotsquared.core.plot.flag.implementations.HangingPlaceFlag;
import com.plotsquared.core.plot.flag.implementations.HostileInteractFlag;
import com.plotsquared.core.plot.flag.implementations.ItemDropFlag;
import com.plotsquared.core.plot.flag.implementations.KeepInventoryFlag;
import com.plotsquared.core.plot.flag.implementations.MiscInteractFlag;
import com.plotsquared.core.plot.flag.implementations.PlayerInteractFlag;
import com.plotsquared.core.plot.flag.implementations.PreventCreativeCopyFlag;
import com.plotsquared.core.plot.flag.implementations.TamedInteractFlag;
import com.plotsquared.core.plot.flag.implementations.UntrustedVisitFlag;
import com.plotsquared.core.plot.flag.implementations.UseFlag;
import com.plotsquared.core.plot.flag.implementations.VehicleBreakFlag;
import com.plotsquared.core.plot.flag.implementations.VehicleUseFlag;
import com.plotsquared.core.plot.flag.implementations.VillagerInteractFlag;
import com.plotsquared.core.plot.flag.types.BlockTypeWrapper;
import com.plotsquared.core.plot.message.PlotMessage;
import com.plotsquared.core.util.MainUtil;
import com.plotsquared.core.util.MathMan;
import com.plotsquared.core.util.Permissions;
import com.plotsquared.core.util.PremiumVerification;
import com.plotsquared.core.util.RegExUtil;
import com.plotsquared.core.util.entity.EntityCategories;
import com.plotsquared.core.util.task.TaskManager;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.world.block.BlockType;
import io.papermc.lib.PaperLib;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Tameable;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerEggThrowEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.event.vehicle.VehicleEntityCollisionEvent;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.help.HelpTopic;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Player Events involving plots.
 */
@SuppressWarnings("unused")
public class PlayerEventListener extends PlotListener implements Listener {

    // To prevent recursion
    private boolean tmpTeleport = true;
    private Field fieldPlayer;
    private PlayerMoveEvent moveTmp;
    private String internalVersion;

    {
        try {
            fieldPlayer = PlayerEvent.class.getDeclaredField("player");
            fieldPlayer.setAccessible(true);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    @EventHandler public void onVehicleEntityCollision(VehicleEntityCollisionEvent e) {
        if (e.getVehicle().getType() == EntityType.BOAT) {
            Location location = BukkitUtil.getLocation(e.getEntity());
            if (location.isPlotArea()) {
                if (e.getEntity() instanceof Player) {
                    PlotPlayer<Player> player = BukkitUtil.getPlayer((Player) e.getEntity());
                    Plot plot = player.getCurrentPlot();
                    if (plot != null) {
                        if (!plot.isAdded(player.getUUID())) {
                            //Here the event is only canceled if the player is not the owner
                            //of the property on which he is located.
                            e.setCancelled(true);
                        }
                    } else {
                        e.setCancelled(true);
                    }
                } else {
                    //Here the event is cancelled too, otherwise you can move the
                    //boat with EchoPets or other mobs running around on the plot.
                    e.setCancelled(true);
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void playerCommand(PlayerCommandPreprocessEvent event) {
        String msg = event.getMessage().toLowerCase().replaceAll("/", "").trim();
        if (msg.isEmpty()) {
            return;
        }
        Player player = event.getPlayer();
        PlotPlayer<Player> plotPlayer = BukkitUtil.getPlayer(player);
        Location location = plotPlayer.getLocation();
        PlotArea area = location.getPlotArea();
        if (area == null) {
            return;
        }
        String[] parts = msg.split(" ");
        Plot plot = plotPlayer.getCurrentPlot();
        // Check WorldEdit
        switch (parts[0].toLowerCase()) {
            case "up":
            case "/up":
            case "worldedit:up":
            case "worldedit:/up":
                if (plot == null || (!plot.isAdded(plotPlayer.getUUID()) && !Permissions
                    .hasPermission(plotPlayer, Captions.PERMISSION_ADMIN_BUILD_OTHER, true))) {
                    event.setCancelled(true);
                    return;
                }
        }
        if (plot == null && !area.isRoadFlags()) {
            return;
        }

        List<String> blockedCommands = plot != null ?
            plot.getFlag(BlockedCmdsFlag.class) :
            area.getFlag(BlockedCmdsFlag.class);
        if (!blockedCommands.isEmpty() && !Permissions
            .hasPermission(plotPlayer, Captions.PERMISSION_ADMIN_INTERACT_BLOCKED_CMDS)) {
            String part = parts[0];
            if (parts[0].contains(":")) {
                part = parts[0].split(":")[1];
                msg = msg.replace(parts[0].split(":")[0] + ':', "");
            }
            String s1 = part;
            List<String> aliases = new ArrayList<>();
            for (HelpTopic cmdLabel : Bukkit.getServer().getHelpMap().getHelpTopics()) {
                if (part.equals(cmdLabel.getName())) {
                    break;
                }
                String label = cmdLabel.getName().replaceFirst("/", "");
                if (aliases.contains(label)) {
                    continue;
                }
                PluginCommand p;
                if ((p = Bukkit.getPluginCommand(label)) != null) {
                    for (String a : p.getAliases()) {
                        if (aliases.contains(a)) {
                            continue;
                        }
                        aliases.add(a);
                        a = a.replaceFirst("/", "");
                        if (!a.equals(label) && a.equals(part)) {
                            part = label;
                            break;
                        }
                    }
                }
            }
            if (!s1.equals(part)) {
                msg = msg.replace(s1, part);
            }
            for (String s : blockedCommands) {
                Pattern pattern;
                if (!RegExUtil.compiledPatterns.containsKey(s)) {
                    RegExUtil.compiledPatterns.put(s, pattern = Pattern.compile(s));
                } else {
                    pattern = RegExUtil.compiledPatterns.get(s);
                }
                if (pattern.matcher(msg).matches()) {
                    String perm;
                    if (plot != null && plot.isAdded(plotPlayer.getUUID())) {
                        perm = "plots.admin.command.blocked-cmds.shared";
                    } else {
                        perm = "plots.admin.command.blocked-cmds.road";
                    }
                    if (!Permissions.hasPermission(plotPlayer, perm)) {
                        MainUtil.sendMessage(plotPlayer, Captions.COMMAND_BLOCKED);
                        event.setCancelled(true);
                    }
                    return;
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPreLoin(final AsyncPlayerPreLoginEvent event) {
        final UUID uuid;
        if (Settings.UUID.OFFLINE) {
            if (Settings.UUID.FORCE_LOWERCASE) {
                uuid = UUID.nameUUIDFromBytes(
                    ("OfflinePlayer:" + event.getName().toLowerCase()).getBytes(Charsets.UTF_8));
            } else {
                uuid = UUID.nameUUIDFromBytes(
                    ("OfflinePlayer:" + event.getName()).getBytes(Charsets.UTF_8));
            }
        } else {
            uuid = event.getUniqueId();
        }
        PlotSquared.get().getImpromptuUUIDPipeline().storeImmediately(event.getName(), uuid);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onConnect(PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        BukkitUtil.removePlayer(player.getUniqueId());
        final PlotPlayer<Player> pp = BukkitUtil.getPlayer(player);

        Location location = pp.getLocation();
        PlotArea area = location.getPlotArea();
        if (area != null) {
            Plot plot = area.getPlot(location);
            if (plot != null) {
                plotEntry(pp, plot);
            }
        }
        // Delayed

        // Async
        TaskManager.runTaskLaterAsync(() -> {
            if (!player.hasPlayedBefore() && player.isOnline()) {
                player.saveData();
            }
            PlotSquared.get().getEventDispatcher().doJoinTask(pp);
        }, 20);

        if (pp.hasPermission(Captions.PERMISSION_ADMIN_UPDATE_NOTIFICATION.getTranslated())
            && Settings.Enabled_Components.UPDATE_NOTIFICATIONS && PremiumVerification.isPremium()
            && UpdateUtility.hasUpdate) {
            new PlotMessage("-----------------------------------").send(pp);
            new PlotMessage(Captions.PREFIX + "There appears to be a PlotSquared update available!")
                .color("$1").send(pp);
            new PlotMessage(
                Captions.PREFIX + "&6You are running version " + UpdateUtility.internalVersion
                    .versionString() + ", &6latest version is " + UpdateUtility.spigotVersion)
                .color("$1").send(pp);
            new PlotMessage(Captions.PREFIX + "Download at:").color("$1").send(pp);
            player.sendMessage("    https://www.spigotmc.org/resources/77506/updates");
            new PlotMessage("-----------------------------------").send(pp);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void playerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        PlotPlayer<Player> pp = BukkitUtil.getPlayer(player);
        PlotSquared.get().getEventDispatcher().doRespawnTask(pp);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        BukkitPlayer pp = BukkitUtil.getPlayer(player);
        Plot lastPlot = pp.getMeta(PlotPlayer.META_LAST_PLOT);
        org.bukkit.Location to = event.getTo();
        //noinspection ConstantConditions
        if (to != null) {
            Location location = BukkitUtil.getLocation(to);
            PlotArea area = location.getPlotArea();
            if (area == null) {
                if (lastPlot != null) {
                    plotExit(pp, lastPlot);
                    pp.deleteMeta(PlotPlayer.META_LAST_PLOT);
                }
                pp.deleteMeta(PlotPlayer.META_LOCATION);
                return;
            }
            Plot plot = area.getPlot(location);
            if (plot != null) {
                final boolean result = DenyTeleportFlag.allowsTeleport(pp, plot);
                // there is one possibility to still allow teleportation:
                // to is identical to the plot's home location, and untrusted-visit is true
                // i.e. untrusted-visit can override deny-teleport
                // this is acceptable, because otherwise it wouldn't make sense to have both flags set
                if (!result && !(plot.getFlag(UntrustedVisitFlag.class) && plot.getHomeSynchronous()
                    .equals(BukkitUtil.getLocationFull(to)))) {
                    MainUtil.sendMessage(pp, Captions.NO_PERMISSION_EVENT,
                        Captions.PERMISSION_ADMIN_ENTRY_DENIED);
                    event.setCancelled(true);
                }
            }
        }
        playerMove(event);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void vehicleMove(VehicleMoveEvent event) throws IllegalAccessException {
        final org.bukkit.Location from = event.getFrom();
        final org.bukkit.Location to = event.getTo();

        int toX, toZ;
        if ((toX = MathMan.roundInt(to.getX())) != MathMan.roundInt(from.getX())
            | (toZ = MathMan.roundInt(to.getZ())) != MathMan.roundInt(from.getZ())) {
            Vehicle vehicle = event.getVehicle();

            // Check allowed
            if (!vehicle.getPassengers().isEmpty()) {
                Entity passenger = vehicle.getPassengers().get(0);

                if (passenger instanceof Player) {
                    final Player player = (Player) passenger;
                    // reset
                    if (moveTmp == null) {
                        moveTmp = new PlayerMoveEvent(null, from, to);
                    }
                    moveTmp.setFrom(from);
                    moveTmp.setTo(to);
                    moveTmp.setCancelled(false);
                    fieldPlayer.set(moveTmp, player);

                    List<Entity> passengers = vehicle.getPassengers();

                    this.playerMove(moveTmp);
                    org.bukkit.Location dest;
                    if (moveTmp.isCancelled()) {
                        dest = from;
                    } else if (MathMan.roundInt(moveTmp.getTo().getX()) != toX
                        || MathMan.roundInt(moveTmp.getTo().getZ()) != toZ) {
                        dest = to;
                    } else {
                        dest = null;
                    }
                    if (dest != null) {
                        vehicle.eject();
                        vehicle.setVelocity(new Vector(0d, 0d, 0d));
                        PaperLib.teleportAsync(vehicle, dest);
                        passengers.forEach(vehicle::addPassenger);
                        return;
                    }
                }
                if (Settings.Enabled_Components.KILL_ROAD_VEHICLES) {
                    final com.sk89q.worldedit.world.entity.EntityType entityType =
                        BukkitAdapter.adapt(vehicle.getType());
                    // Horses etc are vehicles, but they're also animals
                    // so this filters out all living entities
                    if (EntityCategories.VEHICLE.contains(entityType) && !EntityCategories.ANIMAL
                        .contains(entityType)) {
                        List<MetadataValue> meta = vehicle.getMetadata("plot");
                        Plot toPlot = BukkitUtil.getLocation(to).getPlot();
                        if (!meta.isEmpty()) {
                            Plot origin = (Plot) meta.get(0).value();
                            if (origin != null && !origin.getBasePlot(false).equals(toPlot)) {
                                vehicle.remove();
                            }
                        } else if (toPlot != null) {
                            vehicle.setMetadata("plot",
                                new FixedMetadataValue((Plugin) PlotSquared.get().IMP, toPlot));
                        }
                    }
                }
            }

        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void playerMove(PlayerMoveEvent event) {
        org.bukkit.Location from = event.getFrom();
        org.bukkit.Location to = event.getTo();
        int x2;
        if (MathMan.roundInt(from.getX()) != (x2 = MathMan.roundInt(to.getX()))) {
            Player player = event.getPlayer();
            BukkitPlayer pp = BukkitUtil.getPlayer(player);
            // Cancel teleport
            if (TaskManager.TELEPORT_QUEUE.remove(pp.getName())) {
                MainUtil.sendMessage(pp, Captions.TELEPORT_FAILED);
            }
            // Set last location
            Location location = BukkitUtil.getLocation(to);
            pp.setMeta(PlotPlayer.META_LOCATION, location);
            PlotArea area = location.getPlotArea();
            if (area == null) {
                pp.deleteMeta(PlotPlayer.META_LAST_PLOT);
                return;
            }
            Plot now = area.getPlot(location);
            Plot lastPlot = pp.getMeta(PlotPlayer.META_LAST_PLOT);
            if (now == null) {
                if (lastPlot != null && !plotExit(pp, lastPlot) && this.tmpTeleport && !pp
                    .getMeta("kick", false)) {
                    MainUtil.sendMessage(pp, Captions.NO_PERMISSION_EVENT,
                        Captions.PERMISSION_ADMIN_EXIT_DENIED);
                    this.tmpTeleport = false;
                    if (lastPlot.equals(BukkitUtil.getLocation(from).getPlot())) {
                        player.teleport(from);
                    } else {
                        player.teleport(player.getWorld().getSpawnLocation());
                    }
                    this.tmpTeleport = true;
                    event.setCancelled(true);
                    return;
                }
            } else if (now.equals(lastPlot)) {
                ForceFieldListener.handleForcefield(player, pp, now);
            } else if (!plotEntry(pp, now) && this.tmpTeleport) {
                MainUtil.sendMessage(pp, Captions.NO_PERMISSION_EVENT,
                    Captions.PERMISSION_ADMIN_ENTRY_DENIED);
                this.tmpTeleport = false;
                to.setX(from.getBlockX());
                to.setY(from.getBlockY());
                to.setZ(from.getBlockZ());
                player.teleport(event.getTo());
                this.tmpTeleport = true;
                return;
            }
            int border = area.getBorder();
            if (x2 > border && this.tmpTeleport) {
                to.setX(border - 1);
                this.tmpTeleport = false;
                player.teleport(event.getTo());
                this.tmpTeleport = true;
                MainUtil.sendMessage(pp, Captions.BORDER);
            }
            if (x2 < -border && this.tmpTeleport) {
                to.setX(-border + 1);
                this.tmpTeleport = false;
                player.teleport(event.getTo());
                this.tmpTeleport = true;
                MainUtil.sendMessage(pp, Captions.BORDER);
            }
        }
        int z2;
        if (MathMan.roundInt(from.getZ()) != (z2 = MathMan.roundInt(to.getZ()))) {
            Player player = event.getPlayer();
            BukkitPlayer pp = BukkitUtil.getPlayer(player);
            // Cancel teleport
            if (TaskManager.TELEPORT_QUEUE.remove(pp.getName())) {
                MainUtil.sendMessage(pp, Captions.TELEPORT_FAILED);
            }
            // Set last location
            Location location = BukkitUtil.getLocation(to);
            pp.setMeta(PlotPlayer.META_LOCATION, location);
            PlotArea area = location.getPlotArea();
            if (area == null) {
                pp.deleteMeta(PlotPlayer.META_LAST_PLOT);
                return;
            }
            Plot now = area.getPlot(location);
            Plot lastPlot = pp.getMeta(PlotPlayer.META_LAST_PLOT);
            if (now == null) {
                if (lastPlot != null && !plotExit(pp, lastPlot) && this.tmpTeleport && !pp
                    .getMeta("kick", false)) {
                    MainUtil.sendMessage(pp, Captions.NO_PERMISSION_EVENT,
                        Captions.PERMISSION_ADMIN_EXIT_DENIED);
                    this.tmpTeleport = false;
                    if (lastPlot.equals(BukkitUtil.getLocation(from).getPlot())) {
                        player.teleport(from);
                    } else {
                        player.teleport(player.getWorld().getSpawnLocation());
                    }
                    this.tmpTeleport = true;
                    event.setCancelled(true);
                    return;
                }
            } else if (now.equals(lastPlot)) {
                ForceFieldListener.handleForcefield(player, pp, now);
            } else if (!plotEntry(pp, now) && this.tmpTeleport) {
                MainUtil.sendMessage(pp, Captions.NO_PERMISSION_EVENT,
                    Captions.PERMISSION_ADMIN_ENTRY_DENIED);
                this.tmpTeleport = false;
                player.teleport(from);
                to.setX(from.getBlockX());
                to.setY(from.getBlockY());
                to.setZ(from.getBlockZ());
                player.teleport(event.getTo());
                this.tmpTeleport = true;
                return;
            }
            int border = area.getBorder();
            if (z2 > border && this.tmpTeleport) {
                to.setZ(border - 1);
                this.tmpTeleport = false;
                player.teleport(event.getTo());
                this.tmpTeleport = true;
                MainUtil.sendMessage(pp, Captions.BORDER);
            } else if (z2 < -border && this.tmpTeleport) {
                to.setZ(-border + 1);
                this.tmpTeleport = false;
                player.teleport(event.getTo());
                this.tmpTeleport = true;
                MainUtil.sendMessage(pp, Captions.BORDER);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW) public void onChat(AsyncPlayerChatEvent event) {
        if (event.isCancelled()) {
            return;
        }

        BukkitPlayer plotPlayer = BukkitUtil.getPlayer(event.getPlayer());
        Location location = plotPlayer.getLocation();
        PlotArea area = location.getPlotArea();
        if (area == null) {
            return;
        }
        Plot plot = area.getPlot(location);
        if (plot == null) {
            return;
        }
        if (!((plot.getFlag(ChatFlag.class) && area.isPlotChat() && plotPlayer.getAttribute("chat"))
            || area.isForcingPlotChat())) {
            return;
        }
        if (plot.isDenied(plotPlayer.getUUID()) && !Permissions
            .hasPermission(plotPlayer, Captions.PERMISSION_ADMIN_CHAT_BYPASS)) {
            return;
        }
        event.setCancelled(true);
        String message = event.getMessage();
        String format = Captions.PLOT_CHAT_FORMAT.getTranslated();
        String sender = event.getPlayer().getDisplayName();
        PlotId id = plot.getId();
        Set<Player> recipients = event.getRecipients();
        recipients.clear();
        Set<Player> spies = new HashSet<>();
        for (final PlotPlayer<?> pp : PlotSquared.imp().getPlayerManager().getPlayers()) {
            if (pp.getAttribute("chatspy")) {
                spies.add(((BukkitPlayer) pp).player);
            } else {
                Plot current = pp.getCurrentPlot();
                if (current != null && current.getBasePlot(false).equals(plot)) {
                    recipients.add(((BukkitPlayer) pp).player);
                }
            }
        }
        String partial = ChatColor.translateAlternateColorCodes('&',
            format.replace("%plot_id%", id.getX() + ";" + id.getY()).replace("%sender%", sender));
        if (plotPlayer.hasPermission("plots.chat.color")) {
            message = Captions.color(message);
        }
        String full = partial.replace("%msg%", message);
        for (Player receiver : recipients) {
            receiver.sendMessage(full);
        }
        if (!spies.isEmpty()) {
            String spyMessage = Captions.PLOT_CHAT_SPY_FORMAT.getTranslated()
                .replace("%plot_id%", id.getX() + ";" + id.getY()).replace("%sender%", sender)
                .replace("%msg%", message);
            for (Player player : spies) {
                player.sendMessage(spyMessage);
            }
        }
        PlotSquared.debug(full);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onWorldChanged(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        BukkitPlayer pp = BukkitUtil.getPlayer(player);
        // Delete last location
        Plot plot = (Plot) pp.deleteMeta(PlotPlayer.META_LAST_PLOT);
        pp.deleteMeta(PlotPlayer.META_LOCATION);
        if (plot != null) {
            plotExit(pp, plot);
        }
        if (PlotSquared.get().worldedit != null) {
            if (!Permissions.hasPermission(pp, Captions.PERMISSION_WORLDEDIT_BYPASS)) {
                if (pp.getAttribute("worldedit")) {
                    pp.removeAttribute("worldedit");
                }
            }
        }
        if (Settings.Enabled_Components.PERMISSION_CACHE) {
            pp.deleteMeta("perm");
        }
        Location location = pp.getLocation();
        PlotArea area = location.getPlotArea();
        if (location.isPlotArea()) {
            plot = location.getPlot();
            if (plot != null) {
                plotEntry(pp, plot);
            }
        }
    }

    @SuppressWarnings("deprecation")
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        /*if (!event.isLeftClick() || (event.getAction() != InventoryAction.PLACE_ALL) || event
            .isShiftClick()) {
            return;
        }*/
        HumanEntity entity = event.getWhoClicked();
        if (!(entity instanceof Player) || !PlotSquared.get()
            .hasPlotArea(entity.getWorld().getName())) {
            return;
        }

        HumanEntity clicker = event.getWhoClicked();
        if (!(clicker instanceof Player)) {
            return;
        }
        Player player = (Player) clicker;
        BukkitPlayer pp = BukkitUtil.getPlayer(player);
        final PlotInventory inventory = PlotInventory.getOpenPlotInventory(pp);
        if (inventory != null && event.getRawSlot() == event.getSlot()) {
            if (!inventory.onClick(event.getSlot())) {
                event.setResult(Event.Result.DENY);
                event.setCancelled(true);
                inventory.close();
            }
        }
        PlayerInventory inv = player.getInventory();
        int slot = inv.getHeldItemSlot();
        if ((slot > 8) || !event.getEventName().equals("InventoryCreativeEvent")) {
            return;
        }
        ItemStack current = inv.getItemInHand();
        ItemStack newItem = event.getCursor();
        if (newItem == null) {
            return;
        }
        ItemMeta newMeta = newItem.getItemMeta();
        ItemMeta oldMeta = newItem.getItemMeta();

        if (event.getClick() == ClickType.CREATIVE) {
            final Plot plot = pp.getCurrentPlot();
            if (plot != null) {
                if (plot.getFlag(PreventCreativeCopyFlag.class) && !plot
                    .isAdded(player.getUniqueId()) && !Permissions
                    .hasPermission(pp, Captions.PERMISSION_ADMIN_INTERACT_OTHER)) {
                    final ItemStack newStack =
                        new ItemStack(newItem.getType(), newItem.getAmount());
                    event.setCursor(newStack);
                    plot.debug(player.getName()
                        + " could not creative-copy an item because prevent-creative-copy = true");
                }
            } else {
                PlotArea area = pp.getPlotAreaAbs();
                if (area != null && area.isRoadFlags() && area
                    .getRoadFlag(PreventCreativeCopyFlag.class)) {
                    final ItemStack newStack =
                        new ItemStack(newItem.getType(), newItem.getAmount());
                    event.setCursor(newStack);
                }
            }
            return;
        }

        String newLore = "";
        if (newMeta != null) {
            List<String> lore = newMeta.getLore();
            if (lore != null) {
                newLore = lore.toString();
            }
        }
        String oldLore = "";
        if (oldMeta != null) {
            List<String> lore = oldMeta.getLore();
            if (lore != null) {
                oldLore = lore.toString();
            }
        }
        if (!"[(+NBT)]".equals(newLore) || (current.equals(newItem) && newLore.equals(oldLore))) {
            switch (newItem.getType()) {
                case LEGACY_BANNER:
                case PLAYER_HEAD:
                    if (newMeta != null) {
                        break;
                    }
                default:
                    return;
            }
        }
        Block block = player.getTargetBlock(null, 7);
        org.bukkit.block.BlockState state = block.getState();
        Material stateType = state.getType();
        Material itemType = newItem.getType();
        if (stateType != itemType) {
            switch (stateType) {
                case LEGACY_STANDING_BANNER:
                case LEGACY_WALL_BANNER:
                    if (itemType == Material.LEGACY_BANNER) {
                        break;
                    }
                case LEGACY_SKULL:
                    if (itemType == Material.LEGACY_SKULL_ITEM) {
                        break;
                    }
                default:
                    return;
            }
        }
        Location location = BukkitUtil.getLocation(state.getLocation());
        PlotArea area = location.getPlotArea();
        if (area == null) {
            return;
        }
        Plot plot = area.getPlotAbs(location);
        boolean cancelled = false;
        if (plot == null) {
            if (!Permissions.hasPermission(pp, "plots.admin.interact.road")) {
                MainUtil.sendMessage(pp, Captions.NO_PERMISSION_EVENT, "plots.admin.interact.road");
                cancelled = true;
            }
        } else if (!plot.hasOwner()) {
            if (!Permissions.hasPermission(pp, "plots.admin.interact.unowned")) {
                MainUtil
                    .sendMessage(pp, Captions.NO_PERMISSION_EVENT, "plots.admin.interact.unowned");
                cancelled = true;
            }
        } else {
            UUID uuid = pp.getUUID();
            if (!plot.isAdded(uuid)) {
                if (!Permissions.hasPermission(pp, "plots.admin.interact.other")) {
                    MainUtil.sendMessage(pp, Captions.NO_PERMISSION_EVENT,
                        "plots.admin.interact.other");
                    cancelled = true;
                }
            }
        }
        if (cancelled) {
            if ((current.getType() == newItem.getType()) && (current.getDurability() == newItem
                .getDurability())) {
                event.setCursor(
                    new ItemStack(newItem.getType(), newItem.getAmount(), newItem.getDurability()));
                event.setCancelled(true);
                return;
            }
            event.setCursor(
                new ItemStack(newItem.getType(), newItem.getAmount(), newItem.getDurability()));
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInteract(PlayerInteractAtEntityEvent e) {
        Entity entity = e.getRightClicked();
        if (!(entity instanceof ArmorStand) && !(entity instanceof ItemFrame)) {
            return;
        }
        Location location = BukkitUtil.getLocation(e.getRightClicked().getLocation());
        PlotArea area = location.getPlotArea();
        if (area == null) {
            return;
        }
        EntitySpawnListener.testNether(entity);
        Plot plot = location.getPlotAbs();
        BukkitPlayer pp = BukkitUtil.getPlayer(e.getPlayer());
        if (plot == null) {
            if (!area.isRoadFlags() && !area.getRoadFlag(MiscInteractFlag.class) && !Permissions
                .hasPermission(pp, "plots.admin.interact.road")) {
                MainUtil.sendMessage(pp, Captions.NO_PERMISSION_EVENT, "plots.admin.interact.road");
                e.setCancelled(true);
            }
        } else {
            if (Settings.Done.RESTRICT_BUILDING && DoneFlag.isDone(plot)) {
                if (!Permissions.hasPermission(pp, Captions.PERMISSION_ADMIN_BUILD_OTHER)) {
                    MainUtil.sendMessage(pp, Captions.NO_PERMISSION_EVENT,
                        Captions.PERMISSION_ADMIN_BUILD_OTHER);
                    e.setCancelled(true);
                    return;
                }
            }
            if (!plot.hasOwner()) {
                if (!Permissions.hasPermission(pp, "plots.admin.interact.unowned")) {
                    MainUtil.sendMessage(pp, Captions.NO_PERMISSION_EVENT,
                        "plots.admin.interact.unowned");
                    e.setCancelled(true);
                }
            } else {
                UUID uuid = pp.getUUID();
                if (plot.isAdded(uuid)) {
                    return;
                }
                if (plot.getFlag(MiscInteractFlag.class)) {
                    return;
                }
                if (!Permissions.hasPermission(pp, "plots.admin.interact.other")) {
                    MainUtil.sendMessage(pp, Captions.NO_PERMISSION_EVENT,
                        "plots.admin.interact.other");
                    e.setCancelled(true);
                    plot.debug(pp.getName() + " could not interact with " + entity.getType()
                        + " bcause misc-interact = false");
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onCancelledInteract(PlayerInteractEvent event) {
        if (event.isCancelled() && event.getAction() == Action.RIGHT_CLICK_AIR) {
            Player player = event.getPlayer();
            BukkitPlayer pp = BukkitUtil.getPlayer(player);
            PlotArea area = pp.getPlotAreaAbs();
            if (area == null) {
                return;
            }
            if (event.getAction() == Action.RIGHT_CLICK_AIR) {
                Material item = event.getMaterial();
                if (item.toString().toLowerCase().endsWith("_egg")) {
                    event.setCancelled(true);
                    event.setUseItemInHand(Event.Result.DENY);
                }
            }
            ItemStack hand = player.getInventory().getItemInMainHand();
            ItemStack offHand = player.getInventory().getItemInOffHand();
            Material type = hand.getType();
            Material offType = offHand.getType();
            if (type == Material.AIR) {
                type = offType;
            }
            if (type.toString().toLowerCase().endsWith("_egg")) {
                Block block = player.getTargetBlockExact(5, FluidCollisionMode.SOURCE_ONLY);
                if (block != null && block.getType() != Material.AIR) {
                    Location location = BukkitUtil.getLocation(block.getLocation());
                    if (!PlotSquared.get().getEventDispatcher()
                        .checkPlayerBlockEvent(pp, PlayerBlockEventType.SPAWN_MOB, location, null,
                            true)) {
                        event.setCancelled(true);
                        event.setUseItemInHand(Event.Result.DENY);
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        BukkitPlayer pp = BukkitUtil.getPlayer(player);
        PlotArea area = pp.getPlotAreaAbs();
        if (area == null) {
            return;
        }
        PlayerBlockEventType eventType = null;
        BlockType blocktype1;
        Block block = event.getClickedBlock();
        if (block == null) {
            return;
        }
        Location location = BukkitUtil.getLocation(block.getLocation());
        Action action = event.getAction();
        outer:
        switch (action) {
            case PHYSICAL: {
                eventType = PlayerBlockEventType.TRIGGER_PHYSICAL;
                blocktype1 = BukkitAdapter.asBlockType(block.getType());
                System.out.println("cc");
                break;
            }
            //todo rearrange the right click code. it is all over the place.
            case RIGHT_CLICK_BLOCK: {
                Material blockType = block.getType();
                eventType = PlayerBlockEventType.INTERACT_BLOCK;
                blocktype1 = BukkitAdapter.asBlockType(block.getType());

                if (blockType.isInteractable()) {
                    if (!player.isSneaking()) {
                        break;
                    }
                    ItemStack hand = player.getInventory().getItemInMainHand();
                    ItemStack offHand = player.getInventory().getItemInOffHand();

                    // sneaking players interact with blocks if both hands are empty
                    if (hand.getType() == Material.AIR && offHand.getType() == Material.AIR) {
                        break;
                    }
                }

                Material type = event.getMaterial();

                // in the following, lb needs to have the material of the item in hand i.e. type
                switch (type) {
                    case REDSTONE:
                    case STRING:
                    case PUMPKIN_SEEDS:
                    case MELON_SEEDS:
                    case COCOA_BEANS:
                    case WHEAT_SEEDS:
                    case BEETROOT_SEEDS:
                    case SWEET_BERRIES:
                        return;
                    default:
                        //eventType = PlayerBlockEventType.PLACE_BLOCK;
                        if (type.isBlock()) {
                            return;
                        }
                }
                if (PaperLib.isPaper()) {
                    if (MaterialTags.SPAWN_EGGS.isTagged(type) || Material.EGG.equals(type)) {
                        eventType = PlayerBlockEventType.SPAWN_MOB;
                        break;
                    }
                } else {
                    if (type.toString().toLowerCase().endsWith("egg")) {
                        eventType = PlayerBlockEventType.SPAWN_MOB;
                        break;
                    }
                }
                if (type.isEdible()) {
                    //Allow all players to eat while also allowing the block place event ot be fired
                    return;
                }
                switch (type) {
                    case ACACIA_BOAT:
                    case BIRCH_BOAT:
                    case CHEST_MINECART:
                    case COMMAND_BLOCK_MINECART:
                    case DARK_OAK_BOAT:
                    case FURNACE_MINECART:
                    case HOPPER_MINECART:
                    case JUNGLE_BOAT:
                    case MINECART:
                    case OAK_BOAT:
                    case SPRUCE_BOAT:
                    case TNT_MINECART:
                        eventType = PlayerBlockEventType.PLACE_VEHICLE;
                        break outer;
                    case FIREWORK_ROCKET:
                    case FIREWORK_STAR:
                        eventType = PlayerBlockEventType.SPAWN_MOB;
                        break outer;
                    case BOOK:
                    case KNOWLEDGE_BOOK:
                    case WRITABLE_BOOK:
                    case WRITTEN_BOOK:
                        eventType = PlayerBlockEventType.READ;
                        break outer;
                    case ARMOR_STAND:
                        location = BukkitUtil
                            .getLocation(block.getRelative(event.getBlockFace()).getLocation());
                        eventType = PlayerBlockEventType.PLACE_MISC;
                        break outer;
                }
                break;
            }
            case LEFT_CLICK_BLOCK: {
                location = BukkitUtil.getLocation(block.getLocation());
                //eventType = PlayerBlockEventType.BREAK_BLOCK;
                blocktype1 = BukkitAdapter.asBlockType(block.getType());
                if (block.getType() == Material.DRAGON_EGG) {
                    eventType = PlayerBlockEventType.TELEPORT_OBJECT;
                    break;
                }

                return;
            }
            default:
                return;
        }
        if (PlotSquared.get().worldedit != null && pp.getAttribute("worldedit")) {
            if (event.getMaterial() == Material
                .getMaterial(PlotSquared.get().worldedit.getConfiguration().wandItem)) {
                return;
            }
        }
        if (!PlotSquared.get().getEventDispatcher()
            .checkPlayerBlockEvent(pp, eventType, location, blocktype1, true)) {
            System.out.println("dd");
            event.setCancelled(true);
            event.setUseInteractedBlock(Event.Result.DENY);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBucketEmpty(PlayerBucketEmptyEvent event) {
        BlockFace bf = event.getBlockFace();
        Block block =
            event.getBlockClicked().getLocation().add(bf.getModX(), bf.getModY(), bf.getModZ())
                .getBlock();
        Location location = BukkitUtil.getLocation(block.getLocation());
        PlotArea area = location.getPlotArea();
        if (area == null) {
            return;
        }
        BukkitPlayer pp = BukkitUtil.getPlayer(event.getPlayer());
        Plot plot = area.getPlot(location);
        if (plot == null) {
            if (Permissions.hasPermission(pp, Captions.PERMISSION_ADMIN_BUILD_ROAD)) {
                return;
            }
            MainUtil.sendMessage(pp, Captions.NO_PERMISSION_EVENT,
                Captions.PERMISSION_ADMIN_BUILD_ROAD);
            event.setCancelled(true);
        } else if (!plot.hasOwner()) {
            if (Permissions.hasPermission(pp, Captions.PERMISSION_ADMIN_BUILD_UNOWNED)) {
                return;
            }
            MainUtil.sendMessage(pp, Captions.NO_PERMISSION_EVENT,
                Captions.PERMISSION_ADMIN_BUILD_UNOWNED);
            event.setCancelled(true);
        } else if (!plot.isAdded(pp.getUUID())) {
            List<BlockTypeWrapper> use = plot.getFlag(UseFlag.class);
            final BlockType blockType = BukkitAdapter.asBlockType(block.getType());
            for (final BlockTypeWrapper blockTypeWrapper : use) {
                if (blockTypeWrapper.accepts(blockType)) {
                    return;
                }
            }
            if (Permissions.hasPermission(pp, Captions.PERMISSION_ADMIN_BUILD_OTHER)) {
                return;
            }
            MainUtil.sendMessage(pp, Captions.NO_PERMISSION_EVENT,
                Captions.PERMISSION_ADMIN_BUILD_OTHER);
            event.setCancelled(true);
        } else if (Settings.Done.RESTRICT_BUILDING && DoneFlag.isDone(plot)) {
            if (!Permissions.hasPermission(pp, Captions.PERMISSION_ADMIN_BUILD_OTHER)) {
                MainUtil.sendMessage(pp, Captions.NO_PERMISSION_EVENT,
                    Captions.PERMISSION_ADMIN_BUILD_OTHER);
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClose(InventoryCloseEvent event) {
        HumanEntity closer = event.getPlayer();
        if (!(closer instanceof Player)) {
            return;
        }
        Player player = (Player) closer;
        PlotInventory.removePlotInventoryOpen(BukkitUtil.getPlayer(player));
    }

    @EventHandler(priority = EventPriority.MONITOR) public void onLeave(PlayerQuitEvent event) {
        TaskManager.TELEPORT_QUEUE.remove(event.getPlayer().getName());
        BukkitPlayer pp = BukkitUtil.getPlayer(event.getPlayer());
        pp.unregister();
        PlotListener.logout(pp.getUUID());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBucketFill(PlayerBucketFillEvent event) {
        Block blockClicked = event.getBlockClicked();
        Location location = BukkitUtil.getLocation(blockClicked.getLocation());
        PlotArea area = location.getPlotArea();
        if (area == null) {
            return;
        }
        Player player = event.getPlayer();
        BukkitPlayer plotPlayer = BukkitUtil.getPlayer(player);
        Plot plot = area.getPlot(location);
        if (plot == null) {
            if (Permissions.hasPermission(plotPlayer, Captions.PERMISSION_ADMIN_BUILD_ROAD)) {
                return;
            }
            MainUtil.sendMessage(plotPlayer, Captions.NO_PERMISSION_EVENT,
                Captions.PERMISSION_ADMIN_BUILD_ROAD);
            event.setCancelled(true);
        } else if (!plot.hasOwner()) {
            if (Permissions.hasPermission(plotPlayer, Captions.PERMISSION_ADMIN_BUILD_UNOWNED)) {
                return;
            }
            MainUtil.sendMessage(plotPlayer, Captions.NO_PERMISSION_EVENT,
                Captions.PERMISSION_ADMIN_BUILD_UNOWNED);
            event.setCancelled(true);
        } else if (!plot.isAdded(plotPlayer.getUUID())) {
            List<BlockTypeWrapper> use = plot.getFlag(UseFlag.class);
            Block block = event.getBlockClicked();
            final BlockType blockType = BukkitAdapter.asBlockType(block.getType());
            for (final BlockTypeWrapper blockTypeWrapper : use) {
                if (blockTypeWrapper.accepts(blockType)) {
                    return;
                }
            }
            if (Permissions.hasPermission(plotPlayer, Captions.PERMISSION_ADMIN_BUILD_OTHER)) {
                return;
            }
            MainUtil.sendMessage(plotPlayer, Captions.NO_PERMISSION_EVENT,
                Captions.PERMISSION_ADMIN_BUILD_OTHER);
            event.setCancelled(true);
        } else if (Settings.Done.RESTRICT_BUILDING && DoneFlag.isDone(plot)) {
            if (!Permissions.hasPermission(plotPlayer, Captions.PERMISSION_ADMIN_BUILD_OTHER)) {
                MainUtil.sendMessage(plotPlayer, Captions.NO_PERMISSION_EVENT,
                    Captions.PERMISSION_ADMIN_BUILD_OTHER);
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onHangingPlace(HangingPlaceEvent event) {
        Block block = event.getBlock().getRelative(event.getBlockFace());
        Location location = BukkitUtil.getLocation(block.getLocation());
        PlotArea area = location.getPlotArea();
        if (area == null) {
            return;
        }
        Player p = event.getPlayer();
        if (p == null) {
            PlotSquared.debug("PlotSquared does not support HangingPlaceEvent for non-players.");
            event.setCancelled(true);
            return;
        }
        BukkitPlayer pp = BukkitUtil.getPlayer(p);
        Plot plot = area.getPlot(location);
        if (plot == null) {
            if (!Permissions.hasPermission(pp, Captions.PERMISSION_ADMIN_BUILD_ROAD)) {
                MainUtil.sendMessage(pp, Captions.NO_PERMISSION_EVENT,
                    Captions.PERMISSION_ADMIN_BUILD_ROAD);
                event.setCancelled(true);
            }
        } else {
            if (!plot.hasOwner()) {
                if (!Permissions.hasPermission(pp, Captions.PERMISSION_ADMIN_BUILD_UNOWNED)) {
                    MainUtil.sendMessage(pp, Captions.NO_PERMISSION_EVENT,
                        Captions.PERMISSION_ADMIN_BUILD_UNOWNED);
                    event.setCancelled(true);
                }
                return;
            }
            if (!plot.isAdded(pp.getUUID())) {
                if (!plot.getFlag(HangingPlaceFlag.class)) {
                    if (!Permissions.hasPermission(pp, Captions.PERMISSION_ADMIN_BUILD_OTHER)) {
                        MainUtil.sendMessage(pp, Captions.NO_PERMISSION_EVENT,
                            Captions.PERMISSION_ADMIN_BUILD_OTHER);
                        event.setCancelled(true);
                    }
                    return;
                }
            }
            if (BukkitEntityUtil.checkEntity(event.getEntity(), plot)) {
                event.setCancelled(true);
            }

        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onHangingBreakByEntity(HangingBreakByEntityEvent event) {
        Entity remover = event.getRemover();
        if (remover instanceof Player) {
            Player p = (Player) remover;
            Location location = BukkitUtil.getLocation(event.getEntity());
            PlotArea area = location.getPlotArea();
            if (area == null) {
                return;
            }
            BukkitPlayer pp = BukkitUtil.getPlayer(p);
            Plot plot = area.getPlot(location);
            if (plot == null) {
                if (!Permissions.hasPermission(pp, Captions.PERMISSION_ADMIN_DESTROY_ROAD)) {
                    MainUtil.sendMessage(pp, Captions.NO_PERMISSION_EVENT,
                        Captions.PERMISSION_ADMIN_DESTROY_ROAD);
                    event.setCancelled(true);
                }
            } else if (!plot.hasOwner()) {
                if (!Permissions.hasPermission(pp, Captions.PERMISSION_ADMIN_DESTROY_UNOWNED)) {
                    MainUtil.sendMessage(pp, Captions.NO_PERMISSION_EVENT,
                        Captions.PERMISSION_ADMIN_DESTROY_UNOWNED);
                    event.setCancelled(true);
                }
            } else if (!plot.isAdded(pp.getUUID())) {
                if (plot.getFlag(HangingBreakFlag.class)) {
                    return;
                }
                if (!Permissions.hasPermission(pp, Captions.PERMISSION_ADMIN_DESTROY_OTHER)) {
                    MainUtil.sendMessage(pp, Captions.NO_PERMISSION_EVENT,
                        Captions.PERMISSION_ADMIN_DESTROY_OTHER);
                    event.setCancelled(true);
                    plot.debug(p.getName()
                        + " could not break hanging entity because hanging-break = false");
                }
            }
        } else if (remover instanceof Projectile) {
            Projectile p = (Projectile) remover;
            if (p.getShooter() instanceof Player) {
                Player shooter = (Player) p.getShooter();
                Location location = BukkitUtil.getLocation(event.getEntity());
                PlotArea area = location.getPlotArea();
                if (area == null) {
                    return;
                }
                BukkitPlayer player = BukkitUtil.getPlayer(shooter);
                Plot plot = area.getPlot(BukkitUtil.getLocation(event.getEntity()));
                if (plot != null) {
                    if (!plot.hasOwner()) {
                        if (!Permissions
                            .hasPermission(player, Captions.PERMISSION_ADMIN_DESTROY_UNOWNED)) {
                            MainUtil.sendMessage(player, Captions.NO_PERMISSION_EVENT,
                                Captions.PERMISSION_ADMIN_DESTROY_UNOWNED);
                            event.setCancelled(true);
                        }
                    } else if (!plot.isAdded(player.getUUID())) {
                        if (!plot.getFlag(HangingBreakFlag.class)) {
                            if (!Permissions
                                .hasPermission(player, Captions.PERMISSION_ADMIN_DESTROY_OTHER)) {
                                MainUtil.sendMessage(player, Captions.NO_PERMISSION_EVENT,
                                    Captions.PERMISSION_ADMIN_DESTROY_OTHER);
                                event.setCancelled(true);
                                plot.debug(player.getName()
                                    + " could not break hanging entity because hanging-break = false");
                            }
                        }
                    }
                }
            }
        } else {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        Location location = BukkitUtil.getLocation(event.getRightClicked().getLocation());
        PlotArea area = location.getPlotArea();
        if (area == null) {
            return;
        }
        Player p = event.getPlayer();
        BukkitPlayer pp = BukkitUtil.getPlayer(p);
        Plot plot = area.getPlot(location);
        if (plot == null && !area.isRoadFlags()) {
            if (!Permissions.hasPermission(pp, Captions.PERMISSION_ADMIN_INTERACT_ROAD)) {
                MainUtil.sendMessage(pp, Captions.NO_PERMISSION_EVENT,
                    Captions.PERMISSION_ADMIN_INTERACT_ROAD);
                event.setCancelled(true);
            }
        } else if (plot != null && !plot.hasOwner()) {
            if (!Permissions.hasPermission(pp, Captions.PERMISSION_ADMIN_INTERACT_UNOWNED)) {
                MainUtil.sendMessage(pp, Captions.NO_PERMISSION_EVENT,
                    Captions.PERMISSION_ADMIN_INTERACT_UNOWNED);
                event.setCancelled(true);
            }
        } else if ((plot != null && !plot.isAdded(pp.getUUID())) || (plot == null && area
            .isRoadFlags())) {
            final Entity entity = event.getRightClicked();
            final com.sk89q.worldedit.world.entity.EntityType entityType =
                BukkitAdapter.adapt(entity.getType());

            FlagContainer flagContainer;
            if (plot == null) {
                flagContainer = area.getRoadFlagContainer();
            } else {
                flagContainer = plot.getFlagContainer();
            }

            if (EntityCategories.HOSTILE.contains(entityType) && flagContainer
                .getFlag(HostileInteractFlag.class).getValue()) {
                return;
            }

            if (EntityCategories.ANIMAL.contains(entityType) && flagContainer
                .getFlag(AnimalInteractFlag.class).getValue()) {
                return;
            }

            // This actually makes use of the interface, so we don't use the
            // category
            if (entity instanceof Tameable && ((Tameable) entity).isTamed() && flagContainer
                .getFlag(TamedInteractFlag.class).getValue()) {
                return;
            }

            if (EntityCategories.VEHICLE.contains(entityType) && flagContainer
                .getFlag(VehicleUseFlag.class).getValue()) {
                return;
            }

            if (EntityCategories.PLAYER.contains(entityType) && flagContainer
                .getFlag(PlayerInteractFlag.class).getValue()) {
                return;
            }

            if (EntityCategories.VILLAGER.contains(entityType) && flagContainer
                .getFlag(VillagerInteractFlag.class).getValue()) {
                return;
            }

            if ((EntityCategories.HANGING.contains(entityType) || EntityCategories.OTHER
                .contains(entityType)) && flagContainer.getFlag(MiscInteractFlag.class)
                .getValue()) {
                return;
            }

            if (!Permissions.hasPermission(pp, Captions.PERMISSION_ADMIN_INTERACT_OTHER)) {
                MainUtil.sendMessage(pp, Captions.NO_PERMISSION_EVENT,
                    Captions.PERMISSION_ADMIN_INTERACT_OTHER);
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onVehicleDestroy(VehicleDestroyEvent event) {
        Location location = BukkitUtil.getLocation(event.getVehicle());
        PlotArea area = location.getPlotArea();
        if (area == null) {
            return;
        }
        Entity attacker = event.getAttacker();
        if (attacker instanceof Player) {
            Player p = (Player) attacker;
            BukkitPlayer pp = BukkitUtil.getPlayer(p);
            Plot plot = area.getPlot(location);
            if (plot == null) {
                if (!Permissions.hasPermission(pp, "plots.admin.vehicle.break.road")) {
                    MainUtil.sendMessage(pp, Captions.NO_PERMISSION_EVENT,
                        "plots.admin.vehicle.break.road");
                    event.setCancelled(true);
                }
            } else {
                if (!plot.hasOwner()) {
                    if (!Permissions.hasPermission(pp, "plots.admin.vehicle.break.unowned")) {
                        MainUtil.sendMessage(pp, Captions.NO_PERMISSION_EVENT,
                            "plots.admin.vehicle.break.unowned");
                        event.setCancelled(true);
                        return;
                    }
                    return;
                }
                if (!plot.isAdded(pp.getUUID())) {
                    if (plot.getFlag(VehicleBreakFlag.class)) {
                        return;
                    }
                    if (!Permissions.hasPermission(pp, "plots.admin.vehicle.break.other")) {
                        MainUtil.sendMessage(pp, Captions.NO_PERMISSION_EVENT,
                            "plots.admin.vehicle.break.other");
                        event.setCancelled(true);
                        plot.debug(pp.getName()
                            + " could not break vehicle because vehicle-break = false");
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerEggThrow(PlayerEggThrowEvent event) {
        Location location = BukkitUtil.getLocation(event.getEgg().getLocation());
        PlotArea area = location.getPlotArea();
        if (area == null) {
            return;
        }
        Player player = event.getPlayer();
        BukkitPlayer plotPlayer = BukkitUtil.getPlayer(player);
        Plot plot = area.getPlot(location);
        if (plot == null) {
            if (!Permissions.hasPermission(plotPlayer, "plots.admin.projectile.road")) {
                MainUtil.sendMessage(plotPlayer, Captions.NO_PERMISSION_EVENT,
                    "plots.admin.projectile.road");
                event.setHatching(false);
            }
        } else if (!plot.hasOwner()) {
            if (!Permissions.hasPermission(plotPlayer, "plots.admin.projectile.unowned")) {
                MainUtil.sendMessage(plotPlayer, Captions.NO_PERMISSION_EVENT,
                    "plots.admin.projectile.unowned");
                event.setHatching(false);
            }
        } else if (!plot.isAdded(plotPlayer.getUUID())) {
            if (!Permissions.hasPermission(plotPlayer, "plots.admin.projectile.other")) {
                MainUtil.sendMessage(plotPlayer, Captions.NO_PERMISSION_EVENT,
                    "plots.admin.projectile.other");
                event.setHatching(false);
            }
        }
    }

    @EventHandler public void onItemDrop(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        BukkitPlayer pp = BukkitUtil.getPlayer(player);
        Location location = pp.getLocation();
        PlotArea area = location.getPlotArea();
        if (area == null) {
            return;
        }
        Plot plot = location.getOwnedPlot();
        if (plot == null) {
            if (area.isRoadFlags() && !area.getRoadFlag(ItemDropFlag.class)) {
                event.setCancelled(true);
            }
            return;
        }
        UUID uuid = pp.getUUID();
        if (!plot.isAdded(uuid)) {
            if (!plot.getFlag(ItemDropFlag.class)) {
                plot.debug(player.getName() + " could not drop item because of item-drop = false");
                event.setCancelled(true);
            }
        }
    }

    @EventHandler public void onItemPickup(EntityPickupItemEvent event) {
        LivingEntity ent = event.getEntity();
        if (ent instanceof Player) {
            Player player = (Player) ent;
            BukkitPlayer pp = BukkitUtil.getPlayer(player);
            Location location = pp.getLocation();
            PlotArea area = location.getPlotArea();
            if (area == null) {
                return;
            }
            Plot plot = location.getOwnedPlot();
            if (plot == null) {
                if (area.isRoadFlags() && area.getRoadFlag(DropProtectionFlag.class)) {
                    event.setCancelled(true);
                }
                return;
            }
            UUID uuid = pp.getUUID();
            if (!plot.isAdded(uuid) && plot.getFlag(DropProtectionFlag.class)) {
                plot.debug(
                    player.getName() + " could not pick up item because of drop-protection = true");
                event.setCancelled(true);
            }
        }
    }

    @EventHandler public void onDeath(final PlayerDeathEvent event) {
        Location location = BukkitUtil.getLocation(event.getEntity());
        PlotArea area = location.getPlotArea();
        if (area == null) {
            return;
        }
        Plot plot = location.getOwnedPlot();
        if (plot == null) {
            if (area.isRoadFlags() && area.getRoadFlag(KeepInventoryFlag.class)) {
                event.setCancelled(true);
            }
            return;
        }
        if (plot.getFlag(KeepInventoryFlag.class)) {
            if (plot.getFlag(KeepInventoryFlag.class)) {
                plot.debug(event.getEntity().getName()
                    + " kept their inventory because of keep-inventory = true");
                event.setKeepInventory(true);
            }
        }
    }
}
