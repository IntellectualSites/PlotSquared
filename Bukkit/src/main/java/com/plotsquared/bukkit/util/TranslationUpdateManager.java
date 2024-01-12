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
package com.plotsquared.bukkit.util;

import com.intellectualsites.annotations.NotPublic;
import com.plotsquared.core.PlotSquared;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

/**
 * This is a helper class which replaces older syntax no longer supported by MiniMessage with replacements in messages_%.json.
 * MiniMessage changed the syntax between major releases. To warrant a smooth upgrade, we attempt to replace any occurrences
 * while loading PlotSquared.
 *
 * @since 7.0.0
 */
@NotPublic
public class TranslationUpdateManager {

    public static void upgradeTranslationFile() throws IOException {
        String suggestCommand = "suggest_command";
        String suggestCommandReplacement = "run_command";
        String minHeight = "minHeight";
        String minheightReplacement = "minheight";
        String maxHeight = "maxHeight";
        String maxheightReplacement = "maxheight";
        String usedGrants = "usedGrants";
        String usedGrantsReplacement = "used_grants";
        String remainingGrants = "remainingGrants";
        String remainingGrantsReplacement = "remaining_grants";
        String minimumRadius = "minimumRadius";
        String minimumRadiusReplacement = "minimum_radius";
        String maximumMoves = "maximumMoves";
        String maximumMovesReplacement = "maximum_moves";
        String userMove = "userMove";
        String userMoveReplacement = "user_move";

        // tag opening / closing characters are important, as the locale keys exist as well, which should not be replaced
        String listInfoUnknown = "<info.unknown>";
        String listInfoUnknownReplacement = "<unknown>";
        String listInfoServer = "<info.server>";
        String listInfoServerReplacement = "<server>";
        String listInfoEveryone = "<info.everyone>";
        String listInfoEveryoneReplacement = "<everyone>";

        try (Stream<Path> paths = Files.walk(Paths.get(PlotSquared.platform().getDirectory().toPath().resolve("lang").toUri()))) {
            paths
                    .filter(Files::isRegularFile)
                    .filter(p -> p.getFileName().toString().matches("messages_[a-z]{2}\\.json"))
                    .forEach(p -> {
                        replaceInFile(p, suggestCommand, suggestCommandReplacement);
                        replaceInFile(p, minHeight, minheightReplacement);
                        replaceInFile(p, maxHeight, maxheightReplacement);
                        replaceInFile(p, usedGrants, usedGrantsReplacement);
                        replaceInFile(p, remainingGrants, remainingGrantsReplacement);
                        replaceInFile(p, minimumRadius, minimumRadiusReplacement);
                        replaceInFile(p, maximumMoves, maximumMovesReplacement);
                        replaceInFile(p, userMove, userMoveReplacement);
                        replaceInFile(p, listInfoUnknown, listInfoUnknownReplacement);
                        replaceInFile(p, listInfoServer, listInfoServerReplacement);
                        replaceInFile(p, listInfoEveryone, listInfoEveryoneReplacement);
                    });
        }
    }

    private static void replaceInFile(Path path, String searchText, String replacementText) {
        try {
            String content = Files.readString(path);
            if (content.contains(searchText)) {
                content = content.replaceAll(searchText, replacementText);
                Files.writeString(path, content);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
