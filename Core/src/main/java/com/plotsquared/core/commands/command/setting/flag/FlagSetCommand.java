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

public final class FlagSetCommand extends FlagCommandBean {

    private static final CloudKey<PlotFlag<?, ?>> COMPONENT_FLAG = CloudKey.of("flag", new TypeToken<PlotFlag<?, ?>>() {});
    private static final CloudKey<String> COMPONENT_VALUE = CloudKey.of("value", String.class);

    private final EventDispatcher eventDispatcher;

    @Inject
    public FlagSetCommand(final @NonNull EventDispatcher eventDispatcher) {
        this.eventDispatcher = eventDispatcher;
    }

    @Override
    protected Command.@NonNull Builder<PlotPlayer<?>> configurePlotCommand(
            final Command.@NonNull Builder<PlotPlayer<?>> builder
    ) {
        return builder.literal("set")
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
        if (event.getEventResult() != Result.FORCE && !checkPermValue(player, flag, flag.getName(), flagValue)) {
            return;
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

        plot.setFlag(parsedFlag);
        player.sendMessage(
                TranslatableCaption.of("flag.flag_added"),
                TagResolver.builder()
                        .tag("flag", Tag.inserting(Component.text(flag.getName())))
                        .tag("value", Tag.inserting(Component.text(parsedFlag.toString())))
                        .build()
        );
    }
}
