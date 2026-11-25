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
package com.plotsquared.core.command;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Covers the message that players with chat spy receive when somebody writes a plot comment.
 */
class CommentChatSpyTest {

    private static final String[] COMMENT = {
            "public",
            "<red><bold>[Server]</bold></red>",
            "<click:run_command:'/op",
            "attacker'>Click",
            "to",
            "confirm</click>"
    };

    private static final String TYPED_COMMAND =
            "/plot comment public <red><bold>[Server]</bold></red> <click:run_command:'/op attacker'>Click to confirm</click>";

    @Test
    @DisplayName("A comment relayed to chat spies carries no click event")
    void relayedCommentCarriesNoClickEvent() {
        final Component relayed = relay(COMMENT);
        Assertions.assertFalse(
                hasClickEvent(relayed),
                () -> "a comment must not reach chat spies with a click event: " + relayed
        );
    }

    @Test
    @DisplayName("A comment relayed to chat spies carries no colour chosen by its author")
    void relayedCommentCarriesNoColour() {
        final Component relayed = relay(COMMENT);
        Assertions.assertFalse(
                hasColour(relayed),
                () -> "a comment must not reach chat spies in a colour chosen by its author: " + relayed
        );
    }

    @Test
    @DisplayName("A comment relayed to chat spies carries no decoration chosen by its author")
    void relayedCommentCarriesNoDecoration() {
        final Component relayed = relay(COMMENT);
        Assertions.assertFalse(
                hasDecoration(relayed),
                () -> "a comment must not reach chat spies with a decoration chosen by its author: " + relayed
        );
    }

    @Test
    @DisplayName("A comment relayed to chat spies shows the command exactly as it was typed")
    void relayedCommentShowsTheTypedCommand() {
        Assertions.assertEquals(
                TYPED_COMMAND,
                plainText(relay(COMMENT)),
                "chat spies must see the tags of a comment as text"
        );
    }

    /**
     * Renders the relay the way a player receives it: the format is parsed as MiniMessage with the resolver applied.
     */
    private static Component relay(final String[] args) {
        return MiniMessage.miniMessage().deserialize(Comment.CHAT_SPY_FORMAT, Comment.chatSpyResolver(args));
    }

    private static boolean hasClickEvent(final Component component) {
        if (component.clickEvent() != null) {
            return true;
        }
        for (final Component child : component.children()) {
            if (hasClickEvent(child)) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasColour(final Component component) {
        if (component.color() != null) {
            return true;
        }
        for (final Component child : component.children()) {
            if (hasColour(child)) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasDecoration(final Component component) {
        for (final TextDecoration decoration : TextDecoration.values()) {
            if (component.decoration(decoration) == TextDecoration.State.TRUE) {
                return true;
            }
        }
        for (final Component child : component.children()) {
            if (hasDecoration(child)) {
                return true;
            }
        }
        return false;
    }

    private static String plainText(final Component component) {
        final StringBuilder text = new StringBuilder();
        if (component instanceof TextComponent textComponent) {
            text.append(textComponent.content());
        }
        for (final Component child : component.children()) {
            text.append(plainText(child));
        }
        return text.toString();
    }

}
