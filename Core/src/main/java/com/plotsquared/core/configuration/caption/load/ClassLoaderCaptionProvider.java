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
package com.plotsquared.core.configuration.caption.load;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;

import static com.plotsquared.core.configuration.caption.load.CaptionLoader.loadFromReader;

final class ClassLoaderCaptionProvider implements DefaultCaptionProvider {

    private static final Logger logger = LoggerFactory.getLogger("P2/" + ClassLoaderCaptionProvider.class.getSimpleName());
    private final ClassLoader classLoader;
    private final Function<@NonNull Locale, @NonNull String> urlProvider;

    ClassLoaderCaptionProvider(
            final @NonNull ClassLoader classLoader,
            final @NonNull Function<@NonNull Locale, @NonNull String> urlProvider
    ) {
        this.classLoader = classLoader;
        this.urlProvider = urlProvider;
    }

    @Override
    public @Nullable Map<String, String> loadDefaults(final @NonNull Locale locale) {
        final String url = this.urlProvider.apply(locale);
        try {
            final InputStream stream = this.classLoader.getResourceAsStream(url);
            if (stream == null) {
                logger.warn("No resource for locale '{}' found", locale);
                return null;
            }
            try (final BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
                return loadFromReader(reader);
            }
        } catch (final IOException e) {
            logger.error("Unable to load language resource", e);
            return null;
        }
    }

}
