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
import com.plotsquared.core.uuid.UUIDMapping;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;

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
@UtilityClass
public class TabCompletions {

    private final Cache<String, List<String>> cachedCompletionValues =
        CacheBuilder.newBuilder().expireAfterWrite(1, TimeUnit.MINUTES).build();

    private final Command booleanTrueCompletion = new Command(null, false, "true", "",
            RequiredType.NONE, null) {};
    private final Command booleanFalseCompletion = new Command(null, false, "false", "",
            RequiredType.NONE, null) {};

    /**
     * Get a list of tab completions corresponding to player names. This uses the UUID pipeline
     * cache, so it will complete will all names known to PlotSquared
     *
     * @param input    Command input
     * @param existing Players that should not be included in completions
     * @return List of completions
     */
    @NotNull public List<Command> completePlayers(@NotNull final String input,
        @NotNull final List<String> existing) {
        return completePlayers("players", input, existing, uuid -> true);
    }

    /**
     * Get a list of tab completions corresponding to player names added to the given plot.
     *
     * @param plot     Plot to complete added players for
     * @param input    Command input
     * @param existing Players that should not be included in completions
     * @return List of completions
     */
    @NotNull public List<Command> completeAddedPlayers(@NotNull final Plot plot, @NotNull final String input,
                                                       @NotNull final List<String> existing) {
        return completePlayers("added" + plot, input, existing,
                uuid -> plot.getMembers().contains(uuid)
                || plot.getTrusted().contains(uuid)
                || plot.getDenied().contains(uuid));
    }

    @NotNull public List<Command> completePlayersInPlot(@NotNull final Plot plot, @NotNull final String input,
                                                        @NotNull final List<String> existing) {
        List<String> players = cachedCompletionValues.getIfPresent("inPlot" + plot);
        if (players == null) {
            final List<PlotPlayer<?>> inPlot = plot.getPlayersInPlot();
            players = new ArrayList<>(inPlot.size());
            for (PlotPlayer<?> player : inPlot) {
                players.add(player.getName());
            }
            cachedCompletionValues.put("inPlot" + plot, players);
        }
        return filterCached(players, input, existing);
    }

    /**
     * Get a list of completions corresponding to WorldEdit(/FAWE) patterns. This uses
     * WorldEdit's pattern completer internally.
     *
     * @param input Command input
     * @return List of completions
     */
    @NotNull public List<Command> completePatterns(@NotNull final String input) {
        return PatternUtil.getSuggestions(input.trim()).stream()
            .map(value -> value.toLowerCase(Locale.ENGLISH).replace("minecraft:", ""))
            .filter(value -> value.startsWith(input.toLowerCase(Locale.ENGLISH)))
            .map(value -> new Command(null, false, value, "", RequiredType.NONE, null) {
            }).collect(Collectors.toList());
    }

    @NotNull public List<Command> completeBoolean(@NotNull final String input) {
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
     * @param cacheIdentifier Cache key
     * @param input           Command input
     * @param existing        Players that should not be included in completions
     * @param uuidFilter      Filter applied before caching values
     * @return List of completions
     */
    private List<Command> completePlayers(@NotNull final String cacheIdentifier, @NotNull final String input,
                                          @NotNull final List<String> existing,
                                          @NotNull final Predicate<UUID> uuidFilter) {
        List<String> players;
        if (Settings.Enabled_Components.EXTENDED_USERNAME_COMPLETION) {
            players = cachedCompletionValues.getIfPresent(cacheIdentifier);
            if (players == null) {
                final Collection<UUIDMapping> mappings =
                        PlotSquared.get().getImpromptuUUIDPipeline().getAllImmediately();
                players = new ArrayList<>(mappings.size());
                for (final UUIDMapping mapping : mappings) {
                    if (uuidFilter.test(mapping.getUuid())) {
                        players.add(mapping.getUsername());
                    }
                }
                cachedCompletionValues.put(cacheIdentifier, players);
            }
        } else {
            final Collection<? extends PlotPlayer<?>> onlinePlayers = PlotSquared.imp().getPlayerManager().getPlayers();
            players = new ArrayList<>(onlinePlayers.size());
            for (final PlotPlayer<?> player : onlinePlayers) {
                if (uuidFilter.test(player.getUUID())) {
                    players.add(player.getName());
                }
            }
        }
        return filterCached(players, input, existing);
    }

    private List<Command> filterCached(Collection<String> playerNames, String input, List<String> existing) {
        final String processedInput = input.toLowerCase(Locale.ENGLISH);
        return playerNames.stream().filter(player -> player.toLowerCase(Locale.ENGLISH).startsWith(processedInput))
                .filter(player -> !existing.contains(player)).map(
                        player -> new Command(null, false, player, "", RequiredType.NONE,
                                CommandCategory.INFO) {
                        })
                /* If there are more than 200 suggestions, just send the first 200 */
                .limit(200)
                .collect(Collectors.toList());
    }

}
