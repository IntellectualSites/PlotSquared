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
package com.plotsquared.core.components;

import com.plotsquared.core.command.CommandCategory;
import com.plotsquared.core.command.CommandDeclaration;
import com.plotsquared.core.command.RequiredType;
import com.plotsquared.core.command.SubCommand;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.PlotInventory;

@CommandDeclaration(command = "components",
        permission = "plots.components",
        description = "Open the component preset GUI",
        usage = "/plot components",
        category = CommandCategory.APPEARANCE,
        requiredType = RequiredType.PLAYER)
public class ComponentCommand extends SubCommand {

    private final ComponentPresetManager componentPresetManager;

    public ComponentCommand(final ComponentPresetManager componentPresetManager) {
        this.componentPresetManager = componentPresetManager;
    }

    @Override
    public boolean onCommand(final PlotPlayer<?> player, final String[] args) {
        final PlotInventory inventory = componentPresetManager.buildInventory(player);
        if (inventory != null) {
            inventory.openInventory();
        }
        return true;
    }

}
