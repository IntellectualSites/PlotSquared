package com.plotsquared.plot.util;

import com.plotsquared.database.AbstractDBTest;
import com.plotsquared.database.DBFunc;
import com.plotsquared.util.uuid.UUIDHandlerImplementation;
import com.plotsquared.util.tasks.RunnableVal;
import com.plotsquared.util.uuid.UUIDWrapper;
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
