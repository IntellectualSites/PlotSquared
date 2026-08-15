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

import com.google.common.base.Objects;
import com.plotsquared.core.PlotSquared;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Caption that is user modifiable
 */
public final class TranslatableCaption implements NamespacedCaption {

    /**
     * Default caption namespace
     */
    public static final String DEFAULT_NAMESPACE = "plotsquared";

    private final String namespace;
    private final String key;

    private TranslatableCaption(final @NonNull String namespace, final @NonNull String key) {
        this.namespace = namespace;
        this.key = key;
    }

    /**
     * Get a new {@link TranslatableCaption} instance
     *
     * @param rawKey Caption key in the format namespace:key. If no namespace is
     *               included, {@link #DEFAULT_NAMESPACE} will be used.
     * @return Caption instance
     */
    public static @NonNull TranslatableCaption of(final @NonNull String rawKey) {
        final String namespace;
        final String key;
        if (rawKey.contains(":")) {
            final String[] split = rawKey.split(Pattern.quote(":"));
            namespace = split[0];
            key = split[1];
        } else {
            namespace = DEFAULT_NAMESPACE;
            key = rawKey;
        }
        return new TranslatableCaption(
                namespace.toLowerCase(Locale.ENGLISH),
                key.toLowerCase(Locale.ENGLISH)
        );
    }

    /**
     * Get a new {@link TranslatableCaption} instance
     *
     * @param namespace Caption namespace
     * @param key       Caption key
     * @return Caption instance
     */
    public static @NonNull TranslatableCaption of(
            final @NonNull String namespace,
            final @NonNull String key
    ) {
        return new TranslatableCaption(
                namespace.toLowerCase(Locale.ENGLISH),
                key.toLowerCase(Locale.ENGLISH)
        );
    }

    @Override
    public @NonNull String getComponent(final @NonNull LocaleHolder localeHolder) {
        return PlotSquared.get().getCaptionMap(this.namespace).getMessage(this, localeHolder);
    }

    @Override
    public @NonNull Component toComponent(@NonNull final LocaleHolder localeHolder) {
        return this.toComponent(localeHolder, new TagResolver[0]);
    }

    @Override
    public @NonNull Component toComponent(
            @NonNull final LocaleHolder localeHolder,
            final @NonNull TagResolver @NonNull ... tagResolvers
    ) {
        if (getKey().equals("core.prefix")) {
            return MiniMessage.miniMessage().deserialize(getComponent(localeHolder));
        }
        TagResolver[] finalResolvers = Arrays.copyOf(tagResolvers, tagResolvers.length + 1);
        finalResolvers[finalResolvers.length - 1] = TagResolver.resolver(
                "prefix",
                Tag.inserting(TranslatableCaption.of("core.prefix").toComponent(localeHolder))
        );
        return MiniMessage.miniMessage().deserialize(getComponent(localeHolder), finalResolvers);
    }

    @Override
    public @NonNull String getKey() {
        return this.key;
    }

    @Override
    public @NonNull String getNamespace() {
        return this.namespace;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        final TranslatableCaption that = (TranslatableCaption) o;
        return Objects.equal(this.getNamespace(), that.getNamespace()) && Objects
                .equal(this.getKey(), that.getKey());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.getNamespace(), this.getKey());
    }

    @Override
    public @NotNull String toString() {
        return "TranslatableCaption(" + getNamespace() + ":" + getKey() + ")";
    }

}
