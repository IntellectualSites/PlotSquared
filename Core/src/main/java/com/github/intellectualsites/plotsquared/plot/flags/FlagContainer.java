package com.github.intellectualsites.plotsquared.plot.flags;

import com.google.common.collect.ImmutableMap;
import lombok.EqualsAndHashCode;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Container type for {@link PlotFlag plot flags}.
 */
@EqualsAndHashCode(of = "flagMap") @SuppressWarnings("unused") public class FlagContainer {

    @Setter private FlagContainer parentContainer;
    private final Map<String, String> unknownFlags = new HashMap<>();
    private final Map<Class<?>, PlotFlag<?, ?>> flagMap = new HashMap<>();
    private final PlotFlagUpdateHandler plotFlagUpdateHandler;
    private final Collection<PlotFlagUpdateHandler> updateSubscribers = new ArrayList<>();

    public FlagContainer(@Nullable final FlagContainer parentContainer,
        @Nullable PlotFlagUpdateHandler plotFlagUpdateHandler) {
        this.parentContainer = parentContainer;
        this.plotFlagUpdateHandler = plotFlagUpdateHandler;
        if (!(this instanceof GlobalFlagContainer)) {
            GlobalFlagContainer.getInstance().subscribe(this::handleUnknowns);
        }
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
        final PlotFlag<?,?> oldInstance = this.flagMap.put(flag.getClass(), flag);
        final PlotFlagUpdateType plotFlagUpdateType;
        if (oldInstance != null) {
            plotFlagUpdateType = PlotFlagUpdateType.FLAG_UPDATED;
        } else {
            plotFlagUpdateType = PlotFlagUpdateType.FLAG_ADDED;
        }
        if (this.plotFlagUpdateHandler != null) {
            this.plotFlagUpdateHandler.handle(flag, plotFlagUpdateType);
        }
        this.updateSubscribers.forEach(subscriber -> subscriber.handle(flag, plotFlagUpdateType));
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
        this.updateSubscribers.forEach(subscriber -> subscriber.handle(flag, PlotFlagUpdateType.FLAG_REMOVED));
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

    public void addAll(final FlagContainer container) {
        this.addAll(container.flagMap.values());
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

    public void subscribe(final PlotFlagUpdateHandler plotFlagUpdateHandler) {
        this.updateSubscribers.add(plotFlagUpdateHandler);
    }

    private void handleUnknowns(final PlotFlag<?, ?> flag, final PlotFlagUpdateType plotFlagUpdateType) {
        if (plotFlagUpdateType != PlotFlagUpdateType.FLAG_REMOVED && this.unknownFlags.containsKey(flag.getName())) {
            final String value = this.unknownFlags.remove(flag.getName());
            if (value != null) {
                try {
                    this.addFlag(flag.parse(value));
                } catch (final Exception ignored) {
                }
            }
        }
    }

    public void addUnknownFlag(final String flagName, final String value) {
        this.unknownFlags.put(flagName.toLowerCase(Locale.ENGLISH), value);
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
        FLAG_REMOVED,
        /**
         * A flag was already stored in this container,
         * but a new instance has bow replaced it
         */
        FLAG_UPDATED
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
