package com.plotsquared.core.commands.suggestions;

import cloud.commandframework.arguments.suggestion.BlockingSuggestionProvider;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.context.CommandInput;
import cloud.commandframework.keys.CloudKey;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.flag.PlotFlag;
import com.plotsquared.core.plot.flag.types.ListFlag;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

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
            final StringBuilder prefix = new StringBuilder();
            for (int i = 0; i < split.length - i; i++) {
                prefix.append(split[i]).append(",");
            }

            final String cmp;
            if (!input.peekString().endsWith(",")) {
                cmp = split[split.length - 1];
            } else {
                prefix.append(split[split.length - 1]).append(",");
                cmp = "";
            }

            return completions.stream()
                    .filter(value -> value.startsWith(cmp.toLowerCase(Locale.ENGLISH)))
                    .map(value -> prefix + value)
                    .toList();
        }
        return completions;
    }
}
