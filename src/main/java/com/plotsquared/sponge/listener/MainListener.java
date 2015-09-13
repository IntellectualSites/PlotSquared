package com.plotsquared.sponge.listener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.living.Ambient;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.entity.living.animal.Animal;
import org.spongepowered.api.entity.living.monster.Monster;
import org.spongepowered.api.entity.player.Player;
import org.spongepowered.api.entity.vehicle.Boat;
import org.spongepowered.api.entity.vehicle.minecart.Minecart;
import org.spongepowered.api.event.Subscribe;
import org.spongepowered.api.event.block.BlockMoveEvent;
import org.spongepowered.api.event.block.BlockRedstoneUpdateEvent;
import org.spongepowered.api.event.block.FloraGrowEvent;
import org.spongepowered.api.event.block.FluidSpreadEvent;
import org.spongepowered.api.event.entity.EntityChangeBlockEvent;
import org.spongepowered.api.event.entity.EntitySpawnEvent;
import org.spongepowered.api.event.entity.EntityTeleportEvent;
import org.spongepowered.api.event.entity.player.PlayerBreakBlockEvent;
import org.spongepowered.api.event.entity.player.PlayerChangeWorldEvent;
import org.spongepowered.api.event.entity.player.PlayerChatEvent;
import org.spongepowered.api.event.entity.player.PlayerInteractBlockEvent;
import org.spongepowered.api.event.entity.player.PlayerJoinEvent;
import org.spongepowered.api.event.entity.player.PlayerMoveEvent;
import org.spongepowered.api.event.entity.player.PlayerPlaceBlockEvent;
import org.spongepowered.api.event.entity.player.PlayerQuitEvent;
import org.spongepowered.api.event.message.CommandEvent;
import org.spongepowered.api.event.network.PlayerConnectionEvent;
import org.spongepowered.api.event.world.ChunkPreGenerateEvent;
import org.spongepowered.api.event.world.WorldOnExplosionEvent;
import org.spongepowered.api.network.PlayerConnection;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.Texts;
import org.spongepowered.api.util.command.CommandSource;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.explosion.Explosion;
import org.spongepowered.api.world.extent.Extent;

import com.flowpowered.math.vector.Vector3d;
import com.flowpowered.math.vector.Vector3i;
import com.google.common.base.Predicate;
import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.config.Settings;
import com.intellectualcrafters.plot.database.DBFunc;
import com.intellectualcrafters.plot.flag.Flag;
import com.intellectualcrafters.plot.flag.FlagManager;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotBlock;
import com.intellectualcrafters.plot.object.PlotId;
import com.intellectualcrafters.plot.object.PlotManager;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.object.PlotWorld;
import com.intellectualcrafters.plot.object.StringWrapper;
import com.intellectualcrafters.plot.util.ChunkManager;
import com.intellectualcrafters.plot.util.EventUtil;
import com.intellectualcrafters.plot.util.ExpireManager;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.Permissions;
import com.intellectualcrafters.plot.util.StringMan;
import com.intellectualcrafters.plot.util.TaskManager;
import com.intellectualcrafters.plot.util.UUIDHandler;
import com.plotsquared.listener.PlotListener;
import com.plotsquared.sponge.SpongeMain;
import com.plotsquared.sponge.object.SpongePlayer;
import com.plotsquared.sponge.util.SpongeUtil;

public class MainListener {
    
    /*
     * TODO:
     *  - Anything marked with a TODO below
     *  - BlockSpreadEvent
     *  - BlockPhysicsEvent
     *  - BlockFormEvent
     *  - BlockFadeEvent
     *  - BlockFromToEvent
     *  - BlockDamageEvent
     *  - Structure (tree etc)
     *  - PlayerIgniteBlockEvent
     *  - PlayerBucketEmptyEvent
     *  - PlayerBucketFillEvent
     *  - VehicleCreateEvent
     *  - HangingPlaceEvent
     *  - HangingBreakEvent
     *  - Liquid flow
     *  - PVP
     *  - PVE
     *  - VehicleDestroy
     *  - Projectile
     */
    
    @Subscribe
    public void onFluidSpread(final FluidSpreadEvent event) {
        // TODO This event isn't called
        final Location loc = SpongeUtil.getLocation(event.getLocation());
        final Plot plot = MainUtil.getPlot(loc);
        if (plot == null) {
            if (MainUtil.isPlotAreaAbs(loc)) {
                event.setCancelled(true);
            }
            return;
        }
        event.filterLocations(new Predicate<org.spongepowered.api.world.Location<World>>() {
            @Override
            public boolean apply(final org.spongepowered.api.world.Location<World> loc) {
                if (!plot.equals(MainUtil.getPlot(SpongeUtil.getLocation(loc)))) {
                    return false;
                }
                return true;
            }
        });
    }
    
