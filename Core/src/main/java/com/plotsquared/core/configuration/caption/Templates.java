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

import com.plotsquared.core.plot.PlotArea;
import com.plotsquared.core.util.PlayerManager;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.placeholder.Placeholder;
import net.kyori.adventure.text.minimessage.placeholder.PlaceholderResolver;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.UUID;

/**
 * Utility class that generates {@link net.kyori.adventure.text.minimessage.placeholder.Placeholder templates}
 * @deprecated Use {@link Placeholders} instead
 */
@Deprecated(forRemoval = true, since = "6.3.0")
public final class Templates {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.builder().build();

    private Templates() {
        throw new UnsupportedOperationException(
                "This is a utility class and cannot be instantiated");
    }

    /**
     * Create a {@link net.kyori.adventure.text.minimessage.placeholder.Placeholder} from a PlotSquared {@link Caption}
     *
     * @param localeHolder Locale holder
     * @param key          Template key
     * @param caption      Caption object
     * @param replacements Replacements
     * @return Generated template
     *
     * @deprecated Use {@link Placeholders#miniMessage(LocaleHolder, String, Caption, Placeholder[])} instead
     */
    @Deprecated(forRemoval = true, since = "6.3.0")
    public static @NonNull Placeholder<?> of(
            final @NonNull LocaleHolder localeHolder,
            final @NonNull String key, final @NonNull Caption caption,
            final @NonNull Placeholder<?>... replacements
    ) {
        return net.kyori.adventure.text.minimessage.placeholder.Placeholder.component(key, MINI_MESSAGE.deserialize(caption.getComponent(localeHolder),
                PlaceholderResolver.placeholders(replacements)));
    }

    /**
     * Create a {@link net.kyori.adventure.text.minimessage.placeholder.Placeholder} from a username (using UUID mappings)
     *
     * @param key  Template key
     * @param uuid Player UUID
     * @return Generated template
     *
     * @deprecated Use {@link Placeholders#miniMessage(String, UUID)} instead
     */
    @Deprecated(forRemoval = true, since = "6.3.0")
    public static @NonNull Placeholder<?> of(final @NonNull String key, final @NonNull UUID uuid) {
        final String username = PlayerManager.getName(uuid);
        return Placeholder.miniMessage(key, username);
    }

    /**
     * Create a {@link Placeholder} from a string
     *
     * @param key   Template key
     * @param value Template value
     * @return Generated template
     *
     * @deprecated Use {@link Placeholders#miniMessage(String, String)} instead
     */
    @Deprecated(forRemoval = true, since = "6.3.0")
    public static @NonNull Placeholder<?> of(final @NonNull String key, final @NonNull String value) {
        return Placeholder.miniMessage(key, value);
    }

    /**
     * Create a {@link Placeholder} from a plot area
     *
     * @param key  Template Key
     * @param area Plot area
     * @return Generated template
     *
     * @deprecated Use {@link Placeholders#miniMessage(String, PlotArea)} instead
     */
    @Deprecated(forRemoval = true, since = "6.3.0")
    public static @NonNull Placeholder<?> of(final @NonNull String key, final @NonNull PlotArea area) {
        return Placeholder.miniMessage(key, area.toString());
    }

    /**
     * Create a {@link Placeholder} from a number
     *
     * @param key    Template key
     * @param number Number
     * @return Generated template
     *
     * @deprecated Use {@link Placeholders#miniMessage(String, Number)} instead
     */
    @Deprecated(forRemoval = true, since = "6.3.0")
    public static @NonNull Placeholder<?> of(final @NonNull String key, final @NonNull Number number) {
        return Placeholder.miniMessage(key, number.toString());
    }

}
