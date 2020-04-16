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
package com.plotsquared.core.util.helpmenu;

import com.plotsquared.core.command.Argument;
import com.plotsquared.core.command.Command;
import com.plotsquared.core.configuration.Captions;
import com.plotsquared.core.util.StringMan;

public class HelpObject {

    private final String _rendered;

    public HelpObject(final Command command, final String label) {
        _rendered = StringMan.replaceAll(Captions.HELP_ITEM.getTranslated(), "%usage%",
            command.getUsage().replaceAll("\\{label\\}", label), "[%alias%]",
            !command.getAliases().isEmpty() ?
                "(" + StringMan.join(command.getAliases(), "|") + ")" :
                "", "%desc%", command.getDescription(), "%arguments%",
            buildArgumentList(command.getRequiredArguments()), "{label}", label);
    }

    @Override public String toString() {
        return _rendered;
    }

    private String buildArgumentList(final Argument[] arguments) {
        if (arguments == null) {
            return "";
        }
        final StringBuilder builder = new StringBuilder();
        for (final Argument<?> argument : arguments) {
            builder.append("[").append(argument.getName()).append(" (")
                .append(argument.getExample()).append(")],");
        }
        return arguments.length > 0 ? builder.substring(0, builder.length() - 1) : "";
    }
}
