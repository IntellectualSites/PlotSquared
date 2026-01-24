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
package com.plotsquared.core.plot;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Objects;

public class PlotTitle {

    /**
     * @since 6.1.0
     */
    public static final PlotTitle CONFIGURED = new PlotTitle();

    private final String title;
    private final String subtitle;

    private PlotTitle() {
        title = null;
        subtitle = null;
    }

    /**
     * @since 6.0.10
     */
    public PlotTitle(String title, String subtitle) {
        Objects.requireNonNull(title);
        Objects.requireNonNull(subtitle);
        this.title = title;
        this.subtitle = subtitle;
    }

    /**
     * @since 6.0.10
     */
    @Nullable
    public String title() {
        return title;
    }

    /**
     * @since 6.0.10
     */
    @Nullable
    public String subtitle() {
        return subtitle;
    }

    /**
     * Provides a string representation of this plot title value (used in placeholders).
     *
     * @return the plot title representation in the format {@code "<title>" "<subtitle>"}
     * @since 7.5.5
     */
    @Override
    public String toString() {
        return "\"%s\" \"%s\"".formatted(
                this.title != null ? this.title : "",
                this.subtitle != null ? this.subtitle : ""
        );
    }

}
