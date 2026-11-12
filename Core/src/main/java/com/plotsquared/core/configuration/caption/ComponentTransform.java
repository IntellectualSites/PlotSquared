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
import net.kyori.adventure.text.event.ClickEvent;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Set;

public interface ComponentTransform {

    /**
     * Creates a transform that applies the given transform on all child components and the
     * component itself. The children are transformed before the component itself is transformed.
     *
     * @param transform the transform to apply.
     * @return a new transform which is applied on all child components and the component itself.
     * @since 6.0.10
     */
    static ComponentTransform nested(ComponentTransform transform) {
        return new NestedComponentTransform(transform);
    }

    /**
     * Creates a transform that removes click events of the given actions from a component.
     * Note: To remove click events from children too, the returned transform must be wrapped
     * using {@link #nested(ComponentTransform)}.
     *
     * @param actionsToRemove the actions used to filter which click events should be removed.
     * @return a new transform that removes click events from a component.
     * @since 6.0.10
     */
    static ComponentTransform stripClicks(ClickEvent.Action... actionsToRemove) {
        return new ClickStripTransform(Set.of(actionsToRemove));
    }

    /**
     * Applies this transform on the given component and returns the result.
     *
     * @param original the component to transform.
     * @return the transformed component.
     * @since 6.0.10
     */
    @NonNull Component transform(@NonNull Component original);

}
