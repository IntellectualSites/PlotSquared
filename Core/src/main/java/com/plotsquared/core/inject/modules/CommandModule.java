package com.plotsquared.core.inject.modules;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.google.inject.multibindings.Multibinder;
import com.plotsquared.core.commands.PlotSquaredCommandBean;
import com.plotsquared.core.commands.command.setting.flag.FlagSetCommand;

public final class CommandModule extends AbstractModule {

    @Override
    protected void configure() {
        final Multibinder<PlotSquaredCommandBean> commands = Multibinder.newSetBinder(
                this.binder(),
                PlotSquaredCommandBean.class
        );

        commands.addBinding().to(FlagSetCommand.class).in(Scopes.SINGLETON);
    }
}
