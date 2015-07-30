package com.plotsquared.sponge;

import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

import org.spongepowered.api.entity.player.Player;
import org.spongepowered.api.entity.player.gamemode.GameMode;
import org.spongepowered.api.entity.player.gamemode.GameModes;
import org.spongepowered.api.service.permission.PermissionService;
import org.spongepowered.api.text.Texts;
import org.spongepowered.api.text.chat.ChatTypes;
import org.spongepowered.api.text.title.Title;
import org.spongepowered.api.world.World;

import com.flowpowered.math.vector.Vector3d;
import com.intellectualcrafters.plot.PS;
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
import com.plotsquared.sponge.util.SpongeUtil;

public class SpongePlayer implements PlotPlayer {

    public final Player player;
    private UUID uuid;
    private String name;
    private int op = 0;
    private long last = 0;
    private HashSet<String> hasPerm = new HashSet<>();
    private HashSet<String> noPerm = new HashSet<>();
    
    private HashMap<String, Object> meta;

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
        return (long) (player.getJoinData().getLastPlayed().getSeconds()) * 1000;
    }

    @Override
    public Location getLocation() {
        return SpongeUtil.getLocation(player);
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
        return this.player.hasPermission(perm);
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
    public boolean isOp() {
        if (this.op != 0) {
            return this.op != 1;
        }
        final boolean result = this.player.hasPermission("*");
        if (!result) {
            this.op = 1;
            return false;
        }
        this.op = 2;
        return true;
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
        // TODO set compass target
        throw new UnsupportedOperationException("NOT IMPLEMENTED YET");
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
    public void setMeta(String key, Object value) {
        if (this.meta == null) {
            this.meta = new HashMap<String, Object>();
        }
        this.meta.put(key, value);
    }

    @Override
    public Object getMeta(String key) {
        if (this.meta != null) {
            return this.meta.get(key);
        }
        return null;
    }

    @Override
    public void deleteMeta(String key) {
        if (this.meta != null) {
            this.meta.remove(key);
        }
    }
    
    @Override
    public String toString() {
        return getName();
    }

    @Override
    public void setAttribute(String key) {
        key = "plotsquared_user_attributes." + key;
        EconHandler.manager.setPermission(this, key, true);
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
        EconHandler.manager.setPermission(this, key, false);
    }

    @Override
    public void setWeather(PlotWeather weather) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("NOT IMPLEMENTED YET");
    }

    @Override
    public PlotGamemode getGamemode() {
        // TODO Auto-generated method stub
        GameMode gamemode = player.getGameModeData().getValue();
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
        switch (gamemode) {
            case ADVENTURE:
                player.getGameModeData().setGameMode(GameModes.ADVENTURE);
                return;
            case CREATIVE:
                player.getGameModeData().setGameMode(GameModes.CREATIVE);
                return;
            case SPECTATOR:
                player.getGameModeData().setGameMode(GameModes.SPECTATOR);
                return;
            case SURVIVAL:
                player.getGameModeData().setGameMode(GameModes.SURVIVAL);
                return;
        }
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
