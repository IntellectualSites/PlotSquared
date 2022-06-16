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
package com.plotsquared.core.events;

import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.PlotId;
import com.sk89q.worldedit.function.pattern.Pattern;

/**
 * Called when a plot component is set
 */
public class PlotComponentSetEvent extends PlotEvent {

    private String component;
    private Pattern pattern;

    /**
     * PlotComponentSetEvent: Called when a player attempts to set the component of a plot (e.g. walls)
     *
     * @param plot      The plot having its component set
     * @param component The component being set
     * @param pattern   The pattern the component is being set to
     */
    public PlotComponentSetEvent(Plot plot, String component, Pattern pattern) {
        super(plot);
        this.component = component;
        this.pattern = pattern;
    }

    /**
     * Get the PlotId
     *
     * @return PlotId
     */
    public PlotId getPlotId() {
        return getPlot().getId();
    }

    /**
     * Get the world name
     *
     * @return String
     */
    public String getWorld() {
        return getPlot().getWorldName();
    }

    /**
     * Get the component which was set
     *
     * @return Component name
     */
    public String getComponent() {
        return this.component;
    }

    /**
     * Change the component being set
     *
     * @param component the component to set
     */
    public void setComponent(String component) {
        this.component = component;
    }

    /**
     * Get the pattern being set
     *
     * @return Pattern
     */
    public Pattern getPattern() {
        return this.pattern;
    }

    /**
     * Change the pattern being set
     *
     * @param pattern the pattern to set
     */
    public void setPattern(Pattern pattern) {
        this.pattern = pattern;
    }

}
