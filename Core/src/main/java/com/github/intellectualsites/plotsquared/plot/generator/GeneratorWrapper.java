package com.github.intellectualsites.plotsquared.plot.generator;

import com.github.intellectualsites.plotsquared.plot.util.block.BlockUtil;

import com.github.intellectualsites.plotsquared.plot.object.PlotArea;

public interface GeneratorWrapper<T> {

    IndependentPlotGenerator getPlotGenerator();

    T getPlatformGenerator();

    void augment(PlotArea area);

    boolean isFull();

    @Override String toString();

    @Override boolean equals(Object obj);
}
