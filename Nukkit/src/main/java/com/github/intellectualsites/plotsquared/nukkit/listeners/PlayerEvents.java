package com.github.intellectualsites.plotsquared.nukkit.listeners;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.block.Block;
import cn.nukkit.command.CommandSender;
import cn.nukkit.entity.*;
import cn.nukkit.entity.item.EntityPotion;
import cn.nukkit.entity.item.EntityVehicle;
import cn.nukkit.entity.mob.EntityMob;
import cn.nukkit.entity.passive.EntityAnimal;
import cn.nukkit.entity.passive.EntityTameable;
import cn.nukkit.entity.passive.EntityWaterAnimal;
import cn.nukkit.entity.projectile.EntityProjectile;
import cn.nukkit.event.*;
import cn.nukkit.event.block.*;
import cn.nukkit.event.entity.*;
import cn.nukkit.event.inventory.InventoryCloseEvent;
import cn.nukkit.event.player.*;
import cn.nukkit.event.potion.PotionCollideEvent;
import cn.nukkit.event.redstone.RedstoneUpdateEvent;
import cn.nukkit.metadata.MetadataValue;
import cn.nukkit.plugin.Plugin;
import com.github.intellectualsites.plotsquared.nukkit.object.NukkitPlayer;
import com.github.intellectualsites.plotsquared.nukkit.util.NukkitUtil;
import com.github.intellectualsites.plotsquared.plot.PlotSquared;
import com.github.intellectualsites.plotsquared.plot.config.C;
import com.github.intellectualsites.plotsquared.plot.config.Settings;
import com.github.intellectualsites.plotsquared.plot.flag.Flags;
import com.github.intellectualsites.plotsquared.plot.listener.PlotListener;
import com.github.intellectualsites.plotsquared.plot.object.*;
import com.github.intellectualsites.plotsquared.plot.util.*;
import com.google.common.base.Optional;

import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Pattern;

@SuppressWarnings("unused")
public class PlayerEvents extends PlotListener implements Listener {

    private boolean pistonBlocks = true;
    // To prevent recursion
    private boolean tmpTeleport = true;

    public static boolean checkEntity(Entity entity, Plot plot) {
        if (plot == null || !plot.hasOwner() || plot.getFlags().isEmpty() && plot
            .getArea().DEFAULT_FLAGS.isEmpty()) {
            return false;
        }
        if (entity instanceof EntityLiving) {
            if (entity instanceof EntityCreature) {
                if (entity instanceof EntityAnimal || entity instanceof EntityWaterAnimal) {
                    return EntityUtil.checkEntity(plot, Flags.ENTITY_CAP, Flags.MOB_CAP, Flags.ANIMAL_CAP);
                } else if (entity instanceof EntityMob) {
                    return EntityUtil.checkEntity(plot, Flags.ENTITY_CAP, Flags.MOB_CAP, Flags.HOSTILE_CAP);
                } else if (entity instanceof EntityHuman) {
                    return false;
                } else {
                    return EntityUtil.checkEntity(plot, Flags.ENTITY_CAP, Flags.MOB_CAP, Flags.MOB_CAP);
                }
            } else {
                return EntityUtil.checkEntity(plot, Flags.ENTITY_CAP, Flags.MOB_CAP);
            }
        } else if (entity instanceof EntityVehicle) {
            return EntityUtil.checkEntity(plot, Flags.ENTITY_CAP, Flags.VEHICLE_CAP);
        } else if (entity instanceof EntityHanging) {
            return EntityUtil.checkEntity(plot, Flags.ENTITY_CAP, Flags.MISC_CAP);
        } else {
            return EntityUtil.checkEntity(plot, Flags.ENTITY_CAP);
        }
    }

