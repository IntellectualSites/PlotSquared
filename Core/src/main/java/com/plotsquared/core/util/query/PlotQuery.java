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
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.plotsquared.core.util.query;

import com.google.common.base.Preconditions;
import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.PlotArea;
import com.plotsquared.core.plot.Rating;
import com.plotsquared.core.plot.flag.implementations.DoneFlag;
import com.plotsquared.core.plot.world.PlotAreaManager;
import com.plotsquared.core.util.MathMan;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * This represents a plot query, and can be used to
 * search for plots matching certain criteria.
 * <p>
 * The queries can be reused as no results are stored
 * in the query itself
 */
public final class PlotQuery implements Iterable<Plot> {

    private final Collection<PlotFilter> filters = new LinkedList<>();
    private final PlotAreaManager plotAreaManager;
    private PlotProvider plotProvider;
    private SortingStrategy sortingStrategy = SortingStrategy.NO_SORTING;
    private PlotArea priorityArea;
    private Comparator<Plot> plotComparator;

    private PlotQuery(@Nonnull final PlotAreaManager plotAreaManager) {
        this.plotAreaManager = plotAreaManager;
        this.plotProvider = new GlobalPlotProvider(plotAreaManager);
    }

    /**
     * Create a new plot query instance
     *
     * @return New query
     */
    public static PlotQuery newQuery() {
        return new PlotQuery(PlotSquared.get().getPlotAreaManager());
    }

    /**
     * Query for plots in a single area
     *
     * @param area Area
     * @return The query instance
     */
    @Nonnull public PlotQuery inArea(@Nonnull final PlotArea area) {
        Preconditions.checkNotNull(area, "Area may not be null");
        this.plotProvider = new AreaLimitedPlotProvider(Collections.singletonList(area));
        return this;
    }

    /**
     * Query for plots in all areas in a world
     *
     * @param world World name
     * @return The query instance
     */
    @Nonnull public PlotQuery inWorld(@Nonnull final String world) {
        Preconditions.checkNotNull(world, "World may not be null");
        this.plotProvider = new AreaLimitedPlotProvider(this.plotAreaManager.getPlotAreasSet(world));
        return this;
    }

    /**
     * Query for plots in specific areas
     *
     * @param areas Plot areas
     * @return The query instance
     */
    @Nonnull public PlotQuery inAreas(@Nonnull final Collection<PlotArea> areas) {
        Preconditions.checkNotNull(areas, "Areas may not be null");
        Preconditions.checkState(!areas.isEmpty(), "At least one area must be provided");
        this.plotProvider = new AreaLimitedPlotProvider(Collections.unmodifiableCollection(areas));
        return this;
    }

    /**
     * Query for expired plots
     *
     * @return The query instance
     */
    @Nonnull public PlotQuery expiredPlots() {
        this.plotProvider = new ExpiredPlotProvider();
        return this;
    }

    /**
     * Query for all plots
     *
     * @return The query instance
     */
    @Nonnull public PlotQuery allPlots() {
        this.plotProvider = new GlobalPlotProvider(this.plotAreaManager);
        return this;
    }

    /**
     * Don't query at all
     *
     * @return The query instance
     */
    @Nonnull public PlotQuery noPlots() {
        this.plotProvider = new NullProvider();
        return this;
    }

    /**
     * Query for plots based on a search term
     *
     * @param searchTerm search term to use (uuid, plotID, username)
     *
     * @return The query instance
     */
    @Nonnull public PlotQuery plotsBySearch(@Nonnull final String searchTerm) {
        Preconditions.checkNotNull(searchTerm, "Search term may not be null");
        this.plotProvider = new SearchPlotProvider(searchTerm);
        return this;
    }

    /**
     * Query with a pre-defined result
     *
     * @param plot to return when Query is searched
     *
     * @return The query instance
     */
    @Nonnull public PlotQuery withPlot(@Nonnull final Plot plot) {
        Preconditions.checkNotNull(plot, "Plot may not be null");
        this.plotProvider = new FixedPlotProvider(plot);
        return this;
    }

