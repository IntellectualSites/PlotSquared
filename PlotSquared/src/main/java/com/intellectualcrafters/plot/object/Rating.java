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
    
    public Rating(int value) {
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
    
    
}
