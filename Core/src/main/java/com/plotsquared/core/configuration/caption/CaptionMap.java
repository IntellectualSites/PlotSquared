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

import javax.annotation.Nonnull;
import java.util.Locale;

/**
 * Map containing mappings between {@link TranslatableCaption captions} and
 * {@link net.kyori.adventure.text.Component components}
 */
public interface CaptionMap {

    /**
     * Get a message using the server locale
     *
     * @param caption Caption containing the caption key
     * @return Component
     * @throws NoSuchCaptionException if no caption with the given key exists
     */
    @Nonnull String getMessage(@Nonnull TranslatableCaption caption) throws NoSuchCaptionException;

    /**
     * Get a message using a specific locale
     *
     * @param caption Caption containing the caption key
     * @param localeHolder Holder that determines the message locale
     * @return Component
     * @throws NoSuchCaptionException if no caption with the given key exists
     */
    String getMessage(@Nonnull TranslatableCaption caption, @Nonnull LocaleHolder localeHolder) throws NoSuchCaptionException;

    /**
     * Check if the map supports a given locale
     *
     * @param locale Locale
     * @return True if the map supports the locale
     */
    boolean supportsLocale(@Nonnull Locale locale);

    /**
     * Get the locale of the messages stored in the map
     *
     * @return Message locale
     */
    @Nonnull Locale getLocale();

    class NoSuchCaptionException extends IllegalArgumentException {

        public NoSuchCaptionException(@Nonnull final KeyedCaption caption) {
            super(String.format("No caption with the key '%s' exists in the map", caption.getKey()));
        }

    }

}
