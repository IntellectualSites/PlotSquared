package com.plotsquared.bukkit.listeners;

import com.google.common.base.Optional;
import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.config.Settings;
import com.intellectualcrafters.plot.flag.Flags;
import com.intellectualcrafters.plot.flag.IntegerFlag;
import com.intellectualcrafters.plot.object.*;
import com.intellectualcrafters.plot.util.*;
import com.plotsquared.bukkit.BukkitMain;
import com.plotsquared.bukkit.object.BukkitLazyBlock;
import com.plotsquared.bukkit.object.BukkitPlayer;
import com.plotsquared.bukkit.util.BukkitUtil;
import com.plotsquared.bukkit.util.BukkitVersion;
import com.plotsquared.listener.PlayerBlockEventType;
import com.plotsquared.listener.PlotListener;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.*;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.vehicle.VehicleCreateEvent;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.event.vehicle.VehicleEntityCollisionEvent;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.event.world.StructureGrowEvent;
import org.bukkit.help.HelpTopic;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.projectiles.BlockProjectileSource;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.util.Vector;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Pattern;

/**
 * Player Events involving plots.
 */
public class PlayerEvents extends PlotListener implements Listener {

    private boolean pistonBlocks = true;
    private float lastRadius;
    // To prevent recursion
    private boolean tmpTeleport = true;

    private boolean v112 =
        PS.get().checkVersion(PS.imp().getServerVersion(), BukkitVersion.v1_12_0);

    public static void sendBlockChange(final org.bukkit.Location bloc, final Material type,
        final byte data) {
        TaskManager.runTaskLater(() -> {
            String world = bloc.getWorld().getName();
            int x = bloc.getBlockX();
            int z = bloc.getBlockZ();
            int distance = Bukkit.getViewDistance() * 16;
            for (Entry<String, PlotPlayer> entry : UUIDHandler.getPlayers().entrySet()) {
                PlotPlayer player = entry.getValue();
                Location loc = player.getLocation();
                if (loc.getWorld().equals(world)) {
                    if (16 * Math.abs(loc.getX() - x) / 16 > distance
                        || 16 * Math.abs(loc.getZ() - z) / 16 > distance) {
                        continue;
                    }
                    ((BukkitPlayer) player).player.sendBlockChange(bloc, type, data);
                }
            }
        }, 3);
    }

    public static boolean checkEntity(Entity entity, Plot plot) {
        if (plot == null || !plot.hasOwner() || plot.getFlags().isEmpty() && plot
            .getArea().DEFAULT_FLAGS.isEmpty()) {
            return false;
        }
        switch (entity.getType()) {
            case PLAYER:
                return false;
            case LLAMA_SPIT:
            case SMALL_FIREBALL:
            case FIREBALL:
            case DROPPED_ITEM:
            case EGG:
            case THROWN_EXP_BOTTLE:
            case SPLASH_POTION:
            case LINGERING_POTION:
            case SNOWBALL:
            case ENDER_PEARL:
            case ARROW:
            case TIPPED_ARROW:
            case SPECTRAL_ARROW:
            case SHULKER_BULLET:
            case DRAGON_FIREBALL:
                // projectile
            case PRIMED_TNT:
            case FALLING_BLOCK:
                // Block entities
            case ENDER_CRYSTAL:
            case COMPLEX_PART:
            case FISHING_HOOK:
            case ENDER_SIGNAL:
            case EXPERIENCE_ORB:
            case LEASH_HITCH:
            case FIREWORK:
            case WEATHER:
            case AREA_EFFECT_CLOUD:
            case LIGHTNING:
            case WITHER_SKULL:
            case EVOKER_FANGS:
            case UNKNOWN:
                // non moving / unmovable
                return checkEntity(plot, Flags.ENTITY_CAP);
            case ITEM_FRAME:
            case PAINTING:
            case ARMOR_STAND:
                return checkEntity(plot, Flags.ENTITY_CAP, Flags.MISC_CAP);
            // misc
            case MINECART:
            case MINECART_CHEST:
            case MINECART_COMMAND:
            case MINECART_FURNACE:
            case MINECART_HOPPER:
            case MINECART_MOB_SPAWNER:
            case MINECART_TNT:
            case BOAT:
                return checkEntity(plot, Flags.ENTITY_CAP, Flags.VEHICLE_CAP);
            case POLAR_BEAR:
            case RABBIT:
            case SHEEP:
            case MUSHROOM_COW:
            case OCELOT:
            case PIG:
            case SQUID:
            case VILLAGER:
            case IRON_GOLEM:
            case WOLF:
            case CHICKEN:
            case COW:
            case SNOWMAN:
            case BAT:
            case HORSE:
            case DONKEY:
            case LLAMA:
            case MULE:
            case ZOMBIE_HORSE:
            case SKELETON_HORSE:
            case PARROT:
                // animal
                return checkEntity(plot, Flags.ENTITY_CAP, Flags.MOB_CAP, Flags.ANIMAL_CAP);
            case ILLUSIONER:
            case BLAZE:
            case CAVE_SPIDER:
            case CREEPER:
            case ENDERMAN:
            case ENDERMITE:
            case ENDER_DRAGON:
            case GHAST:
            case GIANT:
            case GUARDIAN:
            case MAGMA_CUBE:
            case PIG_ZOMBIE:
            case SILVERFISH:
            case SKELETON:
            case SLIME:
            case SPIDER:
            case WITCH:
            case WITHER:
            case ZOMBIE:
            case SHULKER:
            case HUSK:
            case STRAY:
            case ELDER_GUARDIAN:
            case WITHER_SKELETON:
            case VINDICATOR:
            case EVOKER:
            case VEX:
            case ZOMBIE_VILLAGER:
                // monster
                return checkEntity(plot, Flags.ENTITY_CAP, Flags.MOB_CAP, Flags.HOSTILE_CAP);
            default:
                if (entity instanceof LivingEntity) {
                    if (entity instanceof Animals) {
                        return checkEntity(plot, Flags.ENTITY_CAP, Flags.MOB_CAP, Flags.ANIMAL_CAP);
                    } else if (entity instanceof Monster) {
                        return checkEntity(plot, Flags.ENTITY_CAP, Flags.MOB_CAP,
                            Flags.HOSTILE_CAP);
                    } else {
                        return checkEntity(plot, Flags.ENTITY_CAP, Flags.MOB_CAP);
                    }
                }
                if (entity instanceof Vehicle) {
                    return checkEntity(plot, Flags.ENTITY_CAP, Flags.VEHICLE_CAP);
                }
                if (entity instanceof Hanging) {
                    return checkEntity(plot, Flags.ENTITY_CAP, Flags.MISC_CAP);
                }
                return checkEntity(plot, Flags.ENTITY_CAP);
        }
    }

