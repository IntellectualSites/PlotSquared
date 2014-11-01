package com.intellectualcrafters.plot;

import java.util.ArrayList;
import java.util.Collections;

/**
 * String comparsion library
 * @author Citymonstret
 */
public class StringComparsion {

    private String bestMatch;
    private double match;

    public StringComparsion(String input, Object[] objects) {
        double c = 0;
        for(Object o : objects) {
            if((c = compare(input, o.toString())) > match) {
                match = c;
                bestMatch = o.toString();
            }
        }
    }

    public String getBestMatch() {
        return this.bestMatch;
    }

    public Object[] getBestMatchAdvanced() {
        return new Object[] {
                match,
                bestMatch
        };
    }

    public static double compare(String s1, String s2) {
        ArrayList p1 = wLetterPair(s1.toUpperCase()),
                p2 = wLetterPair(s2.toUpperCase());
        int intersection = 0, union = p1.size() + p2.size();
        for (Object aP1 : p1) {
            for(Object aP2 : p2) {
                if(aP1.equals(aP2)) {
                    intersection++;
                    p2.remove(aP2);
                    break;
                }
            }
        }
        return (2.0 * intersection) / union;
    }

    public static ArrayList wLetterPair(String s) {
        ArrayList<String> aPairs = new ArrayList<>();
        String[] wo = s.split("\\s");
        for (String aWo : wo) {
            String[] po = sLetterPair(aWo);
            Collections.addAll(aPairs, po);
        }
        return aPairs;
    }

    public static String[] sLetterPair(String s) {
        int numPair = s.length() - 1;
        String[] p = new String[numPair];
        for(int i = 0; i < numPair; i++)
            p[i] = s.substring(i, i + 2);
        return p;
    }

}
