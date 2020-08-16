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
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.plotsquared.core.player;

import com.google.inject.Inject;
import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.command.RequiredType;
import com.plotsquared.core.configuration.caption.Caption;
import com.plotsquared.core.configuration.caption.CaptionUtility;
import com.plotsquared.core.configuration.caption.TranslatableCaption;
import com.plotsquared.core.database.DBFunc;
import com.plotsquared.core.events.TeleportCause;
import com.plotsquared.core.inject.annotations.ConsoleActor;
import com.plotsquared.core.location.Location;
import com.plotsquared.core.permissions.PermissionHandler;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.PlotArea;
import com.plotsquared.core.plot.PlotWeather;
import com.plotsquared.core.plot.world.PlotAreaManager;
import com.plotsquared.core.util.EconHandler;
import com.plotsquared.core.util.EventDispatcher;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.world.gamemode.GameMode;
import com.sk89q.worldedit.world.gamemode.GameModes;
import com.sk89q.worldedit.world.item.ItemType;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.Template;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;

public class ConsolePlayer extends PlotPlayer<Actor> {

    private static final Logger logger = LoggerFactory.getLogger("P2/" + ConsolePlayer.class.getSimpleName());
    private static final MiniMessage MINI_MESSAGE = MiniMessage.builder().build();
    private static ConsolePlayer instance;

    private final Actor actor;

    @Inject private ConsolePlayer(@Nonnull final PlotAreaManager plotAreaManager,
                                  @Nonnull final EventDispatcher eventDispatcher,
                                  @ConsoleActor @Nonnull final Actor actor,
                                  @Nullable final EconHandler econHandler,
                                  @Nonnull final PermissionHandler permissionHandler) {
        super(plotAreaManager, eventDispatcher, econHandler, permissionHandler);
        this.actor = actor;
        this.setupPermissionProfile();
        final PlotArea[] areas = plotAreaManager.getAllPlotAreas();
        final PlotArea area;
        if (areas.length > 0) {
            area = areas[0];
        } else {
            area = null;
        }
        Location location;
        if (area != null) {
            CuboidRegion region = area.getRegion();
            location = Location.at(area.getWorldName(),
                region.getMinimumPoint().getX() + region.getMaximumPoint().getX() / 2, 0,
                region.getMinimumPoint().getZ() + region.getMaximumPoint().getZ() / 2);
        } else {
            location = Location.at("", 0, 0, 0);
        }
        setMeta("location", location);
    }

    public static ConsolePlayer getConsole() {
        if (instance == null) {
            instance = PlotSquared.platform().getInjector().getInstance(ConsolePlayer.class);
            instance.teleport(instance.getLocation());
        }
        return instance;
    }

    @Override public Actor toActor() {
        return this.actor;
    }

    @Override public Actor getPlatformPlayer() {
        return this.toActor();
    }

    @Override public boolean canTeleport(@Nonnull Location location) {
        return true;
    }

    @Override
    public void sendTitle(@Nonnull final Caption title, @Nonnull final Caption subtitle,
        final int fadeIn, final int stay, final int fadeOut, @Nonnull final Template... replacements) {
    }

    @Nonnull @Override public Location getLocation() {
        return this.getMeta("location");
    }

    @Override public Location getLocationFull() {
        return getLocation();
    }

    @Nonnull @Override public UUID getUUID() {
        return DBFunc.EVERYONE;
    }

    @Override public long getLastPlayed() {
        return System.currentTimeMillis();
    }

    @Override public boolean hasPermission(@Nonnull String permission) {
        return true;
    }

    @Override public void sendMessage(@Nonnull final Caption caption,
                                     @Nonnull final Template... replacements) {
        String message = caption.getComponent(this);
        if (message.isEmpty()) {
            return;
        }
        message = CaptionUtility.format(this, message)
            .replace('\u2010', '%').replace('\u2020', '&').replace('\u2030', '&')
            .replace("<prefix>", TranslatableCaption.of("core.prefix").getComponent(this));
        // Parse the message
        PlotSquared.platform().getConsoleAudience().sendMessage(MINI_MESSAGE.parse(message, replacements));
    }

    @Override public void teleport(Location location, TeleportCause cause) {
        try (final MetaDataAccess<Plot> lastPlot = accessTemporaryMetaData(PlayerMetaDataKeys.TEMPORARY_LAST_PLOT)) {
            if (location.getPlot() == null) {
                lastPlot.remove();
            } else {
                lastPlot.set(location.getPlot());
            }
        }
        try (final MetaDataAccess<Location> locationMetaDataAccess = accessPersistentMetaData(PlayerMetaDataKeys.TEMPORARY_LOCATION)) {
            locationMetaDataAccess.set(location);
        }
    }

    @Override public String getName() {
        return "*";
    }

    @Override public void setCompassTarget(Location location) {
    }

    @Override public void setAttribute(String key) {
    }

    @Override public boolean getAttribute(String key) {
        return false;
    }

    @Override public void removeAttribute(String key) {
    }

    @Override @Nonnull public RequiredType getSuperCaller() {
        return RequiredType.CONSOLE;
    }

    @Override public void setWeather(@Nonnull PlotWeather weather) {
    }

    @Override public @Nonnull GameMode getGameMode() {
        return GameModes.SPECTATOR;
    }

    @Override public void setGameMode(@Nonnull GameMode gameMode) {
    }

    @Override public void setTime(long time) {
    }

    @Override public boolean getFlight() {
        return true;
    }

    @Override public void setFlight(boolean fly) {
    }

    @Override public void playMusic(@Nonnull Location location, @Nonnull ItemType id) {
    }

    @Override public void kick(String message) {
    }

    @Override public void stopSpectating() {
    }

    @Override public boolean isBanned() {
        return false;
    }

    @Override @Nonnull public Audience getAudience() {
        return PlotSquared.platform().getConsoleAudience();
    }

}
