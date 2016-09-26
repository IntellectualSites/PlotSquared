package com.plotsquared.nukkit.object;

import cn.nukkit.Player;
import cn.nukkit.event.player.PlayerTeleportEvent;
import cn.nukkit.plugin.RegisteredListener;
import cn.nukkit.utils.EventException;
import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.EconHandler;
import com.intellectualcrafters.plot.util.PlotGameMode;
import com.intellectualcrafters.plot.util.PlotWeather;
import com.intellectualcrafters.plot.util.StringMan;
import com.intellectualcrafters.plot.util.UUIDHandler;
import com.plotsquared.nukkit.util.NukkitUtil;
import java.util.UUID;

public class NukkitPlayer extends PlotPlayer {
    
    public final Player player;
    public boolean offline;
    private UUID uuid;
    private String name;

    /**
     * <p>Please do not use this method. Instead use
     * NukkitUtil.getPlayer(Player), as it caches player objects.</p>
     * @param player
     */
    public NukkitPlayer(Player player) {
        this.player = player;
        super.populatePersistentMetaMap();
    }

    public NukkitPlayer(Player player, boolean offline) {
        this.player = player;
        this.offline = offline;
        super.populatePersistentMetaMap();
    }

    @Override
    public Location getLocation() {
        Location location = super.getLocation();
        return location == null ? NukkitUtil.getLocation(this.player) : location;
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
        cn.nukkit.level.Location to = NukkitUtil.getLocation(loc);
        cn.nukkit.level.Location from = player.getLocation();
        PlayerTeleportEvent event = new PlayerTeleportEvent(player, from, to, PlayerTeleportEvent.TeleportCause.PLUGIN);
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
        event = new PlayerTeleportEvent(player, to, from, PlayerTeleportEvent.TeleportCause.PLUGIN);
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
    public void sendMessage(String message) {
        if (!StringMan.isEqual(this.<String>getMeta("lastMessage"), message) || (System.currentTimeMillis() - this.<Long>getMeta("lastMessageTime") > 5000)) {
            setMeta("lastMessage", message);
            setMeta("lastMessageTime", System.currentTimeMillis());
            this.player.sendMessage(message);
        }
    }
    
    @Override
    public void teleport(Location to) {
        if (Math.abs(to.getX()) >= 30000000 || Math.abs(to.getZ()) >= 30000000) {
            return;
        }
        cn.nukkit.level.Location loc = new cn.nukkit.level.Location(to.getX() + 0.5, to.getY(), to.getZ() + 0.5, to.getYaw(), to.getPitch(), NukkitUtil.getWorld(to.getWorld()));
        this.player.teleport(loc);
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
        throw new UnsupportedOperationException("Not implemented yet: setCompassTarget");
    }
    
    @Override
    public Location getLocationFull() {
        return NukkitUtil.getLocationFull(this.player);
    }

    @Override
    public void setWeather(PlotWeather weather) {
        throw new UnsupportedOperationException("Not implemented yet: setWeather");
    }
    
    @Override
    public PlotGameMode getGameMode() {
        switch (this.player.getGamemode()) {
            case 0:
                return PlotGameMode.SURVIVAL;
            case 1:
                return PlotGameMode.CREATIVE;
            case 2:
                return PlotGameMode.ADVENTURE;
            case 3:
                return PlotGameMode.SPECTATOR;
            default:
                return PlotGameMode.NOT_SET;
        }
    }
    
    @Override
    public void setGameMode(PlotGameMode gameMode) {
        switch (gameMode) {
            case ADVENTURE:
                this.player.setGamemode(2);
                break;
            case CREATIVE:
                this.player.setGamemode(1);
                break;
            case SPECTATOR:
                this.player.setGamemode(3);
                break;
            case SURVIVAL:
                this.player.setGamemode(0);
                break;
            default:
                this.player.setGamemode(0);
                break;
        }
    }
    
    @Override
    public void setTime(long time) {
        throw new UnsupportedOperationException("Not implemented yet: setTIme");
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
        throw new UnsupportedOperationException("Not implemented yet: playMusic");
    }
    
    @Override
    public void kick(String message) {
        player.kick(message);
    }

    @Override public void stopSpectating() {
        // Do nothing
    }

    @Override
    public boolean isBanned() {
        return this.player.isBanned();
    }
}
