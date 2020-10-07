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
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.plotsquared.core.configuration.caption;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This class handles loading and updating of message files.
 */
public final class CaptionLoader {
    private static final Logger logger = LoggerFactory.getLogger("P2/" + CaptionLoader.class.getSimpleName());

    private static final Map<String, String> DEFAULT_MESSAGES;
    private static final Locale DEFAULT_LOCALE;
    private static final Gson GSON;
    private static final Pattern FILE_NAME_PATTERN;

    static {
        FILE_NAME_PATTERN = Pattern.compile("messages_(.*)\\.json");
        GSON = new GsonBuilder()
                .setPrettyPrinting()
                .disableHtmlEscaping()
                .create();
        DEFAULT_LOCALE = Locale.ENGLISH;
        Map<String, String> temp;
        try {
            temp = loadResource(DEFAULT_LOCALE);
        } catch (Exception e) {
            logger.error("Failed to load default messages", e);
            temp = Collections.emptyMap();
        }
        DEFAULT_MESSAGES = temp;
    }

    private CaptionLoader() {
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
    @Nonnull public static CaptionMap loadAll(@Nonnull final Path directory) throws IOException {
        final Map<Locale, CaptionMap> localeMaps = new HashMap<>();
        try (final Stream<Path> files = Files.list(directory)) {
            final List<Path> captionFiles = files.filter(Files::isRegularFile).collect(Collectors.toList());
            for (Path file : captionFiles) {
                try {
                    final CaptionMap localeMap = loadSingle(file);
                    localeMaps.put(localeMap.getLocale(), localeMap);
                } catch (Exception e) {
                    logger.error("Failed to load language file '{}'", file.getFileName().toString(), e);
                }
            }
            logger.info("Loaded {} message files. Loaded Languages: {}", localeMaps.size(), localeMaps.keySet());
            return new PerUserLocaleCaptionMap(localeMaps);
        }
    }

    /**
     * Load a message file into a new CaptionMap. The file name must match
     * the pattern {@code messages_<locale>.json} where {@code <locale>}
     * is a valid {@link Locale} string.
     *
     * @param file The file to load
     * @return A new CaptionMap containing the loaded messages
     * @throws IOException if the file couldn't be accessed or read successfully.
     * @throws IllegalArgumentException if the file name doesn't match the specified format.
     */
    @Nonnull public static CaptionMap loadSingle(@Nonnull final Path file) throws IOException {
        final String fileName = file.getFileName().toString();
        final Matcher matcher = FILE_NAME_PATTERN.matcher(fileName);
        final Locale locale;
        if (matcher.matches()) {
            locale = Locale.forLanguageTag(matcher.group(1));
        } else {
            throw new IllegalArgumentException(fileName + " is an invalid message file (cannot extract locale)");
        }
        try (final BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            Map<String, String> map = loadFromReader(reader);
            if (patch(map, locale)) {
                save(file, map); // update the file using the modified map
            }
            return new LocalizedCaptionMap(locale, map.entrySet().stream()
                    .collect(Collectors.toMap(entry -> TranslatableCaption.of(entry.getKey()), Map.Entry::getValue)));
        }
    }

    @SuppressWarnings("UnstableApiUsage")
    private static Map<String, String> loadFromReader(final Reader reader) {
        final Type type = new TypeToken<Map<String, String>>() {}.getType();
        return new LinkedHashMap<>(GSON.fromJson(reader, type));
    }

    private static Map<String, String> loadResource(final Locale locale) {
        final String url = String.format("lang/messages_%s.json", locale.toString());
        try {
            final InputStream stream = CaptionLoader.class.getClassLoader().getResourceAsStream(url);
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

    private static void save(final Path file, final Map<String, String> content) {
        try (final BufferedWriter writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            GSON.toJson(content, writer);
            logger.info("Saved {} with new content", file.getFileName());
        } catch (final IOException e) {
            logger.error("Failed to save caption file '{}'", file.getFileName().toString(), e);
        }
    }

    /**
     * Add missing entries to the given map.
     * Entries are missing if the key exists in {@link #DEFAULT_MESSAGES} but isn't present
     * in the given map. For a missing key, a value will be loaded either from
     * the resource matching the given locale or from {@link #DEFAULT_MESSAGES} if
     * no matching resource was found or the key isn't present in the resource.
     *
     * @param map    the map to patch
     * @param locale the locale to get the resource from
     * @return {@code true} if the map was patched.
     */
    private static boolean patch(final Map<String, String> map, final Locale locale) {
        boolean modified = false;
        Map<String, String> languageSpecific;
        if (locale.equals(DEFAULT_LOCALE)) {
            languageSpecific = DEFAULT_MESSAGES;
        } else {
            languageSpecific = loadResource(locale);
            if (languageSpecific == null) { // fallback for languages not provided by PlotSquared
                languageSpecific = DEFAULT_MESSAGES;
            }
        }
        for (Map.Entry<String, String> entry : DEFAULT_MESSAGES.entrySet()) {
            if (!map.containsKey(entry.getKey())) {
                final String value = languageSpecific.getOrDefault(entry.getKey(), entry.getValue());
                map.put(entry.getKey(), value);
                modified = true;
            }
        }
        return modified;
    }
}
