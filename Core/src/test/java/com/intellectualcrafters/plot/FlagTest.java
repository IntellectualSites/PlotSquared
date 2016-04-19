package com.intellectualcrafters.plot;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import com.google.common.base.Optional;
import com.intellectualcrafters.plot.database.AbstractDBTEst;
import com.intellectualcrafters.plot.database.DBFunc;
import com.intellectualcrafters.plot.flag.Flags;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotBlock;
import com.intellectualcrafters.plot.object.PlotId;
import com.intellectualcrafters.plot.util.EventUtil;
import com.intellectualcrafters.plot.util.EventUtilTest;
import org.hamcrest.core.IsCollectionContaining;
import org.junit.Before;
import org.junit.Test;

import java.util.HashSet;
import java.util.UUID;

public class FlagTest {

    private PlotBlock testBlock;

    @Before
    public void setUp() throws Exception {
        EventUtil.manager = new EventUtilTest();
        DBFunc.dbManager = new AbstractDBTEst();
    }

    @Test
    public void flagTest() throws Exception {
        Plot plot = new Plot(null, new PlotId(0, 0));
        plot.owner = UUID.fromString("84499644-ad72-454b-a19d-f28c28df382b");
        Flags.USE.parseValue("33,33:1,6:4");
        plot.setFlag(Flags.USE, Flags.USE.parseValue("33,33:1,6:4"));
        Optional<HashSet<PlotBlock>> flag = plot.getFlag(Flags.USE);
        if (flag.isPresent()) {
            System.out.println(Flags.USE.valueToString(flag.get()));
        }
        testBlock = new PlotBlock((short) 1, (byte) 0);
        flag.get().add(testBlock);
        if (flag.isPresent()) {
            System.out.println(Flags.USE.valueToString(flag.get()));
        }
        Optional<HashSet<PlotBlock>> flag2 = plot.getFlag(Flags.USE);
        if (flag2.isPresent()) {
            assertThat(flag2.get(), IsCollectionContaining.hasItem(testBlock));
        }
        if (flag.isPresent() && flag2.isPresent()) {
            assertEquals(flag.get(), flag2.get());
        }
    }
}
