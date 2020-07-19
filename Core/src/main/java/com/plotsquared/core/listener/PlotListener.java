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
package com.plotsquared.core.listener;

import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.collection.ByteArrayUtilities;
import com.plotsquared.core.configuration.Captions;
import com.plotsquared.core.configuration.Settings;
import com.plotsquared.core.configuration.caption.Templates;
import com.plotsquared.core.configuration.caption.TranslatableCaption;
import com.plotsquared.core.events.PlotFlagRemoveEvent;
import com.plotsquared.core.events.Result;
import com.plotsquared.core.location.Location;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.PlotArea;
import com.plotsquared.core.plot.PlotWeather;
import com.plotsquared.core.plot.comment.CommentManager;
import com.plotsquared.core.plot.expiration.ExpireManager;
import com.plotsquared.core.plot.flag.GlobalFlagContainer;
import com.plotsquared.core.plot.flag.PlotFlag;
import com.plotsquared.core.plot.flag.implementations.DenyExitFlag;
import com.plotsquared.core.plot.flag.implementations.FarewellFlag;
import com.plotsquared.core.plot.flag.implementations.FeedFlag;
import com.plotsquared.core.plot.flag.implementations.FlyFlag;
import com.plotsquared.core.plot.flag.implementations.GamemodeFlag;
import com.plotsquared.core.plot.flag.implementations.GreetingFlag;
import com.plotsquared.core.plot.flag.implementations.GuestGamemodeFlag;
import com.plotsquared.core.plot.flag.implementations.HealFlag;
import com.plotsquared.core.plot.flag.implementations.MusicFlag;
import com.plotsquared.core.plot.flag.implementations.NotifyEnterFlag;
import com.plotsquared.core.plot.flag.implementations.NotifyLeaveFlag;
import com.plotsquared.core.plot.flag.implementations.TimeFlag;
import com.plotsquared.core.plot.flag.implementations.TitlesFlag;
import com.plotsquared.core.plot.flag.implementations.WeatherFlag;
import com.plotsquared.core.plot.flag.types.TimedFlag;
import com.plotsquared.core.util.EventDispatcher;
import com.plotsquared.core.util.MainUtil;
import com.plotsquared.core.util.Permissions;
import com.plotsquared.core.util.PlayerManager;
import com.plotsquared.core.util.StringMan;
import com.plotsquared.core.util.task.TaskManager;
import com.plotsquared.core.util.task.TaskTime;
import com.sk89q.worldedit.world.gamemode.GameMode;
import com.sk89q.worldedit.world.gamemode.GameModes;
import com.sk89q.worldedit.world.item.ItemType;
import com.sk89q.worldedit.world.item.ItemTypes;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public class PlotListener {

    private final HashMap<UUID, Interval> feedRunnable = new HashMap<>();
    private final HashMap<UUID, Interval> healRunnable = new HashMap<>();

    private final EventDispatcher eventDispatcher;

    public PlotListener(@Nullable final EventDispatcher eventDispatcher) {
        this.eventDispatcher = eventDispatcher;
    }

    public void startRunnable() {
        TaskManager.runTaskRepeat(() -> {
            if (!healRunnable.isEmpty()) {
                for (Iterator<Map.Entry<UUID, Interval>> iterator =
                     healRunnable.entrySet().iterator(); iterator.hasNext(); ) {
                    Map.Entry<UUID, Interval> entry = iterator.next();
                    Interval value = entry.getValue();
                    ++value.count;
                    if (value.count == value.interval) {
                        value.count = 0;
                        PlotPlayer<?> player = PlotPlayer.wrap(entry.getKey());
                        if (player == null) {
                            iterator.remove();
                            continue;
                        }
                        double level = PlotSquared.platform().getWorldUtil().getHealth(player);
                        if (level != value.max) {
                            PlotSquared.platform().getWorldUtil().setHealth(player, Math.min(level + value.amount, value.max));
                        }
                    }
                }
            }
            if (!feedRunnable.isEmpty()) {
                for (Iterator<Map.Entry<UUID, Interval>> iterator =
                     feedRunnable.entrySet().iterator(); iterator.hasNext(); ) {
                    Map.Entry<UUID, Interval> entry = iterator.next();
                    Interval value = entry.getValue();
                    ++value.count;
                    if (value.count == value.interval) {
                        value.count = 0;
                        PlotPlayer<?> player = PlotSquared.platform().getWorldUtil().getPlayer(entry.getKey());
                        if (player == null) {
                            iterator.remove();
                            continue;
                        }
                        int level = PlotSquared.platform().getWorldUtil().getFoodLevel(player);
                        if (level != value.max) {
                            PlotSquared.platform().getWorldUtil().setFoodLevel(player, Math.min(level + value.amount, value.max));
                        }
                    }
                }
            }
        }, TaskTime.seconds(1L));
    }

    public boolean plotEntry(final PlotPlayer<?> player, final Plot plot) {
        if (plot.isDenied(player.getUUID()) && !Permissions
            .hasPermission(player, "plots.admin.entry.denied")) {
            return false;
        }
        Plot last = player.getMeta(PlotPlayer.META_LAST_PLOT);
        if ((last != null) && !last.getId().equals(plot.getId())) {
            plotExit(player, last);
        }
        if (ExpireManager.IMP != null) {
            ExpireManager.IMP.handleEntry(player, plot);
        }
        player.setMeta(PlotPlayer.META_LAST_PLOT, plot);
        this.eventDispatcher.callEntry(player, plot);
        if (plot.hasOwner()) {
            // This will inherit values from PlotArea
            final TitlesFlag.TitlesFlagValue titleFlag = plot.getFlag(TitlesFlag.class);
            final boolean titles;
            if (titleFlag == TitlesFlag.TitlesFlagValue.NONE) {
                titles = Settings.TITLES;
            } else {
                titles = titleFlag == TitlesFlag.TitlesFlagValue.TRUE;
            }

            final String greeting = plot.getFlag(GreetingFlag.class);
            if (!greeting.isEmpty()) {
                plot.format(Captions.PREFIX_GREETING.getTranslated() + greeting, player, false)
                    .thenAcceptAsync(player::sendMessage);
            }

            if (plot.getFlag(NotifyEnterFlag.class)) {
                if (!Permissions.hasPermission(player, "plots.flag.notify-enter.bypass")) {
                    for (UUID uuid : plot.getOwners()) {
                        final PlotPlayer owner = PlotSquared.platform().getPlayerManager().getPlayerIfExists(uuid);
                        if (owner != null && !owner.getUUID().equals(player.getUUID())) {
                            MainUtil.sendMessage(owner, Captions.NOTIFY_ENTER.getTranslated()
                                .replace("%player", player.getName())
                                .replace("%plot", plot.getId().toString()));
                        }
                    }
                }
            }

            final FlyFlag.FlyStatus flyStatus = plot.getFlag(FlyFlag.class);
            if (flyStatus != FlyFlag.FlyStatus.DEFAULT) {
                boolean flight = player.getFlight();
                GameMode gamemode = player.getGameMode();
                if (flight != (gamemode == GameModes.CREATIVE || gamemode == GameModes.SPECTATOR)) {
                    player.setPersistentMeta("flight",
                        ByteArrayUtilities.booleanToBytes(player.getFlight()));
                }
                player.setFlight(flyStatus == FlyFlag.FlyStatus.ENABLED);
            }

            final GameMode gameMode = plot.getFlag(GamemodeFlag.class);
            if (!gameMode.equals(GamemodeFlag.DEFAULT)) {
                if (player.getGameMode() != gameMode) {
                    if (!Permissions.hasPermission(player, "plots.gamemode.bypass")) {
                        player.setGameMode(gameMode);
                    } else {
                        MainUtil.sendMessage(player, StringMan
                            .replaceAll(Captions.GAMEMODE_WAS_BYPASSED.getTranslated(), "{plot}",
                                plot.getId(), "{gamemode}", gameMode));
                    }
                }
            }

            final GameMode guestGameMode = plot.getFlag(GuestGamemodeFlag.class);
            if (!guestGameMode.equals(GamemodeFlag.DEFAULT)) {
                if (player.getGameMode() != guestGameMode && !plot.isAdded(player.getUUID())) {
                    if (!Permissions.hasPermission(player, "plots.gamemode.bypass")) {
                        player.setGameMode(guestGameMode);
                    } else {
                        MainUtil.sendMessage(player, StringMan
                            .replaceAll(Captions.GAMEMODE_WAS_BYPASSED.getTranslated(), "{plot}",
                                plot.getId(), "{gamemode}", guestGameMode));
                    }
                }
            }

            long time = plot.getFlag(TimeFlag.class);
            if (time != TimeFlag.TIME_DISABLED.getValue() && !player.getAttribute("disabletime")) {
                try {
                    player.setTime(time);
                } catch (Exception ignored) {
                    PlotFlag<?, ?> plotFlag =
                        GlobalFlagContainer.getInstance().getFlag(TimeFlag.class);
                    PlotFlagRemoveEvent event =
                        this.eventDispatcher.callFlagRemove(plotFlag, plot);
                    if (event.getEventResult() != Result.DENY) {
                        plot.removeFlag(event.getFlag());
                    }
                }
            }

            player.setWeather(plot.getFlag(WeatherFlag.class));

            ItemType musicFlag = plot.getFlag(MusicFlag.class);
            if (musicFlag != null) {
                final String rawId = musicFlag.getId();
                if (rawId.contains("disc") || musicFlag == ItemTypes.AIR) {
                    Location location = player.getLocation();
                    Location lastLocation = player.getMeta("music");
                    if (lastLocation != null) {
                        player.playMusic(lastLocation, musicFlag);
                        if (musicFlag == ItemTypes.AIR) {
                            player.deleteMeta("music");
                        }
                    }
                    if (musicFlag != ItemTypes.AIR) {
                        try {
                            player.setMeta("music", location);
                            player.playMusic(location, musicFlag);
                        } catch (Exception ignored) {
                        }
                    }
                }
            } else {
                Location lastLoc = player.getMeta("music");
                if (lastLoc != null) {
                    player.deleteMeta("music");
                    player.playMusic(lastLoc, ItemTypes.AIR);
                }
            }
            CommentManager.sendTitle(player, plot);

            if (titles && !player.getAttribute("disabletitles")) {
                if (!Captions.TITLE_ENTERED_PLOT.getTranslated().isEmpty()
                    || !Captions.TITLE_ENTERED_PLOT_SUB.getTranslated().isEmpty()) {
                    TaskManager.runTaskLaterAsync(() -> {
                        Plot lastPlot = player.getMeta(PlotPlayer.META_LAST_PLOT);
                        if ((lastPlot != null) && plot.getId().equals(lastPlot.getId())) {
                            player.sendTitle(
                                TranslatableCaption.of("titles.title_entered_plot"),
                                TranslatableCaption.of("titles.title_entered_plot_sub"),
                                Templates.of("x", lastPlot.getId().getX()),
                                Templates.of("z", lastPlot.getId().getY()),
                                Templates.of("world", plot.getArea()),
                                Templates.of("greeting", greeting),
                                Templates.of("alias", plot.getAlias()),
                                Templates.of("owner", plot.getOwner())
                            );
                        }
                    }, TaskTime.seconds(1L));
                }
            }

            TimedFlag.Timed<Integer> feed = plot.getFlag(FeedFlag.class);
            if (feed != null && feed.getInterval() != 0 && feed.getValue() != 0) {
                feedRunnable
                    .put(player.getUUID(), new Interval(feed.getInterval(), feed.getValue(), 20));
            }
            TimedFlag.Timed<Integer> heal = plot.getFlag(HealFlag.class);
            if (heal != null && heal.getInterval() != 0 && heal.getValue() != 0) {
                healRunnable
                    .put(player.getUUID(), new Interval(heal.getInterval(), heal.getValue(), 20));
            }
            return true;
        }
        return true;
    }

    public boolean plotExit(final PlotPlayer<?> player, Plot plot) {
        Object previous = player.deleteMeta(PlotPlayer.META_LAST_PLOT);
        this.eventDispatcher.callLeave(player, plot);
        if (plot.hasOwner()) {
            PlotArea pw = plot.getArea();
            if (pw == null) {
                return true;
            }
            if (plot.getFlag(DenyExitFlag.class) && !Permissions
                .hasPermission(player, Captions.PERMISSION_ADMIN_EXIT_DENIED) && !player
                .getMeta("kick", false)) {
                if (previous != null) {
                    player.setMeta(PlotPlayer.META_LAST_PLOT, previous);
                }
                return false;
            }
            if (!plot.getFlag(GamemodeFlag.class).equals(GamemodeFlag.DEFAULT) || !plot
                .getFlag(GuestGamemodeFlag.class).equals(GamemodeFlag.DEFAULT)) {
                if (player.getGameMode() != pw.getGameMode()) {
                    if (!Permissions.hasPermission(player, "plots.gamemode.bypass")) {
                        player.setGameMode(pw.getGameMode());
                    } else {
                        MainUtil.sendMessage(player, StringMan
                            .replaceAll(Captions.GAMEMODE_WAS_BYPASSED.getTranslated(), "{plot}",
                                plot.toString(), "{gamemode}",
                                pw.getGameMode().getName().toLowerCase()));
                    }
                }
            }

            final String farewell = plot.getFlag(FarewellFlag.class);
            if (!farewell.isEmpty()) {
                plot.format(Captions.PREFIX_FAREWELL.getTranslated() + farewell, player, false)
                    .thenAcceptAsync(player::sendMessage);
            }

            if (plot.getFlag(NotifyLeaveFlag.class)) {
                if (!Permissions.hasPermission(player, "plots.flag.notify-enter.bypass")) {
                    for (UUID uuid : plot.getOwners()) {
                        final PlotPlayer owner = PlotSquared.platform().getPlayerManager().getPlayerIfExists(uuid);
                        if ((owner != null) && !owner.getUUID().equals(player.getUUID())) {
                            MainUtil.sendMessage(owner, Captions.NOTIFY_LEAVE.getTranslated()
                                .replace("%player", player.getName())
                                .replace("%plot", plot.getId().toString()));
                        }
                    }
                }
            }

            final FlyFlag.FlyStatus flyStatus = plot.getFlag(FlyFlag.class);
            if (flyStatus != FlyFlag.FlyStatus.DEFAULT) {
                if (player.hasPersistentMeta("flight")) {
                    player.setFlight(
                        ByteArrayUtilities.bytesToBoolean(player.getPersistentMeta("flight")));
                    player.removePersistentMeta("flight");
                } else {
                    GameMode gameMode = player.getGameMode();
                    if (gameMode == GameModes.SURVIVAL || gameMode == GameModes.ADVENTURE) {
                        player.setFlight(false);
                    } else if (!player.getFlight()) {
                        player.setFlight(true);
                    }
                }
            }

            if (plot.getFlag(TimeFlag.class) != TimeFlag.TIME_DISABLED.getValue().longValue()) {
                player.setTime(Long.MAX_VALUE);
            }

            final PlotWeather plotWeather = plot.getFlag(WeatherFlag.class);
            if (plotWeather != PlotWeather.CLEAR) {
                player.setWeather(PlotWeather.RESET);
            }

            Location lastLoc = player.getMeta("music");
            if (lastLoc != null) {
                player.deleteMeta("music");
                player.playMusic(lastLoc, ItemTypes.AIR);
            }

            feedRunnable.remove(player.getUUID());
            healRunnable.remove(player.getUUID());
        }
        return true;
    }

    public void logout(UUID uuid) {
        feedRunnable.remove(uuid);
        healRunnable.remove(uuid);
    }

    private static class Interval {

        final int interval;
        final int amount;
        final int max;
        int count = 0;

        Interval(int interval, int amount, int max) {
            this.interval = interval;
            this.amount = amount;
            this.max = max;
        }
    }
}
