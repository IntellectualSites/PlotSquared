package com.plotsquared.sponge.object;

import com.flowpowered.math.vector.Vector3d;
import com.intellectualcrafters.plot.commands.RequiredType;
import com.intellectualcrafters.plot.config.Settings;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.EconHandler;
import com.intellectualcrafters.plot.util.PlotGamemode;
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
    private UUID uuid;
    private String name;
    private long last = 0;
    public HashSet<String> hasPerm = new HashSet<>();
    public HashSet<String> noPerm = new HashSet<>();
    
    public SpongePlayer(final Player player) {
        this.player = player;
        super.populatePersistentMetaMap();
    }
    
    @Override
    public RequiredType getSuperCaller() {
        return RequiredType.PLAYER;
    }
    
    @Override
    public long getPreviousLogin() {
        if (last != 0) {
            return last;
        }
        final Value<Instant> data = player.getJoinData().lastPlayed();
        if (data.exists()) {
            return last = data.get().getEpochSecond() * 1000;
        }
        return 0;
    }
    
    @Override
    public Location getLocation() {
        final Location loc = super.getLocation();
        return loc == null ? SpongeUtil.getLocation(player) : loc;
    }
    
    @Override
    public Location getLocationFull() {
        return SpongeUtil.getLocationFull(player);
    }
    
    @Override
    public UUID getUUID() {
        if (uuid == null) {
            uuid = UUIDHandler.getUUID(this);
        }
        return uuid;
    }
    
    @Override
    public boolean hasPermission(final String perm) {
        if (Settings.PERMISSION_CACHING) {
            if (noPerm.contains(perm)) {
                return false;
            }
            if (hasPerm.contains(perm)) {
                return true;
            }
            final boolean result = player.hasPermission(perm);
            if (!result) {
                noPerm.add(perm);
                return false;
            }
            hasPerm.add(perm);
            return true;
        }
        return player.hasPermission(perm);
    }
    
    @Override
    public void sendMessage(final String message) {
        player.sendMessage(ChatTypes.CHAT, TextSerializers.LEGACY_FORMATTING_CODE.deserialize(message));
    }
    
    @Override
    public void teleport(final Location loc) {
        if ((Math.abs(loc.getX()) >= 30000000) || (Math.abs(loc.getZ()) >= 30000000)) {
            return;
        }
        final String world = player.getWorld().getName();
        if (!world.equals(loc.getWorld())) {
            player.transferToWorld(loc.getWorld(), new Vector3d(loc.getX(), loc.getY(), loc.getZ()));
        } else {
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
        if (name == null) {
            name = player.getName();
        }
        return name;
    }
    
    @Override
    public void setCompassTarget(final Location loc) {
        final TargetedLocationData target = player.getOrCreate(TargetedLocationData.class).get();
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
        if ((EconHandler.manager == null) || player.hasPermission("plotsquared_user_attributes.*")) {
            setMeta(key, true);
            return;
        }
        EconHandler.manager.setPermission(getName(), key, true);
    }
    
    @Override
    public boolean getAttribute(String key) {
        key = "plotsquared_user_attributes." + key;
        if ((EconHandler.manager == null) || player.hasPermission("plotsquared_user_attributes.*")) {
            final Object v = getMeta(key);
            return v == null ? false : (Boolean) v;
        }
        return player.hasPermission(key);
    }
    
    @Override
    public void removeAttribute(String key) {
        key = "plotsquared_user_attributes." + key;
        EconHandler.manager.setPermission(getName(), key, false);
        deleteMeta(key);
    }
    
    @Override
    public void setWeather(final PlotWeather weather) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("NOT IMPLEMENTED YET");
    }
    
    @Override
    public PlotGamemode getGamemode() {
        final GameMode gamemode = player.getGameModeData().type().get();
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
    public void setGamemode(final PlotGamemode gamemode) {
        switch (gamemode) {
            case ADVENTURE:
                player.offer(Keys.GAME_MODE, GameModes.ADVENTURE);
                return;
            case CREATIVE:
                player.offer(Keys.GAME_MODE, GameModes.CREATIVE);
                return;
            case SPECTATOR:
                player.offer(Keys.GAME_MODE, GameModes.SPECTATOR);
                return;
            case SURVIVAL:
                player.offer(Keys.GAME_MODE, GameModes.SURVIVAL);
                return;
        }
    }
    
    @Override
    public void setTime(final long time) {
        // TODO Auto-generated method stub
        if (time != Long.MAX_VALUE) {} else {}
        throw new UnsupportedOperationException("NOT IMPLEMENTED YET");
    }
    
    @Override
    public void setFlight(final boolean fly) {
        player.offer(Keys.IS_FLYING, fly);
        player.offer(Keys.CAN_FLY, fly);
    }
    
    @Override
    public void playMusic(final Location loc, final int id) {
        switch (id) {
            case 0:
                player.playSound(null, SpongeUtil.getLocation(loc).getPosition(), 1);
                break;
            case 2256:
                player.playSound(SoundTypes.RECORDS_11, SpongeUtil.getLocation(loc).getPosition(), 1);
                break;
            case 2257:
                player.playSound(SoundTypes.RECORDS_13, SpongeUtil.getLocation(loc).getPosition(), 1);
                break;
            case 2258:
                player.playSound(SoundTypes.RECORDS_BLOCKS, SpongeUtil.getLocation(loc).getPosition(), 1);
                break;
            case 2259:
                player.playSound(SoundTypes.RECORDS_CAT, SpongeUtil.getLocation(loc).getPosition(), 1);
                break;
            case 2260:
                player.playSound(SoundTypes.RECORDS_CHIRP, SpongeUtil.getLocation(loc).getPosition(), 1);
                break;
            case 2261:
                player.playSound(SoundTypes.RECORDS_FAR, SpongeUtil.getLocation(loc).getPosition(), 1);
                break;
            case 2262:
                player.playSound(SoundTypes.RECORDS_MALL, SpongeUtil.getLocation(loc).getPosition(), 1);
                break;
            case 2263:
                player.playSound(SoundTypes.RECORDS_MELLOHI, SpongeUtil.getLocation(loc).getPosition(), 1);
                break;
            case 2264:
                player.playSound(SoundTypes.RECORDS_STAL, SpongeUtil.getLocation(loc).getPosition(), 1);
                break;
            case 2265:
                player.playSound(SoundTypes.RECORDS_STRAD, SpongeUtil.getLocation(loc).getPosition(), 1);
                break;
            case 2266:
                player.playSound(SoundTypes.RECORDS_WAIT, SpongeUtil.getLocation(loc).getPosition(), 1);
                break;
            case 2267:
                player.playSound(SoundTypes.RECORDS_WARD, SpongeUtil.getLocation(loc).getPosition(), 1);
                break;
        }
    }
    
    @Override
    public void kick(final String message) {
        player.kick(SpongeUtil.getText(message));
    }
    
    @Override
    public boolean isBanned() {
        BanService service = Sponge.getServiceManager().provide(BanService.class).get();
        return service.isBanned(player.getProfile());
    }
}
