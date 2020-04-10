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
package com.github.intellectualsites.plotsquared.plot.util.world;

import com.github.intellectualsites.plotsquared.plot.util.MathMan;
import com.sk89q.worldedit.world.item.ItemType;
import com.sk89q.worldedit.world.item.ItemTypes;
import com.sk89q.worldedit.world.registry.LegacyMapper;

import java.util.Locale;

public final class ItemUtil {
    private ItemUtil() {
    }

    public static ItemType get(String input) {
        if (input == null || input.isEmpty()) {
            return ItemTypes.AIR;
        }
        input = input.toLowerCase(Locale.ROOT);
        if (Character.isDigit(input.charAt(0))) {
            String[] split = input.split(":");
            if (MathMan.isInteger(split[0])) {
                if (split.length == 2) {
                    if (MathMan.isInteger(split[1])) {
                        return LegacyMapper.getInstance()
                            .getItemFromLegacy(Integer.parseInt(split[0]),
                                Integer.parseInt(split[1]));
                    }
                } else {
                    return LegacyMapper.getInstance().getItemFromLegacy(Integer.parseInt(split[0]));
                }
            }
        }
        if (!input.split("\\[", 2)[0].contains(":"))
            input = "minecraft:" + input;
        return ItemTypes.get(input);
    }

    public static final ItemType[] parse(String commaDelimited) {
        String[] split = commaDelimited.split(",(?![^\\(\\[]*[\\]\\)])");
        ItemType[] result = new ItemType[split.length];
        for (int i = 0; i < split.length; i++) {
            result[i] = get(split[i]);
        }
        return result;
    }
}
