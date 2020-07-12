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
package com.plotsquared.core.configuration;

import com.google.common.base.Preconditions;
import com.plotsquared.core.configuration.caption.LocaleHolder;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

@RequiredArgsConstructor
public final class StaticCaption implements Caption {

    private final String value;
    private final boolean usePrefix;

    /**
     * @deprecated Use {@link #of(String)}
     */
    @Deprecated public StaticCaption(final String value) {
        this(value, true);
    }

    /**
     * Create a new static caption from the given text
     *
     * @param text Text
     * @return Created caption
     */
    @NotNull public static StaticCaption of(@NotNull final String text) {
        return new StaticCaption(Preconditions.checkNotNull(text, "Text may not be null"));
    }

    @Override
    public @NotNull String getComponent(@NotNull LocaleHolder localeHolder) {
        return this.value; // can't be translated
    }
}
