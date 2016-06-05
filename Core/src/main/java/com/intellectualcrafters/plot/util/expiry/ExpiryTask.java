package com.intellectualcrafters.plot.util.expiry;

import com.intellectualcrafters.plot.config.Settings;
import com.intellectualcrafters.plot.object.PlotArea;
import java.util.concurrent.TimeUnit;

public class ExpiryTask {
    private final Settings.AUTO_CLEAR settings;

    public ExpiryTask(Settings.AUTO_CLEAR settings) {
        this.settings = settings;
    }

    public boolean applies(PlotArea area) {
        return settings.WORLDS.contains(area.toString()) || settings.WORLDS.contains(area.worldname) || settings.WORLDS.contains("*");
    }

    public boolean applies(long diff) {
        return diff > TimeUnit.DAYS.toMillis(settings.DAYS);
    }

    public boolean needsAnalysis() {
        return settings.THRESHOLD > 0;
    }

    public boolean applies(PlotAnalysis analysis) {
        return analysis.getComplexity(settings) <= settings.THRESHOLD;
    }

    public boolean requiresConfirmation() {
        return settings.CONFIRMATION;
    }
}
