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
import com.plotsquared.core.player.MetaDataAccess;
import com.plotsquared.core.player.PlayerMetaDataKeys;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.flag.GlobalFlagContainer;
import com.plotsquared.core.plot.flag.PlotFlag;
import com.plotsquared.core.plot.flag.implementations.DoneFlag;
import com.plotsquared.core.plot.flag.implementations.ServerPlotFlag;
import com.plotsquared.core.util.EventDispatcher;
import com.plotsquared.core.util.PlayerManager;
import com.plotsquared.core.util.query.PlotQuery;
import net.kyori.adventure.text.Component;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

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
        return PlotSquared.platform().toLegacyPlatformString(caption.toComponent(localeHolder).asComponent());
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
        this.createPlaceholder("base_plot_count", player -> Integer.toString(PlotQuery.newQuery()
                .ownedBy(player)
                .whereBasePlot()
                .thatPasses(plot -> !DoneFlag.isDone(plot))
                .count())
        );
        this.createPlaceholder("plot_count", player -> Integer.toString(player.getPlotCount()));
        this.createPlaceholder("currentplot_alias", (player, plot) -> {
            if (plot.getAlias().isEmpty()) {
                return legacyComponent(TranslatableCaption.of("info.none"), player);
            }
            return plot.getAlias();
        });
        this.createPlaceholder("currentplot_owner", (player, plot) -> {
            if (plot.getFlag(ServerPlotFlag.class)) {
                return legacyComponent(TranslatableCaption.of("info.server"), player);
            }
            final UUID plotOwner = plot.getOwnerAbs();
            if (plotOwner == null) {
                return legacyComponent(TranslatableCaption.of("generic.generic_unowned"), player);
            }
            try {
                return PlotSquared.platform().playerManager().getUsernameCaption(plotOwner)
                        .get(Settings.UUID.BLOCKING_TIMEOUT, TimeUnit.MILLISECONDS).getComponent(player);
            } catch (final Exception ignored) {
            }
            return legacyComponent(TranslatableCaption.of("info.unknown"), player);
        });
        this.createPlaceholder("currentplot_owners", (player, plot) -> {
            if (plot.getFlag(ServerPlotFlag.class)) {
                return legacyComponent(TranslatableCaption.of("info.server"), player);
            }
            final Set<UUID> plotOwners = plot.getOwners();
            if (plotOwners.isEmpty()) {
                return legacyComponent(TranslatableCaption.of("generic.generic_unowned"), player);
            }
            return plotOwners.stream().map(PlotSquared.platform().playerManager()::getUsernameCaption).map(f -> {
                try {
                    return f.get(Settings.UUID.BLOCKING_TIMEOUT, TimeUnit.MILLISECONDS).getComponent(player);
                } catch (final Exception ignored) {
                    return legacyComponent(TranslatableCaption.of("info.unknown"), player);
                }
            }).collect(Collectors.joining(", "));
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
            if (plot.getTimestamp() == 0 || !plot.hasOwner()) {
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
        this.createPlaceholder("currentplot_abs_x", (player, plot) -> Integer.toString(plot.getId().getX()), true);
        this.createPlaceholder("currentplot_abs_y", (player, plot) -> Integer.toString(plot.getId().getY()), true);
        this.createPlaceholder("currentplot_abs_xy", (player, plot) -> plot.getId().toString(), true);
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
        this.createPlaceholder("currentplot_size", (player, plot) -> String.valueOf(plot.getConnectedPlots().size()));
        this.createPlaceholder("total_grants", player -> {
            try (final MetaDataAccess<Integer> metaDataAccess = player.accessPersistentMetaData(PlayerMetaDataKeys.PERSISTENT_GRANTED_PLOTS)) {
                return Integer.toString(metaDataAccess.get().orElse(0));
            }
        });
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
        this.createPlaceholder(key, placeholderFunction, false);
    }

    /**
     * Create a functional placeholder
     *
     * @param key                 Placeholder key
     * @param placeholderFunction Placeholder generator. Cannot return null
     * @param requireAbsolute     If the plot given to the placeholder should be the absolute (not base) plot
     * @since 7.5.9
     */
    public void createPlaceholder(
            final @NonNull String key,
            final @NonNull BiFunction<PlotPlayer<?>, Plot, String> placeholderFunction,
            final boolean requireAbsolute
    ) {
        this.registerPlaceholder(new PlotSpecificPlaceholder(key, requireAbsolute) {
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
    public record PlaceholderAddedEvent(
            Placeholder placeholder
    ) {

    }

}
