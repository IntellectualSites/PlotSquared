////////////////////////////////////////////////////////////////////////////////////////////////////
// PlotSquared - A plot manager and world generator for the Bukkit API                             /
// Copyright (c) 2014 IntellectualSites/IntellectualCrafters                                       /
//                                                                                                 /
// This program is free software; you can redistribute it and/or modify                            /
// it under the terms of the GNU General Public License as published by                            /
// the Free Software Foundation; either version 3 of the License, or                               /
// (at your option) any later version.                                                             /
//                                                                                                 /
// This program is distributed in the hope that it will be useful,                                 /
// but WITHOUT ANY WARRANTY; without even the implied warranty of                                  /
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the                                   /
// GNU General Public License for more details.                                                    /
//                                                                                                 /
// You should have received a copy of the GNU General Public License                               /
// along with this program; if not, write to the Free Software Foundation,                         /
// Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301  USA                               /
//                                                                                                 /
// You can contact us via: support@intellectualsites.com                                           /
////////////////////////////////////////////////////////////////////////////////////////////////////

package com.intellectualcrafters.plot;

import java.util.ArrayList;
import java.util.Collections;

/**
 * String comparsion library
 *
 * @author Citymonstret
 */
public class StringComparsion {

    private String bestMatch;
    private double match = 0;
    private Object bestMatchObject;

    public StringComparsion(final String input, final Object[] objects) {
        double c = 0;
        this.bestMatch = objects[0].toString();
        this.bestMatchObject = objects[0];
        for (final Object o : objects) {
            if ((c = compare(input, o.toString())) > this.match) {
                this.match = c;
                this.bestMatch = o.toString();
                this.bestMatchObject = o;
            }
        }
    }

    public Object getMatchObject() {
        return this.bestMatchObject;
    }

    public String getBestMatch() {
        return this.bestMatch;
    }

    public Object[] getBestMatchAdvanced() {
        return new Object[]{this.match, this.bestMatch};
    }

    public static double compare(final String s1, final String s2) {
        final ArrayList p1 = wLetterPair(s1.toUpperCase()), p2 = wLetterPair(s2.toUpperCase());
        int intersection = 0;
        final int union = p1.size() + p2.size();
        for (final Object aP1 : p1) {
            for (final Object aP2 : p2) {
                if (aP1.equals(aP2)) {
                    intersection++;
                    p2.remove(aP2);
                    break;
                }
            }
        }
        return (2.0 * intersection) / union;
    }

    public static ArrayList wLetterPair(final String s) {
        final ArrayList<String> aPairs = new ArrayList<>();
        final String[] wo = s.split("\\s");
        for (final String aWo : wo) {
            final String[] po = sLetterPair(aWo);
            Collections.addAll(aPairs, po);
        }
        return aPairs;
    }

    public static String[] sLetterPair(final String s) {
        final int numPair = s.length() - 1;
        final String[] p = new String[numPair];
        for (int i = 0; i < numPair; i++) {
            p[i] = s.substring(i, i + 2);
        }
        return p;
    }

}
