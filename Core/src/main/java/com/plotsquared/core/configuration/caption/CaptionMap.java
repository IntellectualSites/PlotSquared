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
package com.plotsquared.core.configuration.caption;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Locale;
import java.util.Set;

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
    @NonNull String getMessage(final @NonNull TranslatableCaption caption) throws NoSuchCaptionException;

    /**
     * Get a message using a specific locale
     *
     * @param caption      Caption containing the caption key
     * @param localeHolder Holder that determines the message locale
     * @return Component
     * @throws NoSuchCaptionException if no caption with the given key exists
     */
    @NonNull String getMessage(final @NonNull TranslatableCaption caption, final @NonNull LocaleHolder localeHolder) throws
            NoSuchCaptionException;

    /**
     * Check if the map supports a given locale
     *
     * @param locale Locale
     * @return {@code true} if the map supports the locale
     */
    boolean supportsLocale(final @NonNull Locale locale);

    /**
     * Get the locale of the messages stored in the map
     *
     * @return Message locale
     */
    @NonNull Locale getLocale();

    /**
     * Gets a copy of the set of captions stored in the CaptionMap
     *
     * @return An immutable set of TranslatableCaption
     */
    @NonNull Set<TranslatableCaption> getCaptions();

    class NoSuchCaptionException extends IllegalArgumentException {

        public NoSuchCaptionException(final @NonNull NamespacedCaption caption) {
            super(String.format("No caption with the key '%s:%s' exists in the map", caption.getNamespace(), caption.getKey()));
        }

    }

}
