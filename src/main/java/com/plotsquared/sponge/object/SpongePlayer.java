package com.plotsquared.sponge.object;

import java.util.Date;
import java.util.HashSet;
import java.util.UUID;

import org.spongepowered.api.entity.player.Player;
import org.spongepowered.api.entity.player.gamemode.GameMode;
import org.spongepowered.api.entity.player.gamemode.GameModes;
import org.spongepowered.api.text.chat.ChatTypes;

import com.flowpowered.math.vector.Vector3d;
import com.intellectualcrafters.plot.commands.RequiredType;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.config.Settings;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.EconHandler;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.PlotGamemode;
import com.intellectualcrafters.plot.util.PlotWeather;
import com.intellectualcrafters.plot.util.UUIDHandler;
import com.plotsquared.sponge.SpongeMain;
import com.plotsquared.sponge.util.SpongeUtil;

public class SpongePlayer extends PlotPlayer {

    public final Player player;
    private UUID uuid;
    private String name;
    private long last = 0;
    public HashSet<String> hasPerm = new HashSet<>();
    public HashSet<String> noPerm = new HashSet<>();

    public SpongePlayer(Player player) {
        this.player = player;
    }
    
    @Override
    public void sendMessage(C c, String... args) {
        MainUtil.sendMessage(this, c, args);
    }

    @Override
    public RequiredType getSuperCaller() {
        return RequiredType.PLAYER;
    }

    @Override
    public long getPreviousLogin() {
        Value<Date> data = player.getJoinData().lastPlayed();
        if (data.exists()) {
            return last = data.get().getSeconds() * 1000;
        }
        return 0;
    }

    @Override
    public Location getLocation() {
        Location loc = super.getLocation();
        return loc == null ? SpongeUtil.getLocation(this.player) : loc; 
    }

    @Override
    public Location getLocationFull() {
        return SpongeUtil.getLocationFull(player);
    }

    @Override
    public UUID getUUID() {
        if (this.uuid == null) {
            this.uuid = UUIDHandler.getUUID(this);
        }
        return uuid;
    }

    @Override
    public boolean hasPermission(String perm) {
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
        boolean value = this.player.hasPermission(perm);
        
        // TODO check children
        
        return value;
    }

    @Override
    public void sendMessage(String message) {
        player.sendMessage(ChatTypes.CHAT, message);
    }

    @Override
    public void teleport(Location loc) {
        String world = player.getWorld().getName();
        if (!world.equals(loc.getWorld())) {
            player.transferToWorld(loc.getWorld(), new Vector3d(loc.getX(), loc.getY(), loc.getZ()));
        }
        else {
            org.spongepowered.api.world.Location current = player.getLocation();
            current = current.setPosition(new Vector3d(loc.getX(), loc.getY(), loc.getZ()));
            player.setLocation(current);
        }
    }

    @Override
    public boolean isOnline() {
        return player.isOnline();
    }

    @Override
    public String getName() {
        if (this.name == null) {
            this.name = this.player.getName();
        }
        return this.name;
    }

    @Override
    public void setCompassTarget(Location loc) {
        TargetedLocationData target = player.getOrCreate(TargetedLocationData.class).get();
        target.set(Keys.TARGETED_LOCATION, SpongeUtil.getLocation(loc));
    }

    @Override
    public void loadData() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("NOT IMPLEMENTED YET");
    }

    @Override
    public void saveData() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("NOT IMPLEMENTED YET");
    }

    @Override
    public void setAttribute(String key) {
        key = "plotsquared_user_attributes." + key;
        EconHandler.manager.setPermission(getName(), key, true);
    }

    @Override
    public boolean getAttribute(String key) {
        key = "plotsquared_user_attributes." + key;
        
        // TODO register attributes
        
        return false;
    }

    @Override
    public void removeAttribute(String key) {
        key = "plotsquared_user_attributes." + key;
        EconHandler.manager.setPermission(getName(), key, false);
    }

    @Override
    public void setWeather(PlotWeather weather) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("NOT IMPLEMENTED YET");
    }

    @Override
    public PlotGamemode getGamemode() {
        GameMode gamemode = player.getGameModeData().type().get();
        if (gamemode == GameModes.ADVENTURE) {
            return PlotGamemode.ADVENTURE;
        }
        if (gamemode == GameModes.CREATIVE) {
            return PlotGamemode.CREATIVE;
        }
        if (gamemode == GameModes.SPECTATOR) {
            return PlotGamemode.SPECTATOR;
        }
        if (gamemode == GameModes.SURVIVAL) {
            return PlotGamemode.SURVIVAL;
        }
        throw new UnsupportedOperationException("INVALID GAMEMODE");
    }

    @Override
    public void setGamemode(PlotGamemode gamemode) {
        // TODO Auto-generated method stub
//        switch (gamemode) {
//            case ADVENTURE:
//                player.offer(Keys.GAME_MODE, GameModes.ADVENTURE);
//                return;
//            case CREATIVE:
//                player.offer(Keys.GAME_MODE, GameModes.CREATIVE);
//                return;
//            case SPECTATOR:
//                player.offer(Keys.GAME_MODE, GameModes.SPECTATOR);
//                return;
//            case SURVIVAL:
//                player.offer(Keys.GAME_MODE, GameModes.SURVIVAL);
//                return;
//        }
    }

    @Override
    public void setTime(long time) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("NOT IMPLEMENTED YET");
    }

    @Override
    public void setFlight(boolean fly) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("NOT IMPLEMENTED YET");
    }

    @Override
    public void playMusic(Location loc, int id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("NOT IMPLEMENTED YET");
    }

    @Override
    public void kick(String message) {
        player.kick(SpongeMain.THIS.getText(message));
    }
}
