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
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.List;
import java.util.stream.Collectors;

/**
 * A transform that applies a nested transform on all child components and the component itself.
 */
final class NestedComponentTransform implements ComponentTransform {

    private final ComponentTransform transform;

    public NestedComponentTransform(final ComponentTransform transform) {
        this.transform = transform;
    }

    @Override
    public @NonNull Component transform(final @NonNull Component original) {
        return this.transform.transform(original.children(transformChildren(original.children())));
    }

    private List<Component> transformChildren(List<Component> children) {
        return children.stream().map(this::transform).collect(Collectors.toList());
    }

}
