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
import java.util.Random;

import static com.google.common.base.Preconditions.checkNotNull;

public abstract class RandomCollection<T> {

    protected Random random;

    public RandomCollection(Map<T, Double> weights, Random random) {
        this.random = random;
    }

    public static <T> RandomCollection<T> of(Map<T, Double> weights, Random random) {
        try {
            return new FlatRandomCollection<>(weights, random);
        } catch (IllegalArgumentException ignore) {
            return new SimpleRandomCollection<>(weights, random);
        }
    }

    public Random getRandom() {
        return random;
    }

    public void setRandom(Random random) {
        checkNotNull(random);
        this.random = random;
    }

    public abstract T next();

}
