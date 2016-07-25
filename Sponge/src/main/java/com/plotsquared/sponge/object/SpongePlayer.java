package com.plotsquared.sponge.object;

import com.flowpowered.math.vector.Vector3d;
import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.commands.RequiredType;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.PlotGameMode;
import com.intellectualcrafters.plot.util.PlotWeather;
import com.intellectualcrafters.plot.util.StringMan;
import com.intellectualcrafters.plot.util.UUIDHandler;
import com.plotsquared.sponge.util.SpongeUtil;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.mutable.TargetedLocationData;
import org.spongepowered.api.effect.sound.SoundTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.gamemode.GameMode;
import org.spongepowered.api.entity.living.player.gamemode.GameModes;
import org.spongepowered.api.service.ban.BanService;
import org.spongepowered.api.text.chat.ChatTypes;
import org.spongepowered.api.text.serializer.TextSerializers;
import org.spongepowered.api.world.World;

import java.util.Optional;
import java.util.UUID;

public class SpongePlayer extends PlotPlayer {
    
    public final Player player;
    private UUID uuid;
    private String name;

    public SpongePlayer(Player player) {
        this.player = player;
        super.populatePersistentMetaMap();
    }
    
    @Override
    public RequiredType getSuperCaller() {
        return RequiredType.PLAYER;
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

    @Override public long getLastPlayed() {
        return this.player.lastPlayed().get().toEpochMilli();
    }

    @Override
    public boolean hasPermission(String permission) {
        return this.player.hasPermission(permission);
    }
    
    @Override
    public void sendMessage(String message) {
        if (!StringMan.isEqual(this.getMeta("lastMessage"), message) || (System.currentTimeMillis() - this.<Long>getMeta("lastMessageTime") > 5000)) {
            setMeta("lastMessage", message);
            setMeta("lastMessageTime", System.currentTimeMillis());
            this.player.sendMessage(ChatTypes.CHAT, TextSerializers.LEGACY_FORMATTING_CODE.deserialize(message));
        }
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
            org.spongepowered.api.world.Location<World> current = this.player.getLocation();
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
        Optional<TargetedLocationData> target = this.player.getOrCreate(TargetedLocationData.class);
        if (target.isPresent()) {
            target.get().set(Keys.TARGETED_LOCATION, SpongeUtil.getLocation(location).getPosition());
        } else {
            PS.debug("Failed to set compass target.");
        }
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
        } else if (gamemode == GameModes.CREATIVE) {
            return PlotGameMode.CREATIVE;
        } else if (gamemode == GameModes.SPECTATOR) {
            return PlotGameMode.SPECTATOR;
        } else if (gamemode == GameModes.SURVIVAL) {
            return PlotGameMode.SURVIVAL;
        } else {
            return PlotGameMode.NOT_SET;
        }
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
            case NOT_SET:
                this.player.offer(Keys.GAME_MODE, GameModes.NOT_SET);
                return;
            default:
                this.player.offer(Keys.GAME_MODE, GameModes.NOT_SET);
        }
    }
    
    @Override
    public void setTime(long time) {
        throw new UnsupportedOperationException("NOT IMPLEMENTED YET");
    }
    
    @Override
    public boolean getFlight() {
        Optional<Boolean> flying = player.get(Keys.CAN_FLY);
        return flying.isPresent() && flying.get();
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
                this.player.playSound(SoundTypes.RECORD_11, SpongeUtil.getLocation(location).getPosition(), 1);
                break;
            case 2257:
                this.player.playSound(SoundTypes.RECORD_13, SpongeUtil.getLocation(location).getPosition(), 1);
                break;
            case 2258:
                this.player.playSound(SoundTypes.RECORD_BLOCKS, SpongeUtil.getLocation(location).getPosition(), 1);
                break;
            case 2259:
                this.player.playSound(SoundTypes.RECORD_CAT, SpongeUtil.getLocation(location).getPosition(), 1);
                break;
            case 2260:
                this.player.playSound(SoundTypes.RECORD_CHIRP, SpongeUtil.getLocation(location).getPosition(), 1);
                break;
            case 2261:
                this.player.playSound(SoundTypes.RECORD_FAR, SpongeUtil.getLocation(location).getPosition(), 1);
                break;
            case 2262:
                this.player.playSound(SoundTypes.RECORD_MALL, SpongeUtil.getLocation(location).getPosition(), 1);
                break;
            case 2263:
                this.player.playSound(SoundTypes.RECORD_MELLOHI, SpongeUtil.getLocation(location).getPosition(), 1);
                break;
            case 2264:
                this.player.playSound(SoundTypes.RECORD_STAL, SpongeUtil.getLocation(location).getPosition(), 1);
                break;
            case 2265:
                this.player.playSound(SoundTypes.RECORD_STRAD, SpongeUtil.getLocation(location).getPosition(), 1);
                break;
            case 2266:
                this.player.playSound(SoundTypes.RECORD_WAIT, SpongeUtil.getLocation(location).getPosition(), 1);
                break;
            case 2267:
                this.player.playSound(SoundTypes.RECORD_WARD, SpongeUtil.getLocation(location).getPosition(), 1);
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
        Optional<BanService> service = Sponge.getServiceManager().provide(BanService.class);
        return service.isPresent() && service.get().isBanned(this.player.getProfile());
    }
}
