package com.plotsquared.core.configuration.caption;

import com.plotsquared.core.player.PlotPlayer;

import java.util.Collections;
import java.util.Locale;
import java.util.Map;

public class PerUserLocaleCaptionMap extends LocalizedCaptionMap {
    private final Map<Locale, CaptionMap> localeMap;

    public PerUserLocaleCaptionMap(Map<Locale, CaptionMap> localeMap) {
        super(Locale.ROOT, Collections.emptyMap());
        this.localeMap = localeMap;
    }

    @Override
    public String getMessage(TranslatableCaption caption, PlotPlayer<?> context) {
        return this.localeMap.get(context.getLocale()).getMessage(caption);
    }

    @Override
    public boolean supportsLocale(Locale locale) {
        return this.localeMap.containsKey(locale);
    }
}
