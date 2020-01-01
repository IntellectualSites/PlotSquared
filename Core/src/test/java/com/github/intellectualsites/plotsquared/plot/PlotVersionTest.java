package com.github.intellectualsites.plotsquared.plot;

import org.junit.Test;

public class PlotVersionTest {

    @Test public void tryParse() {
        //These are all random values chosen to form the test class.
        PlotVersion version = new PlotVersion("4.340", "f06903f", "19.08.05");
        System.out.println(version.build);

    }
}
