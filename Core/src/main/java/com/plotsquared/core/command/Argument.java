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
package com.plotsquared.core.command;

import com.plotsquared.core.plot.PlotId;

public abstract class Argument<T> {

    public static final Argument<Integer> Integer = new Argument<>("int", 16) {
        @Override
        public Integer parse(String in) {
            Integer value = null;
            try {
                value = java.lang.Integer.parseInt(in);
            } catch (Exception ignored) {
            }
            return value;
        }
    };
    public static final Argument<Boolean> Boolean = new Argument<>("boolean", true) {
        @Override
        public Boolean parse(String in) {
            Boolean value = null;
            if (in.equalsIgnoreCase("true") || in.equalsIgnoreCase("Yes") || in
                    .equalsIgnoreCase("1")) {
                value = true;
            } else if (in.equalsIgnoreCase("false") || in.equalsIgnoreCase("No") || in
                    .equalsIgnoreCase("0")) {
                value = false;
            }
            return value;
        }
    };
    public static final Argument<String> String = new Argument<>("String", "Example") {
        @Override
        public String parse(String in) {
            return in;
        }
    };
    public static final Argument<String> PlayerName =
            new Argument<>("PlayerName", "<player | *>") {
                @Override
                public String parse(String in) {
                    return in.length() <= 16 ? in : null;
                }
            };
    public static final Argument<PlotId> PlotID =
            new Argument<>("PlotID", PlotId.of(-6, 3)) {
                @Override
                public PlotId parse(String in) {
                    return PlotId.fromString(in);
                }
            };
    private final String name;
    private final T example;

    public Argument(String name, T example) {
        this.name = name;
        this.example = example;
    }

    public abstract T parse(String in);

    @Override
    public final String toString() {
        return this.getName();
    }

    public final String getName() {
        return this.name;
    }

    public final T getExample() {
        return this.example;
    }

}
