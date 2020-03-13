package com.github.intellectualsites.plotsquared.plot.commands;

import com.github.intellectualsites.plotsquared.commands.Command;
import com.github.intellectualsites.plotsquared.commands.CommandDeclaration;
import com.github.intellectualsites.plotsquared.plot.PlotSquared;
import com.github.intellectualsites.plotsquared.plot.config.CaptionUtility;
import com.github.intellectualsites.plotsquared.plot.config.Captions;
import com.github.intellectualsites.plotsquared.plot.config.Settings;
import com.github.intellectualsites.plotsquared.plot.events.PlotFlagAddEvent;
import com.github.intellectualsites.plotsquared.plot.events.PlotFlagRemoveEvent;
import com.github.intellectualsites.plotsquared.plot.events.Result;
import com.github.intellectualsites.plotsquared.plot.flags.FlagParseException;
import com.github.intellectualsites.plotsquared.plot.flags.GlobalFlagContainer;
import com.github.intellectualsites.plotsquared.plot.flags.InternalFlag;
import com.github.intellectualsites.plotsquared.plot.flags.PlotFlag;
import com.github.intellectualsites.plotsquared.plot.flags.types.IntegerFlag;
import com.github.intellectualsites.plotsquared.plot.flags.types.ListFlag;
import com.github.intellectualsites.plotsquared.plot.object.Location;
import com.github.intellectualsites.plotsquared.plot.object.Plot;
import com.github.intellectualsites.plotsquared.plot.object.PlotMessage;
import com.github.intellectualsites.plotsquared.plot.object.PlotPlayer;
import com.github.intellectualsites.plotsquared.plot.object.RunnableVal2;
import com.github.intellectualsites.plotsquared.plot.object.RunnableVal3;
import com.github.intellectualsites.plotsquared.plot.util.MainUtil;
import com.github.intellectualsites.plotsquared.plot.util.MathMan;
import com.github.intellectualsites.plotsquared.plot.util.Permissions;
import com.github.intellectualsites.plotsquared.plot.util.StringComparison;
import com.github.intellectualsites.plotsquared.plot.util.StringMan;
import com.github.intellectualsites.plotsquared.plot.util.helpmenu.HelpMenu;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@CommandDeclaration(command = "flag",
    aliases = {"f", "flag"},
    usage = "/plot flag <set|remove|add|list|info> <flag> <value>",
    description = "Manage plot flags",
    category = CommandCategory.SETTINGS,
    requiredType = RequiredType.NONE,
    permission = "plots.flag")
@SuppressWarnings("unused")
public final class FlagCommand extends Command {

    public FlagCommand() {
        super(MainCommand.getInstance(), true);
    }

    private static boolean sendMessage(PlotPlayer player, Captions message, Object... args) {
        message.send(player, args);
        return true;
    }

