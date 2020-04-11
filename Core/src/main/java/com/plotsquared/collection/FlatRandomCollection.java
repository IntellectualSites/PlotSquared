package com.plotsquared.collection;

import com.plotsquared.util.MathMan;

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
