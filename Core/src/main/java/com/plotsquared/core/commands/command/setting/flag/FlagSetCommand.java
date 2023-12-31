package com.plotsquared.core.commands.command.setting.flag;

import cloud.commandframework.Command;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.keys.CloudKey;
import com.google.inject.Inject;
import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.command.CommandCategory;
import com.plotsquared.core.commands.CommandRequirement;
import com.plotsquared.core.commands.CommonCommandRequirement;
import com.plotsquared.core.commands.PlotSquaredCommandBean;
import com.plotsquared.core.commands.suggestions.FlagValueSuggestionProvider;
import com.plotsquared.core.configuration.Settings;
import com.plotsquared.core.configuration.caption.CaptionUtility;
import com.plotsquared.core.configuration.caption.TranslatableCaption;
import com.plotsquared.core.events.PlotFlagAddEvent;
import com.plotsquared.core.events.Result;
import com.plotsquared.core.permissions.Permission;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.flag.FlagParseException;
import com.plotsquared.core.plot.flag.PlotFlag;
import com.plotsquared.core.plot.flag.types.IntegerFlag;
import com.plotsquared.core.plot.flag.types.ListFlag;
import com.plotsquared.core.util.EventDispatcher;
import com.plotsquared.core.util.MathMan;
import io.leangen.geantyref.TypeToken;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.List;
import java.util.Set;

import static cloud.commandframework.arguments.standard.StringParser.greedyStringParser;
import static com.plotsquared.core.commands.parser.PlotFlagParser.plotFlagParser;

public final class FlagSetCommand extends PlotSquaredCommandBean {

    private static final CloudKey<PlotFlag<?, ?>> COMPONENT_FLAG = CloudKey.of("flag", new TypeToken<PlotFlag<?, ?>>() {});
    private static final CloudKey<String> COMPONENT_VALUE = CloudKey.of("value", String.class);

    private static boolean checkPermValue(
            final @NonNull PlotPlayer<?> player,
            final @NonNull PlotFlag<?, ?> flag, @NonNull String key, @NonNull String value
    ) {
        key = key.toLowerCase();
        value = value.toLowerCase();
        String perm = Permission.PERMISSION_SET_FLAG_KEY_VALUE.format(key.toLowerCase(), value.toLowerCase());
        if (flag instanceof IntegerFlag && MathMan.isInteger(value)) {
            try {
                int numeric = Integer.parseInt(value);
                // Getting full permission without ".<amount>" at the end
                perm = perm.substring(0, perm.length() - value.length() - 1);
                boolean result = false;
                if (numeric >= 0) {
                    int checkRange = PlotSquared.get().getPlatform().equalsIgnoreCase("bukkit") ?
                            numeric :
                            Settings.Limit.MAX_PLOTS;
                    result = player.hasPermissionRange(perm, checkRange) >= numeric;
                }
                if (!result) {
                    player.sendMessage(
                            TranslatableCaption.of("permission.no_permission"),
                            TagResolver.resolver(
                                    "node",
                                    Tag.inserting(Component.text(perm + "." + numeric))
                            )
                    );
                }
                return result;
            } catch (NumberFormatException ignore) {
            }
        } else if (flag instanceof final ListFlag<?, ?> listFlag) {
            try {
                PlotFlag<? extends List<?>, ?> parsedFlag = listFlag.parse(value);
                for (final Object entry : parsedFlag.getValue()) {
                    final String permission = Permission.PERMISSION_SET_FLAG_KEY_VALUE.format(
                            key.toLowerCase(),
                            entry.toString().toLowerCase()
                    );
                    final boolean result = player.hasPermission(permission);
                    if (!result) {
                        player.sendMessage(
                                TranslatableCaption.of("permission.no_permission"),
                                TagResolver.resolver("node", Tag.inserting(Component.text(permission)))
                        );
                        return false;
                    }
                }
            } catch (final FlagParseException e) {
                player.sendMessage(
                        TranslatableCaption.of("flag.flag_parse_error"),
                        TagResolver.builder()
                                .tag("flag_name", Tag.inserting(Component.text(flag.getName())))
                                .tag("flag_value", Tag.inserting(Component.text(e.getValue())))
                                .tag("error", Tag.inserting(e.getErrorMessage().toComponent(player)))
                                .build()
                );
                return false;
            } catch (final Exception e) {
                return false;
            }
            return true;
        }
        boolean result;
        String basePerm = Permission.PERMISSION_SET_FLAG_KEY.format(key.toLowerCase());
        if (flag.isValuedPermission()) {
            result = player.hasKeyedPermission(basePerm, value);
        } else {
            result = player.hasPermission(basePerm);
            perm = basePerm;
        }
        if (!result) {
            player.sendMessage(
                    TranslatableCaption.of("permission.no_permission"),
                    TagResolver.resolver("node", Tag.inserting(Component.text(perm)))
            );
        }
        return result;
    }

    private final EventDispatcher eventDispatcher;

    @Inject
    public FlagSetCommand(final @NonNull EventDispatcher eventDispatcher) {
        this.eventDispatcher = eventDispatcher;
    }

    @Override
    public @NonNull CommandCategory category() {
        return CommandCategory.SETTINGS;
    }

    @Override
    public @NonNull Set<@NonNull CommandRequirement> requirements() {
        // TODO: Figure out how to handle the override permission check :)
        return Set.of(CommonCommandRequirement.REQUIRES_PLOT, CommonCommandRequirement.REQUIRES_OWNER);
    }

    @Override
    protected Command.@NonNull Builder<PlotPlayer<?>> configurePlotCommand(
            final Command.@NonNull Builder<PlotPlayer<?>> builder
    ) {
        return builder.literal("flag")
                .literal("set")
                .required(COMPONENT_FLAG, plotFlagParser())
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
