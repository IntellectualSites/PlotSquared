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
 *                  Copyright (C) 2021 IntellectualSites
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
package com.plotsquared.core.configuration.caption;

import net.kyori.adventure.text.minimessage.placeholder.Placeholder;

public class CaptionHolder {

    private Caption caption = StaticCaption.miniMessage("");
    private Placeholder<?>[] placeholders = new Placeholder[0];

    public void set(Caption caption) {
        this.caption = caption;
    }

    /**
     * @return a {@link Caption} from a {@link StaticCaption}
     *
     * @deprecated use {@link #caption()} instead
     */
    @Deprecated(forRemoval = true, since = "6.3.0")
    public Caption get() {
        return this.caption;
    }

    /**
     * @return a {@link Caption} from a {@link StaticCaption}
     * @since 6.3.0
     */
    public Caption caption() {
        return this.caption;
    }

    /**
     * @return an array of {@link net.kyori.adventure.text.minimessage.placeholder.Placeholder}s
     * @deprecated use {@link #placeholders()} instead
     */
    @Deprecated(forRemoval = true, since = "6.3.0")
    public Placeholder<?>[] getTemplates() {
        return this.placeholders;
    }

    /**
     * @return an array of {@link net.kyori.adventure.text.minimessage.placeholder.Placeholder}s
     * @since 6.3.0
     */
    public Placeholder<?>[] placeholders() {
        return this.placeholders;
    }

    /**
     * @param placeholders placeholders
     *
     * @deprecated use {@link #parsePlaceholders(Placeholder...)} instead
     */
    @Deprecated(forRemoval = true, since = "6.3.0")
    public void setTemplates(Placeholder<?>... placeholders) {
        this.placeholders = placeholders;
    }

    /**
     * @param placeholders placeholders
     * @since 6.3.0
     */
    public void parsePlaceholders(Placeholder<?>... placeholders) {
        this.placeholders = placeholders;
    }

}
