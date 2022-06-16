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

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Locale;
import java.util.Map;
import java.util.function.Function;

public interface DefaultCaptionProvider {

    /**
     * Returns a DefaultCaptionProvider that loads captions from a {@link ClassLoader}'s resources.
     * The resource urls are determined by applying the given function to a locale.
     *
     * @param classLoader the class loader to load caption resources from.
     * @param urlProvider the function to get an url from a locale.
     * @return a caption provider using a function to determine resource urls.
     */
    static @NonNull DefaultCaptionProvider forClassLoader(
            final @NonNull ClassLoader classLoader,
            final @NonNull Function<@NonNull Locale, @NonNull String> urlProvider
    ) {
        return new ClassLoaderCaptionProvider(classLoader, urlProvider);
    }

    /**
     * Returns a DefaultCaptionProvider that loads captions from a {@link ClassLoader}'s resources.
     * The resource urls are determined by replacing the first occurrence of {@code %s} in the string with
     * {@link Locale#toString()}.
     *
     * @param classLoader the class loader to load caption resources from.
     * @param toFormat    a string that can be formatted to result in a valid resource url when calling
     *                    {@code String.format(toFormat, Locale#toString)}
     * @return a caption provider using string formatting to determine resource urls.
     */
    static @NonNull DefaultCaptionProvider forClassLoaderFormatString(
            final @NonNull ClassLoader classLoader,
            final @NonNull String toFormat
    ) {
        return forClassLoader(classLoader, locale -> String.format(toFormat, locale));
    }

    /**
     * Loads default translation values for a specific language and returns it as a map.
     * If no default translation exists, {@code null} is returned. A returned map might be empty.
     *
     * @param locale the locale to load the values for.
     * @return a map of default values for the given locale.
     */
    @Nullable Map<@NonNull String, @NonNull String> loadDefaults(final @NonNull Locale locale);

}
