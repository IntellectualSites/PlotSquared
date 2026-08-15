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
package com.plotsquared.core.plot.flag;

import com.plotsquared.core.configuration.caption.Caption;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

public class FlagParseException extends Exception {

    private final PlotFlag<?, ?> flag;
    private final String value;
    private final Caption errorMessage;
    private final TagResolver[] tagResolvers;

    /**
     * Construct a new flag parse exception to indicate that an attempt to parse a plot
     * flag was unsuccessful.
     *
     * @param flag         Flag instance
     * @param value        Value that failed ot parse
     * @param errorMessage An error message explaining the failure
     * @param args         Arguments used to format the error message
     */
    public FlagParseException(
            final PlotFlag<?, ?> flag, final String value,
            final Caption errorMessage, final TagResolver... args
    ) {
        super(String.format("Failed to parse flag of type '%s'. Value '%s' was not accepted.",
                flag.getName(), value
        ));
        this.flag = flag;
        this.value = value;
        this.errorMessage = errorMessage;
        this.tagResolvers = args;
    }

    /**
     * Returns the value that caused the parse exception
     *
     * @return Value that failed to parse
     */
    public String getValue() {
        return this.value;
    }

    /**
     * Returns the class that threw the exception
     *
     * @return Flag that threw the exception
     */
    public PlotFlag<?, ?> getFlag() {
        return this.flag;
    }

    /**
     * Get the error message that was supplied by the flag instance.
     *
     * @return Error message.
     */
    public Caption getErrorMessage() {
        return errorMessage;
    }

    /**
     * Get the templates that were supplied by the flag instance.
     *
     * @return Message templates.
     */
    public TagResolver[] getTagResolvers() {
        return tagResolvers;
    }

}
