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
package com.plotsquared.core.plot.expiration;

import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.configuration.Settings;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.PlotArea;
import com.plotsquared.core.plot.world.PlotAreaManager;
import com.plotsquared.core.util.query.PlotQuery;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class ExpiryTask {

    private final Settings.Auto_Clear settings;
    private final PlotAreaManager plotAreaManager;
    private long cutoffThreshold = Long.MIN_VALUE;

    public ExpiryTask(final Settings.Auto_Clear settings, final @NonNull PlotAreaManager plotAreaManager) {
        this.settings = settings;
        this.plotAreaManager = plotAreaManager;
    }

    public Settings.Auto_Clear getSettings() {
        return settings;
    }

    public boolean allowsArea(PlotArea area) {
        return settings.WORLDS.contains(area.toString()) || settings.WORLDS
                .contains(area.getWorldName()) || settings.WORLDS.contains("*");
    }

    public boolean applies(PlotArea area) {
        if (allowsArea(area)) {
            if (settings.REQUIRED_PLOTS <= 0) {
                return true;
            }
            Set<Plot> plots = null;
            if (cutoffThreshold != Long.MAX_VALUE
                    && area.getPlots().size() > settings.REQUIRED_PLOTS
                    || (plots = getPlotsToCheck()).size() > settings.REQUIRED_PLOTS) {
                // calculate cutoff
                if (cutoffThreshold == Long.MIN_VALUE) {
                    plots = plots != null ? plots : getPlotsToCheck();
                    int diff = settings.REQUIRED_PLOTS;
                    boolean min = true;
                    if (plots.size() > settings.REQUIRED_PLOTS) {
                        min = false;
                        diff = plots.size() - settings.REQUIRED_PLOTS;
                    }
                    ExpireManager expireManager = PlotSquared.platform().expireManager();
                    List<Long> entireList =
                            plots.stream().map(plot -> expireManager.getAge(plot, settings.DELETE_IF_OWNER_IS_UNKNOWN))
                                    .collect(Collectors.toList());
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
        final Collection<PlotArea> areas = new LinkedList<>();
        for (final PlotArea plotArea : this.plotAreaManager.getAllPlotAreas()) {
            if (this.allowsArea(plotArea)) {
                areas.add(plotArea);
            }
        }
        return PlotQuery.newQuery().inAreas(areas).asSet();
    }

    public boolean applies(long diff) {
        return diff > TimeUnit.DAYS.toMillis(settings.DAYS) && diff > cutoffThreshold;
    }

    public boolean appliesAccountAge(long accountAge) {
        if (settings.SKIP_ACCOUNT_AGE_DAYS != -1) {
            return accountAge <= TimeUnit.DAYS.toMillis(settings.SKIP_ACCOUNT_AGE_DAYS);
        }
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

    /**
     * Returns {@code true} if this task respects unknown owners
     *
     * @return {@code true} if unknown owners should be counted as never online
     * @since 6.4.0
     */
    public boolean shouldDeleteForUnknownOwner() {
        return settings.DELETE_IF_OWNER_IS_UNKNOWN;
    }

}
