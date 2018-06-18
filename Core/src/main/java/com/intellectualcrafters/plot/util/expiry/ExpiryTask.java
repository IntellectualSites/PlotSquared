package com.intellectualcrafters.plot.util.expiry;

import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.config.Settings;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotArea;
import com.intellectualcrafters.plot.object.PlotFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class ExpiryTask {
    private final Settings.Auto_Clear settings;

    public ExpiryTask(Settings.Auto_Clear settings) {
        this.settings = settings;
    }

    public Settings.Auto_Clear getSettings() {
        return settings;
    }

    public boolean allowsArea(PlotArea area) {
        return settings.WORLDS.contains(area.toString()) || settings.WORLDS.contains(area.worldname) || settings.WORLDS.contains("*");
    }

    public boolean applies(PlotArea area) {
        if (allowsArea(area)) {
            if (settings.REQUIRED_PLOTS <= 0) {
                return true;
            }
            Set<Plot> plots = null;
            if (cutoffThreshold != Long.MAX_VALUE && area.getPlots().size() > settings.REQUIRED_PLOTS || (plots = getPlotsToCheck()).size() > settings.REQUIRED_PLOTS) {
                // calculate cutoff
                if (cutoffThreshold == Long.MIN_VALUE) {
                    plots = plots != null ? plots : getPlotsToCheck();
                    int diff = settings.REQUIRED_PLOTS;
                    boolean min = true;
                    if (settings.REQUIRED_PLOTS - plots.size() < settings.REQUIRED_PLOTS) {
                        min = false;
                        diff = settings.REQUIRED_PLOTS - plots.size();
                    }
                    List<Long> entireList = new ArrayList<>();
                    for (Plot plot : plots) {
                        entireList.add(ExpireManager.IMP.getAge(plot));
                    }
                    List<Long> top = new ArrayList<>(diff + 1);
                    if (diff > 1000) {
                        Collections.sort(entireList);
                        cutoffThreshold = entireList.get(settings.REQUIRED_PLOTS);
                    } else {
                        loop:
                        for (long num : entireList) {
                            int size = top.size();
                            if (size == 0) {
                                top.add(num);
                                continue;
                            }
                            long end = top.get(size - 1);
                            if (min ? num < end : num > end) {
                                for (int i = 0; i < size; i++) {
                                    long existing = top.get(i);
                                    if (min ? num < existing : num > existing) {
                                        top.add(i, num);
                                        if (size == diff) {
                                            top.remove(size);
                                        }
                                        continue loop;
                                    }
                                }
                            }
                            if (size < diff) {
                                top.add(num);
                            }
                        }
                        cutoffThreshold = top.get(top.size() - 1);
                    }
                    // Add half a day, as expiry is performed each day
                    cutoffThreshold += (TimeUnit.DAYS.toMillis(1) / 2);
                }
                return true;
            } else {
                cutoffThreshold = Long.MAX_VALUE;
            }
        }
        return false;
    }

    public Set<Plot> getPlotsToCheck() {
        return PS.get().getPlots(new PlotFilter() {
            @Override
            public boolean allowsArea(PlotArea area) {
                return ExpiryTask.this.allowsArea(area);
            }
        });
    }

    private long cutoffThreshold = Long.MIN_VALUE;

    public boolean applies(long diff) {
        return diff > TimeUnit.DAYS.toMillis(settings.DAYS) && diff > cutoffThreshold;
    }

    public boolean appliesAccountAge(long accountAge) {
        if (settings.SKIP_ACCOUNT_AGE_DAYS != -1)
            return accountAge <= TimeUnit.DAYS.toMillis(settings.SKIP_ACCOUNT_AGE_DAYS);
        return false;
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