    private static boolean checkPermValue(@Nonnull final PlotPlayer player,
        @NotNull final PlotFlag<?, ?> flag, @NotNull String key, @NotNull String value) {
        key = key.toLowerCase();
        value = value.toLowerCase();
        String perm = CaptionUtility
            .format(player, Captions.PERMISSION_SET_FLAG_KEY_VALUE.getTranslated(),
                key.toLowerCase(), value.toLowerCase());
        if (flag instanceof IntegerFlag && MathMan.isInteger(value)) {
            try {
                int numeric = Integer.parseInt(value);
                perm = perm.substring(0, perm.length() - value.length() - 1);
                if (numeric > 0) {
                    int checkRange = PlotSquared.get().getPlatform().equalsIgnoreCase("bukkit") ?
                        numeric :
                        Settings.Limit.MAX_PLOTS;
                    final boolean result = player.hasPermissionRange(perm, checkRange) >= numeric;
                    if (!result) {
                        MainUtil.sendMessage(player, Captions.NO_PERMISSION, CaptionUtility
                            .format(player, Captions.PERMISSION_SET_FLAG_KEY_VALUE.getTranslated(),
                                key.toLowerCase(), value.toLowerCase()));
                    }
                    return result;
                }
            } catch (NumberFormatException ignore) {
            }
        } else if (flag instanceof ListFlag) {
            final ListFlag<?, ?> listFlag = (ListFlag<?, ?>) flag;
            try {
                PlotFlag<? extends List<?>, ?> parsedFlag = listFlag.parse(value);
                for (final Object entry : parsedFlag.getValue()) {
                    final String permission = CaptionUtility
                        .format(player, Captions.PERMISSION_SET_FLAG_KEY_VALUE.getTranslated(),
                            key.toLowerCase(), entry.toString().toLowerCase());
                    final boolean result = Permissions.hasPermission(player, permission);
                    if (!result) {
                        MainUtil.sendMessage(player, Captions.NO_PERMISSION, CaptionUtility
                            .format(player, Captions.PERMISSION_SET_FLAG_KEY_VALUE.getTranslated(),
                                key.toLowerCase(), value.toLowerCase()));
                        return false;
                    }
                }
            } catch (final FlagParseException e) {
                MainUtil.sendMessage(player,
                    Captions.FLAG_PARSE_ERROR.getTranslated().replace("%flag_name%", flag.getName())
                        .replace("%flag_value%", e.getValue())
                        .replace("%error%", e.getErrorMessage()));
                return false;
            } catch (final Exception e) {
                return false;
            }
            return true;
        }
        final boolean result = Permissions.hasPermission(player, perm);
        if (!result) {
            MainUtil.sendMessage(player, Captions.NO_PERMISSION, CaptionUtility
                .format(player, Captions.PERMISSION_SET_FLAG_KEY_VALUE.getTranslated(),
                    key.toLowerCase(), value.toLowerCase()));
        }
        return result;
    }

    /**
     * Checks if the player is allowed to modify the flags at their current location
     *
     * @return true if the player is allowed to modify the flags at their current location
     */
    private static boolean checkRequirements(@NotNull final PlotPlayer player) {
        final Location location = player.getLocation();
        final Plot plot = location.getPlotAbs();
        if (plot == null) {
            MainUtil.sendMessage(player, Captions.NOT_IN_PLOT);
            return false;
        }
        if (!plot.hasOwner()) {
            sendMessage(player, Captions.PLOT_NOT_CLAIMED);
            return false;
        }
        if (!plot.isOwner(player.getUUID()) && !Permissions
            .hasPermission(player, Captions.PERMISSION_SET_FLAG_OTHER)) {
            MainUtil
                .sendMessage(player, Captions.NO_PERMISSION, Captions.PERMISSION_SET_FLAG_OTHER);
            return false;
        }
        return true;
    }

    /**
     * Attempt to extract the plot flag from the command arguments. If the flag cannot
     * be found, a flag suggestion may be sent to the player.
     *
     * @param player Player executing the command
     * @param arg    String to extract flag from
     * @return The flag, if found, else null
     */
    @Nullable private static PlotFlag<?, ?> getFlag(@NotNull final PlotPlayer player,
        @NotNull final String arg) {
        if (arg != null && arg.length() > 0) {
            final PlotFlag<?, ?> flag = GlobalFlagContainer.getInstance().getFlagFromString(arg);
            if (flag instanceof InternalFlag || flag == null) {
                boolean suggested = false;
                try {
                    final StringComparison<PlotFlag<?, ?>> stringComparison =
                        new StringComparison<>(arg,
                            GlobalFlagContainer.getInstance().getFlagMap().values());
                    final String best = stringComparison.getBestMatch();
                    if (best != null) {
                        MainUtil.sendMessage(player, Captions.NOT_VALID_FLAG_SUGGESTED, best);
                        suggested = true;
                    }
                } catch (final Exception ignored) { /* Happens sometimes because of mean code */ }
                if (!suggested) {
                    MainUtil.sendMessage(player, Captions.NOT_VALID_FLAG);
                }
                return null;
            }
            return flag;
        }
        return null;
    }

    @Override public CompletableFuture<Boolean> execute(PlotPlayer player, String[] args,
        RunnableVal3<Command, Runnable, Runnable> confirm,
        RunnableVal2<Command, CommandResult> whenDone) throws CommandException {
        if (args.length == 0 || !Arrays
            .asList("set", "s", "list", "l", "delete", "remove", "r", "add", "a", "info", "i")
            .contains(args[0].toLowerCase(Locale.ENGLISH))) {
            new HelpMenu(player).setCategory(CommandCategory.SETTINGS)
                .setCommands(this.getCommands()).generateMaxPages()
                .generatePage(0, getParent().toString()).render();
            return CompletableFuture.completedFuture(true);
        }
        return super.execute(player, args, confirm, whenDone);
    }

