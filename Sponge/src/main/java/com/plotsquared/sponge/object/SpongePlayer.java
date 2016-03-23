package com.plotsquared.sponge.object;

import com.flowpowered.math.vector.Vector3d;
import com.intellectualcrafters.plot.commands.RequiredType;
import com.intellectualcrafters.plot.config.Settings;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.PlotGameMode;
import com.intellectualcrafters.plot.util.PlotWeather;
import com.intellectualcrafters.plot.util.UUIDHandler;
import com.plotsquared.sponge.util.SpongeUtil;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.mutable.TargetedLocationData;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.api.effect.sound.SoundTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.gamemode.GameMode;
import org.spongepowered.api.entity.living.player.gamemode.GameModes;
import org.spongepowered.api.service.ban.BanService;
import org.spongepowered.api.text.chat.ChatTypes;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.time.Instant;
import java.util.HashSet;
import java.util.UUID;

public class SpongePlayer extends PlotPlayer {
    
    public final Player player;
    public HashSet<String> hasPerm = new HashSet<>();
    public HashSet<String> noPerm = new HashSet<>();
    private UUID uuid;
    private String name;
    private long last = 0;

    public SpongePlayer(Player player) {
        this.player = player;
        super.populatePersistentMetaMap();
    }
    
    @Override
    public RequiredType getSuperCaller() {
        return RequiredType.PLAYER;
    }
    
    @Override
    public long getPreviousLogin() {
        if (this.last != 0) {
            return this.last;
        }
        Value<Instant> data = this.player.getJoinData().lastPlayed();
        if (data.exists()) {
            return this.last = data.get().getEpochSecond() * 1000;
        }
        return 0;
    }
    
    @Override
    public Location getLocation() {
        Location location = super.getLocation();
        if (location == null) {
            return SpongeUtil.getLocation(this.player);
        } else {
            return location;
        }
    }
    
    @Override
    public Location getLocationFull() {
        return SpongeUtil.getLocationFull(this.player);
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
        if (Settings.PERMISSION_CACHING) {
            if (this.noPerm.contains(permission)) {
                return false;
            }
            if (this.hasPerm.contains(permission)) {
                return true;
            }
            boolean result = this.player.hasPermission(permission);
            if (!result) {
                this.noPerm.add(permission);
                return false;
            }
            this.hasPerm.add(permission);
            return true;
        }
        return this.player.hasPermission(permission);
    }
    
    @Override
    public void sendMessage(String message) {
        this.player.sendMessage(ChatTypes.CHAT, TextSerializers.LEGACY_FORMATTING_CODE.deserialize(message));
    }
    
    @Override
    public void teleport(Location location) {
        if ((Math.abs(location.getX()) >= 30000000) || (Math.abs(location.getZ()) >= 30000000)) {
            return;
        }
        String world = this.player.getWorld().getName();
        if (!world.equals(location.getWorld())) {
            this.player.transferToWorld(location.getWorld(), new Vector3d(location.getX(), location.getY(), location.getZ()));
        } else {
            org.spongepowered.api.world.Location current = this.player.getLocation();
            current = current.setPosition(new Vector3d(location.getX(), location.getY(), location.getZ()));
            this.player.setLocation(current);
        }
    }
    
    @Override
    public boolean isOnline() {
        return this.player.isOnline();
    }
    
    @Override
    public String getName() {
        if (this.name == null) {
            this.name = this.player.getName();
        }
        return this.name;
    }
    
    @Override
    public void setCompassTarget(Location location) {
        TargetedLocationData target = this.player.getOrCreate(TargetedLocationData.class).get();
        target.set(Keys.TARGETED_LOCATION, SpongeUtil.getLocation(location).getPosition());
    }

