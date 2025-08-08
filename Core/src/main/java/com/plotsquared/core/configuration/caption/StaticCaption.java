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

import com.google.common.base.Preconditions;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.checkerframework.checker.nullness.qual.NonNull;

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
    public static @NonNull StaticCaption of(final @NonNull String text) {
        return new StaticCaption(Preconditions.checkNotNull(text, "Text may not be null"));
    }

    @Override
    public @NonNull String getComponent(@NonNull LocaleHolder localeHolder) {
        return this.value; // can't be translated
    }

    @Override
    public @NonNull Component toComponent(@NonNull final LocaleHolder localeHolder) {
        return MiniMessage.miniMessage().deserialize(this.value);
    }

    @Override
    public @NonNull Component toComponent(
            @NonNull final LocaleHolder localeHolder,
            final @NonNull TagResolver @NonNull ... tagResolvers
    ) {
        return MiniMessage.miniMessage().deserialize(this.value, tagResolvers);
    }

    @Override
    public @NonNull String toString() {
        return "StaticCaption(" + value + ")";
    }

}