    @Override public Collection<Command> tab(final PlotPlayer player, final String[] args,
        final boolean space) {
        if (args.length == 1) {
            return Stream
                .of("s", "set", "add", "a", "remove", "r", "delete", "info", "i", "list", "l")
                .filter(value -> value.startsWith(args[0].toLowerCase(Locale.ENGLISH)))
                .map(value -> new Command(null, false, value, "", RequiredType.NONE, null) {
                }).collect(Collectors.toList());
        } else if (Arrays.asList("s", "set", "add", "a", "remove", "r", "delete", "info", "i")
            .contains(args[0].toLowerCase(Locale.ENGLISH)) && args.length == 2) {
            return GlobalFlagContainer.getInstance().getRecognizedPlotFlags().stream()
                .filter(flag -> !(flag instanceof InternalFlag))
                .filter(flag -> flag.getName().startsWith(args[1].toLowerCase(Locale.ENGLISH)))
                .map(flag -> new Command(null, false, flag.getName(), "", RequiredType.NONE, null) {
                }).collect(Collectors.toList());
        } else if (Arrays.asList("s", "set", "add", "a", "remove", "r", "delete")
            .contains(args[0].toLowerCase(Locale.ENGLISH)) && args.length == 3) {
            try {
                final PlotFlag<?, ?> flag =
                    GlobalFlagContainer.getInstance().getFlagFromString(args[1]);
                if (flag != null) {
                    Stream<String> stream = flag.getTabCompletions().stream();
                    if (flag instanceof ListFlag && args[2].contains(",")) {
                        final String[] split = args[2].split(",");
                        // Prefix earlier values onto all suggestions
                        StringBuilder prefix = new StringBuilder();
                        for (int i = 0; i < split.length - 1; i++) {
                            prefix.append(split[i]).append(",");
                        }
                        final String cmp;
                        if (!args[2].endsWith(",")) {
                            cmp = split[split.length - 1];
                        } else {
                            prefix.append(split[split.length - 1]).append(",");
                            cmp = "";
                        }
                        return stream
                            .filter(value -> value.startsWith(cmp.toLowerCase(Locale.ENGLISH))).map(
                                value -> new Command(null, false, prefix + value, "",
                                    RequiredType.NONE, null) {
                                }).collect(Collectors.toList());
                    } else {
                        return stream
                            .filter(value -> value.startsWith(args[2].toLowerCase(Locale.ENGLISH)))
                            .map(value -> new Command(null, false, value, "", RequiredType.NONE,
                                null) {
                            }).collect(Collectors.toList());
                    }
                }
            } catch (final Exception e) {
            }
        }
        return tabOf(player, args, space);
    }

    @CommandDeclaration(command = "set",
        aliases = {"s", "set"},
        usage = "/plot flag set <flag> <value>",
        description = "Set a plot flag",
        category = CommandCategory.SETTINGS,
        requiredType = RequiredType.NONE,
        permission = "plots.set.flag")
    public void set(final Command command, final PlotPlayer player, final String[] args,
        final RunnableVal3<Command, Runnable, Runnable> confirm,
        final RunnableVal2<Command, CommandResult> whenDone) {
        if (!checkRequirements(player)) {
            return;
        }
        if (args.length < 2) {
            MainUtil.sendMessage(player, Captions.COMMAND_SYNTAX, "/plot flag set <flag> <value>");
            return;
        }
        final PlotFlag<?, ?> plotFlag = getFlag(player, args[0]);
        if (plotFlag == null) {
            return;
        }
        Plot plot = player.getLocation().getPlotAbs();
        PlotFlagAddEvent event = new PlotFlagAddEvent(plotFlag, plot);
        if (event.getEventResult() == Result.DENY) {
            player.sendMessage(CaptionUtility.format(player, event.getEventResult().getReason()));
            return;
        }
        boolean force = event.getEventResult() == Result.FORCE;
        final String value = StringMan.join(Arrays.copyOfRange(args, 1, args.length), " ");
        if (!force && !checkPermValue(player, plotFlag, args[0], value)) {
            return;
        }
        final PlotFlag<?, ?> parsed;
        try {
            parsed = plotFlag.parse(value);
        } catch (final FlagParseException e) {
            MainUtil.sendMessage(player,
                Captions.FLAG_PARSE_ERROR.getTranslated().replace("%flag_name%", plotFlag.getName())
                    .replace("%flag_value%", e.getValue()).replace("%error%", e.getErrorMessage()));
            return;
        }
        plot.setFlag(parsed);
        MainUtil.sendMessage(player, Captions.FLAG_ADDED);
    }

