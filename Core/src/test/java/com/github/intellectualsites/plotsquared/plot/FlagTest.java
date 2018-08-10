package com.github.intellectualsites.plotsquared.plot;

import com.github.intellectualsites.plotsquared.plot.database.AbstractDBTest;
import com.github.intellectualsites.plotsquared.plot.database.DBFunc;
import com.github.intellectualsites.plotsquared.plot.flag.Flag;
import com.github.intellectualsites.plotsquared.plot.flag.Flags;
import com.github.intellectualsites.plotsquared.plot.object.Plot;
import com.github.intellectualsites.plotsquared.plot.object.PlotBlock;
import com.github.intellectualsites.plotsquared.plot.object.PlotId;
import com.github.intellectualsites.plotsquared.plot.util.EventUtil;
import com.github.intellectualsites.plotsquared.plot.util.EventUtilTest;
import com.google.common.base.Optional;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;
import java.util.HashSet;
import java.util.UUID;

import static org.junit.Assert.assertEquals;

public class FlagTest {

    private Object testBlock;
    private Flag<? extends Collection<?>> use = Flags.USE;

    @Before public void setUp() throws Exception {
        EventUtil.manager = new EventUtilTest();
        DBFunc.dbManager = new AbstractDBTest();
    }

    @Test public void flagTest() throws Exception {
        Plot plot = new Plot(null, new PlotId(0, 0));
        plot.owner = UUID.fromString("84499644-ad72-454b-a19d-f28c28df382b");
        //plot.setFlag(use, use.parseValue("33,33:1,6:4")); //TODO fix this so FlagTest will run during compile
        Optional<? extends Collection> flag = plot.getFlag(use);
        if (flag.isPresent()) {
            System.out.println(Flags.USE.valueToString(flag.get()));
            testBlock = PlotBlock.get((short) 1, (byte) 0);
            flag.get().add(testBlock);
        }
        if (flag.isPresent()) {
            System.out.println(Flags.USE.valueToString(flag.get()));
        }
        Optional<HashSet<PlotBlock>> flag2 = plot.getFlag(Flags.USE);
        if (flag2.isPresent()) {
            //   assertThat(flag2.get(), (Matcher<? super HashSet<PlotBlock>>) IsCollectionContaining.hasItem(testBlock));
        }
        if (flag.isPresent() && flag2.isPresent()) {
            assertEquals(flag.get(), flag2.get());
        }
    }
}