    @Subscribe
    public void onMobSpawn(final EntitySpawnEvent event) {
        final Entity entity = event.getEntity();
        if (entity instanceof Player) {
            return;
        }
        final Location loc = SpongeUtil.getLocation(event.getLocation());
        final String world = loc.getWorld();
        final PlotWorld plotworld = PS.get().getPlotWorld(world);
        if (plotworld == null) {
            return;
        }
        final Plot plot = MainUtil.getPlot(loc);
        if (plot == null) {
            if (MainUtil.isPlotRoad(loc)) {
                event.setCancelled(true);
            }
            return;
        }
        PS.get().getPlotWorld(world);
        
        // TODO selectively cancel depending on spawn reason
        // - Not sure if possible to get spawn reason (since there are no callbacks)
        
        if (entity.getType() == EntityTypes.DROPPED_ITEM) {
            if (FlagManager.isPlotFlagFalse(plot, "item-drop")) {
                event.setCancelled(true);
            }
            return;
        }
        int[] mobs = null;
        if (entity instanceof Living) {
            if (!plotworld.MOB_SPAWNING) {
                event.setCancelled(true);
                return;
            }
            final Flag mobCap = FlagManager.getPlotFlag(plot, "mob-cap");
            if (mobCap != null) {
                final Integer cap = (Integer) mobCap.getValue();
                if (cap == 0) {
                    event.setCancelled(true);
                    return;
                }
                if (mobs == null) {
                    mobs = ChunkManager.manager.countEntities(plot);
                }
                if (mobs[3] >= cap) {
                    event.setCancelled(true);
                    return;
                }
            }
            if ((entity instanceof Ambient) || (entity instanceof Animal)) {
                final Flag animalFlag = FlagManager.getPlotFlag(plot, "animal-cap");
                if (animalFlag != null) {
                    final int cap = ((Integer) animalFlag.getValue());
                    if (cap == 0) {
                        event.setCancelled(true);
                        return;
                    }
                    if (mobs == null) {
                        mobs = ChunkManager.manager.countEntities(plot);
                    }
                    if (mobs[1] >= cap) {
                        event.setCancelled(true);
                        return;
                    }
                }
            }
            if (entity instanceof Monster) {
                final Flag monsterFlag = FlagManager.getPlotFlag(plot, "hostile-cap");
                if (monsterFlag != null) {
                    final int cap = ((Integer) monsterFlag.getValue());
                    if (cap == 0) {
                        event.setCancelled(true);
                        return;
                    }
                    if (mobs == null) {
                        mobs = ChunkManager.manager.countEntities(plot);
                    }
                    if (mobs[2] >= cap) {
                        event.setCancelled(true);
                        return;
                    }
                }
            }
            return;
        }
        if ((entity instanceof Minecart) || (entity instanceof Boat)) {
            final Flag vehicleFlag = FlagManager.getPlotFlag(plot, "vehicle-cap");
            if (vehicleFlag != null) {
                final int cap = ((Integer) vehicleFlag.getValue());
                if (cap == 0) {
                    event.setCancelled(true);
                    return;
                }
                if (mobs == null) {
                    mobs = ChunkManager.manager.countEntities(plot);
                }
                if (mobs[4] >= cap) {
                    event.setCancelled(true);
                    return;
                }
            }
        }
        final Flag entityCap = FlagManager.getPlotFlag(plot, "entity-cap");
        if (entityCap != null) {
            final Integer cap = (Integer) entityCap.getValue();
            if (cap == 0) {
                event.setCancelled(true);
                return;
            }
            if (mobs == null) {
                mobs = ChunkManager.manager.countEntities(plot);
            }
            if (mobs[0] >= cap) {
                event.setCancelled(true);
                return;
            }
        }
        
    }
    
    @Subscribe
    public void onBlockChange(final EntityChangeBlockEvent event) {
        final Entity entity = event.getEntity();
        if (entity.getType() == EntityTypes.PLAYER) {
            return;
        }
        if (PS.get().isPlotWorld(entity.getWorld().getName())) {
            event.setCancelled(true);
        }
    }
    
    @Subscribe
    public void onCommand(final CommandEvent event) {
        switch (event.getCommand().toLowerCase()) {
            case "plotme": {
                final CommandSource source = event.getSource();
                if (Settings.USE_PLOTME_ALIAS) {
                    SpongeMain.THIS.getGame().getCommandDispatcher().process(source, ("plots " + event.getArguments()).trim());
                } else {
                    source.sendMessage(SpongeMain.THIS.getText(C.NOT_USING_PLOTME.s()));
                }
                event.setCancelled(true);
            }
        }
        // TODO
    }
    
