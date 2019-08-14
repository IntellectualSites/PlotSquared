package com.github.intellectualsites.plotsquared.bukkit.object;

import com.github.intellectualsites.plotsquared.bukkit.util.BukkitUtil;
import com.github.intellectualsites.plotsquared.plot.PlotSquared;
import com.github.intellectualsites.plotsquared.plot.config.Captions;
import com.github.intellectualsites.plotsquared.plot.object.Location;
import com.github.intellectualsites.plotsquared.plot.object.PlotBlock;
import com.github.intellectualsites.plotsquared.plot.object.PlotPlayer;
import com.github.intellectualsites.plotsquared.plot.util.EconHandler;
import com.github.intellectualsites.plotsquared.plot.util.MathMan;
import com.github.intellectualsites.plotsquared.plot.util.PlotGameMode;
import com.github.intellectualsites.plotsquared.plot.util.PlotWeather;
import com.github.intellectualsites.plotsquared.plot.util.StringMan;
import com.github.intellectualsites.plotsquared.plot.util.UUIDHandler;
import io.papermc.lib.PaperLib;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.WeatherType;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.RegisteredListener;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class BukkitPlayer extends PlotPlayer {

    private static boolean CHECK_EFFECTIVE = true;
    public final Player player;
    private boolean offline;
    private UUID uuid;
    private String name;

    /**
     * <p>Please do not use this method. Instead use
     * BukkitUtil.getPlayer(Player), as it caches player objects.</p>
     *
     * @param player Bukkit player instance
     */
    public BukkitPlayer(@NotNull final Player player) {
        this.player = player;
        super.populatePersistentMetaMap();
    }

    public BukkitPlayer(@NotNull final Player player, final boolean offline) {
        this.player = player;
        this.offline = offline;
        super.populatePersistentMetaMap();
    }

    @Override public Location getLocation() {
        final Location location = super.getLocation();
        return location == null ? BukkitUtil.getLocation(this.player) : location;
    }

    @NotNull @Override public UUID getUUID() {
        if (this.uuid == null) {
            this.uuid = UUIDHandler.getUUID(this);
        }
        return this.uuid;
    }

    @Override public long getLastPlayed() {
        return this.player.getLastPlayed();
    }

    @Override public boolean canTeleport(@NotNull final Location location) {
        final org.bukkit.Location to = BukkitUtil.getLocation(location);
        final org.bukkit.Location from = player.getLocation();
        PlayerTeleportEvent event = new PlayerTeleportEvent(player, from, to);
        callEvent(event);
        if (event.isCancelled() || !event.getTo().equals(to)) {
            return false;
        }
        event = new PlayerTeleportEvent(player, to, from);
        callEvent(event);
        return true;
    }

    @Override
    public void sendTitle(String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        player.sendTitle(title, subtitle, fadeIn, stay, fadeOut);
    }

    private void callEvent(@NotNull final Event event) {
        final RegisteredListener[] listeners = event.getHandlers().getRegisteredListeners();
        for (final RegisteredListener listener : listeners) {
            if (listener.getPlugin().getName().equals(PlotSquared.imp().getPluginName())) {
                continue;
            }
            try {
                listener.callEvent(event);
            } catch (final EventException e) {
                e.printStackTrace();
            }
        }
    }

    @Override public boolean hasPermission(final String permission) {
        if (this.offline && EconHandler.manager != null) {
            return EconHandler.manager.hasPermission(getName(), permission);
        }
        return this.player.hasPermission(permission);
    }

    @Override public int hasPermissionRange(final String stub, final int range) {
        if (hasPermission(Captions.PERMISSION_ADMIN.getTranslated())) {
            return Integer.MAX_VALUE;
        }
        final String[] nodes = stub.split("\\.");
        final StringBuilder n = new StringBuilder();
        for (int i = 0; i < (nodes.length - 1); i++) {
            n.append(nodes[i]).append(".");
            if (!stub.equals(n + Captions.PERMISSION_STAR.getTranslated())) {
                if (hasPermission(n + Captions.PERMISSION_STAR.getTranslated())) {
                    return Integer.MAX_VALUE;
                }
            }
        }
        if (hasPermission(stub + ".*")) {
            return Integer.MAX_VALUE;
        }
        int max = 0;
        if (CHECK_EFFECTIVE) {
            boolean hasAny = false;
            String stubPlus = stub + ".";
        final Set<PermissionAttachmentInfo> effective = player.getEffectivePermissions();
            if (!effective.isEmpty()) {
                for (PermissionAttachmentInfo attach : effective) {
                    String permStr = attach.getPermission();
                    if (permStr.startsWith(stubPlus)) {
                        hasAny = true;
                        String end = permStr.substring(stubPlus.length());
                        if (MathMan.isInteger(end)) {
                            int val = Integer.parseInt(end);
                            if (val > range) {
                                return val;
                            }
                            if (val > max) {
                                max = val;
                            }
                        }
                    }
                }
                if (hasAny) {
                    return max;
                }
                // Workaround
                for (PermissionAttachmentInfo attach : effective) {
                    String permStr = attach.getPermission();
                    if (permStr.startsWith("plots.") && !permStr.equals("plots.use")) {
                        return max;
                    }
                }
                CHECK_EFFECTIVE = false;
            }
        }
        for (int i = range; i > 0; i--) {
            if (hasPermission(stub + "." + i)) {
                return i;
            }
        }
        return max;
    }

    @Override public boolean isPermissionSet(final String permission) {
        return this.player.isPermissionSet(permission);
    }

    @Override public void sendMessage(final String message) {
        if (!StringMan.isEqual(this.getMeta("lastMessage"), message) || (
            System.currentTimeMillis() - this.<Long>getMeta("lastMessageTime") > 5000)) {
            setMeta("lastMessage", message);
            setMeta("lastMessageTime", System.currentTimeMillis());
            this.player.sendMessage(message);
        }
    }

    @Override public void teleport(@NotNull final Location location) {
        if (Math.abs(location.getX()) >= 30000000 || Math.abs(location.getZ()) >= 30000000) {
            return;
        }
        final org.bukkit.Location bukkitLocation = new org.bukkit.Location(BukkitUtil.getWorld(location.getWorld()), location.getX() + 0.5,
            location.getY(), location.getZ() + 0.5, location.getYaw(), location.getPitch());
        PaperLib.teleportAsync(player, bukkitLocation);
    }

    @Override public String getName() {
        if (this.name == null) {
            this.name = this.player.getName();
        }
        return this.name;
    }

    @Override public boolean isOnline() {
        return !this.offline && this.player.isOnline();
    }

    @Override public void setCompassTarget(Location location) {
        this.player.setCompassTarget(
            new org.bukkit.Location(BukkitUtil.getWorld(location.getWorld()), location.getX(),
                location.getY(), location.getZ()));
    }

    @Override public Location getLocationFull() {
        return BukkitUtil.getLocationFull(this.player);
    }

    @Override public void setWeather(@NotNull final PlotWeather weather) {
        switch (weather) {
            case CLEAR:
                this.player.setPlayerWeather(WeatherType.CLEAR);
                break;
            case RAIN:
                this.player.setPlayerWeather(WeatherType.DOWNFALL);
                break;
            case RESET:
            default:
                this.player.resetPlayerWeather();
                break;
        }
    }

    @NotNull @Override public PlotGameMode getGameMode() {
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

    @Override public void setGameMode(@NotNull final PlotGameMode gameMode) {
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
            default:
                this.player.setGameMode(GameMode.SURVIVAL);
                break;
        }
    }

    @Override public void setTime(final long time) {
        if (time != Long.MAX_VALUE) {
            this.player.setPlayerTime(time, false);
        } else {
            this.player.resetPlayerTime();
        }
    }

    @Override public boolean getFlight() {
        return player.getAllowFlight();
    }

    @Override public void setFlight(boolean fly) {
        this.player.setAllowFlight(fly);
    }

    @Override public void playMusic(@NotNull final Location location, @NotNull final PlotBlock id) {
        if (PlotBlock.isEverything(id) || id.isAir()) {
            // Let's just stop all the discs because why not?
            for (final Sound sound : Arrays.stream(Sound.values())
                .filter(sound -> sound.name().contains("DISC")).collect(Collectors.toList())) {
                player.stopSound(sound);
            }
            // this.player.playEffect(BukkitUtil.getLocation(location), Effect.RECORD_PLAY, Material.AIR);
        } else {
            // this.player.playEffect(BukkitUtil.getLocation(location), Effect.RECORD_PLAY, id.to(Material.class));
            this.player.playSound(BukkitUtil.getLocation(location),
                Sound.valueOf(id.to(Material.class).name()), Float.MAX_VALUE, 1f);
        }
    }

    @Override public void kick(final String message) {
        this.player.kickPlayer(message);
    }

    @Override public void stopSpectating() {
        if (getGameMode() == PlotGameMode.SPECTATOR) {
            this.player.setSpectatorTarget(null);
        }
    }

    @Override public boolean isBanned() {
        return this.player.isBanned();
    }
}
