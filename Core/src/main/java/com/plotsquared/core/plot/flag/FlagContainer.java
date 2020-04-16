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
package com.plotsquared.core.plot.flag;

import com.google.common.collect.ImmutableMap;
import lombok.EqualsAndHashCode;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Container type for {@link PlotFlag plot flags}.
 */
@EqualsAndHashCode(of = "flagMap")
public class FlagContainer {

    private final Map<String, String> unknownFlags = new HashMap<>();
    private final Map<Class<?>, PlotFlag<?, ?>> flagMap = new HashMap<>();
    private final PlotFlagUpdateHandler plotFlagUpdateHandler;
    private final Collection<PlotFlagUpdateHandler> updateSubscribers = new ArrayList<>();
    @Setter private FlagContainer parentContainer;

    /**
     * Construct a new flag container with an optional parent container and update handler.
     * Default values are inherited from the parent container. At the top
     * of the parent-child hierarchy must be the {@link GlobalFlagContainer}
     * (or an equivalent top level flag container).
     *
     * @param parentContainer       Parent container. The top level flag container should not have a parent,
     *                              and can set this parameter to null. If this is not a top level
     *                              flag container, the parent should not be null.
     * @param plotFlagUpdateHandler Event handler that will be called whenever a plot flag is
     *                              added, removed or updated in this flag container.
     */
    public FlagContainer(@Nullable final FlagContainer parentContainer,
        @Nullable PlotFlagUpdateHandler plotFlagUpdateHandler) {
        this.parentContainer = parentContainer;
        this.plotFlagUpdateHandler = plotFlagUpdateHandler;
        if (!(this instanceof GlobalFlagContainer)) {
            GlobalFlagContainer.getInstance().subscribe(this::handleUnknowns);
        }
    }

    /**
     * Construct a new flag container with an optional parent container.
     * Default values are inherited from the parent container. At the top
     * of the parent-child hierarchy must be the {@link GlobalFlagContainer}
     * (or an equivalent top level flag container).
     *
     * @param parentContainer Parent container. The top level flag container should not have a parent,
     *                        and can set this parameter to null. If this is not a top level
     *                        flag container, the parent should not be null.
     */
    public FlagContainer(@Nullable final FlagContainer parentContainer) {
        this(parentContainer, null);
    }

    /**
     * Cast a plot flag with wildcard parameters into a parametrisized
     * PlotFlag. This is an unsafe operation, and should only be performed
     * if the generic parameters are known beforehand.
     *
     * @param flag Flag instance
     * @param <V>  Flag value type
     * @param <T>  Flag type
     * @return Casted flag
     */
    @SuppressWarnings("ALL") public static <V, T extends PlotFlag<V, ?>> T castUnsafe(
        final PlotFlag<?, ?> flag) {
        return (T) flag;
    }

    /**
     * Return the parent container (if the container has a parent)
     *
     * @return Parent container, if it exists
     */
    @Nullable public FlagContainer getParentContainer() {
        return this.parentContainer;
    }

    @SuppressWarnings("unused") protected Map<Class<?>, PlotFlag<?, ?>> getInternalPlotFlagMap() {
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
        final PlotFlag<?, ?> oldInstance = this.flagMap.put(flag.getClass(), flag);
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
        this.updateSubscribers
            .forEach(subscriber -> subscriber.handle(flag, PlotFlagUpdateType.FLAG_REMOVED));
        if (value == null) {
            return null;
        } else {
            return (V) value;
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
     * with wildcard generic types.
     *
     * @param flagClass The {@link PlotFlag} class.
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
    @Nullable public <V, T extends PlotFlag<V, ?>> T queryLocal(final Class<?> flagClass) {
        final PlotFlag<?, ?> localFlag = this.flagMap.get(flagClass);
        if (localFlag == null) {
            return null;
        } else {
            return castUnsafe(localFlag);
        }
    }

    /**
     * Subscribe to flag updates in this particular flag container instance.
     * Updates are: a flag being removed, a flag being added or a flag
     * being updated.
     *
     * @param plotFlagUpdateHandler The update handler which will react to changes.
     * @see PlotFlagUpdateType Plot flag update types
     */
    public void subscribe(@NotNull final PlotFlagUpdateHandler plotFlagUpdateHandler) {
        this.updateSubscribers.add(plotFlagUpdateHandler);
    }

    private void handleUnknowns(final PlotFlag<?, ?> flag,
        final PlotFlagUpdateType plotFlagUpdateType) {
        if (plotFlagUpdateType != PlotFlagUpdateType.FLAG_REMOVED && this.unknownFlags
            .containsKey(flag.getName())) {
            final String value = this.unknownFlags.remove(flag.getName());
            if (value != null) {
                try {
                    this.addFlag(flag.parse(value));
                } catch (final Exception ignored) {
                }
            }
        }
    }

    /**
     * Register a flag key-value pair which cannot yet be associated with
     * an existing flag instance (such as when third party flag values are
     * loaded before the flag type has been registered).
     * <p>
     * These values will be registered in the flag container if the associated
     * flag type is registered in the top level flag container.
     *
     * @param flagName Flag name
     * @param value    Flag value
     */
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
    @FunctionalInterface
    public interface PlotFlagUpdateHandler {

        /**
         * Act on the flag update event
         *
         * @param plotFlag Plot flag
         * @param type     Update type
         */
        void handle(PlotFlag<?, ?> plotFlag, PlotFlagUpdateType type);

    }

}
