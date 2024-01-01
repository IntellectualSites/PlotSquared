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
package com.plotsquared.core.commands.suggestions;

import cloud.commandframework.arguments.suggestion.BlockingSuggestionProvider;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.context.CommandInput;
import cloud.commandframework.keys.CloudKey;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.flag.PlotFlag;
import com.plotsquared.core.plot.flag.types.ListFlag;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Suggestion provider that provides context-aware {@link PlotFlag plot flag} value suggestions using
 * {@link PlotFlag#getTabCompletions()}.
 */
public final class FlagValueSuggestionProvider implements BlockingSuggestionProvider.Strings<PlotPlayer<?>> {

    private final CloudKey<PlotFlag<?, ?>> flagKey;

    /**
     * Creates a new suggestion provider.
     *
     * @param flagKey the key of the argument that contains the flag to provide value suggestions for
     */
    public FlagValueSuggestionProvider(final @NonNull CloudKey<PlotFlag<?, ?>> flagKey) {
        this.flagKey = Objects.requireNonNull(flagKey, "flagKey");
    }

    @Override
    public @NonNull Iterable<@NonNull String> stringSuggestions(
            @NonNull final CommandContext<PlotPlayer<?>> context,
            @NonNull final CommandInput input
    ) {
        final PlotFlag<?, ?> plotFlag = context.getOrDefault(this.flagKey, null);
        if (plotFlag == null) {
            return List.of();
        }
        final Collection<String> completions = plotFlag.getTabCompletions();
        if (plotFlag instanceof ListFlag<?,?> && input.peekString().contains(",")) {
            final String[] split = input.peekString().split(",");
            final List<String> existingValues = new ArrayList<>(Arrays.asList(split));

            final String completingValue;
            if (!input.peekString().endsWith(",")) {
                // In this case we want to complete the value we're currently typing.
                completingValue = split[split.length - 1];
                existingValues.remove(existingValues.size() - 1);
            } else {
                completingValue = null;
            }

            final String prefix = existingValues.stream().collect(Collectors.joining(",", "", ","));
            return completions.stream()
                    .filter(value -> !existingValues.contains(value))
                    .filter(value -> completingValue == null || value.startsWith(completingValue))
                    .map(value -> prefix + value)
                    .toList();
        }
        return completions;
    }
}
