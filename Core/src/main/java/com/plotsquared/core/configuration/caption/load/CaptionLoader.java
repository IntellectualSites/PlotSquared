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

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.plotsquared.core.configuration.caption.CaptionMap;
import com.plotsquared.core.configuration.caption.LocalizedCaptionMap;
import com.plotsquared.core.configuration.caption.PerUserLocaleCaptionMap;
import com.plotsquared.core.configuration.caption.TranslatableCaption;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This class handles loading and updating of message files.
 */
public final class CaptionLoader {

    private static final Logger LOGGER = LogManager.getLogger("PlotSquared/" + CaptionLoader.class.getSimpleName());

    private static final Gson GSON;

    static {
        GSON = new GsonBuilder()
                .setPrettyPrinting()
                .disableHtmlEscaping()
                .create();
    }

    private final Map<String, String> defaultMessages;
    private final Locale defaultLocale;
    private final Function<Path, Locale> localeExtractor;
    private final DefaultCaptionProvider captionProvider;
    private final String namespace;

    private CaptionLoader(
            final @NonNull Locale internalLocale,
            final @NonNull Function<@NonNull Path, @NonNull Locale> localeExtractor,
            final @NonNull DefaultCaptionProvider captionProvider,
            final @NonNull String namespace
    ) {
        this.defaultLocale = internalLocale;
        this.localeExtractor = localeExtractor;
        this.captionProvider = captionProvider;
        this.namespace = namespace;
        Map<String, String> temp;
        try {
            temp = this.captionProvider.loadDefaults(internalLocale);
        } catch (Exception e) {
            LOGGER.error("Failed to load default messages", e);
            temp = Collections.emptyMap();
        }
        this.defaultMessages = temp;
    }

    /**
     * Returns a new CaptionLoader instance. That instance will use the internalLocale to extract default values
     * from the captionProvider
     *
     * @param internalLocale  the locale used internally to resolve default messages from the caption provider.
     * @param localeExtractor a function to extract a locale from a path, e.g. by its name.
     * @param captionProvider the provider for default captions.
     * @return a CaptionLoader instance that can load and patch message files.
     */
    public static @NonNull CaptionLoader of(
            final @NonNull Locale internalLocale,
            final @NonNull Function<@NonNull Path, @NonNull Locale> localeExtractor,
            final @NonNull DefaultCaptionProvider captionProvider,
            final @NonNull String namespace
    ) {
        return new CaptionLoader(internalLocale, localeExtractor, captionProvider, namespace);
    }

    /**
     * Returns a function that extracts a locale from a path using the given pattern.
     * The pattern is required to have (at least) one capturing group, as this is used to access the locale
     * tag.The function will throw an {@link IllegalArgumentException} if the matcher doesn't match the file name
     * of the input path. The language tag is loaded using {@link Locale#forLanguageTag(String)}.
     *
     * @param pattern the pattern to match and extract the language tag with.
     * @return a function to extract a locale from a path using a pattern.
     * @see Matcher#group(int)
     * @see Path#getFileName()
     */
    public static @NonNull Function<@NonNull Path, @NonNull Locale> patternExtractor(final @NonNull Pattern pattern) {
        return path -> {
            final String fileName = path.getFileName().toString();
            final Matcher matcher = pattern.matcher(fileName);
            if (matcher.matches()) {
                return Locale.forLanguageTag(matcher.group(1));
            } else {
                throw new IllegalArgumentException(fileName + " is an invalid message file (cannot extract locale)");
            }
        };
    }

    /**
     * Loads a map of translation keys mapping to their translations from a reader.
     * The format is expected to be a json object:
     * <pre>{@code
     * {
     *     "key1": "value a",
     *     "key2": "value b",
     *     ...
     * }
     * }</pre>
     *
     * @param reader the reader to read the map from.
     * @return the translation map.
     */
    @SuppressWarnings("UnstableApiUsage")
    static @NonNull Map<@NonNull String, @NonNull String> loadFromReader(final @NonNull Reader reader) {
        final Type type = new TypeToken<Map<String, String>>() {
        }.getType();
        return new LinkedHashMap<>(GSON.fromJson(reader, type));
    }

    private static void save(final Path file, final Map<String, String> content) {
        try (final BufferedWriter writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            GSON.toJson(content, writer);
            LOGGER.info("Saved {} with new content", file.getFileName());
        } catch (final IOException e) {
            LOGGER.error("Failed to save caption file '{}'", file.getFileName().toString(), e);
        }
    }