    @Subscribe
    public void onBlockMove(final BlockMoveEvent event) {
        final org.spongepowered.api.world.Location block = event.getLocations().get(0);
        final Extent extent = block.getExtent();
        if (extent instanceof World) {
            final World world = (World) extent;
            final String worldname = world.getName();
            if (!PS.get().isPlotWorld(worldname)) {
                return;
            }
            event.filterLocations(new Predicate<org.spongepowered.api.world.Location<World>>() {
                @Override
                public boolean apply(final org.spongepowered.api.world.Location loc) {
                    if (MainUtil.isPlotRoad(SpongeUtil.getLocation(worldname, loc))) {
                        return false;
                    }
                    return true;
                }
            });
        }
    }
    
    @Subscribe
    public void onFloraGrow(final FloraGrowEvent event) {
        final org.spongepowered.api.world.Location block = event.getLocation();
        final Extent extent = block.getExtent();
        if (extent instanceof World) {
            final World world = (World) extent;
            final String worldname = world.getName();
            if (!PS.get().isPlotWorld(worldname)) {
                return;
            }
            if (MainUtil.isPlotRoad(SpongeUtil.getLocation(worldname, block))) {
                event.setCancelled(true);
            }
        }
    }
    
    @Subscribe
    public void onChat(final PlayerChatEvent event) {
        final Player player = event.getEntity();
        final String world = player.getWorld().getName();
        if (!PS.get().isPlotWorld(world)) {
            return;
        }
        final PlotWorld plotworld = PS.get().getPlotWorld(world);
        final PlotPlayer plr = SpongeUtil.getPlayer(player);
        if (!plotworld.PLOT_CHAT && ((plr.getMeta("chat") == null) || !(Boolean) plr.getMeta("chat"))) {
            return;
        }
        final Location loc = SpongeUtil.getLocation(player);
        final Plot plot = MainUtil.getPlot(loc);
        if (plot == null) {
            return;
        }
        final Text message = event.getUnformattedMessage();
        
        // TODO use display name rather than username
        //  - Getting displayname currently causes NPE, so wait until sponge fixes that
        
        final String sender = player.getName();
        final PlotId id = plot.id;
        final String newMessage = StringMan.replaceAll(C.PLOT_CHAT_FORMAT.s(), "%plot_id%", id.x + ";" + id.y, "%sender%", sender);
        final Text forcedMessage = event.getMessage();
        //        String forcedMessage = StringMan.replaceAll(C.PLOT_CHAT_FORCED.s(), "%plot_id%", id.x + ";" + id.y, "%sender%", sender);
        for (final PlotPlayer user : UUIDHandler.getPlayers().values()) {
            String toSend;
            if (plot.equals(MainUtil.getPlot(user.getLocation()))) {
                toSend = newMessage;
            } else if (Permissions.hasPermission(user, C.PERMISSION_COMMANDS_CHAT)) {
                ((SpongePlayer) user).player.sendMessage(forcedMessage);
                continue;
            } else {
                continue;
            }
            final String[] split = (toSend + " ").split("%msg%");
            final List<Text> components = new ArrayList<>();
            Text prefix = null;
            for (final String part : split) {
                if (prefix != null) {
                    components.add(prefix);
                } else {
                    prefix = message;
                }
                components.add(Texts.of(part));
            }
            ((SpongePlayer) user).player.sendMessage(Texts.join(components));
        }
        event.setNewMessage(Texts.of());
        event.setCancelled(true);
    }
    
    @Subscribe
    public void onBigBoom(final WorldOnExplosionEvent event) {
        final World worldObj = event.getWorld();
        final String world = worldObj.getName();
        if (!PS.get().isPlotWorld(world)) {
            return;
        }
        final Explosion explosion = event.getExplosion();
        final Vector3d origin = explosion.getOrigin();
        final Location loc = new Location(world, origin.getFloorX(), origin.getFloorY(), origin.getFloorZ());
        final Plot plot = MainUtil.getPlot(loc);
        if ((plot != null) && plot.hasOwner()) {
            if (FlagManager.isPlotFlagTrue(plot, "explosion")) {
                event.filterLocations(new Predicate<org.spongepowered.api.world.Location<World>>() {
                    @Override
                    public boolean apply(final org.spongepowered.api.world.Location loc) {
                        if (!plot.equals(MainUtil.getPlot(SpongeUtil.getLocation(loc)))) {
                            return false;
                        }
                        return true;
                    }
                });
                event.filterEntities(new Predicate<Entity>() {
                    @Override
                    public boolean apply(final Entity entity) {
                        if (!plot.equals(MainUtil.getPlot(SpongeUtil.getLocation(entity)))) {
                            return false;
                        }
                        return true;
                    }
                });
                return;
            }
        }
        if (MainUtil.isPlotArea(loc)) {
            explosion.shouldBreakBlocks(false);
            explosion.canCauseFire(false);
            explosion.setRadius(0);
            event.filterEntities(new Predicate<Entity>() {
                @Override
                public boolean apply(final Entity entity) {
                    if (!plot.equals(MainUtil.getPlot(SpongeUtil.getLocation(entity)))) {
                        return false;
                    }
                    return true;
                }
            });
            return;
        } else {
            if (FlagManager.isPlotFlagTrue(plot, "explosion")) {
                event.filterLocations(new Predicate<org.spongepowered.api.world.Location<World>>() {
                    @Override
                    public boolean apply(final org.spongepowered.api.world.Location loc) {
                        if (!plot.equals(MainUtil.getPlot(SpongeUtil.getLocation(loc)))) {
                            return false;
                        }
                        return true;
                    }
                });
                event.filterEntities(new Predicate<Entity>() {
                    @Override
                    public boolean apply(final Entity entity) {
                        if (!plot.equals(MainUtil.getPlot(SpongeUtil.getLocation(entity)))) {
                            return false;
                        }
                        return true;
                    }
                });
                return;
            }
        }
    }
    
