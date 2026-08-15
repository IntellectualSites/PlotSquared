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
package com.plotsquared.core.util;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.command.Command;
import com.plotsquared.core.command.CommandCategory;
import com.plotsquared.core.command.RequiredType;
import com.plotsquared.core.configuration.Settings;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.PlotArea;
import com.plotsquared.core.uuid.UUIDMapping;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Tab completion utilities
 */
public final class TabCompletions {

    private static final Cache<String, List<String>> cachedCompletionValues =
            CacheBuilder.newBuilder()
                    .expireAfterWrite(Settings.Tab_Completions.CACHE_EXPIRATION, TimeUnit.SECONDS)
                    .build();

    private static final Command booleanTrueCompletion = new Command(null, false, "true", "",
            RequiredType.NONE, null
    ) {
    };
    private static final Command booleanFalseCompletion = new Command(null, false, "false", "",
            RequiredType.NONE, null
    ) {
    };

    private TabCompletions() {
        throw new UnsupportedOperationException(
                "This is a utility class and cannot be instantiated");
    }

    /**
     * Get a list of tab completions corresponding to player names. This uses the UUID pipeline
     * cache, so it will complete will all names known to PlotSquared
     *
     * @param input    Command input
     * @param issuer   The player who issued the tab completion
     * @param existing Players that should not be included in completions
     * @return List of completions
     * @since 6.1.3
     */
    public static @NonNull List<Command> completePlayers(
            final @NonNull PlotPlayer<?> issuer,
            final @NonNull String input,
            final @NonNull List<String> existing
    ) {
        return completePlayers("players", issuer, input, existing, uuid -> true);
    }

    /**
     * Get a list of tab completions corresponding to player names added to the given plot.
     *
     * @param issuer   The player who issued the tab completion
     * @param plot     Plot to complete added players for
     * @param input    Command input
     * @param existing Players that should not be included in completions
     * @return List of completions
     * @since 6.1.3
     */
    public static @NonNull List<Command> completeAddedPlayers(
            final @NonNull PlotPlayer<?> issuer,
            final @NonNull Plot plot,
            final @NonNull String input, final @NonNull List<String> existing
    ) {
        return completePlayers("added" + plot, issuer, input, existing,
                uuid -> plot.getMembers().contains(uuid)
                        || plot.getTrusted().contains(uuid)
                        || plot.getDenied().contains(uuid)
        );
    }

    public static @NonNull List<Command> completePlayersInPlot(
            final @NonNull PlotPlayer<?> issuer,
            final @NonNull Plot plot,
            final @NonNull String input, final @NonNull List<String> existing
    ) {
        List<String> players = cachedCompletionValues.getIfPresent("inPlot" + plot);
        if (players == null) {
            final List<PlotPlayer<?>> inPlot = plot.getPlayersInPlot();
            players = new ArrayList<>(inPlot.size());
            for (PlotPlayer<?> player : inPlot) {
                if (issuer.canSee(player)) {
                    players.add(player.getName());
                }
            }
            cachedCompletionValues.put("inPlot" + plot, players);
        }
        return filterCached(players, input, existing);
    }

    /**
     * Get a list of completions corresponding to WorldEdit(/FastAsyncWorldEdit) patterns. This uses
     * WorldEdit's pattern completer internally.
     *
     * @param input Command input
     * @return List of completions
     */
    public static @NonNull List<Command> completePatterns(final @NonNull String input) {
        return PatternUtil.getSuggestions(input.trim()).stream()
                .map(value -> value.toLowerCase(Locale.ENGLISH).replace("minecraft:", ""))
                .filter(value -> value.startsWith(input.toLowerCase(Locale.ENGLISH)))
                .map(value -> new Command(null, false, value, "", RequiredType.NONE, null) {
                }).collect(Collectors.toList());
    }

    public static @NonNull List<Command> completeBoolean(final @NonNull String input) {
        if (input.isEmpty()) {
            return Arrays.asList(booleanTrueCompletion, booleanFalseCompletion);
        }
        if ("true".startsWith(input)) {
            return Collections.singletonList(booleanTrueCompletion);
        }
        if ("false".startsWith(input)) {
            return Collections.singletonList(booleanFalseCompletion);
        }
        return Collections.emptyList();
    }

