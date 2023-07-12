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
package com.plotsquared.core.command;

import com.plotsquared.core.configuration.caption.TranslatableCaption;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.util.task.RunnableVal2;
import com.plotsquared.core.util.task.RunnableVal3;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

@CommandDeclaration(command = "toggle",
        aliases = {"attribute"},
        permission = "plots.toggle",
        usage = "/plot toggle <chat | chatspy | clear-confirmation | time | titles | worldedit>",
        requiredType = RequiredType.NONE,
        category = CommandCategory.SETTINGS)
public class Toggle extends Command {

    public Toggle() {
        super(MainCommand.getInstance(), true);
    }

    @CommandDeclaration(command = "chatspy",
            aliases = {"spy"},
            permission = "plots.admin.command.chatspy")
    public void chatspy(
            Command command, PlotPlayer<?> player, String[] args,
            RunnableVal3<Command, Runnable, Runnable> confirm,
            RunnableVal2<Command, CommandResult> whenDone
    ) {
        if (toggle(player, "chatspy")) {
            player.sendMessage(
                    TranslatableCaption.of("toggle.toggle_disabled"),
                    TagResolver.resolver("setting", Tag.inserting(Component.text(command.toString())))
            );
        } else {
            player.sendMessage(
                    TranslatableCaption.of("toggle.toggle_enabled"),
                    TagResolver.resolver("setting", Tag.inserting(Component.text(command.toString())))
            );
        }
    }

    @CommandDeclaration(command = "worldedit",
            aliases = {"we", "wea"},
            permission = "plots.worldedit.bypass")
    public void worldedit(
            Command command, PlotPlayer<?> player, String[] args,
            RunnableVal3<Command, Runnable, Runnable> confirm,
            RunnableVal2<Command, CommandResult> whenDone
    ) {
        if (toggle(player, "worldedit")) {
            player.sendMessage(
                    TranslatableCaption.of("toggle.toggle_disabled"),
                    TagResolver.resolver("setting", Tag.inserting(Component.text(command.toString())))
            );
        } else {
            player.sendMessage(
                    TranslatableCaption.of("toggle.toggle_enabled"),
                    TagResolver.resolver("setting", Tag.inserting(Component.text(command.toString())))
            );
        }
    }

    @CommandDeclaration(command = "chat",
            permission = "plots.toggle.chat")
    public void chat(
            Command command, PlotPlayer<?> player, String[] args,
            RunnableVal3<Command, Runnable, Runnable> confirm,
            RunnableVal2<Command, CommandResult> whenDone
    ) {
        if (toggle(player, "chat")) {
            player.sendMessage(
                    TranslatableCaption.of("toggle.toggle_disabled"),
                    TagResolver.resolver("setting", Tag.inserting(Component.text(command.toString())))
            );
        } else {
            player.sendMessage(
                    TranslatableCaption.of("toggle.toggle_enabled"),
                    TagResolver.resolver("setting", Tag.inserting(Component.text(command.toString())))
            );
        }
    }

    @CommandDeclaration(command = "clear-confirmation",
            permission = "plots.admin.command.autoclear")
    public void clearConfirmation(
            Command command, PlotPlayer<?> player, String[] args,
            RunnableVal3<Command, Runnable, Runnable> confirm,
            RunnableVal2<Command, CommandResult> whenDone
    ) {
        if (toggle(player, "ignoreExpireTask")) {
            player.sendMessage(
                    TranslatableCaption.of("toggle.toggle_enabled"),
                    TagResolver.resolver("setting", Tag.inserting(Component.text(command.toString())))
            );
        } else {
            player.sendMessage(
                    TranslatableCaption.of("toggle.toggle_disabled"),
                    TagResolver.resolver("setting", Tag.inserting(Component.text(command.toString())))
            );
        }
    }

    @CommandDeclaration(command = "titles",
            permission = "plots.toggle.titles")
    public void titles(
            Command command, PlotPlayer<?> player, String[] args,
            RunnableVal3<Command, Runnable, Runnable> confirm,
            RunnableVal2<Command, CommandResult> whenDone
    ) {
        if (toggle(player, "disabletitles")) {
            player.sendMessage(
                    TranslatableCaption.of("toggle.toggle_enabled"),
                    TagResolver.resolver("setting", Tag.inserting(Component.text(command.toString())))
            );
        } else {
            player.sendMessage(
                    TranslatableCaption.of("toggle.toggle_disabled"),
                    TagResolver.resolver("setting", Tag.inserting(Component.text(command.toString())))
            );
        }
    }

    @CommandDeclaration(command = "time",
            permission = "plots.toggle.time")
    public void time(
            Command command, PlotPlayer<?> player, String[] args,
            RunnableVal3<Command, Runnable, Runnable> confirm,
            RunnableVal2<Command, CommandResult> whenDone
    ) {
        if (toggle(player, "disabletime")) {
            player.sendMessage(
                    TranslatableCaption.of("toggle.toggle_enabled"),
                    TagResolver.resolver("setting", Tag.inserting(Component.text(command.toString())))
            );
        } else {
            player.sendMessage(
                    TranslatableCaption.of("toggle.toggle_disabled"),
                    TagResolver.resolver("setting", Tag.inserting(Component.text(command.toString())))
            );
        }
    }

    @CommandDeclaration(command = "debug",
            permission = "plots.toggle.debug")
    public void debug(
            Command command, PlotPlayer<?> player, String[] args,
            RunnableVal3<Command, Runnable, Runnable> confirm,
            RunnableVal2<Command, CommandResult> whenDone
    ) {
        if (toggle(player, "debug")) {
            player.sendMessage(
                    TranslatableCaption.of("toggle.toggle_disabled"),
                    TagResolver.resolver("setting", Tag.inserting(Component.text(command.toString())))
            );
        } else {
            player.sendMessage(
                    TranslatableCaption.of("toggle.toggle_enabled"),
                    TagResolver.resolver("setting", Tag.inserting(Component.text(command.toString())))
            );
        }
        player.refreshDebug();
    }

    public boolean toggle(PlotPlayer<?> player, String key) {
        if (player.getAttribute(key)) {
            player.removeAttribute(key);
            return true;
        } else {
            player.setAttribute(key);
            return false;
        }
    }

}