    @Subscribe
    public void onChunkPreGenerator(final ChunkPreGenerateEvent event) {
        final org.spongepowered.api.world.Chunk chunk = event.getChunk();
        final World world = chunk.getWorld();
        final String worldname = world.getName();
        if (MainUtil.worldBorder.containsKey(worldname)) {
            final int border = MainUtil.getBorder(worldname);
            final Vector3i min = world.getBlockMin();
            final int x = Math.abs(min.getX());
            final int z = Math.abs(min.getZ());
            if ((x > border) || (z > border)) {
                // TODO cancel this chunk from loading
                // - Currently not possible / this event doesn't seem to be called
            }
        }
    }
    
    @Subscribe
    public void onRedstoneEvent(final BlockRedstoneUpdateEvent event) {
        final org.spongepowered.api.world.Location block = event.getLocation();
        final Location loc = SpongeUtil.getLocation(block);
        if ((loc == null) || !PS.get().isPlotWorld(loc.getWorld())) {
            return;
        }
        final Plot plot = MainUtil.getPlot(loc);
        if ((plot == null) || !plot.hasOwner()) {
            return;
        }
        if (event.getOldSignalStrength() > event.getNewSignalStrength()) {
            return;
        }
        if (Settings.REDSTONE_DISABLER) {
            if (UUIDHandler.getPlayer(plot.owner) == null) {
                boolean disable = true;
                for (final UUID trusted : plot.getTrusted()) {
                    if (UUIDHandler.getPlayer(trusted) != null) {
                        disable = false;
                        break;
                    }
                }
                if (disable) {
                    event.setNewSignalStrength(0);
                    return;
                }
            }
        }
        FlagManager.getPlotFlag(plot, "redstone");
        if (FlagManager.isPlotFlagFalse(plot, "redstone")) {
            event.setNewSignalStrength(0);
            // TODO only disable clocks
        }
    }
    
    @Subscribe
    public void onBlockBreak(final PlayerBreakBlockEvent event) {
        final Player player = event.getEntity();
        final World world = player.getWorld();
        final String worldname = world.getName();
        final org.spongepowered.api.world.Location blockLoc = event.getLocation();
        final Location loc = SpongeUtil.getLocation(worldname, blockLoc);
        final Plot plot = MainUtil.getPlot(loc);
        if (plot != null) {
            if (blockLoc.getY() == 0) {
                event.setCancelled(true);
                return;
            }
            final PlotPlayer pp = SpongeUtil.getPlayer(player);
            if (!plot.hasOwner()) {
                if (Permissions.hasPermission(pp, C.PERMISSION_ADMIN_DESTROY_UNOWNED)) {
                    return;
                }
                MainUtil.sendMessage(pp, C.NO_PERMISSION_EVENT, C.PERMISSION_ADMIN_DESTROY_UNOWNED);
                event.setCancelled(true);
                return;
            } else if (!plot.isAdded(pp.getUUID())) {
                final Flag destroy = FlagManager.getPlotFlag(plot, "break");
                final BlockState state = blockLoc.getBlock();
                if ((destroy != null) && ((HashSet<PlotBlock>) destroy.getValue()).contains(SpongeMain.THIS.getPlotBlock(state))) {
                    return;
                }
                if (Permissions.hasPermission(pp, C.PERMISSION_ADMIN_DESTROY_OTHER)) {
                    return;
                }
                MainUtil.sendMessage(pp, C.NO_PERMISSION_EVENT, C.PERMISSION_ADMIN_DESTROY_OTHER);
                event.setCancelled(true);
            } else if (Settings.DONE_RESTRICTS_BUILDING && plot.getSettings().flags.containsKey("done")) {
                if (!Permissions.hasPermission(pp, C.PERMISSION_ADMIN_BUILD_OTHER)) {
                    MainUtil.sendMessage(pp, C.NO_PERMISSION_EVENT, C.PERMISSION_ADMIN_BUILD_OTHER);
                    event.setCancelled(true);
                    return;
                }
            }
            return;
        }
        final PlotPlayer pp = SpongeUtil.getPlayer(player);
        if (Permissions.hasPermission(pp, C.PERMISSION_ADMIN_DESTROY_ROAD)) {
            return;
        }
        if (MainUtil.isPlotArea(loc)) {
            MainUtil.sendMessage(pp, C.NO_PERMISSION_EVENT, C.PERMISSION_ADMIN_DESTROY_ROAD);
            event.setCancelled(true);
        }
    }
    
