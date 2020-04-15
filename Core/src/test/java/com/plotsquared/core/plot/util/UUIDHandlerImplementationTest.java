package com.plotsquared.core.plot.util;

import com.plotsquared.core.database.AbstractDBTest;
import com.plotsquared.core.database.DBFunc;
import com.plotsquared.core.util.uuid.UUIDHandlerImplementation;
import com.plotsquared.core.util.task.RunnableVal;
import com.plotsquared.core.util.uuid.UUIDWrapper;
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
