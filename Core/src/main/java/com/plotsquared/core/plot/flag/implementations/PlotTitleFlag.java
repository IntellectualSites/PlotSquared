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
package com.plotsquared.core.plot.flag.implementations;

import com.plotsquared.core.configuration.caption.TranslatableCaption;
import com.plotsquared.core.plot.PlotTitle;
import com.plotsquared.core.plot.flag.FlagParseException;
import com.plotsquared.core.plot.flag.PlotFlag;
import com.plotsquared.core.util.StringMan;
import org.checkerframework.checker.nullness.qual.NonNull;

public class PlotTitleFlag extends PlotFlag<PlotTitle, PlotTitleFlag> {

    public static final PlotTitleFlag TITLE_FLAG_DEFAULT = new PlotTitleFlag(PlotTitle.CONFIGURED);

    /**
     * Construct a new flag instance.
     *
     * @param value Flag value
     */
    protected PlotTitleFlag(PlotTitle value) {
        super(
                value,
                TranslatableCaption.of("flags.flag_category_string"),
                TranslatableCaption.of("flags.flag_description_title")
        );
    }

    @Override
    public PlotTitleFlag parse(@NonNull String input) throws FlagParseException {
        if (input.equals("CONFIGURED")) {
            return TITLE_FLAG_DEFAULT;
        }
        if (!input.contains("\"")) {
            return new PlotTitleFlag(new PlotTitle(input, ""));
        }

        var split = StringMan.splitMessage(input);

        if (split.isEmpty() || split.size() > 2) {
            throw new FlagParseException(this, input, TranslatableCaption.of("flags.flag_error_title"));
        }
        PlotTitle value;
        if (split.size() == 1) {
            value = new PlotTitle(split.get(0), "");
        } else {
            value = new PlotTitle(split.get(0), split.get(1));
        }
        return new PlotTitleFlag(value);
    }

    @Override
    public PlotTitleFlag merge(@NonNull PlotTitle newValue) {
        if (getValue().title().isEmpty() && getValue().subtitle().isEmpty()) {
            return new PlotTitleFlag(newValue);
        } else if (getValue().subtitle().isEmpty()) {
            return new PlotTitleFlag(new PlotTitle(getValue().title(), newValue.subtitle()));
        } else if (getValue().title().isEmpty()) {
            return new PlotTitleFlag(new PlotTitle(newValue.title(), getValue().subtitle()));
        } else {
            return this;
        }
    }

    @Override
    public String toString() {
        if (getValue() == PlotTitle.CONFIGURED) {
            return "CONFIGURED";
        }
        return "\"" + getValue().title() + "\" \"" + getValue().subtitle() + "\"";
    }

    @Override
    public boolean isValuedPermission() {
        return false;
    }

    @Override
    public String getExample() {
        return "\"A Title\" \"The subtitle\"";
    }

    @Override
    protected PlotTitleFlag flagOf(@NonNull PlotTitle value) {
        return new PlotTitleFlag(value);
    }

}