    @Subscribe
    public void onBlockPlace(final PlayerPlaceBlockEvent event) {
        final Player player = event.getEntity();
        final World world = player.getWorld();
        final String worldname = world.getName();
        final org.spongepowered.api.world.Location blockLoc = event.getLocation();
        final Location loc = SpongeUtil.getLocation(worldname, blockLoc);
        final Plot plot = MainUtil.getPlot(loc);
        if (plot != null) {
            if (blockLoc.getY() == 0) {
                event.setCancelled(true);
                return;
            }
            final PlotPlayer pp = SpongeUtil.getPlayer(player);
            if (!plot.hasOwner()) {
                if (Permissions.hasPermission(pp, C.PERMISSION_ADMIN_BUILD_UNOWNED)) {
                    return;
                }
                MainUtil.sendMessage(pp, C.NO_PERMISSION_EVENT, C.PERMISSION_ADMIN_BUILD_UNOWNED);
                event.setCancelled(true);
                return;
            } else if (!plot.isAdded(pp.getUUID())) {
                final Flag destroy = FlagManager.getPlotFlag(plot, "place");
                final BlockState state = blockLoc.getBlock();
                if ((destroy != null) && ((HashSet<PlotBlock>) destroy.getValue()).contains(SpongeMain.THIS.getPlotBlock(state))) {
                    return;
                }
                if (Permissions.hasPermission(pp, C.PERMISSION_ADMIN_BUILD_OTHER)) {
                    return;
                }
                MainUtil.sendMessage(pp, C.NO_PERMISSION_EVENT, C.PERMISSION_ADMIN_DESTROY_OTHER);
                event.setCancelled(true);
            } else if (Settings.DONE_RESTRICTS_BUILDING && plot.getSettings().flags.containsKey("done")) {
                if (!Permissions.hasPermission(pp, C.PERMISSION_ADMIN_BUILD_OTHER)) {
                    MainUtil.sendMessage(pp, C.NO_PERMISSION_EVENT, C.PERMISSION_ADMIN_BUILD_OTHER);
                    event.setCancelled(true);
                    return;
                }
            }
            return;
        }
        final PlotPlayer pp = SpongeUtil.getPlayer(player);
        if (Permissions.hasPermission(pp, C.PERMISSION_ADMIN_BUILD_ROAD)) {
            return;
        }
        if (MainUtil.isPlotArea(loc)) {
            MainUtil.sendMessage(pp, C.NO_PERMISSION_EVENT, C.PERMISSION_ADMIN_BUILD_ROAD);
            event.setCancelled(true);
        }
    }
    
    @Subscribe
    public void onBlockInteract(final PlayerInteractBlockEvent event) {
        final Player player = event.getEntity();
        final World world = player.getWorld();
        final String worldname = world.getName();
        final org.spongepowered.api.world.Location blockLoc = event.getLocation();
        final Location loc = SpongeUtil.getLocation(worldname, blockLoc);
        final Plot plot = MainUtil.getPlot(loc);
        if (plot != null) {
            if (blockLoc.getY() == 0) {
                event.setCancelled(true);
                return;
            }
            if (!plot.hasOwner()) {
                final PlotPlayer pp = SpongeUtil.getPlayer(player);
                if (Permissions.hasPermission(pp, C.PERMISSION_ADMIN_INTERACT_UNOWNED)) {
                    return;
                }
                MainUtil.sendMessage(pp, C.NO_PERMISSION_EVENT, C.PERMISSION_ADMIN_INTERACT_UNOWNED);
                event.setCancelled(true);
                return;
            }
            final PlotPlayer pp = SpongeUtil.getPlayer(player);
            if (!plot.isAdded(pp.getUUID())) {
                final Flag destroy = FlagManager.getPlotFlag(plot, "use");
                final BlockState state = blockLoc.getBlock();
                if ((destroy != null) && ((HashSet<PlotBlock>) destroy.getValue()).contains(SpongeMain.THIS.getPlotBlock(state))) {
                    return;
                }
                if (Permissions.hasPermission(pp, C.PERMISSION_ADMIN_INTERACT_OTHER)) {
                    return;
                }
                MainUtil.sendMessage(pp, C.NO_PERMISSION_EVENT, C.PERMISSION_ADMIN_INTERACT_OTHER);
                event.setCancelled(true);
            }
            return;
        }
        final PlotPlayer pp = SpongeUtil.getPlayer(player);
        if (Permissions.hasPermission(pp, C.PERMISSION_ADMIN_INTERACT_ROAD)) {
            return;
        }
        if (MainUtil.isPlotArea(loc)) {
            MainUtil.sendMessage(pp, C.NO_PERMISSION_EVENT, C.PERMISSION_ADMIN_INTERACT_ROAD);
            event.setCancelled(true);
        }
    }
    
