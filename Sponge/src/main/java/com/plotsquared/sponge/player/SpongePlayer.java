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
 *               Copyright (C) 2014 - 2022 IntellectualSites
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
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.plotsquared.sponge.player;

import com.plotsquared.core.events.TeleportCause;
import com.plotsquared.core.location.Location;
import com.plotsquared.core.permissions.PermissionHandler;
import com.plotsquared.core.player.ConsolePlayer;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.PlotWeather;
import com.plotsquared.core.plot.world.PlotAreaManager;
import com.plotsquared.core.util.EventDispatcher;
import com.plotsquared.sponge.util.SpongeUtil;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.world.gamemode.GameModes;
import com.sk89q.worldedit.world.item.ItemType;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Server;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.math.vector.Vector3d;

import java.util.UUID;

import static com.sk89q.worldedit.world.gamemode.GameModes.SPECTATOR;

public class SpongePlayer extends PlotPlayer<ServerPlayer> {

    private static boolean CHECK_EFFECTIVE = true;
    public final ServerPlayer player;
    private String name;

    /**
     * <p>Please do not use this method. Instead use
     * BukkitUtil.getPlayer(Player), as it caches player objects.</p>
     *
     * @param plotAreaManager   PlotAreaManager instance
     * @param eventDispatcher   EventDispatcher instance
     * @param player            Bukkit player instance
     * @param permissionHandler PermissionHandler instance
     */
    public SpongePlayer(
            final @NonNull PlotAreaManager plotAreaManager,
            final @NonNull EventDispatcher eventDispatcher,
            final @NonNull ServerPlayer player,
            final @NonNull PermissionHandler permissionHandler
    ) {
        this(plotAreaManager, eventDispatcher, player, false, permissionHandler);
    }

    public SpongePlayer(
            final @NonNull PlotAreaManager plotAreaManager,
            final @NonNull EventDispatcher eventDispatcher,
            final @NonNull ServerPlayer player,
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
        throw new UnsupportedOperationException();
    }

    @Override
    public ServerPlayer getPlatformPlayer() {
        return this.player;
    }

    @NonNull
    @Override
    public UUID getUUID() {
/*
TODO Fix me

        if (Settings.UUID.OFFLINE) {
            if (Settings.UUID.FORCE_LOWERCASE) {
                return UUID.nameUUIDFromBytes(("OfflinePlayer:" +
                        getName().toLowerCase()).getBytes(Charsets.UTF_8));
            } else {
                return UUID.nameUUIDFromBytes(("OfflinePlayer:" +
                        getName()).getBytes(Charsets.UTF_8));
            }
        }
*/
        return player.uniqueId();
    }

    @Override
    @NonNegative
    public long getLastPlayed() {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public boolean canTeleport(final @NonNull Location location) {
        throw new UnsupportedOperationException();
    }

    @Override
    @NonNegative
    public int hasPermissionRange(
            final @NonNull String stub, @NonNegative final int range
    ) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void teleport(final @NonNull Location location, final @NonNull TeleportCause cause) {
        if (Math.abs(location.getX()) >= 30000000 || Math.abs(location.getZ()) >= 30000000) {
            return;
        }
        final ServerLocation spongeLocation = ServerLocation.of(
                ResourceKey.resolve(location.getWorldName()),
                location.getX() + 0.5,
                location.getY(),
                location.getZ() + 0.5
        );
        //TODO has not been tested.
        player.setLocationAndRotation(spongeLocation, Vector3d.from(location.getPitch(), location.getYaw(), 0));
    }

    @Override
    public String getName() {
        if (this.name == null) {
            this.name = this.player.name();
        }
        return this.player.name();
    }

    @Override
    public void setCompassTarget(Location location) {
        //Setting compass targets changed since the last Sponge API.
    }

    @Override
    public Location getLocationFull() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setWeather(final @NonNull PlotWeather weather) {
        throw new UnsupportedOperationException();
    }

    @Override
    public com.sk89q.worldedit.world.gamemode.GameMode getGameMode() {
        return GameModes.get(player.gameMode().get().key(RegistryTypes.GAME_MODE).asString());
    }

    @Override
    public void setGameMode(final com.sk89q.worldedit.world.gamemode.GameMode gameMode) {
        player.gameMode().set(Sponge.game().registry(RegistryTypes.GAME_MODE).value(ResourceKey.resolve(gameMode.getId())));
    }

    @Override
    public void setTime(final long time) {
        throw new UnsupportedOperationException("Sponge still doesn't support this after more than 4 years of work on API 8.");
    }

    @Override
    public boolean getFlight() {
        return player.canFly().get();
    }

    @Override
    public void setFlight(boolean fly) {
        this.player.canFly().set(fly);
    }

    @Override
    public void playMusic(final @NonNull Location location, final @NonNull ItemType id) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void kick(final String message) {
        this.player.kick(Component.text(message));
    }

    @Override
    public void stopSpectating() {
        if (getGameMode() == SPECTATOR) {
            this.player.spectatorTarget().set(null);
        }
    }

    @Override
    public boolean isBanned() {
        throw new UnsupportedOperationException();
    }

    @Override
    public @NonNull Audience getAudience() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean canSee(final PlotPlayer<?> other) {
        if (other instanceof ConsolePlayer) {
            return true;
        } else {
            return this.player.canSee(null); //TODO Fix Me?
        }
    }

}
