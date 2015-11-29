package com.plotsquared.sponge.listener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;

import org.spongepowered.api.GameProfile;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.Transform;
import org.spongepowered.api.entity.explosive.Explosive;
import org.spongepowered.api.entity.explosive.PrimedTNT;
import org.spongepowered.api.entity.living.Ambient;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.entity.living.animal.Animal;
import org.spongepowered.api.entity.living.monster.Monster;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.vehicle.Boat;
import org.spongepowered.api.entity.vehicle.minecart.Minecart;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.action.LightningEvent;
import org.spongepowered.api.event.action.MessageEvent;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.block.GrowBlockEvent;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.block.MoveBlockEvent;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.command.SendCommandEvent;
import org.spongepowered.api.event.entity.BreedEntityEvent;
import org.spongepowered.api.event.entity.DisplaceEntityEvent;
import org.spongepowered.api.event.entity.SpawnEntityEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.event.world.ExplosionEvent;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.Texts;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.extent.Extent;

import com.flowpowered.math.vector.Vector3d;
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
import com.intellectualcrafters.plot.util.ExpireManager;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.MathMan;
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
     *  - BlockPhysicsEvent
     *  - BlockFormEvent
     *  - BlockFadeEvent
     *  - BlockFromToEvent
     *  - BlockDamageEvent
     *  - Structure (tree etc)
     *  - ChunkPreGenerateEvent
     *  - PlayerIgniteBlockEvent
     *  - PlayerBucketEmptyEvent
     *  - PlayerBucketFillEvent
     *  - VehicleCreateEvent
     *  - HangingPlaceEvent
     *  - HangingBreakEvent
     *  - EntityChangeBlockEvent
     *  - PVP
     *  - block dispense
     *  - PVE
     *  - VehicleDestroy
     *  - Projectile
     *  - enderman harvest
     */
    
    //    @Listener
    //    public void onFluidSpread(final NotifyNeighborBlockEvent event) {
    //        onPhysics(event);
    //    }
    //    
    //    @Listener
    //    public void onFluidSpread(final NotifyNeighborBlockEvent.Burn event) {
    //        onPhysics(event);
    //    }
    //    
    //    @Listener
    //    public void onFluidSpread(final NotifyNeighborBlockEvent.Ignite event) {
    //        onPhysics(event);
    //    }
    //    
    //    @Listener
    //    public void onFluidSpread(final NotifyNeighborBlockEvent.Power event) {
    //        // TODO redstone
    //    }
    //
    //    public void onPhysics(final NotifyNeighborBlockEvent event) {
    //        final AtomicBoolean cancelled = new AtomicBoolean(false);
    //        final Map<Direction, org.spongepowered.api.world.Location<World>> relatives = event.getRelatives();
    //        event.filterDirections(new Predicate<Direction>() {
    //            
    //            @Override
    //            public boolean test(Direction dir) {
    //                if (cancelled.get()) {
    //                    return true;
    //                }
    //                org.spongepowered.api.world.Location<World> loc = relatives.get(dir);
    //                com.intellectualcrafters.plot.object.Location plotloc = SpongeUtil.getLocation(loc.getExtent().getName(), loc);
    //                Plot plot = MainUtil.getPlot(plotloc);
    //                if (plot == null) {
    //                    if (MainUtil.isPlotAreaAbs(plotloc)) {
    //                        cancelled.set(true);
    //                        return false;
    //                    }
    //                    cancelled.set(true);
    //                    return true;
    //                }
    //                org.spongepowered.api.world.Location<World> relative = loc.getRelative(dir);
    //                com.intellectualcrafters.plot.object.Location relLoc = SpongeUtil.getLocation(relative.getExtent().getName(), relative);
    //                if (plot.equals(MainUtil.getPlot(relLoc))) {
    //                    return true;
    //                }
    //                return false;
    //            }
    //        });
    //    }
    
    public <T> T getCause(Cause cause, Class<T> clazz) {
        Optional<?> root = cause.root();
        if (root.isPresent()) {
            Object source = root.get();
            if (clazz.isInstance(source)) {
                return (T) source;
            }
        }
        return null;
    }
    
    @Listener
    public void onCommand(final BreedEntityEvent.Breed event) {
        final Location loc = SpongeUtil.getLocation(event.getTargetEntity());
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
        if (!plotworld.SPAWN_BREEDING) {
            event.setCancelled(true);
        }
    }
    
    @Listener
    public void onMobSpawn(final SpawnEntityEvent event) {
        World world = event.getTargetWorld();
        final PlotWorld plotworld = PS.get().getPlotWorld(world.getName());
        if (plotworld == null) {
            return;
        }
        List<Entity> entities = event.getEntities();
        event.filterEntities(new Predicate<Entity>() {
            
            @Override
            public boolean test(Entity entity) {
                if (entity instanceof Player) {
                    return true;
                }
                final Location loc = SpongeUtil.getLocation(entity);
                final Plot plot = MainUtil.getPlot(loc);
                if (plot == null) {
                    if (MainUtil.isPlotRoad(loc)) {
                        return false;
                    }
                    return true;
                }
                //        Player player = this.<Player> getCause(event.getCause());
                // TODO selectively cancel depending on spawn reason
                // - Not sure if possible to get spawn reason (since there are no callbacks)
                //        if (player != null && !plotworld.SPAWN_EGGS) {
                //            return false;
                //            return true;
                //        }
                
                if (entity.getType() == EntityTypes.ITEM) {
                    if (FlagManager.isPlotFlagFalse(plot, "item-drop")) {
                        return false;
                    }
                    return true;
                }
                int[] mobs = null;
                if (entity instanceof Living) {
                    if (!plotworld.MOB_SPAWNING) {
                        return false;
                    }
                    final Flag mobCap = FlagManager.getPlotFlagRaw(plot, "mob-cap");
                    if (mobCap != null) {
                        final Integer cap = (Integer) mobCap.getValue();
                        if (cap == 0) {
                            return false;
                        }
                        if (mobs == null) {
                            mobs = MainUtil.countEntities(plot);
                        }
                        if (mobs[3] >= cap) {
                            return false;
                        }
                    }
                    if ((entity instanceof Ambient) || (entity instanceof Animal)) {
                        final Flag animalFlag = FlagManager.getPlotFlagRaw(plot, "animal-cap");
                        if (animalFlag != null) {
                            final int cap = ((Integer) animalFlag.getValue());
                            if (cap == 0) {
                                return false;
                            }
                            if (mobs == null) {
                                mobs = MainUtil.countEntities(plot);
                            }
                            if (mobs[1] >= cap) {
                                return false;
                            }
                        }
                    }
                    if (entity instanceof Monster) {
                        final Flag monsterFlag = FlagManager.getPlotFlagRaw(plot, "hostile-cap");
                        if (monsterFlag != null) {
                            final int cap = ((Integer) monsterFlag.getValue());
                            if (cap == 0) {
                                return false;
                            }
                            if (mobs == null) {
                                mobs = MainUtil.countEntities(plot);
                            }
                            if (mobs[2] >= cap) {
                                return false;
                            }
                        }
                    }
                    return true;
                }
                if ((entity instanceof Minecart) || (entity instanceof Boat)) {
                    final Flag vehicleFlag = FlagManager.getPlotFlagRaw(plot, "vehicle-cap");
                    if (vehicleFlag != null) {
                        final int cap = ((Integer) vehicleFlag.getValue());
                        if (cap == 0) {
                            return false;
                        }
                        if (mobs == null) {
                            mobs = MainUtil.countEntities(plot);
                        }
                        if (mobs[4] >= cap) {
                            return false;
                        }
                    }
                }
                final Flag entityCap = FlagManager.getPlotFlagRaw(plot, "entity-cap");
                if (entityCap != null) {
                    final Integer cap = (Integer) entityCap.getValue();
                    if (cap == 0) {
                        return false;
                    }
                    if (mobs == null) {
                        mobs = MainUtil.countEntities(plot);
                    }
                    if (mobs[0] >= cap) {
                        return false;
                    }
                }
                if (entity instanceof PrimedTNT) {
                    Vector3d pos = entity.getLocation().getPosition();
                    entity.setRotation(new Vector3d(MathMan.roundInt(pos.getX()), MathMan.roundInt(pos.getY()), MathMan.roundInt(pos.getZ())));
                }
                return true;
            }
        });
    }
    
    @Listener
    public void onCommand(final SendCommandEvent event) {
        switch (event.getCommand().toLowerCase()) {
            case "plotme": {
                Player source = this.<Player> getCause(event.getCause(), Player.class);
                if (source == null) {
                    return;
                }
                if (Settings.USE_PLOTME_ALIAS) {
                    SpongeMain.THIS.getGame().getCommandDispatcher().process(source, ("plots " + event.getArguments()).trim());
                } else {
                    source.sendMessage(SpongeMain.THIS.getText(C.NOT_USING_PLOTME.s()));
                }
                event.setCancelled(true);
            }
        }
    }
    
    public void onBlockChange(ChangeBlockEvent event) {
        final World world = event.getTargetWorld();
        final String worldname = world.getName();
        if (!PS.get().isPlotWorld(worldname)) {
            return;
        }
        List<Transaction<BlockSnapshot>> transactions = event.getTransactions();
        Transaction<BlockSnapshot> first = transactions.get(0);
        Location loc = SpongeUtil.getLocation(worldname, first.getOriginal().getPosition());
        Plot plot = MainUtil.getPlot(loc);
        if (plot == null) {
            if (!MainUtil.isPlotAreaAbs(loc)) {
                return;
            }
            event.setCancelled(true);
            return;
        }
        event.filter(new Predicate<org.spongepowered.api.world.Location<World>>() {
            
            @Override
            public boolean test(org.spongepowered.api.world.Location<World> loc) {
                if (MainUtil.isPlotRoad(SpongeUtil.getLocation(worldname, loc))) {
                    return false;
                }
                return true;
            }
        });
    }
    
    @Listener
    public void onBlockMove(final MoveBlockEvent event) {
        onBlockChange(event);
    }
    
    @Listener
    public void onFloraGrow(final GrowBlockEvent event) {
        onBlockChange(event);
    }
    
    @Listener
    public void onLightning(final LightningEvent.Strike event) {
        onBlockChange(event);
    }
    
    public void printCause(String method, Cause cause) {
        System.out.println(method + ": " + cause.toString());
        System.out.println(method + ": " + cause.getClass());
        System.out.println(method + ": " + (cause.root().isPresent() ? cause.root().get() : null));
    }
    
    @Listener
    public void onChat(final MessageEvent event) {
        // TODO
        Player player = this.<Player> getCause(event.getCause(), Player.class);
        if (player == null) {
            return;
        }
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
        final Text message = event.getMessage();
        
        // TODO use display name rather than username
        //  - Getting displayname currently causes NPE, so wait until sponge fixes that
        
        final String sender = player.getName();
        final PlotId id = plot.id;
        final String newMessage = StringMan.replaceAll(C.PLOT_CHAT_FORMAT.s(), "%plot_id%", id.x + ";" + id.y, "%sender%", sender);
        final Text forcedMessage = event.getMessage();
        //        String forcedMessage = StringMan.replaceAll(C.PLOT_CHAT_FORCED.s(), "%plot_id%", id.x + ";" + id.y, "%sender%", sender);
        for (Entry<String, PlotPlayer> entry : UUIDHandler.getPlayers().entrySet()) {
            PlotPlayer user = entry.getValue();
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
        event.setMessage(null);
    }
    
    @Listener
    public void onBigBoom(final ExplosionEvent.Detonate event) {
        final World world = event.getTargetWorld();
        final String worldname = world.getName();
        if (!PS.get().isPlotWorld(worldname)) {
            return;
        }
        Optional<Explosive> source = event.getExplosion().getSourceExplosive();
        if (!source.isPresent()) {
            event.setCancelled(true);
            return;
        }
        Explosive tnt = source.get();
        Location origin = SpongeUtil.getLocation(worldname, tnt.getRotation());
        Plot originPlot = origin.getPlot();
        Location current = SpongeUtil.getLocation(tnt);
        final Plot currentPlot = current.getPlot();
        if (!Objects.equals(originPlot, currentPlot)) {
            event.setCancelled(true);
            return;
        }
        if (originPlot == null && !MainUtil.isPlotAreaAbs(current)) {
            return;
        }
        if (!FlagManager.isPlotFlagTrue(currentPlot, "explosion")) {
            event.setCancelled(true);
            return;
        }
        event.filter(new Predicate<org.spongepowered.api.world.Location<World>>() {
            @Override
            public boolean test(org.spongepowered.api.world.Location<World> loc) {
                return currentPlot.equals(SpongeUtil.getLocation(loc.getExtent().getName(), loc).getPlot());
            }
        });
        event.filterEntities(new Predicate<Entity>() {
            @Override
            public boolean test(Entity entity) {
                return currentPlot.equals(SpongeUtil.getLocation(entity).getPlot());
            }
        });
    }
    
    //    @Listener
    //    public void onChunkPreGenerator(final ChunkPreGenerateEvent event) {
    //        final org.spongepowered.api.world.Chunk chunk = event.getChunk();
    //        final World world = chunk.getWorld();
    //        final String worldname = world.getName();
    //        if (MainUtil.worldBorder.containsKey(worldname)) {
    //            final int border = MainUtil.getBorder(worldname);
    //            final Vector3i min = world.getBlockMin();
    //            final int x = Math.abs(min.getX());
    //            final int z = Math.abs(min.getZ());
    //            if ((x > border) || (z > border)) {
    //                // TODO cancel this chunk from loading
    //                // - Currently not possible / this event doesn't seem to be called
    //            }
    //        }
    //    }
    
    @Listener
    public void onBlockBreak(final ChangeBlockEvent.Decay event) {
        onBlockChange(event);
    }
    
    @Listener
    public void onBlockBreak(final ChangeBlockEvent.Fluid event) {
        onBlockChange(event);
    }
    
    @Listener
    public void onBlockBreak(final ChangeBlockEvent.Grow event) {
        onBlockChange(event);
    }
    
    @Listener
    public void onBlockBreak(final ChangeBlockEvent.Modify event) {
        onBlockChange(event);
    }
    
    @Listener
    public void onBlockBreak(final ChangeBlockEvent.Break event) {
        Player player = this.<Player> getCause(event.getCause(), Player.class);
        if (player == null) {
            event.setCancelled(true);
            return;
        }
        final PlotPlayer pp = SpongeUtil.getPlayer(player);
        final World world = event.getTargetWorld();
        final String worldname = world.getName();
        if (!PS.get().isPlotWorld(worldname)) {
            return;
        }
        List<Transaction<BlockSnapshot>> transactions = event.getTransactions();
        Transaction<BlockSnapshot> first = transactions.get(0);
        BlockSnapshot pos = first.getOriginal();
        Location loc = SpongeUtil.getLocation(worldname, pos.getPosition());
        Plot plot = MainUtil.getPlot(loc);
        if (plot == null) {
            if (!MainUtil.isPlotAreaAbs(loc)) {
                return;
            }
            if (!Permissions.hasPermission(pp, C.PERMISSION_ADMIN_DESTROY_ROAD)) {
                event.setCancelled(true);
                return;
            }
        } else if (transactions.size() == 1) {
            if (!plot.hasOwner()) {
                if (Permissions.hasPermission(pp, C.PERMISSION_ADMIN_DESTROY_UNOWNED)) {
                    return;
                }
                MainUtil.sendMessage(pp, C.NO_PERMISSION_EVENT, C.PERMISSION_ADMIN_DESTROY_UNOWNED);
                event.setCancelled(true);
                return;
            }
            if (plot.isAdded(pp.getUUID()) || Permissions.hasPermission(pp, C.PERMISSION_ADMIN_DESTROY_OTHER)) {
                return;
            } else {
                MainUtil.sendMessage(pp, C.NO_PERMISSION_EVENT, C.PERMISSION_ADMIN_DESTROY_OTHER);
                final Flag destroy = FlagManager.getPlotFlagRaw(plot, "break");
                final BlockState state = pos.getState();
                if ((destroy == null) || !((HashSet<PlotBlock>) destroy.getValue()).contains(SpongeMain.THIS.getPlotBlock(state))) {
                    event.setCancelled(true);
                    return;
                }
            }
        }
        event.filter(new Predicate<org.spongepowered.api.world.Location<World>>() {
            
            @Override
            public boolean test(org.spongepowered.api.world.Location<World> l) {
                Location loc = SpongeUtil.getLocation(worldname, l);
                Plot plot = loc.getPlot();
                if (plot == null) {
                    if (!MainUtil.isPlotAreaAbs(loc)) {
                        return true;
                    }
                    if (!Permissions.hasPermission(pp, C.PERMISSION_ADMIN_DESTROY_ROAD)) {
                        return false;
                    }
                    return true;
                }
                if (!plot.hasOwner()) {
                    if (Permissions.hasPermission(pp, C.PERMISSION_ADMIN_DESTROY_UNOWNED)) {
                        return true;
                    }
                    MainUtil.sendMessage(pp, C.NO_PERMISSION_EVENT, C.PERMISSION_ADMIN_DESTROY_UNOWNED);
                    return false;
                }
                if (plot.isAdded(pp.getUUID()) || Permissions.hasPermission(pp, C.PERMISSION_ADMIN_DESTROY_OTHER)) {
                    return true;
                } else {
                    final Flag destroy = FlagManager.getPlotFlagRaw(plot, "break");
                    final BlockState state = l.getBlock();
                    if ((destroy != null) && ((HashSet<PlotBlock>) destroy.getValue()).contains(SpongeMain.THIS.getPlotBlock(state))) {
                        return true;
                    }
                    MainUtil.sendMessage(pp, C.NO_PERMISSION_EVENT, C.PERMISSION_ADMIN_DESTROY_OTHER);
                    return false;
                }
            }
        });
    }
    
    @Listener
    public void onBlockPlace(final ChangeBlockEvent.Place event) {
        Player player = this.<Player> getCause(event.getCause(), Player.class);
        if (player == null) {
            event.setCancelled(true);
            return;
        }
        final PlotPlayer pp = SpongeUtil.getPlayer(player);
        final World world = event.getTargetWorld();
        final String worldname = world.getName();
        if (!PS.get().isPlotWorld(worldname)) {
            return;
        }
        List<Transaction<BlockSnapshot>> transactions = event.getTransactions();
        Transaction<BlockSnapshot> first = transactions.get(0);
        BlockSnapshot pos = first.getOriginal();
        Location loc = SpongeUtil.getLocation(worldname, pos.getPosition());
        Plot plot = MainUtil.getPlot(loc);
        if (plot == null) {
            if (!MainUtil.isPlotAreaAbs(loc)) {
                return;
            }
            if (!Permissions.hasPermission(pp, C.PERMISSION_ADMIN_BUILD_ROAD)) {
                event.setCancelled(true);
                return;
            }
        } else if (transactions.size() == 1) {
            if (!plot.hasOwner()) {
                if (Permissions.hasPermission(pp, C.PERMISSION_ADMIN_BUILD_UNOWNED)) {
                    return;
                }
                MainUtil.sendMessage(pp, C.NO_PERMISSION_EVENT, C.PERMISSION_ADMIN_BUILD_UNOWNED);
                event.setCancelled(true);
                return;
            }
            if (plot.isAdded(pp.getUUID()) || Permissions.hasPermission(pp, C.PERMISSION_ADMIN_BUILD_OTHER)) {
                return;
            } else {
                MainUtil.sendMessage(pp, C.NO_PERMISSION_EVENT, C.PERMISSION_ADMIN_BUILD_OTHER);
                final Flag BUILD = FlagManager.getPlotFlagRaw(plot, C.FLAG_PLACE.s());
                final BlockState state = pos.getState();
                if ((BUILD == null) || !((HashSet<PlotBlock>) BUILD.getValue()).contains(SpongeMain.THIS.getPlotBlock(state))) {
                    event.setCancelled(true);
                    return;
                }
            }
        }
        event.filter(new Predicate<org.spongepowered.api.world.Location<World>>() {
            
            @Override
            public boolean test(org.spongepowered.api.world.Location<World> l) {
                Location loc = SpongeUtil.getLocation(worldname, l);
                Plot plot = loc.getPlot();
                if (plot == null) {
                    if (!MainUtil.isPlotAreaAbs(loc)) {
                        return true;
                    }
                    if (!Permissions.hasPermission(pp, C.PERMISSION_ADMIN_BUILD_ROAD)) {
                        return false;
                    }
                    return true;
                }
                if (!plot.hasOwner()) {
                    if (Permissions.hasPermission(pp, C.PERMISSION_ADMIN_BUILD_UNOWNED)) {
                        return true;
                    }
                    MainUtil.sendMessage(pp, C.NO_PERMISSION_EVENT, C.PERMISSION_ADMIN_BUILD_UNOWNED);
                    return false;
                }
                if (plot.isAdded(pp.getUUID()) || Permissions.hasPermission(pp, C.PERMISSION_ADMIN_BUILD_OTHER)) {
                    return true;
                } else {
                    final Flag build = FlagManager.getPlotFlagRaw(plot, C.FLAG_PLACE.s());
                    final BlockState state = l.getBlock();
                    if ((build != null) && ((HashSet<PlotBlock>) build.getValue()).contains(SpongeMain.THIS.getPlotBlock(state))) {
                        return true;
                    }
                    MainUtil.sendMessage(pp, C.NO_PERMISSION_EVENT, C.PERMISSION_ADMIN_BUILD_OTHER);
                    return false;
                }
            }
        });
    }
    
    @Listener
    public void onBlockInteract(final InteractBlockEvent.Secondary event) {
        final Player player = this.<Player> getCause(event.getCause(), Player.class);
        if (player == null) {
            event.setCancelled(true);
            return;
        }
        Optional<org.spongepowered.api.world.Location<World>> target = event.getTargetBlock().getLocation();
        if (!target.isPresent()) {
            return;
        }
        org.spongepowered.api.world.Location<World> l = target.get();
        Location loc = SpongeUtil.getLocation(l);
        Plot plot = MainUtil.getPlot(loc);
        PlotPlayer pp = SpongeUtil.getPlayer(player);
        if (plot == null) {
            if (!MainUtil.isPlotAreaAbs(loc)) {
                return;
            }
            if (!Permissions.hasPermission(pp, C.PERMISSION_ADMIN_INTERACT_ROAD)) {
                event.setCancelled(true);
                return;
            }
            return;
        }
        if (!plot.hasOwner()) {
            if (Permissions.hasPermission(pp, C.PERMISSION_ADMIN_INTERACT_UNOWNED)) {
                return;
            }
            MainUtil.sendMessage(pp, C.NO_PERMISSION_EVENT, C.PERMISSION_ADMIN_INTERACT_UNOWNED);
            event.setCancelled(true);
            return;
        }
        if (plot.isAdded(pp.getUUID()) || Permissions.hasPermission(pp, C.PERMISSION_ADMIN_INTERACT_OTHER)) {
            return;
        } else {
            final Flag flag = FlagManager.getPlotFlagRaw(plot, "use");
            if ((flag != null) && ((HashSet<PlotBlock>) flag.getValue()).contains(SpongeMain.THIS.getPlotBlock(l.getBlock()))) {
                return;
            }
            MainUtil.sendMessage(pp, C.NO_PERMISSION_EVENT, C.PERMISSION_ADMIN_INTERACT_OTHER);
            event.setCancelled(true);
            return;
        }
    }
    
    @Listener
    public void onConnect(final ClientConnectionEvent.Login event) {
        GameProfile profile = event.getProfile();
        if (profile == null) {
            return;
        }
        if (profile.getName().equals("PlotSquared") || profile.getUniqueId().equals(DBFunc.everyone) || DBFunc.everyone.equals(UUIDHandler.getUUID(profile.getName(), null))) {
            event.setCancelled(true);
        }
    }
    
    @Listener
    public void onJoin(final ClientConnectionEvent.Join event) {
        final Player player = event.getTargetEntity();
        SpongeUtil.removePlayer(player.getName());
        final PlotPlayer pp = SpongeUtil.getPlayer(player);
        final String username = pp.getName();
        final StringWrapper name = new StringWrapper(username);
        final UUID uuid = pp.getUUID();
        UUIDHandler.add(name, uuid);
        ExpireManager.dates.put(uuid, System.currentTimeMillis());
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
    
    @Listener
    public void onQuit(final ClientConnectionEvent.Disconnect event) {
        final Player player = event.getTargetEntity();
        final PlotPlayer pp = SpongeUtil.getPlayer(player);
        pp.unregister();
    }
    
    public int getInt(final double value) {
        return (int) (value < 0 ? value - 1 : value);
    }
    
    @Listener
    public void onMove(final DisplaceEntityEvent.TargetPlayer event) {
        final org.spongepowered.api.world.Location from = event.getFromTransform().getLocation();
        org.spongepowered.api.world.Location to = event.getToTransform().getLocation();
        int x2;
        if (getInt(from.getX()) != (x2 = getInt(to.getX()))) {
            final Player player = event.getTargetEntity();
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
                        event.setCancelled(true);
                    } else {
                        event.setToTransform(new Transform<>(world.getSpawnLocation()));
                    }
                    return;
                }
            } else if ((lastPlot != null) && id.equals(lastPlot.id)) {
                return;
            } else {
                final Plot plot = MainUtil.getPlot(worldname, id);
                if (!PlotListener.plotEntry(pp, plot)) {
                    MainUtil.sendMessage(pp, C.NO_PERMISSION_EVENT, C.PERMISSION_ADMIN_ENTRY_DENIED);
                    if (!plot.getBasePlot(false).equals(MainUtil.getPlot(SpongeUtil.getLocation(worldname, from)))) {
                        event.setCancelled(true);
                    } else {
                        event.setToTransform(new Transform<>(world.getSpawnLocation()));
                    }
                    return;
                }
            }
            final Integer border = MainUtil.worldBorder.get(worldname);
            if (border != null) {
                if (x2 > border) {
                    final Vector3d pos = to.getPosition();
                    to = to.setPosition(new Vector3d(border - 4, pos.getY(), pos.getZ()));
                    event.setToTransform(new Transform(to));
                    MainUtil.sendMessage(pp, C.BORDER);
                } else if (x2 < -border) {
                    final Vector3d pos = to.getPosition();
                    to = to.setPosition(new Vector3d(-border + 4, pos.getY(), pos.getZ()));
                    event.setToTransform(new Transform(to));
                    MainUtil.sendMessage(pp, C.BORDER);
                }
            }
            return;
        }
        int z2;
        if (getInt(from.getZ()) != (z2 = getInt(to.getZ()))) {
            final Player player = event.getTargetEntity();
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
                        event.setCancelled(true);
                    } else {
                        event.setToTransform(new Transform<>(world.getSpawnLocation()));
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
                        event.setCancelled(true);
                    } else {
                        event.setToTransform(new Transform<>(world.getSpawnLocation()));
                    }
                    return;
                }
            }
            final Integer border = MainUtil.worldBorder.get(worldname);
            if (border != null) {
                if (z2 > border) {
                    final Vector3d pos = to.getPosition();
                    to = to.setPosition(new Vector3d(pos.getX(), pos.getY(), border - 4));
                    event.setToTransform(new Transform(to));
                    MainUtil.sendMessage(pp, C.BORDER);
                } else if (z2 < -border) {
                    final Vector3d pos = to.getPosition();
                    to = to.setPosition(new Vector3d(pos.getX(), pos.getY(), -border + 4));
                    event.setToTransform(new Transform(to));
                    MainUtil.sendMessage(pp, C.BORDER);
                }
            }
        }
    }
}
