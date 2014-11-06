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
        return new Object[] { this.match, this.bestMatch };
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
