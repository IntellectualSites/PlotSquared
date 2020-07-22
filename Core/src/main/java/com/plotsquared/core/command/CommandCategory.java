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

import com.plotsquared.core.configuration.caption.Caption;
import com.plotsquared.core.configuration.Captions;
import com.plotsquared.core.configuration.caption.LocaleHolder;
import com.plotsquared.core.configuration.caption.TranslatableCaption;

import javax.annotation.Nonnull;

/**
 * CommandCategory.
 */
public enum CommandCategory implements Caption {
    /**
     * Claiming CommandConfig.
     * Such as: /plot claim
     */
    CLAIMING(TranslatableCaption.of("category.command_category_claiming")),
    /**
     * Teleportation CommandConfig.
     * Such as: /plot visit
     */
    TELEPORT(TranslatableCaption.of("category.command_category_teleport")),
    /**
     * Protection.
     */
    SETTINGS(TranslatableCaption.of("category.command_category_settings")),
    /**
     * Chat.
     */
    CHAT(TranslatableCaption.of("category.command_category_chat")),
    /**
     * Web.
     */
    SCHEMATIC(TranslatableCaption.of("category.command_category_schematic")),
    /**
     * Cosmetic.
     */
    APPEARANCE(TranslatableCaption.of("category.command_category_appearance")),
    /**
     * Information CommandConfig.
     * Such as: /plot info
     */
    INFO(TranslatableCaption.of("category.command_category_info")),
    /**
     * Debug CommandConfig.
     * Such as: /plot debug
     */
    DEBUG(TranslatableCaption.of("category.command_category_debug")),
    /**
     * Administration commands.
     */
    ADMINISTRATION(TranslatableCaption.of("category.command_category_administration"));
    /**
     * The category name (Readable).
     */
    private final Caption caption;

    CommandCategory(final Caption caption) {
        this.caption = caption;
    }

    // TODO this method shouldn't be invoked
    @Deprecated @Override public String toString() {
        return this.caption.getComponent(LocaleHolder.console());
    }

    @Nonnull
    @Override
    public String getComponent(@Nonnull LocaleHolder localeHolder) {
        return this.caption.getComponent(localeHolder);
    }
}