    @CommandDeclaration(command = "add",
        aliases = {"a", "add"},
        usage = "/plot flag add <flag> <value>",
        description = "Add a plot flag value",
        category = CommandCategory.SETTINGS,
        requiredType = RequiredType.NONE,
        permission = "plots.flag.add")
    public void add(final Command command, PlotPlayer player, final String[] args,
        final RunnableVal3<Command, Runnable, Runnable> confirm,
        final RunnableVal2<Command, CommandResult> whenDone) {
        if (!checkRequirements(player)) {
            return;
        }
        if (args.length < 2) {
            MainUtil.sendMessage(player, Captions.COMMAND_SYNTAX, "/plot flag add <flag> <values>");
            return;
        }
        final PlotFlag<?, ?> plotFlag = getFlag(player, args[0]);
        if (plotFlag == null) {
            return;
        }
        Plot plot = player.getLocation().getPlotAbs();
        PlotFlagAddEvent event = new PlotFlagAddEvent(plotFlag, plot);
        if (event.getEventResult() == Result.DENY) {
            player.sendMessage(CaptionUtility.format(player, event.getEventResult().getReason()));
            return;
        }
        boolean force = event.getEventResult() == Result.FORCE;
        final PlotFlag localFlag = player.getLocation().getPlotAbs().getFlagContainer()
            .getFlag(event.getFlag().getClass());
        if (!force) {
            for (String entry : args[1].split(",")) {
                if (!checkPermValue(player, event.getFlag(), args[0], entry)) {
                    return;
                }
            }
        } final String value = StringMan.join(Arrays.copyOfRange(args, 1, args.length), " ");
        final PlotFlag parsed;
        try {
            parsed = event.getFlag().parse(value);
        } catch (FlagParseException e) {
            MainUtil.sendMessage(player,
                Captions.FLAG_PARSE_ERROR.getTranslated().replace("%flag_name%", plotFlag.getName())
                    .replace("%flag_value%", e.getValue()).replace("%error%", e.getErrorMessage()));
            return;
        }
        boolean result =
            player.getLocation().getPlotAbs().setFlag(localFlag.merge(parsed.getValue()));
        if (!result) {
            MainUtil.sendMessage(player, Captions.FLAG_NOT_ADDED);
            return;
        }
        MainUtil.sendMessage(player, Captions.FLAG_ADDED);
    }

