package com.intellectualcrafters.plot.object;

import java.util.UUID;

import org.bukkit.entity.Player;

import com.intellectualcrafters.plot.util.bukkit.BukkitUtil;
import com.intellectualcrafters.plot.util.bukkit.UUIDHandler;

public class BukkitPlayer implements PlotPlayer {

    public final Player player;
    UUID uuid;
    String name;
    
    
    public BukkitPlayer(Player player, String name, UUID uuid) {
        this.player = player;
        this.name = name;
        this.uuid = uuid;
    }
    
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
        return player.hasPermission(perm);
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
        return this.player.isOp();
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
