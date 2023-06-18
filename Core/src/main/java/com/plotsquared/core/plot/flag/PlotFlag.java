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

import com.google.common.base.Preconditions;
import com.plotsquared.core.configuration.caption.Caption;
import net.kyori.adventure.text.Component;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Collection;
import java.util.Collections;

/**
 * A plot flag is any property that can be assigned
 * to a plot, that will alter its functionality in some way.
 * These are user assignable in-game, or via configuration files.
 *
 * @param <T> Value contained in the flag.
 */
public abstract class PlotFlag<T, F extends PlotFlag<T, F>> {

    private final T value;
    private final Caption flagCategory;
    private final Caption flagDescription;
    private final String flagName;

    /**
     * Construct a new flag instance.
     *
     * @param value           Flag value
     * @param flagCategory    The flag category
     * @param flagDescription A caption describing the flag functionality
     */
    protected PlotFlag(
            final @NonNull T value, final @NonNull Caption flagCategory,
            final @NonNull Caption flagDescription
    ) {
        this.value = Preconditions.checkNotNull(value, "flag value may not be null");
        this.flagCategory =
                Preconditions.checkNotNull(flagCategory, "flag category may not be null");
        this.flagDescription =
                Preconditions.checkNotNull(flagDescription, "flag description may not be null");
        // Parse flag name
        // noinspection unchecked
        this.flagName = getFlagName(this.getClass());
    }

    /**
     * Return the name of the flag.
     *
     * @param flagClass Flag class
     * @param <T>       Value type
     * @param <F>       Flag type
     * @return The name of the flag implemented by the given class
     */
    public static <T, F extends PlotFlag<T, F>> String getFlagName(Class<F> flagClass) {
        final StringBuilder flagName = new StringBuilder();
        final char[] chars = flagClass.getSimpleName().replace("Flag", "").toCharArray();
        for (int i = 0; i < chars.length; i++) {
            if (i == 0) {
                flagName.append(Character.toLowerCase(chars[i]));
            } else if (Character.isUpperCase(chars[i])) {
                flagName.append('-').append(Character.toLowerCase(chars[i]));
            } else {
                flagName.append(chars[i]);
            }
        }
        return flagName.toString();
    }

    /**
     * Gets the flag name as a Kyori {@link Component}
     *
     * @see #getFlagName(Class)
     * @since 7.0.0
     */
    public static <T, F extends PlotFlag<T, F>> Component getFlagNameComponent(Class<F> flagClass) {
        return Component.text(getFlagName(flagClass));
    }

    /**
     * Get the flag value
     *
     * @return Non-nullable flag value
     */
    public @NonNull
    final T getValue() {
        return this.value;
    }

    /**
     * Parse a string into a flag, and throw an exception in the case that the
     * string does not represent a valid flag value. This instance won't change its
     * state, but instead an instance holding the parsed flag value will be returned.
     *
     * @param input String to parse.
     * @return Parsed value, if valid.
     * @throws FlagParseException If the value could not be parsed.
     */
    public abstract F parse(final @NonNull String input) throws FlagParseException;

    /**
     * Merge this flag's value with another value and return an instance
     * holding the merged value.
     *
     * @param newValue New flag value.
     * @return Flag containing parsed flag value.
     */
    public abstract F merge(final @NonNull T newValue);

    /**
     * Returns a string representation of the flag instance, that when
     * passed through {@link #parse(String)} will result in an equivalent
     * instance of the flag.
     *
     * @return String representation of the flag
     */
    public abstract String toString();

    /**
     * Get the flag name.
     *
     * @return Flag name
     */
    public final String getName() {
        return this.flagName;
    }

    /**
     * Get a simple caption that describes the flag usage.
     *
     * @return Flag description.
     */
    public Caption getFlagDescription() {
        return this.flagDescription;
    }

    /**
     * Get the category this flag belongs to. Usually a caption from {@link com.plotsquared.core.configuration.caption.TranslatableCaption}
     * <p>
     * These categories are used to categorize the flags when outputting
     * flag lists to players.
     *
     * @return Flag category
     */
    public Caption getFlagCategory() {
        return this.flagCategory;
    }

    /**
     * Get if the flag's permission should check for values. E.g. plots.flag.set.music.VALUE
     *
     * @return if valued permission
     * @since 6.0.10
     */
    public boolean isValuedPermission() {
        return true;
    }

    /**
     * An example of a string that would parse into a valid
     * flag value.
     *
     * @return An example flag value.
     */
    public abstract String getExample();

    protected abstract F flagOf(@NonNull T value);

    /**
     * Create a new instance of the flag using a provided
     * (non-null) value.
     *
     * @param value The flag value
     * @return The created flag instance
     */
    public final F createFlagInstance(final @NonNull T value) {
        return flagOf(Preconditions.checkNotNull(value));
    }

    /**
     * Get the tab completable values associated with the flag type, or
     * an empty collection if tab completion isn't supported.
     *
     * @return Collection containing tab completable flag values
     */
    public Collection<String> getTabCompletions() {
        return Collections.emptyList();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final PlotFlag<?, ?> plotFlag = (PlotFlag<?, ?>) o;
        return value.equals(plotFlag.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

}
