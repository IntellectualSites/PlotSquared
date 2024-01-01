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
package com.plotsquared.core.commands.command.setting.flag;

import cloud.commandframework.Command;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.keys.CloudKey;
import com.google.inject.Inject;
import com.plotsquared.core.commands.parser.PlotFlagParser;
import com.plotsquared.core.commands.suggestions.FlagValueSuggestionProvider;
import com.plotsquared.core.configuration.caption.CaptionUtility;
import com.plotsquared.core.configuration.caption.TranslatableCaption;
import com.plotsquared.core.events.PlotFlagAddEvent;
import com.plotsquared.core.events.Result;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.flag.FlagParseException;
import com.plotsquared.core.plot.flag.PlotFlag;
import com.plotsquared.core.util.EventDispatcher;
import io.leangen.geantyref.TypeToken;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.checkerframework.checker.nullness.qual.NonNull;

import static cloud.commandframework.arguments.standard.StringParser.greedyStringParser;
import static com.plotsquared.core.commands.parser.PlotFlagParser.plotFlagParser;

public final class FlagAddCommand extends FlagCommandBean {

    private static final CloudKey<PlotFlag<?, ?>> COMPONENT_FLAG = CloudKey.of("flag", new TypeToken<PlotFlag<?, ?>>() {});
    private static final CloudKey<String> COMPONENT_VALUE = CloudKey.of("value", String.class);

    private final EventDispatcher eventDispatcher;

    @Inject
    public FlagAddCommand(final @NonNull EventDispatcher eventDispatcher) {
        this.eventDispatcher = eventDispatcher;
    }

    @Override
    protected Command.@NonNull Builder<PlotPlayer<?>> configurePlotCommand(
            final Command.@NonNull Builder<PlotPlayer<?>> builder
    ) {
        return builder.literal("add")
                .required(COMPONENT_FLAG, plotFlagParser(PlotFlagParser.FlagSource.GLOBAL))
                .required(COMPONENT_VALUE, greedyStringParser(), new FlagValueSuggestionProvider(COMPONENT_FLAG));
    }

    @Override
    public void execute(final @NonNull CommandContext<PlotPlayer<?>> commandContext) {
        final PlotPlayer<?> player = commandContext.sender();
        final Plot plot = commandContext.inject(Plot.class).orElseThrow();
        final PlotFlag<?, ?> flag = commandContext.get(COMPONENT_FLAG);
        final String flagValue = commandContext.get(COMPONENT_VALUE);

        final PlotFlagAddEvent event = this.eventDispatcher.callFlagAdd(flag, plot);
        if (event.getEventResult() == Result.DENY) {
            player.sendMessage(
                    TranslatableCaption.of("events.event_denied"),
                    TagResolver.resolver("value", Tag.inserting(Component.text("Flag set")))
            );
            return;
        }
        if (event.getEventResult() != Result.FORCE) {
            final String[] split = flagValue.split(",");
            for (final String entry : split) {
                if (!checkPermValue(player, flag, flag.getName(), entry)) {
                    return;
                }
            }
        }

        final String sanitizedValue = CaptionUtility.stripClickEvents(flag, flagValue);
        final PlotFlag<?, ?> parsedFlag;
        try {
            parsedFlag = flag.parse(flagValue);
        } catch (final FlagParseException e) {
            player.sendMessage(
                    TranslatableCaption.of("flag.flag_parse_error"),
                    TagResolver.builder()
                            .tag("flag_name", Tag.inserting(Component.text(flag.getName())))
                            .tag("flag_value", Tag.inserting(Component.text(e.getValue())))
                            .tag("error", Tag.inserting(e.getErrorMessage().toComponent(player)))
                            .build()
            );
            return;
        }

        final boolean result = plot.setFlag(plot.getFlagContainer().getFlag(flag.getClass()).merge(parsedFlag.getValue()));
        if (!result) {
            player.sendMessage(TranslatableCaption.of("flag.flag_not_added"));
            return;
        }

        player.sendMessage(
                TranslatableCaption.of("flag.flag_added"),
                TagResolver.builder()
                        .tag("flag", Tag.inserting(Component.text(flag.getName())))
                        .tag("value", Tag.inserting(Component.text(parsedFlag.toString())))
                        .build()
        );
    }
}
