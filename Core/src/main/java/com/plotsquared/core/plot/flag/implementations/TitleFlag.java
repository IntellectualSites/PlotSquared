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
 *                  Copyright (C) 2021 IntellectualSites
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
package com.plotsquared.core.plot.flag.implementations;

import com.plotsquared.core.configuration.caption.TranslatableCaption;
import com.plotsquared.core.plot.flag.FlagParseException;
import com.plotsquared.core.plot.flag.PlotFlag;
import com.plotsquared.core.util.ItemUtil;
import com.sk89q.worldedit.world.item.ItemType;
import com.sk89q.worldedit.world.item.ItemTypes;
import org.checkerframework.checker.nullness.qual.NonNull;

public class TitleFlag extends PlotFlag<String[], TitleFlag> {

    public static final TitleFlag TITLE_FLAG_EMPTY = new TitleFlag(new String[0]);

    /**
     * Construct a new flag instance.
     *
     * @param value Flag value
     */
    protected TitleFlag(String[] value) {
        super(value, TranslatableCaption.of("flags.flag_category_string"), TranslatableCaption.of("flags.flag_description_title"));
    }

    @Override
    public TitleFlag parse(@NonNull String input) throws FlagParseException {
        if (!input.contains("\"")) {
            throw new FlagParseException(this, input, TranslatableCaption.of("flags.flag_error_title"));
        }
        input = input.substring(input.indexOf("\""));
        input = input.substring(0, input.lastIndexOf("\"") + 1);
        String[] inputs = input.split("\"");
        String[] value;
        if (inputs.length == 2) {
            value = new String[]{inputs[1], ""};
        } else if (inputs.length > 3) {
            value = new String[]{inputs[1], inputs[3]};
        } else {
            throw new FlagParseException(this, input, TranslatableCaption.of("flags.flag_error_title"));
        }
        return new TitleFlag(value);
    }

    @Override
    public TitleFlag merge(@NonNull String[] newValue) {
        if (getValue()[0].isEmpty() && getValue()[1].isEmpty()) {
            return new TitleFlag(newValue);
        } else if (getValue()[1].isEmpty()) {
            return new TitleFlag(new String[]{getValue()[0], newValue[1]});
        } else if (getValue()[0].isEmpty()) {
            return new TitleFlag(new String[]{newValue[0], getValue()[1]});
        } else {
            return this;
        }
    }

    @Override
    public String toString() {
        return getValue()[0] + getValue()[1];
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
    protected TitleFlag flagOf(@NonNull String[] value) {
        return new TitleFlag(value);
    }

}
