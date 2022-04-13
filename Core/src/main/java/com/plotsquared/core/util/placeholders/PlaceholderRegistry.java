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
package com.plotsquared.core.util.placeholders;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.configuration.Settings;
import com.plotsquared.core.configuration.caption.LocaleHolder;
import com.plotsquared.core.configuration.caption.TranslatableCaption;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.flag.GlobalFlagContainer;
import com.plotsquared.core.plot.flag.PlotFlag;
import com.plotsquared.core.util.EventDispatcher;
import com.plotsquared.core.util.PlayerManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;
import java.util.function.BiFunction;

/**
 * Registry that contains {@link Placeholder placeholders}
 */
@Singleton
public final class PlaceholderRegistry {

    private final Map<String, Placeholder> placeholders;
    private final EventDispatcher eventDispatcher;

    @Inject
    public PlaceholderRegistry(final @NonNull EventDispatcher eventDispatcher) {
        this.placeholders = Maps.newHashMap();
        this.eventDispatcher = eventDispatcher;
        this.registerDefault();
    }

    /**
     * Converts a {@link Component} into a legacy-formatted string.
     *
     * @param caption      the caption key.
     * @param localeHolder the locale holder to get the component for
     * @return a legacy-formatted string.
     */
    private static String legacyComponent(TranslatableCaption caption, LocaleHolder localeHolder) {
        Component component = MiniMessage.get().parse(caption.getComponent(localeHolder));
        return PlotSquared.platform().toLegacyPlatformString(component);
    }

    private void registerDefault() {
        final GlobalFlagContainer globalFlagContainer = GlobalFlagContainer.getInstance();
        for (final PlotFlag<?, ?> flag : globalFlagContainer.getRecognizedPlotFlags()) {
            this.registerPlaceholder(new PlotFlagPlaceholder(flag, true));
            this.registerPlaceholder(new PlotFlagPlaceholder(flag, false));
        }
        GlobalFlagContainer.getInstance().subscribe((flag, type) -> {
            this.registerPlaceholder(new PlotFlagPlaceholder(flag, true));
            this.registerPlaceholder(new PlotFlagPlaceholder(flag, false));
        });
        this.createPlaceholder("world_name", player -> player.getLocation().getWorldName());
        this.createPlaceholder("has_plot", player -> player.getPlotCount() > 0 ? "true" : "false");
        this.createPlaceholder("allowed_plot_count", (player) -> {
            if (player.getAllowedPlots() >= Integer.MAX_VALUE) { // Beautifies cases with '*' permission
                return legacyComponent(TranslatableCaption.of("info.infinite"), player);
            }
            return Integer.toString(player.getAllowedPlots());
        });
        this.createPlaceholder("plot_count", player -> Integer.toString(player.getPlotCount()));
        this.createPlaceholder("currentplot_alias", (player, plot) -> {
            if (plot.getAlias().isEmpty()) {
                return legacyComponent(TranslatableCaption.of("info.none"), player);
            }
            return plot.getAlias();
        });
        this.createPlaceholder("currentplot_owner", (player, plot) -> {
            final UUID plotOwner = plot.getOwnerAbs();
            if (plotOwner == null) {
                return legacyComponent(TranslatableCaption.of("generic.generic_unowned"), player);
            }

            try {
                return PlayerManager.resolveName(plotOwner, false).getComponent(player);
            } catch (final Exception ignored) {
            }
            return legacyComponent(TranslatableCaption.of("info.unknown"), player);
        });
        this.createPlaceholder("currentplot_members", (player, plot) -> {
            if (plot.getMembers().isEmpty() && plot.getTrusted().isEmpty()) {
                return legacyComponent(TranslatableCaption.of("info.none"), player);
            }
            return String.valueOf(plot.getMembers().size() + plot.getTrusted().size());
        });
        this.createPlaceholder("currentplot_members_added", (player, plot) -> {
            if (plot.getMembers().isEmpty()) {
                return legacyComponent(TranslatableCaption.of("info.none"), player);
            }
            return String.valueOf(plot.getMembers().size());
        });
        this.createPlaceholder("currentplot_members_trusted", (player, plot) -> {
            if (plot.getTrusted().isEmpty()) {
                return legacyComponent(TranslatableCaption.of("info.none"), player);
            }
            return String.valueOf(plot.getTrusted().size());
        });
        this.createPlaceholder("currentplot_members_denied", (player, plot) -> {
            if (plot.getDenied().isEmpty()) {
                return legacyComponent(TranslatableCaption.of("info.none"), player);
            }
            return String.valueOf(plot.getDenied().size());
        });
        this.createPlaceholder("currentplot_members_trusted_list", (player, plot) -> {
            if (plot.getTrusted().isEmpty()) {
                return legacyComponent(TranslatableCaption.of("info.none"), player);
            }
            return PlotSquared.platform().toLegacyPlatformString(
                    PlayerManager.getPlayerList(plot.getTrusted(), player));
        });
        this.createPlaceholder("currentplot_members_added_list", (player, plot) -> {
            if (plot.getMembers().isEmpty()) {
                return legacyComponent(TranslatableCaption.of("info.none"), player);
            }
            return PlotSquared.platform().toLegacyPlatformString(
                    PlayerManager.getPlayerList(plot.getMembers(), player));
        });
        this.createPlaceholder("currentplot_members_denied_list", (player, plot) -> {
            if (plot.getDenied().isEmpty()) {
                return legacyComponent(TranslatableCaption.of("info.none"), player);
            }
            return PlotSquared.platform().toLegacyPlatformString(
                    PlayerManager.getPlayerList(plot.getDenied(), player));
        });
        this.createPlaceholder("currentplot_creationdate", (player, plot) -> {
            if (plot.getTimestamp() == 0) {
                return legacyComponent(TranslatableCaption.of("info.unknown"), player);
            }
            long creationDate = plot.getTimestamp();
            SimpleDateFormat sdf = new SimpleDateFormat(Settings.Timeformat.DATE_FORMAT);
            sdf.setTimeZone(TimeZone.getTimeZone(Settings.Timeformat.TIME_ZONE));
            return sdf.format(creationDate);
        });
        this.createPlaceholder("currentplot_can_build", (player, plot) ->
                plot.isAdded(player.getUUID()) ? "true" : "false");
        this.createPlaceholder("currentplot_x", (player, plot) -> Integer.toString(plot.getId().getX()));
        this.createPlaceholder("currentplot_y", (player, plot) -> Integer.toString(plot.getId().getY()));
        this.createPlaceholder("currentplot_xy", (player, plot) -> plot.getId().toString());
        this.createPlaceholder("currentplot_rating", (player, plot) -> {
            if (Double.isNaN(plot.getAverageRating())) {
                return legacyComponent(TranslatableCaption.of("placeholder.nan"), player);
            }
            BigDecimal roundRating = BigDecimal.valueOf(plot.getAverageRating()).setScale(2, RoundingMode.HALF_UP);
            if (!Settings.General.SCIENTIFIC) {
                return String.valueOf(roundRating);
            } else {
                return Double.toString(plot.getAverageRating());
            }
        });
        this.createPlaceholder("currentplot_biome", (player, plot) -> plot.getBiomeSynchronous().toString());
    }

