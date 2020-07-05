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
package com.plotsquared.core.player;

import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.command.RequiredType;
import com.plotsquared.core.configuration.Caption;
import com.plotsquared.core.configuration.caption.TranslatableCaption;
import com.plotsquared.core.database.DBFunc;
import com.plotsquared.core.events.TeleportCause;
import com.plotsquared.core.location.Location;
import com.plotsquared.core.plot.PlotArea;
import com.plotsquared.core.plot.PlotWeather;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.world.gamemode.GameMode;
import com.sk89q.worldedit.world.gamemode.GameModes;
import com.sk89q.worldedit.world.item.ItemType;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.Template;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class ConsolePlayer extends PlotPlayer<Actor> {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.builder().build();
    private static ConsolePlayer instance;

    private ConsolePlayer() {
        PlotArea area = PlotSquared.get().getFirstPlotArea();
        Location location;
        if (area != null) {
            CuboidRegion region = area.getRegion();
            location = new Location(area.getWorldName(),
                region.getMinimumPoint().getX() + region.getMaximumPoint().getX() / 2, 0,
                region.getMinimumPoint().getZ() + region.getMaximumPoint().getZ() / 2);
        } else {
            location = new Location("world", 0, 0, 0);
        }
        setMeta("location", location);
    }

    public static ConsolePlayer getConsole() {
        if (instance == null) {
            instance = new ConsolePlayer();
            instance.teleport(instance.getLocation());
        }
        return instance;
    }

    @Override public Actor toActor() {
        return PlotSquared.get().IMP.getConsole();
    }

    @Override public Actor getPlatformPlayer() {
        return this.toActor();
    }

    @Override public boolean canTeleport(@NotNull Location location) {
        return true;
    }

    @Override
    public void sendTitle(@NotNull final Caption title, @NotNull final Caption subtitle,
        final int fadeIn, final int stay, final int fadeOut, @NotNull final Template... replacements) {
    }

    @NotNull @Override public Location getLocation() {
        return this.getMeta("location");
    }

    @Override public Location getLocationFull() {
        return getLocation();
    }

    @NotNull @Override public UUID getUUID() {
        return DBFunc.EVERYONE;
    }

    @Override public long getLastPlayed() {
        return 0;
    }

    @Override public boolean hasPermission(@NotNull String permission) {
        return true;
    }

    @Override public boolean isPermissionSet(@NotNull String permission) {
        return true;
    }

    @Override public void sendMessage(@NotNull final Caption caption, @NotNull final Template... replacements) {
        final String message = caption.getComponent(this);
        if (message.isEmpty()) {
            return;
        }
        // Create the template list, and add the prefix as a replacement
        final List<Template> templates = Arrays.asList(replacements);
        templates.add(Template.of("prefix", MINI_MESSAGE.parse(
            TranslatableCaption.of("core.prefix").getComponent(this))));
        // Parse the message
        PlotSquared.imp().getConsoleAudience().sendMessage(MINI_MESSAGE.parse(message, templates));
    }

    @Override public void teleport(Location location, TeleportCause cause) {
        setMeta(META_LAST_PLOT, location.getPlot());
        setMeta(META_LOCATION, location);
    }

    @Override public boolean isOnline() {
        return true;
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

    @Override @NotNull public RequiredType getSuperCaller() {
        return RequiredType.CONSOLE;
    }

    @Override public void setWeather(@NotNull PlotWeather weather) {
    }

    @Override public @NotNull GameMode getGameMode() {
        return GameModes.SPECTATOR;
    }

    @Override public void setGameMode(@NotNull GameMode gameMode) {
    }

    @Override public void setTime(long time) {
    }

    @Override public boolean getFlight() {
        return true;
    }

    @Override public void setFlight(boolean fly) {
    }

    @Override public void playMusic(@NotNull Location location, @NotNull ItemType id) {
    }

    @Override public void kick(String message) {
    }

    @Override public void stopSpectating() {
    }

    @Override public boolean isBanned() {
        return false;
    }

}
