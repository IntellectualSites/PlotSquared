package com.github.intellectualsites.plotsquared.plot.flags;

import com.github.intellectualsites.plotsquared.plot.config.Captions;
import com.google.common.base.Preconditions;
import lombok.EqualsAndHashCode;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

/**
 * A plot flag is any property that can be assigned
 * to a plot, that will alter its functionality in some way.
 * These are user assignable in-game, or via configuration files.
 *
 * @param <T> Value contained in the flag.
 */
@EqualsAndHashCode(of = "value") public abstract class PlotFlag<T, F extends PlotFlag<T, F>> {

    private final T value;
    private final Captions flagCategory;
    private final Captions flagDescription;

    /**
     * Construct a new flag instance.
     *
     * @param value Flag value
     */
    protected PlotFlag(@NotNull final T value, @NotNull final Captions flagCategory,
        @NotNull final Captions flagDescription) {
        this.value = Preconditions.checkNotNull(value, "flag value may not be null");
        this.flagCategory = Preconditions.checkNotNull(flagCategory, "flag category may not be null");
        this.flagDescription = Preconditions.checkNotNull(flagDescription, "flag description may not be null");
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
        return this.getClass().getSimpleName().toLowerCase(Locale.ENGLISH);
    }

    public Captions getFlagDescription() {
        return this.flagDescription;
    }

    public Captions getFlagCategory() {
        return this.flagCategory;
    }

    public abstract String getExample();

    protected abstract F flagOf(@NotNull T value);

}
