package com.intellectualcrafters.plot.object;

import java.util.HashSet;
import java.util.UUID;

import org.bukkit.entity.Player;

import com.intellectualcrafters.plot.config.Settings;
import com.intellectualcrafters.plot.util.bukkit.BukkitUtil;
import com.intellectualcrafters.plot.util.bukkit.UUIDHandler;

public class BukkitPlayer implements PlotPlayer {
    
    public final Player player;
    UUID uuid;
    String name;
    public HashSet<String> hasPerm = new HashSet<>();
    public HashSet<String> noPerm = new HashSet<>();
    private int op = 0;
    private long last = 0;

    /**
     * Please do not use this method. Instead use BukkitUtil.getPlayer(Player), as it caches player objects.
     * @param player
     */
    public BukkitPlayer(final Player player) {
        this.player = player;
    }
    
    public long getPreviousLogin() {
        if (last == 0) {
            last = player.getLastPlayed();
        }
        return last;
    }

    @Override
    public Location getLocation() {
        return BukkitUtil.getLocation(this.player);
    }
    
    @Override
    public UUID getUUID() {
        if (this.uuid == null) {
            this.uuid = UUIDHandler.getUUID(this);
        }
        return this.uuid;
    }
    
    @Override
    public boolean hasPermission(final String perm) {
        if (Settings.PERMISSION_CACHING) {
            if (this.noPerm.contains(perm)) {
                return false;
            }
            if (this.hasPerm.contains(perm)) {
                return true;
            }
            final boolean result = this.player.hasPermission(perm);
            if (!result) {
                this.noPerm.add(perm);
                return false;
            }
            this.hasPerm.add(perm);
            return true;
        }
        return this.player.hasPermission(perm);
    }
    
    @Override
    public void sendMessage(final String message) {
        this.player.sendMessage(message);
    }
    
    @Override
    public void teleport(final Location loc) {
        this.player.teleport(new org.bukkit.Location(BukkitUtil.getWorld(loc.getWorld()), loc.getX(), loc.getY(), loc.getZ()));

    }
    
    @Override
    public boolean isOp() {
        if (this.op != 0) {
            if (this.op == 1) {
                return false;
            }
            return true;
        }
        final boolean result = this.player.isOp();
        if (!result) {
            this.op = 1;
            return false;
        }
        this.op = 2;
        return true;
    }
    
    @Override
    public String getName() {
        if (this.name == null) {
            this.name = this.player.getName();
        }
        return this.name;
    }
    
    @Override
    public boolean isOnline() {
        return this.player.isOnline();
    }
    
    @Override
    public void setCompassTarget(final Location loc) {
        this.player.setCompassTarget(new org.bukkit.Location(BukkitUtil.getWorld(loc.getWorld()), loc.getX(), loc.getY(), loc.getZ()));

    }

    @Override
    public Location getLocationFull() {
        return BukkitUtil.getLocationFull(this.player);
    }

}
