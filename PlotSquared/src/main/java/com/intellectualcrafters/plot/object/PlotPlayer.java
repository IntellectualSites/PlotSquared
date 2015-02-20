package com.intellectualcrafters.plot.object;

import com.intellectualcrafters.plot.BukkitMain;
import com.intellectualcrafters.plot.util.bukkit.UUIDHandler;

import java.util.UUID;

/**
 * Created 2015-02-20 for PlotSquared
 *
 * @author Citymonstret
 */
public class PlotPlayer {

    private final String name;
    private final Location location;

    public PlotPlayer(final String name, final Location location) {
        this.name = name;
        this.location = location;
    }

    @Override
    public String toString() {
        return this.name;
    }

    public Location getLocation() {
        return this.location;
    }

    public UUID getUUID() {
        return UUIDHandler.getUUID(name);
    }

    public boolean hasPermission(final String perm) {
        return BukkitMain.hasPermission(this, perm);
    }

    public void sendMessage(final String message) {
        return BukkitMain.sendMessage( final PlotPlayer player);
    }
}
