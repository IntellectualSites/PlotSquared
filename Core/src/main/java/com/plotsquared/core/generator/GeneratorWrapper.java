package com.plotsquared.core.generator;

import com.plotsquared.core.plot.PlotArea;

public interface GeneratorWrapper<T> {

    IndependentPlotGenerator getPlotGenerator();

    T getPlatformGenerator();

    void augment(PlotArea area);

    boolean isFull();

    @Override String toString();

    @Override boolean equals(Object obj);
}
