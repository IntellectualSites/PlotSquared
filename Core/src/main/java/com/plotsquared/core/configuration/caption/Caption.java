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

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Any message that can be sent to a player, the console, etc.
 */
public interface Caption {

    /**
     * Get the message that should be sent to the recipient
     *
     * @param localeHolder Locale holder
     * @return Message
     */
    @NonNull String getComponent(@NonNull LocaleHolder localeHolder);

    /**
     * Get the Adventure {@link ComponentLike} for this caption
     *
     * @param localeHolder Locale holder
     * @return {@link ComponentLike}
     * @since 7.0.0
     */
    @NonNull Component toComponent(@NonNull LocaleHolder localeHolder);

    /**
     * Get the Adventure {@link ComponentLike} for this caption while applying custom {@link TagResolver}
     * (apart from the default {@code core.prefix})
     * @param localeHolder Local holder
     * @param tagResolvers custom tag resolvers to replace placeholders / parameters
     * @return {@link ComponentLike}
     * @since 7.5.4
     */
    @NonNull Component toComponent(@NonNull LocaleHolder localeHolder, @NonNull TagResolver @NonNull... tagResolvers);

    @NonNull String toString();

}
