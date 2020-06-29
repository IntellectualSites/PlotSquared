package com.plotsquared.core.configuration.caption;

import com.plotsquared.core.configuration.Caption;
import com.plotsquared.core.configuration.Captions;
import com.plotsquared.core.player.PlotPlayer;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Deprecated
public class LegacyCaptionMap implements CaptionMap {
    private final CaptionMap parent;
    private final Map<String, Caption> legacyMap;

    public LegacyCaptionMap(CaptionMap parent) {
        this.parent = parent;
        this.legacyMap = new HashMap<>();
        // add here custom mappings
    }

    @Override
    public String getMessage(TranslatableCaption caption) {
        Caption legacy = this.legacyMap.computeIfAbsent(caption.getKey(), key -> {
            String captionsName = key.substring(key.indexOf('.') + 1).toUpperCase(Locale.ROOT);
            try {
                return Captions.valueOf(captionsName);
            } catch (IllegalArgumentException ignored) {
                return null;
            }
        });
        if (legacy == null) {
            return this.parent.getMessage(caption);
        }
        return legacy.getTranslated();
    }

    @Override
    public String getMessage(TranslatableCaption caption, PlotPlayer<?> context) {
        return getMessage(caption); // Captions does not allow per player locale
    }

    @Override
    public boolean supportsLocale(Locale locale) {
        return false;
    }

    @Override
    public Locale getLocale() {
        return Locale.ROOT;
    }
}
