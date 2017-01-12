package com.plotsquared.bukkit.object;

import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.EconHandler;
import com.intellectualcrafters.plot.util.PlotGameMode;
import com.intellectualcrafters.plot.util.PlotWeather;
import com.intellectualcrafters.plot.util.StringMan;
import com.intellectualcrafters.plot.util.UUIDHandler;
import com.plotsquared.bukkit.util.BukkitUtil;
import org.bukkit.Effect;
import org.bukkit.GameMode;
import org.bukkit.WeatherType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventException;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

import java.util.UUID;
import org.bukkit.plugin.RegisteredListener;

public class BukkitPlayer extends PlotPlayer {
    
    public final Player player;
    public boolean offline;
    private UUID uuid;
    private String name;

    /**
     * <p>Please do not use this method. Instead use
     * BukkitUtil.getPlayer(Player), as it caches player objects.</p>
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

    @Override public long getLastPlayed() {
        return this.player.getLastPlayed();
    }

    @Override
    public boolean canTeleport(Location loc) {
        org.bukkit.Location to = BukkitUtil.getLocation(loc);
        org.bukkit.Location from = player.getLocation();
        PlayerTeleportEvent event = new PlayerTeleportEvent(player, from, to);
        RegisteredListener[] listeners = event.getHandlers().getRegisteredListeners();
        for (RegisteredListener listener : listeners) {
            if (listener.getPlugin().getName().equals(PS.imp().getPluginName())) {
                continue;
            }
            try {
                listener.callEvent(event);
            } catch (EventException e) {
                e.printStackTrace();
            }
        }
        if (event.isCancelled() || !event.getTo().equals(to)) {
            return false;
        }
        event = new PlayerTeleportEvent(player, to, from);
        for (RegisteredListener listener : listeners) {
            if (listener.getPlugin().getName().equals(PS.imp().getPluginName())) {
                continue;
            }
            try {
                listener.callEvent(event);
            } catch (EventException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    @Override
    public boolean hasPermission(String permission) {
        if (this.offline && EconHandler.manager != null) {
            return EconHandler.manager.hasPermission(getName(), permission);
        }
        return this.player.hasPermission(permission);
    }

    @Override
    public boolean isPermissionSet(String permission) {
        return this.player.isPermissionSet(permission);
    }

    @Override
    public void sendMessage(String message) {
        if (!StringMan.isEqual(this.<String>getMeta("lastMessage"), message) || (System.currentTimeMillis() - this.<Long>getMeta("lastMessageTime") > 5000)) {
            setMeta("lastMessage", message);
            setMeta("lastMessageTime", System.currentTimeMillis());
            this.player.sendMessage(message);
        }
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
                return PlotGameMode.NOT_SET;
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
    public boolean getFlight() {
        return player.getAllowFlight();
    }

    @Override
    public void playMusic(Location location, int id) {
        //noinspection deprecation
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
