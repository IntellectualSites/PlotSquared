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
 *                  Copyright (C) 2020 IntellectualSites
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
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.plotsquared.core.configuration.caption;

import com.google.common.base.Preconditions;

import javax.annotation.Nonnull;

public final class StaticCaption implements Caption {

    private final String value;

    private StaticCaption(final String value) {
        this.value = value;
    }

    /**
     * Create a new static caption from the given text
     *
     * @param text Text
     * @return Created caption
     */
    @Nonnull public static StaticCaption of(@Nonnull final String text) {
        return new StaticCaption(Preconditions.checkNotNull(text, "Text may not be null"));
    }

    @Override
    public @Nonnull String getComponent(@Nonnull LocaleHolder localeHolder) {
        return this.value; // can't be translated
    }
}
