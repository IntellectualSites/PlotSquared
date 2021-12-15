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
 *                  Copyright (C) 2021 IntellectualSites
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
package com.plotsquared.core.util.helpmenu;

import com.plotsquared.core.command.CommandCategory;
import com.plotsquared.core.configuration.caption.StaticCaption;
import com.plotsquared.core.configuration.caption.TranslatableCaption;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.util.StringMan;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.placeholder.Placeholder;
import net.kyori.adventure.text.minimessage.placeholder.PlaceholderResolver;

import java.util.ArrayList;
import java.util.List;

public class HelpPage {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.builder().build();
    private final List<HelpObject> helpObjects;
    private final Placeholder<?> catTemplate;
    private final Placeholder<?> curTemplate;
    private final Placeholder<?> maxTemplate;

    public HelpPage(CommandCategory category, int currentPage, int maxPages) {
        this.helpObjects = new ArrayList<>();
        this.catTemplate = Placeholder.miniMessage("category", category == null ? "ALL" : category.name());
        this.curTemplate = Placeholder.miniMessage("current", String.valueOf(currentPage + 1));
        this.maxTemplate = Placeholder.miniMessage("max", String.valueOf(maxPages + 1));
    }

    public void render(PlotPlayer<?> player) {
        if (this.helpObjects.size() < 1) {
            player.sendMessage(TranslatableCaption.miniMessage("help.no_permission"));
        } else {
            Placeholder<?> header = Placeholder.miniMessage("header", TranslatableCaption.miniMessage("help.help_header").getComponent(player));
            Placeholder<?> page_header = Placeholder.component(
                    "page_header",
                    MINI_MESSAGE.deserialize(
                            TranslatableCaption.miniMessage("help.help_page_header").getComponent(player),
                            PlaceholderResolver.placeholders(
                                    catTemplate,
                                    curTemplate,
                                    maxTemplate
                            )
                    )
            );
            Placeholder<?> help_objects = Placeholder.miniMessage("help_objects", StringMan.join(this.helpObjects, ""));
            Placeholder<?> footer = Placeholder.miniMessage("footer", TranslatableCaption.miniMessage("help.help_footer").getComponent(player));
            player.sendMessage(
                    StaticCaption.miniMessage("<header>\n<page_header>\n<help_objects>\n<footer>"),
                    header,
                    page_header,
                    help_objects,
                    footer
            );
        }
    }

    public void addHelpItem(HelpObject object) {
        this.helpObjects.add(object);
    }

}
