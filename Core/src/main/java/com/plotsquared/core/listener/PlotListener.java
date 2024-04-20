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

import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.configuration.Settings;
import com.plotsquared.core.configuration.caption.Caption;
import com.plotsquared.core.configuration.caption.StaticCaption;
import com.plotsquared.core.configuration.caption.TranslatableCaption;
import com.plotsquared.core.events.PlotFlagRemoveEvent;
import com.plotsquared.core.events.Result;
import com.plotsquared.core.location.Location;
import com.plotsquared.core.permissions.Permission;
import com.plotsquared.core.player.MetaDataAccess;
import com.plotsquared.core.player.PlayerMetaDataKeys;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.PlotArea;
import com.plotsquared.core.plot.PlotTitle;
import com.plotsquared.core.plot.PlotWeather;
import com.plotsquared.core.plot.comment.CommentManager;
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
import com.plotsquared.core.plot.flag.implementations.PlotTitleFlag;
import com.plotsquared.core.plot.flag.implementations.ServerPlotFlag;
import com.plotsquared.core.plot.flag.implementations.TimeFlag;
import com.plotsquared.core.plot.flag.implementations.TitlesFlag;
import com.plotsquared.core.plot.flag.implementations.WeatherFlag;
import com.plotsquared.core.plot.flag.types.TimedFlag;
import com.plotsquared.core.util.EventDispatcher;
import com.plotsquared.core.util.task.TaskManager;
import com.plotsquared.core.util.task.TaskTime;
import com.sk89q.worldedit.world.gamemode.GameMode;
import com.sk89q.worldedit.world.gamemode.GameModes;
import com.sk89q.worldedit.world.item.ItemType;
import com.sk89q.worldedit.world.item.ItemTypes;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class PlotListener {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    private final HashMap<UUID, Interval> feedRunnable = new HashMap<>();
    private final HashMap<UUID, Interval> healRunnable = new HashMap<>();
    private final Map<UUID, List<StatusEffect>> playerEffects = new HashMap<>();

    private final EventDispatcher eventDispatcher;

    public PlotListener(final @Nullable EventDispatcher eventDispatcher) {
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
                        final PlotPlayer<?> player = PlotSquared.platform().playerManager().getPlayerIfExists(entry.getKey());
                        if (player == null) {
                            iterator.remove();
                            continue;
                        }
                        // Don't attempt to heal dead players - they will get stuck in the abyss (#4406)
                        if (PlotSquared.platform().worldUtil().getHealth(player) <= 0) {
                            continue;
                        }
                        double level = PlotSquared.platform().worldUtil().getHealth(player);
                        if (level != value.max) {
                            PlotSquared.platform().worldUtil().setHealth(player, Math.min(level + value.amount, value.max));
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
                        final PlotPlayer<?> player = PlotSquared.platform().playerManager().getPlayerIfExists(entry.getKey());
                        if (player == null) {
                            iterator.remove();
                            continue;
                        }
                        int level = PlotSquared.platform().worldUtil().getFoodLevel(player);
                        if (level != value.max) {
                            PlotSquared.platform().worldUtil().setFoodLevel(player, Math.min(level + value.amount, value.max));
                        }
                    }
                }
            }

            if (!playerEffects.isEmpty()) {
                long currentTime = System.currentTimeMillis();
                for (Iterator<Map.Entry<UUID, List<StatusEffect>>> iterator =
                     playerEffects.entrySet().iterator(); iterator.hasNext(); ) {
                    Map.Entry<UUID, List<StatusEffect>> entry = iterator.next();
                    List<StatusEffect> effects = entry.getValue();
                    effects.removeIf(effect -> currentTime > effect.expiresAt);
                    if (effects.isEmpty()) {
                        iterator.remove();
                    }
                }
            }
        }, TaskTime.seconds(1L));
    }

    public boolean plotEntry(final PlotPlayer<?> player, final Plot plot) {
        if (plot.isDenied(player.getUUID()) && !player.hasPermission("plots.admin.entry.denied")) {
            player.sendMessage(
                    TranslatableCaption.of("deny.no_enter"),
                    TagResolver.resolver("plot", Tag.inserting(Component.text(plot.toString())))
            );
            return false;
        }
        try (final MetaDataAccess<Plot> lastPlot = player.accessTemporaryMetaData(PlayerMetaDataKeys.TEMPORARY_LAST_PLOT)) {
            Plot last = lastPlot.get().orElse(null);
            if ((last != null) && !last.getId().equals(plot.getId())) {
                plotExit(player, last);
            }
            if (PlotSquared.platform().expireManager() != null) {
                PlotSquared.platform().expireManager().handleEntry(player, plot);
            }
            lastPlot.set(plot);
        }
        this.eventDispatcher.callEntry(player, plot);
        if (plot.hasOwner()) {
            // This will inherit values from PlotArea
            final TitlesFlag.TitlesFlagValue titlesFlag = plot.getFlag(TitlesFlag.class);
            final boolean titles;
            if (titlesFlag == TitlesFlag.TitlesFlagValue.NONE) {
                titles = Settings.Titles.DISPLAY_TITLES;
            } else {
                titles = titlesFlag == TitlesFlag.TitlesFlagValue.TRUE;
            }

            String greeting = plot.getFlag(GreetingFlag.class);
            if (!greeting.isEmpty()) {
                if (!Settings.Chat.NOTIFICATION_AS_ACTIONBAR) {
                    plot.format(StaticCaption.of(greeting), player, false).thenAcceptAsync(player::sendMessage);
                } else {
                    plot.format(StaticCaption.of(greeting), player, false).thenAcceptAsync(player::sendActionBar);
                }
            }

            if (plot.getFlag(NotifyEnterFlag.class)) {
                if (!player.hasPermission("plots.flag.notify-enter.bypass")) {
                    for (UUID uuid : plot.getOwners()) {
                        final PlotPlayer<?> owner = PlotSquared.platform().playerManager().getPlayerIfExists(uuid);
                        if (owner != null && !owner.getUUID().equals(player.getUUID()) && owner.canSee(player)) {
                            Caption caption = TranslatableCaption.of("notification.notify_enter");
                            notifyPlotOwner(player, plot, owner, caption);
                        }
                    }
                }
            }

            final FlyFlag.FlyStatus flyStatus = plot.getFlag(FlyFlag.class);
            if (!player.hasPermission(Permission.PERMISSION_ADMIN_FLIGHT)) {
                if (flyStatus != FlyFlag.FlyStatus.DEFAULT) {
                    boolean flight = player.getFlight();
                    GameMode gamemode = player.getGameMode();
                    if (flight != (gamemode == GameModes.CREATIVE || gamemode == GameModes.SPECTATOR)) {
                        try (final MetaDataAccess<Boolean> metaDataAccess = player.accessPersistentMetaData(PlayerMetaDataKeys.PERSISTENT_FLIGHT)) {
                            metaDataAccess.set(player.getFlight());
                        }
                    }
                    player.setFlight(flyStatus == FlyFlag.FlyStatus.ENABLED);
                }
            }

            final GameMode gameMode = plot.getFlag(GamemodeFlag.class);
            if (!gameMode.equals(GamemodeFlag.DEFAULT)) {
                if (player.getGameMode() != gameMode) {
                    if (!player.hasPermission("plots.gamemode.bypass")) {
                        player.setGameMode(gameMode);
                    } else {
                        player.sendMessage(
                                TranslatableCaption.of("gamemode.gamemode_was_bypassed"),
                                TagResolver.builder()
                                        .tag("gamemode", Tag.inserting(Component.text(gameMode.toString())))
                                        .tag("plot", Tag.inserting(Component.text(plot.getId().toString())))
                                        .build()
                        );
                    }
                }
            }

            final GameMode guestGameMode = plot.getFlag(GuestGamemodeFlag.class);
            if (!guestGameMode.equals(GamemodeFlag.DEFAULT)) {
                if (player.getGameMode() != guestGameMode && !plot.isAdded(player.getUUID())) {
                    if (!player.hasPermission("plots.gamemode.bypass")) {
                        player.setGameMode(guestGameMode);
                    } else {
                        player.sendMessage(
                                TranslatableCaption.of("gamemode.gamemode_was_bypassed"),
                                TagResolver.builder()
                                        .tag("gamemode", Tag.inserting(Component.text(guestGameMode.toString())))
                                        .tag("plot", Tag.inserting(Component.text(plot.getId().toString())))
                                        .build()
                        );
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

            try (final MetaDataAccess<Location> musicMeta =
                         player.accessTemporaryMetaData(PlayerMetaDataKeys.TEMPORARY_MUSIC)) {
                if (musicFlag != null) {
                    final String rawId = musicFlag.getId();
                    if (rawId.contains("disc") || musicFlag == ItemTypes.AIR) {
                        Location location = player.getLocation();
                        Location lastLocation = musicMeta.get().orElse(null);
                        if (lastLocation != null) {
                            plot.getCenter(center -> player.playMusic(center.add(0, Short.MAX_VALUE, 0), musicFlag));
                            if (musicFlag == ItemTypes.AIR) {
                                musicMeta.remove();
                            }
                        }
                        if (musicFlag != ItemTypes.AIR) {
                            try {
                                musicMeta.set(location);
                                plot.getCenter(center -> player.playMusic(center.add(0, Short.MAX_VALUE, 0), musicFlag));
                            } catch (Exception ignored) {
                            }
                        }
                    }
                } else {
                    musicMeta.get().ifPresent(lastLoc -> {
                        musicMeta.remove();
                        player.playMusic(lastLoc, ItemTypes.AIR);
                    });
                }
            }

            CommentManager.sendTitle(player, plot);

            if (titles && !player.getAttribute("disabletitles")) {
                String title;
                String subtitle;
                PlotTitle titleFlag = plot.getFlag(PlotTitleFlag.class);
                boolean fromFlag;
                if (titleFlag.title() != null && titleFlag.subtitle() != null) {
                    title = titleFlag.title();
                    subtitle = titleFlag.subtitle();
                    fromFlag = true;
                } else {
                    title = "";
                    subtitle = "";
                    fromFlag = false;
                }
                if (fromFlag || !plot.getFlag(ServerPlotFlag.class) || Settings.Titles.DISPLAY_DEFAULT_ON_SERVER_PLOT) {
                    TaskManager.runTaskLaterAsync(() -> {
                        Plot lastPlot;
                        try (final MetaDataAccess<Plot> lastPlotAccess =
                                     player.accessTemporaryMetaData(PlayerMetaDataKeys.TEMPORARY_LAST_PLOT)) {
                            lastPlot = lastPlotAccess.get().orElse(null);
                        }
                        if ((lastPlot != null) && plot.getId().equals(lastPlot.getId()) && plot.hasOwner()) {
                            final UUID plotOwner = plot.getOwnerAbs();
                            Caption header = fromFlag ? StaticCaption.of(title) : TranslatableCaption.of("titles" +
                                    ".title_entered_plot");
                            Caption subHeader = fromFlag ? StaticCaption.of(subtitle) : TranslatableCaption.of("titles" +
                                    ".title_entered_plot_sub");

                            CompletableFuture<TagResolver> future = PlotSquared.platform().playerManager()
                                    .getUsernameCaption(plotOwner).thenApply(caption -> TagResolver.builder()
                                            .tag("owner", Tag.inserting(caption.toComponent(player)))
                                            .tag("plot", Tag.inserting(Component.text(lastPlot.getId().toString())))
                                            .tag("world", Tag.inserting(Component.text(player.getLocation().getWorldName())))
                                            .tag("alias", Tag.inserting(Component.text(plot.getAlias())))
                                            .build()
                                    );

                            future.whenComplete((tagResolver, throwable) -> {
                                if (Settings.Titles.TITLES_AS_ACTIONBAR) {
                                    player.sendActionBar(header, tagResolver);
                                } else {
                                    player.sendTitle(header, subHeader, tagResolver);
                                }
                            });
                        }
                    }, TaskTime.seconds(1L));
                }
            }

            TimedFlag.Timed<Integer> feed = plot.getFlag(FeedFlag.class);
            if (feed.interval() != 0 && feed.value() != 0) {
                feedRunnable
                        .put(player.getUUID(), new Interval(feed.interval(), feed.value(), 20));
            }
            TimedFlag.Timed<Integer> heal = plot.getFlag(HealFlag.class);
            if (heal.interval() != 0 && heal.value() != 0) {
                healRunnable
                        .put(player.getUUID(), new Interval(heal.interval(), heal.value(), 20));
            }
            return true;
        }
        return true;
    }

    public boolean plotExit(final PlotPlayer<?> player, Plot plot) {
        try (final MetaDataAccess<Plot> lastPlot = player.accessTemporaryMetaData(PlayerMetaDataKeys.TEMPORARY_LAST_PLOT)) {
            final Plot previous = lastPlot.remove();

            List<StatusEffect> effects = playerEffects.remove(player.getUUID());
            if (effects != null) {
                long currentTime = System.currentTimeMillis();
                effects.forEach(effect -> {
                    if (currentTime <= effect.expiresAt) {
                        player.removeEffect(effect.name);
                    }
                });
            }

            if (plot.hasOwner()) {
                PlotArea pw = plot.getArea();
                if (pw == null) {
                    return true;
                }
                try (final MetaDataAccess<Boolean> kickAccess =
                             player.accessTemporaryMetaData(PlayerMetaDataKeys.TEMPORARY_KICK)) {
                    if (plot.getFlag(DenyExitFlag.class) && !player.hasPermission(Permission.PERMISSION_ADMIN_EXIT_DENIED) &&
                            !kickAccess.get().orElse(false)) {
                        if (previous != null) {
                            lastPlot.set(previous);
                        }
                        return false;
                    }
                }
                if (!plot.getFlag(GamemodeFlag.class).equals(GamemodeFlag.DEFAULT) || !plot
                        .getFlag(GuestGamemodeFlag.class).equals(GamemodeFlag.DEFAULT)) {
                    if (player.getGameMode() != pw.getGameMode()) {
                        if (!player.hasPermission("plots.gamemode.bypass")) {
                            player.setGameMode(pw.getGameMode());
                        } else {
                            player.sendMessage(
                                    TranslatableCaption.of("gamemode.gamemode_was_bypassed"),
                                    TagResolver.builder()
                                            .tag("gamemode", Tag.inserting(Component.text(pw.getGameMode().toString())))
                                            .tag("plot", Tag.inserting(Component.text(plot.toString())))
                                            .build()
                            );
                        }
                    }
                }

                String farewell = plot.getFlag(FarewellFlag.class);
                if (!farewell.isEmpty()) {
                    if (!Settings.Chat.NOTIFICATION_AS_ACTIONBAR) {
                        plot.format(StaticCaption.of(farewell), player, false).thenAcceptAsync(player::sendMessage);
                    } else {
                        plot.format(StaticCaption.of(farewell), player, false).thenAcceptAsync(player::sendActionBar);
                    }
                }

                if (plot.getFlag(NotifyLeaveFlag.class)) {
                    if (!player.hasPermission("plots.flag.notify-leave.bypass")) {
                        for (UUID uuid : plot.getOwners()) {
                            final PlotPlayer<?> owner = PlotSquared.platform().playerManager().getPlayerIfExists(uuid);
                            if ((owner != null) && !owner.getUUID().equals(player.getUUID()) && owner.canSee(player)) {
                                Caption caption = TranslatableCaption.of("notification.notify_leave");
                                notifyPlotOwner(player, plot, owner, caption);
                            }
                        }
                    }
                }

                final FlyFlag.FlyStatus flyStatus = plot.getFlag(FlyFlag.class);
                if (flyStatus != FlyFlag.FlyStatus.DEFAULT) {
                    try (final MetaDataAccess<Boolean> metaDataAccess = player.accessPersistentMetaData(PlayerMetaDataKeys.PERSISTENT_FLIGHT)) {
                        final Optional<Boolean> value = metaDataAccess.get();
                        if (value.isPresent()) {
                            player.setFlight(value.get());
                            metaDataAccess.remove();
                        } else {
                            GameMode gameMode = player.getGameMode();
                            if (gameMode == GameModes.SURVIVAL || gameMode == GameModes.ADVENTURE) {
                                player.setFlight(false);
                            } else if (!player.getFlight()) {
                                player.setFlight(true);
                            }
                        }
                    }
                }

                if (plot.getFlag(TimeFlag.class) != TimeFlag.TIME_DISABLED.getValue().longValue()) {
                    player.setTime(Long.MAX_VALUE);
                }

                final PlotWeather plotWeather = plot.getFlag(WeatherFlag.class);
                if (plotWeather != PlotWeather.OFF) {
                    player.setWeather(PlotWeather.WORLD);
                }

                try (final MetaDataAccess<Location> musicAccess =
                             player.accessTemporaryMetaData(PlayerMetaDataKeys.TEMPORARY_MUSIC)) {
                    musicAccess.get().ifPresent(lastLoc -> {
                        musicAccess.remove();
                        player.playMusic(lastLoc, ItemTypes.AIR);
                    });
                }

                feedRunnable.remove(player.getUUID());
                healRunnable.remove(player.getUUID());
            }
        } finally {
            this.eventDispatcher.callLeave(player, plot);
        }
        return true;
    }

    private void notifyPlotOwner(final PlotPlayer<?> player, final Plot plot, final PlotPlayer<?> owner, final Caption caption) {
        TagResolver resolver = TagResolver.builder()
                .tag("player", Tag.inserting(Component.text(player.getName())))
                .tag("plot", Tag.inserting(Component.text(plot.getId().toString())))
                .tag("area", Tag.inserting(Component.text(String.valueOf(plot.getArea()))))
                .build();
        if (!Settings.Chat.NOTIFICATION_AS_ACTIONBAR) {
            owner.sendMessage(caption, resolver);
        } else {
            owner.sendActionBar(caption, resolver);
        }
    }

    public void logout(UUID uuid) {
        feedRunnable.remove(uuid);
        healRunnable.remove(uuid);
        playerEffects.remove(uuid);
    }

    /**
     * Marks an effect as a status effect that will be removed on leaving a plot
     *
     * @param uuid      The uuid of the player the effect belongs to
     * @param name      The name of the status effect
     * @param expiresAt The time when the effect expires
     * @since 6.10.0
     */
    public void addEffect(@NonNull UUID uuid, @NonNull String name, long expiresAt) {
        List<StatusEffect> effects = playerEffects.getOrDefault(uuid, new ArrayList<>());
        effects.removeIf(effect -> effect.name.equals(name));
        if (expiresAt != -1) {
            effects.add(new StatusEffect(name, expiresAt));
        }
        playerEffects.put(uuid, effects);
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

    private record StatusEffect(@NonNull String name, long expiresAt) {

        private StatusEffect(@NonNull String name, long expiresAt) {
            this.name = name;
            this.expiresAt = expiresAt;
        }

    }

}
