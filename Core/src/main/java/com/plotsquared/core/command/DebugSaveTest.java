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
package com.plotsquared.core.command;

import com.plotsquared.core.configuration.caption.TranslatableCaption;
import com.plotsquared.core.database.DBFunc;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.util.query.PlotQuery;

import java.util.List;

@CommandDeclaration(command = "debugsavetest",
        permission = "plots.debugsavetest",
        category = CommandCategory.DEBUG,
        requiredType = RequiredType.CONSOLE,
        usage = "/plot debugsavetest")
public class DebugSaveTest extends SubCommand {

    @Override
    public boolean onCommand(final PlotPlayer<?> player, String[] args) {
        final List<Plot> plots = PlotQuery.newQuery().allPlots().asList();
        player.sendMessage(TranslatableCaption.of("debugsavetest.starting"));
        DBFunc.createPlotsAndData(
                plots,
                () -> player.sendMessage(TranslatableCaption.of("debugsavetest.done"))
        );
        return true;
    }

}
