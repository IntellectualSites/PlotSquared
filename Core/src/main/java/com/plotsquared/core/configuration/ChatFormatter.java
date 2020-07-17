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
package com.plotsquared.core.configuration;

import com.plotsquared.core.player.PlotPlayer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

@FunctionalInterface
public interface ChatFormatter {

    Collection<ChatFormatter> formatters =
        new ArrayList<>(Collections.singletonList(new PlotSquaredChatFormatter()));

    void format(ChatContext context);

    final class ChatContext {

        private final PlotPlayer<?> recipient;
        private String message;
        private final Object[] args;
        private final boolean rawOutput;

        public ChatContext(final PlotPlayer<?> recipient, final String message, final Object[] args,
            final boolean rawOutput) {
            this.recipient = recipient;
            this.message = message;
            this.args = args;
            this.rawOutput = rawOutput;
        }

        public PlotPlayer<?> getRecipient() {
            return this.recipient;
        }

        public String getMessage() {
            return this.message;
        }

        public Object[] getArgs() {
            return this.args;
        }

        public boolean isRawOutput() {
            return this.rawOutput;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }

}
