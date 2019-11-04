package com.github.intellectualsites.plotsquared.plot.util;

import com.github.intellectualsites.plotsquared.plot.database.AbstractDBTest;
import com.github.intellectualsites.plotsquared.plot.database.DBFunc;
import com.github.intellectualsites.plotsquared.plot.object.RunnableVal;
import com.github.intellectualsites.plotsquared.plot.uuid.UUIDWrapper;
import org.junit.Before;

import java.util.UUID;

public class UUIDHandlerImplementationTest extends UUIDHandlerImplementation {

    public UUIDHandlerImplementationTest(UUIDWrapper wrapper) {
        super(wrapper);
    }

    @Before public void setUp() throws Exception {
        EventUtil.manager = new EventUtilTest();
        DBFunc.dbManager = new AbstractDBTest();
    }

    @Override public void fetchUUID(String name, RunnableVal<UUID> ifFetch) {

    }
}
