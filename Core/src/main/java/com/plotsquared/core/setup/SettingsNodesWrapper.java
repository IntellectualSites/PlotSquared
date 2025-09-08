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
package com.plotsquared.core.setup;

import com.plotsquared.core.configuration.ConfigurationNode;

/**
 * This class wraps an array of {@link ConfigurationNode}s.
 */
public record SettingsNodesWrapper(
        ConfigurationNode[] settingsNodes,
        SetupStep afterwards
) {

    /**
     * Returns the first step of this wrapper or the step or the
     * {@code afterwards} step if no step is available.
     *
     * @return the first step or {@code afterwards}.
     */
    public SetupStep getFirstStep() {
        return this.settingsNodes.length == 0 ? this.afterwards : new SettingsNodeStep(this.settingsNodes[0], 0, this);
    }

}
