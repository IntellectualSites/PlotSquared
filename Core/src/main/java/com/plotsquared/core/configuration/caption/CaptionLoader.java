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
 *                  Copyright (C) ${year} IntellectualSites
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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class CaptionLoader {

    private static final Gson GSON = new GsonBuilder().create();
    private static final Pattern FILE_NAME_PATTERN = Pattern.compile("messages_(.*)\\.json");

    private CaptionLoader() {
    }

    public static CaptionMap loadAll(@Nonnull final Path directory) throws IOException {
        final Map<Locale, CaptionMap> localeMaps = new HashMap<>();
        try (Stream<Path> files = Files.list(directory)) {
            List<Path> captionFiles = files.filter(Files::isRegularFile).collect(Collectors.toList());
            for (Path file : captionFiles) {
                CaptionMap localeMap = loadSingle(file);
                localeMaps.put(localeMap.getLocale(), localeMap);
            }
            return new PerUserLocaleCaptionMap(localeMaps);
        }
    }

    public static CaptionMap loadSingle(@Nonnull final Path file) throws IOException {
        final String fileName = file.getFileName().toString();
        final Matcher matcher = FILE_NAME_PATTERN.matcher(fileName);
        final Locale locale;
        if (matcher.matches()) {
            locale = Locale.forLanguageTag(matcher.group(1));
        } else {
            throw new IllegalArgumentException(fileName + " is an invalid message file (cannot extract locale)");
        }
        JsonObject object = GSON.fromJson(
                Files.newBufferedReader(file, StandardCharsets.UTF_16),
                JsonObject.class);
        Map<TranslatableCaption, String> captions = new HashMap<>();
        for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
            TranslatableCaption key = TranslatableCaption.of(entry.getKey());
            captions.put(key, entry.getValue().getAsString());
        }
        return new LocalizedCaptionMap(locale, captions);
    }

}
