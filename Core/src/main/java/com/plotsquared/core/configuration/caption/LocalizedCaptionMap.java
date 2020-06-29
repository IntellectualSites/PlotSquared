package com.plotsquared.core.configuration.caption;

import com.plotsquared.core.player.PlotPlayer;

import java.util.Locale;
import java.util.Map;

public class LocalizedCaptionMap implements CaptionMap {
    private final Locale locale;
    private final Map<TranslatableCaption, String> captions;

    public LocalizedCaptionMap(Locale locale, Map<TranslatableCaption, String> captions) {
        this.locale = locale;
        this.captions = captions;
    }

    @Override public String getMessage(TranslatableCaption caption) {
        return this.captions.get(caption);
    }

    @Override public String getMessage(TranslatableCaption caption, PlotPlayer<?> context) {
        return getMessage(caption); // use the translation of this locale
    }

    @Override public boolean supportsLocale(Locale locale) {
        return this.locale.equals(locale);
    }

    @Override public Locale getLocale() {
        return this.locale;
    }
}
