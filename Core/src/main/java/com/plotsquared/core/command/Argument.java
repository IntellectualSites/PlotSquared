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
package com.plotsquared.core.command;

import com.plotsquared.core.plot.PlotId;

public abstract class Argument<T> {

    public static final Argument<Integer> Integer = new Argument<Integer>("int", 16) {
        @Override public Integer parse(String in) {
            Integer value = null;
            try {
                value = java.lang.Integer.parseInt(in);
            } catch (Exception ignored) {
            }
            return value;
        }
    };
    public static final Argument<Boolean> Boolean = new Argument<Boolean>("boolean", true) {
        @Override public Boolean parse(String in) {
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
    public static final Argument<String> String = new Argument<String>("String", "Example") {
        @Override public String parse(String in) {
            return in;
        }
    };
    public static final Argument<String> PlayerName =
        new Argument<String>("PlayerName", "<player|*>") {
            @Override public String parse(String in) {
                return in.length() <= 16 ? in : null;
            }
        };
    public static final Argument<PlotId> PlotID =
        new Argument<PlotId>("PlotID", PlotId.of(-6, 3)) {
            @Override public PlotId parse(String in) {
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

    @Override public final String toString() {
        return this.getName();
    }

    public final String getName() {
        return this.name;
    }

    public final T getExample() {
        return this.example;
    }
}
