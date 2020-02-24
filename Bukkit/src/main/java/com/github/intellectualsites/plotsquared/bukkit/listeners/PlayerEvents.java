package com.github.intellectualsites.plotsquared.bukkit.listeners;

import com.destroystokyo.paper.MaterialTags;
import com.github.intellectualsites.plotsquared.bukkit.object.BukkitPlayer;
import com.github.intellectualsites.plotsquared.bukkit.util.BukkitUtil;
import com.github.intellectualsites.plotsquared.bukkit.util.UpdateUtility;
import com.github.intellectualsites.plotsquared.plot.PlotSquared;
import com.github.intellectualsites.plotsquared.plot.config.Captions;
import com.github.intellectualsites.plotsquared.plot.config.Settings;
import com.github.intellectualsites.plotsquared.plot.flags.implementations.AnimalAttackFlag;
import com.github.intellectualsites.plotsquared.plot.flags.implementations.AnimalCapFlag;
import com.github.intellectualsites.plotsquared.plot.flags.implementations.AnimalInteractFlag;
import com.github.intellectualsites.plotsquared.plot.flags.implementations.BlockBurnFlag;
import com.github.intellectualsites.plotsquared.plot.flags.implementations.BlockIgnitionFlag;
import com.github.intellectualsites.plotsquared.plot.flags.implementations.BlockedCmdsFlag;
import com.github.intellectualsites.plotsquared.plot.flags.implementations.BreakFlag;
import com.github.intellectualsites.plotsquared.plot.flags.implementations.DenyTeleportFlag;
import com.github.intellectualsites.plotsquared.plot.flags.implementations.DisablePhysicsFlag;
import com.github.intellectualsites.plotsquared.plot.flags.implementations.DoneFlag;
import com.github.intellectualsites.plotsquared.plot.flags.implementations.EntityCapFlag;
import com.github.intellectualsites.plotsquared.plot.flags.implementations.ExplosionFlag;
import com.github.intellectualsites.plotsquared.plot.flags.implementations.GrassGrowFlag;
import com.github.intellectualsites.plotsquared.plot.flags.implementations.HangingBreakFlag;
import com.github.intellectualsites.plotsquared.plot.flags.implementations.HangingPlaceFlag;
import com.github.intellectualsites.plotsquared.plot.flags.implementations.HostileAttackFlag;
import com.github.intellectualsites.plotsquared.plot.flags.implementations.HostileCapFlag;
import com.github.intellectualsites.plotsquared.plot.flags.implementations.HostileInteractFlag;
import com.github.intellectualsites.plotsquared.plot.flags.implementations.IceFormFlag;
import com.github.intellectualsites.plotsquared.plot.flags.implementations.IceMeltFlag;
import com.github.intellectualsites.plotsquared.plot.flags.implementations.KelpGrowFlag;
import com.github.intellectualsites.plotsquared.plot.flags.implementations.LiquidFlowFlag;
import com.github.intellectualsites.plotsquared.plot.flags.implementations.MiscBreakFlag;
import com.github.intellectualsites.plotsquared.plot.flags.implementations.MiscCapFlag;
import com.github.intellectualsites.plotsquared.plot.flags.implementations.MiscInteractFlag;
import com.github.intellectualsites.plotsquared.plot.flags.implementations.MobCapFlag;
import com.github.intellectualsites.plotsquared.plot.flags.implementations.MobPlaceFlag;
import com.github.intellectualsites.plotsquared.plot.flags.implementations.MycelGrowFlag;
import com.github.intellectualsites.plotsquared.plot.flags.implementations.PlaceFlag;
import com.github.intellectualsites.plotsquared.plot.flags.implementations.PlayerInteractFlag;
import com.github.intellectualsites.plotsquared.plot.flags.implementations.PveFlag;
import com.github.intellectualsites.plotsquared.plot.flags.implementations.PvpFlag;
import com.github.intellectualsites.plotsquared.plot.flags.implementations.RedstoneFlag;
import com.github.intellectualsites.plotsquared.plot.flags.implementations.SnowFormFlag;
import com.github.intellectualsites.plotsquared.plot.flags.implementations.SnowMeltFlag;
import com.github.intellectualsites.plotsquared.plot.flags.implementations.SoilDryFlag;
import com.github.intellectualsites.plotsquared.plot.flags.implementations.TamedAttackFlag;
import com.github.intellectualsites.plotsquared.plot.flags.implementations.TamedInteractFlag;
import com.github.intellectualsites.plotsquared.plot.flags.implementations.UntrustedVisitFlag;
import com.github.intellectualsites.plotsquared.plot.flags.implementations.UseFlag;
import com.github.intellectualsites.plotsquared.plot.flags.implementations.VehicleBreakFlag;
import com.github.intellectualsites.plotsquared.plot.flags.implementations.VehicleCapFlag;
import com.github.intellectualsites.plotsquared.plot.flags.implementations.VehicleUseFlag;
import com.github.intellectualsites.plotsquared.plot.flags.implementations.VillagerInteractFlag;
import com.github.intellectualsites.plotsquared.plot.flags.implementations.VineGrowFlag;
import com.github.intellectualsites.plotsquared.plot.flags.types.BlockTypeWrapper;
import com.github.intellectualsites.plotsquared.plot.listener.PlayerBlockEventType;
import com.github.intellectualsites.plotsquared.plot.listener.PlotListener;
import com.github.intellectualsites.plotsquared.plot.object.Location;
import com.github.intellectualsites.plotsquared.plot.object.Plot;
import com.github.intellectualsites.plotsquared.plot.object.PlotArea;
import com.github.intellectualsites.plotsquared.plot.object.PlotHandler;
import com.github.intellectualsites.plotsquared.plot.object.PlotId;
import com.github.intellectualsites.plotsquared.plot.object.PlotInventory;
import com.github.intellectualsites.plotsquared.plot.object.PlotMessage;
import com.github.intellectualsites.plotsquared.plot.object.PlotPlayer;
import com.github.intellectualsites.plotsquared.plot.object.StringWrapper;
import com.github.intellectualsites.plotsquared.plot.util.EntityUtil;
import com.github.intellectualsites.plotsquared.plot.util.EventUtil;
import com.github.intellectualsites.plotsquared.plot.util.MainUtil;
import com.github.intellectualsites.plotsquared.plot.util.MathMan;
import com.github.intellectualsites.plotsquared.plot.util.Permissions;
import com.github.intellectualsites.plotsquared.plot.util.RegExUtil;
import com.github.intellectualsites.plotsquared.plot.util.TaskManager;
import com.github.intellectualsites.plotsquared.plot.util.UUIDHandler;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.world.block.BlockType;
import io.papermc.lib.PaperLib;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Animals;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Creature;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Hanging;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.entity.Tameable;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.entity.Vehicle;
import org.bukkit.entity.Villager;
import org.bukkit.entity.WaterMob;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.block.EntityBlockFormEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityCombustByEntityEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.event.entity.LingeringPotionSplashEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
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
import org.bukkit.event.vehicle.VehicleCreateEvent;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.event.vehicle.VehicleEntityCollisionEvent;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.event.world.StructureGrowEvent;
import org.bukkit.help.HelpTopic;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.Directional;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.projectiles.BlockProjectileSource;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.util.Vector;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Player Events involving plots.
 */
@SuppressWarnings("unused") public class PlayerEvents extends PlotListener implements Listener {

