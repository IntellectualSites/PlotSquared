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
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.plotsquared.core.command;

import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.configuration.Settings;
import com.plotsquared.core.configuration.caption.StaticCaption;
import com.plotsquared.core.configuration.caption.Templates;
import com.plotsquared.core.configuration.caption.TranslatableCaption;
import com.plotsquared.core.events.PlotFlagAddEvent;
import com.plotsquared.core.events.PlotFlagRemoveEvent;
import com.plotsquared.core.events.Result;
import com.plotsquared.core.location.Location;
import com.plotsquared.core.permissions.Permission;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.flag.FlagParseException;
import com.plotsquared.core.plot.flag.GlobalFlagContainer;
import com.plotsquared.core.plot.flag.InternalFlag;
import com.plotsquared.core.plot.flag.PlotFlag;
import com.plotsquared.core.plot.flag.types.IntegerFlag;
import com.plotsquared.core.plot.flag.types.ListFlag;
import com.plotsquared.core.util.MathMan;
import com.plotsquared.core.util.Permissions;
import com.plotsquared.core.util.StringComparison;
import com.plotsquared.core.util.StringMan;
import com.plotsquared.core.util.helpmenu.HelpMenu;
import com.plotsquared.core.util.task.RunnableVal2;
import com.plotsquared.core.util.task.RunnableVal3;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.Template;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
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
    usage = "/plot flag <set | remove | add | list | info> <flag> <value>",
    description = "Manage plot flags",
    category = CommandCategory.SETTINGS,
    requiredType = RequiredType.NONE,
    permission = "plots.flag")
@SuppressWarnings("unused")
public final class FlagCommand extends Command {

    public FlagCommand() {
        super(MainCommand.getInstance(), true);
    }

    private static boolean sendMessage(PlotPlayer<?> player) {
        player.sendMessage(
                TranslatableCaption.of("commandconfig.command_syntax"),
                Template.of("value", "/plot flag <set | remove | add | list | info> <flag> <value>")
        );
        return true;
    }

