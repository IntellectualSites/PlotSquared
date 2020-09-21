/*
 *       _____  _       _    _____                                _
 *      |  __ \| |     | |  / ____|                              | |
 *      | |__) | | ___ | |_| (___   __ _ _   _  __ _ _ __ ___  __| |
 *      |  ___/| |/ _ \| __|\___ \ / _` | | | |/ _` | '__/ _ \/ _` |
 *      | |    | | (_) | |_ ____) | (_| | |_| | (_| | | |  __/ (_| |
 *      |_|    |_|\___/ \__|_____/ \__, |\__,_|\__,_|_|  \___|\__,_|
 *                                    | |
 *                                    |_|
 *            PlotSquared plot management system for Minecraft
 *                  Copyright (C) 2020 IntellectualSites
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.plotsquared.core.collection;

import com.plotsquared.core.util.MathMan;

import java.util.ArrayList;
import java.util.Map;
import java.util.Random;

public class FlatRandomCollection<T> extends RandomCollection<T> {
    private T[] values;

    public FlatRandomCollection(Map<T, Double> weights, Random random) {
        super(weights, random);
        int max = 0;
        int[] counts = new int[weights.size()];
        Double[] weightDoubles = weights.values().toArray(new Double[0]);
        for (int i = 0; i < weightDoubles.length; i++) {
            int weight = (int) (weightDoubles[i] * 100);
            counts[i] = weight;
            if (weight != (weightDoubles[i] * 100)) {
                throw new IllegalArgumentException("Too small");
            }
            if (weight > max) {
                max = weight;
            }
        }
        int gcd = MathMan.gcd(counts);
        if (max / gcd > 100000) {
            throw new IllegalArgumentException("Too large");
        }
        ArrayList<T> parsed = new ArrayList<>();
        for (Map.Entry<T, Double> entry : weights.entrySet()) {
            int num = (int) (100 * entry.getValue());
            for (int j = 0; j < num / gcd; j++) {
                parsed.add(entry.getKey());
            }
        }
        this.values = (T[]) parsed.toArray();
    }

    @Override public T next() {
        return values[random.nextInt(values.length)];
    }
}
