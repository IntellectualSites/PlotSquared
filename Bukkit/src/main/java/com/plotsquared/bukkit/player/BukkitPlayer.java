/*
 * PlotSquared, a land and world management plugin for Minecraft.
 * Copyright (C) IntellectualSites <https://intellectualsites.com>
 * Copyright (C) IntellectualSites team and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.plotsquared.bukkit.player;

import com.google.common.base.Charsets;
import com.plotsquared.bukkit.util.BukkitUtil;
import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.configuration.Settings;
import com.plotsquared.core.events.TeleportCause;
import com.plotsquared.core.location.Location;
import com.plotsquared.core.permissions.Permission;
import com.plotsquared.core.permissions.PermissionHandler;
import com.plotsquared.core.player.ConsolePlayer;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.PlotWeather;
import com.plotsquared.core.plot.world.PlotAreaManager;
import com.plotsquared.core.util.EventDispatcher;
import com.plotsquared.core.util.MathMan;
import com.plotsquared.core.util.WorldUtil;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.world.item.ItemType;
import com.sk89q.worldedit.world.item.ItemTypes;
import io.papermc.lib.PaperLib;
import net.kyori.adventure.audience.Audience;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.WeatherType;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.RegisteredListener;
import org.bukkit.potion.PotionEffectType;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Set;
import java.util.UUID;

import static com.sk89q.worldedit.world.gamemode.GameModes.ADVENTURE;
import static com.sk89q.worldedit.world.gamemode.GameModes.CREATIVE;
import static com.sk89q.worldedit.world.gamemode.GameModes.SPECTATOR;
import static com.sk89q.worldedit.world.gamemode.GameModes.SURVIVAL;

public class BukkitPlayer extends PlotPlayer<Player> {

    private static boolean CHECK_EFFECTIVE = true;
    public final Player player;
    private String name;

    /**
     * @param plotAreaManager   PlotAreaManager instance
     * @param eventDispatcher   EventDispatcher instance
     * @param player            Bukkit player instance
     * @param permissionHandler PermissionHandler instance
     */
    BukkitPlayer(
            final @NonNull PlotAreaManager plotAreaManager,
            final @NonNull EventDispatcher eventDispatcher,
            final @NonNull Player player,
            final boolean realPlayer,
            final @NonNull PermissionHandler permissionHandler
    ) {
        super(plotAreaManager, eventDispatcher, permissionHandler);
        this.player = player;
        this.setupPermissionProfile();
        if (realPlayer) {
            super.populatePersistentMetaMap();
        }
    }

    @Override
    public Actor toActor() {
        return BukkitAdapter.adapt(player);
    }

    @Override
    public Player getPlatformPlayer() {
        return this.player;
    }

    @NonNull
    @Override
    public UUID getUUID() {
        if (Settings.UUID.OFFLINE) {
            if (Settings.UUID.FORCE_LOWERCASE) {
                return UUID.nameUUIDFromBytes(("OfflinePlayer:" +
                        getName().toLowerCase()).getBytes(Charsets.UTF_8));
            } else {
                return UUID.nameUUIDFromBytes(("OfflinePlayer:" +
                        getName()).getBytes(Charsets.UTF_8));
            }
        }
        return player.getUniqueId();
    }

    @Override
    @NonNegative
    public long getLastPlayed() {
        return this.player.getLastSeen();
    }

    @Override
    public boolean canTeleport(final @NonNull Location location) {
        if (!WorldUtil.isValidLocation(location)) {
            return false;
        }
        final org.bukkit.Location to = BukkitUtil.adapt(location);
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

    private void callEvent(final @NonNull Event event) {
        final RegisteredListener[] listeners = event.getHandlers().getRegisteredListeners();
        for (final RegisteredListener listener : listeners) {
            if (listener.getPlugin().getName().equals(PlotSquared.platform().pluginName())) {
                continue;
            }
            try {
                listener.callEvent(event);
            } catch (final EventException e) {
                e.printStackTrace();
            }
        }
    }

    @SuppressWarnings("StringSplitter")
    @Override
    @NonNegative
    public int hasPermissionRange(
            final @NonNull String stub,
            @NonNegative final int range
    ) {
        if (hasPermission(Permission.PERMISSION_ADMIN.toString())) {
            return Integer.MAX_VALUE;
        }
        final String[] nodes = stub.split("\\.");
        final StringBuilder n = new StringBuilder();
        // Wildcard check from less specific permission to more specific permission
        for (int i = 0; i < (nodes.length - 1); i++) {
            n.append(nodes[i]).append(".");
            if (!stub.equals(n + Permission.PERMISSION_STAR.toString())) {
                if (hasPermission(n + Permission.PERMISSION_STAR.toString())) {
                    return Integer.MAX_VALUE;
                }
            }
        }
        // Wildcard check for the full permission
        if (hasPermission(stub + ".*")) {
            return Integer.MAX_VALUE;
        }
        // Permission value cache for iterative check
        int max = 0;
        if (CHECK_EFFECTIVE) {
            boolean hasAny = false;
            String stubPlus = stub + ".";
            final Set<PermissionAttachmentInfo> effective = player.getEffectivePermissions();
            if (!effective.isEmpty()) {
                for (PermissionAttachmentInfo attach : effective) {
                    // Ignore all "false" permissions
                    if (!attach.getValue()) {
                        continue;
                    }
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

    @Override
    public void teleport(final @NonNull Location location, final @NonNull TeleportCause cause) {
        if (!WorldUtil.isValidLocation(location)) {
            return;
        }
        final org.bukkit.Location bukkitLocation =
                new org.bukkit.Location(BukkitUtil.getWorld(location.getWorldName()), location.getX() + 0.5,
                        location.getY(), location.getZ() + 0.5, location.getYaw(), location.getPitch()
                );
        PaperLib.teleportAsync(player, bukkitLocation, getTeleportCause(cause));
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
        this.player.setCompassTarget(
                new org.bukkit.Location(BukkitUtil.getWorld(location.getWorldName()), location.getX(),
                        location.getY(), location.getZ()
                ));
    }

    @Override
    public Location getLocationFull() {
        return BukkitUtil.adaptComplete(this.player.getLocation());
    }

    @Override
    public void setWeather(final @NonNull PlotWeather weather) {
        switch (weather) {
            case CLEAR -> this.player.setPlayerWeather(WeatherType.CLEAR);
            case RAIN -> this.player.setPlayerWeather(WeatherType.DOWNFALL);
            case WORLD -> this.player.resetPlayerWeather();
            default -> {
                //do nothing as this is PlotWeather.OFF
            }
        }
    }

    @Override
    public com.sk89q.worldedit.world.gamemode.GameMode getGameMode() {
        return switch (this.player.getGameMode()) {
            case ADVENTURE -> ADVENTURE;
            case CREATIVE -> CREATIVE;
            case SPECTATOR -> SPECTATOR;
            default -> SURVIVAL;
        };
    }

    @Override
    public void setGameMode(final com.sk89q.worldedit.world.gamemode.GameMode gameMode) {
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

    @Override
    public void setTime(final long time) {
        if (time != Long.MAX_VALUE) {
            this.player.setPlayerTime(time, false);
        } else {
            this.player.resetPlayerTime();
        }
    }

    @Override
    public boolean getFlight() {
        return player.getAllowFlight();
    }

    @Override
    public void setFlight(boolean fly) {
        this.player.setAllowFlight(fly);
    }

    @Override
    public void playMusic(final @NonNull Location location, final @NonNull ItemType id) {
        if (id == ItemTypes.AIR) {
            if (PlotSquared.platform().serverVersion()[1] >= 19) {
                player.stopSound(SoundCategory.MUSIC);
                return;
            }
            // 1.18 and downwards require a specific Sound to stop (even tho the packet does not??)
            for (final Sound sound : Sound.values()) {
                if (sound.name().startsWith("MUSIC_DISC")) {
                    this.player.stopSound(sound, SoundCategory.MUSIC);
                }
            }
            return;
        }
        this.player.playSound(BukkitUtil.adapt(location), Sound.valueOf(BukkitAdapter.adapt(id).name()),
                SoundCategory.MUSIC, Float.MAX_VALUE, 1f
        );
    }

    @SuppressWarnings("deprecation") // Needed for Spigot compatibility
    @Override
    public void kick(final String message) {
        this.player.kickPlayer(message);
    }

    @Override
    public void stopSpectating() {
        if (getGameMode() == SPECTATOR) {
            this.player.setSpectatorTarget(null);
        }
    }

    @Override
    public boolean isBanned() {
        return this.player.isBanned();
    }

    @Override
    public @NonNull Audience getAudience() {
        return BukkitUtil.BUKKIT_AUDIENCES.player(this.player);
    }

    @Override
    public void removeEffect(@NonNull String name) {
        PotionEffectType type = PotionEffectType.getByName(name);
        if (type != null) {
            player.removePotionEffect(type);
        }
    }

    @Override
    public boolean canSee(final PlotPlayer<?> other) {
        if (other instanceof ConsolePlayer) {
            return true;
        } else {
            return this.player.canSee(((BukkitPlayer) other).getPlatformPlayer());
        }
    }

    /**
     * Convert from PlotSquared's {@link TeleportCause} to Bukkit's {@link PlayerTeleportEvent.TeleportCause}
     *
     * @param cause PlotSquared teleport cause to convert
     * @return Bukkit's equivalent teleport cause
     */
    public PlayerTeleportEvent.TeleportCause getTeleportCause(final @NonNull TeleportCause cause) {
        if (TeleportCause.CauseSets.COMMAND.contains(cause)) {
            return PlayerTeleportEvent.TeleportCause.COMMAND;
        } else if (cause == TeleportCause.UNKNOWN) {
            return PlayerTeleportEvent.TeleportCause.UNKNOWN;
        }
        return PlayerTeleportEvent.TeleportCause.PLUGIN;
    }

}
