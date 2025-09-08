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

import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

/**
 * A holder for a caption.
 * Useful when constructing messages in multiple steps with {@link TagResolver}s.
 */
public class CaptionHolder {

    private Caption caption = StaticCaption.of("");
    private TagResolver[] tagResolvers = new TagResolver[0];

    /**
     * Set the {@link Caption} to send.
     *
     * @param caption The new caption.
     */
    public void set(Caption caption) {
        this.caption = caption;
    }

    /**
     * Get the {@link Caption} to send.
     *
     * @return The caption to send.
     */
    public Caption get() {
        return this.caption;
    }

    /**
     * Get the {@link TagResolver}s to use when resolving tags in the {@link Caption}.
     *
     * @return The tag resolvers to use.
     * @since 7.0.0
     */
    public TagResolver[] getTagResolvers() {
        return this.tagResolvers;
    }

    /**
     * Set the {@link TagResolver}s to use when resolving tags in the {@link Caption}.
     *
     * @param tagResolvers The tag resolvers to use.
     * @since 7.0.0
     */
    public void setTagResolvers(TagResolver... tagResolvers) {
        this.tagResolvers = tagResolvers;
    }

}
