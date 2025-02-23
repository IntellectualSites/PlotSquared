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