    @Subscribe
    public void onConnect(final PlayerConnectionEvent event) {
        final PlayerConnection connection = event.getConnection();
        final Player player = connection.getPlayer();
        final String name = player.getName();
        final PlotPlayer pp = SpongeUtil.getPlayer(player);
        if (name.equals("PlotSquared") || pp.getUUID().equals(DBFunc.everyone)) {
            player.kick();
            SpongeUtil.removePlayer(pp.getName());
        }
    }
    
    @Subscribe
    public void onJoin(final PlayerJoinEvent event) {
        final Player player = event.getUser();
        SpongeUtil.removePlayer(player.getName());
        final PlotPlayer pp = SpongeUtil.getPlayer(player);
        final String username = pp.getName();
        final StringWrapper name = new StringWrapper(username);
        final UUID uuid = pp.getUUID();
        UUIDHandler.add(name, uuid);
        ExpireManager.dates.put(uuid, System.currentTimeMillis());
        
        // TODO worldedit bypass
        
        if ((PS.get().update != null) && pp.hasPermission("plots.admin")) {
            TaskManager.runTaskLater(new Runnable() {
                @Override
                public void run() {
                    MainUtil.sendMessage(pp, "&6An update for PlotSquared is available: &7/plot update");
                }
            }, 20);
        }
        final Location loc = SpongeUtil.getLocation(player);
        final Plot plot = MainUtil.getPlot(loc);
        if (plot == null) {
            return;
        }
        if (Settings.TELEPORT_ON_LOGIN) {
            MainUtil.teleportPlayer(pp, pp.getLocation(), plot);
            MainUtil.sendMessage(pp, C.TELEPORTED_TO_ROAD);
        }
        PlotListener.plotEntry(pp, plot);
    }
    
    @Subscribe
    public void onQuit(final PlayerQuitEvent event) {
        final Player player = event.getEntity();
        final PlotPlayer pp = SpongeUtil.getPlayer(player);
        ExpireManager.dates.put(pp.getUUID(), System.currentTimeMillis());
        EventUtil.unregisterPlayer(pp);
        
        // TODO unregister WorldEdit manager
        // TODO delete plots on ban
        
        SpongeUtil.removePlayer(pp.getName());
    }
    
    public int getInt(final double value) {
        return (int) (value < 0 ? value - 1 : value);
    }
    