    @Override
    public void setWeather(PlotWeather weather) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("NOT IMPLEMENTED YET");
    }
    
    @Override
    public PlotGameMode getGameMode() {
        GameMode gamemode = this.player.getGameModeData().type().get();
        if (gamemode == GameModes.ADVENTURE) {
            return PlotGameMode.ADVENTURE;
        }
        if (gamemode == GameModes.CREATIVE) {
            return PlotGameMode.CREATIVE;
        }
        if (gamemode == GameModes.SPECTATOR) {
            return PlotGameMode.SPECTATOR;
        }
        if (gamemode == GameModes.SURVIVAL) {
            return PlotGameMode.SURVIVAL;
        }
        throw new UnsupportedOperationException("INVALID GAMEMODE");
    }
    
    @Override
    public void setGameMode(PlotGameMode gameMode) {
        switch (gameMode) {
            case ADVENTURE:
                this.player.offer(Keys.GAME_MODE, GameModes.ADVENTURE);
                return;
            case CREATIVE:
                this.player.offer(Keys.GAME_MODE, GameModes.CREATIVE);
                return;
            case SPECTATOR:
                this.player.offer(Keys.GAME_MODE, GameModes.SPECTATOR);
                return;
            case SURVIVAL:
                this.player.offer(Keys.GAME_MODE, GameModes.SURVIVAL);
                return;
        }
    }
    
    @Override
    public void setTime(long time) {
        // TODO Auto-generated method stub
        if (time != Long.MAX_VALUE) {} else {}
        throw new UnsupportedOperationException("NOT IMPLEMENTED YET");
    }
    
    @Override
    public void setFlight(boolean fly) {
        this.player.offer(Keys.IS_FLYING, fly);
        this.player.offer(Keys.CAN_FLY, fly);
    }
    
    @Override
    public void playMusic(Location location, int id) {
        switch (id) {
            case 0:
                this.player.playSound(null, SpongeUtil.getLocation(location).getPosition(), 1);
                break;
            case 2256:
                this.player.playSound(SoundTypes.RECORDS_11, SpongeUtil.getLocation(location).getPosition(), 1);
                break;
            case 2257:
                this.player.playSound(SoundTypes.RECORDS_13, SpongeUtil.getLocation(location).getPosition(), 1);
                break;
            case 2258:
                this.player.playSound(SoundTypes.RECORDS_BLOCKS, SpongeUtil.getLocation(location).getPosition(), 1);
                break;
            case 2259:
                this.player.playSound(SoundTypes.RECORDS_CAT, SpongeUtil.getLocation(location).getPosition(), 1);
                break;
            case 2260:
                this.player.playSound(SoundTypes.RECORDS_CHIRP, SpongeUtil.getLocation(location).getPosition(), 1);
                break;
            case 2261:
                this.player.playSound(SoundTypes.RECORDS_FAR, SpongeUtil.getLocation(location).getPosition(), 1);
                break;
            case 2262:
                this.player.playSound(SoundTypes.RECORDS_MALL, SpongeUtil.getLocation(location).getPosition(), 1);
                break;
            case 2263:
                this.player.playSound(SoundTypes.RECORDS_MELLOHI, SpongeUtil.getLocation(location).getPosition(), 1);
                break;
            case 2264:
                this.player.playSound(SoundTypes.RECORDS_STAL, SpongeUtil.getLocation(location).getPosition(), 1);
                break;
            case 2265:
                this.player.playSound(SoundTypes.RECORDS_STRAD, SpongeUtil.getLocation(location).getPosition(), 1);
                break;
            case 2266:
                this.player.playSound(SoundTypes.RECORDS_WAIT, SpongeUtil.getLocation(location).getPosition(), 1);
                break;
            case 2267:
                this.player.playSound(SoundTypes.RECORDS_WARD, SpongeUtil.getLocation(location).getPosition(), 1);
                break;
        }
    }
    
    @Override
    public void kick(String message) {
        this.player.kick(SpongeUtil.getText(message));
    }

    @Override public void stopSpectating() {
        //Not Implemented
    }

    @Override
    public boolean isBanned() {
        BanService service = Sponge.getServiceManager().provide(BanService.class).get();
        return service.isBanned(this.player.getProfile());
    }
}
