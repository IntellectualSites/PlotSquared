package com.plotsquared.core.configuration.caption;

import com.plotsquared.core.player.PlotPlayer;

import java.util.Locale;

public interface CaptionMap {

    String getMessage(TranslatableCaption caption);

    String getMessage(TranslatableCaption caption, PlotPlayer<?> context);

    boolean supportsLocale(Locale locale);

    Locale getLocale();
}