    @Subscribe
    public void onMove(final PlayerMoveEvent event) {
        final org.spongepowered.api.world.Location from = event.getOldLocation();
        org.spongepowered.api.world.Location to = event.getNewLocation();
        int x2;
        if (getInt(from.getX()) != (x2 = getInt(to.getX()))) {
            final Player player = event.getUser();
            final PlotPlayer pp = SpongeUtil.getPlayer(player);
            final Extent extent = to.getExtent();
            if (!(extent instanceof World)) {
                pp.deleteMeta("location");
                return;
            }
            pp.setMeta("location", SpongeUtil.getLocation(player));
            final World world = (World) extent;
            final String worldname = ((World) extent).getName();
            final PlotWorld plotworld = PS.get().getPlotWorld(worldname);
            if (plotworld == null) {
                return;
            }
            final PlotManager plotManager = PS.get().getPlotManager(worldname);
            final PlotId id = plotManager.getPlotId(plotworld, x2, 0, getInt(to.getZ()));
            final Plot lastPlot = (Plot) pp.getMeta("lastplot");
            if (id == null) {
                if (lastPlot == null) {
                    return;
                }
                if (!PlotListener.plotExit(pp, lastPlot)) {
                    MainUtil.sendMessage(pp, C.NO_PERMISSION_EVENT, C.PERMISSION_ADMIN_EXIT_DENIED);
                    if (lastPlot.equals(MainUtil.getPlot(SpongeUtil.getLocation(worldname, from)))) {
                        event.setNewLocation(from);
                    } else {
                        event.setNewLocation(world.getSpawnLocation());
                    }
                    return;
                }
            } else if ((lastPlot != null) && id.equals(lastPlot.id)) {
                return;
            } else {
                final Plot plot = MainUtil.getPlot(worldname, id);
                if (!PlotListener.plotEntry(pp, plot)) {
                    MainUtil.sendMessage(pp, C.NO_PERMISSION_EVENT, C.PERMISSION_ADMIN_ENTRY_DENIED);
                    if (!plot.equals(MainUtil.getPlot(SpongeUtil.getLocation(worldname, from)))) {
                        event.setNewLocation(from);
                    } else {
                        event.setNewLocation(world.getSpawnLocation());
                    }
                    return;
                }
            }
            final Integer border = MainUtil.worldBorder.get(worldname);
            if (border != null) {
                if (x2 > border) {
                    final Vector3d pos = to.getPosition();
                    to = to.setPosition(new Vector3d(border - 4, pos.getY(), pos.getZ()));
                    event.setNewLocation(to);
                    MainUtil.sendMessage(pp, C.BORDER);
                } else if (x2 < -border) {
                    final Vector3d pos = to.getPosition();
                    to = to.setPosition(new Vector3d(-border + 4, pos.getY(), pos.getZ()));
                    event.setNewLocation(to);
                    MainUtil.sendMessage(pp, C.BORDER);
                }
            }
            return;
        }
        int z2;
        if (getInt(from.getZ()) != (z2 = getInt(to.getZ()))) {
            final Player player = event.getUser();
            final PlotPlayer pp = SpongeUtil.getPlayer(player);
            final Extent extent = to.getExtent();
            if (!(extent instanceof World)) {
                pp.deleteMeta("location");
                return;
            }
            pp.setMeta("location", SpongeUtil.getLocation(player));
            final World world = (World) extent;
            final String worldname = ((World) extent).getName();
            final PlotWorld plotworld = PS.get().getPlotWorld(worldname);
            if (plotworld == null) {
                return;
            }
            final PlotManager plotManager = PS.get().getPlotManager(worldname);
            final PlotId id = plotManager.getPlotId(plotworld, x2, 0, z2);
            final Plot lastPlot = (Plot) pp.getMeta("lastplot");
            if (id == null) {
                if (lastPlot == null) {
                    return;
                }
                if (!PlotListener.plotExit(pp, lastPlot)) {
                    MainUtil.sendMessage(pp, C.NO_PERMISSION_EVENT, C.PERMISSION_ADMIN_EXIT_DENIED);
                    if (lastPlot.equals(MainUtil.getPlot(SpongeUtil.getLocation(worldname, from)))) {
                        event.setNewLocation(from);
                    } else {
                        event.setNewLocation(world.getSpawnLocation());
                    }
                    return;
                }
            } else if ((lastPlot != null) && id.equals(lastPlot.id)) {
                return;
            } else {
                final Plot plot = MainUtil.getPlot(worldname, id);
                if (!PlotListener.plotEntry(pp, plot)) {
                    MainUtil.sendMessage(pp, C.NO_PERMISSION_EVENT, C.PERMISSION_ADMIN_ENTRY_DENIED);
                    if (!plot.equals(MainUtil.getPlot(SpongeUtil.getLocation(worldname, from)))) {
                        event.setNewLocation(from);
                    } else {
                        event.setNewLocation(world.getSpawnLocation());
                    }
                    return;
                }
            }
            final Integer border = MainUtil.worldBorder.get(worldname);
            if (border != null) {
                if (z2 > border) {
                    final Vector3d pos = to.getPosition();
                    to = to.setPosition(new Vector3d(pos.getX(), pos.getY(), border - 4));
                    event.setNewLocation(to);
                    MainUtil.sendMessage(pp, C.BORDER);
                } else if (z2 < -border) {
                    final Vector3d pos = to.getPosition();
                    to = to.setPosition(new Vector3d(pos.getX(), pos.getY(), -border + 4));
                    event.setNewLocation(to);
                    MainUtil.sendMessage(pp, C.BORDER);
                }
            }
        }
    }
    