    /**
     * Query for base plots only
     *
     * @return The query instance
     */
    @Nonnull public PlotQuery whereBasePlot() {
        return this.addFilter(new PredicateFilter(Plot::isBasePlot));
    }

    /**
     * Query for plots owned by a specific player
     *
     * @param owner Owner UUID
     * @return The query instance
     */
    @Nonnull public PlotQuery ownedBy(@Nonnull final UUID owner) {
        Preconditions.checkNotNull(owner, "Owner may not be null");
        return this.addFilter(new OwnerFilter(owner));
    }

    /**
     * Query for plots owned by a specific player
     *
     * @param owner Owner
     * @return The query instance
     */
    @Nonnull public PlotQuery ownedBy(@Nonnull final PlotPlayer owner) {
        Preconditions.checkNotNull(owner, "Owner may not be null");
        return this.addFilter(new OwnerFilter(owner.getUUID()));
    }

    /**
     * Query for plots with a specific alias
     *
     * @param alias Plot alias
     * @return The query instance
     */
    @Nonnull public PlotQuery withAlias(@Nonnull final String alias) {
        Preconditions.checkNotNull(alias, "Alias may not be null");
        return this.addFilter(new AliasFilter(alias));
    }

    /**
     * Query for plots with a specific member (added/trusted/owner)
     *
     * @param member Member UUID
     * @return The query instance
     */
    @Nonnull public PlotQuery withMember(@Nonnull final UUID member) {
        Preconditions.checkNotNull(member, "Member may not be null");
        return this.addFilter(new MemberFilter(member));
    }

    /**
     * Query for plots that passes a given predicate
     *
     * @param predicate Predicate
     * @return The query instance
     */
    @Nonnull public PlotQuery thatPasses(@Nonnull final Predicate<Plot> predicate) {
        Preconditions.checkNotNull(predicate, "Predicate may not be null");
        return this.addFilter(new PredicateFilter(predicate));
    }

    /**
     * Specify the sorting strategy that will decide how to
     * sort the results. This only matters if you use {@link #asList()}
     *
     * @param strategy Strategy
     * @return The query instance
     */
    @Nonnull public PlotQuery withSortingStrategy(@Nonnull final SortingStrategy strategy) {
        Preconditions.checkNotNull(strategy, "Strategy may not be null");
        this.sortingStrategy = strategy;
        return this;
    }

    /**
     * Use a custom comparator to sort the results
     *
     * @param comparator Comparator
     * @return The query instance
     */
    @Nonnull public PlotQuery sorted(@Nonnull final Comparator<Plot> comparator) {
        Preconditions.checkNotNull(comparator, "Comparator may not be null");
        this.sortingStrategy = SortingStrategy.COMPARATOR;
        this.plotComparator = comparator;
        return this;
    }

    /**
     * Defines the area around which plots may be sorted, depending on the
     * sorting strategy
     *
     * @param plotArea Plot area
     * @return The query instance
     */
    @Nonnull public PlotQuery relativeToArea(@Nonnull final PlotArea plotArea) {
        Preconditions.checkNotNull(plotArea, "Area may not be null");
        this.priorityArea = plotArea;
        return this;
    }

    /**
     * Get all plots that match the given criteria
     *
     * @return Matching plots
     */
    @Nonnull public Stream<Plot> asStream() {
        return this.asList().stream();
    }