    // TODO fix this
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onPhysicsEvent(BlockUpdateEvent event) {
        if (event instanceof RedstoneUpdateEvent) {
            Block block = event.getBlock();
            Location loc = NukkitUtil.getLocation(block.getLocation());
            PlotArea area = loc.getPlotArea();
            if (area == null) {
                return;
            }
            Plot plot = area.getOwnedPlot(loc);
            if (plot == null) {
                return;
            }
            if (Flags.REDSTONE.isFalse(plot)) {
                event.setCancelled(true);
                return;
            }
            if (Settings.Redstone.DISABLE_OFFLINE) {
                if (UUIDHandler.getPlayer(plot.getOwner()) == null) {
                    boolean disable = true;
                    for (UUID trusted : plot.getTrusted()) {
                        if (UUIDHandler.getPlayer(trusted) != null) {
                            disable = false;
                            break;
                        }
                    }
                    if (disable) {
                        event.setCancelled(true);
                        return;
                    }
                }
            }
            if (Settings.Redstone.DISABLE_UNOCCUPIED) {
                for (Map.Entry<String, PlotPlayer> entry : UUIDHandler.getPlayers().entrySet()) {
                    if (plot.equals(entry.getValue().getCurrentPlot())) {
                        return;
                    }
                }
                event.setCancelled(true);
            }
        } else {
            Block block = event.getBlock();
            Location loc = NukkitUtil.getLocation(block.getLocation());
            PlotArea area = loc.getPlotArea();
            if (area == null) {
                return;
            }
            Plot plot = area.getOwnedPlotAbs(loc);
            if (plot == null) {
                return;
            }
            if (Flags.DISABLE_PHYSICS.isFalse(plot)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityCombustByEntity(EntityCombustEvent event) {
        if (event instanceof EntityCombustByEntityEvent) {
            EntityDamageByEntityEvent eventChange =
                new EntityDamageByEntityEvent(((EntityCombustByEntityEvent) event).getCombuster(),
                    event.getEntity(), EntityDamageEvent.DamageCause.FIRE_TICK,
                    event.getDuration());
            onEntityDamageByEntityEvent(eventChange);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDamageByEntityEvent(EntityDamageEvent event) {
        if (event instanceof EntityDamageByEntityEvent) {
            EntityDamageByEntityEvent specific = (EntityDamageByEntityEvent) event;
            Entity damager = specific.getDamager();
            Location l = NukkitUtil.getLocation(damager);
            if (!PlotSquared.get().hasPlotArea(l.getWorld())) {
                return;
            }
            Entity victim = event.getEntity();
            if (!entityDamage(damager, victim)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler public void onProjectileLaunch(ProjectileLaunchEvent event) {
        EntityProjectile entity = event.getEntity();
        if (!(entity instanceof EntityPotion)) {
            return;
        }
        Entity shooter = entity.shootingEntity;
        if (!(shooter instanceof Player)) {
            return;
        }
        Location l = NukkitUtil.getLocation(entity);
        if (!PlotSquared.get().hasPlotArea(l.getWorld())) {
            return;
        }
        PlotPlayer pp = NukkitUtil.getPlayer((Player) shooter);
        Plot plot = l.getOwnedPlot();
        if (plot != null && !plot.isAdded(pp.getUUID())) {
            kill(entity, event);
        }
    }

    @EventHandler public boolean onProjectileHit(ProjectileHitEvent event) {
        EntityProjectile entity = (EntityProjectile) event.getEntity();
        Location loc = NukkitUtil.getLocation(entity);
        if (!PlotSquared.get().hasPlotArea(loc.getWorld())) {
            return true;
        }
        PlotArea area = loc.getPlotArea();
        if (area == null) {
            return true;
        }
        Plot plot = area.getPlotAbs(loc);
        Entity shooter = entity.shootingEntity;
        if (shooter instanceof Player) {
            PlotPlayer pp = NukkitUtil.getPlayer((Player) shooter);
            if (plot == null) {
                if (!Permissions.hasPermission(pp, C.PERMISSION_PROJECTILE_UNOWNED)) {
                    kill(entity, event);
                    return false;
                }
                return true;
            }
            if (plot.isAdded(pp.getUUID()) || Permissions
                .hasPermission(pp, C.PERMISSION_PROJECTILE_OTHER)) {
                return true;
            }
            kill(entity, event);
            return false;
        }
        if (shooter == null) {
            kill(entity, event);
            return false;
        }
        return true;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void playerCommand(PlayerCommandPreprocessEvent event) {
        String msg = event.getMessage().toLowerCase().replaceAll("/", "").trim();
        if (msg.isEmpty()) {
            return;
        }
        String[] split = msg.split(" ");
        Player player = event.getPlayer();
        PlotPlayer pp = NukkitUtil.getPlayer(player);
        Plot plot = pp.getCurrentPlot();
        if (plot == null) {
            return;
        }
        Optional<List<String>> flag = plot.getFlag(Flags.BLOCKED_CMDS);
        if (flag.isPresent() && !Permissions
            .hasPermission(pp, C.PERMISSION_ADMIN_INTERACT_BLOCKED_CMDS)) {
            List<String> blocked_cmds = flag.get();
            for (String s : blocked_cmds) {
                Pattern pattern;
                if (!RegExUtil.compiledPatterns.containsKey(s)) {
                    RegExUtil.compiledPatterns.put(s, pattern = Pattern.compile(s));
                } else {
                    pattern = RegExUtil.compiledPatterns.get(s);
                }
                if (pattern.matcher(msg).matches()) {
                    String perm;
                    if (plot.isAdded(pp.getUUID())) {
                        perm = "plots.admin.command.blocked-cmds.shared";
                    } else {
                        perm = "plots.admin.command.blocked-cmds.other";
                    }
                    if (!Permissions.hasPermission(pp, perm)) {
                        MainUtil.sendMessage(pp, C.COMMAND_BLOCKED);
                        event.setCancelled(true);
                    }
                    return;
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onConnect(PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        NukkitUtil.getPlayer(event.getPlayer()).unregister();
        final PlotPlayer pp = NukkitUtil.getPlayer(player);
        // Now
        String name = pp.getName();
        StringWrapper sw = new StringWrapper(name);
        UUID uuid = pp.getUUID();
        UUIDHandler.add(sw, uuid);

        Location loc = pp.getLocation();
        PlotArea area = loc.getPlotArea();
        if (area != null) {
            Plot plot = area.getPlot(loc);
            if (plot != null) {
                plotEntry(pp, plot);
            }
        }
        // Delayed

        // Async
        TaskManager.runTaskLaterAsync(new Runnable() {
            @Override public void run() {
                if (!player.hasPlayedBefore() && player.isOnline()) {
                    player.save();
                }
                EventUtil.manager.doJoinTask(pp);
            }
        }, 20);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void playerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        PlotPlayer pp = NukkitUtil.getPlayer(player);
        EventUtil.manager.doRespawnTask(pp);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onTeleport(PlayerTeleportEvent event) {
        if (event.getTo() == null || event.getFrom() == null) {
            NukkitUtil.getPlayer(event.getPlayer()).deleteMeta(PlotPlayer.META_LOCATION);
            NukkitUtil.getPlayer(event.getPlayer()).deleteMeta(PlotPlayer.META_LAST_PLOT);
            return;
        }
        cn.nukkit.level.Location from = event.getFrom();
        cn.nukkit.level.Location to = event.getTo();
        int x2;
        if (MathMan.roundInt(from.getX()) != (x2 = MathMan.roundInt(to.getX()))) {
            Player player = event.getPlayer();
            PlotPlayer pp = NukkitUtil.getPlayer(player);
            // Cancel teleport
            TaskManager.TELEPORT_QUEUE.remove(pp.getName());
            // Set last location
            Location loc = NukkitUtil.getLocation(to);
            pp.setMeta(PlotPlayer.META_LOCATION, loc);
            PlotArea area = loc.getPlotArea();
            if (area == null) {
                pp.deleteMeta(PlotPlayer.META_LAST_PLOT);
                return;
            }
            Plot now = area.getPlot(loc);
            Plot lastPlot = pp.getMeta(PlotPlayer.META_LAST_PLOT);
            if (now == null) {
                if (lastPlot != null && !plotExit(pp, lastPlot) && this.tmpTeleport) {
                    MainUtil.sendMessage(pp, C.NO_PERMISSION_EVENT, C.PERMISSION_ADMIN_EXIT_DENIED);
                    this.tmpTeleport = false;
                    if (lastPlot.equals(NukkitUtil.getLocation(from).getPlot())) {
                        player.teleport(from);
                    } else {
                        player.teleport(player.getLevel().getSpawnLocation());
                    }
                    this.tmpTeleport = true;
                    event.setCancelled(true);
                    return;
                }
            } else if (now.equals(lastPlot)) {
                return;
            } else if (!plotEntry(pp, now) && this.tmpTeleport) {
                MainUtil.sendMessage(pp, C.NO_PERMISSION_EVENT, C.PERMISSION_ADMIN_ENTRY_DENIED);
                this.tmpTeleport = false;
                to.setComponents(from.getX(), from.getY(), from.getZ());
                player.teleport(event.getTo());
                this.tmpTeleport = true;
                return;
            }
            Integer border = area.getBorder();
            if (x2 > border) {
                to.setComponents(border - 4, to.getY(), to.getZ());
                this.tmpTeleport = false;
                player.teleport(event.getTo());
                this.tmpTeleport = true;
                MainUtil.sendMessage(pp, C.BORDER);
                return;
            }
            if (x2 < -border) {
                to.setComponents(-border + 4, to.getY(), to.getZ());
                this.tmpTeleport = false;
                player.teleport(event.getTo());
                this.tmpTeleport = true;
                MainUtil.sendMessage(pp, C.BORDER);
                return;
            }
            return;
        }
        int z2;
        if (MathMan.roundInt(from.getZ()) != (z2 = MathMan.roundInt(to.getZ()))) {
            Player player = event.getPlayer();
            PlotPlayer pp = NukkitUtil.getPlayer(player);
            // Cancel teleport
            TaskManager.TELEPORT_QUEUE.remove(pp.getName());
            // Set last location
            Location loc = NukkitUtil.getLocation(to);
            pp.setMeta("location", loc);
            PlotArea area = loc.getPlotArea();
            if (area == null) {
                pp.deleteMeta(PlotPlayer.META_LAST_PLOT);
                return;
            }
            Plot now = area.getPlot(loc);
            Plot lastPlot = pp.getMeta(PlotPlayer.META_LAST_PLOT);
            if (now == null) {
                if (lastPlot != null && !plotExit(pp, lastPlot) && this.tmpTeleport) {
                    MainUtil.sendMessage(pp, C.NO_PERMISSION_EVENT, C.PERMISSION_ADMIN_EXIT_DENIED);
                    this.tmpTeleport = false;
                    if (lastPlot.equals(NukkitUtil.getLocation(from).getPlot())) {
                        player.teleport(from);
                    } else {
                        player.teleport(player.getLevel().getSpawnLocation());
                    }
                    this.tmpTeleport = true;
                    event.setCancelled(true);
                    return;
                }
            } else if (now.equals(lastPlot)) {
                return;
            } else if (!plotEntry(pp, now) && this.tmpTeleport) {
                MainUtil.sendMessage(pp, C.NO_PERMISSION_EVENT, C.PERMISSION_ADMIN_ENTRY_DENIED);
                this.tmpTeleport = false;
                player.teleport(from);
                to.setComponents(from.getX(), from.getY(), from.getZ());
                player.teleport(event.getTo());
                this.tmpTeleport = true;
                return;
            }
            Integer border = area.getBorder();
            if (z2 > border) {
                to.setComponents(to.getX(), to.getY(), border - 4);
                this.tmpTeleport = false;
                player.teleport(event.getTo());
                this.tmpTeleport = true;
                MainUtil.sendMessage(pp, C.BORDER);
            } else if (z2 < -border) {
                to.setComponents(to.getX(), to.getY(), -border + 4);
                this.tmpTeleport = false;
                player.teleport(event.getTo());
                this.tmpTeleport = true;
                MainUtil.sendMessage(pp, C.BORDER);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void playerMove(PlayerMoveEvent event) {
        cn.nukkit.level.Location from = event.getFrom();
        cn.nukkit.level.Location to = event.getTo();
        int x2;
        if (MathMan.roundInt(from.getX()) != (x2 = MathMan.roundInt(to.getX()))) {
            Player player = event.getPlayer();
            PlotPlayer pp = NukkitUtil.getPlayer(player);
            // Cancel teleport
            TaskManager.TELEPORT_QUEUE.remove(pp.getName());
            // Set last location
            Location loc = NukkitUtil.getLocation(to);
            pp.setMeta(PlotPlayer.META_LOCATION, loc);
            PlotArea area = loc.getPlotArea();
            if (area == null) {
                pp.deleteMeta(PlotPlayer.META_LAST_PLOT);
                return;
            }
            Plot now = area.getPlot(loc);
            Plot lastPlot = pp.getMeta(PlotPlayer.META_LAST_PLOT);
            if (now == null) {
                if (lastPlot != null && !plotExit(pp, lastPlot) && this.tmpTeleport) {
                    MainUtil.sendMessage(pp, C.NO_PERMISSION_EVENT, C.PERMISSION_ADMIN_EXIT_DENIED);
                    this.tmpTeleport = false;
                    if (lastPlot.equals(NukkitUtil.getLocation(from).getPlot())) {
                        player.teleport(from);
                    } else {
                        player.teleport(player.getLevel().getSpawnLocation());
                    }
                    this.tmpTeleport = true;
                    event.setCancelled(true);
                    return;
                }
            } else if (now.equals(lastPlot)) {
                return;
            } else if (!plotEntry(pp, now) && this.tmpTeleport) {
                MainUtil.sendMessage(pp, C.NO_PERMISSION_EVENT, C.PERMISSION_ADMIN_ENTRY_DENIED);
                this.tmpTeleport = false;
                to.setComponents(from.getX(), from.getY(), from.getZ());
                player.teleport(event.getTo());
                this.tmpTeleport = true;
                return;
            }
            Integer border = area.getBorder();
            if (x2 > border) {
                to.setComponents(border - 4, to.getY(), to.getZ());
                this.tmpTeleport = false;
                player.teleport(event.getTo());
                this.tmpTeleport = true;
                MainUtil.sendMessage(pp, C.BORDER);
                return;
            }
            if (x2 < -border) {
                to.setComponents(-border + 4, to.getY(), to.getZ());
                this.tmpTeleport = false;
                player.teleport(event.getTo());
                this.tmpTeleport = true;
                MainUtil.sendMessage(pp, C.BORDER);
                return;
            }
            return;
        }
        int z2;
        if (MathMan.roundInt(from.getZ()) != (z2 = MathMan.roundInt(to.getZ()))) {
            Player player = event.getPlayer();
            PlotPlayer pp = NukkitUtil.getPlayer(player);
            // Cancel teleport
            TaskManager.TELEPORT_QUEUE.remove(pp.getName());
            // Set last location
            Location loc = NukkitUtil.getLocation(to);
            pp.setMeta(PlotPlayer.META_LOCATION, loc);
            PlotArea area = loc.getPlotArea();
            if (area == null) {
                pp.deleteMeta(PlotPlayer.META_LAST_PLOT);
                return;
            }
            Plot now = area.getPlot(loc);
            Plot lastPlot = pp.getMeta(PlotPlayer.META_LAST_PLOT);
            if (now == null) {
                if (lastPlot != null && !plotExit(pp, lastPlot) && this.tmpTeleport) {
                    MainUtil.sendMessage(pp, C.NO_PERMISSION_EVENT, C.PERMISSION_ADMIN_EXIT_DENIED);
                    this.tmpTeleport = false;
                    if (lastPlot.equals(NukkitUtil.getLocation(from).getPlot())) {
                        player.teleport(from);
                    } else {
                        player.teleport(player.getLevel().getSpawnLocation());
                    }
                    this.tmpTeleport = true;
                    event.setCancelled(true);
                    return;
                }
            } else if (now.equals(lastPlot)) {
                return;
            } else if (!plotEntry(pp, now) && this.tmpTeleport) {
                MainUtil.sendMessage(pp, C.NO_PERMISSION_EVENT, C.PERMISSION_ADMIN_ENTRY_DENIED);
                this.tmpTeleport = false;
                player.teleport(from);
                to.setComponents(from.getX(), from.getY(), from.getZ());
                player.teleport(event.getTo());
                this.tmpTeleport = true;
                return;
            }
            Integer border = area.getBorder();
            if (z2 > border) {
                to.setComponents(to.getX(), to.getY(), border - 4);
                this.tmpTeleport = false;
                player.teleport(event.getTo());
                this.tmpTeleport = true;
                MainUtil.sendMessage(pp, C.BORDER);
            } else if (z2 < -border) {
                to.setComponents(to.getX(), to.getY(), -border + 4);
                this.tmpTeleport = false;
                player.teleport(event.getTo());
                this.tmpTeleport = true;
                MainUtil.sendMessage(pp, C.BORDER);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW) public void onChat(PlayerChatEvent event) {
        if (event.isCancelled())
            return;

        PlotPlayer plotPlayer = NukkitUtil.getPlayer(event.getPlayer());
        Location location = plotPlayer.getLocation();
        PlotArea area = location.getPlotArea();
        if (area == null || (area.PLOT_CHAT == plotPlayer.getAttribute("chat"))) {
            return;
        }
        Plot plot = area.getPlot(location);
        if (plot == null) {
            return;
        }
        String message = event.getMessage();
        if (plotPlayer.hasPermission("plots.chat.color")) {
            event.setMessage(C.color(message));
        }
        String format = C.PLOT_CHAT_FORMAT.s();
        String sender = event.getPlayer().getDisplayName();
        PlotId id = plot.getId();
        Set<CommandSender> recipients = event.getRecipients();
        recipients.clear();
        for (Entry<String, PlotPlayer> entry : UUIDHandler.getPlayers().entrySet()) {
            PlotPlayer pp = entry.getValue();
            if (pp.getAttribute("chatspy")) {
                String spy = event.getFormat();
                spy = String.format(spy, sender, message);
                pp.sendMessage(spy);
            } else {
                Plot current = pp.getCurrentPlot();
                if (current != null && current.getBasePlot(false).equals(plot)) {
                    recipients.add(((NukkitPlayer) pp).player);
                }
            }
        }
        String newFormat = C.color(
            format.replace("%plot_id%", id.x + ";" + id.y).replace("%sender%", "{%0}")
                .replace("%msg%", "{%1}"));
        event.setFormat(newFormat);
        recipients.add(Server.getInstance().getConsoleSender());
    }

    @EventHandler(priority = EventPriority.LOWEST) public void blockDestroy(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Location location = NukkitUtil.getLocation(event.getBlock().getLocation());
        PlotArea area = location.getPlotArea();
        if (area == null) {
            return;
        }
        Plot plot = area.getPlotAbs(location);
        if (plot != null) {
            PlotPlayer plotPlayer = NukkitUtil.getPlayer(player);
            if (event.getBlock().getY() == 0) {
                if (!Permissions
                    .hasPermission(plotPlayer, C.PERMISSION_ADMIN_DESTROY_GROUNDLEVEL)) {
                    MainUtil.sendMessage(plotPlayer, C.NO_PERMISSION_EVENT,
                        C.PERMISSION_ADMIN_DESTROY_GROUNDLEVEL);
                    event.setCancelled(true);
                    return;
                }
            }
            if (!plot.hasOwner()) {
                if (Permissions.hasPermission(plotPlayer, C.PERMISSION_ADMIN_DESTROY_UNOWNED)) {
                    return;
                }
                MainUtil.sendMessage(plotPlayer, C.NO_PERMISSION_EVENT,
                    C.PERMISSION_ADMIN_DESTROY_UNOWNED);
                event.setCancelled(true);
                return;
            }
            if (!plot.isAdded(plotPlayer.getUUID())) {
                Optional<HashSet<PlotBlock>> destroy = plot.getFlag(Flags.BREAK);
                Block block = event.getBlock();
                if (destroy.isPresent() && destroy.get()
                    .contains(PlotBlock.get((short) block.getId(), block.getDamage()))) {
                    return;
                }
                if (Permissions.hasPermission(plotPlayer, C.PERMISSION_ADMIN_DESTROY_OTHER)) {
                    return;
                }
                MainUtil.sendMessage(plotPlayer, C.NO_PERMISSION_EVENT,
                    C.PERMISSION_ADMIN_DESTROY_OTHER);
                event.setCancelled(true);
            } else if (Settings.Done.RESTRICT_BUILDING && plot.getFlags().containsKey(Flags.DONE)) {
                if (!Permissions.hasPermission(plotPlayer, C.PERMISSION_ADMIN_BUILD_OTHER)) {
                    MainUtil.sendMessage(plotPlayer, C.NO_PERMISSION_EVENT,
                        C.PERMISSION_ADMIN_BUILD_OTHER);
                    event.setCancelled(true);
                    return;
                }
            }
            return;
        }
        PlotPlayer pp = NukkitUtil.getPlayer(player);
        if (Permissions.hasPermission(pp, C.PERMISSION_ADMIN_DESTROY_ROAD)) {
            return;
        }
        if (PlotSquared.get().worldedit != null && pp.getAttribute("worldedit")) {
            if (player.getInventory().getItemInHand().getId() == PlotSquared.get().worldedit
                .getConfiguration().wandItem) {
                return;
            }
        }
        MainUtil.sendMessage(pp, C.NO_PERMISSION_EVENT, C.PERMISSION_ADMIN_DESTROY_ROAD);
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBigBoom(ExplosionPrimeEvent event) {
        Entity entity = event.getEntity();
        Location location = NukkitUtil.getLocation(entity);
        PlotArea area = location.getPlotArea();
        if (area == null) {
            return;
        }
        final Plot finalPlot = area.getOwnedPlot(location);
        if (!entity.hasMetadata("plot")) {
            entity.setMetadata("plot", new MetadataValue((Plugin) PlotSquared.get().IMP) {
                private Plot plot = finalPlot;

                @Override public Object value() {
                    return plot;
                }

                @Override public void invalidate() {
                    plot = null;
                }
            });
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBigBoom(EntityExplodeEvent event) {
        Location location = NukkitUtil.getLocation(event.getPosition());
        PlotArea area = location.getPlotArea();
        if (area == null) {
            if (!PlotSquared.get().hasPlotArea(location.getWorld())) {
                return;
            }
            Iterator<Block> iterator = event.getBlockList().iterator();
            while (iterator.hasNext()) {
                iterator.next();
                if (location.getPlotArea() != null) {
                    iterator.remove();
                }
            }
            return;
        }
        Plot plot = area.getOwnedPlot(location);
        if (plot != null) {
            if (Flags.EXPLOSION.isTrue(plot)) {
                List<MetadataValue> meta = event.getEntity().getMetadata("plot");
                Plot origin;
                if (meta.isEmpty()) {
                    origin = plot;
                } else {
                    origin = (Plot) meta.get(0).value();
                }
                Iterator<Block> iterator = event.getBlockList().iterator();
                while (iterator.hasNext()) {
                    Block block = iterator.next();
                    location = NukkitUtil.getLocation(block.getLocation());
                    if (!area.contains(location.getX(), location.getZ()) || !origin
                        .equals(area.getOwnedPlot(location))) {
                        iterator.remove();
                    }
                }
                return;
            }
        }
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityBlockForm(BlockFormEvent event) {
        String world = event.getBlock().getLevel().getName();
        if (!PlotSquared.get().hasPlotArea(world)) {
            return;
        }
        if (NukkitUtil.getLocation(event.getBlock().getLocation()).getPlotArea() != null) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockSpread(BlockSpreadEvent event) {
        Block block = event.getBlock();
        Location location = NukkitUtil.getLocation(block.getLocation());
        if (location.isPlotRoad()) {
            event.setCancelled(true);
            return;
        }
        PlotArea area = location.getPlotArea();
        if (area == null) {
            return;
        }
        Plot plot = area.getOwnedPlot(location);
        if (plot == null) {
            return;
        }
        Block source = event.getSource();
        switch (source.getId()) {
            case 2:
                if (Flags.GRASS_GROW.isFalse(plot)) {
                    event.setCancelled(true);
                }
                break;
            case 110:
                if (Flags.MYCEL_GROW.isFalse(plot)) {
                    event.setCancelled(true);
                }
                break;
            case 106:
                if (Flags.VINE_GROW.isFalse(plot)) {
                    event.setCancelled(true);
                }
                break;
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onGrow(BlockGrowEvent event) {
        Block b = event.getBlock();
        Location location = NukkitUtil.getLocation(b.getLocation());
        if (location.isUnownedPlotArea()) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        PlotPlayer pp = NukkitUtil.getPlayer(player);
        PlotArea area = pp.getPlotAreaAbs();
        if (area == null) {
            return;
        }
        switch (event.getAction()) {
            case RIGHT_CLICK_BLOCK:
            case LEFT_CLICK_BLOCK:
            case PHYSICAL: {
                Plot plot = pp.getCurrentPlot();
                if (plot == null || !plot.isAdded(pp.getUUID())) {
                    Block block = event.getBlock();
                    if (block != null) {
                        if (plot != null && Flags.USE
                            .contains(plot, PlotBlock.get(block.getId(), block.getDamage()))) {
                            return;
                        }
                    }
                    if (plot == null) {
                        if (Permissions
                            .hasPermission(pp, C.PERMISSION_ADMIN_INTERACT_ROAD.s(), true)) {
                            return;
                        }
                    } else if (!plot.hasOwner()) {
                        if (Permissions
                            .hasPermission(pp, C.PERMISSION_ADMIN_INTERACT_UNOWNED.s(), true)) {
                            return;
                        }
                    } else if (Permissions
                        .hasPermission(pp, C.PERMISSION_ADMIN_INTERACT_OTHER.s(), true)) {
                        return;
                    }
                    event.setCancelled(true);
                    return;
                }
                return;
            }
            case LEFT_CLICK_AIR:
            case RIGHT_CLICK_AIR: {
                Plot plot = pp.getCurrentPlot();
                if (plot == null || !plot.isAdded(pp.getUUID())) {
                    if (plot == null) {
                        if (Permissions
                            .hasPermission(pp, C.PERMISSION_ADMIN_INTERACT_ROAD.s(), true)) {
                            return;
                        }
                    } else if (!plot.hasOwner()) {
                        if (Permissions
                            .hasPermission(pp, C.PERMISSION_ADMIN_INTERACT_UNOWNED.s(), true)) {
                            return;
                        }
                    } else if (Permissions
                        .hasPermission(pp, C.PERMISSION_ADMIN_INTERACT_OTHER.s(), true)) {
                        return;
                    }
                    event.setCancelled(true);
                    return;
                }
                return;
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void creatureSpawnEvent(EntitySpawnEvent event) {
        Entity entity = event.getEntity();
        Location location = NukkitUtil.getLocation(entity.getLocation());
        PlotArea area = location.getPlotArea();
        if (area == null) {
            return;
        }
        Plot plot = area.getOwnedPlotAbs(location);
        if (plot == null) {
            if (!area.MOB_SPAWNING) {
                kill(entity, event);
            }
            return;
        }
        if (checkEntity(entity, plot)) {
            kill(entity, event);
        }
    }

    private void kill(Entity entity, Event event) {
        if (event instanceof Cancellable) {
            event.setCancelled(true);
        }
        entity.kill();
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onEntityFall(EntityBlockChangeEvent event) {
        Entity entity = event.getEntity();
        Location location = NukkitUtil.getLocation(entity);
        if (!PlotSquared.get().hasPlotArea(location.getWorld())) {
            return;
        }
        PlotArea area = location.getPlotArea();
        if (area == null) {
            return;
        }
        Block from = event.getFrom();
        Block to = event.getTo();
        final Plot finalPlot = area.getOwnedPlotAbs(location);
        boolean toBlock = from == null || from.getId() == 0;
        if (toBlock) {
            List<MetadataValue> meta = entity.getMetadata("plot");
            if (meta.isEmpty()) {
                return;
            }
            Plot origin = (Plot) meta.get(0).value();
            if (origin != null && !origin.equals(finalPlot)) {
                kill(entity, event);
            }
        } else {
            entity.setMetadata("plot", new MetadataValue((Plugin) PlotSquared.get().IMP) {
                private Plot plot = finalPlot;

                @Override public Object value() {
                    return plot;
                }

                @Override public void invalidate() {
                    plot = null;
                }
            });
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBurn(BlockBurnEvent event) {
        Block b = event.getBlock();
        Location location = NukkitUtil.getLocation(b.getLocation());

        PlotArea area = location.getPlotArea();
        if (area == null) {
            return;
        }

        Plot plot = location.getOwnedPlot();
        if (plot == null || !plot.getFlag(Flags.BLOCK_BURN, false)) {
            event.setCancelled(true);
            return;
        }

    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBucketEmpty(PlayerBucketEmptyEvent event) {
        Block block = event.getBlockClicked();
        Block b = block.getSide(event.getBlockFace());
        Location location = NukkitUtil.getLocation(b.getLocation());
        PlotArea area = location.getPlotArea();
        if (area == null) {
            return;
        }
        PlotPlayer pp = NukkitUtil.getPlayer(event.getPlayer());
        Plot plot = area.getPlotAbs(location);
        if (plot == null) {
            if (Permissions.hasPermission(pp, C.PERMISSION_ADMIN_BUILD_ROAD)) {
                return;
            }
            MainUtil.sendMessage(pp, C.NO_PERMISSION_EVENT, C.PERMISSION_ADMIN_BUILD_ROAD);
            event.setCancelled(true);
        } else if (!plot.hasOwner()) {
            if (Permissions.hasPermission(pp, C.PERMISSION_ADMIN_BUILD_UNOWNED)) {
                return;
            }
            MainUtil.sendMessage(pp, C.NO_PERMISSION_EVENT, C.PERMISSION_ADMIN_BUILD_UNOWNED);
            event.setCancelled(true);
        } else if (!plot.isAdded(pp.getUUID())) {
            if (Flags.USE.contains(plot, PlotBlock.get(event.getBucket().getId(), 0))) {
                return;
            }
            if (Permissions.hasPermission(pp, C.PERMISSION_ADMIN_BUILD_OTHER)) {
                return;
            }
            MainUtil.sendMessage(pp, C.NO_PERMISSION_EVENT, C.PERMISSION_ADMIN_BUILD_OTHER);
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClose(InventoryCloseEvent event) {
        NukkitUtil.getPlayer(event.getPlayer()).deleteMeta("inventory");
    }

    @EventHandler(priority = EventPriority.MONITOR) public void onLeave(PlayerQuitEvent event) {
        if (TaskManager.TELEPORT_QUEUE.contains(event.getPlayer().getName())) {
            TaskManager.TELEPORT_QUEUE.remove(event.getPlayer().getName());
        }
        PlotPlayer pp = NukkitUtil.getPlayer(event.getPlayer());
        pp.unregister();
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBucketFill(PlayerBucketFillEvent event) {
        Block b = event.getBlockClicked();
        Location location = NukkitUtil.getLocation(b.getLocation());
        PlotArea area = location.getPlotArea();
        if (area == null) {
            return;
        }
        Player player = event.getPlayer();
        PlotPlayer plotPlayer = NukkitUtil.getPlayer(player);
        Plot plot = area.getPlotAbs(location);
        if (plot == null) {
            if (Permissions.hasPermission(plotPlayer, C.PERMISSION_ADMIN_BUILD_ROAD)) {
                return;
            }
            MainUtil.sendMessage(plotPlayer, C.NO_PERMISSION_EVENT, C.PERMISSION_ADMIN_BUILD_ROAD);
            event.setCancelled(true);
        } else if (!plot.hasOwner()) {
            if (Permissions.hasPermission(plotPlayer, C.PERMISSION_ADMIN_BUILD_UNOWNED)) {
                return;
            }
            MainUtil
                .sendMessage(plotPlayer, C.NO_PERMISSION_EVENT, C.PERMISSION_ADMIN_BUILD_UNOWNED);
            event.setCancelled(true);
        } else if (!plot.isAdded(plotPlayer.getUUID())) {
            Optional<HashSet<PlotBlock>> use = plot.getFlag(Flags.USE);
            Block block = event.getBlockClicked();
            if (use.isPresent() && use.get()
                .contains(PlotBlock.get(block.getId(), block.getDamage()))) {
                return;
            }
            if (Permissions.hasPermission(plotPlayer, C.PERMISSION_ADMIN_BUILD_OTHER)) {
                return;
            }
            MainUtil.sendMessage(plotPlayer, C.NO_PERMISSION_EVENT, C.PERMISSION_ADMIN_BUILD_OTHER);
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPotionSplash(PotionCollideEvent event) {
        EntityPotion entity = event.getThrownPotion();
        Location l = NukkitUtil.getLocation(entity);
        if (!PlotSquared.get().hasPlotArea(l.getWorld())) {
            return;
        }
        Entity shooter = entity.shootingEntity;
        if (shooter instanceof Player) {
            PlotPlayer pp = NukkitUtil.getPlayer((Player) shooter);
            Plot plot = l.getOwnedPlotAbs();
            if (plot == null) {
                if (!Permissions.hasPermission(pp, C.PERMISSION_PROJECTILE_UNOWNED)) {
                    kill(entity, event);
                    return;
                }
                return;
            }
            if (plot.isAdded(pp.getUUID()) || Permissions
                .hasPermission(pp, C.PERMISSION_PROJECTILE_OTHER)) {
                return;
            }
            kill(entity, event);
            return;
        }
        if (shooter == null) {
            kill(entity, event);
            return;
        }
    }

    public boolean entityDamage(Entity damager, Entity victim) {
        Location dloc = NukkitUtil.getLocation(damager);
        Location vloc = NukkitUtil.getLocation(victim);
        PlotArea dArea = dloc.getPlotArea();
        PlotArea vArea =
            dArea != null && dArea.contains(vloc.getX(), vloc.getZ()) ? dArea : vloc.getPlotArea();
        if (dArea == null && vArea == null) {
            return true;
        }

        Plot dplot = dArea != null ? dArea.getPlot(dloc) : null;
        Plot vplot = vArea != null ? vArea.getPlot(vloc) : null;

        Plot plot;
        String stub;
        if (dplot == null && vplot == null) {
            if (dArea == null) {
                return true;
            }
            plot = null;
            stub = "road";
        } else {
            // Prioritize plots for close to seamless pvp zones
            if (victim.ticksLived > damager.ticksLived) {
                if (dplot == null || !(victim instanceof Player)) {
                    if (vplot == null) {
                        plot = dplot;
                    } else {
                        plot = vplot;
                    }
                } else {
                    plot = dplot;
                }
            } else if (dplot == null || !(victim instanceof Player)) {
                if (vplot == null) {
                    plot = dplot;
                } else {
                    plot = vplot;
                }
            } else if (vplot == null) {
                plot = dplot;
            } else {
                plot = vplot;
            }
            if (plot.hasOwner()) {
                stub = "other";
            } else {
                stub = "unowned";
            }
        }

        Player player;
        if (damager instanceof Player) { // attacker is player
            player = (Player) damager;
        } else if (damager instanceof EntityProjectile) {
            EntityProjectile projectile = (EntityProjectile) damager;
            Entity shooter = projectile.shootingEntity;
            if (shooter instanceof Player) { // shooter is player
                player = (Player) shooter;
            } else { // shooter is not player
                player = null;
            }
        } else { // Attacker is not player
            player = null;
        }
        if (player != null) {
            PlotPlayer plotPlayer = NukkitUtil.getPlayer(player);
            if (victim instanceof EntityHanging) { // hanging
                if (plot != null && (plot.getFlag(Flags.HANGING_BREAK, false) || plot
                    .isAdded(plotPlayer.getUUID()))) {
                    return true;
                }
                if (!Permissions.hasPermission(plotPlayer, "plots.admin.destroy." + stub)) {
                    MainUtil.sendMessage(plotPlayer, C.NO_PERMISSION_EVENT,
                        "plots.admin.destroy." + stub);
                    return false;
                }
            } else if (false) { // TODO armor stand
                if (plot != null && (plot.getFlag(Flags.MISC_BREAK, false) || plot
                    .isAdded(plotPlayer.getUUID()))) {
                    return true;
                }
                if (!Permissions.hasPermission(plotPlayer, "plots.admin.destroy." + stub)) {
                    MainUtil.sendMessage(plotPlayer, C.NO_PERMISSION_EVENT,
                        "plots.admin.destroy." + stub);
                    return false;
                }
            } else if (victim instanceof EntityMob) { // victim is monster
                if (plot != null && (plot.getFlag(Flags.HOSTILE_ATTACK, false) || plot
                    .getFlag(Flags.PVE, false) || plot.isAdded(plotPlayer.getUUID()))) {
                    return true;
                }
                if (!Permissions.hasPermission(plotPlayer, "plots.admin.pve." + stub)) {
                    MainUtil
                        .sendMessage(plotPlayer, C.NO_PERMISSION_EVENT, "plots.admin.pve." + stub);
                    return false;
                }
            } else if (victim instanceof EntityTameable) {
                if (plot != null && (plot.getFlag(Flags.TAMED_ATTACK, false) || plot
                    .getFlag(Flags.PVE, false) || plot.isAdded(plotPlayer.getUUID()))) {
                    return true;
                }
                if (!Permissions.hasPermission(plotPlayer, "plots.admin.pve." + stub)) {
                    MainUtil
                        .sendMessage(plotPlayer, C.NO_PERMISSION_EVENT, "plots.admin.pve." + stub);
                    return false;
                }
            } else if (victim instanceof Player) {
                if (plot != null) {
                    if (Flags.PVP.isFalse(plot) && !Permissions
                        .hasPermission(plotPlayer, "plots.admin.pvp." + stub)) {
                        MainUtil.sendMessage(plotPlayer, C.NO_PERMISSION_EVENT,
                            "plots.admin.pvp." + stub);
                        return false;
                    } else {
                        return true;
                    }
                }
                if (!Permissions.hasPermission(plotPlayer, "plots.admin.pvp." + stub)) {
                    MainUtil
                        .sendMessage(plotPlayer, C.NO_PERMISSION_EVENT, "plots.admin.pvp." + stub);
                    return false;
                }
            } else if (victim instanceof EntityCreature) { // victim is animal
                if (plot != null && (plot.getFlag(Flags.ANIMAL_ATTACK, false) || plot
                    .getFlag(Flags.PVE, false) || plot.isAdded(plotPlayer.getUUID()))) {
                    return true;
                }
                if (!Permissions.hasPermission(plotPlayer, "plots.admin.pve." + stub)) {
                    MainUtil
                        .sendMessage(plotPlayer, C.NO_PERMISSION_EVENT, "plots.admin.pve." + stub);
                    return false;
                }
            } else if (victim instanceof EntityVehicle) { // Vehicles are managed in vehicle destroy event
                return true;
            } else { // victim is something else
                if (plot != null && (plot.getFlag(Flags.PVE, false) || plot
                    .isAdded(plotPlayer.getUUID()))) {
                    return true;
                }
                if (!Permissions.hasPermission(plotPlayer, "plots.admin.pve." + stub)) {
                    MainUtil
                        .sendMessage(plotPlayer, C.NO_PERMISSION_EVENT, "plots.admin.pve." + stub);
                    return false;
                }
            }
            return true;
        }
        // player is null
        return !(damager instanceof EntityProjectile && !(victim instanceof EntityCreature));
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void blockCreate(BlockPlaceEvent event) {
        Location location = NukkitUtil.getLocation(event.getBlock().getLocation());
        PlotArea area = location.getPlotArea();
        if (area == null) {
            return;
        }
        Player player = event.getPlayer();
        PlotPlayer pp = NukkitUtil.getPlayer(player);
        Plot plot = area.getPlotAbs(location);
        if (plot != null) {
            if (!plot.hasOwner()) {
                if (!Permissions.hasPermission(pp, C.PERMISSION_ADMIN_BUILD_UNOWNED)) {
                    MainUtil
                        .sendMessage(pp, C.NO_PERMISSION_EVENT, C.PERMISSION_ADMIN_BUILD_UNOWNED);
                    event.setCancelled(true);
                    return;
                }
            } else if (!plot.isAdded(pp.getUUID())) {
                Set<PlotBlock> place = plot.getFlag(Flags.PLACE, null);
                if (place != null) {
                    Block block = event.getBlock();
                    if (place.contains(PlotBlock.get((short) block.getId(), block.getDamage()))) {
                        return;
                    }
                }
                if (!Permissions.hasPermission(pp, C.PERMISSION_ADMIN_BUILD_OTHER)) {
                    MainUtil.sendMessage(pp, C.NO_PERMISSION_EVENT, C.PERMISSION_ADMIN_BUILD_OTHER);
                    event.setCancelled(true);
                    return;
                }
            } else if (Settings.Done.RESTRICT_BUILDING && plot.getFlags().containsKey(Flags.DONE)) {
                if (!Permissions.hasPermission(pp, C.PERMISSION_ADMIN_BUILD_OTHER)) {
                    MainUtil.sendMessage(pp, C.NO_PERMISSION_EVENT, C.PERMISSION_ADMIN_BUILD_OTHER);
                    event.setCancelled(true);
                    return;
                }
            }
            if (location.getY() > area.MAX_BUILD_HEIGHT && location.getY() < area.MIN_BUILD_HEIGHT
                && !Permissions.hasPermission(pp, C.PERMISSION_ADMIN_BUILD_HEIGHTLIMIT)) {
                event.setCancelled(true);
                MainUtil.sendMessage(pp,
                    C.HEIGHT_LIMIT.s().replace("{limit}", String.valueOf(area.MAX_BUILD_HEIGHT)));
            }
        } else if (!Permissions.hasPermission(pp, C.PERMISSION_ADMIN_BUILD_OTHER)) {
            MainUtil.sendMessage(pp, C.NO_PERMISSION_EVENT, C.PERMISSION_ADMIN_BUILD_ROAD);
            event.setCancelled(true);
        }
    }
}