    @Subscribe
    public void onWorldChange(final EntityTeleportEvent event) {
        final Entity entity = event.getEntity();
        if (entity instanceof Player) {
            final org.spongepowered.api.world.Location from = event.getOldLocation();
            org.spongepowered.api.world.Location to = event.getNewLocation();
            int x2;
            if (getInt(from.getX()) != (x2 = getInt(to.getX()))) {
                final Player player = (Player) entity;
                final PlotPlayer pp = SpongeUtil.getPlayer(player);
                final Extent extent = to.getExtent();
                if (!(extent instanceof World)) {
                    pp.deleteMeta("location");
                    return;
                }
                pp.setMeta("location", SpongeUtil.getLocation(player));
                final World world = (World) extent;
                final String worldname = ((World) extent).getName();
                final PlotWorld plotworld = PS.get().getPlotWorld(worldname);
                if (plotworld == null) {
                    return;
                }
                final PlotManager plotManager = PS.get().getPlotManager(worldname);
                final PlotId id = plotManager.getPlotId(plotworld, x2, 0, getInt(to.getZ()));
                final Plot lastPlot = (Plot) pp.getMeta("lastplot");
                if (id == null) {
                    if (lastPlot == null) {
                        return;
                    }
                    if (!PlotListener.plotExit(pp, lastPlot)) {
                        MainUtil.sendMessage(pp, C.NO_PERMISSION_EVENT, C.PERMISSION_ADMIN_EXIT_DENIED);
                        if (lastPlot.equals(MainUtil.getPlot(SpongeUtil.getLocation(worldname, from)))) {
                            event.setNewLocation(from);
                        } else {
                            event.setNewLocation(world.getSpawnLocation());
                        }
                        return;
                    }
                } else if ((lastPlot != null) && id.equals(lastPlot.id)) {
                    return;
                } else {
                    final Plot plot = MainUtil.getPlot(worldname, id);
                    if (!PlotListener.plotEntry(pp, plot)) {
                        MainUtil.sendMessage(pp, C.NO_PERMISSION_EVENT, C.PERMISSION_ADMIN_ENTRY_DENIED);
                        if (!plot.equals(MainUtil.getPlot(SpongeUtil.getLocation(worldname, from)))) {
                            event.setNewLocation(from);
                        } else {
                            event.setNewLocation(world.getSpawnLocation());
                        }
                        return;
                    }
                }
                final Integer border = MainUtil.worldBorder.get(worldname);
                if (border != null) {
                    if (x2 > border) {
                        final Vector3d pos = to.getPosition();
                        to = to.setPosition(new Vector3d(border - 4, pos.getY(), pos.getZ()));
                        event.setNewLocation(to);
                        MainUtil.sendMessage(pp, C.BORDER);
                    } else if (x2 < -border) {
                        final Vector3d pos = to.getPosition();
                        to = to.setPosition(new Vector3d(-border + 4, pos.getY(), pos.getZ()));
                        event.setNewLocation(to);
                        MainUtil.sendMessage(pp, C.BORDER);
                    }
                }
                return;
            }
            int z2;
            if (getInt(from.getZ()) != (z2 = getInt(to.getZ()))) {
                final Player player = (Player) entity;
                final PlotPlayer pp = SpongeUtil.getPlayer(player);
                final Extent extent = to.getExtent();
                if (!(extent instanceof World)) {
                    pp.deleteMeta("location");
                    return;
                }
                pp.setMeta("location", SpongeUtil.getLocation(player));
                final String worldname = ((World) extent).getName();
                final PlotWorld plotworld = PS.get().getPlotWorld(worldname);
                if (plotworld == null) {
                    return;
                }
                final PlotManager plotManager = PS.get().getPlotManager(worldname);
                final PlotId id = plotManager.getPlotId(plotworld, x2, 0, z2);
                final Plot lastPlot = (Plot) pp.getMeta("lastplot");
                if (id == null) {
                    if (lastPlot == null) {
                        return;
                    }
                    if (!PlotListener.plotExit(pp, lastPlot)) {
                        MainUtil.sendMessage(pp, C.NO_PERMISSION_EVENT, C.PERMISSION_ADMIN_EXIT_DENIED);
                        if (lastPlot.equals(MainUtil.getPlot(SpongeUtil.getLocation(worldname, from)))) {
                            event.setNewLocation(from);
                        } else {
                            event.setNewLocation(player.getWorld().getSpawnLocation());
                        }
                        return;
                    }
                } else if ((lastPlot != null) && id.equals(lastPlot.id)) {
                    return;
                } else {
                    final Plot plot = MainUtil.getPlot(worldname, id);
                    if (!PlotListener.plotEntry(pp, plot)) {
                        MainUtil.sendMessage(pp, C.NO_PERMISSION_EVENT, C.PERMISSION_ADMIN_ENTRY_DENIED);
                        if (!plot.equals(MainUtil.getPlot(SpongeUtil.getLocation(worldname, from)))) {
                            event.setNewLocation(from);
                        } else {
                            event.setNewLocation(player.getWorld().getSpawnLocation());
                        }
                        return;
                    }
                }
                final Integer border = MainUtil.worldBorder.get(worldname);
                if (border != null) {
                    if (z2 > border) {
                        final Vector3d pos = to.getPosition();
                        to = to.setPosition(new Vector3d(pos.getX(), pos.getY(), border - 4));
                        event.setNewLocation(to);
                        MainUtil.sendMessage(pp, C.BORDER);
                    } else if (z2 < -border) {
                        final Vector3d pos = to.getPosition();
                        to = to.setPosition(new Vector3d(pos.getX(), pos.getY(), -border + 4));
                        event.setNewLocation(to);
                        MainUtil.sendMessage(pp, C.BORDER);
                    }
                }
            }
        }
    }
    
    @Subscribe
    public void onWorldChange(final PlayerChangeWorldEvent event) {
        final PlotPlayer player = SpongeUtil.getPlayer(event.getUser());
        
        player.deleteMeta("location");
        player.deleteMeta("lastplot");
        
        // TODO worldedit mask
        
        if (Settings.PERMISSION_CACHING) {
            ((SpongePlayer) player).hasPerm = new HashSet<>();
            ((SpongePlayer) player).noPerm = new HashSet<>();
        }
    }
}
