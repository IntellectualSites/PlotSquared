package com.plotsquared.sponge.util;

import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.extent.Extent;

import com.flowpowered.math.vector.Vector3d;
import com.flowpowered.math.vector.Vector3i;
import com.google.common.base.Optional;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.MathMan;
import com.intellectualcrafters.plot.util.UUIDHandler;
import com.plotsquared.sponge.SpongeMain;
import com.plotsquared.sponge.object.SpongePlayer;

public class SpongeUtil {

    public static Location getLocation(Entity player) {
        String world = player.getWorld().getName();
        org.spongepowered.api.world.Location loc = player.getLocation();
        Vector3i pos = loc.getBlockPosition();
        return new Location(world, pos.getX(), pos.getY(), pos.getZ());
    }
    
    public static Location getLocation(org.spongepowered.api.world.Location block) {
        Extent extent = block.getExtent();
        if (extent instanceof World) {
            return getLocation(((World) extent).getName(), block);
        }
        return null;
    }
    
    public static Location getLocationFull(Entity player) {
        String world = player.getWorld().getName();
        Vector3d rot = player.getRotation();
        float[] pitchYaw = MathMan.getPitchAndYaw((float) rot.getX(), (float) rot.getY(), (float) rot.getZ());
        org.spongepowered.api.world.Location loc = player.getLocation();
        Vector3i pos = loc.getBlockPosition();
        return new Location(world, pos.getX(), pos.getY(), pos.getZ(), pitchYaw[1], pitchYaw[0]);
    }

    private static Player lastPlayer = null;
    private static PlotPlayer lastPlotPlayer = null;
    
    public static PlotPlayer getPlayer(Player player) {
        if (player == lastPlayer) {
            return lastPlotPlayer;
        }
        String name = player.getName();
        PlotPlayer pp = UUIDHandler.getPlayers().get(name);
        if (pp != null) {
            return pp;
        }
        lastPlotPlayer = new SpongePlayer(player);
        UUIDHandler.getPlayers().put(name, lastPlotPlayer);
        lastPlayer = player;
        return lastPlotPlayer;
    }

    public static Player getPlayer(PlotPlayer player) {
        if (player instanceof SpongePlayer) {
            return ((SpongePlayer) player).player;
        }
        return null;
    }
    
    private static World lastWorld;
    private static String last;

    public static World getWorld(String world) {
        if (world == last) {
            return lastWorld;
        }
        Optional<World> optional = SpongeMain.THIS.getServer().getWorld(world);
        if (!optional.isPresent()) {
            return null;
        }
        return optional.get();
    }

    public static void removePlayer(String player) {
        lastPlayer = null;
        lastPlotPlayer = null;
        UUIDHandler.getPlayers().remove(player);
    }

    public static Location getLocation(String world, org.spongepowered.api.world.Location spawn) {
        return new Location(world, spawn.getBlockX(), spawn.getBlockY(), spawn.getBlockZ());
    }

    public static String getWorldName(org.spongepowered.api.world.Location origin) {
        Extent extent = origin.getExtent();
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

    public static org.spongepowered.api.world.Location getLocation(Location loc) {
        Optional<World> world = SpongeMain.THIS.getServer().getWorld(loc.getWorld());
        if (!world.isPresent()) {
            return null;
        }
        return new org.spongepowered.api.world.Location(world.get(), loc.getX(), loc.getY(), loc.getZ());
    }
}
