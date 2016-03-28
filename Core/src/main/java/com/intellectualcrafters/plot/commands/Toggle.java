////////////////////////////////////////////////////////////////////////////////////////////////////
// PlotSquared - A plot manager and world generator for the Bukkit API                             /
// Copyright (c) 2014 IntellectualSites/IntellectualCrafters                                       /
//                                                                                                 /
// This program is free software; you can redistribute it and/or modify                            /
// it under the terms of the GNU General Public License as published by                            /
// the Free Software Foundation; either version 3 of the License, or                               /
// (at your option) any later version.                                                             /
//                                                                                                 /
// This program is distributed in the hope that it will be useful,                                 /
// but WITHOUT ANY WARRANTY; without even the implied warranty of                                  /
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the                                   /
// GNU General Public License for more details.                                                    /
//                                                                                                 /
// You should have received a copy of the GNU General Public License                               /
// along with this program; if not, write to the Free Software Foundation,                         /
// Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301  USA                               /
//                                                                                                 /
// You can contact us via: support@intellectualsites.com                                           /
////////////////////////////////////////////////////////////////////////////////////////////////////
package com.intellectualcrafters.plot.commands;

import com.intellectualcrafters.plot.config.C;
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
            command = "titles",
            permission = "plots.toggle.titles",
            description = "Toggle plot title messages")
    public void titles(Command command, PlotPlayer player, String[] args, RunnableVal3<Command, Runnable, Runnable> confirm,
            RunnableVal2<Command, CommandResult> whenDone) {
        if (toggle(player, "titles")) {
            MainUtil.sendMessage(player, C.TOGGLE_DISABLED, command.toString());
        } else {
            MainUtil.sendMessage(player, C.TOGGLE_ENABLED, command.toString());
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