    /**
     * Create a functional placeholder
     *
     * @param key                 Placeholder key
     * @param placeholderFunction Placeholder generator. Cannot return null
     */
    @SuppressWarnings("ALL")
    public void createPlaceholder(
            final @NonNull String key,
            final @NonNull Function<PlotPlayer<?>, String> placeholderFunction
    ) {
        this.registerPlaceholder(new Placeholder(key) {
            @Override
            public @NonNull String getValue(final @NonNull PlotPlayer<?> player) {
                return placeholderFunction.apply(player);
            }
        });
    }

    /**
     * Create a functional placeholder
     *
     * @param key                 Placeholder key
     * @param placeholderFunction Placeholder generator. Cannot return null
     */
    public void createPlaceholder(
            final @NonNull String key,
            final @NonNull BiFunction<PlotPlayer<?>, Plot, String> placeholderFunction
    ) {
        this.registerPlaceholder(new PlotSpecificPlaceholder(key) {
            @Override
            public @NonNull String getValue(final @NonNull PlotPlayer<?> player, final @NonNull Plot plot) {
                return placeholderFunction.apply(player, plot);
            }
        });
    }

    /**
     * Register a placeholder
     *
     * @param placeholder Placeholder instance
     */
    public void registerPlaceholder(final @NonNull Placeholder placeholder) {
        final Placeholder previous = this.placeholders
                .put(
                        placeholder.getKey().toLowerCase(Locale.ENGLISH),
                        Preconditions.checkNotNull(placeholder, "Placeholder may not be null")
                );
        if (previous == null) {
            this.eventDispatcher.callGenericEvent(new PlaceholderAddedEvent(placeholder));
        }
    }

    /**
     * Get a placeholder instance from its key
     *
     * @param key Placeholder key
     * @return Placeholder value
     */
    public @Nullable Placeholder getPlaceholder(final @NonNull String key) {
        return this.placeholders.get(
                Preconditions.checkNotNull(key, "Key may not be null").toLowerCase(Locale.ENGLISH));
    }

    /**
     * Get the placeholder value evaluated for a player, and catch and deal with any problems
     * occurring while doing so
     *
     * @param key    Placeholder key
     * @param player Player to evaluate for
     * @return Replacement value
     */
    public @NonNull String getPlaceholderValue(
            final @NonNull String key,
            final @NonNull PlotPlayer<?> player
    ) {
        final Placeholder placeholder = getPlaceholder(key);
        if (placeholder == null) {
            return "";
        }
        String placeholderValue = "";
        try {
            placeholderValue = placeholder.getValue(player);
            // If a placeholder for some reason decides to be disobedient, we catch it here
            if (placeholderValue == null) {
                new RuntimeException(String
                        .format("Placeholder '%s' returned null for player '%s'", placeholder.getKey(),
                                player.getName()
                        )).printStackTrace();
            }
        } catch (final Exception exception) {
            new RuntimeException(String
                    .format("Placeholder '%s' failed to evalulate for player '%s'",
                            placeholder.getKey(), player.getName()
                    ), exception).printStackTrace();
        }
        return placeholderValue;
    }

    /**
     * Get all placeholders
     *
     * @return Unmodifiable collection of placeholders
     */
    public @NonNull Collection<Placeholder> getPlaceholders() {
        return Collections.unmodifiableCollection(this.placeholders.values());
    }

    /**
     * Event called when a new {@link Placeholder} has been added
     */
    public static class PlaceholderAddedEvent {

        private final Placeholder placeholder;

        public PlaceholderAddedEvent(Placeholder placeholder) {
            this.placeholder = placeholder;
        }

        public Placeholder getPlaceholder() {
            return this.placeholder;
        }

    }

}
