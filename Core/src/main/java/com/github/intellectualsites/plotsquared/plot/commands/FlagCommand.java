package com.github.intellectualsites.plotsquared.plot.commands;

import com.github.intellectualsites.plotsquared.commands.Command;
import com.github.intellectualsites.plotsquared.commands.CommandDeclaration;
import com.github.intellectualsites.plotsquared.plot.config.Captions;
import com.github.intellectualsites.plotsquared.plot.flags.GlobalFlagContainer;
import com.github.intellectualsites.plotsquared.plot.flags.PlotFlag;
import com.github.intellectualsites.plotsquared.plot.object.Location;
import com.github.intellectualsites.plotsquared.plot.object.Plot;
import com.github.intellectualsites.plotsquared.plot.object.PlotMessage;
import com.github.intellectualsites.plotsquared.plot.object.PlotPlayer;
import com.github.intellectualsites.plotsquared.plot.object.RunnableVal2;
import com.github.intellectualsites.plotsquared.plot.object.RunnableVal3;
import com.github.intellectualsites.plotsquared.plot.util.MainUtil;
import com.github.intellectualsites.plotsquared.plot.util.Permissions;
import com.github.intellectualsites.plotsquared.plot.util.StringComparison;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

@CommandDeclaration(command = "flag", aliases = {"f",
    "flag"}, usage = "/plot flag <set|remove|add|list|info> <flag> <value>",
    description = "Manage plot flags",
    category = CommandCategory.SETTINGS,
    requiredType = RequiredType.NONE,
    permission = "plots.flag")
public class FlagCommand extends SubCommand {

    @Override public boolean onCommand(PlotPlayer player, String[] args) {
        MainUtil.sendMessage(player, Captions.COMMAND_SYNTAX, getUsage());
        return false;
    }

    /**
     * Checks if the player is allowed to modify the flags at their current location
     *
     * @return true if the player is allowed to modify the flags at their current location
     */
    private boolean checkRequirements(final PlotPlayer player) {
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
        if (!plot.isOwner(player.getUUID()) && !Permissions.hasPermission(player, Captions.PERMISSION_SET_FLAG_OTHER)) {
            MainUtil.sendMessage(player, Captions.NO_PERMISSION, Captions.PERMISSION_SET_FLAG_OTHER);
            return false;
        }
        return true;
    }

    /**
     * Attempt to extract the plot flag from the command arguments. If the flag cannot
     * be found, a flag suggestion may be sent to the player.
     *
     * @param player Player executing the command
     * @param arg String to extract flag from
     * @return The flag, if found, else null
     */
    @Nullable private PlotFlag<?, ?> getFlag(final PlotPlayer player, final String arg) {
        if (arg != null && arg.length() > 0) {
            final PlotFlag<?,?> flag = GlobalFlagContainer.getInstance().getFlagFromString(arg);
            if (flag == null) {
                boolean suggested = false;
                try {
                    final StringComparison<PlotFlag<?, ?>> stringComparison =
                        new StringComparison<>(arg, GlobalFlagContainer.getInstance().getFlagMap().values());
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

    @Override public Collection<Command> tab(PlotPlayer player, String[] args, boolean space) {
        return tabOf(player, args, space, getUsage());
    }

    @CommandDeclaration(command = "set", aliases = {"s", "set"},
        usage = "/plot flag set <flag> <value>",
        description = "Set a plot flag",
        category = CommandCategory.SETTINGS,
        requiredType = RequiredType.NONE,
        permission = "plots.flag.set")
    public void set(Command command, PlotPlayer player, String[] args,
        RunnableVal3<Command, Runnable, Runnable> confirm,
        RunnableVal2<Command, CommandResult> whenDone) {
        if (!checkRequirements(player)) {
            return;
        }
    }

    @CommandDeclaration(command = "add", aliases = {"a", "add"},
        usage = "/plot flag add <flag> <value>",
        description = "Add a plot flag value",
        category = CommandCategory.SETTINGS,
        requiredType = RequiredType.NONE,
        permission = "plots.flag.add")
    public void add(Command command, PlotPlayer player, String[] args,
        RunnableVal3<Command, Runnable, Runnable> confirm,
        RunnableVal2<Command, CommandResult> whenDone) {
        if (!checkRequirements(player)) {
            return;
        }
    }

    @CommandDeclaration(command = "list", aliases = {"l", "list", "flags"},
        usage = "/plot flag list",
        description = "List all available plot flags",
        category = CommandCategory.SETTINGS,
        requiredType = RequiredType.NONE,
        permission = "plots.flag.list")
    public void list(Command command, PlotPlayer player, String[] args,
        RunnableVal3<Command, Runnable, Runnable> confirm,
        RunnableVal2<Command, CommandResult> whenDone) {
        if (!checkRequirements(player)) {
            return;
        }
    }

    @CommandDeclaration(command = "info", aliases = {"i", "info"},
        usage = "/plot flag info <flag>",
        description = "View information about a flag",
        category = CommandCategory.SETTINGS,
        requiredType = RequiredType.NONE,
        permission = "plots.flag.info")
    public void info(Command command, PlotPlayer player, String[] args,
        RunnableVal3<Command, Runnable, Runnable> confirm,
        RunnableVal2<Command, CommandResult> whenDone) {
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
                .color(Captions.FLAG_INFO_COLOR_KEY.getTranslated()).text(plotFlag.getFlagCategory().getTranslated())
                .color(Captions.FLAG_INFO_COLOR_VALUE.getTranslated()).send(player);
            // Flag description
            new PlotMessage(Captions.FLAG_INFO_DESCRIPTION.getTranslated())
                .color(Captions.FLAG_INFO_COLOR_KEY.getTranslated()).send(player);
            new PlotMessage(plotFlag.getFlagDescription().getTranslated())
                .color(Captions.FLAG_INFO_COLOR_VALUE.getTranslated()).send(player);
            // Flag example
            new PlotMessage(Captions.FLAG_INFO_EXAMPLE.getTranslated())
                .color(Captions.FLAG_INFO_COLOR_KEY.getTranslated()).text(plotFlag.getExample())
                .color(Captions.FLAG_INFO_COLOR_VALUE.getTranslated()).suggest("/plot flag set " +
                plotFlag.getName() + " " + plotFlag.getExample()).send(player);
            // Default value
            final String defaultValue = player.getLocation().getPlotArea().getFlagContainer()
                .getFlagErased(plotFlag.getClass()).toString();
            new PlotMessage(Captions.FLAG_INFO_DEFAULT_VALUE.getTranslated())
                .color(Captions.FLAG_INFO_COLOR_KEY.getTranslated()).text(defaultValue)
                .color(Captions.FLAG_INFO_COLOR_VALUE.getTranslated()).send(player);
        }
    }

}
