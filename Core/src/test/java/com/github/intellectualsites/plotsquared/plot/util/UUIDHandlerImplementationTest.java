package com.github.intellectualsites.plotsquared.plot.util;

import com.github.intellectualsites.plotsquared.database.AbstractDBTest;
import com.github.intellectualsites.plotsquared.database.DBFunc;
import com.github.intellectualsites.plotsquared.util.uuid.UUIDHandlerImplementation;
import com.github.intellectualsites.plotsquared.util.tasks.RunnableVal;
import com.github.intellectualsites.plotsquared.util.uuid.UUIDWrapper;
import org.junit.Before;

import java.util.UUID;

public class UUIDHandlerImplementationTest extends UUIDHandlerImplementation {

    public UUIDHandlerImplementationTest(UUIDWrapper wrapper) {
        super(wrapper);
    }

    @Before public void setUp() throws Exception {
        DBFunc.dbManager = new AbstractDBTest();
    }

    @Override public void fetchUUID(String name, RunnableVal<UUID> ifFetch) {

    }
}
