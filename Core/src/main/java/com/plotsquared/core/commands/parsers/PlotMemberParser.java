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
package com.plotsquared.core.commands.parsers;

import cloud.commandframework.annotations.parsers.Parser;
import cloud.commandframework.captions.CaptionVariable;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.exceptions.parsing.NoInputProvidedException;
import cloud.commandframework.exceptions.parsing.ParserException;
import com.google.inject.Inject;
import com.plotsquared.core.commands.PlotSquaredCaptionKeys;
import com.plotsquared.core.commands.arguments.PlotMember;
import com.plotsquared.core.configuration.Settings;
import com.plotsquared.core.inject.annotations.ImpromptuPipeline;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.uuid.UUIDMapping;
import com.plotsquared.core.uuid.UUIDPipeline;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.Serial;
import java.util.Queue;
import java.util.UUID;

public class PlotMemberParser {

    private final UUIDPipeline uuidPipeline;

    @Inject
    public PlotMemberParser(@ImpromptuPipeline final @NonNull UUIDPipeline uuidPipeline) {
        this.uuidPipeline = uuidPipeline;
    }

    @Parser
    public @NonNull PlotMember parse(
            final @NonNull CommandContext<PlotPlayer<?>> context,
            final @NonNull Queue<@NonNull String> input
    ) {
        final var candidate = input.peek();
        if (candidate == null) {
            throw new NoInputProvidedException(this.getClass(), context);
        }

        if ("*".equals(candidate)) {
            return PlotMember.EVERYONE;
        } else if (candidate.length() > 16) {
            try {
                return new PlotMember.Player(UUID.fromString(candidate));
            } catch (IllegalArgumentException ignored) {
                throw new TargetParseException(candidate, context);
            }
        }

        if (Settings.Paper_Components.PAPER_LISTENERS) {
            try {
                return this.uuidPipeline.getUUID(candidate, Settings.UUID.NON_BLOCKING_TIMEOUT)
                        .get()
                        .map(UUIDMapping::getUuid)
                        .map(PlotMember.Player::new)
                        .orElseThrow();
            } catch (Exception e) {
                throw new TargetParseException(candidate, context);
            }
        } else {
            return new PlotMember.LazyPlayer(
                    candidate,
                    () -> this.uuidPipeline.getUUID(candidate, Settings.UUID.NON_BLOCKING_TIMEOUT)
                            .get()
                            .map(UUIDMapping::getUuid)
                            .orElse(null)
            );
        }
    }

    public static final class TargetParseException extends ParserException {

        @Serial
        private static final long serialVersionUID = 927476591631527552L;
        private final String input;

        /**
         * Construct a new Player parse exception
         *
         * @param input   String input
         * @param context Command context
         */
        public TargetParseException(
                final @NonNull String input,
                final @NonNull CommandContext<?> context
        ) {
            super(
                    PlotMemberParser.class,
                    context,
                    PlotSquaredCaptionKeys.ARGUMENT_PARSE_FAILURE_TARGET,
                    CaptionVariable.of("input", input)
            );
            this.input = input;
        }

        /**
         * Get the supplied input
         *
         * @return String value
         */
        public @NonNull String getInput() {
            return this.input;
        }
    }
}
