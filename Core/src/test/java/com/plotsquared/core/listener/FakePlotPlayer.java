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
package com.plotsquared.core.listener;

import com.plotsquared.core.configuration.caption.Caption;
import com.plotsquared.core.configuration.caption.TranslatableCaption;
import com.plotsquared.core.events.TeleportCause;
import com.plotsquared.core.location.Location;
import com.plotsquared.core.player.PlayerMetaDataKeys;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.PlotWeather;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.world.gamemode.GameMode;
import com.sk89q.worldedit.world.gamemode.GameModes;
import com.sk89q.worldedit.world.item.ItemType;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * A player that lives in memory only. It keeps game mode, flight, permissions and received messages, which is what
 * the plot entry and exit handling reads and changes. Persistent meta data is the real implementation of
 * {@link PlotPlayer} and stays in memory, because no database is configured in unit tests.
 */
final class FakePlotPlayer extends PlotPlayer<Object> {

    static {
        // A player sizes its lock repository by the meta data keys known when it is created, so they have to be
        // registered before the first player exists. On a server that happens long before a player joins.
        Objects.requireNonNull(PlayerMetaDataKeys.PERSISTENT_FLIGHT);
    }

    private final UUID uuid = UUID.randomUUID();
    private final Set<String> permissions = new HashSet<>();
    private final List<String> messages = new ArrayList<>();
    private GameMode gameMode;
    private boolean flight;

    FakePlotPlayer(final GameMode gameMode) {
        super(null, null, null);
        this.gameMode = gameMode;
        this.flight = canFlyIn(gameMode);
    }

    private static boolean canFlyIn(final GameMode gameMode) {
        return gameMode == GameModes.CREATIVE || gameMode == GameModes.SPECTATOR;
    }

    void grantPermission(final String permission) {
        this.permissions.add(permission);
    }

    /**
     * @return keys of the translatable messages this player received, in order
     */
    List<String> receivedMessages() {
        return List.copyOf(this.messages);
    }

    @Override
    public boolean hasPermission(final @NonNull String permission) {
        return this.permissions.contains(permission);
    }

    @Override
    public void sendMessage(final @NonNull Caption caption, final @NonNull TagResolver... replacements) {
        this.messages.add(caption instanceof TranslatableCaption translatable ? translatable.getKey() : caption.toString());
    }

    @Override
    public @NonNull GameMode getGameMode() {
        return this.gameMode;
    }

    /**
     * Like a server, a game mode change also decides whether the player may fly.
     */
    @Override
    public void setGameMode(final @NonNull GameMode gameMode) {
        this.gameMode = gameMode;
        this.flight = canFlyIn(gameMode);
    }

    @Override
    public boolean getFlight() {
        return this.flight;
    }

    @Override
    public void setFlight(final boolean fly) {
        this.flight = fly;
    }

    @Override
    public @NonNull UUID getUUID() {
        return this.uuid;
    }

    @Override
    public String getName() {
        return "FakePlotPlayer";
    }

    @Override
    public long getLastPlayed() {
        return 0L;
    }

    @Override
    public Actor toActor() {
        throw new UnsupportedOperationException("not needed by the tests");
    }

    @Override
    public Object getPlatformPlayer() {
        throw new UnsupportedOperationException("not needed by the tests");
    }

    @Override
    public Location getLocationFull() {
        throw new UnsupportedOperationException("not needed by the tests");
    }

    @Override
    public void teleport(final Location location, final TeleportCause cause) {
        throw new UnsupportedOperationException("not needed by the tests");
    }

    @Override
    public void setCompassTarget(final Location location) {
        throw new UnsupportedOperationException("not needed by the tests");
    }

    @Override
    public void setWeather(final @NonNull PlotWeather weather) {
        throw new UnsupportedOperationException("not needed by the tests");
    }

    @Override
    public void setTime(final long time) {
        throw new UnsupportedOperationException("not needed by the tests");
    }

    @Override
    public void playMusic(final @NonNull Location location, final @NonNull ItemType id) {
        throw new UnsupportedOperationException("not needed by the tests");
    }

    @Override
    public boolean isBanned() {
        return false;
    }

    @Override
    public void kick(final String message) {
        throw new UnsupportedOperationException("not needed by the tests");
    }

    @Override
    public boolean canSee(final PlotPlayer<?> other) {
        return true;
    }

    @Override
    public void stopSpectating() {
        throw new UnsupportedOperationException("not needed by the tests");
    }

    @Override
    public @NonNull Audience getAudience() {
        return Audience.empty();
    }

    @Override
    public void removeEffect(final @NonNull String name) {
        throw new UnsupportedOperationException("not needed by the tests");
    }

}