    /**
     * Get all plots that match the given criteria
     *
     * @return Matching plots as a mutable
     */
    @Nonnull public List<Plot> asList() {
        final List<Plot> result;
        if (this.filters.isEmpty()) {
            result = new ArrayList<>(this.plotProvider.getPlots());
        } else {
            final Collection<Plot> plots = this.plotProvider.getPlots();
            result = new ArrayList<>(plots.size());
            outer: for (final Plot plot : plots) {
                for (final PlotFilter filter : this.filters) {
                    if (!filter.accepts(plot)) {
                        continue outer;
                    }
                }
                result.add(plot);
            }
        }
        if (this.sortingStrategy == SortingStrategy.NO_SORTING) {
            return result;
        } else if (this.sortingStrategy == SortingStrategy.SORT_BY_TEMP) {
            return PlotSquared.get().sortPlotsByTemp(result);
        } else if (this.sortingStrategy == SortingStrategy.SORT_BY_DONE) {
            result.sort((a, b) -> {
                String va = a.getFlag(DoneFlag.class);
                String vb = b.getFlag(DoneFlag.class);
                if (MathMan.isInteger(va)) {
                    if (MathMan.isInteger(vb)) {
                        return Integer.parseInt(vb) - Integer.parseInt(va);
                    }
                    return -1;
                }
                return 1;
            });
        } else if (this.sortingStrategy == SortingStrategy.SORT_BY_RATING) {
            result.sort((p1, p2) -> {
                double v1 = 0;
                int p1s = p1.getSettings().getRatings().size();
                int p2s = p2.getRatings().size();
                if (!p1.getSettings().getRatings().isEmpty()) {
                    v1 = p1.getRatings().values().stream().mapToDouble(Rating::getAverageRating)
                        .map(av -> av * av).sum();
                    v1 /= p1s;
                    v1 += p1s;
                }
                double v2 = 0;
                if (!p2.getSettings().getRatings().isEmpty()) {
                    for (Map.Entry<UUID, Rating> entry : p2.getRatings().entrySet()) {
                        double av = entry.getValue().getAverageRating();
                        v2 += av * av;
                    }
                    v2 /= p2s;
                    v2 += p2s;
                }
                if (v2 == v1 && v2 != 0) {
                    return p2s - p1s;
                }
                return (int) Math.signum(v2 - v1);
            });
        } else if (this.sortingStrategy == SortingStrategy.SORT_BY_CREATION) {
            return PlotSquared.get().sortPlots(result, PlotSquared.SortType.CREATION_DATE, this.priorityArea);
        } else if (this.sortingStrategy == SortingStrategy.COMPARATOR) {
            result.sort(this.plotComparator);
        }
        return result;
    }

    /**
     * Get all plots that match the given criteria
     *
     * @return Matching plots as a mutable set
     */
    @Nonnull public Set<Plot> asSet() {
        return new HashSet<>(this.asList());
    }

    /**
     * Get all plots that match the given criteria
     * in the form of a {@link PaginatedPlotResult}
     *
     * @param pageSize The size of the pages. Must be positive.
     * @return Paginated plot result
     */
    @Nonnull public PaginatedPlotResult getPaginated(final int pageSize) {
        Preconditions.checkState(pageSize > 0, "Page size must be greater than 0");
        return new PaginatedPlotResult(this.asList(), pageSize);
    }

    /**
     * Get all plots that match the given criteria
     *
     * @return Matching plots as an immutable collection
     */
    @Nonnull public Collection<Plot> asCollection() {
        return this.asList();
    }

    /**
     * Get the amount of plots contained in the query result
     *
     * @return Result count
     */
    public int count() {
        return this.asList().size();
    }

    /**
     * Get whether any provided plot matches the given filters.
     * If no plot was provided, false will be returned.
     *
     * @return true if any provided plot matches the filters.
     */
    public boolean anyMatch() {
        if (this.filters.isEmpty()) {
            return !this.plotProvider.getPlots().isEmpty();
        } else {
            final Collection<Plot> plots = this.plotProvider.getPlots();
            outer: for (final Plot plot : plots) {
                // a plot must pass all filters to match the criteria
                for (final PlotFilter filter : this.filters) {
                    if (!filter.accepts(plot)) {
                        continue outer;
                    }
                }
                return true; // a plot passed all filters, so we have a match
            }
            return false;
        }
    }

    @Nonnull private PlotQuery addFilter(@Nonnull final PlotFilter filter) {
        this.filters.add(filter);
        return this;
    }

    @Nonnull @Override public Iterator<Plot> iterator() {
        return this.asCollection().iterator();
    }

}