    /**
     * Get a list of integer numbers matching the given input. If the input string
     * is empty, nothing will be returned. The list is unmodifiable.
     *
     * @param input        Input to filter with
     * @param amountLimit  Maximum amount of suggestions
     * @param highestLimit Highest number to include
     * @return Unmodifiable list of number completions
     */
    public static @NonNull List<Command> completeNumbers(
            final @NonNull String input,
            final int amountLimit, final int highestLimit
    ) {
        if (input.isEmpty() || input.length() > highestLimit || !MathMan.isInteger(input)) {
            return Collections.emptyList();
        }
        int offset;
        try {
            offset = Integer.parseInt(input) * 10;
        } catch (NumberFormatException ignored) {
            return Collections.emptyList();
        }
        final List<String> commands = new ArrayList<>();
        for (int i = offset; i <= highestLimit && (offset - i + amountLimit) > 0; i++) {
            commands.add(String.valueOf(i));
        }
        return asCompletions(commands.toArray(new String[0]));
    }

    /**
     * Get a list of plot areas matching the given input.
     * The list is unmodifiable.
     *
     * @param input Input to filter with
     * @return Unmodifiable list of area completions
     */
    public static @NonNull List<Command> completeAreas(final @NonNull String input) {
        final List<Command> completions = new ArrayList<>();
        for (final PlotArea area : PlotSquared.get().getPlotAreaManager().getAllPlotAreas()) {
            String areaName = area.getWorldName();
            if (area.getId() != null) {
                areaName += ";" + area.getId();
            }
            if (!areaName.toLowerCase().startsWith(input.toLowerCase())) {
                continue;
            }
            completions.add(new Command(null, false, areaName, "",
                    RequiredType.NONE, null
            ) {
            });
        }
        return Collections.unmodifiableList(completions);
    }

    public static @NonNull List<Command> asCompletions(String... toFilter) {
        final List<Command> completions = new ArrayList<>();
        for (String completion : toFilter) {
            completions.add(new Command(null, false, completion, "",
                    RequiredType.NONE, null
            ) {
            });
        }
        return Collections.unmodifiableList(completions);
    }

    /**
     * @param cacheIdentifier Cache key
     * @param issuer          The player who issued the tab completion
     * @param input           Command input
     * @param existing        Players that should not be included in completions
     * @param uuidFilter      Filter applied before caching values
     * @return List of completions
     * @since 6.1.3
     */
    private static List<Command> completePlayers(
            final @NonNull String cacheIdentifier,
            final @NonNull PlotPlayer<?> issuer,
            final @NonNull String input, final @NonNull List<String> existing,
            final @NonNull Predicate<UUID> uuidFilter
    ) {
        List<String> players;
        if (Settings.Enabled_Components.EXTENDED_USERNAME_COMPLETION) {
            players = cachedCompletionValues.getIfPresent(cacheIdentifier);
            if (players == null) {
                final Collection<UUIDMapping> mappings =
                        PlotSquared.get().getImpromptuUUIDPipeline().getAllImmediately();
                players = new ArrayList<>(mappings.size());
                for (final UUIDMapping mapping : mappings) {
                    if (uuidFilter.test(mapping.uuid())) {
                        players.add(mapping.username());
                    }
                }
                cachedCompletionValues.put(cacheIdentifier, players);
            }
        } else {
            final Collection<? extends PlotPlayer<?>> onlinePlayers = PlotSquared.platform().playerManager().getPlayers();
            players = new ArrayList<>(onlinePlayers.size());
            for (final PlotPlayer<?> player : onlinePlayers) {
                if (!uuidFilter.test(player.getUUID())) {
                    continue;
                }
                if (issuer != null && !issuer.canSee(player)) {
                    continue;
                }
                players.add(player.getName());
            }
        }
        return filterCached(players, input, existing);
    }

    private static List<Command> filterCached(
            Collection<String> playerNames, String input,
            List<String> existing
    ) {
        final String processedInput = input.toLowerCase(Locale.ENGLISH);
        return playerNames.stream().filter(player -> player.toLowerCase(Locale.ENGLISH).startsWith(processedInput))
                .filter(player -> !existing.contains(player)).map(
                        player -> new Command(null, false, player, "", RequiredType.NONE,
                                CommandCategory.INFO
                        ) {
                        })
                /* If there are more than 200 suggestions, just send the first 200 */
                .limit(200)
                .collect(Collectors.toList());
    }

}
