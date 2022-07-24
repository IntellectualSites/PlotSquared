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

import com.plotsquared.core.PlotVersion;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;

public class PlotVersionTest {

    private static final Logger LOGGER = LogManager.getLogger("PlotSquared/" + PlotVersionTest.class.getSimpleName());

    @Test
    public void tryParse() {
        //These are all random values chosen to form the test class.
        PlotVersion version = new PlotVersion("4.340", "f06903f", "19.08.05");
        LOGGER.info(version.versionString);

    }

}
