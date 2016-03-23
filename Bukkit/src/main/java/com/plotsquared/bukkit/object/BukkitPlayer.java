package com.plotsquared.bukkit.object;

import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.EconHandler;
import com.intellectualcrafters.plot.util.PlotGameMode;
import com.intellectualcrafters.plot.util.PlotWeather;
import com.intellectualcrafters.plot.util.UUIDHandler;
import com.plotsquared.bukkit.util.BukkitUtil;
import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.GameMode;
import org.bukkit.WeatherType;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.PluginManager;

import java.util.UUID;

public class BukkitPlayer extends PlotPlayer {
    
    public final Player player;
    public boolean offline;
    private UUID uuid;
    private String name;
    private long last = 0;
    
    /**
     * <p>Please do not use this method. Instead use BukkitUtil.getPlayer(Player),
     * as it caches player objects.</p>
     * @param player
     */
    public BukkitPlayer(Player player) {
        this.player = player;
        super.populatePersistentMetaMap();
    }

    public BukkitPlayer(Player player, boolean offline) {
        this.player = player;
        this.offline = offline;
        super.populatePersistentMetaMap();
    }
    
    @Override
    public long getPreviousLogin() {
        if (this.last == 0) {
            this.last = this.player.getLastPlayed();
        }
        return this.last;
    }
    
    @Override
    public Location getLocation() {
        Location location = super.getLocation();
        return location == null ? BukkitUtil.getLocation(this.player) : location;
    }
    
    @Override
    public UUID getUUID() {
        if (this.uuid == null) {
            this.uuid = UUIDHandler.getUUID(this);
        }
        return this.uuid;
    }
    
    @Override
    public boolean hasPermission(String permission) {
        if (this.offline && EconHandler.manager != null) {
            return EconHandler.manager.hasPermission(getName(), permission);
        }
        return this.player.hasPermission(permission);
    }

    public Permission getPermission(String node) {
        PluginManager manager = Bukkit.getPluginManager();
        Permission perm = manager.getPermission(node);
        if (perm == null) {
            String[] nodes = node.split("\\.");
            perm = new Permission(node);
            StringBuilder n = new StringBuilder();
            for (int i = 0; i < nodes.length - 1; i++) {
                n.append(nodes[i]).append(".");
                if (!node.equals(n + C.PERMISSION_STAR.s())) {
                    Permission parent = getPermission(n + C.PERMISSION_STAR.s());
                    if (parent != null) {
                        perm.addParent(parent, true);
                    }
                }
            }
            manager.addPermission(perm);
        }
        manager.recalculatePermissionDefaults(perm);
        perm.recalculatePermissibles();
        return perm;
    }
    
    @Override
    public void sendMessage(String message) {
        this.player.sendMessage(message);
    }
    
    @Override
    public void teleport(Location location) {
        if (Math.abs(location.getX()) >= 30000000 || Math.abs(location.getZ()) >= 30000000) {
            return;
        }
        this.player.teleport(
                new org.bukkit.Location(BukkitUtil.getWorld(location.getWorld()), location.getX() + 0.5, location.getY(), location.getZ() + 0.5,
                        location.getYaw(), location.getPitch()), TeleportCause.COMMAND);
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
        return !this.offline && this.player.isOnline();
    }
    
    @Override
    public void setCompassTarget(Location location) {
        this.player.setCompassTarget(
                new org.bukkit.Location(BukkitUtil.getWorld(location.getWorld()), location.getX(), location.getY(), location.getZ()));
        
    }
    
    @Override
    public Location getLocationFull() {
        return BukkitUtil.getLocationFull(this.player);
    }

    @Override
    public void setWeather(PlotWeather weather) {
        switch (weather) {
            case CLEAR:
                this.player.setPlayerWeather(WeatherType.CLEAR);
                break;
            case RAIN:
                this.player.setPlayerWeather(WeatherType.DOWNFALL);
                break;
            case RESET:
                this.player.resetPlayerWeather();
                break;
            default:
                this.player.resetPlayerWeather();
                break;
        }
    }
    
    @Override
    public PlotGameMode getGameMode() {
        switch (this.player.getGameMode()) {
            case ADVENTURE:
                return PlotGameMode.ADVENTURE;
            case CREATIVE:
                return PlotGameMode.CREATIVE;
            case SPECTATOR:
                return PlotGameMode.SPECTATOR;
            case SURVIVAL:
                return PlotGameMode.SURVIVAL;
            default:
                return PlotGameMode.SURVIVAL;
        }
    }
    
    @Override
    public void setGameMode(PlotGameMode gameMode) {
        switch (gameMode) {
            case ADVENTURE:
                this.player.setGameMode(GameMode.ADVENTURE);
                break;
            case CREATIVE:
                this.player.setGameMode(GameMode.CREATIVE);
                break;
            case SPECTATOR:
                this.player.setGameMode(GameMode.SPECTATOR);
                break;
            case SURVIVAL:
                this.player.setGameMode(GameMode.SURVIVAL);
                break;
            default:
                this.player.setGameMode(GameMode.SURVIVAL);
                break;
        }
    }
    
    @Override
    public void setTime(long time) {
        if (time != Long.MAX_VALUE) {
            this.player.setPlayerTime(time, false);
        } else {
            this.player.resetPlayerTime();
        }
    }
    
    @Override
    public void setFlight(boolean fly) {
        this.player.setAllowFlight(fly);
    }
    
    @Override
    public void playMusic(Location location, int id) {
        this.player.playEffect(BukkitUtil.getLocation(location), Effect.RECORD_PLAY, id);
    }
    
    @Override
    public void kick(String message) {
        this.player.kickPlayer(message);
    }

    @Override public void stopSpectating() {
        if (getGameMode() == PlotGameMode.SPECTATOR) {
            this.player.setSpectatorTarget(null);
        }
    }

    @Override
    public boolean isBanned() {
        return this.player.isBanned();
    }
}
