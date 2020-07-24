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
import net.kyori.adventure.text.minimessage.Template;

import javax.annotation.Nonnull;

/**
 * Any entity that is able to execute commands, receive messages &amp; and have
 * permission nodes
 */
public interface CommandCaller {

    /**
     * Send a message to the command caller
     *
     * @param caption      Caption to send
     * @param replacements Variable replacements
     */
    void sendMessage(@Nonnull Caption caption, @Nonnull Template... replacements);

    /**
     * Check the player's permissions. <i>Will be cached if permission caching is enabled.</i>
     *
     * @param permission the name of the permission
     */
    boolean hasPermission(@Nonnull String permission);

    /**
     * Checks if this object contains an override for the specified
     * permission, by fully qualified name
     *
     * @param permission Name of the permission
     * @return true if the permission is set, otherwise false
     */
    boolean isPermissionSet(@Nonnull String permission);

    /**
     * Get the type of the caller
     *
     * @return Caller type
     */
    RequiredType getSuperCaller();

}
