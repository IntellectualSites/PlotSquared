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

import com.plotsquared.core.configuration.Settings;
import com.plotsquared.core.plot.flag.FlagParseException;
import com.plotsquared.core.plot.flag.PlotFlag;
import com.plotsquared.core.plot.flag.implementations.DescriptionFlag;
import com.plotsquared.core.plot.flag.implementations.FarewellFlag;
import com.plotsquared.core.plot.flag.implementations.GreetingFlag;
import com.plotsquared.core.plot.flag.implementations.PlotTitleFlag;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Covers the value that {@code /plot flag set} and {@code /plot flag add} parse and store for the flags whose text is
 * shown to other players as MiniMessage.
 */
class FlagCommandClickStripTest {

    /**
     * A MiniMessage click tag for every click action that can be written as one.
     */
    private static final Map<String, String> CLICK_TAGS = clickTags();

    private static Map<String, String> clickTags() {
        final Map<String, String> tags = new LinkedHashMap<>();
        tags.put("RUN_COMMAND", "<click:run_command:'/op attacker'>Click me</click>");
        tags.put("SUGGEST_COMMAND", "<click:suggest_command:'/op attacker'>Click me</click>");
        tags.put("OPEN_URL", "<click:open_url:'https://example.org'>Click me</click>");
        tags.put("COPY_TO_CLIPBOARD", "<click:copy_to_clipboard:'/op attacker'>Click me</click>");
        tags.put("CHANGE_PAGE", "<click:change_page:'2'>Click me</click>");
        tags.put("OPEN_FILE", "<click:open_file:'file'>Click me</click>");
        return tags;
    }

    private static List<PlotFlag<?, ?>> textFlags() {
        return List.of(
                GreetingFlag.GREETING_FLAG_EMPTY,
                FarewellFlag.FAREWELL_FLAG_EMPTY,
                DescriptionFlag.DESCRIPTION_FLAG_EMPTY,
                PlotTitleFlag.TITLE_FLAG_DEFAULT
        );
    }

    static Stream<Arguments> clickTagsToRemove() {
        return CLICK_TAGS.entrySet().stream()
                .filter(tag -> Settings.Chat.CLICK_EVENT_ACTIONS_TO_REMOVE.contains(tag.getKey()))
                .map(tag -> Arguments.of(tag.getKey(), tag.getValue()));
    }

    static Stream<Arguments> textFlagsAndClickTagsToRemove() {
        final List<Arguments> arguments = new ArrayList<>();
        for (final PlotFlag<?, ?> flag : textFlags()) {
            clickTagsToRemove().forEach(tag -> arguments.add(Arguments.of(flag.getName(), flag, tag.get()[0], tag.get()[1])));
        }
        return arguments.stream();
    }

    static Stream<Arguments> appendingTextFlagsAndClickTagsToRemove() {
        return textFlagsAndClickTagsToRemove().filter(arguments -> !(arguments.get()[1] instanceof PlotTitleFlag));
    }

    @ParameterizedTest(name = "{0}: {2}")
    @MethodSource("textFlagsAndClickTagsToRemove")
    @DisplayName("A value typed for a text flag is parsed without its click event")
    void typedValueIsParsedWithoutClickEvent(
            final String flagName, final PlotFlag<?, ?> flag, final String action, final String clickTag
    ) throws FlagParseException {
        final PlotFlag<?, ?> parsed = FlagCommand.parseFlagValue(flag, arguments(flagName, clickTag));
        Assertions.assertFalse(
                hasClickEvent(parsed.toString()),
                () -> flagName + " must not keep the " + action + " click event of a typed value: " + parsed
        );
    }

    @ParameterizedTest(name = "{0}: {2}")
    @MethodSource("appendingTextFlagsAndClickTagsToRemove")
    @DisplayName("A value added to an existing text flag leaves the stored text without a click event")
    @SuppressWarnings({"unchecked", "rawtypes"})
    void addedValueLeavesStoredTextWithoutClickEvent(
            final String flagName, final PlotFlag flag, final String action, final String clickTag
    ) throws FlagParseException {
        final PlotFlag existing = flag.parse("Welcome");
        final PlotFlag<?, ?> added = FlagCommand.parseFlagValue(flag, arguments(flagName, clickTag));
        final PlotFlag stored = existing.merge(added.getValue());
        Assertions.assertFalse(
                hasClickEvent(stored.toString()),
                () -> flagName + " must not keep the " + action + " click event after a value was added: " + stored
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("clickTagsToRemove")
    @DisplayName("The text inside a removed click tag is kept")
    void textInsideRemovedClickTagIsKept(final String action, final String clickTag) throws FlagParseException {
        final PlotFlag<?, ?> parsed = FlagCommand.parseFlagValue(GreetingFlag.GREETING_FLAG_EMPTY, arguments("greeting", clickTag));
        Assertions.assertEquals(
                "Click me",
                plainText(MiniMessage.miniMessage().deserialize(parsed.toString())),
                "removing the " + action + " click event must keep the visible text"
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("clickTagsToRemove")
    @DisplayName("Every click tag used by these tests carries a click event before it is filtered")
    void testedClickTagCarriesClickEventBeforeFiltering(final String action, final String clickTag) {
        Assertions.assertTrue(
                hasClickEvent(clickTag),
                () -> "the " + action + " test input must carry a click event, otherwise the other tests prove nothing: " + clickTag
        );
    }

    @Test
    @DisplayName("Click events that run, suggest or copy a command or open a link are removed by default")
    void commandAndLinkClickEventsAreRemovedByDefault() {
        Assertions.assertTrue(
                Settings.Chat.CLICK_EVENT_ACTIONS_TO_REMOVE.containsAll(List.of(
                        "RUN_COMMAND",
                        "SUGGEST_COMMAND",
                        "OPEN_URL",
                        "COPY_TO_CLIPBOARD"
                )),
                () -> "the default configuration must remove command and link click events: "
                        + Settings.Chat.CLICK_EVENT_ACTIONS_TO_REMOVE
        );
    }

    /**
     * Builds the arguments the way the command receives them: the flag name, then the value split at spaces.
     */
    private static String[] arguments(final String flagName, final String value) {
        return (flagName + " " + value).split(" ");
    }

    private static boolean hasClickEvent(final String miniMessage) {
        return hasClickEvent(MiniMessage.miniMessage().deserialize(miniMessage));
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
