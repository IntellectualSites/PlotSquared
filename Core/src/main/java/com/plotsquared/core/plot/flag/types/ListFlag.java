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
package com.plotsquared.core.plot.flag.types;

import com.plotsquared.core.configuration.caption.Caption;
import com.plotsquared.core.plot.flag.PlotFlag;
import com.plotsquared.core.util.StringMan;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class ListFlag<V, F extends PlotFlag<List<V>, F>> extends PlotFlag<List<V>, F> {

    public ListFlag(final List<V> valueList, final Caption category, final Caption description) {
        super(Collections.unmodifiableList(valueList), category, description);
    }

    @Override
    public F merge(@NonNull List<V> newValue) {
        final List<V> mergedList = new ArrayList<>();
        // If a server already used PlotSquared before this fix, we remove all present duplicates on an eventual merge
        for (final V v : getValue()) {
            if (!mergedList.contains(v)) {
                mergedList.add(v);
            }
        }
        // Only add new values if not already present from #getValue()
        for (final V v : newValue) {
            if (!mergedList.contains(v)) {
                mergedList.add(v);
            }
        }
        return this.flagOf(mergedList);
    }

    @Override
    public String toString() {
        return StringMan.join(this.getValue(), ",");
    }

}
