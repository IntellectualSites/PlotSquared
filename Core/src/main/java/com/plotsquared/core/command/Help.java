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
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.plotsquared.core.command;

import com.plotsquared.core.configuration.caption.StaticCaption;
import com.plotsquared.core.configuration.caption.TranslatableCaption;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.util.MathMan;
import com.plotsquared.core.util.StringMan;
import com.plotsquared.core.util.helpmenu.HelpMenu;
import com.plotsquared.core.util.task.RunnableVal2;
import com.plotsquared.core.util.task.RunnableVal3;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.minimessage.Template;

import java.util.concurrent.CompletableFuture;

@CommandDeclaration(command = "help",
    description = "Get this help menu",
    aliases = "?",
    category = CommandCategory.INFO,
    usage = "help [category|#]",
    permission = "plots.use")
public class Help extends Command {
    public Help(Command parent) {
        super(parent, true);
    }

    @Override public boolean canExecute(PlotPlayer player, boolean message) {
        return true;
    }

    @Override
    public CompletableFuture<Boolean> execute(PlotPlayer<?> player, String[] args,
        RunnableVal3<Command, Runnable, Runnable> confirm,
        RunnableVal2<Command, CommandResult> whenDone) {
        switch (args.length) {
            case 0:
                return displayHelp(player, null, 0);
            case 1:
                if (MathMan.isInteger(args[0])) {
                    try {
                        return displayHelp(player, null, Integer.parseInt(args[0]));
                    } catch (NumberFormatException ignored) {
                        return displayHelp(player, null, 1);
                    }
                } else {
                    return displayHelp(player, args[0], 1);
                }
            case 2:
                if (MathMan.isInteger(args[1])) {
                    try {
                        return displayHelp(player, args[0], Integer.parseInt(args[1]));
                    } catch (NumberFormatException ignored) {
                        return displayHelp(player, args[0], 1);
                    }
                }
                return CompletableFuture.completedFuture(false);
            default:
                sendUsage(player);
        }
        return CompletableFuture.completedFuture(true);
    }

    public CompletableFuture<Boolean> displayHelp(final PlotPlayer player, final String catRaw,
        final int page) {
        return CompletableFuture.supplyAsync(() -> {
            String cat = catRaw;

            CommandCategory catEnum = null;
            if (cat != null) {
                if (!"all".equalsIgnoreCase(cat)) {
                    for (CommandCategory c : CommandCategory.values()) {
                        if (StringMan.isEqualIgnoreCaseToAny(cat, c.name(), c.toString())) {
                            catEnum = c;
                            cat = c.name();
                            break;
                        }
                    }
                    if (catEnum == null) {
                        cat = null;
                    }
                }
            }
            if (cat == null && page == 0) {
                TextComponent.Builder builder = TextComponent.builder();
                builder.append(MINI_MESSAGE.parse(TranslatableCaption.of("help.help_header").getComponent(player)));
                for (CommandCategory c : CommandCategory.values()) {
                    builder.append("\n").append(MINI_MESSAGE
                        .parse(TranslatableCaption.of("help.help_info_item").getComponent(player), Template.of("category", c.name().toLowerCase()),
                            Template.of("category_desc", c.getComponent(player))));
                }
                builder.append("\n").append(MINI_MESSAGE
                    .parse(TranslatableCaption.of("help.help_info_item").getComponent(player), Template.of("category", "all"),
                        Template.of("category_desc", "Display all commands")));
                builder.append("\n").append(MINI_MESSAGE.parse(TranslatableCaption.of("help.help_footer").getComponent(player)));
                player.sendMessage(StaticCaption.of(MINI_MESSAGE.serialize(builder.asComponent())));
                return true;
            }
            new HelpMenu(player).setCategory(catEnum).getCommands().generateMaxPages().generatePage(page - 1, getParent().toString()).render();
            return true;
        });
    }
}
