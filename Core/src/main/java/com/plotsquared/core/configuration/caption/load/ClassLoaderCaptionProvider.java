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
package com.plotsquared.core.configuration.caption.load;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;

import static com.plotsquared.core.configuration.caption.load.CaptionLoader.loadFromReader;

final class ClassLoaderCaptionProvider implements DefaultCaptionProvider {

    private static final Logger LOGGER = LogManager.getLogger("PlotSquared/" + ClassLoaderCaptionProvider.class.getSimpleName());
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
                LOGGER.info("No resource for locale '{}' found in the plugin file." +
                                "Please ensure you have placed the latest version of the file messages_{}.json in the 'lang' folder." +
                                "You may be able to find completed translations at https://intellectualsites.crowdin.com/plotsquared",
                        locale, locale
                );
                return null;
            }
            try (final BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
                return loadFromReader(reader);
            }
        } catch (final IOException e) {
            LOGGER.error("Unable to load language resource", e);
            return null;
        }
    }

}
