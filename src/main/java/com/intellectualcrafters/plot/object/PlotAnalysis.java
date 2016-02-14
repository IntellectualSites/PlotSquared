package com.intellectualcrafters.plot.object;

import com.intellectualcrafters.configuration.file.YamlConfiguration;
import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.flag.Flag;
import com.intellectualcrafters.plot.flag.FlagManager;
import com.intellectualcrafters.plot.generator.HybridUtils;
import com.intellectualcrafters.plot.util.MathMan;
import com.intellectualcrafters.plot.util.TaskManager;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class PlotAnalysis {

    public static PlotAnalysis MODIFIERS = new PlotAnalysis();
    public static boolean running = false;
    public int changes;
    public int faces;
    public int data;
    public int air;
    public int variety;
    public int changes_sd;
    public int faces_sd;
    public int data_sd;
    public int air_sd;
    public int variety_sd;
    private int complexity;
    
    public static PlotAnalysis getAnalysis(final Plot plot) {
        final Flag flag = FlagManager.getPlotFlagRaw(plot, "analysis");
        if (flag != null) {
            final PlotAnalysis analysis = new PlotAnalysis();
            final List<Integer> values = (List<Integer>) flag.getValue();
            analysis.changes = values.get(0); // 2126
            analysis.faces = values.get(1); // 90
            analysis.data = values.get(2); // 0
            analysis.air = values.get(3); // 19100
            analysis.variety = values.get(4); // 266

            analysis.changes_sd = values.get(5); // 2104
            analysis.faces_sd = values.get(6); // 89
            analysis.data_sd = values.get(7); // 0
            analysis.air_sd = values.get(8); // 18909
            analysis.variety_sd = values.get(9); // 263

            analysis.complexity = analysis.getComplexity();
            return analysis;
        }
        return null;
    }
    
    public static void analyzePlot(final Plot plot, final RunnableVal<PlotAnalysis> whenDone) {
        HybridUtils.manager.analyzePlot(plot, whenDone);
    }
    
    /**
     * This will set the optimal modifiers for the plot analysis based on the current plot ratings<br>
     *  - Will be used to calibrate the threshold for plot clearing
     * @param whenDone
     */
    public static void calcOptimalModifiers(final Runnable whenDone, final double threshold) {
        if (running) {
            PS.debug("Calibration task already in progress!");
            return;
        }
        if ((threshold <= 0) || (threshold >= 1)) {
            PS.debug("Invalid threshold provided! (Cannot be 0 or 100 as then there's no point calibrating)");
            return;
        }
        running = true;
        PS.debug(" - Fetching all plots");
        final ArrayList<Plot> plots = new ArrayList<>(PS.get().getPlots());
        TaskManager.runTaskAsync(new Runnable() {
            @Override
            public void run() {
                final Iterator<Plot> iter = plots.iterator();
                PS.debug(" - $1Reducing " + plots.size() + " plots to those with sufficient data");
                while (iter.hasNext()) {
                    final Plot plot = iter.next();
                    if ((plot.getSettings().ratings == null) || (plot.getSettings().ratings.isEmpty())) {
                        iter.remove();
                    } else {
                        plot.addRunning();
                    }
                }
                PS.debug(" - | Reduced to " + plots.size() + " plots");

                if (plots.size() < 3) {
                    PS.debug("Calibration cancelled due to insufficient comparison data, please try again later");
                    running = false;
                    for (final Plot plot : plots) {
                        plot.removeRunning();
                    }
                    return;
                }

                PS.debug(" - $1Analyzing plot contents (this may take a while)");

                final int[] changes = new int[plots.size()];
                final int[] faces = new int[plots.size()];
                final int[] data = new int[plots.size()];
                final int[] air = new int[plots.size()];
                final int[] variety = new int[plots.size()];

                final int[] changes_sd = new int[plots.size()];
                final int[] faces_sd = new int[plots.size()];
                final int[] data_sd = new int[plots.size()];
                final int[] air_sd = new int[plots.size()];
                final int[] variety_sd = new int[plots.size()];

                final int[] ratings = new int[plots.size()];

                final AtomicInteger mi = new AtomicInteger(0);

                final Thread ratingAnalysis = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        for (; mi.intValue() < plots.size(); mi.incrementAndGet()) {
                            final int i = mi.intValue();
                            final Plot plot = plots.get(i);
                            ratings[i] = (int) ((plot.getAverageRating() + plot.getSettings().ratings.size()) * 100);
                            PS.debug(" | " + plot + " (rating) " + (ratings[i]));
                        }
                    }
                });
                ratingAnalysis.start();

                final ArrayDeque<Plot> plotsQueue = new ArrayDeque<>(plots);
                while (true) {
                    final Plot queuePlot = plotsQueue.poll();
                    if (queuePlot == null) {
                        break;
                    }
                    PS.debug(" | " + queuePlot);
                    final Object lock = new Object();
                    TaskManager.runTask(new Runnable() {
                        @Override
                        public void run() {
                            analyzePlot(queuePlot, new RunnableVal<PlotAnalysis>() {
                                @Override
                                public void run(PlotAnalysis value) {
                                    try {
                                        synchronized (this) {
                                            wait(10000);
                                        }
                                    } catch (final InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                    synchronized (lock) {
                                        queuePlot.removeRunning();
                                        lock.notify();
                                    }
                                }
                            });
                        }
                    });
                    try {
                        synchronized (lock) {
                            lock.wait();
                        }
                    } catch (final InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                PS.debug(" - $1Waiting on plot rating thread: " + ((mi.intValue() * 100) / plots.size()) + "%");
                try {
                    ratingAnalysis.join();
                } catch (final InterruptedException e) {
                    e.printStackTrace();
                }

                PS.debug(" - $1Processing and grouping single plot analysis for bulk processing");
                for (int i = 0; i < plots.size(); i++) {
                    final Plot plot = plots.get(i);
                    PS.debug(" | " + plot);
                    final PlotAnalysis analysis = plot.getComplexity();

                    changes[i] = analysis.changes;
                    faces[i] = analysis.faces;
                    data[i] = analysis.data;
                    air[i] = analysis.air;
                    variety[i] = analysis.variety;

                    changes_sd[i] = analysis.changes_sd;
                    faces_sd[i] = analysis.faces_sd;
                    data_sd[i] = analysis.data_sd;
                    air_sd[i] = analysis.air_sd;
                    variety_sd[i] = analysis.variety_sd;
                }

                PS.debug(" - $1Calculating rankings");

                final int[] rank_ratings = rank(ratings);
                final int n = rank_ratings.length;

                final int optimal_index = (int) Math.round((1 - threshold) * (n - 1));

                PS.debug(" - $1Calculating rank correlation: ");
                PS.debug(" - The analyzed plots which were processed and put into bulk data will be compared and correlated to the plot ranking");
                PS.debug(" - The calculated correlation constant will then be used to calibrate the threshold for auto plot clearing");

                final int[] rank_changes = rank(changes);
                final int[] sd_changes = getSD(rank_changes, rank_ratings);
                final int[] variance_changes = square(sd_changes);
                final int sum_changes = sum(variance_changes);
                final double factor_changes = getCC(n, sum_changes);
                PlotAnalysis.MODIFIERS.changes = factor_changes == 1 ? 0 : (int) ((factor_changes * 1000) / MathMan.getMean(changes));
                PS.debug(" - | changes " + factor_changes);

                final int[] rank_faces = rank(faces);
                final int[] sd_faces = getSD(rank_faces, rank_ratings);
                final int[] variance_faces = square(sd_faces);
                final int sum_faces = sum(variance_faces);
                final double factor_faces = getCC(n, sum_faces);
                PlotAnalysis.MODIFIERS.faces = factor_faces == 1 ? 0 : (int) ((factor_faces * 1000) / MathMan.getMean(faces));
                PS.debug(" - | faces " + factor_faces);

                final int[] rank_data = rank(data);
                final int[] sd_data = getSD(rank_data, rank_ratings);
                final int[] variance_data = square(sd_data);
                final int sum_data = sum(variance_data);
                final double factor_data = getCC(n, sum_data);
                PlotAnalysis.MODIFIERS.data = factor_data == 1 ? 0 : (int) ((factor_data * 1000) / MathMan.getMean(data));
                PS.debug(" - | data " + factor_data);

                final int[] rank_air = rank(air);
                final int[] sd_air = getSD(rank_air, rank_ratings);
                final int[] variance_air = square(sd_air);
                final int sum_air = sum(variance_air);
                final double factor_air = getCC(n, sum_air);
                PlotAnalysis.MODIFIERS.air = factor_air == 1 ? 0 : (int) ((factor_air * 1000) / MathMan.getMean(air));
                PS.debug(" - | air " + factor_air);

                final int[] rank_variety = rank(variety);
                final int[] sd_variety = getSD(rank_variety, rank_ratings);
                final int[] variance_variety = square(sd_variety);
                final int sum_variety = sum(variance_variety);
                final double factor_variety = getCC(n, sum_variety);
                PlotAnalysis.MODIFIERS.variety = factor_variety == 1 ? 0 : (int) ((factor_variety * 1000) / MathMan.getMean(variety));
                PS.debug(" - | variety " + factor_variety);

                final int[] rank_changes_sd = rank(changes_sd);
                final int[] sd_changes_sd = getSD(rank_changes_sd, rank_ratings);
                final int[] variance_changes_sd = square(sd_changes_sd);
                final int sum_changes_sd = sum(variance_changes_sd);
                final double factor_changes_sd = getCC(n, sum_changes_sd);
                PlotAnalysis.MODIFIERS.changes_sd = factor_changes_sd == 1 ? 0 : (int) ((factor_changes_sd * 1000) / MathMan.getMean(changes_sd));
                PS.debug(" - | changes_sd " + factor_changes_sd);

                final int[] rank_faces_sd = rank(faces_sd);
                final int[] sd_faces_sd = getSD(rank_faces_sd, rank_ratings);
                final int[] variance_faces_sd = square(sd_faces_sd);
                final int sum_faces_sd = sum(variance_faces_sd);
                final double factor_faces_sd = getCC(n, sum_faces_sd);
                PlotAnalysis.MODIFIERS.faces_sd = factor_faces_sd == 1 ? 0 : (int) ((factor_faces_sd * 1000) / MathMan.getMean(faces_sd));
                PS.debug(" - | faces_sd " + factor_faces_sd);

                final int[] rank_data_sd = rank(data_sd);
                final int[] sd_data_sd = getSD(rank_data_sd, rank_ratings);
                final int[] variance_data_sd = square(sd_data_sd);
                final int sum_data_sd = sum(variance_data_sd);
                final double factor_data_sd = getCC(n, sum_data_sd);
                PlotAnalysis.MODIFIERS.data_sd = factor_data_sd == 1 ? 0 : (int) ((factor_data_sd * 1000) / MathMan.getMean(data_sd));
                PS.debug(" - | data_sd " + factor_data_sd);

                final int[] rank_air_sd = rank(air_sd);
                final int[] sd_air_sd = getSD(rank_air_sd, rank_ratings);
                final int[] variance_air_sd = square(sd_air_sd);
                final int sum_air_sd = sum(variance_air_sd);
                final double factor_air_sd = getCC(n, sum_air_sd);
                PlotAnalysis.MODIFIERS.air_sd = factor_air_sd == 1 ? 0 : (int) ((factor_air_sd * 1000) / MathMan.getMean(air_sd));
                PS.debug(" - | air_sd " + factor_air_sd);

                final int[] rank_variety_sd = rank(variety_sd);
                final int[] sd_variety_sd = getSD(rank_variety_sd, rank_ratings);
                final int[] variance_variety_sd = square(sd_variety_sd);
                final int sum_variety_sd = sum(variance_variety_sd);
                final double factor_variety_sd = getCC(n, sum_variety_sd);
                PlotAnalysis.MODIFIERS.variety_sd = factor_variety_sd == 1 ? 0 : (int) ((factor_variety_sd * 1000) / MathMan.getMean(variety_sd));
                PS.debug(" - | variety_sd " + factor_variety_sd);

                final int[] complexity = new int[n];

                PS.debug(" $1Calculating threshold");
                int max = 0;
                int min = 0;
                for (int i = 0; i < n; i++) {
                    final Plot plot = plots.get(i);
                    final PlotAnalysis analysis = plot.getComplexity();
                    complexity[i] = analysis.complexity;
                    if (analysis.complexity < min) {
                        min = analysis.complexity;
                    } else if (analysis.complexity > max) {
                        max = analysis.complexity;
                    }
                }
                int optimal_complexity = Integer.MAX_VALUE;
                if ((min > 0) && (max < 102400)) { // If low size, use my fast ranking algorithm
                    final int[] rank_complexity = rank(complexity, max + 1);
                    for (int i = 0; i < n; i++) {
                        if (rank_complexity[i] == optimal_index) {
                            optimal_complexity = complexity[i];
                            break;
                        }
                    }
                    logln("Complexity: ");
                    logln(rank_complexity);
                    logln("Ratings: ");
                    logln(rank_ratings);
                    logln("Correlation: ");
                    logln(getCC(n, sum(square(getSD(rank_complexity, rank_ratings)))));
                    if (optimal_complexity == Integer.MAX_VALUE) {
                        PS.debug("Insufficient data to determine correlation! " + optimal_index + " | " + n);
                        running = false;
                        for (final Plot plot : plots) {
                            plot.removeRunning();
                        }
                        return;
                    }
                } else { // Use the fast radix sort algorithm
                    final int[] sorted = complexity.clone();
                    sort(sorted);
                    optimal_complexity = sorted[optimal_index];
                    logln("Complexity: ");
                    logln(complexity);
                    logln("Ratings: ");
                    logln(rank_ratings);
                }

                // Save calibration
                PS.debug(" $1Saving calibration");
                final YamlConfiguration config = PS.get().config;
                config.set("clear.auto.threshold", optimal_complexity);
                config.set("clear.auto.calibration.changes", PlotAnalysis.MODIFIERS.changes);
                config.set("clear.auto.calibration.faces", PlotAnalysis.MODIFIERS.faces);
                config.set("clear.auto.calibration.data", PlotAnalysis.MODIFIERS.data);
                config.set("clear.auto.calibration.air", PlotAnalysis.MODIFIERS.air);
                config.set("clear.auto.calibration.variety", PlotAnalysis.MODIFIERS.variety);
                config.set("clear.auto.calibration.changes_sd", PlotAnalysis.MODIFIERS.changes_sd);
                config.set("clear.auto.calibration.faces_sd", PlotAnalysis.MODIFIERS.faces_sd);
                config.set("clear.auto.calibration.data_sd", PlotAnalysis.MODIFIERS.data_sd);
                config.set("clear.auto.calibration.air_sd", PlotAnalysis.MODIFIERS.air_sd);
                config.set("clear.auto.calibration.variety_sd", PlotAnalysis.MODIFIERS.variety_sd);
                try {
                    PS.get().config.save(PS.get().configFile);
                } catch (final IOException e) {
                    e.printStackTrace();
                }

                PS.debug("$1Done!");
                running = false;
                for (final Plot plot : plots) {
                    plot.removeRunning();
                }
                whenDone.run();
            }
        });
    }
    
    public static void logln(final Object obj) {
        PS.debug(log(obj));
    }
    
    public static String log(final Object obj) {
        String result = "";
        if (obj.getClass().isArray()) {
            String prefix = "";

            for (int i = 0; i < Array.getLength(obj); i++) {
                result += prefix + log(Array.get(obj, i));
                prefix = ",";
            }
            return "( " + result + " )";
        } else if (obj instanceof List<?>) {
            String prefix = "";
            for (final Object element : (List<?>) obj) {
                result += prefix + log(element);
                prefix = ",";
            }
            return "[ " + result + " ]";
        } else {
            return obj.toString();
        }
    }
    
    /**
     * Get correllation coefficient
     * @return
     */
    public static double getCC(final int n, final int sum) {
        return 1 - ((6 * (double) sum) / (n * ((n * n) - 1)));
    }
    
    /**
     * Sum of an array
     * @param array
     * @return
     */
    public static int sum(final int[] array) {
        int sum = 0;
        for (final int value : array) {
            sum += value;
        }
        return sum;
    }
    
    /**
     * A simple array squaring algorithm<br>
     *  - Used for calculating the variance
     * @param array
     * @return
     */
    public static int[] square(int[] array) {
        array = array.clone();
        for (int i = 0; i < array.length; i++) {
            array[i] *= array[i];
        }
        return array;
    }
    
    /**
     * An optimized lossy standard deviation algorithm
     * @param ranks
     * @return
     */
    public static int[] getSD(final int[]... ranks) {
        if (ranks.length == 0) {
            return null;
        }
        final int size = ranks[0].length;
        final int arrays = ranks.length;
        final int[] result = new int[size];
        for (int j = 0; j < size; j++) {
            int sum = 0;
            for (final int[] rank : ranks) {
                sum += rank[j];
            }
            final int mean = sum / arrays;
            int sd = 0;
            for (final int[] rank : ranks) {
                final int value = rank[j];
                sd += value < mean ? mean - value : value - mean;
            }
            result[j] = sd;
        }
        return result;
    }
    
    /**
     * An optimized algorithm for ranking a very specific set of inputs<br>
     *  - Input is an array of int with a max size of 102400<br>
     *  - A reduced sample space allows for sorting (and ranking in this case) in linear time
     * @param input
     * @return
     */
    public static int[] rank(final int[] input) {
        return rank(input, 102400);
    }
    
    /**
     * An optimized algorithm for ranking a very specific set of inputs
     * @param input
     * @return
     */
    public static int[] rank(final int[] input, final int size) {
        final int[] cache = new int[size];
        int max = 0;
        if (input.length < size) {
            for (final int value : input) {
                if (value > max) {
                    max = value;
                }
                cache[value]++;
            }
        } else {
            max = cache.length - 1;
            for (final int value : input) {
                cache[value]++;
            }
        }
        int last = 0;
        for (int i = max; i >= 0; i--) {
            if (cache[i] != 0) {
                cache[i] += last;
                last = cache[i];
                if (last == input.length) {
                    break;
                }
            }
        }

        final int[] ranks = new int[input.length];
        for (int i = 0; i < input.length; i++) {
            final int index = input[i];
            ranks[i] = cache[index];
            cache[index]--;
        }
        return ranks;
    }
    
    public static void sort(final int[] input) {
        final int SIZE = 10;
        final List<Integer>[] bucket = new ArrayList[SIZE];
        for (int i = 0; i < bucket.length; i++) {
            bucket[i] = new ArrayList<Integer>();
        }
        boolean maxLength = false;
        int tmp = -1, placement = 1;
        while (!maxLength) {
            maxLength = true;
            for (final Integer i : input) {
                tmp = i / placement;
                bucket[tmp % SIZE].add(i);
                if (maxLength && (tmp > 0)) {
                    maxLength = false;
                }
            }
            int a = 0;
            for (int b = 0; b < SIZE; b++) {
                for (final Integer i : bucket[b]) {
                    input[a++] = i;
                }
                bucket[b].clear();
            }
            placement *= SIZE;
        }
    }

    public List<Integer> asList() {
        return Arrays.asList(changes, faces, data, air, variety, changes_sd, faces_sd, data_sd, air_sd, variety_sd);
    }

    public int getComplexity() {
        if (complexity != 0) {
            return complexity;
        }
        complexity = ((changes) * MODIFIERS.changes)
                + ((faces) * MODIFIERS.faces)
                + ((data) * MODIFIERS.data)
                + ((air) * MODIFIERS.air)
                + ((variety) * MODIFIERS.variety)
                + ((changes_sd) * MODIFIERS.changes_sd)
                + ((faces_sd) * MODIFIERS.faces_sd)
                + ((data_sd) * MODIFIERS.data_sd)
                + ((air_sd) * MODIFIERS.air_sd)
                + ((variety_sd) * MODIFIERS.variety_sd);
        return complexity;
    }
}
