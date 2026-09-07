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
package com.plotsquared.core.util.comparator;

import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.Rating;

import java.util.Comparator;
import java.util.Map;
import java.util.UUID;

public class PlotByRatingComparator implements Comparator<Plot> {

    public static final PlotByRatingComparator INSTANCE = new PlotByRatingComparator();

    PlotByRatingComparator() {
    }

    @Override
    public int compare(final Plot p1, final Plot p2) {
        double v1 = 0;
        int p1s = p1.getSettings().getRatings().size();
        int p2s = p2.getRatings().size();
        if (!p1.getSettings().getRatings().isEmpty()) {
            v1 = p1.getRatings().values().stream().mapToDouble(Rating::getAverageRating)
                    .map(av -> av * av).sum();
            v1 /= p1s;
            v1 += p1s;
        }
        double v2 = 0;
        if (!p2.getSettings().getRatings().isEmpty()) {
            for (Map.Entry<UUID, Rating> entry : p2.getRatings().entrySet()) {
                double av = entry.getValue().getAverageRating();
                v2 += av * av;
            }
            v2 /= p2s;
            v2 += p2s;
        }
        if (v2 == v1 && v2 != 0) {
            return p2s - p1s;
        }
        return (int) Math.signum(v2 - v1);
    }

}
