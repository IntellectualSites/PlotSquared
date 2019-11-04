package com.github.intellectualsites.plotsquared.plot.object.collection;

import com.github.intellectualsites.plotsquared.plot.util.block.BlockUtil;

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
        if (weight <= 0)
            return;
        total += weight;
        map.put(total, result);
    }

    public E next() {
        return map.ceilingEntry(random.nextDouble()).getValue();
    }
}
