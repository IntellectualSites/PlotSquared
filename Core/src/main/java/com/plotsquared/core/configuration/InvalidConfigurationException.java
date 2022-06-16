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
package com.plotsquared.core.configuration;

/**
 * Exception thrown when attempting to load an invalid {@link Configuration}.
 */
@SuppressWarnings("serial")
public class InvalidConfigurationException extends Exception {

    /**
     * Creates a new instance of InvalidConfigurationException without a
     * message or cause.
     */
    public InvalidConfigurationException() {
    }

    /**
     * Constructs an instance of InvalidConfigurationException with the
     * specified message.
     *
     * @param msg The details of the exception.
     */
    public InvalidConfigurationException(String msg) {
        super(msg);
    }

    /**
     * Constructs an instance of InvalidConfigurationException with the
     * specified cause.
     *
     * @param cause The cause of the exception.
     */
    public InvalidConfigurationException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructs an instance of InvalidConfigurationException with the
     * specified message and cause.
     *
     * @param cause The cause of the exception.
     * @param msg   The details of the exception.
     */
    public InvalidConfigurationException(String msg, Throwable cause) {
        super(msg, cause);
    }

}
