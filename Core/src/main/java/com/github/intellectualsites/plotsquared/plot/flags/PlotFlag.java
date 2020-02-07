package com.github.intellectualsites.plotsquared.plot.flags;

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
@EqualsAndHashCode(of = "value") public abstract class PlotFlag<T> {

    private T value;

    /**
     * Construct a new flag instance.
     *
     * @param value Flag value
     */
    protected PlotFlag(@NotNull final T value) {
        this.value = Preconditions.checkNotNull(value, "flag values may not be null");
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
     * Update the flag value.
     *
     * @param newValue New flag value.
     */
    public T setFlagValue(@NotNull final T newValue) {
        return (this.value = Preconditions.checkNotNull(newValue, "flag values may not be null"));
    }

    /**
     * Parse a string into a flag value, and throw an exception in the case that the
     * string does not represent a valid flag value. The flag value will be updated to
     * the newly parsed value.
     *
     * @param input String to parse.
     * @return Parsed value, if valid.
     * @throws FlagParseException If the value could not be parsed.
     */
    public abstract T parse(@NotNull final String input) throws FlagParseException;

    /**
     * Merge two flag values into one and updates the flag value.
     *
     * @param oldValue Existing flag value.
     * @param newValue New flag value.
     * @return Flag containing parsed flag value.
     */
    public abstract T merge(@NotNull final T oldValue, @NotNull final T newValue);

    /**
     * Returns a string representation of the flag instance, that when
     * passed through {@link #parse(String)} will result in an equivalent
     * instance of the flag.
     *
     * @return String representation of the flag
     */
    public abstract String toString();

    public final String getName() {
        return this.getClass().getSimpleName();
    }

}
