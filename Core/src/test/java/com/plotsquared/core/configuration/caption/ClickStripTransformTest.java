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
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;


class ClickStripTransformTest {

    @Test
    @DisplayName("Remove click event of specific action correctly")
    void removeClickEvent() {
        var commonAction = ClickEvent.Action.OPEN_FILE;
        var transform = new ClickStripTransform(EnumSet.of(commonAction));
        var component = Component.text("Hello").clickEvent(ClickEvent.openFile("World"));
        var transformedComponent = transform.transform(component);
        Assertions.assertNull(transformedComponent.clickEvent());
    }

    @Test
    @DisplayName("Don't remove click events of other action types")
    void ignoreClickEvent() {
        var actionToRemove = ClickEvent.Action.SUGGEST_COMMAND;
        var transform = new ClickStripTransform(EnumSet.of(actionToRemove));
        var originalClickEvent = ClickEvent.changePage(1337);
        var component = Component.text("Hello")
                .clickEvent(originalClickEvent);
        var transformedComponent = transform.transform(component);
        Assertions.assertEquals(originalClickEvent, transformedComponent.clickEvent());
    }

    @Test
    @DisplayName("Remove nested click events correctly")
    void removeNestedClickEvent() {
        // nested transform is required to apply on children
        var transform = new NestedComponentTransform(new ClickStripTransform(EnumSet.allOf(ClickEvent.Action.class)));
        var inner = Component
                // some arbitrary values that should remain
                .text("World")
                .color(NamedTextColor.AQUA)
                .hoverEvent(HoverEvent.showText(Component.text("ABC")))
                .decorate(TextDecoration.OBFUSCATED)
                .insertion("DEF");
        var component = Component.text("Hello ")
                .append(
                        inner.clickEvent(ClickEvent.openUrl("https://example.org"))
                );
        var transformedComponent = transform.transform(component);
        Assertions.assertFalse(transformedComponent.children().isEmpty()); // child still exists
        Assertions.assertEquals(inner, transformedComponent.children().getFirst()); // only the click event has changed
        Assertions.assertNull(transformedComponent.children().getFirst().clickEvent());
    }

}
