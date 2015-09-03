package com.intellectualcrafters.plot.object;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import com.intellectualcrafters.plot.config.Settings;

public class Rating {
    
    /**
     * This is a map of the rating category to the rating value
     */
    private HashMap<String, Integer> ratingMap;
    
    private boolean changed;
    private int initial;
    
    public Rating(int value) {
        this.initial = value;
        ratingMap = new HashMap<>();
        if (value < 10) {
            for (int i = 0 ; i < Settings.RATING_CATEGORIES.size(); i++) {
                ratingMap.put(Settings.RATING_CATEGORIES.get(i), value);
            }
            changed = true;
            return;
        }
        if (Settings.RATING_CATEGORIES != null && Settings.RATING_CATEGORIES.size() > 1) {
            for (int i = 0 ; i < Settings.RATING_CATEGORIES.size(); i++) {
                ratingMap.put(Settings.RATING_CATEGORIES.get(i), (value % 10) - 1);
                value /= 10;
            }
        }
        else {
            ratingMap.put(null, value);
        }
    }
    
    public List<String> getCategories() {
        if (ratingMap.size() == 1) {
            return new ArrayList<>();
        }
        return new ArrayList<>(ratingMap.keySet());
    }
    
    public double getAverageRating() {
        double total = 0;
        for (Entry<String, Integer> entry : ratingMap.entrySet()) {
            total += entry.getValue();
        }
        return total / (double) ratingMap.size();
    }
    
    public Integer getRating(String category) {
        return ratingMap.get(category);
    }
    
    public boolean setRating(String category, int value) {
        changed = true;
        if (!this.ratingMap.containsKey(category)) {
            return false;
        }
        return this.ratingMap.put(category, value) != null;
    }
    
    public int getAggregate() {
        if (!changed) {
            return initial;
        }
        if (Settings.RATING_CATEGORIES != null && Settings.RATING_CATEGORIES.size() > 1) {
            int val = 0;
            for (int i = 0; i < Settings.RATING_CATEGORIES.size(); i++) {
                val += (i + 1) * Math.pow(10, ratingMap.get(Settings.RATING_CATEGORIES.get(i)));
            }
            return val;
        }
        else {
            return ratingMap.get(null);
        }
        
    }
    
    
}