    @CommandDeclaration(command = "remove",
        aliases = {"r", "remove", "delete"},
        usage = "/plot flag remove <flag> [values]",
        description = "Remove a flag",
        category = CommandCategory.SETTINGS,
        requiredType = RequiredType.NONE,
        permission = "plots.flag.add")
    public void remove(final Command command, PlotPlayer player, final String[] args,
        final RunnableVal3<Command, Runnable, Runnable> confirm,
        final RunnableVal2<Command, CommandResult> whenDone) {
        if (!checkRequirements(player)) {
            return;
        }
        if (args.length != 1 && args.length != 2) {
            MainUtil
                .sendMessage(player, Captions.COMMAND_SYNTAX, "/plot flag remove <flag> [values]");
            return;
        }
        PlotFlag<?, ?> flag = getFlag(player, args[0]);
        if (flag == null) {
            return;
        }
        final Plot plot = player.getLocation().getPlotAbs();
        PlotFlagRemoveEvent event = new PlotFlagRemoveEvent(flag, plot);
        if (event.getEventResult() == Result.DENY) {
            player.sendMessage(CaptionUtility.format(player, event.getEventResult().getReason()));
            return;
        }
        boolean force = event.getEventResult() == Result.FORCE;
        flag = event.getFlag();
        if (!force && !Permissions.hasPermission(player, CaptionUtility
            .format(player, Captions.PERMISSION_SET_FLAG_KEY.getTranslated(),
                args[0].toLowerCase()))) {
            if (args.length != 2) {
                MainUtil.sendMessage(player, Captions.NO_PERMISSION, CaptionUtility
                    .format(player, Captions.PERMISSION_SET_FLAG_KEY.getTranslated(),
                        args[0].toLowerCase()));
                return;
            }
        }
        if (args.length == 2 && flag instanceof ListFlag) {
            String value = StringMan.join(Arrays.copyOfRange(args, 1, args.length), " ");
            final ListFlag listFlag = (ListFlag) flag;
            final List list =
                new ArrayList(plot.getFlag((Class<? extends ListFlag<?, ?>>) listFlag.getClass()));
            final PlotFlag parsedFlag;
            try {
                parsedFlag = listFlag.parse(value);
            } catch (final FlagParseException e) {
                MainUtil.sendMessage(player,
                    Captions.FLAG_PARSE_ERROR.getTranslated().replace("%flag_name%", flag.getName())
                        .replace("%flag_value%", e.getValue())
                        .replace("%error%", e.getErrorMessage()));
                return;
            }
            if (((List) parsedFlag.getValue()).isEmpty()) {
                MainUtil.sendMessage(player, Captions.FLAG_NOT_REMOVED);
                return;
            }
            if (list.removeAll((List) parsedFlag.getValue())) {
                if (list.isEmpty()) {
                    if (plot.removeFlag(flag)) {
                        MainUtil.sendMessage(player, Captions.FLAG_REMOVED);
                        return;
                    } else {
                        MainUtil.sendMessage(player, Captions.FLAG_NOT_REMOVED);
                        return;
                    }
                } else {
                    // MainUtil.sendMessage(player, Captions.FLAG_REMOVED);
                    PlotFlag plotFlag = parsedFlag.createFlagInstance(list);
                    PlotFlagAddEvent addEvent = new PlotFlagAddEvent(plotFlag, plot);
                    if (addEvent.getEventResult() == Result.DENY) {
                        player.sendMessage(CaptionUtility.format(player, addEvent.getEventResult().getReason()));
                        return;
                    }
                    if (plot.setFlag(addEvent.getFlag())) {
                        MainUtil.sendMessage(player, Captions.FLAG_PARTIALLY_REMOVED);
                        return;
                    } else {
                        MainUtil.sendMessage(player, Captions.FLAG_NOT_REMOVED);
                        return;
                    }
                }
            } else {
                MainUtil.sendMessage(player, Captions.FLAG_NOT_REMOVED);
                return;
            }
        } else {
            boolean result = plot.removeFlag(flag);
            if (!result) {
                MainUtil.sendMessage(player, Captions.FLAG_NOT_REMOVED);
                return;
            }
        }
        MainUtil.sendMessage(player, Captions.FLAG_REMOVED);
    }

