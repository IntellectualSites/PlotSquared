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

import com.plotsquared.core.configuration.Settings;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.flag.PlotFlag;
import com.plotsquared.core.plot.flag.implementations.DescriptionFlag;
import com.plotsquared.core.plot.flag.implementations.FarewellFlag;
import com.plotsquared.core.plot.flag.implementations.GreetingFlag;
import com.plotsquared.core.plot.flag.implementations.PlotTitleFlag;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.ParsingException;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Set;
import java.util.regex.Pattern;

import static com.plotsquared.core.configuration.caption.ComponentTransform.nested;
import static com.plotsquared.core.configuration.caption.ComponentTransform.stripClicks;

public class CaptionUtility {

    private static final Pattern LEGACY_FORMATTING = Pattern.compile("§[a-gklmnor0-9]");

    // flags which values are parsed by minimessage
    private static final Set<Class<? extends PlotFlag<?, ?>>> MINI_MESSAGE_FLAGS = Set.of(
            GreetingFlag.class,
            FarewellFlag.class,
            DescriptionFlag.class,
            PlotTitleFlag.class
    );

    private static final ComponentTransform CLICK_STRIP_TRANSFORM = nested(
            stripClicks(
                    Settings.Chat.CLICK_EVENT_ACTIONS_TO_REMOVE.stream()
                            .map(ClickEvent.Action::valueOf)
                            .toArray(ClickEvent.Action[]::new)
            )
    );


    /**
     * Format a chat message but keep the formatting keys
     *
     * @param recipient Message recipient
     * @param message   Message
     * @return Formatted message
     */
    public static String formatRaw(PlotPlayer<?> recipient, String message) {
        final ChatFormatter.ChatContext chatContext =
                new ChatFormatter.ChatContext(recipient, message, true);
        for (final ChatFormatter chatFormatter : ChatFormatter.formatters) {
            chatFormatter.format(chatContext);
        }
        return chatContext.getMessage();
    }

    /**
     * Format a chat message
     *
     * @param recipient Message recipient
     * @param message   Message
     * @return Formatted message
     */
    public static String format(
            final @Nullable PlotPlayer<?> recipient,
            final @NonNull String message
    ) {
        final ChatFormatter.ChatContext chatContext =
                new ChatFormatter.ChatContext(recipient, message, false);
        for (final ChatFormatter chatFormatter : ChatFormatter.formatters) {
            chatFormatter.format(chatContext);
        }
        return chatContext.getMessage();
    }

    /**
     * Strips configured click events from a MiniMessage string.
     *
     * @param miniMessageString the message from which the specified click events should be removed from.
     * @return the string without the click events that are configured to be removed.
     * @see Settings.Chat#CLICK_EVENT_ACTIONS_TO_REMOVE
     * @since 6.0.10
     */
    public static String stripClickEvents(final @NonNull String miniMessageString) {
        // parse, transform and serialize again
        Component component;
        try {
            component = MiniMessage.miniMessage().deserialize(miniMessageString);
        } catch (ParsingException e) {
            // if the String cannot be parsed, we try stripping legacy colors
            String legacyStripped = LEGACY_FORMATTING.matcher(miniMessageString).replaceAll("");
            component = MiniMessage.miniMessage().deserialize(legacyStripped);
        }
        component = CLICK_STRIP_TRANSFORM.transform(component);
        return MiniMessage.miniMessage().serialize(component);
    }

    /**
     * Strips configured MiniMessage click events from a plot flag value.
     * This is used before letting the string be parsed by the plot flag.
     * This method works the same way as {@link #stripClickEvents(String)} but will only
     * strip click events from messages that target flags that are meant to contain MiniMessage strings.
     *
     * @param flag              the flag the message is targeted for.
     * @param miniMessageString the message from which the specified click events should be removed from.
     * @return the string without the click events that are configured to be removed.
     * @see Settings.Chat#CLICK_EVENT_ACTIONS_TO_REMOVE
     * @see #stripClickEvents(String)
     * @since 6.0.10
     */
    public static String stripClickEvents(
            final @NonNull PlotFlag<?, ?> flag,
            final @NonNull String miniMessageString
    ) {
        if (MINI_MESSAGE_FLAGS.contains(flag.getClass())) {
            return stripClickEvents(miniMessageString);
        }
        return miniMessageString;
    }

}
