/*
 * PlotSquared, a land and world management plugin for Minecraft.
 * Copyright (C) IntellectualSites <https://intellectualsites.com>
 * Copyright (C) IntellectualSites team and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.plotsquared.core.inject.modules;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.google.inject.multibindings.Multibinder;
import com.plotsquared.core.commands.PlotSquaredCommandBean;
import com.plotsquared.core.commands.command.setting.flag.FlagAddCommand;
import com.plotsquared.core.commands.command.setting.flag.FlagInfoCommand;
import com.plotsquared.core.commands.command.setting.flag.FlagListCommand;
import com.plotsquared.core.commands.command.setting.flag.FlagRemoveCommand;
import com.plotsquared.core.commands.command.setting.flag.FlagSetCommand;

public final class CommandModule extends AbstractModule {

    @Override
    protected void configure() {
        final Multibinder<PlotSquaredCommandBean> commands = Multibinder.newSetBinder(
                this.binder(),
                PlotSquaredCommandBean.class
        );

        commands.addBinding().to(FlagAddCommand.class).in(Scopes.SINGLETON);
        commands.addBinding().to(FlagInfoCommand.class).in(Scopes.SINGLETON);
        commands.addBinding().to(FlagListCommand.class).in(Scopes.SINGLETON);
        commands.addBinding().to(FlagRemoveCommand.class).in(Scopes.SINGLETON);
        commands.addBinding().to(FlagSetCommand.class).in(Scopes.SINGLETON);
    }
}
