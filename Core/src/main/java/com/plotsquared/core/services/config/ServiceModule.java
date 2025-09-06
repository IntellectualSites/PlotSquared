package com.plotsquared.core.services.config;

import com.google.inject.AbstractModule;
import com.plotsquared.core.services.api.PlayerMetaService;
import com.plotsquared.core.services.api.PlotService;
import com.plotsquared.core.services.impl.PlayerMetaDefaultService;
import com.plotsquared.core.services.impl.PlotDefaultService;

public class ServiceModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(PlayerMetaService.class).to(PlayerMetaDefaultService.class);
        bind(PlotService.class).to(PlotDefaultService.class);
    }

}
