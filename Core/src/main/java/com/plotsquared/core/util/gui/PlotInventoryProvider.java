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
 *               Copyright (C) 2014 - 2022 IntellectualSites
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
package com.plotsquared.core.util.gui;

import com.plotsquared.core.configuration.caption.Caption;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

/**
 * Provider for creating a {@link PlotInventory}
 *
 * @param <P> The platform player
 * @param <I> The platform item
 */
public interface PlotInventoryProvider<P, I> {

    /**
     * Creates a new {@link PlotInventory} based on the passed data for the current platform.
     *
     * @param size           The size of the inventory (must be a multiple of 9)
     * @param titleCaption   The title for the inventory
     * @param titleResolvers The (optional) placeholder resolvers for the inventory
     * @return The platform inventory
     */
    PlotInventory<P, I> createInventory(int size, Caption titleCaption, TagResolver... titleResolvers);

}