    @CommandDeclaration(command = "list",
        aliases = {"l", "list", "flags"},
        usage = "/plot flag list",
        description = "List all available plot flags",
        category = CommandCategory.SETTINGS,
        requiredType = RequiredType.NONE,
        permission = "plots.flag.list")
    public void list(final Command command, final PlotPlayer player, final String[] args,
        final RunnableVal3<Command, Runnable, Runnable> confirm,
        final RunnableVal2<Command, CommandResult> whenDone) {
        if (!checkRequirements(player)) {
            return;
        }

        final Map<String, ArrayList<String>> flags = new HashMap<>();
        for (PlotFlag<?, ?> plotFlag : GlobalFlagContainer.getInstance().getRecognizedPlotFlags()) {
            if (plotFlag instanceof InternalFlag) {
                continue;
            }
            final String category = plotFlag.getFlagCategory().getTranslated();
            final Collection<String> flagList =
                flags.computeIfAbsent(category, k -> new ArrayList<>());
            flagList.add(plotFlag.getName());
        }

        for (final Map.Entry<String, ArrayList<String>> entry : flags.entrySet()) {
            Collections.sort(entry.getValue());
            PlotMessage plotMessage = new PlotMessage(entry.getKey() + ": ")
                .color(Captions.FLAG_INFO_COLOR_KEY.getTranslated());
            final Iterator<String> flagIterator = entry.getValue().iterator();
            while (flagIterator.hasNext()) {
                final String flag = flagIterator.next();
                plotMessage = plotMessage.text(flag).command("/plot flag info " + flag)
                    .color(Captions.FLAG_INFO_COLOR_VALUE.getTranslated()).tooltip(
                        new PlotMessage(Captions.FLAG_LIST_SEE_INFO.getTranslated())
                            .color(Captions.FLAG_INFO_COLOR_VALUE.getTranslated()));
                if (flagIterator.hasNext()) {
                    plotMessage = plotMessage.text(", ")
                        .color(Captions.FLAG_INFO_COLOR_VALUE.getTranslated());
                }
            }
            plotMessage.send(player);
        }
    }

    @CommandDeclaration(command = "info",
        aliases = {"i", "info"},
        usage = "/plot flag info <flag>",
        description = "View information about a flag",
        category = CommandCategory.SETTINGS,
        requiredType = RequiredType.NONE,
        permission = "plots.flag.info")
    public void info(final Command command, final PlotPlayer player, final String[] args,
        final RunnableVal3<Command, Runnable, Runnable> confirm,
        final RunnableVal2<Command, CommandResult> whenDone) {
        if (!checkRequirements(player)) {
            return;
        }
        if (args.length < 1) {
            MainUtil.sendMessage(player, Captions.COMMAND_SYNTAX, "/plot flag info <flag>");
            return;
        }
        final PlotFlag<?, ?> plotFlag = getFlag(player, args[0]);
        if (plotFlag != null) {
            Captions.FLAG_INFO_HEADER.send(player);
            // Flag name
            new PlotMessage(Captions.FLAG_INFO_NAME.getTranslated())
                .color(Captions.FLAG_INFO_COLOR_KEY.getTranslated()).text(plotFlag.getName())
                .color(Captions.FLAG_INFO_COLOR_VALUE.getTranslated()).send(player);
            // Flag category
            new PlotMessage(Captions.FLAG_INFO_CATEGORY.getTranslated())
                .color(Captions.FLAG_INFO_COLOR_KEY.getTranslated())
                .text(plotFlag.getFlagCategory().getTranslated())
                .color(Captions.FLAG_INFO_COLOR_VALUE.getTranslated()).send(player);
            // Flag description
            new PlotMessage(Captions.FLAG_INFO_DESCRIPTION.getTranslated())
                .color(Captions.FLAG_INFO_COLOR_KEY.getTranslated()).send(player);
            new PlotMessage(plotFlag.getFlagDescription().getTranslated())
                .color(Captions.FLAG_INFO_COLOR_VALUE.getTranslated()).send(player);
            // Flag example
            new PlotMessage(Captions.FLAG_INFO_EXAMPLE.getTranslated())
                .color(Captions.FLAG_INFO_COLOR_KEY.getTranslated())
                .text("/plot flag set " + plotFlag.getName() + " " + plotFlag.getExample())
                .color(Captions.FLAG_INFO_COLOR_VALUE.getTranslated())
                .suggest("/plot flag set " + plotFlag.getName() + " " + plotFlag.getExample())
                .send(player);
            // Default value
            final String defaultValue = player.getLocation().getPlotArea().getFlagContainer()
                .getFlagErased(plotFlag.getClass()).toString();
            new PlotMessage(Captions.FLAG_INFO_DEFAULT_VALUE.getTranslated())
                .color(Captions.FLAG_INFO_COLOR_KEY.getTranslated()).text(defaultValue)
                .color(Captions.FLAG_INFO_COLOR_VALUE.getTranslated()).send(player);
            // Footer. Done this way to prevent the duplicate-message-thingy from catching it
            MainUtil.sendMessage(player, "&r" + Captions.FLAG_INFO_FOOTER.getTranslated());
        }
    }

}