    /**
     * Load all message files in the given directory into a new CaptionMap.
     *
     * @param directory The directory to load files from
     * @return A new CaptionMap containing the loaded messages
     * @throws IOException if the files in the given path can't be listed
     * @see Files#list(Path)
     * @see #loadSingle(Path)
     */
    public @NonNull CaptionMap loadAll(final @NonNull Path directory) throws IOException {
        final Map<Locale, CaptionMap> localeMaps = new HashMap<>();
        try (final Stream<Path> files = Files.list(directory)) {
            final List<Path> captionFiles = files.filter(Files::isRegularFile).toList();
            for (Path file : captionFiles) {
                try {
                    final CaptionMap localeMap = loadSingle(file);
                    localeMaps.put(localeMap.getLocale(), localeMap);
                } catch (Exception e) {
                    LOGGER.error("Failed to load language file '{}'", file.getFileName().toString(), e);
                }
            }
            LOGGER.info("Loaded {} message files. Loaded Languages: {}", localeMaps.size(), localeMaps.keySet());
            return new PerUserLocaleCaptionMap(localeMaps);
        }
    }

    /**
     * Load a message file into a new CaptionMap. The file name must match
     * the pattern expected by the {@link #localeExtractor}.
     * Note that this method does not attempt to create a new file.
     *
     * @param file The file to load
     * @return A new CaptionMap containing the loaded messages
     * @throws IOException              if the file couldn't be accessed or read successfully.
     * @throws IllegalArgumentException if the file name doesn't match the specified format.
     * @see #loadOrCreateSingle(Path)
     */
    public @NonNull CaptionMap loadSingle(final @NonNull Path file) throws IOException {
        final Locale locale = this.localeExtractor.apply(file);
        try (final BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            Map<String, String> map = loadFromReader(reader);
            if (patch(map, locale)) {
                save(file, map); // update the file using the modified map
            }
            return new LocalizedCaptionMap(locale, mapToCaptions(map));
        }
    }

    /**
     * Load a message file into a new CaptionMap. The file name must match
     * the pattern expected by the {@link #localeExtractor}.
     * If no file exists at the given path, this method will
     * attempt to create one and fill it with default values.
     *
     * @param file The file to load
     * @return A new CaptionMap containing the loaded messages
     * @throws IOException              if the file couldn't be accessed or read successfully.
     * @throws IllegalArgumentException if the file name doesn't match the specified format.
     * @see #loadSingle(Path)
     * @since 6.9.3
     */
    public @NonNull CaptionMap loadOrCreateSingle(final @NonNull Path file) throws IOException {
        final Locale locale = this.localeExtractor.apply(file);
        if (!Files.exists(file)) {
            Map<String, String> map = new LinkedHashMap<>();
            patch(map, locale);
            save(file, map);
            return new LocalizedCaptionMap(locale, mapToCaptions(map));
        } else {
            return loadSingle(file);
        }
    }

    private @NonNull Map<TranslatableCaption, String> mapToCaptions(Map<String, String> map) {
        return map.entrySet().stream().collect(
                Collectors.toMap(
                        entry -> TranslatableCaption.of(this.namespace, entry.getKey()),
                        Map.Entry::getValue
                ));
    }

    /**
     * Add missing entries to the given map.
     * Entries are missing if the key exists in {@link #defaultLocale} but isn't present
     * in the given map. For a missing key, a value will be loaded either from
     * the resource matching the given locale or from {@link #defaultLocale} if
     * no matching resource was found or the key isn't present in the resource.
     *
     * @param map    the map to patch
     * @param locale the locale to get the resource from
     * @return {@code true} if the map was patched.
     */
    private boolean patch(final Map<String, String> map, final Locale locale) {
        boolean modified = false;
        Map<String, String> languageSpecific;
        if (locale.equals(this.defaultLocale)) {
            languageSpecific = this.defaultMessages;
        } else {
            languageSpecific = this.captionProvider.loadDefaults(locale);
            if (languageSpecific == null) { // fallback for languages not provided
                languageSpecific = this.defaultMessages;
            }
        }
        for (Map.Entry<String, String> entry : this.defaultMessages.entrySet()) {
            if (!map.containsKey(entry.getKey())) {
                final String value = languageSpecific.getOrDefault(entry.getKey(), entry.getValue());
                map.put(entry.getKey(), value);
                modified = true;
            }
        }
        return modified;
    }

}
