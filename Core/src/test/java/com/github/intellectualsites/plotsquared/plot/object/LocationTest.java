package com.github.intellectualsites.plotsquared.plot.object;

import org.junit.Test;

import java.util.logging.Logger;

public class LocationTest {

    private static final Logger logger = Logger.getLogger(LocationTest.class.getName());

    @Test public void cloning() {
        String world = "plotworld";
        Location location1 = new Location(world, 0, 0, 0);
        logger.info(location1.toString());
        Location clone = location1.clone();
        world = "normal";
        logger.info(clone.toString());
        location1.getBlockVector3()

    }
}
