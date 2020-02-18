package com.github.intellectualsites.plotsquared.plot.flags;

import com.google.common.collect.ImmutableMap;
import lombok.EqualsAndHashCode;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Container type for {@link PlotFlag plot flags}.
 */
@EqualsAndHashCode(of = "flagMap") @SuppressWarnings("unused") public class FlagContainer {

    private final FlagContainer parentContainer;
    private final Map<Class<?>, PlotFlag<?, ?>> flagMap = new HashMap<>();
    private final PlotFlagUpdateHandler plotFlagUpdateHandler;

    public FlagContainer(@Nullable final FlagContainer parentContainer,
        @Nullable PlotFlagUpdateHandler plotFlagUpdateHandler) {
        this.parentContainer = parentContainer;
        this.plotFlagUpdateHandler = plotFlagUpdateHandler;
    }

    public FlagContainer(@Nullable final FlagContainer parentContainer) {
        this(parentContainer, null);
    }

    @SuppressWarnings("ALL")
    public static <V, T extends PlotFlag<V, ?>> T castUnsafe(final PlotFlag<?, ?> flag) {
        return (T) flag;
    }

    /**
     * Return the parent container (if the container has a parent)
     *
     * @return Parent container
     */
    public FlagContainer getParentContainer() {
        return this.parentContainer;
    }

    protected Map<Class<?>, PlotFlag<?, ?>> getInternalPlotFlagMap() {
        return this.flagMap;
    }

    /**
     * Get an immutable view of the underlying flag map
     *
     * @return Immutable flag map
     */
    public Map<Class<?>, PlotFlag<?, ?>> getFlagMap() {
        return ImmutableMap.<Class<?>, PlotFlag<?, ?>>builder().putAll(this.flagMap).build();
    }

    /**
     * Add a flag to the container
     *
     * @param flag Flag to add
     * @see #addAll(Collection) to add multiple flags
     */
    public <V, T extends PlotFlag<V, ?>> void addFlag(final T flag) {
        this.flagMap.put(flag.getClass(), flag);
        if (this.plotFlagUpdateHandler != null) {
            this.plotFlagUpdateHandler.handle(flag, PlotFlagUpdateType.FLAG_ADDED);
        }
    }

    /**
     * Remove a flag from the container
     *
     * @param flag Flag to remove
     */
    public <V, T extends PlotFlag<V, ?>> V removeFlag(final T flag) {
        final Object value = this.flagMap.remove(flag.getClass());
        if (this.plotFlagUpdateHandler != null) {
            this.plotFlagUpdateHandler.handle(flag, PlotFlagUpdateType.FLAG_REMOVED);
        }
        if (value == null) {
            return null;
        } else {
            return (V) flag;
        }
    }

    /**
     * Add all flags to the container
     *
     * @param flags Flags to add
     * @see #addFlag(PlotFlag) to add a single flagg
     */
    public void addAll(final Collection<PlotFlag<?, ?>> flags) {
        for (final PlotFlag<?, ?> flag : flags) {
            this.addFlag(flag);
        }
    }

    /**
     * Clears the local flag map
     */
    public void clearLocal() {
        this.flagMap.clear();
    }

    /**
     * Get a collection of all recognized plot flags. Will by
     * default use the values contained in {@link GlobalFlagContainer}.
     *
     * @return All recognized flag types
     */
    public Collection<PlotFlag<?, ?>> getRecognizedPlotFlags() {
        return this.getHighestClassContainer().getFlagMap().values();
    }

    /**
     * Recursively seek for the highest order flag container.
     * This will by default return {@link GlobalFlagContainer}.
     *
     * @return Highest order class container.
     */
    public final FlagContainer getHighestClassContainer() {
        if (this.getParentContainer() != null) {
            return this.getParentContainer();
        }
        return this;
    }

    /**
     * Has the same functionality as {@link #getFlag(Class)}, but
     * with erased generic types.
     */
    public PlotFlag<?, ?> getFlagErased(Class<?> flagClass) {
        final PlotFlag<?, ?> flag = this.flagMap.get(flagClass);
        if (flag != null) {
            return flag;
        } else {
            if (getParentContainer() != null) {
                return getParentContainer().getFlagErased(flagClass);
            }
        }
        return null;
    }

    /**
     * Query all levels of flag containers for a flag. This guarantees that a flag
     * instance is returned, as long as it is registered in the
     * {@link GlobalFlagContainer global flag container}.
     *
     * @param flagClass Flag class to query for
     * @param <V>       Flag value type
     * @param <T>       Flag type
     * @return Flag instance
     */
    public <V, T extends PlotFlag<V, ?>> T getFlag(final Class<? extends T> flagClass) {
        final PlotFlag<?, ?> flag = this.flagMap.get(flagClass);
        if (flag != null) {
            return castUnsafe(flag);
        } else {
            if (getParentContainer() != null) {
                return getParentContainer().getFlag(flagClass);
            }
        }
        return null;
    }

    /**
     * Check for flag existence in this flag container instance.
     *
     * @param flagClass Flag class to query for
     * @param <V>       Flag value type
     * @param <T>       Flag type
     * @return The flag instance, if it exists in this container, else null.
     */
    @Nullable public <V, T extends PlotFlag<V, ?>> T queryLocal(
        final Class<?> flagClass) {
        final PlotFlag<?, ?> localFlag = this.flagMap.get(flagClass);
        if (localFlag == null) {
            return null;
        } else {
            return castUnsafe(localFlag);
        }
    }

    /**
     * Update event types used in {@link PlotFlagUpdateHandler}.
     */
    public enum PlotFlagUpdateType {
        /**
         * A flag was added to a plot container
         */
        FLAG_ADDED,
        /**
         * A flag was removed from a plot container
         */
        FLAG_REMOVED
    }


    /**
     * Handler for update events in {@link FlagContainer flag containers}.
     */
    @FunctionalInterface public interface PlotFlagUpdateHandler {

        /**
         * Act on the flag update event
         *
         * @param plotFlag Plot flag
         * @param type     Update type
         */
        void handle(PlotFlag<?, ?> plotFlag, PlotFlagUpdateType type);

    }

}
