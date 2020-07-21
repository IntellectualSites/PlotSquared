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
package com.plotsquared.core.configuration.caption;

import com.plotsquared.core.plot.PlotArea;
import com.plotsquared.core.util.MainUtil;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.Template;

import javax.annotation.Nonnull;
import java.util.UUID;

/**
 * Utility class that generates {@link net.kyori.adventure.text.minimessage.Template templates}
 */
public final class Templates {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.builder().build();

    private Templates() {
        throw new UnsupportedOperationException(
            "This is a utility class and cannot be instantiated");
    }

    /**
     * Create a {@link net.kyori.adventure.text.minimessage.Template} from a PlotSquared {@link Caption}
     *
     * @param localeHolder Locale holder
     * @param key          Template key
     * @param caption      Caption object
     * @param replacements Replacements
     * @return Generated template
     */
    @Nonnull public static Template of(@Nonnull final LocaleHolder localeHolder,
        @Nonnull final String key, @Nonnull final Caption caption,
        @Nonnull final Template... replacements) {
        return Template.of(key, MINI_MESSAGE.parse(caption.getComponent(localeHolder), replacements));
    }

    /**
     * Create a {@link Template} from a username (using UUID mappings)
     *
     * @param key          Template key
     * @param uuid         Player UUID
     * @return Generated template
     */
    @Nonnull public static Template of(@Nonnull final String key, @Nonnull final UUID uuid) {
        final String username = MainUtil.getName(uuid);
        return Template.of(key, username);
    }

    /**
     * Create a {@link Template} from a string
     *
     * @param key   Template key
     * @param value Template value
     * @return Generated template
     */
    @Nonnull public static Template of(@Nonnull final String key, @Nonnull final String value) {
        return Template.of(key, value);
    }

    /**
     * Create a {@link Template} from a plot area
     *
     * @param key  Template Key
     * @param area Plot area
     * @return Generated template
     */
    @Nonnull public static Template of(@Nonnull final String key, @Nonnull final PlotArea area) {
        return Template.of(key, area.toString());
    }

    /**
     * Create a {@link Template} from a number
     *
     * @param key    Template key
     * @param number Number
     * @return Generated template
     */
    @Nonnull public static Template of(@Nonnull final String key, @Nonnull final Number number) {
        return Template.of(key, number.toString());
    }

}
