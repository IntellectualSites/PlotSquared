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
package com.plotsquared.core.util.helpmenu;

import com.plotsquared.core.command.CommandCategory;
import com.plotsquared.core.configuration.Captions;
import com.plotsquared.core.configuration.caption.StaticCaption;
import com.plotsquared.core.configuration.caption.TranslatableCaption;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.util.StringMan;
import net.kyori.adventure.text.minimessage.Template;

import java.util.ArrayList;
import java.util.List;

public class HelpPage {

    private final List<HelpObject> helpObjects;
    private final String header;

    public HelpPage(CommandCategory category, int currentPage, int maxPages) {
        this.helpObjects = new ArrayList<>();
        this.header = Captions.HELP_PAGE_HEADER.getTranslated()
            .replace("%category%", category == null ? "ALL" : category.toString())
            .replace("%current%", (currentPage + 1) + "").replace("%max%", (maxPages + 1) + "");
    }

    public void render(PlotPlayer player) {
        if (this.helpObjects.size() < 1) {
            player.sendMessage(
                    TranslatableCaption.of("invalid.not_valid_number"),
                    Template.of("value", "(0)")
            );
        } else {
            String message =
                Captions.HELP_HEADER.getTranslated() + "\n" + this.header + "\n" + StringMan
                    .join(this.helpObjects, "\n") + "\n" + Captions.HELP_FOOTER.getTranslated();
            player.sendMessage(StaticCaption.of(message));
        }
    }

    public void addHelpItem(HelpObject object) {
        this.helpObjects.add(object);
    }
}
