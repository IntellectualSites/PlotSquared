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
package com.plotsquared.core.collection;

import java.util.Map;
import java.util.NavigableMap;
import java.util.Random;
import java.util.TreeMap;

public class SimpleRandomCollection<E> extends RandomCollection<E> {

    private final NavigableMap<Double, E> map = new TreeMap<>();
    private double total = 0;

    public SimpleRandomCollection(Map<E, Double> weights, Random random) {
        super(weights, random);
        for (Map.Entry<E, Double> entry : weights.entrySet()) {
            add(entry.getValue(), entry.getKey());
        }
    }

    public void add(double weight, E result) {
        if (weight <= 0) {
            return;
        }
        total += weight;
        map.put(total, result);
    }

    public E next() {
        return map.ceilingEntry(random.nextDouble()).getValue();
    }

}
