package com.intellectualcrafters.plot.commands;

import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.object.PlotArea;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.object.RunnableVal2;
import com.intellectualcrafters.plot.object.RunnableVal3;
import com.intellectualcrafters.plot.util.MainUtil;
import com.plotsquared.general.commands.Command;
import com.plotsquared.general.commands.CommandDeclaration;

@CommandDeclaration(
        command = "toggle",
        aliases = {"attribute"},
        permission = "plots.use",
        description = "Toggle per user settings",
        requiredType = RequiredType.NONE,
        category = CommandCategory.SETTINGS)
public class Toggle extends Command {
    public Toggle() {
        super(MainCommand.getInstance(), true);
    }

    @CommandDeclaration(
            command = "chatspy",
            aliases = {"spy"},
            permission = "plots.admin.command.chat",
            description = "Toggle admin chat spying")
    public void chatspy(Command command, PlotPlayer player, String[] args, RunnableVal3<Command, Runnable, Runnable> confirm,
            RunnableVal2<Command, CommandResult> whenDone) {
        if (toggle(player, "chatspy")) {
            MainUtil.sendMessage(player, C.TOGGLE_DISABLED, command.toString());
        } else {
            MainUtil.sendMessage(player, C.TOGGLE_ENABLED, command.toString());
        }
    }

    @CommandDeclaration(
            command = "worldedit",
            aliases = {"we", "wea"},
            permission = "plots.worldedit.bypass",
            description = "Toggle worldedit area restrictions")
    public void worldedit(Command command, PlotPlayer player, String[] args, RunnableVal3<Command, Runnable, Runnable> confirm,
            RunnableVal2<Command, CommandResult> whenDone) {
        if (toggle(player, "worldedit")) {
            MainUtil.sendMessage(player, C.TOGGLE_DISABLED, command.toString());
        } else {
            MainUtil.sendMessage(player, C.TOGGLE_ENABLED, command.toString());
        }
    }

    @CommandDeclaration(
            command = "chat",
            permission = "plots.toggle.chat",
            description = "Toggle plot chat")
    public void chat(Command command, PlotPlayer player, String[] args, RunnableVal3<Command, Runnable, Runnable> confirm,
                     RunnableVal2<Command, CommandResult> whenDone) {
        if (toggle(player, "chat")) {
            MainUtil.sendMessage(player, C.TOGGLE_DISABLED, command.toString());
        } else {
            MainUtil.sendMessage(player, C.TOGGLE_ENABLED, command.toString());
        }
    }

    @CommandDeclaration(
            command = "clear-confirmation",
            permission = "plots.admin.command.autoclear",
            description = "Toggle autoclear confirmation")
    public void clearConfirmation(Command command, PlotPlayer player, String[] args, RunnableVal3<Command, Runnable, Runnable> confirm,
                     RunnableVal2<Command, CommandResult> whenDone) {
        if (toggle(player, "ignoreExpireTask")) {
            MainUtil.sendMessage(player, C.TOGGLE_DISABLED, command.toString());
        } else {
            MainUtil.sendMessage(player, C.TOGGLE_ENABLED, command.toString());
        }
    }

    @CommandDeclaration(
            command = "titles",
            permission = "plots.toggle.titles",
            description = "Toggle plot title messages")
    public void titles(Command command, PlotPlayer player, String[] args, RunnableVal3<Command, Runnable, Runnable> confirm,
            RunnableVal2<Command, CommandResult> whenDone) {
        PlotArea area = player.getApplicablePlotArea();
        boolean chat = area == null ? false : area.PLOT_CHAT;
        if (toggle(player, "disabletitles") != chat) {
            MainUtil.sendMessage(player, C.TOGGLE_ENABLED, command.toString());
        } else {
            MainUtil.sendMessage(player, C.TOGGLE_DISABLED, command.toString());
        }
    }

    public boolean toggle(PlotPlayer player, String key) {
        if (player.getAttribute(key)) {
            player.removeAttribute(key);
            return true;
        } else {
            player.setAttribute(key);
            return false;
        }
    }
}