    private boolean pistonBlocks = true;
    private float lastRadius;
    // To prevent recursion
    private boolean tmpTeleport = true;
    private Field fieldPlayer;
    private PlayerMoveEvent moveTmp;
    private String internalVersion;
    private String spigotVersion;

    {
        try {
            fieldPlayer = PlayerEvent.class.getDeclaredField("player");
            fieldPlayer.setAccessible(true);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    public static void sendBlockChange(final org.bukkit.Location bloc, final BlockData data) {
        TaskManager.runTaskLater(() -> {
            String world = bloc.getWorld().getName();
            int x = bloc.getBlockX();
            int z = bloc.getBlockZ();
            int distance = Bukkit.getViewDistance() * 16;
            for (Entry<String, PlotPlayer> entry : UUIDHandler.getPlayers().entrySet()) {
                PlotPlayer player = entry.getValue();
                Location location = player.getLocation();
                if (location.getWorld().equals(world)) {
                    if (16 * Math.abs(location.getX() - x) / 16 > distance
                        || 16 * Math.abs(location.getZ() - z) / 16 > distance) {
                        continue;
                    }
                    ((BukkitPlayer) player).player.sendBlockChange(bloc, data);
                }
            }
        }, 3);
    }

    public static boolean checkEntity(Entity entity, Plot plot) {
        if (plot == null || !plot.hasOwner() || plot.getFlags().isEmpty() && plot
            .getArea().getFlagContainer().getFlagMap().isEmpty()) {
            return false;
        }
        switch (entity.getType()) {
            case PLAYER:
                return false;
            case ARROW:
            case DRAGON_FIREBALL:
            case DROPPED_ITEM:
            case EGG:
            case ENDER_PEARL:
            case FIREBALL:
            case LLAMA_SPIT:
            case SHULKER_BULLET:
            case SMALL_FIREBALL:
            case SNOWBALL:
            case SPECTRAL_ARROW:
            case SPLASH_POTION:
            case THROWN_EXP_BOTTLE:
                // projectile
            case FALLING_BLOCK:
            case PRIMED_TNT:
                // Block entities
            case AREA_EFFECT_CLOUD:
            case ENDER_CRYSTAL:
            case ENDER_SIGNAL:
            case EVOKER_FANGS:
            case EXPERIENCE_ORB:
            case FIREWORK:
            case FISHING_HOOK:
            case LEASH_HITCH:
            case LIGHTNING:
            case UNKNOWN:
            case WITHER_SKULL:
                // non moving / unmovable
                return EntityUtil.checkEntity(plot, EntityCapFlag.ENTITY_CAP_UNLIMITED);
            case ARMOR_STAND:
            case ITEM_FRAME:
            case PAINTING:
                return EntityUtil.checkEntity(plot,
                    EntityCapFlag.ENTITY_CAP_UNLIMITED,
                    MiscCapFlag.MISC_CAP_UNLIMITED);
            // misc
            case BOAT:
            case MINECART:
            case MINECART_CHEST:
            case MINECART_COMMAND:
            case MINECART_FURNACE:
            case MINECART_HOPPER:
            case MINECART_MOB_SPAWNER:
            case MINECART_TNT:
                return EntityUtil.checkEntity(plot,
                    EntityCapFlag.ENTITY_CAP_UNLIMITED,
                    VehicleCapFlag.VEHICLE_CAP_UNLIMITED);
            case BAT:
            case CHICKEN:
            case CAT:
            case COD:
            case COW:
            case DOLPHIN:
            case DONKEY:
            case FOX:
            case HORSE:
            case IRON_GOLEM:
            case LLAMA:
            case MULE:
            case MUSHROOM_COW:
            case OCELOT:
            case PANDA:
            case PARROT:
            case PIG:
            case POLAR_BEAR:
            case PUFFERFISH:
            case RABBIT:
            case SALMON:
            case SHEEP:
            case SKELETON_HORSE:
            case SNOWMAN:
            case SQUID:
            case TRADER_LLAMA:
            case TROPICAL_FISH:
            case TURTLE:
            case VILLAGER:
            case WOLF:
            case ZOMBIE_HORSE:
                // animal
                return EntityUtil.checkEntity(plot,
                    EntityCapFlag.ENTITY_CAP_UNLIMITED,
                    MobCapFlag.MOB_CAP_UNLIMITED,
                    AnimalCapFlag.ANIMAL_CAP_UNLIMITED);
            case BLAZE:
            case CAVE_SPIDER:
            case CREEPER:
            case DROWNED:
            case ELDER_GUARDIAN:
            case ENDERMAN:
            case ENDERMITE:
            case ENDER_DRAGON:
            case EVOKER:
            case GHAST:
            case GIANT:
            case GUARDIAN:
            case HUSK:
            case ILLUSIONER:
            case MAGMA_CUBE:
            case PIG_ZOMBIE:
            case SHULKER:
            case SILVERFISH:
            case SKELETON:
            case SLIME:
            case SPIDER:
            case STRAY:
            case VEX:
            case VINDICATOR:
            case WITCH:
            case WITHER:
            case WITHER_SKELETON:
            case ZOMBIE:
            case ZOMBIE_VILLAGER:
            case PILLAGER:
            case PHANTOM:
            case RAVAGER:
                // monster
                return EntityUtil
                    .checkEntity(plot,
                        EntityCapFlag.ENTITY_CAP_UNLIMITED,
                        MobCapFlag.MOB_CAP_UNLIMITED,
                        HostileCapFlag.HOSTILE_CAP_UNLIMITED);
            default:
                if (entity instanceof LivingEntity) {
                    if (entity instanceof Animals || entity instanceof WaterMob) {
                        return EntityUtil.checkEntity(plot,
                            EntityCapFlag.ENTITY_CAP_UNLIMITED,
                            MobCapFlag.MOB_CAP_UNLIMITED,
                            AnimalCapFlag.ANIMAL_CAP_UNLIMITED);
                    } else if (entity instanceof Monster) {
                        return EntityUtil.checkEntity(plot,
                            EntityCapFlag.ENTITY_CAP_UNLIMITED,
                            MobCapFlag.MOB_CAP_UNLIMITED,
                            HostileCapFlag.HOSTILE_CAP_UNLIMITED);
                    } else {
                        return EntityUtil.checkEntity(plot,
                            EntityCapFlag.ENTITY_CAP_UNLIMITED,
                            MobCapFlag.MOB_CAP_UNLIMITED);
                    }
                }
                if (entity instanceof Vehicle) {
                    return EntityUtil.checkEntity(plot,
                        EntityCapFlag.ENTITY_CAP_UNLIMITED,
                        VehicleCapFlag.VEHICLE_CAP_UNLIMITED);
                }
                if (entity instanceof Hanging) {
                    return EntityUtil.checkEntity(plot,
                        EntityCapFlag.ENTITY_CAP_UNLIMITED,
                        MiscCapFlag.MISC_CAP_UNLIMITED);
                }
                return EntityUtil.checkEntity(plot, EntityCapFlag.ENTITY_CAP_UNLIMITED);
        }
    }

    @EventHandler public void onVehicleEntityCollision(VehicleEntityCollisionEvent e) {
        if (e.getVehicle().getType() == EntityType.BOAT) {
            Location location = BukkitUtil.getLocation(e.getEntity());
            if (location.isPlotArea()) {
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
    }

    @EventHandler public void onRedstoneEvent(BlockRedstoneEvent event) {
        Block block = event.getBlock();
/*        switch (block.getType()) {
            case OBSERVER:
            case REDSTONE:
            case REDSTONE_ORE:
            case REDSTONE_BLOCK:
            case REDSTONE_TORCH:
            case REDSTONE_WALL_TORCH:
            case REDSTONE_WIRE:
            case REDSTONE_LAMP:
            case PISTON_HEAD:
            case PISTON:
            case STICKY_PISTON:
            case MOVING_PISTON:
            case LEVER:
            case ACACIA_BUTTON:
            case BIRCH_BUTTON:
            case DARK_OAK_BUTTON:
            case JUNGLE_BUTTON:
            case OAK_BUTTON:
            case SPRUCE_BUTTON:
            case STONE_BUTTON:
            case STONE_PRESSURE_PLATE:
            case ACACIA_PRESSURE_PLATE:
            case BIRCH_PRESSURE_PLATE:
            case DARK_OAK_PRESSURE_PLATE:
            case HEAVY_WEIGHTED_PRESSURE_PLATE:
            case JUNGLE_PRESSURE_PLATE:
            case LIGHT_WEIGHTED_PRESSURE_PLATE:
            case OAK_PRESSURE_PLATE:
            case SPRUCE_PRESSURE_PLATE:
            case SPRUCE_DOOR:
            case BIRCH_DOOR:
            case JUNGLE_DOOR:
            case ACACIA_DOOR:
            case DARK_OAK_DOOR:
            case IRON_DOOR:
            case OAK_DOOR:
            case IRON_TRAPDOOR:
            case SPRUCE_FENCE_GATE:
            case BIRCH_FENCE_GATE:
            case JUNGLE_FENCE_GATE:
            case ACACIA_FENCE_GATE:
            case DARK_OAK_FENCE_GATE:
            case OAK_FENCE_GATE:
            case POWERED_RAIL:
                return;
            default:*/
        Location location = BukkitUtil.getLocation(block.getLocation());
        PlotArea area = location.getPlotArea();
        if (area == null) {
            return;
        }
        Plot plot = location.getOwnedPlot();
        if (plot == null) {
            return;
        }
        if (!plot.getFlag(RedstoneFlag.class)) {
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
                disable = UUIDHandler.getPlayer(plot.guessOwner()) == null;
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
        //}
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onPhysicsEvent(BlockPhysicsEvent event) {
        switch (event.getChangedType()) {
            case COMPARATOR: {
                Block block = event.getBlock();
                Location location = BukkitUtil.getLocation(block.getLocation());
                if (location.isPlotArea()) {
                    return;
                }
                Plot plot = location.getOwnedPlotAbs();
                if (plot == null) {
                    return;
                }
                if (!plot.getFlag(RedstoneFlag.class)) {
                    event.setCancelled(true);
                }
                return;
            }
            case ANVIL:
            case DRAGON_EGG:
            case GRAVEL:
            case SAND:
            case TURTLE_EGG:
            case TURTLE_HELMET:
            case TURTLE_SPAWN_EGG: {
                Block block = event.getBlock();
                Location location = BukkitUtil.getLocation(block.getLocation());
                PlotArea area = location.getPlotArea();
                if (area == null) {
                    return;
                }
                Plot plot = area.getOwnedPlotAbs(location);
                if (plot == null) {
                    return;
                }
                if (plot.getFlag(DisablePhysicsFlag.class)) {
                    event.setCancelled(true);
                }
                return;
            }
            default:
                if (Settings.Redstone.DETECT_INVALID_EDGE_PISTONS) {
                    Block block = event.getBlock();
                    switch (block.getType()) {
                        case PISTON:
                        case STICKY_PISTON:
                            org.bukkit.block.data.Directional piston =
                                (org.bukkit.block.data.Directional) block.getBlockData();
                            Location location = BukkitUtil.getLocation(block.getLocation());
                            PlotArea area = location.getPlotArea();
                            if (area == null) {
                                return;
                            }
                            Plot plot = area.getOwnedPlotAbs(location);
                            if (plot == null) {
                                return;
                            }
                            switch (piston.getFacing()) {
                                case EAST:
                                    location.setX(location.getX() + 1);
                                    break;
                                case SOUTH:
                                    location.setX(location.getX() - 1);
                                    break;
                                case WEST:
                                    location.setZ(location.getZ() + 1);
                                    break;
                                case NORTH:
                                    location.setZ(location.getZ() - 1);
                                    break;
                            }
                            Plot newPlot = area.getOwnedPlotAbs(location);
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
        Location location = BukkitUtil.getLocation(entity);
        if (!PlotSquared.get().hasPlotArea(location.getWorld())) {
            return;
        }
        PlotPlayer pp = BukkitUtil.getPlayer((Player) shooter);
        Plot plot = location.getOwnedPlot();
        if (plot != null && !plot.isAdded(pp.getUUID())) {
            entity.remove();
            event.setCancelled(true);
        }
    }

    @EventHandler public boolean onProjectileHit(ProjectileHitEvent event) {
        Projectile entity = event.getEntity();
        Location location = BukkitUtil.getLocation(entity);
        if (!PlotSquared.get().hasPlotArea(location.getWorld())) {
            return true;
        }
        PlotArea area = location.getPlotArea();
        if (area == null) {
            return true;
        }
        Plot plot = area.getPlot(location);
        ProjectileSource shooter = entity.getShooter();
        if (shooter instanceof Player) {
            PlotPlayer pp = BukkitUtil.getPlayer((Player) shooter);
            if (plot == null) {
                if (!Permissions.hasPermission(pp, Captions.PERMISSION_PROJECTILE_UNOWNED)) {
                    entity.remove();
                    return false;
                }
                return true;
            }
            if (plot.isAdded(pp.getUUID()) || Permissions
                .hasPermission(pp, Captions.PERMISSION_PROJECTILE_OTHER)) {
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

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void playerCommand(PlayerCommandPreprocessEvent event) {
        String msg = event.getMessage().toLowerCase().replaceAll("/", "").trim();
        if (msg.isEmpty()) {
            return;
        }
        Player player = event.getPlayer();
        PlotPlayer plotPlayer = BukkitUtil.getPlayer(player);
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
        if (plot == null) {
            return;
        }

        List<String> blockedCommands = plot.getFlag(BlockedCmdsFlag.class);
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
                    if (plot.isAdded(plotPlayer.getUUID())) {
                        perm = "plots.admin.command.blocked-cmds.shared";
                    } else {
                        perm = "plots.admin.command.blocked-cmds.other";
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
            EventUtil.manager.doJoinTask(pp);
        }, 20);

        if (pp.hasPermission(Captions.PERMISSION_ADMIN_UPDATE_NOTIFICATION.getTranslated())
                && Settings.Enabled_Components.UPDATE_NOTIFICATIONS) {
            try {
                HttpsURLConnection connection = (HttpsURLConnection) new URL("https://api.spigotmc.org/legacy/update.php?resource=1177").openConnection();
                connection.setRequestMethod("GET");
                spigotVersion = (new BufferedReader(new InputStreamReader(connection.getInputStream()))).readLine();
            } catch (IOException e) {
                new PlotMessage(Captions.PREFIX + "Unable to check for updates, check console for further information.").color("$13");
                PlotSquared.log(Captions.PREFIX + "&cUnable to check for updates because: " + e);
                return;
            }

            if (!UpdateUtility.internalVersion.equals(UpdateUtility.spigotVersion)) {
                new PlotMessage("-----------------------------------").send(pp);
                new PlotMessage(Captions.PREFIX + "There appears to be a PlotSquared update available!").color("$1").tooltip("https://www.spigotmc.org/resources/1177/updates").send(pp);
                new PlotMessage(Captions.PREFIX + "The latest version is " + spigotVersion).color("$1").tooltip("https://www.spigotmc.org/resources/1177/updates").send(pp);
                new PlotMessage(Captions.PREFIX + "https://www.spigotmc.org/resources/1177/updates").color("$1").tooltip("https://www.spigotmc.org/resources/1177/updates").send(pp);
                new PlotMessage("-----------------------------------").send(pp);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void playerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        PlotPlayer pp = BukkitUtil.getPlayer(player);
        EventUtil.manager.doRespawnTask(pp);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        PlotPlayer pp = BukkitUtil.getPlayer(player);
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
                if (!result && !(plot.getFlag(UntrustedVisitFlag.class) && plot.getHome().equals(BukkitUtil.getLocationFull(to)))) {
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
                    switch (vehicle.getType()) {
                        case BOAT:
                        case ENDER_CRYSTAL:
                        case MINECART:
                        case MINECART_CHEST:
                        case MINECART_COMMAND:
                        case MINECART_FURNACE:
                        case MINECART_HOPPER:
                        case MINECART_MOB_SPAWNER:
                        case MINECART_TNT: {
                            List<MetadataValue> meta = vehicle.getMetadata("plot");
                            Plot toPlot = BukkitUtil.getLocation(to).getPlot();
                            if (!meta.isEmpty()) {
                                Plot origin = (Plot) meta.get(0).value();
                                if (!origin.getBasePlot(false).equals(toPlot)) {
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
            PlotPlayer pp = BukkitUtil.getPlayer(player);
            // Cancel teleport
            TaskManager.TELEPORT_QUEUE.remove(pp.getName());
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
        if (plot.isDenied(plotPlayer.getUUID())) {
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
            message = Captions.color(message);
        }
        String full = partial.replace("%msg%", message);
        for (Player receiver : recipients) {
            receiver.sendMessage(full);
        }
        if (!spies.isEmpty()) {
            String spyMessage = Captions.PLOT_CHAT_SPY_FORMAT.getTranslated()
                .replace("%plot_id%", id.x + ";" + id.y)
                    .replace("%sender%", sender).replace("%msg%", message);
            for (Player player : spies) {
                player.sendMessage(spyMessage);
            }
        }
        PlotSquared.debug(full);
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
                    .hasPermission(plotPlayer, Captions.PERMISSION_ADMIN_DESTROY_GROUNDLEVEL)) {
                    MainUtil.sendMessage(plotPlayer, Captions.NO_PERMISSION_EVENT,
                        Captions.PERMISSION_ADMIN_DESTROY_GROUNDLEVEL);
                    event.setCancelled(true);
                    return;
                }
            } else if (
                (location.getY() > area.MAX_BUILD_HEIGHT || location.getY() < area.MIN_BUILD_HEIGHT)
                    && !Permissions
                    .hasPermission(plotPlayer, Captions.PERMISSION_ADMIN_BUILD_HEIGHT_LIMIT)) {
                event.setCancelled(true);
                MainUtil.sendMessage(plotPlayer, Captions.HEIGHT_LIMIT.getTranslated()
                    .replace("{limit}", String.valueOf(area.MAX_BUILD_HEIGHT)));
            }
            if (!plot.hasOwner()) {
                if (!Permissions
                    .hasPermission(plotPlayer, Captions.PERMISSION_ADMIN_DESTROY_UNOWNED, true)) {
                    event.setCancelled(true);
                }
                return;
            }
            if (!plot.isAdded(plotPlayer.getUUID())) {
                List<BlockTypeWrapper> destroy = plot.getFlag(BreakFlag.class);
                Block block = event.getBlock();
                final BlockType blockType = BukkitAdapter.asBlockType(block.getType());
                for (final BlockTypeWrapper blockTypeWrapper : destroy) {
                    if (blockTypeWrapper.accepts(blockType)) {
                        return;
                    }
                }
                if (Permissions
                    .hasPermission(plotPlayer, Captions.PERMISSION_ADMIN_DESTROY_OTHER)) {
                    return;
                }
                MainUtil.sendMessage(plotPlayer, Captions.NO_PERMISSION_EVENT,
                    Captions.PERMISSION_ADMIN_DESTROY_OTHER);
                event.setCancelled(true);
            } else if (Settings.Done.RESTRICT_BUILDING && DoneFlag.isDone(plot)) {
                if (!Permissions.hasPermission(plotPlayer, Captions.PERMISSION_ADMIN_BUILD_OTHER)) {
                    MainUtil.sendMessage(plotPlayer, Captions.NO_PERMISSION_EVENT,
                        Captions.PERMISSION_ADMIN_BUILD_OTHER);
                    event.setCancelled(true);
                    return;
                }
            }
            return;
        }
        PlotPlayer pp = BukkitUtil.getPlayer(player);
        if (Permissions.hasPermission(pp, Captions.PERMISSION_ADMIN_DESTROY_ROAD)) {
            return;
        }
        if (PlotSquared.get().worldedit != null && pp.getAttribute("worldedit")) {
            if (player.getInventory().getItemInMainHand().getType() == Material
                .getMaterial(PlotSquared.get().worldedit.getConfiguration().wandItem)) {
                return;
            }
        }
        MainUtil
            .sendMessage(pp, Captions.NO_PERMISSION_EVENT, Captions.PERMISSION_ADMIN_DESTROY_ROAD);
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBigBoom(EntityExplodeEvent event) {
        Location location = BukkitUtil.getLocation(event.getLocation());
        PlotArea area = location.getPlotArea();
        boolean plotArea = location.isPlotArea();
        if (!plotArea) {
            if (!PlotSquared.get().hasPlotArea(location.getWorld())) {
                return;
            }
            return;
        }
        Plot plot = area.getOwnedPlot(location);
        if (plot != null) {
            if (plot.getFlag(ExplosionFlag.class)) {
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
                                    new FixedMetadataValue((Plugin) PlotSquared.get().IMP, plot));
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
    public void onWorldChanged(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        PlotPlayer pp = BukkitUtil.getPlayer(player);
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

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPeskyMobsChangeTheWorldLikeWTFEvent(EntityChangeBlockEvent event) {
        Entity e = event.getEntity();
        if (!(e instanceof FallingBlock)) {
            Location location = BukkitUtil.getLocation(event.getBlock().getLocation());
            PlotArea area = location.getPlotArea();
            if (area != null) {
                Plot plot = area.getOwnedPlot(location);
                if (plot != null && plot.getFlag(MobPlaceFlag.class)) {
                    return;
                }
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityBlockForm(EntityBlockFormEvent event) {
        String world = event.getBlock().getWorld().getName();
        if (!PlotSquared.get().hasPlotArea(world)) {
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
                if (plot.getFlag(IceFormFlag.class)) {
                    return;
                }
                event.setCancelled(true);
                return;
            }
            PlotPlayer plotPlayer = BukkitUtil.getPlayer(player);
            if (!plot.isAdded(plotPlayer.getUUID())) {
                if (plot.getFlag(IceFormFlag.class)) {
                    return;
                }
                event.setCancelled(true);
                return;
            }
            return;
        }
        if (!plot.getFlag(IceFormFlag.class)) {
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
                if (!plot.getFlag(GrassGrowFlag.class)) {
                    event.setCancelled(true);
                }
                break;
            case MYCELIUM:
                if (!plot.getFlag(MycelGrowFlag.class)) {
                    event.setCancelled(true);
                }
                break;
            case VINE:
                if (!plot.getFlag(VineGrowFlag.class)) {
                    event.setCancelled(true);
                }
                break;
            case KELP:
                if (!plot.getFlag(KelpGrowFlag.class)) {
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
                if (!plot.getFlag(SnowFormFlag.class)) {
                    event.setCancelled(true);
                }
                return;
            case ICE:
            case FROSTED_ICE:
            case PACKED_ICE:
                if (!plot.getFlag(IceFormFlag.class)) {
                    event.setCancelled(true);
                }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockDamage(BlockDamageEvent event) {
        Player player = event.getPlayer();
        Location location = BukkitUtil.getLocation(event.getBlock().getLocation());
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
                if (Permissions
                    .hasPermission(plotPlayer, Captions.PERMISSION_ADMIN_DESTROY_UNOWNED)) {
                    return;
                }
                event.setCancelled(true);
                return;
            }
            PlotPlayer plotPlayer = BukkitUtil.getPlayer(player);
            if (!plot.isAdded(plotPlayer.getUUID())) {
                List<BlockTypeWrapper> destroy = plot.getFlag(BreakFlag.class);
                Block block = event.getBlock();
                if (destroy.contains(BlockTypeWrapper.get(BukkitAdapter.asBlockType(block.getType()))) || Permissions
                    .hasPermission(plotPlayer, Captions.PERMISSION_ADMIN_DESTROY_OTHER)) {
                    return;
                }
                event.setCancelled(true);
                return;
            }
            return;
        }
        PlotPlayer plotPlayer = BukkitUtil.getPlayer(player);
        if (Permissions.hasPermission(plotPlayer, Captions.PERMISSION_ADMIN_DESTROY_ROAD)) {
            return;
        }
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onFade(BlockFadeEvent event) {
        Block block = event.getBlock();
        Location location = BukkitUtil.getLocation(block.getLocation());
        PlotArea area = location.getPlotArea();
        if (area == null) {
            return;
        }
        Plot plot = area.getOwnedPlot(location);
        if (plot == null) {
            event.setCancelled(true);
            return;
        }
        switch (block.getType()) {
            case ICE:
                if (!plot.getFlag(IceMeltFlag.class)) {
                    event.setCancelled(true);
                }
                break;
            case SNOW:
                if (!plot.getFlag(SnowMeltFlag.class)) {
                    event.setCancelled(true);
                }
                break;
            case FARMLAND:
                if (!plot.getFlag(SoilDryFlag.class)) {
                    event.setCancelled(true);
                }
                break;
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
            if (plot.getFlag(DisablePhysicsFlag.class)) {
                event.setCancelled(true);
                return;
            } else if (!area.contains(fLocation.getX(), fLocation.getZ()) || !Objects
                .equals(plot, area.getOwnedPlot(fLocation))) {
                event.setCancelled(true);
                return;
            }
            if (!plot.getFlag(LiquidFlowFlag.class)) {
                switch (to.getType()) {
                    case WATER:
                    case LAVA:
                        event.setCancelled(true);
                }
            }
        } else if (!area.contains(fLocation.getX(), fLocation.getZ()) || !Objects
            .equals(null, area.getOwnedPlot(fLocation))) {
            event.setCancelled(true);
        } else if (event.getBlock().isLiquid()) {
            final org.bukkit.Location location = event.getBlock().getLocation();

            /*
                X = block location
                A-H = potential plot locations

               Z
               ^
               |    A B C
               o    D X E
               |    F G H
               v
                <-----O-----> x
             */
            if (BukkitUtil.getPlot(location.clone().add(-1, 0, 1)  /* A */ ) != null ||
                BukkitUtil.getPlot(location.clone().add(1, 0, 0)   /* B */ ) != null ||
                BukkitUtil.getPlot(location.clone().add(1, 0, 1)   /* C */ ) != null ||
                BukkitUtil.getPlot(location.clone().add(-1, 0, 0)  /* D */ ) != null ||
                BukkitUtil.getPlot(location.clone().add(1, 0, 0)   /* E */ ) != null ||
                BukkitUtil.getPlot(location.clone().add(-1, 0, -1) /* F */ ) != null ||
                BukkitUtil.getPlot(location.clone().add(0, 0, -1)  /* G */ ) != null ||
                BukkitUtil.getPlot(location.clone().add(1, 0, 1)   /* H */ ) != null) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onGrow(BlockGrowEvent event) {
        Block block = event.getBlock();
        Location location = BukkitUtil.getLocation(block.getLocation());
        if (location.isUnownedPlotArea()) {
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
            if (!PlotSquared.get().hasPlotArea(location.getWorld())) {
                return;
            }
            for (Block block1 : event.getBlocks()) {
                if (BukkitUtil.getLocation(block1.getLocation().add(relative)).isPlotArea()) {
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
        for (Block block1 : event.getBlocks()) {
            Location bloc = BukkitUtil.getLocation(block1.getLocation());
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
            if (!PlotSquared.get().hasPlotArea(location.getWorld())) {
                return;
            }
            if (this.pistonBlocks) {
                try {
                    for (Block pulled : event.getBlocks()) {
                        location = BukkitUtil.getLocation(pulled.getLocation());
                        if (location.isPlotArea()) {
                            event.setCancelled(true);
                            return;
                        }
                    }
                } catch (Throwable ignored) {
                    this.pistonBlocks = false;
                }
            }
            if (!this.pistonBlocks && !block.getType().toString().contains("PISTON")) {
                BlockFace dir = event.getDirection();
                location = BukkitUtil.getLocation(block.getLocation()
                    .add(dir.getModX() * 2, dir.getModY() * 2, dir.getModZ() * 2));
                if (location.isPlotArea()) {
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
        if (!this.pistonBlocks && !block.getType().toString().contains("PISTON")) {
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
    public void onBlockDispense(BlockDispenseEvent event) {
        Material type = event.getItem().getType();
        switch (type) {
            case WATER_BUCKET:
            case LAVA_BUCKET: {
                if (event.getBlock().getType() == Material.DROPPER) {
                    return;
                }
                BlockFace targetFace =
                    ((Directional) event.getBlock().getState().getData()).getFacing();
                Location location =
                    BukkitUtil.getLocation(event.getBlock().getRelative(targetFace).getLocation());
                if (location.isPlotRoad()) {
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onStructureGrow(StructureGrowEvent event) {
        if (!PlotSquared.get().hasPlotArea(event.getWorld().getName())) {
            return;
        }
        List<org.bukkit.block.BlockState> blocks = event.getBlocks();
        if (blocks.isEmpty()) {
            return;
        }
        Location location = BukkitUtil.getLocation(blocks.get(0).getLocation());
        PlotArea area = location.getPlotArea();
        if (area == null) {
            for (int i = blocks.size() - 1; i >= 0; i--) {
                location = BukkitUtil.getLocation(blocks.get(i).getLocation());
                if (location.isPlotArea()) {
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
             * plot → the base plot of the merged area
             * origin → the plot where the event gets called
             */

            // Are plot and origin different AND are both plots merged
            if (!Objects.equals(plot, origin) && (!plot.isMerged() && !origin.isMerged())) {
                event.getBlocks().remove(i);
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
        PlotPlayer pp = BukkitUtil.getPlayer(player);
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
        ItemMeta newMeta = newItem.getItemMeta();
        ItemMeta oldMeta = newItem.getItemMeta();
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
    public void onPotionSplash(LingeringPotionSplashEvent event) {
        Projectile entity = event.getEntity();
        Location location = BukkitUtil.getLocation(entity);
        if (!PlotSquared.get().hasPlotArea(location.getWorld())) {
            return;
        }
        if (!this.onProjectileHit(event)) {
            event.setCancelled(true);
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
        PlotPlayer pp = BukkitUtil.getPlayer(e.getPlayer());
        if (plot == null) {
            if (!Permissions.hasPermission(pp, "plots.admin.interact.road")) {
                MainUtil
                    .sendMessage(pp, Captions.NO_PERMISSION_EVENT, "plots.admin.interact.road");
                e.setCancelled(true);
            }
        } else {
            if (Settings.Done.RESTRICT_BUILDING && DoneFlag.isDone(plot)) {
                if (!Permissions.hasPermission(pp, Captions.PERMISSION_ADMIN_BUILD_OTHER)) {
                    MainUtil.sendMessage(pp, Captions.NO_PERMISSION_EVENT, Captions.PERMISSION_ADMIN_BUILD_OTHER);
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
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBigBoom(BlockExplodeEvent event) {
        Block block = event.getBlock();
        Location location = BukkitUtil.getLocation(block.getLocation());
        String world = location.getWorld();
        if (!PlotSquared.get().hasPlotArea(world)) {
            return;
        }
        PlotArea area = location.getPlotArea();
        if (area == null) {
            Iterator<Block> iterator = event.blockList().iterator();
            while (iterator.hasNext()) {
                location = BukkitUtil.getLocation(iterator.next().getLocation());
                if (location.isPlotArea()) {
                    iterator.remove();
                }
            }
            return;
        }
        Plot plot = area.getOwnedPlot(location);
        if (plot == null || !plot.getFlag(ExplosionFlag.class)) {
            event.setCancelled(true);
        }
        event.blockList().removeIf(
            blox -> !plot.equals(area.getOwnedPlot(BukkitUtil.getLocation(blox.getLocation()))));
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onCancelledInteract(PlayerInteractEvent event) {
        if (event.isCancelled() && event.getAction() == Action.RIGHT_CLICK_AIR) {
            Player player = event.getPlayer();
            PlotPlayer pp = BukkitUtil.getPlayer(player);
            PlotArea area = pp.getPlotAreaAbs();
            if (area == null) {
                return;
            }
            if (event.getAction() == Action.RIGHT_CLICK_AIR) {
                Material item = event.getMaterial();
                if (item.toString().toLowerCase().endsWith("egg")) {
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
            if (type.toString().toLowerCase().endsWith("egg")) {
                Block block = player.getTargetBlockExact(5, FluidCollisionMode.SOURCE_ONLY);
                if (block != null && block.getType() != Material.AIR) {
                    Location location = BukkitUtil.getLocation(block.getLocation());
                    if (!EventUtil.manager
                        .checkPlayerBlockEvent(pp, PlayerBlockEventType.SPAWN_MOB, location, null, true)) {
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
        BlockType blocktype1;
        Block block = event.getClickedBlock();
        Location location = BukkitUtil.getLocation(block.getLocation());
        Action action = event.getAction();
        outer: switch (action) {
            case PHYSICAL: {
                eventType = PlayerBlockEventType.TRIGGER_PHYSICAL;
                blocktype1 = BukkitAdapter.asBlockType(block.getType());
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
                        break outer;
                    }
                } else {
                    if (type.toString().toLowerCase().endsWith("egg")) {
                        eventType = PlayerBlockEventType.SPAWN_MOB;
                        break outer;
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
            if (event.getMaterial() == Material.getMaterial(PlotSquared.get().worldedit.getConfiguration().wandItem)) {
                return;
            }
        }
        if (!EventUtil.manager.checkPlayerBlockEvent(pp, eventType, location, blocktype1, true)) {
            event.setCancelled(true);
            event.setUseInteractedBlock(Event.Result.DENY);
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
        //TODO needs an overhaul for the increased number of spawn reasons added to this event.
        //I can't believe they waited so damn long to expand this API set.
        switch (reason) {
            case DISPENSE_EGG:
            case EGG:
            case OCELOT_BABY:
            case SPAWNER_EGG:
                if (!area.SPAWN_EGGS) {
                    event.setCancelled(true);
                    return;
                }
                break;
            case REINFORCEMENTS:
            case NATURAL:
            case CHUNK_GEN:
                if (!area.MOB_SPAWNING) {
                    event.setCancelled(true);
                    return;
                }
            case BREEDING:
                if (!area.SPAWN_BREEDING) {
                    event.setCancelled(true);
                    return;
                }
                break;
            case BUILD_IRONGOLEM:
            case BUILD_SNOWMAN:
            case BUILD_WITHER:
            case CUSTOM:
                if (!area.SPAWN_CUSTOM && entity.getType() != EntityType.ARMOR_STAND) {
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
        if (!PlotSquared.get().hasPlotArea(worldName)) {
            return;
        }
        Location location = BukkitUtil.getLocation(block.getLocation());
        PlotArea area = location.getPlotArea();
        if (area == null) {
            return;
        }
        Plot plot = area.getOwnedPlotAbs(location);
        if (plot == null || plot.getFlag(DisablePhysicsFlag.class)) {
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
                .setMetadata("plot", new FixedMetadataValue((Plugin) PlotSquared.get().IMP, plot));
        }
    }

    @EventHandler public void onPrime(ExplosionPrimeEvent event) {
        this.lastRadius = event.getRadius() + 1;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBurn(BlockBurnEvent event) {
        Block block = event.getBlock();
        Location location = BukkitUtil.getLocation(block.getLocation());

        PlotArea area = location.getPlotArea();
        if (area == null) {
            return;
        }

        Plot plot = location.getOwnedPlot();
        if (plot == null || !plot.getFlag(BlockBurnFlag.class)) {
            event.setCancelled(true);
        }

    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockIgnite(BlockIgniteEvent event) {
        Player player = event.getPlayer();
        Entity ignitingEntity = event.getIgnitingEntity();
        Block block = event.getBlock();
        BlockIgniteEvent.IgniteCause igniteCause = event.getCause();
        Location location1 = BukkitUtil.getLocation(block.getLocation());
        PlotArea area = location1.getPlotArea();
        if (area == null) {
            return;
        }
        if (igniteCause == BlockIgniteEvent.IgniteCause.LIGHTNING) {
            event.setCancelled(true);
            return;
        }

        Plot plot = area.getOwnedPlotAbs(location1);
        if (player != null) {
            PlotPlayer pp = BukkitUtil.getPlayer(player);
            if (plot == null) {
                if (!Permissions.hasPermission(pp, Captions.PERMISSION_ADMIN_BUILD_ROAD)) {
                    MainUtil.sendMessage(pp, Captions.NO_PERMISSION_EVENT,
                        Captions.PERMISSION_ADMIN_BUILD_ROAD);
                    event.setCancelled(true);
                }
            } else if (!plot.hasOwner()) {
                if (!Permissions.hasPermission(pp, Captions.PERMISSION_ADMIN_BUILD_UNOWNED)) {
                    MainUtil.sendMessage(pp, Captions.NO_PERMISSION_EVENT,
                        Captions.PERMISSION_ADMIN_BUILD_UNOWNED);
                    event.setCancelled(true);
                }
            } else if (!plot.isAdded(pp.getUUID())) {
                if (!Permissions.hasPermission(pp, Captions.PERMISSION_ADMIN_BUILD_OTHER)) {
                    MainUtil.sendMessage(pp, Captions.NO_PERMISSION_EVENT,
                        Captions.PERMISSION_ADMIN_BUILD_OTHER);
                    event.setCancelled(true);
                }
            } else if (!plot.getFlag(BlockIgnitionFlag.class)) {
                event.setCancelled(true);
            }
        } else {
            if (plot == null) {
                event.setCancelled(true);
                return;
            }
            if (ignitingEntity != null) {
                if (!plot.getFlag(BlockIgnitionFlag.class)) {
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
                    !plot.getFlag(BlockIgnitionFlag.class) || plotIgnited == null
                        || !plotIgnited.equals(plot)) ||
                    (igniteCause == BlockIgniteEvent.IgniteCause.SPREAD
                        || igniteCause == BlockIgniteEvent.IgniteCause.LAVA) && (
                        !plot.getFlag(BlockIgnitionFlag.class) || plotIgnited == null
                            || !plotIgnited.equals(plot))) {
                    event.setCancelled(true);
                }
            }
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
        PlotPlayer pp = BukkitUtil.getPlayer(event.getPlayer());
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
        PlotPlayer pp = BukkitUtil.getPlayer(event.getPlayer());
        pp.unregister();
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
        PlotPlayer plotPlayer = BukkitUtil.getPlayer(player);
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
    public void onVehicleCreate(VehicleCreateEvent event) {
        Vehicle entity = event.getVehicle();
        Location location = BukkitUtil.getLocation(entity);
        PlotArea area = location.getPlotArea();
        if (area == null) {
            return;
        }
        Plot plot = area.getOwnedPlotAbs(location);
        if (plot == null || checkEntity(entity, plot)) {
            entity.remove();
            return;
        }
        if (Settings.Enabled_Components.KILL_ROAD_VEHICLES) {
            entity
                .setMetadata("plot", new FixedMetadataValue((Plugin) PlotSquared.get().IMP, plot));
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
        PlotPlayer pp = BukkitUtil.getPlayer(p);
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
            if (!Permissions.hasPermission(pp, Captions.PERMISSION_ADMIN_INTERACT_ROAD)) {
                MainUtil.sendMessage(pp, Captions.NO_PERMISSION_EVENT,
                    Captions.PERMISSION_ADMIN_INTERACT_ROAD);
                event.setCancelled(true);
            }
        } else if (!plot.hasOwner()) {
            if (!Permissions.hasPermission(pp, Captions.PERMISSION_ADMIN_INTERACT_UNOWNED)) {
                MainUtil.sendMessage(pp, Captions.NO_PERMISSION_EVENT,
                    Captions.PERMISSION_ADMIN_INTERACT_UNOWNED);
                event.setCancelled(true);
            }
        } else if (!plot.isAdded(pp.getUUID())) {
            Entity entity = event.getRightClicked();
            if (entity instanceof Monster && plot.getFlag(HostileInteractFlag.class)) {
                return;
            }
            if (entity instanceof Animals && plot.getFlag(AnimalInteractFlag.class)) {
                return;
            }
            if (entity instanceof Tameable && ((Tameable) entity).isTamed() && plot.getFlag(
                TamedInteractFlag.class)) {
                return;
            }
            if (entity instanceof Vehicle && plot.getFlag(VehicleUseFlag.class)) {
                return;
            }
            if (entity instanceof Player && plot.getFlag(PlayerInteractFlag.class)) {
                return;
            }
            if (entity instanceof Villager && plot.getFlag(VillagerInteractFlag.class)) {
                return;
            }
            if (entity instanceof ItemFrame && plot.getFlag(MiscInteractFlag.class)) {
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
            PlotPlayer pp = BukkitUtil.getPlayer(p);
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
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPotionSplash(PotionSplashEvent event) {
        ThrownPotion damager = event.getPotion();
        Location location = BukkitUtil.getLocation(damager);
        if (!PlotSquared.get().hasPlotArea(location.getWorld())) {
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

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityCombustByEntity(EntityCombustByEntityEvent event) {
        EntityDamageByEntityEvent eventChange =
            new EntityDamageByEntityEvent(event.getCombuster(), event.getEntity(),
                EntityDamageEvent.DamageCause.FIRE_TICK, event.getDuration());
        onEntityDamageByEntityEvent(eventChange);
        if (eventChange.isCancelled()) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent event) {
        Entity damager = event.getDamager();
        Location location = BukkitUtil.getLocation(damager);
        if (!PlotSquared.get().hasPlotArea(location.getWorld())) {
            return;
        }
        Entity victim = event.getEntity();
/*
        if (victim.getType().equals(EntityType.ITEM_FRAME)) {
            Plot plot = BukkitUtil.getLocation(victim).getPlot();
            if (plot != null && !plot.isAdded(damager.getUniqueId())) {
                event.setCancelled(true);
                return;
            }
        }
*/
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

    private boolean entityDamage(Entity damager, Entity victim) {
        return entityDamage(damager, victim, null);
    }

    private boolean entityDamage(Entity damager, Entity victim,
        EntityDamageEvent.DamageCause cause) {
        Location dloc = BukkitUtil.getLocation(damager);
        Location vloc = BukkitUtil.getLocation(victim);
        PlotArea dArea = dloc.getPlotArea();
        PlotArea vArea;
        if (dArea != null && dArea.contains(vloc.getX(), vloc.getZ())) {
            vArea = dArea;
        } else {
            vArea = vloc.getPlotArea();
        }
        if (dArea == null && vArea == null) {
            return true;
        }

        Plot dplot;
        if (dArea != null) {
            dplot = dArea.getPlot(dloc);
        } else {
            dplot = null;
        }
        Plot vplot;
        if (vArea != null) {
            vplot = vArea.getPlot(vloc);
        } else {
            vplot = null;
        }

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
            if (victim.getTicksLived() > damager.getTicksLived()) {
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
        } else if (damager instanceof Projectile) {
            Projectile projectile = (Projectile) damager;
            ProjectileSource shooter = projectile.getShooter();
            if (shooter instanceof Player) { // shooter is player
                player = (Player) shooter;
            } else { // shooter is not player
                if (shooter instanceof BlockProjectileSource) {
                    Location sLoc = BukkitUtil
                        .getLocation(((BlockProjectileSource) shooter).getBlock().getLocation());
                    dplot = dArea.getPlot(sLoc);
                }
                player = null;
            }
        } else { // Attacker is not player
            player = null;
        }
        if (player != null) {
            PlotPlayer plotPlayer = BukkitUtil.getPlayer(player);
            if (victim instanceof Hanging) { // hanging
                if (plot != null && (plot.getFlag(HangingBreakFlag.class)) || plot
                    .isAdded(plotPlayer.getUUID())) {
                    if (Settings.Done.RESTRICT_BUILDING && DoneFlag.isDone(plot)) {
                        if (!Permissions.hasPermission(plotPlayer, Captions.PERMISSION_ADMIN_BUILD_OTHER)) {
                            MainUtil.sendMessage(plotPlayer, Captions.NO_PERMISSION_EVENT, Captions.PERMISSION_ADMIN_BUILD_OTHER);
                            return false;
                        }
                    }
                    return true;
                }
                if (!Permissions.hasPermission(plotPlayer, "plots.admin.destroy." + stub)) {
                    MainUtil.sendMessage(plotPlayer, Captions.NO_PERMISSION_EVENT,
                        "plots.admin.destroy." + stub);
                    return false;
                }
            } else if (victim.getType() == EntityType.ARMOR_STAND) {
                if (plot != null && (plot.getFlag(MiscBreakFlag.class) || plot
                    .isAdded(plotPlayer.getUUID()))) {
                    return true;
                }
                if (!Permissions.hasPermission(plotPlayer, "plots.admin.destroy." + stub)) {
                    MainUtil.sendMessage(plotPlayer, Captions.NO_PERMISSION_EVENT,
                        "plots.admin.destroy." + stub);
                    return false;
                }
            } else if (victim instanceof Monster
                || victim instanceof EnderDragon) { // victim is monster
                if (plot != null && (plot.getFlag(HostileAttackFlag.class) || plot.getFlag(PveFlag.class) ||
                    plot.isAdded(plotPlayer.getUUID()))) {
                    return true;
                }
                if (!Permissions.hasPermission(plotPlayer, "plots.admin.pve." + stub)) {
                    MainUtil.sendMessage(plotPlayer, Captions.NO_PERMISSION_EVENT,
                        "plots.admin.pve." + stub);
                    return false;
                }
            } else if (victim instanceof Tameable) { // victim is tameable
                if (plot != null && (plot.getFlag(TamedAttackFlag.class) || plot.getFlag(PveFlag.class) ||
                    plot.isAdded(plotPlayer.getUUID()))) {
                    return true;
                }
                if (!Permissions.hasPermission(plotPlayer, "plots.admin.pve." + stub)) {
                    MainUtil.sendMessage(plotPlayer, Captions.NO_PERMISSION_EVENT,
                        "plots.admin.pve." + stub);
                    return false;
                }
            } else if (victim instanceof Player) {
                if (plot != null) {
                    if (!plot.getFlag(PvpFlag.class) && !Permissions
                        .hasPermission(plotPlayer, "plots.admin.pvp." + stub)) {
                        MainUtil.sendMessage(plotPlayer, Captions.NO_PERMISSION_EVENT,
                            "plots.admin.pvp." + stub);
                        return false;
                    } else {
                        return true;
                    }
                }
                if (!Permissions.hasPermission(plotPlayer, "plots.admin.pvp." + stub)) {
                    MainUtil.sendMessage(plotPlayer, Captions.NO_PERMISSION_EVENT,
                        "plots.admin.pvp." + stub);
                    return false;
                }
            } else if (victim instanceof Creature) { // victim is animal
                if (plot != null && (plot.getFlag(AnimalAttackFlag.class) || plot.getFlag(PveFlag.class)
                    || plot.isAdded(plotPlayer.getUUID()))) {
                    return true;
                }
                if (!Permissions.hasPermission(plotPlayer, "plots.admin.pve." + stub)) {
                    MainUtil.sendMessage(plotPlayer, Captions.NO_PERMISSION_EVENT,
                        "plots.admin.pve." + stub);
                    return false;
                }
            } else if (victim instanceof Vehicle) { // Vehicles are managed in vehicle destroy event
                return true;
            } else { // victim is something else
                if (plot != null && (plot.getFlag(PveFlag.class) || plot
                    .isAdded(plotPlayer.getUUID()))) {
                    return true;
                }
                if (!Permissions.hasPermission(plotPlayer, "plots.admin.pve." + stub)) {
                    MainUtil.sendMessage(plotPlayer, Captions.NO_PERMISSION_EVENT,
                        "plots.admin.pve." + stub);
                    return false;
                }
            }
            return true;
        } else if (dplot != null && (!dplot.equals(vplot) || Objects
            .equals(dplot.guessOwner(), vplot.guessOwner()))) {
            return vplot != null && vplot.getFlag(PveFlag.class);
        }
        //disable the firework damage. too much of a headache to support at the moment.
        if (vplot != null) {
            if (EntityDamageEvent.DamageCause.ENTITY_EXPLOSION == cause
                && damager.getType() == EntityType.FIREWORK) {
                return false;
            }
        }
        return ((vplot != null && vplot.getFlag(PveFlag.class)) || !(damager instanceof Arrow
            && !(victim instanceof Creature)));
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerEggThrow(PlayerEggThrowEvent event) {
        Location location = BukkitUtil.getLocation(event.getEgg().getLocation());
        PlotArea area = location.getPlotArea();
        if (area == null) {
            return;
        }
        Player player = event.getPlayer();
        PlotPlayer plotPlayer = BukkitUtil.getPlayer(player);
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
                && !Permissions.hasPermission(pp, Captions.PERMISSION_ADMIN_BUILD_HEIGHT_LIMIT)) {
                event.setCancelled(true);
                MainUtil.sendMessage(pp, Captions.HEIGHT_LIMIT.getTranslated()
                    .replace("{limit}", String.valueOf(area.MAX_BUILD_HEIGHT)));
            }
            if (!plot.hasOwner()) {
                if (!Permissions.hasPermission(pp, Captions.PERMISSION_ADMIN_BUILD_UNOWNED)) {
                    MainUtil.sendMessage(pp, Captions.NO_PERMISSION_EVENT,
                        Captions.PERMISSION_ADMIN_BUILD_UNOWNED);
                    event.setCancelled(true);
                    return;
                }
            } else if (!plot.isAdded(pp.getUUID())) {
                List<BlockTypeWrapper> place = plot.getFlag(PlaceFlag.class);
                if (place != null) {
                Block block = event.getBlock();
                    if (place.contains(BlockTypeWrapper.get(BukkitAdapter.asBlockType(block.getType())))) {
                    return;
                    }
                }
                if (!Permissions.hasPermission(pp, Captions.PERMISSION_ADMIN_BUILD_OTHER)) {
                    MainUtil.sendMessage(pp, Captions.NO_PERMISSION_EVENT,
                        Captions.PERMISSION_ADMIN_BUILD_OTHER);
                    event.setCancelled(true);
                    return;
                }
            } else if (Settings.Done.RESTRICT_BUILDING && DoneFlag.isDone(plot)) {
                if (!Permissions.hasPermission(pp, Captions.PERMISSION_ADMIN_BUILD_OTHER)) {
                    MainUtil.sendMessage(pp, Captions.NO_PERMISSION_EVENT,
                        Captions.PERMISSION_ADMIN_BUILD_OTHER);
                    event.setCancelled(true);
                    return;
                }
            }
            if (plot.getFlag(DisablePhysicsFlag.class)) {
                Block block = event.getBlockPlaced();
                if (block.getType().hasGravity()) {
                    sendBlockChange(block.getLocation(), block.getBlockData());
                }
            }
        } else if (!Permissions.hasPermission(pp, Captions.PERMISSION_ADMIN_BUILD_ROAD)) {
            MainUtil.sendMessage(pp, Captions.NO_PERMISSION_EVENT,
                Captions.PERMISSION_ADMIN_BUILD_ROAD);
        event.setCancelled(true);
    }
}
}
