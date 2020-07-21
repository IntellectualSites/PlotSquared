/*
 *       _____  _       _    _____                                _
 *      |  __ \| |     | |  / ____|                              | |
 *      | |__) | | ___ | |_| (___   __ _ _   _  __ _ _ __ ___  __| |
 *      |  ___/| |/ _ \| __|\___ \ / _` | | | |/ _` | '__/ _ \/ _` |
 *      | |    | | (_) | |_ ____) | (_| | |_| | (_| | | |  __/ (_| |
 *      |_|    |_|\___/ \__|_____/ \__, |\__,_|\__,_|_|  \___|\__,_|
 *                                    | |
 *                                    |_|
 *            PlotSquared plot management system for Minecraft
 *                  Copyright (C) 2020 IntellectualSites
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.plotsquared.core.configuration.caption;

import com.plotsquared.core.player.PlotPlayer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
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
    void format(@Nonnull ChatContext context);

    final class ChatContext {

        private final PlotPlayer<?> recipient;
        private String message;
        ;
        private final boolean rawOutput;

        /**
         * Create a new chat context
         *
         * @param recipient Message recipient
         * @param message   Message
         * @param rawOutput Whether or not formatting keys should be included in the
         *                  final message
         */
        public ChatContext(@Nullable final PlotPlayer<?> recipient, @Nonnull final String message,
            final boolean rawOutput) {
            this.recipient = recipient;
            this.message = message;
            this.rawOutput = rawOutput;
        }

        /**
         * Get the message recipient
         *
         * @return Recipient
         */
        @Nullable public PlotPlayer<?> getRecipient() {
            return this.recipient;
        }

        /**
         * Get the message stored in the context
         *
         * @return Stored message
         */
        @Nonnull public String getMessage() {
            return this.message;
        }

        /**
         * Whether or not the output should escape
         * any formatting keys
         *
         * @return True if raw output is to be used
         */
        public boolean isRawOutput() {
            return this.rawOutput;
        }

        /**
         * Set the new message
         *
         * @param message Message
         */
        public void setMessage(@Nonnull final String message) {
            this.message = message;
        }
    }

}
