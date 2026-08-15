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
package com.plotsquared.core.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

public class StringManTest {

    @Test
    public void ensureThatAllVariationsHasTheExpectedOutcome() {

        List<Message> messages = List.of(
                new Message("title", List.of("title")),
                new Message("title \"sub title\"", List.of("title", "sub title")),
                new Message("\"a title\" subtitle", List.of("a title", "subtitle")),
                new Message("\"title\" \"subtitle\"", List.of("title", "subtitle")),
                new Message(
                        "\"How <bold>bold</bold> of you\" \"to assume I like <rainbow>rainbows</rainbow>\"",
                        List.of("How <bold>bold</bold> of you", "to assume I like <rainbow>rainbows</rainbow>")
                )
        );

        for (Message message : messages) {
            var messageList = StringMan.splitMessage(message.input);

            Assertions.assertEquals(message.expected.size(), messageList.size());
            if (message.expected.size() > 0) {
                Assertions.assertEquals(message.expected.get(0), messageList.get(0));
            }
            if (message.expected.size() > 1) {
                Assertions.assertEquals(message.expected.get(1), messageList.get(1));
            }
        }
    }

    private record Message(String input, List<String> expected) {

    }

}
