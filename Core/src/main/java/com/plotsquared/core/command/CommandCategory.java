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

import com.plotsquared.core.configuration.Captions;
import lombok.RequiredArgsConstructor;

/**
 * CommandCategory.
 */
@RequiredArgsConstructor
public enum CommandCategory {
    /**
     * Claiming CommandConfig.
     * Such as: /plot claim
     */
    CLAIMING(Captions.COMMAND_CATEGORY_CLAIMING),
    /**
     * Teleportation CommandConfig.
     * Such as: /plot visit
     */
    TELEPORT(Captions.COMMAND_CATEGORY_TELEPORT),
    /**
     * Protection.
     */
    SETTINGS(Captions.COMMAND_CATEGORY_SETTINGS),
    /**
     * Chat.
     */
    CHAT(Captions.COMMAND_CATEGORY_CHAT),
    /**
     * Web.
     */
    SCHEMATIC(Captions.COMMAND_CATEGORY_SCHEMATIC),
    /**
     * Cosmetic.
     */
    APPEARANCE(Captions.COMMAND_CATEGORY_APPEARANCE),
    /**
     * Information CommandConfig.
     * Such as: /plot info
     */
    INFO(Captions.COMMAND_CATEGORY_INFO),
    /**
     * Debug CommandConfig.
     * Such as: /plot debug
     */
    DEBUG(Captions.COMMAND_CATEGORY_DEBUG),
    /**
     * Administration commands.
     */
    ADMINISTRATION(Captions.COMMAND_CATEGORY_ADMINISTRATION);
    /**
     * The category name (Readable).
     */
    private final Captions caption;

    @Override public String toString() {
        return this.caption.getTranslated();
    }
}
