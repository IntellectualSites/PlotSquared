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
package com.plotsquared.core.plot;

import com.plotsquared.core.configuration.Settings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.IntStream;

public class Rating {

    private static final String LIKE_INTERNAL = "__LIKES__";

    /**
     * This is a map of the rating category to the rating value
     */
    private final HashMap<String, Integer> ratingMap;
    private final int initial;
    private boolean changed;

    public Rating(int value) {
        this.ratingMap = new HashMap<>();
        if (Settings.Ratings.USE_LIKES) {
            this.initial = value == 10 ? 10 : 1;
            this.ratingMap.put(LIKE_INTERNAL, this.initial == 10 ? 10 : 1);
        } else {
            this.initial = value;
            if (Settings.Ratings.CATEGORIES != null && Settings.Ratings.CATEGORIES.size() > 1) {
                if (value < 10) {
                    for (String ratingCategory : Settings.Ratings.CATEGORIES) {
                        this.ratingMap.put(ratingCategory, value);
                    }
                    this.changed = true;
                    return;
                }
                for (String ratingCategory : Settings.Ratings.CATEGORIES) {
                    this.ratingMap.put(ratingCategory, value % 10 - 1);
                    value = value / 10;
                }
            } else {
                this.ratingMap.put(null, value);
            }
        }
    }

    public List<String> getCategories() {
        if (this.ratingMap.size() == 1) {
            return new ArrayList<>(0);
        }
        return new ArrayList<>(this.ratingMap.keySet());
    }

    public double getAverageRating() {
        if (Settings.Ratings.USE_LIKES) {
            return getLike() ? 10 : 1;
        }
        double total = this.ratingMap.values().stream().mapToDouble(v -> v).sum();
        return total / this.ratingMap.size();
    }

    public boolean getLike() {
        final Integer rating = this.getRating(LIKE_INTERNAL);
        return rating != null && rating == 10;
    }

    public Integer getRating(String category) {
        return this.ratingMap.get(category);
    }

    public boolean setRating(String category, int value) {
        this.changed = true;
        if (!this.ratingMap.containsKey(category)) {
            return false;
        }
        return this.ratingMap.put(category, value) != null;
    }

    public int getAggregate() {
        if (!this.changed) {
            return this.initial;
        }
        if (Settings.Ratings.USE_LIKES) {
            return this.ratingMap.get(LIKE_INTERNAL);
        }
        if (Settings.Ratings.CATEGORIES != null && Settings.Ratings.CATEGORIES.size() > 1) {
            return IntStream.range(0, Settings.Ratings.CATEGORIES.size()).map(
                    i -> (int) ((i + 1) * Math
                            .pow(10, this.ratingMap.get(Settings.Ratings.CATEGORIES.get(i))))).sum();
        } else {
            return this.ratingMap.get(null);
        }

    }

}
