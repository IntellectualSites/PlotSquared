package com.plotsquared.sponge.util;

import java.util.Optional;

import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.extent.Extent;

import com.flowpowered.math.vector.Vector3d;
import com.flowpowered.math.vector.Vector3i;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.MathMan;
import com.intellectualcrafters.plot.util.UUIDHandler;
import com.plotsquared.sponge.SpongeMain;
import com.plotsquared.sponge.object.SpongePlayer;

public class SpongeUtil {
    
    public static Location getLocation(final Entity player) {
        final String world = player.getWorld().getName();
        final org.spongepowered.api.world.Location loc = player.getLocation();
        final Vector3i pos = loc.getBlockPosition();
        return new Location(world, pos.getX(), pos.getY(), pos.getZ());
    }
    
    public static Location getLocation(final org.spongepowered.api.world.Location<World> block) {
        return getLocation(block.getExtent().getName(), block);
    }
    
    public static Location getLocationFull(final Entity player) {
        final String world = player.getWorld().getName();
        final Vector3d rot = player.getRotation();
        final float[] pitchYaw = MathMan.getPitchAndYaw((float) rot.getX(), (float) rot.getY(), (float) rot.getZ());
        final org.spongepowered.api.world.Location loc = player.getLocation();
        final Vector3i pos = loc.getBlockPosition();
        return new Location(world, pos.getX(), pos.getY(), pos.getZ(), pitchYaw[1], pitchYaw[0]);
    }
    
    private static Player lastPlayer = null;
    private static PlotPlayer lastPlotPlayer = null;
    
    public static PlotPlayer getPlayer(final Player player) {
        if (player == lastPlayer) {
            return lastPlotPlayer;
        }
        final String name = player.getName();
        final PlotPlayer pp = UUIDHandler.getPlayer(name);
        if (pp != null) {
            return pp;
        }
        lastPlotPlayer = new SpongePlayer(player);
        UUIDHandler.getPlayers().put(name, lastPlotPlayer);
        lastPlayer = player;
        return lastPlotPlayer;
    }
    
    public static Player getPlayer(final PlotPlayer player) {
        if (player instanceof SpongePlayer) {
            return ((SpongePlayer) player).player;
        }
        return null;
    }
    
    private static World lastWorld;
    private static String last;
    
    public static World getWorld(final String world) {
        if (world == last) {
            return lastWorld;
        }
        final Optional<World> optional = SpongeMain.THIS.getServer().getWorld(world);
        if (!optional.isPresent()) {
            return null;
        }
        return optional.get();
    }
    
    public static void removePlayer(final String player) {
        lastPlayer = null;
        lastPlotPlayer = null;
    }
    
    public static Location getLocation(final String world, final org.spongepowered.api.world.Location spawn) {
        return new Location(world, spawn.getBlockX(), spawn.getBlockY(), spawn.getBlockZ());
    }
    
    public static String getWorldName(final org.spongepowered.api.world.Location origin) {
        final Extent extent = origin.getExtent();
        if (extent == lastWorld) {
            return last;
        }
        if (extent instanceof World) {
            lastWorld = (World) extent;
            last = ((World) extent).getName();
            return last;
        }
        return null;
    }
    
    public static org.spongepowered.api.world.Location getLocation(final Location loc) {
        final Optional<World> world = SpongeMain.THIS.getServer().getWorld(loc.getWorld());
        if (!world.isPresent()) {
            return null;
        }
        return new org.spongepowered.api.world.Location(world.get(), loc.getX(), loc.getY(), loc.getZ());
    }
    
    public static Location getLocation(String world, Vector3i position) {
        return new Location(world, position.getX(), position.getY(), position.getZ());
    }
    
    public static Location getLocation(String world, Vector3d position) {
        return new Location(world, MathMan.roundInt(position.getX()), MathMan.roundInt(position.getY()), MathMan.roundInt(position.getZ()));
    }
}
