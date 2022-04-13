/*
 *       _____  _       _    _____                                _
 *      |  __ \| |     | |  / ____|                              | |
 *      | |__) | | ___ | |_| (___   __ _ _   _  __ _ _ __ ___  __| |
 *      |  ___/| |/ _ \| __|\___ \ / _` | | | |/ _` | '__/ _ \/ _` |
 *      | |    | | (_) | |_ ____) | (_| | |_| | (_| | | |  __/ (_| |
 *      |_|    |_|\___/ \__|_____/ \__, |\__,_|\__,_|_|  \___|\__,_|
 *                                    | |
 *                                    |_|
 *            PlotSquared plot management system for Minecraft
 *               Copyright (C) 2014 - 2022 IntellectualSites
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.plotsquared.core.plot;

import javax.annotation.Nullable;
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

}
