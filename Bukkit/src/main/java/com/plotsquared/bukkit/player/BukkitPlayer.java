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
package com.plotsquared.bukkit.player;

import com.google.common.base.Charsets;
import com.plotsquared.bukkit.util.BukkitUtil;
import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.configuration.Captions;
import com.plotsquared.core.configuration.Settings;
import com.plotsquared.core.events.TeleportCause;
import com.plotsquared.core.location.Location;
import com.plotsquared.core.permissions.PermissionHandler;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.PlotWeather;
import com.plotsquared.core.plot.world.PlotAreaManager;
import com.plotsquared.core.util.EconHandler;
import com.plotsquared.core.util.EventDispatcher;
import com.plotsquared.core.util.MathMan;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.world.item.ItemType;
import com.sk89q.worldedit.world.item.ItemTypes;
import io.papermc.lib.PaperLib;
import net.kyori.adventure.audience.Audience;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.WeatherType;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.RegisteredListener;

import javax.annotation.Nonnegative;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.sk89q.worldedit.world.gamemode.GameModes.ADVENTURE;
import static com.sk89q.worldedit.world.gamemode.GameModes.CREATIVE;
import static com.sk89q.worldedit.world.gamemode.GameModes.SPECTATOR;
import static com.sk89q.worldedit.world.gamemode.GameModes.SURVIVAL;

public class BukkitPlayer extends PlotPlayer<Player> {

    private static boolean CHECK_EFFECTIVE = true;
    public final Player player;
    private final EconHandler econHandler;
    private String name;
    private String lastMessage = "";
    private long lastMessageTime = 0L;
    /**
     * <p>Please do not use this method. Instead use
     * BukkitUtil.getPlayer(Player), as it caches player objects.</p>
     *
     * @param player Bukkit player instance
     */
    public BukkitPlayer(@Nonnull final PlotAreaManager plotAreaManager, @Nonnull final EventDispatcher eventDispatcher,
        @Nonnull final Player player, @Nullable final EconHandler econHandler, @Nonnull final PermissionHandler permissionHandler) {
        this(plotAreaManager, eventDispatcher, player, false, econHandler, permissionHandler);
    }

    public BukkitPlayer(@Nonnull final PlotAreaManager plotAreaManager, @Nonnull final
        EventDispatcher eventDispatcher, @Nonnull final Player player,
        final boolean realPlayer, @Nullable final EconHandler econHandler,
        @Nonnull final PermissionHandler permissionHandler) {
        super(plotAreaManager, eventDispatcher, econHandler, permissionHandler);
        this.player = player;
        this.econHandler = econHandler;
        this.setupPermissionProfile();
        if (realPlayer) {
            super.populatePersistentMetaMap();
        }
    }

    @Override public Actor toActor() {
        return BukkitAdapter.adapt(player);
    }

    @Override public Player getPlatformPlayer() {
        return this.player;
    }

    @Nonnull @Override public UUID getUUID() {
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

    @Override @Nonnegative public long getLastPlayed() {
        return this.player.getLastSeen();
    }

    @Override public boolean canTeleport(@Nonnull final Location location) {
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

    private void callEvent(@Nonnull final Event event) {
        final RegisteredListener[] listeners = event.getHandlers().getRegisteredListeners();
        for (final RegisteredListener listener : listeners) {
            if (listener.getPlugin().getName().equals(PlotSquared.platform().getPluginName())) {
                continue;
            }
            try {
                listener.callEvent(event);
            } catch (final EventException e) {
                e.printStackTrace();
            }
        }
    }

    @Override public boolean hasPermission(@Nonnull final String permission) {
        if (this.offline && this.econHandler != null) {
            return this.econHandler.hasPermission(getName(), permission);
        }
        return this.player.hasPermission(permission);
    }

    @Override @Nonnegative public int hasPermissionRange(@Nonnull final String stub,
                                            @Nonnegative final int range) {
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

    @Override
    public void teleport(@Nonnull final Location location, @Nonnull final TeleportCause cause) {
        if (Math.abs(location.getX()) >= 30000000 || Math.abs(location.getZ()) >= 30000000) {
            return;
        }
        final org.bukkit.Location bukkitLocation =
            new org.bukkit.Location(BukkitUtil.getWorld(location.getWorldName()), location.getX() + 0.5,
                location.getY(), location.getZ() + 0.5, location.getYaw(), location.getPitch());
        PaperLib.teleportAsync(player, bukkitLocation, getTeleportCause(cause));
    }

    @Override public String getName() {
        if (this.name == null) {
            this.name = this.player.getName();
        }
        return this.name;
    }

    @Override public void setCompassTarget(Location location) {
        this.player.setCompassTarget(
            new org.bukkit.Location(BukkitUtil.getWorld(location.getWorldName()), location.getX(),
                location.getY(), location.getZ()));
    }

    @Override public Location getLocationFull() {
        return BukkitUtil.adaptComplete(this.player.getLocation());
    }

    @Override public void setWeather(@Nonnull final PlotWeather weather) {
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

    @Override public com.sk89q.worldedit.world.gamemode.GameMode getGameMode() {
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

    @Override public void playMusic(@Nonnull final Location location, @Nonnull final ItemType id) {
        if (id == ItemTypes.AIR) {
            // Let's just stop all the discs because why not?
            for (final Sound sound : Arrays.stream(Sound.values())
                .filter(sound -> sound.name().contains("DISC")).collect(Collectors.toList())) {
                player.stopSound(sound);
            }
            // this.player.playEffect(BukkitUtil.getLocation(location), Effect.RECORD_PLAY, Material.AIR);
        } else {
            // this.player.playEffect(BukkitUtil.getLocation(location), Effect.RECORD_PLAY, id.to(Material.class));
            this.player.playSound(BukkitUtil.adapt(location),
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

    @Override @Nonnull public Audience getAudience() {
        return BukkitUtil.BUKKIT_AUDIENCES.player(this.player);
    }


    public PlayerTeleportEvent.TeleportCause getTeleportCause(@Nonnull final TeleportCause cause) {
        switch (cause) {
            case COMMAND:
                return PlayerTeleportEvent.TeleportCause.COMMAND;
            case PLUGIN:
                return PlayerTeleportEvent.TeleportCause.PLUGIN;
            default:
                return PlayerTeleportEvent.TeleportCause.UNKNOWN;
        }
    }
}
