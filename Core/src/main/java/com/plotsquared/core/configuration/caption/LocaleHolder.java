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
package com.plotsquared.core.configuration.caption;

import com.plotsquared.core.player.ConsolePlayer;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Locale;

/**
 * Any entity that has a locale
 */
public interface LocaleHolder {

    /**
     * Get the console locale holder.
     * <p>
     * You can use {@link ConsolePlayer#getConsole()} for direct access to the {@link ConsolePlayer}
     * </p>
     *
     * @return Console locale holder
     */
    @NonNull
    static LocaleHolder console() {
        return ConsolePlayer.getConsole();
    }

    /**
     * Get the locale used by the holder
     *
     * @return Locale
     */
    @NonNull Locale getLocale();

    /**
     * Set the locale for the holder
     *
     * @param locale New locale
     */
    void setLocale(@NonNull Locale locale);

}
