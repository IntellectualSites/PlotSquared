package com.github.intellectualsites.plotsquared.plot.flags;

import com.github.intellectualsites.plotsquared.plot.config.Caption;
import com.google.common.base.Preconditions;
import lombok.EqualsAndHashCode;
import org.jetbrains.annotations.NotNull;

/**
 * A plot flag is any property that can be assigned
 * to a plot, that will alter its functionality in some way.
 * These are user assignable in-game, or via configuration files.
 *
 * @param <T> Value contained in the flag.
 */
@EqualsAndHashCode(of = "value") public abstract class PlotFlag<T, F extends PlotFlag<T, F>> {

    private final T value;
    private final Caption flagCategory;
    private final Caption flagDescription;
    private final String flagName;

    /**
     * Construct a new flag instance.
     *
     * @param value Flag value
     */
    protected PlotFlag(@NotNull final T value, @NotNull final Caption flagCategory,
        @NotNull final Caption flagDescription) {
        this.value = Preconditions.checkNotNull(value, "flag value may not be null");
        this.flagCategory =
            Preconditions.checkNotNull(flagCategory, "flag category may not be null");
        this.flagDescription =
            Preconditions.checkNotNull(flagDescription, "flag description may not be null");
        // Parse flag name
        final StringBuilder flagName = new StringBuilder();
        final char[] chars = this.getClass().getSimpleName().replace("Flag", "").toCharArray();
        for (int i = 0; i < chars.length; i++) {
            if (i == 0) {
                flagName.append(Character.toLowerCase(chars[i]));
            } else if (Character.isUpperCase(chars[i])) {
                flagName.append('-').append(Character.toLowerCase(chars[i]));
            } else {
                flagName.append(chars[i]);
            }
        }
        this.flagName = flagName.toString();
    }

    /**
     * Get the flag value
     *
     * @return Non-nullable flag value
     */
    @NotNull public final T getValue() {
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
    public abstract F parse(@NotNull final String input) throws FlagParseException;

    /**
     * Merge this flag's value with another value and return an instance
     * holding the merged value.
     *
     * @param newValue New flag value.
     * @return Flag containing parsed flag value.
     */
    public abstract F merge(@NotNull final T newValue);

    /**
     * Returns a string representation of the flag instance, that when
     * passed through {@link #parse(String)} will result in an equivalent
     * instance of the flag.
     *
     * @return String representation of the flag
     */
    public abstract String toString();

    public final String getName() {
        return this.flagName;
    }

    public Caption getFlagDescription() {
        return this.flagDescription;
    }

    public Caption getFlagCategory() {
        return this.flagCategory;
    }

    public abstract String getExample();

    protected abstract F flagOf(@NotNull T value);

    public final F createFlagInstance(@NotNull final T value) {
        return flagOf(Preconditions.checkNotNull(value));
    }

}