    @EventHandler public void onRedstoneEvent(BlockRedstoneEvent event) {
        Block block = event.getBlock();
        switch (block.getType()) {
            case OBSERVER:
            case REDSTONE_LAMP_OFF:
            case REDSTONE_WIRE:
            case REDSTONE_LAMP_ON:
            case PISTON_BASE:
            case PISTON_STICKY_BASE:
            case IRON_DOOR_BLOCK:
            case LEVER:
            case WOODEN_DOOR:
            case FENCE_GATE:
            case WOOD_BUTTON:
            case STONE_BUTTON:
            case IRON_PLATE:
            case WOOD_PLATE:
            case STONE_PLATE:
            case GOLD_PLATE:
            case SPRUCE_DOOR:
            case BIRCH_DOOR:
            case JUNGLE_DOOR:
            case ACACIA_DOOR:
            case DARK_OAK_DOOR:
            case IRON_TRAPDOOR:
            case SPRUCE_FENCE_GATE:
            case BIRCH_FENCE_GATE:
            case JUNGLE_FENCE_GATE:
            case ACACIA_FENCE_GATE:
            case DARK_OAK_FENCE_GATE:
            case POWERED_RAIL:
                return;
            default:
                Location loc = BukkitUtil.getLocation(block.getLocation());
                PlotArea area = loc.getPlotArea();
                if (area == null) {
                    return;
                }
                Plot plot = area.getOwnedPlot(loc);
                if (plot == null) {
                    return;
                }
                if (Flags.REDSTONE.isFalse(plot)) {
                    event.setNewCurrent(0);
                    return;
                }
                if (Settings.Redstone.DISABLE_OFFLINE) {
                    boolean disable;
                    if (plot.isMerged()) {
                        disable = true;
                        for (UUID owner : plot.getOwners()) {
                            if (UUIDHandler.getPlayer(owner) != null) {
                                disable = false;
                                break;
                            }
                        }
                    } else {
                        disable = UUIDHandler.getPlayer(plot.owner) == null;
                    }
                    if (disable) {
                        for (UUID trusted : plot.getTrusted()) {
                            if (UUIDHandler.getPlayer(trusted) != null) {
                                disable = false;
                                break;
                            }
                        }
                        if (disable) {
                            event.setNewCurrent(0);
                            return;
                        }
                    }
                }
                if (Settings.Redstone.DISABLE_UNOCCUPIED) {
                    for (Entry<String, PlotPlayer> entry : UUIDHandler.getPlayers().entrySet()) {
                        if (plot.equals(entry.getValue().getCurrentPlot())) {
                            return;
                        }
                    }
                    event.setNewCurrent(0);
                }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onPhysicsEvent(BlockPhysicsEvent event) {
        switch (event.getChangedType()) {
            case REDSTONE_COMPARATOR_OFF:
            case REDSTONE_COMPARATOR_ON: {
                Block block = event.getBlock();
                Location loc = BukkitUtil.getLocation(block.getLocation());
                PlotArea area = loc.getPlotArea();
                if (area == null) {
                    return;
                }
                Plot plot = area.getOwnedPlotAbs(loc);
                if (plot == null) {
                    return;
                }
                if (Flags.REDSTONE.isFalse(plot)) {
                    event.setCancelled(true);
                }
                return;
            }
            case DRAGON_EGG:
            case ANVIL:
            case SAND:
            case CONCRETE_POWDER:
            case GRAVEL: {
                Block block = event.getBlock();
                Location loc = BukkitUtil.getLocation(block.getLocation());
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
                return;
            }
            default:
                if (Settings.Redstone.DETECT_INVALID_EDGE_PISTONS) {
                    Block block = event.getBlock();
                    switch (block.getType()) {
                        case PISTON_BASE:
                        case PISTON_STICKY_BASE:
                            Location loc = BukkitUtil.getLocation(block.getLocation());
                            PlotArea area = loc.getPlotArea();
                            if (area == null) {
                                return;
                            }
                            Plot plot = area.getOwnedPlotAbs(loc);
                            if (plot == null) {
                                return;
                            }
                            int data = block.getData();
                            switch (data) {
                                case 5:
                                case 13:
                                    loc.setX(loc.getX() + 1);
                                    break;
                                case 4:
                                case 12:
                                    loc.setX(loc.getX() - 1);
                                    break;
                                case 3:
                                case 11:
                                    loc.setZ(loc.getZ() + 1);
                                    break;
                                case 2:
                                case 10:
                                    loc.setZ(loc.getZ() - 1);
                                    break;
                            }
                            Plot newPlot = area.getOwnedPlotAbs(loc);
                            if (!plot.equals(newPlot)) {
                                event.setCancelled(true);
                                return;
                            }
                    }
                }
                break;
        }
    }

    @EventHandler public void onProjectileLaunch(ProjectileLaunchEvent event) {
        Projectile entity = event.getEntity();
        if (!(entity instanceof ThrownPotion)) {
            return;
        }
        ProjectileSource shooter = entity.getShooter();
        if (!(shooter instanceof Player)) {
            return;
        }
        Location l = BukkitUtil.getLocation(entity);
        if (!PS.get().hasPlotArea(l.getWorld())) {
            return;
        }
        PlotPlayer pp = BukkitUtil.getPlayer((Player) shooter);
        Plot plot = l.getOwnedPlot();
        if (plot != null && !plot.isAdded(pp.getUUID())) {
            entity.remove();
            event.setCancelled(true);
        }
    }

    @EventHandler public boolean onProjectileHit(ProjectileHitEvent event) {
        Projectile entity = event.getEntity();
        Location loc = BukkitUtil.getLocation(entity);
        if (!PS.get().hasPlotArea(loc.getWorld())) {
            return true;
        }
        PlotArea area = loc.getPlotArea();
        if (area == null) {
            return true;
        }
        Plot plot = area.getPlot(loc);
        ProjectileSource shooter = entity.getShooter();
        if (shooter instanceof Player) {
            PlotPlayer pp = BukkitUtil.getPlayer((Player) shooter);
            if (plot == null) {
                if (!Permissions.hasPermission(pp, C.PERMISSION_PROJECTILE_UNOWNED)) {
                    entity.remove();
                    return false;
                }
                return true;
            }
            if (plot.isAdded(pp.getUUID()) || Permissions
                .hasPermission(pp, C.PERMISSION_PROJECTILE_OTHER)) {
                return true;
            }
            entity.remove();
            return false;
        }
        if (!(shooter instanceof Entity) && shooter != null) {
            if (plot == null) {
                entity.remove();
                return false;
            }
            Location sLoc =
                BukkitUtil.getLocation(((BlockProjectileSource) shooter).getBlock().getLocation());
            if (!area.contains(sLoc.getX(), sLoc.getZ())) {
                entity.remove();
                return false;
            }
            Plot sPlot = area.getOwnedPlotAbs(sLoc);
            if (sPlot == null || !PlotHandler.sameOwners(plot, sPlot)) {
                entity.remove();
                return false;
            }
        }
        return true;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void playerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        PlotPlayer pp = BukkitUtil.getPlayer(player);
        EventUtil.manager.doRespawnTask(pp);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void playerCommand(PlayerCommandPreprocessEvent event) {
        String msg = event.getMessage().toLowerCase().replaceAll("/", "").trim();
        if (msg.isEmpty()) {
            return;
        }
        Player player = event.getPlayer();
        PlotPlayer pp = BukkitUtil.getPlayer(player);
        Location loc = pp.getLocation();
        PlotArea area = loc.getPlotArea();
        if (area == null) {
            return;
        }
        String[] parts = msg.split(" ");
        Plot plot = pp.getCurrentPlot();
        if (BukkitMain.worldEdit != null) { // Check WorldEdit
            switch (parts[0].toLowerCase()) {
                case "up":
                case "/up":
                case "worldedit:up":
                case "worldedit:/up":
                    if (plot == null || (!plot.isAdded(pp.getUUID()) && !Permissions
                        .hasPermission(pp, C.PERMISSION_ADMIN_BUILD_OTHER, true))) {
                        event.setCancelled(true);
                        return;
                    }
            }
        }
        if (plot == null) {
            return;
        }
        Optional<List<String>> flag = plot.getFlag(Flags.BLOCKED_CMDS);
        if (flag.isPresent() && !Permissions
            .hasPermission(pp, C.PERMISSION_ADMIN_INTERACT_BLOCKED_CMDS)) {
            List<String> blocked_cmds = flag.get();
            String c = parts[0];
            if (parts[0].contains(":")) {
                c = parts[0].split(":")[1];
                msg = msg.replace(parts[0].split(":")[0] + ':', "");
            }
            String l = c;
            List<String> aliases = new ArrayList<>();
            for (HelpTopic cmdLabel : Bukkit.getServer().getHelpMap().getHelpTopics()) {
                if (c.equals(cmdLabel.getName())) {
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
                        if (!a.equals(label) && a.equals(c)) {
                            c = label;
                            break;
                        }
                    }
                }
            }
            if (!l.equals(c)) {
                msg = msg.replace(l, c);
            }
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

    private Field fieldPlayer;

    {
        try {
            fieldPlayer = PlayerEvent.class.getDeclaredField("player");
            fieldPlayer.setAccessible(true);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    private PlayerMoveEvent moveTmp;

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onConnect(PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        UUIDHandler.getPlayers().remove(player.getName());
        BukkitUtil.removePlayer(player.getName());
        final PlotPlayer pp = BukkitUtil.getPlayer(player);
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
        TaskManager.runTaskLaterAsync(() -> {
            if (!player.hasPlayedBefore() && player.isOnline()) {
                player.saveData();
            }
            EventUtil.manager.doJoinTask(pp);
        }, 20);
    }

    @EventHandler public void onVehicleEntityCollision(VehicleEntityCollisionEvent e) {
        if (e.getVehicle().getType() == EntityType.BOAT) {
            Location location = BukkitUtil.getLocation(e.getEntity());
            if (location.getPlotArea() == null) {
                //we don't care about events that happen outside of a plot area.
                return;
            }
            if (e.getEntity() instanceof Player) {
                PlotPlayer player = BukkitUtil.getPlayer((Player) e.getEntity());
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

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onTeleport(PlayerTeleportEvent event) {
        if (event.getTo() == null || event.getFrom() == null || !event.getFrom().getWorld()
            .equals(event.getTo().getWorld())) {
            BukkitUtil.getPlayer(event.getPlayer()).deleteMeta("location");
            BukkitUtil.getPlayer(event.getPlayer()).deleteMeta("lastplot");
            org.bukkit.Location to = event.getTo();
            if (to != null) {
                Player player = event.getPlayer();
                PlotPlayer pp = PlotPlayer.wrap(player);
                Location loc = BukkitUtil.getLocation(to);
                PlotArea area = PS.get().getPlotAreaAbs(loc);
                if (area == null) {
                    return;
                }
                Plot plot = area.getPlot(loc);
                if (plot != null) {
                    plotEntry(pp, plot);
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


            Entity passenger = vehicle.getPassenger();

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

                List<Entity> passengers;
                if (v112) {
                    passengers = vehicle.getPassengers();
                } else {
                    passengers = null;
                }

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
                    if (passengers != null) {
                        vehicle.eject();
                        vehicle.setVelocity(new Vector(0d, 0d, 0d));
                        vehicle.teleport(dest);
                        for (final Entity entity : passengers) {
                            vehicle.addPassenger(entity);
                        }
                    } else {
                        vehicle.eject();
                        vehicle.setVelocity(new Vector(0d, 0d, 0d));
                        vehicle.teleport(dest);
                        vehicle.setPassenger(player);
                    }
                    return;
                }
            }
            if (Settings.Enabled_Components.KILL_ROAD_VEHICLES) {
                switch (vehicle.getType()) {
                    case MINECART:
                    case MINECART_CHEST:
                    case MINECART_COMMAND:
                    case MINECART_FURNACE:
                    case MINECART_HOPPER:
                    case MINECART_MOB_SPAWNER:
                    case ENDER_CRYSTAL:
                    case MINECART_TNT:
                    case BOAT: {
                        List<MetadataValue> meta = vehicle.getMetadata("plot");
                        Plot toPlot = BukkitUtil.getLocation(to).getPlot();
                        if (!meta.isEmpty()) {
                            Plot origin = (Plot) meta.get(0).value();
                            if (!origin.getBasePlot(false).equals(toPlot)) {
                                vehicle.remove();
                            }
                        } else if (toPlot != null) {
                            vehicle.setMetadata("plot",
                                new FixedMetadataValue((Plugin) PS.get().IMP, toPlot));
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
            PlotPlayer pp = BukkitUtil.getPlayer(player);
            // Cancel teleport
            TaskManager.TELEPORT_QUEUE.remove(pp.getName());
            // Set last location
            Location loc = BukkitUtil.getLocation(to);
            pp.setMeta("location", loc);
            PlotArea area = loc.getPlotArea();
            if (area == null) {
                pp.deleteMeta("lastplot");
                return;
            }
            Plot now = area.getPlot(loc);
            Plot lastPlot = pp.getMeta("lastplot");
            if (now == null) {
                if (lastPlot != null && !plotExit(pp, lastPlot) && this.tmpTeleport && !pp
                    .getMeta("kick", false)) {
                    MainUtil.sendMessage(pp, C.NO_PERMISSION_EVENT, C.PERMISSION_ADMIN_EXIT_DENIED);
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
                MainUtil.sendMessage(pp, C.NO_PERMISSION_EVENT, C.PERMISSION_ADMIN_ENTRY_DENIED);
                this.tmpTeleport = false;
                to.setX(from.getBlockX());
                to.setY(from.getBlockY());
                to.setZ(from.getBlockZ());
                player.teleport(event.getTo());
                this.tmpTeleport = true;
                return;
            }
            Integer border = area.getBorder();
            if (x2 > border && this.tmpTeleport) {
                to.setX(border - 1);
                this.tmpTeleport = false;
                player.teleport(event.getTo());
                this.tmpTeleport = true;
                MainUtil.sendMessage(pp, C.BORDER);
            }
            if (x2 < -border && this.tmpTeleport) {
                to.setX(-border + 1);
                this.tmpTeleport = false;
                player.teleport(event.getTo());
                this.tmpTeleport = true;
                MainUtil.sendMessage(pp, C.BORDER);
            }
        }
        int z2;
        if (MathMan.roundInt(from.getZ()) != (z2 = MathMan.roundInt(to.getZ()))) {
            Player player = event.getPlayer();
            PlotPlayer pp = BukkitUtil.getPlayer(player);
            // Cancel teleport
            TaskManager.TELEPORT_QUEUE.remove(pp.getName());
            // Set last location
            Location loc = BukkitUtil.getLocation(to);
            pp.setMeta("location", loc);
            PlotArea area = loc.getPlotArea();
            if (area == null) {
                pp.deleteMeta("lastplot");
                return;
            }
            Plot now = area.getPlot(loc);
            Plot lastPlot = pp.getMeta("lastplot");
            if (now == null) {
                if (lastPlot != null && !plotExit(pp, lastPlot) && this.tmpTeleport && !pp
                    .getMeta("kick", false)) {
                    MainUtil.sendMessage(pp, C.NO_PERMISSION_EVENT, C.PERMISSION_ADMIN_EXIT_DENIED);
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
                MainUtil.sendMessage(pp, C.NO_PERMISSION_EVENT, C.PERMISSION_ADMIN_ENTRY_DENIED);
                this.tmpTeleport = false;
                player.teleport(from);
                to.setX(from.getBlockX());
                to.setY(from.getBlockY());
                to.setZ(from.getBlockZ());
                player.teleport(event.getTo());
                this.tmpTeleport = true;
                return;
            }
            Integer border = area.getBorder();
            if (z2 > border && this.tmpTeleport) {
                to.setZ(border - 1);
                this.tmpTeleport = false;
                player.teleport(event.getTo());
                this.tmpTeleport = true;
                MainUtil.sendMessage(pp, C.BORDER);
            } else if (z2 < -border && this.tmpTeleport) {
                to.setZ(-border + 1);
                this.tmpTeleport = false;
                player.teleport(event.getTo());
                this.tmpTeleport = true;
                MainUtil.sendMessage(pp, C.BORDER);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW) public void onChat(AsyncPlayerChatEvent event) {
        if (event.isCancelled()) {
            return;
        }

        PlotPlayer plotPlayer = BukkitUtil.getPlayer(event.getPlayer());
        Location location = plotPlayer.getLocation();
        PlotArea area = location.getPlotArea();
        if (area == null || (area.PLOT_CHAT == plotPlayer.getAttribute("chat"))) {
            return;
        }
        Plot plot = area.getPlot(location);
        if (plot == null) {
            return;
        }
        event.setCancelled(true);
        String message = event.getMessage();
        String format = C.PLOT_CHAT_FORMAT.s();
        String sender = event.getPlayer().getDisplayName();
        PlotId id = plot.getId();
        Set<Player> recipients = event.getRecipients();
        Set<Player> spies = new HashSet<>();
        recipients.clear();
        for (Entry<String, PlotPlayer> entry : UUIDHandler.getPlayers().entrySet()) {
            PlotPlayer pp = entry.getValue();
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
            format.replace("%plot_id%", id.x + ";" + id.y).replace("%sender%", sender));
        if (plotPlayer.hasPermission("plots.chat.color")) {
            message = C.color(message);
        }
        String full = partial.replace("%msg%", message);
        for (Player receiver : recipients) {
            receiver.sendMessage(full);
        }
        if (!spies.isEmpty()) {
            String spyMessage = C.PLOT_CHAT_SPY_FORMAT.s().replace("%plot_id%", id.x + ";" + id.y)
                .replace("%sender%", sender).replace("%msg%", message);
            for (Player player : spies) {
                player.sendMessage(spyMessage);
            }
        }
        PS.debug(full);
    }

    @EventHandler(priority = EventPriority.LOWEST) public void blockDestroy(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Location location = BukkitUtil.getLocation(event.getBlock().getLocation());
        PlotArea area = location.getPlotArea();
        if (area == null) {
            return;
        }
        Plot plot = area.getPlot(location);
        if (plot != null) {
            PlotPlayer plotPlayer = BukkitUtil.getPlayer(player);
            if (event.getBlock().getY() == 0) {
                if (!Permissions
                    .hasPermission(plotPlayer, C.PERMISSION_ADMIN_DESTROY_GROUNDLEVEL)) {
                    MainUtil.sendMessage(plotPlayer, C.NO_PERMISSION_EVENT,
                        C.PERMISSION_ADMIN_DESTROY_GROUNDLEVEL);
                    event.setCancelled(true);
                    return;
                }
            } else if (
                (location.getY() > area.MAX_BUILD_HEIGHT || location.getY() < area.MIN_BUILD_HEIGHT)
                    && !Permissions
                    .hasPermission(plotPlayer, C.PERMISSION_ADMIN_BUILD_HEIGHTLIMIT)) {
                event.setCancelled(true);
                MainUtil.sendMessage(plotPlayer,
                    C.HEIGHT_LIMIT.s().replace("{limit}", String.valueOf(area.MAX_BUILD_HEIGHT)));
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
                //noinspection deprecation
                if (destroy.isPresent() && destroy.get()
                    .contains(PlotBlock.get((short) block.getTypeId(), block.getData()))) {
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
        PlotPlayer pp = BukkitUtil.getPlayer(player);
        if (Permissions.hasPermission(pp, C.PERMISSION_ADMIN_DESTROY_ROAD)) {
            return;
        }
        if (PS.get().worldedit != null && pp.getAttribute("worldedit")) {
            //noinspection deprecation
            if (player.getItemInHand().getTypeId() == PS.get().worldedit
                .getConfiguration().wandItem) {
                return;
            }
        }
        MainUtil.sendMessage(pp, C.NO_PERMISSION_EVENT, C.PERMISSION_ADMIN_DESTROY_ROAD);
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onWorldChanged(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        PlotPlayer pp = BukkitUtil.getPlayer(player);
        // Delete last location
        pp.deleteMeta("location");
        Plot plot = (Plot) pp.deleteMeta("lastplot");
        if (plot != null) {
            plotExit(pp, plot);
        }
        if (PS.get().worldedit != null) {
            if (!Permissions.hasPermission(pp, C.PERMISSION_WORLDEDIT_BYPASS)) {
                if (pp.getAttribute("worldedit")) {
                    pp.removeAttribute("worldedit");
                }
            }
        }
        if (Settings.Enabled_Components.PERMISSION_CACHE) {
            pp.deleteMeta("perm");
        }
        Location loc = pp.getLocation();
        PlotArea area = PS.get().getPlotAreaAbs(loc);
        if (area == null) {
            return;
        }
        plot = area.getPlot(loc);
        if (plot != null) {
            plotEntry(pp, plot);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBigBoom(EntityExplodeEvent event) {
        Location location = BukkitUtil.getLocation(event.getLocation());
        PlotArea area = location.getPlotArea();
        if (area == null) {
            if (!PS.get().hasPlotArea(location.getWorld())) {
                return;
            }
            Iterator<Block> iterator = event.blockList().iterator();
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
                if (this.lastRadius != 0) {
                    List<Entity> nearby = event.getEntity()
                        .getNearbyEntities(this.lastRadius, this.lastRadius, this.lastRadius);
                    for (Entity near : nearby) {
                        if (near instanceof TNTPrimed || near.getType()
                            .equals(EntityType.MINECART_TNT)) {
                            if (!near.hasMetadata("plot")) {
                                near.setMetadata("plot",
                                    new FixedMetadataValue((Plugin) PS.get().IMP, plot));
                            }
                        }
                    }
                    this.lastRadius = 0;
                }
                Iterator<Block> iterator = event.blockList().iterator();
                while (iterator.hasNext()) {
                    Block block = iterator.next();
                    location = BukkitUtil.getLocation(block.getLocation());
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
    public void onEntityBlockForm(EntityBlockFormEvent event) {
        String world = event.getBlock().getWorld().getName();
        if (!PS.get().hasPlotArea(world)) {
            return;
        }
        Location location = BukkitUtil.getLocation(event.getBlock().getLocation());
        PlotArea area = location.getPlotArea();
        if (area == null) {
            return;
        }
        Plot plot = area.getOwnedPlot(location);
        if (plot == null) {
            event.setCancelled(true);
            return;
        }
        Entity entity = event.getEntity();
        if (entity instanceof Player) {
            Player player = (Player) entity;
            if (!plot.hasOwner()) {
                PlotPlayer plotPlayer = BukkitUtil.getPlayer(player);
                if (Flags.ICE_FORM.isTrue(plot)) {
                    return;
                }
                event.setCancelled(true);
                return;
            }
            PlotPlayer plotPlayer = BukkitUtil.getPlayer(player);
            if (!plot.isAdded(plotPlayer.getUUID())) {
                if (Flags.ICE_FORM.isTrue(plot)) {
                    return;
                }
                event.setCancelled(true);
                return;
            }
            return;
        }
        if (!Flags.ICE_FORM.isTrue(plot)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockSpread(BlockSpreadEvent event) {
        Block block = event.getBlock();
        Location location = BukkitUtil.getLocation(block.getLocation());
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
        switch (event.getSource().getType()) {
            case GRASS:
                if (Flags.GRASS_GROW.isFalse(plot)) {
                    event.setCancelled(true);
                }
                break;
            case MYCEL:
                if (Flags.MYCEL_GROW.isFalse(plot)) {
                    event.setCancelled(true);
                }
                break;
            case VINE:
                if (Flags.VINE_GROW.isFalse(plot)) {
                    event.setCancelled(true);
                }
                break;
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockForm(BlockFormEvent event) {
        Block block = event.getBlock();
        Location location = BukkitUtil.getLocation(block.getLocation());
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
        switch (event.getNewState().getType()) {
            case SNOW:
            case SNOW_BLOCK:
                if (Flags.SNOW_FORM.isFalse(plot)) {
                    event.setCancelled(true);
                }
                return;
            case ICE:
            case FROSTED_ICE:
            case PACKED_ICE:
                if (Flags.ICE_FORM.isFalse(plot)) {
                    event.setCancelled(true);
                }
                return;
            case STONE:
            case OBSIDIAN:
            case COBBLESTONE:
                // TODO event ?
                return;
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPeskyMobsChangeTheWorldLikeWTFEvent(EntityChangeBlockEvent event) {
        Entity e = event.getEntity();
        if (!(e instanceof FallingBlock)) {
            Location location = BukkitUtil.getLocation(event.getBlock().getLocation());
            PlotArea area = location.getPlotArea();
            if (area != null) {
                Plot plot = area.getOwnedPlot(location);
                if (plot != null && Flags.MOB_BREAK.isTrue(plot)) {
                    return;
                }
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onFade(BlockFadeEvent event) {
        Block b = event.getBlock();
        Location location = BukkitUtil.getLocation(b.getLocation());
        PlotArea area = location.getPlotArea();
        if (area == null) {
            return;
        }
        Plot plot = area.getOwnedPlot(location);
        if (plot == null) {
            event.setCancelled(true);
            return;
        }
        switch (b.getType()) {
            case ICE:
                if (Flags.ICE_MELT.isFalse(plot)) {
                    event.setCancelled(true);
                }
                break;
            case SNOW:
                if (Flags.SNOW_MELT.isFalse(plot)) {
                    event.setCancelled(true);
                }
                break;
            case SOIL:
                if (Flags.SOIL_DRY.isFalse(plot)) {
                    event.setCancelled(true);
                }
                break;
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockDamage(BlockDamageEvent event) {
        Player player = event.getPlayer();
        Location location = BukkitUtil.getLocation(event.getBlock().getLocation());
        if (player == null) {
            if (location.isPlotRoad()) {
                event.setCancelled(true);
                return;
            }
        }
        PlotArea area = location.getPlotArea();
        if (area == null) {
            return;
        }
        Plot plot = area.getPlot(location);
        if (plot != null) {
            if (location.getY() == 0) {
                event.setCancelled(true);
                return;
            }
            if (!plot.hasOwner()) {
                PlotPlayer plotPlayer = BukkitUtil.getPlayer(player);
                if (Permissions.hasPermission(plotPlayer, C.PERMISSION_ADMIN_DESTROY_UNOWNED)) {
                    return;
                }
                event.setCancelled(true);
                return;
            }
            PlotPlayer plotPlayer = BukkitUtil.getPlayer(player);
            if (!plot.isAdded(plotPlayer.getUUID())) {
                Optional<HashSet<PlotBlock>> destroy = plot.getFlag(Flags.BREAK);
                Block block = event.getBlock();
                //noinspection deprecation
                if (destroy.isPresent() && destroy.get()
                    .contains(PlotBlock.get((short) block.getTypeId(), block.getData()))
                    || Permissions.hasPermission(plotPlayer, C.PERMISSION_ADMIN_DESTROY_OTHER)) {
                    return;
                }
                event.setCancelled(true);
                return;
            }
            return;
        }
        PlotPlayer plotPlayer = BukkitUtil.getPlayer(player);
        if (Permissions.hasPermission(plotPlayer, C.PERMISSION_ADMIN_DESTROY_ROAD)) {
            return;
        }
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onGrow(BlockGrowEvent event) {
        Block b = event.getBlock();
        Location location = BukkitUtil.getLocation(b.getLocation());
        if (location.isUnownedPlotArea()) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onChange(BlockFromToEvent event) {
        Block from = event.getBlock();
        Block to = event.getToBlock();
        Location tLocation = BukkitUtil.getLocation(to.getLocation());
        PlotArea area = tLocation.getPlotArea();
        if (area == null) {
            return;
        }
        Plot plot = area.getOwnedPlot(tLocation);
        Location fLocation = BukkitUtil.getLocation(from.getLocation());
        if (plot != null) {
            if (Flags.DISABLE_PHYSICS.isFalse(plot)) {
                event.setCancelled(true);
                return;
            } else if (!area.contains(fLocation.getX(), fLocation.getZ()) || !Objects
                .equals(plot, area.getOwnedPlot(fLocation))) {
                event.setCancelled(true);
                return;
            }
            if (Flags.LIQUID_FLOW.isFalse(plot)) {
                switch (to.getType()) {
                    case WATER:
                    case STATIONARY_WATER:
                    case LAVA:
                    case STATIONARY_LAVA:
                        event.setCancelled(true);
                }
            }
        } else if (!area.contains(fLocation.getX(), fLocation.getZ()) || !Objects
            .equals(plot, area.getOwnedPlot(fLocation))) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockPistonExtend(BlockPistonExtendEvent event) {
        Block block = event.getBlock();
        Location location = BukkitUtil.getLocation(block.getLocation());
        BlockFace face = event.getDirection();
        Vector relative = new Vector(face.getModX(), face.getModY(), face.getModZ());
        PlotArea area = location.getPlotArea();
        if (area == null) {
            if (!PS.get().hasPlotArea(location.getWorld())) {
                return;
            }
            for (Block b : event.getBlocks()) {
                if (BukkitUtil.getLocation(b.getLocation().add(relative)).getPlotArea() != null) {
                    event.setCancelled(true);
                    return;
                }
            }
            return;
        }
        Plot plot = area.getOwnedPlot(location);
        if (plot == null) {
            event.setCancelled(true);
            return;
        }
        List<Block> blocks = event.getBlocks();
        for (Block b : blocks) {
            Location bloc = BukkitUtil.getLocation(b.getLocation());
            if (!area.contains(bloc.getX(), bloc.getZ()) || !area
                .contains(bloc.getX() + relative.getBlockX(), bloc.getZ() + relative.getBlockZ())) {
                event.setCancelled(true);
                return;
            }
            if (!plot.equals(area.getOwnedPlot(bloc)) || !plot.equals(area.getOwnedPlot(
                bloc.add(relative.getBlockX(), relative.getBlockY(), relative.getBlockZ())))) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockPistonRetract(BlockPistonRetractEvent event) {
        Block block = event.getBlock();
        Location location = BukkitUtil.getLocation(block.getLocation());
        PlotArea area = location.getPlotArea();
        if (area == null) {
            if (!PS.get().hasPlotArea(location.getWorld())) {
                return;
            }
            if (this.pistonBlocks) {
                try {
                    for (Block pulled : event.getBlocks()) {
                        location = BukkitUtil.getLocation(pulled.getLocation());
                        if (location.getPlotArea() != null) {
                            event.setCancelled(true);
                            return;
                        }
                    }
                } catch (Throwable ignored) {
                    this.pistonBlocks = false;
                }
            }
            if (!this.pistonBlocks && block.getType() != Material.PISTON_BASE) {
                BlockFace dir = event.getDirection();
                location = BukkitUtil.getLocation(block.getLocation()
                    .add(dir.getModX() * 2, dir.getModY() * 2, dir.getModZ() * 2));
                if (location.getPlotArea() != null) {
                    event.setCancelled(true);
                    return;
                }
            }
            return;
        }
        Plot plot = area.getOwnedPlot(location);
        BlockFace dir = event.getDirection();
        //        Location head = location.add(-dir.getModX(), -dir.getModY(), -dir.getModZ());
        //
        //        if (!Objects.equals(plot, area.getOwnedPlot(head))) {
        //            // FIXME: cancelling the event doesn't work here. See issue #1484
        //            event.setCancelled(true);
        //            return;
        //        }
        if (this.pistonBlocks) {
            try {
                for (Block pulled : event.getBlocks()) {
                    Location from = BukkitUtil.getLocation(
                        pulled.getLocation().add(dir.getModX(), dir.getModY(), dir.getModZ()));
                    Location to = BukkitUtil.getLocation(pulled.getLocation());
                    if (!area.contains(to.getX(), to.getZ())) {
                        event.setCancelled(true);
                        return;
                    }
                    Plot fromPlot = area.getOwnedPlot(from);
                    Plot toPlot = area.getOwnedPlot(to);
                    if (!Objects.equals(fromPlot, toPlot)) {
                        event.setCancelled(true);
                        return;
                    }
                }
            } catch (Throwable ignored) {
                this.pistonBlocks = false;
            }
        }
        if (!this.pistonBlocks && block.getType() != Material.PISTON_BASE) {
            location = BukkitUtil.getLocation(
                block.getLocation().add(dir.getModX() * 2, dir.getModY() * 2, dir.getModZ() * 2));
            if (!area.contains(location)) {
                event.setCancelled(true);
                return;
            }
            Plot newPlot = area.getOwnedPlot(location);
            if (!Objects.equals(plot, newPlot)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onStructureGrow(StructureGrowEvent event) {
        if (!PS.get().hasPlotArea(event.getWorld().getName())) {
            return;
        }
        List<BlockState> blocks = event.getBlocks();
        if (blocks.isEmpty()) {
            return;
        }
        Location location = BukkitUtil.getLocation(blocks.get(0).getLocation());
        PlotArea area = location.getPlotArea();
        if (area == null) {
            for (int i = blocks.size() - 1; i >= 0; i--) {
                location = BukkitUtil.getLocation(blocks.get(i).getLocation());
                if (location.getPlotArea() != null) {
                    blocks.remove(i);
                }
            }
            return;
        } else {
            Plot origin = area.getOwnedPlot(location);
            if (origin == null) {
                event.setCancelled(true);
                return;
            }
            for (int i = blocks.size() - 1; i >= 0; i--) {
                location = BukkitUtil.getLocation(blocks.get(i).getLocation());
                if (!area.contains(location.getX(), location.getZ())) {
                    blocks.remove(i);
                    continue;
                }
                Plot plot = area.getOwnedPlot(location);
                if (!Objects.equals(plot, origin)) {
                    event.getBlocks().remove(i);
                }
            }
        }
        Plot origin = area.getPlot(location);
        if (origin == null) {
            event.setCancelled(true);
            return;
        }
        for (int i = blocks.size() - 1; i >= 0; i--) {
            location = BukkitUtil.getLocation(blocks.get(i).getLocation());
            Plot plot = area.getOwnedPlot(location);
            /*
             * plot -> the base plot of the merged area
             * origin -> the plot where the event gets called
             */

            // Are plot and origin not the same AND are both plots merged
            if (!Objects.equals(plot, origin) && (!plot.isMerged() && !origin.isMerged())) {
                event.getBlocks().remove(i);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockDispense(BlockDispenseEvent event) {
        Material type = event.getItem().getType();
        switch (type) {
            case WATER_BUCKET:
            case LAVA_BUCKET: {
                if (event.getBlock().getType() == Material.DROPPER) {
                    return;
                }
                BlockFace targetFace =
                    ((org.bukkit.material.Dispenser) event.getBlock().getState().getData())
                        .getFacing();
                Location location =
                    BukkitUtil.getLocation(event.getBlock().getRelative(targetFace).getLocation());
                if (location.isPlotRoad()) {
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onCancelledInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        PlotPlayer pp = BukkitUtil.getPlayer(player);
        PlotArea area = pp.getPlotAreaAbs();
        if (area == null) {
            return;
        }
        if (event.getAction() == Action.RIGHT_CLICK_AIR) {
            Material item = event.getItem().getType();
            if (item == Material.MONSTER_EGG || item == Material.MONSTER_EGGS) {
                event.setCancelled(true);
                event.setUseItemInHand(Event.Result.DENY);
            }
        }
        if (event.isCancelled() && event.getAction() == Action.RIGHT_CLICK_AIR) {
            @SuppressWarnings("deprecation") ItemStack hand = player.getItemInHand();
            Material type;
            if (hand == null) {
                type = Material.AIR;
            } else {
                type = hand.getType();
            }
            if (type == Material.MONSTER_EGG || type == Material.MONSTER_EGGS) {
                Set<Material> transparent =
                    new HashSet<>(Arrays.asList(Material.WATER, Material.STATIONARY_WATER));
                Block block = player.getTargetBlock(transparent, 5);
                if (block != null && block.getType() != Material.AIR) {
                    Location location = BukkitUtil.getLocation(block.getLocation());
                    //noinspection deprecation
                    if (!EventUtil.manager
                        .checkPlayerBlockEvent(pp, PlayerBlockEventType.SPAWN_MOB, location,
                            new BukkitLazyBlock(PlotBlock.get((short) type.getId(), (byte) 0)),
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
        PlotPlayer pp = BukkitUtil.getPlayer(player);
        PlotArea area = pp.getPlotAreaAbs();
        if (area == null) {
            return;
        }
        PlayerBlockEventType eventType = null;
        BukkitLazyBlock lb;
        Location location;
        Action action = event.getAction();
        switch (action) {
            case PHYSICAL: {
                eventType = PlayerBlockEventType.TRIGGER_PHYSICAL;
                Block block = event.getClickedBlock();
                lb = new BukkitLazyBlock(block);
                location = BukkitUtil.getLocation(block.getLocation());
                break;
            }
            case RIGHT_CLICK_BLOCK: {
                Block block = event.getClickedBlock();
                location = BukkitUtil.getLocation(block.getLocation());
                Material blockType = block.getType();
                @SuppressWarnings("deprecation") int blockId = blockType.getId();
                switch (blockType) {
                    case ANVIL:
                    case ACACIA_DOOR:
                    case BIRCH_DOOR:
                    case DARK_OAK_DOOR:
                    case IRON_DOOR:
                    case JUNGLE_DOOR:
                    case SPRUCE_DOOR:
                    case TRAP_DOOR:
                    case IRON_TRAPDOOR:
                    case WOOD_DOOR:
                    case WOODEN_DOOR:
                    case TRAPPED_CHEST:
                    case ENDER_CHEST:
                    case CHEST:
                    case ACACIA_FENCE_GATE:
                    case BIRCH_FENCE_GATE:
                    case DARK_OAK_FENCE_GATE:
                    case FENCE_GATE:
                    case JUNGLE_FENCE_GATE:
                    case SPRUCE_FENCE_GATE:
                    case LEVER:
                    case DIODE:
                    case DIODE_BLOCK_OFF:
                    case DIODE_BLOCK_ON:
                    case COMMAND:
                    case REDSTONE_COMPARATOR:
                    case REDSTONE_COMPARATOR_OFF:
                    case REDSTONE_COMPARATOR_ON:
                    case REDSTONE_ORE:
                    case WOOD_BUTTON:
                    case STONE_BUTTON:
                    case BEACON:
                    case BED_BLOCK:
                    case SIGN:
                    case WALL_SIGN:
                    case SIGN_POST:
                    case ENCHANTMENT_TABLE:
                    case BREWING_STAND:
                    case STANDING_BANNER:
                    case BURNING_FURNACE:
                    case FURNACE:
                    case CAKE_BLOCK:
                    case DISPENSER:
                    case DROPPER:
                    case HOPPER:
                    case NOTE_BLOCK:
                    case JUKEBOX:
                    case WORKBENCH:
                    case SILVER_SHULKER_BOX:
                    case BLACK_SHULKER_BOX:
                    case BLUE_SHULKER_BOX:
                    case RED_SHULKER_BOX:
                    case PINK_SHULKER_BOX:
                    case ORANGE_SHULKER_BOX:
                    case WHITE_SHULKER_BOX:
                    case YELLOW_SHULKER_BOX:
                    case BROWN_SHULKER_BOX:
                    case CYAN_SHULKER_BOX:
                    case GREEN_SHULKER_BOX:
                    case PURPLE_SHULKER_BOX:
                    case GRAY_SHULKER_BOX:
                    case LIME_SHULKER_BOX:
                    case LIGHT_BLUE_SHULKER_BOX:
                    case MAGENTA_SHULKER_BOX:
                    case COMMAND_REPEATING:
                    case COMMAND_CHAIN:
                        eventType = PlayerBlockEventType.INTERACT_BLOCK;
                        break;
                    case DRAGON_EGG:
                        eventType = PlayerBlockEventType.TELEPORT_OBJECT;
                        break;
                    default:
                        if (blockId > 197) {
                            eventType = PlayerBlockEventType.INTERACT_BLOCK;
                        }
                        break;
                }
                lb = new BukkitLazyBlock(blockId, block);
                @SuppressWarnings("deprecation") ItemStack hand = player.getItemInHand();
                if (eventType != null && (eventType != PlayerBlockEventType.INTERACT_BLOCK
                    || !player.isSneaking())) {
                    break;
                }
                Material type;
                if (hand == null) {
                    type = null;
                } else {
                    type = hand.getType();
                }
                int id;
                if (type == null) {
                    id = 0;
                } else {
                    id = type.getId();
                }
                if (type == Material.AIR) {
                    eventType = PlayerBlockEventType.INTERACT_BLOCK;
                    break;
                }
                if (id < 198) {
                    location = BukkitUtil
                        .getLocation(block.getRelative(event.getBlockFace()).getLocation());
                    eventType = PlayerBlockEventType.PLACE_BLOCK;
                    lb = new BukkitLazyBlock(id, block);
                    break;
                }
                Material handType = hand.getType();
                lb = new BukkitLazyBlock(PlotBlock.get((short) handType.getId(), (byte) 0));
                switch (handType) {
                    case FIREWORK:
                    case MONSTER_EGG:
                    case MONSTER_EGGS:
                        eventType = PlayerBlockEventType.SPAWN_MOB;
                        break;
                    case ARMOR_STAND:
                        location = BukkitUtil
                            .getLocation(block.getRelative(event.getBlockFace()).getLocation());
                        eventType = PlayerBlockEventType.PLACE_MISC;
                        break;
                    case WRITTEN_BOOK:
                    case BOOK_AND_QUILL:
                    case BOOK:
                        eventType = PlayerBlockEventType.READ;
                        break;
                    case APPLE:
                    case BAKED_POTATO:
                    case MUSHROOM_SOUP:
                    case BREAD:
                    case CARROT:
                    case CARROT_ITEM:
                    case COOKIE:
                    case GRILLED_PORK:
                    case POISONOUS_POTATO:
                    case MUTTON:
                    case PORK:
                    case POTATO:
                    case POTATO_ITEM:
                    case POTION:
                    case PUMPKIN_PIE:
                    case RABBIT:
                    case RABBIT_FOOT:
                    case RABBIT_STEW:
                    case RAW_BEEF:
                    case RAW_FISH:
                    case RAW_CHICKEN:
                        eventType = PlayerBlockEventType.EAT;
                        break;
                    case MINECART:
                    case STORAGE_MINECART:
                    case POWERED_MINECART:
                    case HOPPER_MINECART:
                    case EXPLOSIVE_MINECART:
                    case COMMAND_MINECART:
                    case BOAT:
                        eventType = PlayerBlockEventType.PLACE_VEHICLE;
                        break;
                    default:
                        eventType = PlayerBlockEventType.INTERACT_BLOCK;
                        break;
                }
                break;
            }
            case LEFT_CLICK_BLOCK:
                Block block = event.getClickedBlock();
                location = BukkitUtil.getLocation(block.getLocation());
                eventType = PlayerBlockEventType.BREAK_BLOCK;
                lb = new BukkitLazyBlock(block);
                break;
            case LEFT_CLICK_AIR:
                if (event.getItem() != null) {
                    if (event.getItem().getType() == Material.MONSTER_EGG
                        || event.getItem().getType() == Material.MONSTER_EGGS) {
                        event.setCancelled(true);
                        event.setUseItemInHand(Event.Result.DENY);
                    }
                }
                return;
            default:
                return;
        }
        if (PS.get().worldedit != null && pp.getAttribute("worldedit")) {
            //noinspection deprecation
            if (player.getItemInHand().getTypeId() == PS.get().worldedit
                .getConfiguration().wandItem) {
                return;
            }
        }
        if (!EventUtil.manager.checkPlayerBlockEvent(pp, eventType, location, lb, true)) {
            event.setCancelled(true);
            event.setUseItemInHand(Event.Result.DENY);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void creatureSpawnEvent(CreatureSpawnEvent event) {
        Entity entity = event.getEntity();
        Location location = BukkitUtil.getLocation(entity.getLocation());
        PlotArea area = location.getPlotArea();
        if (area == null) {
            return;
        }
        CreatureSpawnEvent.SpawnReason reason = event.getSpawnReason();
        switch (reason) {
            case SPAWNER_EGG:
            case DISPENSE_EGG:
            case OCELOT_BABY:
                if (!area.SPAWN_EGGS) {
                    event.setCancelled(true);
                    return;
                }
                break;
            case BREEDING:
                if (!area.SPAWN_BREEDING) {
                    event.setCancelled(true);
                    return;
                }
                break;
            case BUILD_IRONGOLEM:
            case BUILD_WITHER:
            case BUILD_SNOWMAN:
            case CUSTOM:
                //noinspection deprecation
                if (!area.SPAWN_CUSTOM && entity.getType().getTypeId() != 30) {
                    event.setCancelled(true);
                    return;
                }
                break;
            case SPAWNER:
                if (!area.MOB_SPAWNER_SPAWNING) {
                    event.setCancelled(true);
                    return;
                }
                break;
        }
        Plot plot = area.getOwnedPlotAbs(location);
        if (plot == null) {
            if (!area.MOB_SPAWNING) {
                event.setCancelled(true);
            }
            return;
        }
        if (checkEntity(entity, plot)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onEntityFall(EntityChangeBlockEvent event) {
        if (event.getEntityType() != EntityType.FALLING_BLOCK) {
            return;
        }
        Block block = event.getBlock();
        World world = block.getWorld();
        String worldName = world.getName();
        if (!PS.get().hasPlotArea(worldName)) {
            return;
        }
        Location location = BukkitUtil.getLocation(block.getLocation());
        PlotArea area = location.getPlotArea();
        if (area == null) {
            return;
        }
        Plot plot = area.getOwnedPlotAbs(location);
        if (plot == null || plot.getFlag(Flags.DISABLE_PHYSICS, false)) {
            event.setCancelled(true);
            return;
        }
        if (event.getTo().hasGravity()) {
            Entity entity = event.getEntity();
            List<MetadataValue> meta = entity.getMetadata("plot");
            if (meta.isEmpty()) {
                return;
            }
            Plot origin = (Plot) meta.get(0).value();
            if (origin != null && !origin.equals(plot)) {
                event.setCancelled(true);
                entity.remove();
            }
        } else if (event.getTo() == Material.AIR) {
            event.getEntity()
                .setMetadata("plot", new FixedMetadataValue((Plugin) PS.get().IMP, plot));
        }
    }

    public static boolean checkEntity(Plot plot, IntegerFlag... flags) {
        if (Settings.Done.RESTRICT_BUILDING && Flags.DONE.isSet(plot)) {
            return true;
        }
        int[] mobs = null;
        for (IntegerFlag flag : flags) {
            int i;
            switch (flag.getName()) {
                case "entity-cap":
                    i = 0;
                    break;
                case "mob-cap":
                    i = 3;
                    break;
                case "hostile-cap":
                    i = 2;
                    break;
                case "animal-cap":
                    i = 1;
                    break;
                case "vehicle-cap":
                    i = 4;
                    break;
                case "misc-cap":
                    i = 5;
                    break;
                default:
                    i = 0;
            }
            int cap = plot.getFlag(flag, Integer.MAX_VALUE);
            if (cap == Integer.MAX_VALUE) {
                continue;
            }
            if (cap == 0) {
                return true;
            }
            if (mobs == null) {
                mobs = plot.countEntities();
            }
            if (mobs[i] >= cap) {
                plot.setMeta("EntityCount", mobs);
                plot.setMeta("EntityCountTime", System.currentTimeMillis());
                return true;
            }
        }
        if (mobs != null) {
            for (IntegerFlag flag : flags) {
                int i;
                switch (flag.getName()) {
                    case "entity-cap":
                        i = 0;
                        break;
                    case "mob-cap":
                        i = 3;
                        break;
                    case "hostile-cap":
                        i = 2;
                        break;
                    case "animal-cap":
                        i = 1;
                        break;
                    case "vehicle-cap":
                        i = 4;
                        break;
                    case "misc-cap":
                        i = 5;
                        break;
                    default:
                        i = 0;
                }
                mobs[i]++;
            }
            plot.setMeta("EntityCount", mobs);
            plot.setMeta("EntityCountTime", System.currentTimeMillis());
        }
        return false;
    }

    @EventHandler public void onPrime(ExplosionPrimeEvent event) {
        this.lastRadius = event.getRadius() + 1;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBurn(BlockBurnEvent event) {
        Block b = event.getBlock();
        Location location = BukkitUtil.getLocation(b.getLocation());

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
    public void onBlockIgnite(BlockIgniteEvent event) {
        Player player = event.getPlayer();
        Entity ignitingEntity = event.getIgnitingEntity();
        Block block = event.getBlock();
        BlockIgniteEvent.IgniteCause igniteCause = event.getCause();
        Location loc;
        if (block != null) {
            loc = BukkitUtil.getLocation(block.getLocation());
        } else if (ignitingEntity != null) {
            loc = BukkitUtil.getLocation(ignitingEntity);
        } else if (player != null) {
            loc = BukkitUtil.getLocation(player);
        } else {
            return;
        }
        PlotArea area = loc.getPlotArea();
        if (area == null) {
            return;
        }
        if (igniteCause == BlockIgniteEvent.IgniteCause.LIGHTNING) {
            event.setCancelled(true);
            return;
        }

        Plot plot = area.getOwnedPlotAbs(loc);
        if (player != null) {
            PlotPlayer pp = BukkitUtil.getPlayer(player);
            if (plot == null) {
                if (!Permissions.hasPermission(pp, C.PERMISSION_ADMIN_BUILD_ROAD)) {
                    MainUtil.sendMessage(pp, C.NO_PERMISSION_EVENT, C.PERMISSION_ADMIN_BUILD_ROAD);
                    event.setCancelled(true);
                }
            } else if (!plot.hasOwner()) {
                if (!Permissions.hasPermission(pp, C.PERMISSION_ADMIN_BUILD_UNOWNED)) {
                    MainUtil
                        .sendMessage(pp, C.NO_PERMISSION_EVENT, C.PERMISSION_ADMIN_BUILD_UNOWNED);
                    event.setCancelled(true);
                }
            } else if (!plot.isAdded(pp.getUUID())) {
                if (!Permissions.hasPermission(pp, C.PERMISSION_ADMIN_BUILD_OTHER)) {
                    MainUtil.sendMessage(pp, C.NO_PERMISSION_EVENT, C.PERMISSION_ADMIN_BUILD_OTHER);
                    event.setCancelled(true);
                }
            } else if (Flags.BLOCK_IGNITION.isFalse(plot)) {
                event.setCancelled(true);
            }
        } else {
            if (plot == null) {
                event.setCancelled(true);
                return;
            }
            if (ignitingEntity != null) {
                if (!plot.getFlag(Flags.BLOCK_IGNITION, false)) {
                    event.setCancelled(true);
                    return;
                }
                if (igniteCause == BlockIgniteEvent.IgniteCause.FIREBALL) {
                    if (ignitingEntity instanceof Fireball) {
                        Projectile fireball = (Projectile) ignitingEntity;
                        Location location = null;
                        if (fireball.getShooter() instanceof Entity) {
                            Entity shooter = (Entity) fireball.getShooter();
                            location = BukkitUtil.getLocation(shooter.getLocation());
                        } else if (fireball.getShooter() instanceof BlockProjectileSource) {
                            Block shooter =
                                ((BlockProjectileSource) fireball.getShooter()).getBlock();
                            location = BukkitUtil.getLocation(shooter.getLocation());
                        }
                        if (location != null && !plot.equals(location.getPlot())) {
                            event.setCancelled(true);
                        }
                    }
                }

            } else if (event.getIgnitingBlock() != null) {
                Block ignitingBlock = event.getIgnitingBlock();
                Plot plotIgnited = BukkitUtil.getLocation(ignitingBlock.getLocation()).getPlot();
                if (igniteCause == BlockIgniteEvent.IgniteCause.FLINT_AND_STEEL && (
                    !plot.getFlag(Flags.BLOCK_IGNITION, false) || plotIgnited == null
                        || !plotIgnited.equals(plot)) ||
                    (igniteCause == BlockIgniteEvent.IgniteCause.SPREAD
                        || igniteCause == BlockIgniteEvent.IgniteCause.LAVA) && (
                        !plot.getFlag(Flags.BLOCK_IGNITION).or(false) || plotIgnited == null
                            || !plotIgnited.equals(plot))) {
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBucketEmpty(PlayerBucketEmptyEvent event) {
        BlockFace bf = event.getBlockFace();
        Block b =
            event.getBlockClicked().getLocation().add(bf.getModX(), bf.getModY(), bf.getModZ())
                .getBlock();
        Location location = BukkitUtil.getLocation(b.getLocation());
        PlotArea area = location.getPlotArea();
        if (area == null) {
            return;
        }
        PlotPlayer pp = BukkitUtil.getPlayer(event.getPlayer());
        Plot plot = area.getPlot(location);
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
            //noinspection deprecation
            if (Flags.USE.contains(plot, PlotBlock.get(event.getBucket().getId(), 0))) {
                return;
            }
            if (Permissions.hasPermission(pp, C.PERMISSION_ADMIN_BUILD_OTHER)) {
                return;
            }
            MainUtil.sendMessage(pp, C.NO_PERMISSION_EVENT, C.PERMISSION_ADMIN_BUILD_OTHER);
            event.setCancelled(true);
        } else if (Settings.Done.RESTRICT_BUILDING && plot.getFlags().containsKey(Flags.DONE)) {
            if (!Permissions.hasPermission(pp, C.PERMISSION_ADMIN_BUILD_OTHER)) {
                MainUtil.sendMessage(pp, C.NO_PERMISSION_EVENT, C.PERMISSION_ADMIN_BUILD_OTHER);
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClick(InventoryClickEvent event) {
        HumanEntity clicker = event.getWhoClicked();
        if (!(clicker instanceof Player)) {
            return;
        }
        Player player = (Player) clicker;
        PlotPlayer pp = BukkitUtil.getPlayer(player);
        PlotInventory inventory = pp.getMeta("inventory");
        if (inventory != null && event.getRawSlot() == event.getSlot()) {
            if (!inventory.onClick(event.getSlot())) {
                event.setCancelled(true);
                inventory.close();
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
        BukkitUtil.getPlayer(player).deleteMeta("inventory");
    }

    @EventHandler(priority = EventPriority.MONITOR) public void onLeave(PlayerQuitEvent event) {
        TaskManager.TELEPORT_QUEUE.remove(event.getPlayer().getName());
        PlotPlayer pp = BukkitUtil.getPlayer(event.getPlayer());
        pp.unregister();
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBucketFill(PlayerBucketFillEvent event) {
        Block b = event.getBlockClicked();
        Location location = BukkitUtil.getLocation(b.getLocation());
        PlotArea area = location.getPlotArea();
        if (area == null) {
            return;
        }
        Player player = event.getPlayer();
        PlotPlayer plotPlayer = BukkitUtil.getPlayer(player);
        Plot plot = area.getPlot(location);
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
            //noinspection deprecation
            if (use.isPresent() && use.get()
                .contains(PlotBlock.get(block.getTypeId(), block.getData()))) {
                return;
            }
            if (Permissions.hasPermission(plotPlayer, C.PERMISSION_ADMIN_BUILD_OTHER)) {
                return;
            }
            MainUtil.sendMessage(plotPlayer, C.NO_PERMISSION_EVENT, C.PERMISSION_ADMIN_BUILD_OTHER);
            event.setCancelled(true);
        } else if (Settings.Done.RESTRICT_BUILDING && plot.getFlags().containsKey(Flags.DONE)) {
            if (!Permissions.hasPermission(plotPlayer, C.PERMISSION_ADMIN_BUILD_OTHER)) {
                MainUtil
                    .sendMessage(plotPlayer, C.NO_PERMISSION_EVENT, C.PERMISSION_ADMIN_BUILD_OTHER);
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onVehicleCreate(VehicleCreateEvent event) {
        Vehicle entity = event.getVehicle();
        Location location = BukkitUtil.getLocation(entity);
        PlotArea area = location.getPlotArea();
        if (area == null) {
            return;
        }
        location.getPlotAbs();
        Plot plot = area.getOwnedPlotAbs(location);
        if (plot == null || checkEntity(entity, plot)) {
            entity.remove();
            return;
        }
        if (Settings.Enabled_Components.KILL_ROAD_VEHICLES) {
            entity.setMetadata("plot", new FixedMetadataValue((Plugin) PS.get().IMP, plot));
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onHangingPlace(HangingPlaceEvent event) {
        Block b = event.getBlock().getRelative(event.getBlockFace());
        Location location = BukkitUtil.getLocation(b.getLocation());
        PlotArea area = location.getPlotArea();
        if (area == null) {
            return;
        }
        Player p = event.getPlayer();
        PlotPlayer pp = BukkitUtil.getPlayer(p);
        Plot plot = area.getPlot(location);
        if (plot == null) {
            if (!Permissions.hasPermission(pp, C.PERMISSION_ADMIN_BUILD_ROAD)) {
                MainUtil.sendMessage(pp, C.NO_PERMISSION_EVENT, C.PERMISSION_ADMIN_BUILD_ROAD);
                event.setCancelled(true);
            }
        } else {
            if (!plot.hasOwner()) {
                if (!Permissions.hasPermission(pp, C.PERMISSION_ADMIN_BUILD_UNOWNED)) {
                    MainUtil
                        .sendMessage(pp, C.NO_PERMISSION_EVENT, C.PERMISSION_ADMIN_BUILD_UNOWNED);
                    event.setCancelled(true);
                }
                return;
            }
            if (!plot.isAdded(pp.getUUID())) {
                if (!plot.getFlag(Flags.HANGING_PLACE, false)) {
                    if (!Permissions.hasPermission(pp, C.PERMISSION_ADMIN_BUILD_OTHER)) {
                        MainUtil
                            .sendMessage(pp, C.NO_PERMISSION_EVENT, C.PERMISSION_ADMIN_BUILD_OTHER);
                        event.setCancelled(true);
                        return;
                    }
                    return;
                }
            }
            if (checkEntity(event.getEntity(), plot)) {
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
            PlotPlayer pp = BukkitUtil.getPlayer(p);
            Plot plot = area.getPlot(location);
            if (plot == null) {
                if (!Permissions.hasPermission(pp, C.PERMISSION_ADMIN_DESTROY_ROAD)) {
                    MainUtil
                        .sendMessage(pp, C.NO_PERMISSION_EVENT, C.PERMISSION_ADMIN_DESTROY_ROAD);
                    event.setCancelled(true);
                }
            } else {
                if (Settings.Done.RESTRICT_BUILDING && plot.hasFlag(Flags.DONE)) {
                    if (!Permissions.hasPermission(pp, C.PERMISSION_ADMIN_BUILD_OTHER)) {
                        MainUtil.sendMessage(pp, C.NO_PERMISSION_EVENT, C.PERMISSION_ADMIN_BUILD_OTHER);
                        event.setCancelled(true);
                        return;
                    }
                }

                if (!plot.hasOwner()) {
                    if (!Permissions.hasPermission(pp, C.PERMISSION_ADMIN_DESTROY_UNOWNED)) {
                        MainUtil.sendMessage(pp, C.NO_PERMISSION_EVENT, C.PERMISSION_ADMIN_DESTROY_UNOWNED);
                        event.setCancelled(true);
                    }
                } else if (!plot.isAdded(pp.getUUID())) {
                    if (plot.getFlag(Flags.HANGING_BREAK, false)) {
                        return;
                    }
                    if (!Permissions.hasPermission(pp, C.PERMISSION_ADMIN_DESTROY_OTHER)) {
                        MainUtil.sendMessage(pp, C.NO_PERMISSION_EVENT, C.PERMISSION_ADMIN_DESTROY_OTHER);
                        event.setCancelled(true);
                    }
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
                PlotPlayer player = BukkitUtil.getPlayer(shooter);
                Plot plot = area.getPlot(BukkitUtil.getLocation(event.getEntity()));
                if (plot != null) {
                    if (Settings.Done.RESTRICT_BUILDING && plot.hasFlag(Flags.DONE)) {
                        if (!Permissions.hasPermission(player, C.PERMISSION_ADMIN_BUILD_OTHER)) {
                            MainUtil.sendMessage(player, C.NO_PERMISSION_EVENT, C.PERMISSION_ADMIN_BUILD_OTHER);
                            event.setCancelled(true);
                            return;
                        }
                    }

                    if (!plot.hasOwner()) {
                        if (!Permissions
                            .hasPermission(player, C.PERMISSION_ADMIN_DESTROY_UNOWNED)) {
                            MainUtil.sendMessage(player, C.NO_PERMISSION_EVENT,
                                C.PERMISSION_ADMIN_DESTROY_UNOWNED);
                            event.setCancelled(true);
                        }
                    } else if (!plot.isAdded(player.getUUID())) {
                        if (!plot.getFlag(Flags.HANGING_BREAK, false)) {
                            if (!Permissions
                                .hasPermission(player, C.PERMISSION_ADMIN_DESTROY_OTHER)) {
                                MainUtil.sendMessage(player, C.NO_PERMISSION_EVENT,
                                    C.PERMISSION_ADMIN_DESTROY_OTHER);
                                event.setCancelled(true);
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
        PlotPlayer pp = BukkitUtil.getPlayer(p);
        Plot plot = area.getPlot(location);
        if (plot == null) {
            if (!Permissions.hasPermission(pp, C.PERMISSION_ADMIN_INTERACT_ROAD)) {
                MainUtil.sendMessage(pp, C.NO_PERMISSION_EVENT, C.PERMISSION_ADMIN_INTERACT_ROAD);
                event.setCancelled(true);
            }
        } else {
            if (Settings.Done.RESTRICT_BUILDING && plot.hasFlag(Flags.DONE)) {
                if (!Permissions.hasPermission(pp, C.PERMISSION_ADMIN_BUILD_OTHER)) {
                    MainUtil.sendMessage(pp, C.NO_PERMISSION_EVENT, C.PERMISSION_ADMIN_BUILD_OTHER);
                    event.setCancelled(true);
                    return;
                }
            }
            if (!plot.hasOwner()) {
                if (!Permissions.hasPermission(pp, C.PERMISSION_ADMIN_INTERACT_UNOWNED)) {
                    MainUtil.sendMessage(pp, C.NO_PERMISSION_EVENT, C.PERMISSION_ADMIN_INTERACT_UNOWNED);
                    event.setCancelled(true);
                }
            } else if (!plot.isAdded(pp.getUUID())) {
                Entity entity = event.getRightClicked();
                if (entity instanceof Creature && plot.getFlag(Flags.CREATURE_INTERACT, false)) {
                    return;
                }
                if (entity instanceof Monster && plot.getFlag(Flags.HOSTILE_INTERACT, false)) {
                    return;
                }
                if (entity instanceof Animals && plot.getFlag(Flags.ANIMAL_INTERACT, false)) {
                    return;
                }
                if (entity instanceof Tameable && ((Tameable) entity).isTamed() && plot
                    .getFlag(Flags.TAMED_INTERACT, false)) {
                    return;
                }
                if (entity instanceof Vehicle && plot.getFlag(Flags.VEHICLE_USE, false)) {
                    return;
                }
                if (entity instanceof Player && plot.getFlag(Flags.PLAYER_INTERACT, false)) {
                    return;
                }
                if (entity instanceof Villager && plot.getFlag(Flags.VILLAGER_INTERACT, false)) {
                    return;
                }
                if (entity instanceof ItemFrame && plot.getFlag(Flags.MISC_INTERACT, false)) {
                    return;
                }
                if (!Permissions.hasPermission(pp, C.PERMISSION_ADMIN_INTERACT_OTHER)) {
                    MainUtil.sendMessage(pp, C.NO_PERMISSION_EVENT, C.PERMISSION_ADMIN_INTERACT_OTHER);
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onVehicleDestroy(VehicleDestroyEvent event) {
        Location l = BukkitUtil.getLocation(event.getVehicle());
        PlotArea area = l.getPlotArea();
        if (area == null) {
            return;
        }
        Entity d = event.getAttacker();
        if (d instanceof Player) {
            Player p = (Player) d;
            PlotPlayer pp = BukkitUtil.getPlayer(p);
            Plot plot = area.getPlot(l);
            if (plot == null) {
                if (!Permissions.hasPermission(pp, "plots.admin.vehicle.break.road")) {
                    MainUtil
                        .sendMessage(pp, C.NO_PERMISSION_EVENT, "plots.admin.vehicle.break.road");
                    event.setCancelled(true);
                }
            } else {
                if (!plot.hasOwner()) {
                    if (!Permissions.hasPermission(pp, "plots.admin.vehicle.break.unowned")) {
                        MainUtil.sendMessage(pp, C.NO_PERMISSION_EVENT,
                            "plots.admin.vehicle.break.unowned");
                        event.setCancelled(true);
                        return;
                    }
                    return;
                }
                if (!plot.isAdded(pp.getUUID())) {
                    if (plot.getFlag(Flags.VEHICLE_BREAK, false)) {
                        return;
                    }
                    if (!Permissions.hasPermission(pp, "plots.admin.vehicle.break.other")) {
                        MainUtil.sendMessage(pp, C.NO_PERMISSION_EVENT,
                            "plots.admin.vehicle.break.other");
                        event.setCancelled(true);
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPotionSplash(PotionSplashEvent event) {
        ThrownPotion damager = event.getPotion();
        Location l = BukkitUtil.getLocation(damager);
        if (!PS.get().hasPlotArea(l.getWorld())) {
            return;
        }
        int count = 0;
        for (LivingEntity victim : event.getAffectedEntities()) {
            if (!entityDamage(damager, victim)) {
                event.setIntensity(victim, 0);
                count++;
            }
        }
        if ((count > 0 && count == event.getAffectedEntities().size()) || !onProjectileHit(event)) {
            event.setCancelled(true);
        }
    }

    @SuppressWarnings("deprecation") @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityCombustByEntity(EntityCombustByEntityEvent event) {
        EntityDamageByEntityEvent eventChange = null;
        if (PS.get().checkVersion(PS.get().IMP.getServerVersion(), BukkitVersion.v1_11_0)) {
            eventChange = new EntityDamageByEntityEvent(event.getCombuster(), event.getEntity(),
                EntityDamageEvent.DamageCause.FIRE_TICK, (double) event.getDuration());
        } else {
            try {
                Constructor<EntityDamageByEntityEvent> constructor = EntityDamageByEntityEvent.class
                    .getConstructor(Entity.class, Entity.class, EntityDamageEvent.DamageCause.class,
                        Integer.TYPE);
                eventChange = constructor.newInstance(event.getCombuster(), event.getEntity(),
                    EntityDamageEvent.DamageCause.FIRE_TICK, event.getDuration());
            } catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        if (eventChange == null) {
            return;
        }
        onEntityDamageByEntityEvent(eventChange);
        if (eventChange.isCancelled()) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent event) {
        Entity damager = event.getDamager();
        Location l = BukkitUtil.getLocation(damager);
        if (!PS.get().hasPlotArea(l.getWorld())) {
            return;
        }
        Entity victim = event.getEntity();
        if (!entityDamage(damager, victim, event.getCause())) {
            if (event.isCancelled()) {
                if (victim instanceof Ageable) {
                    Ageable ageable = (Ageable) victim;
                    if (ageable.getAge() == -24000) {
                        ageable.setAge(0);
                        ageable.setAdult();
                    }
                }
            }
            event.setCancelled(true);
        }
    }

    public boolean entityDamage(Entity damager, Entity victim) {
        return entityDamage(damager,victim,null);
    }

    public boolean entityDamage(Entity damager, Entity victim, EntityDamageEvent.DamageCause cause) {
        Location damagerLocation = BukkitUtil.getLocation(damager);
        Location victimLocation = BukkitUtil.getLocation(victim);
        PlotArea damagerArea = damagerLocation.getPlotArea();
        PlotArea victimArea;
        if (damagerArea != null && damagerArea
            .contains(victimLocation.getX(), victimLocation.getZ())) {
            victimArea = damagerArea;
        } else {
            victimArea = victimLocation.getPlotArea();
        }
        if (damagerArea == null && victimArea == null) {
            return true;
        }

        Plot damagerPlot;
        if (damagerArea != null) {
            damagerPlot = damagerArea.getPlot(damagerLocation);
        } else {
            damagerPlot = null;
        }
        Plot victimPlot;
        if (victimArea != null) {
            victimPlot = victimArea.getPlot(victimLocation);
        } else {
            victimPlot = null;
        }

        Plot plot;
        String stub;
        if (damagerPlot == null && victimPlot == null) {
            if (damagerArea == null) {
                return true;
            }
            plot = null;
            stub = "road";
        } else {
            // Prioritize plots for close to seamless pvp zones
            if (victim.getTicksLived() > damager.getTicksLived()) {
                if (damagerPlot == null || !(victim instanceof Player)) {
                    if (victimPlot == null) {
                        plot = damagerPlot;
                    } else {
                        plot = victimPlot;
                    }
                } else {
                    plot = damagerPlot;
                }
            } else if (damagerPlot == null || !(victim instanceof Player)) {
                if (victimPlot == null) {
                    plot = damagerPlot;
                } else {
                    plot = victimPlot;
                }
            } else if (victimPlot == null) {
                plot = damagerPlot;
            } else {
                plot = victimPlot;
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
        } else if (damager instanceof Projectile) {
            Projectile projectile = (Projectile) damager;
            ProjectileSource shooter = projectile.getShooter();
            if (shooter instanceof Player) { // shooter is player
                player = (Player) shooter;
            } else { // shooter is not player
                if (shooter instanceof BlockProjectileSource) {
                    Location sLoc = BukkitUtil
                        .getLocation(((BlockProjectileSource) shooter).getBlock().getLocation());
                    damagerPlot = damagerArea.getPlot(sLoc);
                }
                player = null;
            }
        } else { // Attacker is not player
            player = null;
        }
        if (player != null) {
            PlotPlayer plotPlayer = BukkitUtil.getPlayer(player);
            if (victim instanceof Hanging) { // hanging
                if (plot != null && (plot.getFlag(Flags.HANGING_BREAK, false) || plot
                    .isAdded(plotPlayer.getUUID()))) {
                    return true;
                }
                if (!Permissions.hasPermission(plotPlayer, "plots.admin.destroy." + stub)) {
                    MainUtil.sendMessage(plotPlayer, C.NO_PERMISSION_EVENT,
                        "plots.admin.destroy." + stub);
                    return false;
                }
            } else if (victim.getEntityId() == 30) {
                if (plot != null && (plot.getFlag(Flags.MISC_BREAK, false) || plot
                    .isAdded(plotPlayer.getUUID()))) {
                    return true;
                }
                if (!Permissions.hasPermission(plotPlayer, "plots.admin.destroy." + stub)) {
                    MainUtil.sendMessage(plotPlayer, C.NO_PERMISSION_EVENT,
                        "plots.admin.destroy." + stub);
                    return false;
                }
            } else if (victim instanceof Monster
                || victim instanceof EnderDragon) { // victim is monster
                if (plot != null && (plot.getFlag(Flags.HOSTILE_ATTACK, false) || plot
                    .getFlag(Flags.PVE, false) || plot.isAdded(plotPlayer.getUUID()))) {
                    return true;
                }
                if (!Permissions.hasPermission(plotPlayer, "plots.admin.pve." + stub)) {
                    MainUtil
                        .sendMessage(plotPlayer, C.NO_PERMISSION_EVENT, "plots.admin.pve." + stub);
                    return false;
                }
            } else if (victim instanceof Tameable) { // victim is tameable
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
            } else if (victim instanceof Creature) { // victim is animal
                if (plot != null && (plot.getFlag(Flags.ANIMAL_ATTACK, false) || plot
                    .getFlag(Flags.PVE, false) || plot.isAdded(plotPlayer.getUUID()))) {
                    return true;
                }
                if (!Permissions.hasPermission(plotPlayer, "plots.admin.pve." + stub)) {
                    MainUtil
                        .sendMessage(plotPlayer, C.NO_PERMISSION_EVENT, "plots.admin.pve." + stub);
                    return false;
                }
            } else if (victim instanceof Vehicle) { // Vehicles are managed in vehicle destroy event
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

        //disable the firework damage. too much of a headache to support on legacy.
        if (victimPlot != null) {
            if (EntityDamageEvent.DamageCause.ENTITY_EXPLOSION == cause && damager.getType() == EntityType.FIREWORK) {
                return false;
            }
        }
        if (damagerPlot != null && (!damagerPlot.equals(victimPlot) || Objects
            .equals(damagerPlot.owner, victimPlot.owner))) {
            return victimPlot != null && Flags.PVE.isTrue(victimPlot);
        }
        if (victimPlot != null) {
            if (Flags.PVE.isTrue(victimPlot)) {
                return true;
            }
        }
        if (!(damager instanceof Arrow && !(victim instanceof Creature))) {
            return true;
        }
        return false;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerEggThrow(PlayerEggThrowEvent event) {
        Location l = BukkitUtil.getLocation(event.getEgg().getLocation());
        PlotArea area = l.getPlotArea();
        if (area == null) {
            return;
        }
        Player player = event.getPlayer();
        PlotPlayer plotPlayer = BukkitUtil.getPlayer(player);
        Plot plot = area.getPlot(l);
        if (plot == null) {
            if (!Permissions.hasPermission(plotPlayer, "plots.admin.projectile.road")) {
                MainUtil
                    .sendMessage(plotPlayer, C.NO_PERMISSION_EVENT, "plots.admin.projectile.road");
                event.setHatching(false);
            }
        } else if (!plot.hasOwner()) {
            if (!Permissions.hasPermission(plotPlayer, "plots.admin.projectile.unowned")) {
                MainUtil.sendMessage(plotPlayer, C.NO_PERMISSION_EVENT,
                    "plots.admin.projectile.unowned");
                event.setHatching(false);
            }
        } else if (!plot.isAdded(plotPlayer.getUUID())) {
            if (!Permissions.hasPermission(plotPlayer, "plots.admin.projectile.other")) {
                MainUtil
                    .sendMessage(plotPlayer, C.NO_PERMISSION_EVENT, "plots.admin.projectile.other");
                event.setHatching(false);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void blockCreate(BlockPlaceEvent event) {
        Location location = BukkitUtil.getLocation(event.getBlock().getLocation());
        PlotArea area = location.getPlotArea();
        if (area == null) {
            return;
        }
        Player player = event.getPlayer();
        PlotPlayer pp = BukkitUtil.getPlayer(player);
        Plot plot = area.getPlot(location);
        if (plot != null) {
            if ((location.getY() > area.MAX_BUILD_HEIGHT || location.getY() < area.MIN_BUILD_HEIGHT)
                && !Permissions.hasPermission(pp, C.PERMISSION_ADMIN_BUILD_HEIGHTLIMIT)) {
                event.setCancelled(true);
                MainUtil.sendMessage(pp,
                    C.HEIGHT_LIMIT.s().replace("{limit}", String.valueOf(area.MAX_BUILD_HEIGHT)));
            }
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
                    if (place.contains(PlotBlock.get((short) block.getTypeId(), block.getData()))) {
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
            if (plot.getFlag(Flags.DISABLE_PHYSICS, false)) {
                Block block = event.getBlockPlaced();
                if (block.getType().hasGravity()) {
                    sendBlockChange(block.getLocation(), block.getType(), block.getData());
                }
            }
        } else if (!Permissions.hasPermission(pp, C.PERMISSION_ADMIN_BUILD_ROAD)) {
            MainUtil.sendMessage(pp, C.NO_PERMISSION_EVENT, C.PERMISSION_ADMIN_BUILD_ROAD);
            event.setCancelled(true);
        }
    }
}
