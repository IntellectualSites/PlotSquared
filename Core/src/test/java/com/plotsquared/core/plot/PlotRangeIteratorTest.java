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
package com.plotsquared.core.plot;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

public class PlotRangeIteratorTest {

    @Test
    public void singlePlotIterator() {
        // an iterator that should only contain the given plot
        PlotId id = PlotId.of(3, 7);
        PlotId.PlotRangeIterator range = PlotId.PlotRangeIterator.range(id, id);
        Assertions.assertTrue(range.hasNext());
        Assertions.assertEquals(id, range.next());
        Assertions.assertFalse(range.hasNext());
        Assertions.assertThrows(NoSuchElementException.class, range::next);
    }

    // the tests below assume a specific order (first increasing y, then increasing x)
    // this is not a written requirement but makes testing easier

    @Test
    public void squareAreaPlotIterator() {
        PlotId id00 = PlotId.of(0, 0);
        PlotId id01 = PlotId.of(0, 1);
        PlotId id10 = PlotId.of(1, 0);
        PlotId id11 = PlotId.of(1, 1);
        List<PlotId> all = Arrays.asList(id00, id01, id10, id11);
        PlotId.PlotRangeIterator range = PlotId.PlotRangeIterator.range(id00, id11);
        for (PlotId id : all) {
            Assertions.assertTrue(range.hasNext());
            Assertions.assertEquals(id, range.next());
        }
        Assertions.assertFalse(range.hasNext());
        Assertions.assertThrows(NoSuchElementException.class, range::next);
    }

    @Test
    public void rectangleYAreaPlotIterator() {
        // max y > max x
        PlotId id00 = PlotId.of(0, 0);
        PlotId id01 = PlotId.of(0, 1);
        PlotId id02 = PlotId.of(0, 2);
        PlotId id10 = PlotId.of(1, 0);
        PlotId id11 = PlotId.of(1, 1);
        PlotId id12 = PlotId.of(1, 2);
        List<PlotId> all = Arrays.asList(id00, id01, id02, id10, id11, id12);
        PlotId.PlotRangeIterator range = PlotId.PlotRangeIterator.range(id00, id12);
        for (PlotId id : all) {
            Assertions.assertTrue(range.hasNext());
            Assertions.assertEquals(id, range.next());
        }
        Assertions.assertFalse(range.hasNext());
        Assertions.assertThrows(NoSuchElementException.class, range::next);
    }

    @Test
    public void rectangleXAreaPlotIterator() {
        // max x > max y
        PlotId id00 = PlotId.of(0, 0);
        PlotId id01 = PlotId.of(0, 1);
        PlotId id10 = PlotId.of(1, 0);
        PlotId id11 = PlotId.of(1, 1);
        PlotId id20 = PlotId.of(2, 0);
        PlotId id21 = PlotId.of(2, 1);
        List<PlotId> all = Arrays.asList(id00, id01, id10, id11, id20, id21);
        PlotId.PlotRangeIterator range = PlotId.PlotRangeIterator.range(id00, id21);
        for (PlotId id : all) {
            Assertions.assertTrue(range.hasNext());
            Assertions.assertEquals(id, range.next());
        }
        Assertions.assertFalse(range.hasNext());
        Assertions.assertThrows(NoSuchElementException.class, range::next);
    }

    @Test
    public void resetYOfIteratorToStart() {
        PlotId id00 = PlotId.of(0, 1);
        PlotId id01 = PlotId.of(1, 2);
        PlotId.PlotRangeIterator range = PlotId.PlotRangeIterator.range(id00, id01);

        for (int i = 0; i < 4; i++) {
            Assertions.assertNotEquals(0, range.next().getY());
        }
        Assertions.assertFalse(range.hasNext());
        Assertions.assertThrows(NoSuchElementException.class, range::next);
    }

}
