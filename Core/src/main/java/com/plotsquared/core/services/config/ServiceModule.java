package com.plotsquared.core.services.config;

import com.google.inject.AbstractModule;
import com.plotsquared.core.services.api.PlayerMetaService;
import com.plotsquared.core.services.impl.PlayerMetaDefaultService;

public class ServiceModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(PlayerMetaService.class).to(PlayerMetaDefaultService.class);
    }

}
