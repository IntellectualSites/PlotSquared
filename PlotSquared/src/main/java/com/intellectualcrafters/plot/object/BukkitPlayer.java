package com.intellectualcrafters.plot.object;

import java.util.HashSet;
import java.util.UUID;

import org.bukkit.entity.Player;

import com.intellectualcrafters.plot.util.bukkit.BukkitUtil;
import com.intellectualcrafters.plot.util.bukkit.UUIDHandler;

public class BukkitPlayer implements PlotPlayer {

    public final Player player;
    UUID uuid;
    String name;
    private HashSet<String> hasPerm;
    private HashSet<String> noPerm;
    private int op = 0;
    
    /**
     * Please do not use this method. Instead use BukkitUtil.getPlayer(Player), as it caches player objects.
     * @param player
     */
    public BukkitPlayer(Player player) {
        this.player = player;
    }
    
    @Override
    public Location getLocation() {
        return BukkitUtil.getLocation(this.player);
    }

    @Override
    public UUID getUUID() {
        if (this.uuid == null) {
            this.uuid = UUIDHandler.getUUID(this.player);
        }
        return this.uuid;
    }

    @Override
    public boolean hasPermission(String perm) {
        if (noPerm.contains(perm)) {
            return false;
        }
        if (hasPerm.contains(perm)) {
            return true;
        }
        boolean result = player.hasPermission(perm);
        if (!result) {
            noPerm.add(perm);
            return false;
        }
        hasPerm.add(perm);
        return true;
    }

    @Override
    public void sendMessage(String message) {
        this.player.sendMessage(message);
    }

    @Override
    public void teleport(Location loc) {
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
        boolean result = this.player.isOp();
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
            this.name = player.getName();
        }
        return this.name;
    }

    @Override
    public boolean isOnline() {
        return this.player.isOnline();
    }
    
}
