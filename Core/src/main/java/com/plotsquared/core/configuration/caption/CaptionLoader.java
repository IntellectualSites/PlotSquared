package com.plotsquared.core.configuration.caption;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;

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

    public static CaptionMap loadAll(@NotNull final Path directory) throws IOException {
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

    public static CaptionMap loadSingle(@NotNull final Path file) throws IOException {
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
