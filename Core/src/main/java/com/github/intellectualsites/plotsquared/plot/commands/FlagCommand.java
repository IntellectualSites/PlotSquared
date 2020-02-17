package com.github.intellectualsites.plotsquared.plot.commands;

import com.github.intellectualsites.plotsquared.commands.Command;
import com.github.intellectualsites.plotsquared.commands.CommandDeclaration;
import com.github.intellectualsites.plotsquared.plot.PlotSquared;
import com.github.intellectualsites.plotsquared.plot.config.CaptionUtility;
import com.github.intellectualsites.plotsquared.plot.config.Captions;
import com.github.intellectualsites.plotsquared.plot.config.Settings;
import com.github.intellectualsites.plotsquared.plot.flags.FlagParseException;
import com.github.intellectualsites.plotsquared.plot.flags.GlobalFlagContainer;
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
import java.util.Map;

@CommandDeclaration(command = "flag", aliases = {"f",
    "flag"}, usage = "/plot flag <set|remove|add|list|info> <flag> <value>", description = "Manage plot flags", category = CommandCategory.SETTINGS, requiredType = RequiredType.NONE, permission = "plots.flag")
@SuppressWarnings("unused") public final class FlagCommand extends SubCommand {

    private static boolean checkPermValue(@Nonnull final PlotPlayer player,
        @NotNull final PlotFlag<?, ?> flag, @NotNull String key, @NotNull String value) {
        key = key.toLowerCase();
        value = value.toLowerCase();
        String perm = CaptionUtility
            .format(Captions.PERMISSION_SET_FLAG_KEY_VALUE.getTranslated(), key.toLowerCase(),
                value.toLowerCase());
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
                            .format(Captions.PERMISSION_SET_FLAG_KEY_VALUE.getTranslated(),
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
                        .format(Captions.PERMISSION_SET_FLAG_KEY_VALUE.getTranslated(),
                            key.toLowerCase(), entry.toString().toLowerCase());
                    final boolean result = Permissions.hasPermission(player, permission);
                    if (!result) {
                        MainUtil.sendMessage(player, Captions.NO_PERMISSION, CaptionUtility
                            .format(Captions.PERMISSION_SET_FLAG_KEY_VALUE.getTranslated(),
                                key.toLowerCase(), value.toLowerCase()));
                        return false;
                    }
                }
            } catch (final Exception e) {
                return false;
            }
            return true;
        }
        final boolean result = Permissions.hasPermission(player, perm);
        if (!result) {
            MainUtil.sendMessage(player, Captions.NO_PERMISSION, CaptionUtility
                .format(Captions.PERMISSION_SET_FLAG_KEY_VALUE.getTranslated(), key.toLowerCase(),
                    value.toLowerCase()));
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
            if (flag == null) {
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

    @Override public boolean onCommand(final PlotPlayer player, final String[] args) {
        new HelpMenu(player).setCategory(CommandCategory.SETTINGS).setCommands(this.getCommands())
            .generateMaxPages()
            .generatePage(0, getParent().toString()).render();
        return true;
    }

    @Override public Collection<Command> tab(final PlotPlayer player, final String[] args,
        final boolean space) {
        return tabOf(player, args, space, getUsage());
    }

    @CommandDeclaration(command = "set", aliases = {"s",
        "set"}, usage = "/plot flag set <flag> <value>", description = "Set a plot flag", category = CommandCategory.SETTINGS, requiredType = RequiredType.NONE, permission = "plots.set.flag")
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
        final String value = StringMan.join(Arrays.copyOfRange(args, 1, args.length), " ");
        if (!checkPermValue(player, plotFlag, args[0], value)) {
            return;
        }
        final PlotFlag<?, ?> parsed;
        try {
            parsed = plotFlag.parse(value);
        } catch (final FlagParseException e) {
            Captions.FLAG_PARSE_EXCEPTION
                .send(player, e.getFlag().getName(), e.getValue(), e.getErrorMessage());
            return;
        }
        player.getLocation().getPlotAbs().setFlag(parsed);
        MainUtil.sendMessage(player, Captions.FLAG_ADDED);
    }

    @CommandDeclaration(command = "add", aliases = {"a",
        "add"}, usage = "/plot flag add <flag> <value>", description = "Add a plot flag value", category = CommandCategory.SETTINGS, requiredType = RequiredType.NONE, permission = "plots.flag.add")
    public void add(final Command command, PlotPlayer player, final String[] args,
        final RunnableVal3<Command, Runnable, Runnable> confirm,
        final RunnableVal2<Command, CommandResult> whenDone) {
        if (!checkRequirements(player)) {
            return;
        }
        if (args.length < 3) {
            MainUtil.sendMessage(player, Captions.COMMAND_SYNTAX, "/plot flag add <flag> <values>");
            return;
        }
        final PlotFlag flag = getFlag(player, args[0]);
        if (flag == null) {
            return;
        }
        for (String entry : args[1].split(",")) {
            if (!checkPermValue(player, flag, args[0], entry)) {
                return;
            }
        }
        final String value = StringMan.join(Arrays.copyOfRange(args, 1, args.length), " ");
        final PlotFlag parsed;
        try {
            parsed = flag.parse(value);
        } catch (FlagParseException e) {
            Captions.FLAG_PARSE_EXCEPTION
                .send(player, e.getFlag().getName(), e.getValue(), e.getErrorMessage());
            return;
        }
        boolean result = player.getLocation().getPlotAbs()
            .setFlag(flag.merge(parsed.getValue()));
        if (!result) {
            MainUtil.sendMessage(player, Captions.FLAG_NOT_ADDED);
            return;
        }
        MainUtil.sendMessage(player, Captions.FLAG_ADDED);
    }

    @CommandDeclaration(command = "remove", aliases = {"r", "remove",
        "delete"}, usage = "/plot flag remove <flag> [values]", description = "Remove a flag", category = CommandCategory.SETTINGS, requiredType = RequiredType.NONE, permission = "plots.flag.add")
    public void remove(final Command command, PlotPlayer player, final String[] args,
        final RunnableVal3<Command, Runnable, Runnable> confirm,
        final RunnableVal2<Command, CommandResult> whenDone) {
        if (!checkRequirements(player)) {
            return;
        }
        if (args.length != 1 && args.length != 2) {
            MainUtil.sendMessage(player, Captions.COMMAND_SYNTAX, "/plot flag remove <flag> [values]");
            return;
        }
        final PlotFlag<?, ?> flag = getFlag(player, args[0]);
        if (flag == null) {
            return;
        }
        if (!Permissions.hasPermission(player,
            CaptionUtility.format(Captions.PERMISSION_SET_FLAG_KEY.getTranslated(), args[0].toLowerCase()))) {
            if (args.length != 2) {
                MainUtil.sendMessage(player, Captions.NO_PERMISSION, CaptionUtility
                    .format(Captions.PERMISSION_SET_FLAG_KEY.getTranslated(), args[0].toLowerCase()));
                return;
            }
        }
        final Plot plot = player.getLocation().getPlotAbs();
        if (args.length == 2 && flag instanceof ListFlag) {
            String value = StringMan.join(Arrays.copyOfRange(args, 1, args.length), " ");
            final ListFlag<?, ?> listFlag = (ListFlag<?, ?>) flag;
            final List<?> list = plot.getFlag((Class<? extends ListFlag<?, ?>>) listFlag.getClass());
            final PlotFlag<? extends List<?>, ?> parsedFlag;
            try {
                parsedFlag = listFlag.parse(value);
            } catch (final FlagParseException e) {
                Captions.FLAG_PARSE_EXCEPTION
                    .send(player, e.getFlag().getName(), e.getValue(), e.getErrorMessage());
                return;
            }
            if (parsedFlag.getValue().isEmpty()) {
                MainUtil.sendMessage(player, Captions.FLAG_NOT_REMOVED);
                return;
            }
            if (list.removeAll(parsedFlag.getValue())) {
                if (list.isEmpty()) {
                    if (plot.removeFlag(flag)) {
                        MainUtil.sendMessage(player, Captions.FLAG_REMOVED);
                        return;
                    } else {
                        MainUtil.sendMessage(player, Captions.FLAG_NOT_REMOVED);
                        return;
                    }
                } else {
                    MainUtil.sendMessage(player, Captions.FLAG_REMOVED);
                }
            } else {
                MainUtil.sendMessage(player, Captions.FLAG_NOT_REMOVED);
                return;
            }
            // TODO reimplement somewhere else: DBFunc.setFlags(plot, plot.getFlags());
            return;
        } else {
            boolean result = plot.removeFlag(flag);
            if (!result) {
                MainUtil.sendMessage(player, Captions.FLAG_NOT_REMOVED);
                return;
            }
        }
        /* TODO reimplement, maybe handle it with events?
                if (flag == Flags.TIME) {
                    player.setTime(Long.MAX_VALUE);
                } else if (flag == Flags.WEATHER) {
                    player.setWeather(PlotWeather.RESET);
                }*/
        MainUtil.sendMessage(player, Captions.FLAG_REMOVED);
    }

    @CommandDeclaration(command = "list", aliases = {"l", "list",
        "flags"}, usage = "/plot flag list", description = "List all available plot flags", category = CommandCategory.SETTINGS, requiredType = RequiredType.NONE, permission = "plots.flag.list")
    public void list(final Command command, final PlotPlayer player, final String[] args,
        final RunnableVal3<Command, Runnable, Runnable> confirm,
        final RunnableVal2<Command, CommandResult> whenDone) {
        if (!checkRequirements(player)) {
            return;
        }

        final Map<String, ArrayList<String>> flags = new HashMap<>();
        for (PlotFlag<?, ?> plotFlag : GlobalFlagContainer.getInstance().getRecognizedPlotFlags()) {
            final String category = plotFlag.getFlagCategory().getTranslated();
            final Collection<String> flagList = flags.computeIfAbsent(category, k -> new ArrayList<>());
            flagList.add(plotFlag.getName());
        }

        for (final Map.Entry<String, ArrayList<String>> entry : flags.entrySet()) {
            Collections.sort(entry.getValue());
            PlotMessage plotMessage = new PlotMessage(entry.getKey() + ": ").color(Captions.FLAG_INFO_COLOR_KEY.getTranslated());
            final Iterator<String> flagIterator = entry.getValue().iterator();
            while (flagIterator.hasNext()) {
                final String flag = flagIterator.next();
                plotMessage = plotMessage.text(flag).command("/plot flag info " + flag)
                    .color(Captions.FLAG_INFO_COLOR_VALUE.getTranslated())
                    .tooltip(new PlotMessage(Captions.FLAG_LIST_SEE_INFO.getTranslated())
                        .color(Captions.FLAG_INFO_COLOR_VALUE.getTranslated()));
                if (flagIterator.hasNext()) {
                    plotMessage = plotMessage.text(", ")
                        .color(Captions.FLAG_INFO_COLOR_VALUE.getTranslated());
                }
            }
            plotMessage.send(player);
        }
    }

    @CommandDeclaration(command = "info", aliases = {"i",
        "info"}, usage = "/plot flag info <flag>", description = "View information about a flag", category = CommandCategory.SETTINGS, requiredType = RequiredType.NONE, permission = "plots.flag.info")
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
                .color(Captions.FLAG_INFO_COLOR_KEY.getTranslated()).text(plotFlag.getExample())
                .color(Captions.FLAG_INFO_COLOR_VALUE.getTranslated())
                .suggest("/plot flag set " + plotFlag.getName() + " " + plotFlag.getExample())
                .send(player);
            // Default value
            final String defaultValue = player.getLocation().getPlotArea().getFlagContainer()
                .getFlagErased(plotFlag.getClass()).toString();
            new PlotMessage(Captions.FLAG_INFO_DEFAULT_VALUE.getTranslated())
                .color(Captions.FLAG_INFO_COLOR_KEY.getTranslated()).text(defaultValue)
                .color(Captions.FLAG_INFO_COLOR_VALUE.getTranslated()).send(player);
        }
    }

}