    private static boolean checkPermValue(@Nonnull final PlotPlayer<?> player,
        @Nonnull final PlotFlag<?, ?> flag, @Nonnull String key, @Nonnull String value) {
        key = key.toLowerCase();
        value = value.toLowerCase();
        String perm = Permission.PERMISSION_SET_FLAG_KEY_VALUE.format(key.toLowerCase(), value.toLowerCase());
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
                        player.sendMessage(TranslatableCaption.of("permission.no_permission"),
                            Template.of("node", Permission.PERMISSION_SET_FLAG_KEY_VALUE.format(key.toLowerCase(), value.toLowerCase())));
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
                    final String permission = Permission.PERMISSION_SET_FLAG_KEY_VALUE.format(key.toLowerCase(), entry.toString().toLowerCase());
                    final boolean result = Permissions.hasPermission(player, permission);
                    if (!result) {
                        player.sendMessage(TranslatableCaption.of("permission.no_permission"), Template.of("node", permission));
                        return false;
                    }
                }
            } catch (final FlagParseException e) {
                player.sendMessage(
                        TranslatableCaption.of("flag.flag_parse_error"),
                        Template.of("flag_name", flag.getName()),
                        Template.of("flag_value", e.getValue()),
                        Templates.of(player, "error", e.getErrorMessage(), e.getTemplates())
                );
                return false;
            } catch (final Exception e) {
                return false;
            }
            return true;
        }
        final boolean result = Permissions.hasPermission(player, perm);
        if (!result) {
            player.sendMessage(TranslatableCaption.of("permission.no_permission"), Template.of("node", perm));
        }
        return result;
    }

    /**
     * Checks if the player is allowed to modify the flags at their current location
     *
     * @return true if the player is allowed to modify the flags at their current location
     */
    private static boolean checkRequirements(@Nonnull final PlotPlayer<?> player) {
        final Location location = player.getLocation();
        final Plot plot = location.getPlotAbs();
        if (plot == null) {
            player.sendMessage(TranslatableCaption.of("errors.not_in_plot"));
            return false;
        }
        if (!plot.hasOwner()) {
            player.sendMessage(TranslatableCaption.of("working.plot_not_claimed"));
            return false;
        }
        if (!plot.isOwner(player.getUUID()) && !Permissions
            .hasPermission(player, Permission.PERMISSION_SET_FLAG_OTHER)) {
            player.sendMessage(
                    TranslatableCaption.of("permission.no_permission"),
                    Template.of("node", "plots.set.flag.other")
            );
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
    @Nullable private static PlotFlag<?, ?> getFlag(@Nonnull final PlotPlayer<?> player,
        @Nonnull final String arg) {
        if (arg.length() > 0) {
            final PlotFlag<?, ?> flag = GlobalFlagContainer.getInstance().getFlagFromString(arg);
            if (flag instanceof InternalFlag || flag == null) {
                boolean suggested = false;
                try {
                    final StringComparison<PlotFlag<?, ?>> stringComparison =
                        new StringComparison<>(arg,
                            GlobalFlagContainer.getInstance().getFlagMap().values());
                    final String best = stringComparison.getBestMatch();
                    if (best != null) {
                        player.sendMessage(
                                TranslatableCaption.of("flag.not_valid_flag_suggested"),
                                Template.of("value", best)
                        );
                        suggested = true;
                    }
                } catch (final Exception ignored) { /* Happens sometimes because of mean code */ }
                if (!suggested) {
                    player.sendMessage(TranslatableCaption.of("flag.not_valid_flag"));
                }
                return null;
            }
            return flag;
        }
        return null;
    }

    @Override
    public CompletableFuture<Boolean> execute(PlotPlayer<?> player, String[] args,
        RunnableVal3<Command, Runnable, Runnable> confirm,
        RunnableVal2<Command, CommandResult> whenDone) throws CommandException {
        if (args.length == 0 || !Arrays
            .asList("set", "s", "list", "l", "delete", "remove", "r", "add", "a", "info", "i")
            .contains(args[0].toLowerCase(Locale.ENGLISH))) {
            new HelpMenu(player).setCategory(CommandCategory.SETTINGS)
                .setCommands(this.getCommands()).generateMaxPages()
                .generatePage(0, getParent().toString(), player).render();
            return CompletableFuture.completedFuture(true);
        }
        return super.execute(player, args, confirm, whenDone);
    }

    @Override
    public Collection<Command> tab(final PlotPlayer<?> player, final String[] args,
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
    public void set(final Command command, final PlotPlayer<?> player, final String[] args,
        final RunnableVal3<Command, Runnable, Runnable> confirm,
        final RunnableVal2<Command, CommandResult> whenDone) {
        if (!checkRequirements(player)) {
            return;
        }
        if (args.length < 2) {
            player.sendMessage(
                    TranslatableCaption.of("commandconfig.command_syntax"),
                    Template.of("value", "/plot flag set <flag> <value>")
            );
            return;
        }
        final PlotFlag<?, ?> plotFlag = getFlag(player, args[0]);
        if (plotFlag == null) {
            return;
        }
        Plot plot = player.getLocation().getPlotAbs();
        PlotFlagAddEvent event = new PlotFlagAddEvent(plotFlag, plot);
        if (event.getEventResult() == Result.DENY) {
            player.sendMessage(
                    TranslatableCaption.of("events.event_denied"),
                    Template.of("value", "Flag set"));
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
            player.sendMessage(
                    TranslatableCaption.of("flag.flag_parse_error"),
                    Template.of("flag_name", plotFlag.getName()),
                    Template.of("flag_value", e.getValue()),
                    Templates.of(player, "error", e.getErrorMessage(), e.getTemplates())
            );
            return;
        }
        plot.setFlag(parsed);
        player.sendMessage(TranslatableCaption.of("flag.flag_added"));
    }

    @CommandDeclaration(command = "add",
        aliases = {"a", "add"},
        usage = "/plot flag add <flag> <value>",
        description = "Add a plot flag value",
        category = CommandCategory.SETTINGS,
        requiredType = RequiredType.NONE,
        permission = "plots.flag.add")
    public void add(final Command command, PlotPlayer<?> player, final String[] args,
        final RunnableVal3<Command, Runnable, Runnable> confirm,
        final RunnableVal2<Command, CommandResult> whenDone) {
        if (!checkRequirements(player)) {
            return;
        }
        if (args.length < 2) {
            player.sendMessage(
                    TranslatableCaption.of("commandconfig.command_syntax"),
                    Template.of("value", "/plot flag add <flag> <values>")
            );
            return;
        }
        final PlotFlag<?, ?> plotFlag = getFlag(player, args[0]);
        if (plotFlag == null) {
            return;
        }
        Plot plot = player.getLocation().getPlotAbs();
        PlotFlagAddEvent event = new PlotFlagAddEvent(plotFlag, plot);
        if (event.getEventResult() == Result.DENY) {
            player.sendMessage(
                    TranslatableCaption.of("events.event_denied"),
                    Template.of("value", "Flag add"));
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
        }
        final String value = StringMan.join(Arrays.copyOfRange(args, 1, args.length), " ");
        final PlotFlag parsed;
        try {
            parsed = event.getFlag().parse(value);
        } catch (FlagParseException e) {
            player.sendMessage(
                    TranslatableCaption.of("flag.flag_parse_error"),
                    Template.of("flag_name", plotFlag.getName()),
                    Template.of("flag_value", e.getValue()),
                    Templates.of(player, "error", e.getErrorMessage(), e.getTemplates())
            );
            return;
        }
        boolean result =
            player.getLocation().getPlotAbs().setFlag(localFlag.merge(parsed.getValue()));
        if (!result) {
            player.sendMessage(TranslatableCaption.of("flag.flag_not_added"));
            return;
        }
        player.sendMessage(TranslatableCaption.of("flag.flag_added"));
    }

    @CommandDeclaration(command = "remove",
        aliases = {"r", "remove", "delete"},
        usage = "/plot flag remove <flag> [values]",
        description = "Remove a flag",
        category = CommandCategory.SETTINGS,
        requiredType = RequiredType.NONE,
        permission = "plots.flag.remove")
    public void remove(final Command command, PlotPlayer<?> player, final String[] args,
        final RunnableVal3<Command, Runnable, Runnable> confirm,
        final RunnableVal2<Command, CommandResult> whenDone) {
        if (!checkRequirements(player)) {
            return;
        }
        if (args.length != 1 && args.length != 2) {
            player.sendMessage(
                    TranslatableCaption.of("commandconfig.command_syntax"),
                    Template.of("value", "/plot flag remove <flag> [values]")
            );
            return;
        }
        PlotFlag<?, ?> flag = getFlag(player, args[0]);
        if (flag == null) {
            return;
        }
        final Plot plot = player.getLocation().getPlotAbs();
        PlotFlagRemoveEvent event = new PlotFlagRemoveEvent(flag, plot);
        if (event.getEventResult() == Result.DENY) {
            player.sendMessage(
                    TranslatableCaption.of("events.event_denied"),
                    Template.of("value", "Flag remove"));
            return;
        }
        boolean force = event.getEventResult() == Result.FORCE;
        flag = event.getFlag();
        if (!force && !Permissions.hasPermission(player, Permission.PERMISSION_SET_FLAG_KEY.format(args[0].toLowerCase()))) {
            if (args.length != 2) {
                player.sendMessage(TranslatableCaption.of("permission.no_permission"),
                    Template.of("node", Permission.PERMISSION_SET_FLAG_KEY.format(args[0].toLowerCase())));
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
                player.sendMessage(
                        TranslatableCaption.of("flag.flag_parse_error"),
                        Template.of("flag_name", flag.getName()),
                        Template.of("flag_value", e.getValue()),
                        Templates.of(player, "error", e.getErrorMessage(), e.getTemplates())
                );
                return;
            }
            if (((List) parsedFlag.getValue()).isEmpty()) {
                player.sendMessage(TranslatableCaption.of("flag.flag_not_removed"));
                return;
            }
            if (list.removeAll((List) parsedFlag.getValue())) {
                if (list.isEmpty()) {
                    if (plot.removeFlag(flag)) {
                        player.sendMessage(TranslatableCaption.of("flag.flag_removed"));
                        return;
                    } else {
                        player.sendMessage(TranslatableCaption.of("flag.flag_not_removed"));
                        return;
                    }
                } else {
                    // MainUtil.sendMessage(player, Permission.FLAG_REMOVED);
                    PlotFlag plotFlag = parsedFlag.createFlagInstance(list);
                    PlotFlagAddEvent addEvent = new PlotFlagAddEvent(plotFlag, plot);
                    if (addEvent.getEventResult() == Result.DENY) {
                        player.sendMessage(
                                TranslatableCaption.of("events.event_denied"),
                                Template.of("value", "Re-addition of " + plotFlag.getName())
                        );
                        return;
                    }
                    if (plot.setFlag(addEvent.getFlag())) {
                        player.sendMessage(TranslatableCaption.of("flag.flag_partially_removed"));
                        return;
                    } else {
                        player.sendMessage(TranslatableCaption.of("flag.flag_not_removed"));
                        return;
                    }
                }
            } else {
                player.sendMessage(TranslatableCaption.of("flag.flag_not_removed"));
                return;
            }
        } else {
            boolean result = plot.removeFlag(flag);
            if (!result) {
                player.sendMessage(TranslatableCaption.of("flag.flag_not_removed"));
                return;
            }
        }
        player.sendMessage(TranslatableCaption.of("flag.flag_removed"));
    }

    @CommandDeclaration(command = "list",
        aliases = {"l", "list", "flags"},
        usage = "/plot flag list",
        description = "List all available plot flags",
        category = CommandCategory.SETTINGS,
        requiredType = RequiredType.NONE,
        permission = "plots.flag.list")
    public void list(final Command command, final PlotPlayer<?> player, final String[] args,
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
            final String category = MINI_MESSAGE.stripTokens(plotFlag.getFlagCategory().getComponent(player));
            final Collection<String> flagList =
                flags.computeIfAbsent(category, k -> new ArrayList<>());
            flagList.add(plotFlag.getName());
        }

        for (final Map.Entry<String, ArrayList<String>> entry : flags.entrySet()) {
            Collections.sort(entry.getValue());
            Component category =
                MINI_MESSAGE.parse(TranslatableCaption.of("flag.flag_list_categories").getComponent(player), Template.of("category", entry.getKey()));
            final Iterator<String> flagIterator = entry.getValue().iterator();
            while (flagIterator.hasNext()) {
                final String flag = flagIterator.next();
                category.append(MINI_MESSAGE
                    .parse(TranslatableCaption.of("flag.flag_list_flag").getComponent(player), Template.of("command", "/plot flag info " + flag),
                        Template.of("flag", flag), Template.of("suffix", flagIterator.hasNext() ? ", " : "")));
            }
            player.sendMessage(StaticCaption.of(MINI_MESSAGE.serialize(category)));
        }
    }

    @CommandDeclaration(command = "info",
        aliases = {"i", "info"},
        usage = "/plot flag info <flag>",
        description = "View information about a flag",
        category = CommandCategory.SETTINGS,
        requiredType = RequiredType.NONE,
        permission = "plots.flag.info")
    public void info(final Command command, final PlotPlayer<?> player, final String[] args,
        final RunnableVal3<Command, Runnable, Runnable> confirm,
        final RunnableVal2<Command, CommandResult> whenDone) {
        if (!checkRequirements(player)) {
            return;
        }
        if (args.length < 1) {
            player.sendMessage(
                    TranslatableCaption.of("commandconfig.command_syntax"),
                    Template.of("value", "/plot flag info <flag>")
            );
            return;
        }
        final PlotFlag<?, ?> plotFlag = getFlag(player, args[0]);
        if (plotFlag != null) {
            player.sendMessage(TranslatableCaption.of("flag.flag_info_header"));
            // Flag name
            player.sendMessage(TranslatableCaption.of("flag.flag_info_name"), Template.of("flag", plotFlag.getName()));
            // Flag category
            player.sendMessage(TranslatableCaption.of("flag.flag_info_category"),
                    Templates.of(player, "value", plotFlag.getFlagCategory()));
            // Flag description
            // TODO maybe merge and \n instead?
            player.sendMessage(TranslatableCaption.of("flag.flag_info_description"));
            player.sendMessage(plotFlag.getFlagDescription());
            // Flag example
            player.sendMessage(TranslatableCaption.of("flag.flag_info_example"),
                    Template.of("flag", plotFlag.getName()),
                    Template.of("value", plotFlag.getExample()));
            // Default value
            final String defaultValue = player.getLocation().getPlotArea().getFlagContainer()
                .getFlagErased(plotFlag.getClass()).toString();
            player.sendMessage(TranslatableCaption.of("flag.flag_info_default_value"),
                    Template.of("value", defaultValue));
            // Footer. Done this way to prevent the duplicate-message-thingy from catching it
            player.sendMessage(TranslatableCaption.of("flag.flag_info_footer"));
        }
    }

}
