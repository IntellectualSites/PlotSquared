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
package com.plotsquared.core.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.function.Function;

/**
 * String comparison library.
 */
public class StringComparison<T> {

    private final Function<T, String> toString;
    private T bestMatch;
    private double match = Integer.MAX_VALUE;
    private T bestMatchObject;

    /**
     * Constructor
     *
     * @param input   Input Base Value
     * @param objects Objects to compare
     */
    public StringComparison(String input, T[] objects) {
        this(input, objects, Object::toString);
    }

    public StringComparison(String input, T[] objects, Function<T, String> toString) {
        this.toString = toString;
        init(input, objects);
    }

    public StringComparison(String input, Collection<T> objects) {
        this(input, objects, Object::toString);
    }

    @SuppressWarnings("unchecked")
    public StringComparison(String input, Collection<T> objects, Function<T, String> toString) {
        this(input, (T[]) objects.toArray(), toString);
    }

    /**
     * You should call init(...) when you are ready to get a String comparison value.
     */
    public StringComparison() {
        this.toString = Object::toString;
    }

    /**
     * Compare two strings
     *
     * @param s1 String Base
     * @param s2 Object
     * @return match
     */
    public static int compare(String s1, String s2) {
        int distance = StringMan.getLevenshteinDistance(s1, s2);
        if (s2.contains(s1)) {
            distance -= Math.min(s1.length(), s2.length());
        }
        if (s2.startsWith(s1)) {
            distance -= 4;
        }
        return distance;
    }

    /**
     * Create an ArrayList containing pairs of letters
     *
     * @param s string to split
     * @return ArrayList
     */
    public static ArrayList<String> wLetterPair(String s) {
        ArrayList<String> aPairs = new ArrayList<>();
        String[] wo = s.split("\\s");
        for (String aWo : wo) {
            String[] po = sLetterPair(aWo);
            Collections.addAll(aPairs, po);
        }
        return aPairs;
    }

    /**
     * Get an array containing letter pairs
     *
     * @param s string to split
     * @return Array
     */
    public static String[] sLetterPair(String s) {
        int numPair = s.length() - 1;
        String[] p = new String[numPair];
        for (int i = 0; i < numPair; i++) {
            p[i] = s.substring(i, i + 2);
        }
        return p;
    }

    public void init(String input, T[] objects) {
        int c;
        this.bestMatch = objects[0];
        this.bestMatchObject = objects[0];
        input = input.toLowerCase();
        for (T o : objects) {
            if ((c = compare(input, getString(o).toLowerCase())) < this.match) {
                this.match = c;
                this.bestMatch = o;
                this.bestMatchObject = o;
            }
        }
    }

    public String getString(T o) {
        return this.toString.apply(o);
    }

    /**
     * Get the object
     *
     * @return match object
     */
    public T getMatchObject() {
        return this.bestMatchObject;
    }

    /**
     * Get the best match value
     *
     * @return match value
     */
    public String getBestMatch() {
        return getString(this.bestMatch);
    }

    /**
     * Will return both the match number, and the actual match string
     *
     * @return object[] containing: double, String
     */
    public ComparisonResult getBestMatchAdvanced() {
        return new ComparisonResult(this.match, this.bestMatch);
    }


    /**
     * The comparison result
     */
    public class ComparisonResult {

        public final T best;
        public final double match;

        /**
         * The constructor
         *
         * @param match Match value
         * @param best  Best Match
         */
        public ComparisonResult(double match, T best) {
            this.match = match;
            this.best = best;
        }

    }

}
