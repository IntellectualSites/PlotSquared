package com.plotsquared.collection;

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
