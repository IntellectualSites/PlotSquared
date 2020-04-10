/*
 *       _____  _       _    _____                                _
 *      |  __ \| |     | |  / ____|                              | |
 *      | |__) | | ___ | |_| (___   __ _ _   _  __ _ _ __ ___  __| |
 *      |  ___/| |/ _ \| __|\___ \ / _` | | | |/ _` | '__/ _ \/ _` |
 *      | |    | | (_) | |_ ____) | (_| | |_| | (_| | | |  __/ (_| |
 *      |_|    |_|\___/ \__|_____/ \__, |\__,_|\__,_|_|  \___|\__,_|
 *                                    | |
 *                                    |_|
 *            PlotSquared plot management system for Minecraft
 *                  Copyright (C) 2020 IntellectualSites
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.github.intellectualsites.plotsquared.bukkit.object;

import com.github.intellectualsites.plotsquared.bukkit.util.BukkitUtil;
import com.github.intellectualsites.plotsquared.plot.PlotSquared;
import com.github.intellectualsites.plotsquared.plot.config.Captions;
import com.github.intellectualsites.plotsquared.plot.object.Location;
import com.github.intellectualsites.plotsquared.plot.object.PlotPlayer;
import com.github.intellectualsites.plotsquared.plot.object.TeleportCause;
import com.github.intellectualsites.plotsquared.plot.util.EconHandler;
import com.github.intellectualsites.plotsquared.plot.util.MathMan;
import com.github.intellectualsites.plotsquared.plot.util.PlotWeather;
import com.github.intellectualsites.plotsquared.plot.util.StringMan;
import com.github.intellectualsites.plotsquared.plot.util.UUIDHandler;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.world.item.ItemType;
import com.sk89q.worldedit.world.item.ItemTypes;
import io.papermc.lib.PaperLib;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.WeatherType;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.RegisteredListener;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.sk89q.worldedit.world.gamemode.GameModes.ADVENTURE;
import static com.sk89q.worldedit.world.gamemode.GameModes.CREATIVE;
import static com.sk89q.worldedit.world.gamemode.GameModes.SPECTATOR;
import static com.sk89q.worldedit.world.gamemode.GameModes.SURVIVAL;

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

    @Override public Actor toActor() {
        return BukkitAdapter.adapt(player);
    }

    @NotNull @Override public Location getLocation() {
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

    @Override public void sendMessage(String message) {
        message = message.replace('\u2010', '%')
            .replace('\u2020', '&').replace('\u2030', '&');
        if (!StringMan.isEqual(this.getMeta("lastMessage"), message) || (
            System.currentTimeMillis() - this.<Long>getMeta("lastMessageTime") > 5000)) {
            setMeta("lastMessage", message);
            setMeta("lastMessageTime", System.currentTimeMillis());
            this.player.sendMessage(message);
        }
    }

    @Override public void teleport(@NotNull final Location location, @NotNull final TeleportCause cause) {
        if (Math.abs(location.getX()) >= 30000000 || Math.abs(location.getZ()) >= 30000000) {
            return;
        }
        final org.bukkit.Location bukkitLocation = new org.bukkit.Location(BukkitUtil.getWorld(location.getWorld()), location.getX() + 0.5,
            location.getY(), location.getZ() + 0.5, location.getYaw(), location.getPitch());
        PaperLib.teleportAsync(player, bukkitLocation, getTeleportCause(cause));
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

    @NotNull @Override public com.sk89q.worldedit.world.gamemode.GameMode getGameMode() {
        switch (this.player.getGameMode()) {
            case ADVENTURE:
                return ADVENTURE;
            case CREATIVE:
                return CREATIVE;
            case SPECTATOR:
                return SPECTATOR;
            case SURVIVAL:
            default:
                return SURVIVAL;
        }
    }

    @Override public void setGameMode(@NotNull final com.sk89q.worldedit.world.gamemode.GameMode gameMode) {
        if (ADVENTURE.equals(gameMode)) {
            this.player.setGameMode(GameMode.ADVENTURE);
        } else if (CREATIVE.equals(gameMode)) {
            this.player.setGameMode(GameMode.CREATIVE);
        } else if (SPECTATOR.equals(gameMode)) {
            this.player.setGameMode(GameMode.SPECTATOR);
        } else {
            this.player.setGameMode(GameMode.SURVIVAL);
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

    @Override public void playMusic(@NotNull final Location location, @NotNull final ItemType id) {
        if (id == ItemTypes.AIR) {
            // Let's just stop all the discs because why not?
            for (final Sound sound : Arrays.stream(Sound.values())
                .filter(sound -> sound.name().contains("DISC")).collect(Collectors.toList())) {
                player.stopSound(sound);
            }
            // this.player.playEffect(BukkitUtil.getLocation(location), Effect.RECORD_PLAY, Material.AIR);
        } else {
            // this.player.playEffect(BukkitUtil.getLocation(location), Effect.RECORD_PLAY, id.to(Material.class));
            this.player.playSound(BukkitUtil.getLocation(location),
                Sound.valueOf(BukkitAdapter.adapt(id).name()), Float.MAX_VALUE, 1f);
        }
    }

    @Override public void kick(final String message) {
        this.player.kickPlayer(message);
    }

    @Override public void stopSpectating() {
        if (getGameMode() == SPECTATOR) {
            this.player.setSpectatorTarget(null);
        }
    }

    @Override public boolean isBanned() {
        return this.player.isBanned();
    }


    public PlayerTeleportEvent.TeleportCause getTeleportCause(@NotNull final TeleportCause cause) {
        switch (cause) {
            case COMMAND:
                return PlayerTeleportEvent.TeleportCause.COMMAND;
            case PLUGIN:
                return PlayerTeleportEvent.TeleportCause.PLUGIN;
            default: return PlayerTeleportEvent.TeleportCause.UNKNOWN;
        }
    }
}
