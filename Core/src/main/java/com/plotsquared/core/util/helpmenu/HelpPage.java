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
package com.plotsquared.core.util.helpmenu;

import com.plotsquared.core.command.CommandCategory;
import com.plotsquared.core.configuration.caption.StaticCaption;
import com.plotsquared.core.configuration.caption.TranslatableCaption;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.util.ComponentHelper;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

import java.util.ArrayList;
import java.util.List;

public class HelpPage {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private final List<HelpObject> helpObjects;
    private final TagResolver pageHeaderResolver;

    public HelpPage(CommandCategory category, int currentPage, int maxPages) {
        this.helpObjects = new ArrayList<>();
        this.pageHeaderResolver = TagResolver.builder()
                .tag("category", Tag.inserting(Component.text(category == null ? "ALL" : category.name())))
                .tag("current", Tag.inserting(Component.text(currentPage + 1)))
                .tag("max", Tag.inserting(Component.text(maxPages + 1)))
                .build();
    }

    public void render(PlotPlayer<?> player) {
        if (this.helpObjects.size() < 1) {
            player.sendMessage(TranslatableCaption.of("help.no_permission"));
        } else {
            TagResolver contentResolver = TagResolver.builder()
                    .tag("header", Tag.inserting(TranslatableCaption.of("help.help_header").toComponent(player)))
                    .tag("page_header", Tag.inserting(MINI_MESSAGE.deserialize(
                            TranslatableCaption.of("help.help_page_header").getComponent(player),
                            pageHeaderResolver
                    )))
                    .tag("help_objects", Tag.inserting(ComponentHelper.join(this.helpObjects, Component.text("\n"))))
                    .tag("footer", Tag.inserting(TranslatableCaption.of("help.help_footer").toComponent(player)))
                    .build();
            player.sendMessage(
                    StaticCaption.of("<header>\n<page_header>\n<help_objects>\n<footer>"),
                    contentResolver
            );
        }
    }

    public void addHelpItem(HelpObject object) {
        this.helpObjects.add(object);
    }

}
