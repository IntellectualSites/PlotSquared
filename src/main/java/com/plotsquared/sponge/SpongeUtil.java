package com.plotsquared.sponge;

import org.spongepowered.api.entity.Entity;

import com.flowpowered.math.vector.Vector3d;
import com.flowpowered.math.vector.Vector3i;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.util.MathMan;

public class SpongeUtil {

    public static Location getLocation(Entity player) {
        String world = player.getWorld().getName();
        org.spongepowered.api.world.Location loc = player.getLocation();
        Vector3i pos = loc.getBlockPosition();
        return new Location(world, pos.getX(), pos.getY(), pos.getZ());
    }
    
    public static Location getLocationFull(Entity player) {
        String world = player.getWorld().getName();
        Vector3d rot = player.getRotation();
        float[] pitchYaw = MathMan.getPitchAndYaw((float) rot.getX(), (float) rot.getY(), (float) rot.getZ());
        org.spongepowered.api.world.Location loc = player.getLocation();
        Vector3i pos = loc.getBlockPosition();
        return new Location(world, pos.getX(), pos.getY(), pos.getZ(), pitchYaw[1], pitchYaw[0]);
    }
    
}
