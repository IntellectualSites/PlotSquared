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

import com.plotsquared.core.player.PlotPlayer;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

@FunctionalInterface
public interface ChatFormatter {

    Collection<ChatFormatter> formatters = new ArrayList<>(Collections.singletonList(new PlotSquaredChatFormatter()));

    /**
     * Format a message using all registered formatters
     *
     * @param context Message to format
     */
    void format(@NonNull ChatContext context);

    final class ChatContext {

        private final PlotPlayer<?> recipient;
        private final boolean rawOutput;
        private String message;

        /**
         * Create a new chat context
         *
         * @param recipient Message recipient
         * @param message   Message
         * @param rawOutput Whether or not formatting keys should be included in the
         *                  final message
         */
        public ChatContext(
                final @Nullable PlotPlayer<?> recipient, final @NonNull String message,
                final boolean rawOutput
        ) {
            this.recipient = recipient;
            this.message = message;
            this.rawOutput = rawOutput;
        }

        /**
         * Get the message recipient
         *
         * @return Recipient
         */
        public @Nullable PlotPlayer<?> getRecipient() {
            return this.recipient;
        }

        /**
         * Get the message stored in the context
         *
         * @return Stored message
         */
        public @NonNull String getMessage() {
            return this.message;
        }

        /**
         * Set the new message
         *
         * @param message Message
         */
        public void setMessage(final @NonNull String message) {
            this.message = message;
        }

        /**
         * Whether or not the output should escape
         * any formatting keys
         *
         * @return {@code true} if raw output is to be used
         */
        public boolean isRawOutput() {
            return this.rawOutput;
        }

    }

}
