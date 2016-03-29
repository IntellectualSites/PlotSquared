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

    public static PlotAnalysis getAnalysis(Plot plot) {
        Flag flag = FlagManager.getPlotFlagRaw(plot, "analysis");
        if (flag != null) {
            PlotAnalysis analysis = new PlotAnalysis();
            List<Integer> values = (List<Integer>) flag.getValue();
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

    public static void analyzePlot(Plot plot, RunnableVal<PlotAnalysis> whenDone) {
        HybridUtils.manager.analyzePlot(plot, whenDone);
    }
    
    /**
     * This will set the optimal modifiers for the plot analysis based on the current plot ratings<br>
     *  - Will be used to calibrate the threshold for plot clearing
     * @param whenDone
     * @param threshold
     */
    public static void calcOptimalModifiers(final Runnable whenDone, final double threshold) {
        if (running) {
            PS.debug("Calibration task already in progress!");
            return;
        }
        if (threshold <= 0 || threshold >= 1) {
            PS.debug("Invalid threshold provided! (Cannot be 0 or 100 as then there's no point calibrating)");
            return;
        }
        running = true;
        PS.debug(" - Fetching all plots");
        final ArrayList<Plot> plots = new ArrayList<>(PS.get().getPlots());
        TaskManager.runTaskAsync(new Runnable() {
            @Override
            public void run() {
                Iterator<Plot> iterator = plots.iterator();
                PS.debug(" - $1Reducing " + plots.size() + " plots to those with sufficient data");
                while (iterator.hasNext()) {
                    Plot plot = iterator.next();
                    if (plot.getSettings().ratings == null || plot.getSettings().getRatings().isEmpty()) {
                        iterator.remove();
                    } else {
                        plot.addRunning();
                    }
                }
                PS.debug(" - | Reduced to " + plots.size() + " plots");

                if (plots.size() < 3) {
                    PS.debug("Calibration cancelled due to insufficient comparison data, please try again later");
                    running = false;
                    for (Plot plot : plots) {
                        plot.removeRunning();
                    }
                    return;
                }

                PS.debug(" - $1Analyzing plot contents (this may take a while)");

                int[] changes = new int[plots.size()];
                int[] faces = new int[plots.size()];
                int[] data = new int[plots.size()];
                int[] air = new int[plots.size()];
                int[] variety = new int[plots.size()];

                int[] changes_sd = new int[plots.size()];
                int[] faces_sd = new int[plots.size()];
                int[] data_sd = new int[plots.size()];
                int[] air_sd = new int[plots.size()];
                int[] variety_sd = new int[plots.size()];

                final int[] ratings = new int[plots.size()];

                final AtomicInteger mi = new AtomicInteger(0);

                Thread ratingAnalysis = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        for (; mi.intValue() < plots.size(); mi.incrementAndGet()) {
                            int i = mi.intValue();
                            Plot plot = plots.get(i);
                            ratings[i] = (int) ((plot.getAverageRating() + plot.getSettings().getRatings().size()) * 100);
                            PS.debug(" | " + plot + " (rating) " + ratings[i]);
                        }
                    }
                });
                ratingAnalysis.start();

                ArrayDeque<Plot> plotsQueue = new ArrayDeque<>(plots);
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
                                    } catch (InterruptedException e) {
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
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                PS.debug(" - $1Waiting on plot rating thread: " + mi.intValue() * 100 / plots.size() + "%");
                try {
                    ratingAnalysis.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                PS.debug(" - $1Processing and grouping single plot analysis for bulk processing");
                for (int i = 0; i < plots.size(); i++) {
                    Plot plot = plots.get(i);
                    PS.debug(" | " + plot);
                    PlotAnalysis analysis = plot.getComplexity();

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

                int[] rankRatings = rank(ratings);
                int n = rankRatings.length;

                int optimalIndex = (int) Math.round((1 - threshold) * (n - 1));

                PS.debug(" - $1Calculating rank correlation: ");
                PS.debug(" - The analyzed plots which were processed and put into bulk data will be compared and correlated to the plot ranking");
                PS.debug(" - The calculated correlation constant will then be used to calibrate the threshold for auto plot clearing");

                int[] rankChanges = rank(changes);
                int[] sdChanges = getSD(rankChanges, rankRatings);
                int[] varianceChanges = square(sdChanges);
                int sumChanges = sum(varianceChanges);
                double factorChanges = getCC(n, sumChanges);
                PlotAnalysis.MODIFIERS.changes = factorChanges == 1 ? 0 : (int) (factorChanges * 1000 / MathMan.getMean(changes));
                PS.debug(" - | changes " + factorChanges);

                int[] rankFaces = rank(faces);
                int[] sdFaces = getSD(rankFaces, rankRatings);
                int[] varianceFaces = square(sdFaces);
                int sumFaces = sum(varianceFaces);
                double factorFaces = getCC(n, sumFaces);
                PlotAnalysis.MODIFIERS.faces = factorFaces == 1 ? 0 : (int) (factorFaces * 1000 / MathMan.getMean(faces));
                PS.debug(" - | faces " + factorFaces);

                int[] rankData = rank(data);
                int[] sdData = getSD(rankData, rankRatings);
                int[] variance_data = square(sdData);
                int sum_data = sum(variance_data);
                double factor_data = getCC(n, sum_data);
                PlotAnalysis.MODIFIERS.data = factor_data == 1 ? 0 : (int) (factor_data * 1000 / MathMan.getMean(data));
                PS.debug(" - | data " + factor_data);

                int[] rank_air = rank(air);
                int[] sd_air = getSD(rank_air, rankRatings);
                int[] variance_air = square(sd_air);
                int sum_air = sum(variance_air);
                double factor_air = getCC(n, sum_air);
                PlotAnalysis.MODIFIERS.air = factor_air == 1 ? 0 : (int) (factor_air * 1000 / MathMan.getMean(air));
                PS.debug(" - | air " + factor_air);

                int[] rank_variety = rank(variety);
                int[] sd_variety = getSD(rank_variety, rankRatings);
                int[] variance_variety = square(sd_variety);
                int sum_variety = sum(variance_variety);
                double factor_variety = getCC(n, sum_variety);
                PlotAnalysis.MODIFIERS.variety = factor_variety == 1 ? 0 : (int) (factor_variety * 1000 / MathMan.getMean(variety));
                PS.debug(" - | variety " + factor_variety);

                int[] rank_changes_sd = rank(changes_sd);
                int[] sd_changes_sd = getSD(rank_changes_sd, rankRatings);
                int[] variance_changes_sd = square(sd_changes_sd);
                int sum_changes_sd = sum(variance_changes_sd);
                double factor_changes_sd = getCC(n, sum_changes_sd);
                PlotAnalysis.MODIFIERS.changes_sd = factor_changes_sd == 1 ? 0 : (int) (factor_changes_sd * 1000 / MathMan.getMean(changes_sd));
                PS.debug(" - | changes_sd " + factor_changes_sd);

                int[] rank_faces_sd = rank(faces_sd);
                int[] sd_faces_sd = getSD(rank_faces_sd, rankRatings);
                int[] variance_faces_sd = square(sd_faces_sd);
                int sum_faces_sd = sum(variance_faces_sd);
                double factor_faces_sd = getCC(n, sum_faces_sd);
                PlotAnalysis.MODIFIERS.faces_sd = factor_faces_sd == 1 ? 0 : (int) (factor_faces_sd * 1000 / MathMan.getMean(faces_sd));
                PS.debug(" - | faces_sd " + factor_faces_sd);

                int[] rank_data_sd = rank(data_sd);
                int[] sd_data_sd = getSD(rank_data_sd, rankRatings);
                int[] variance_data_sd = square(sd_data_sd);
                int sum_data_sd = sum(variance_data_sd);
                double factor_data_sd = getCC(n, sum_data_sd);
                PlotAnalysis.MODIFIERS.data_sd = factor_data_sd == 1 ? 0 : (int) (factor_data_sd * 1000 / MathMan.getMean(data_sd));
                PS.debug(" - | data_sd " + factor_data_sd);

                int[] rank_air_sd = rank(air_sd);
                int[] sd_air_sd = getSD(rank_air_sd, rankRatings);
                int[] variance_air_sd = square(sd_air_sd);
                int sum_air_sd = sum(variance_air_sd);
                double factor_air_sd = getCC(n, sum_air_sd);
                PlotAnalysis.MODIFIERS.air_sd = factor_air_sd == 1 ? 0 : (int) (factor_air_sd * 1000 / MathMan.getMean(air_sd));
                PS.debug(" - | air_sd " + factor_air_sd);

                int[] rank_variety_sd = rank(variety_sd);
                int[] sd_variety_sd = getSD(rank_variety_sd, rankRatings);
                int[] variance_variety_sd = square(sd_variety_sd);
                int sum_variety_sd = sum(variance_variety_sd);
                double factor_variety_sd = getCC(n, sum_variety_sd);
                PlotAnalysis.MODIFIERS.variety_sd = factor_variety_sd == 1 ? 0 : (int) (factor_variety_sd * 1000 / MathMan.getMean(variety_sd));
                PS.debug(" - | variety_sd " + factor_variety_sd);

                int[] complexity = new int[n];

                PS.debug(" $1Calculating threshold");
                int max = 0;
                int min = 0;
                for (int i = 0; i < n; i++) {
                    Plot plot = plots.get(i);
                    PlotAnalysis analysis = plot.getComplexity();
                    complexity[i] = analysis.complexity;
                    if (analysis.complexity < min) {
                        min = analysis.complexity;
                    } else if (analysis.complexity > max) {
                        max = analysis.complexity;
                    }
                }
                int optimalComplexity = Integer.MAX_VALUE;
                if (min > 0 && max < 102400) { // If low size, use my fast ranking algorithm
                    int[] rankComplexity = rank(complexity, max + 1);
                    for (int i = 0; i < n; i++) {
                        if (rankComplexity[i] == optimalIndex) {
                            optimalComplexity = complexity[i];
                            break;
                        }
                    }
                    logln("Complexity: ");
                    logln(rankComplexity);
                    logln("Ratings: ");
                    logln(rankRatings);
                    logln("Correlation: ");
                    logln(getCC(n, sum(square(getSD(rankComplexity, rankRatings)))));
                    if (optimalComplexity == Integer.MAX_VALUE) {
                        PS.debug("Insufficient data to determine correlation! " + optimalIndex + " | " + n);
                        running = false;
                        for (Plot plot : plots) {
                            plot.removeRunning();
                        }
                        return;
                    }
                } else { // Use the fast radix sort algorithm
                    int[] sorted = complexity.clone();
                    sort(sorted);
                    optimalComplexity = sorted[optimalIndex];
                    logln("Complexity: ");
                    logln(complexity);
                    logln("Ratings: ");
                    logln(rankRatings);
                }

                // Save calibration
                PS.debug(" $1Saving calibration");
                YamlConfiguration config = PS.get().config;
                config.set("clear.auto.threshold", optimalComplexity);
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
                } catch (IOException e) {
                    e.printStackTrace();
                }

                PS.debug("$1Done!");
                running = false;
                for (Plot plot : plots) {
                    plot.removeRunning();
                }
                whenDone.run();
            }
        });
    }

    public static void logln(Object obj) {
        PS.debug(log(obj));
    }

    public static String log(Object obj) {
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
            for (Object element : (List<?>) obj) {
                result += prefix + log(element);
                prefix = ",";
            }
            return "[ " + result + " ]";
        } else {
            return obj.toString();
        }
    }
    
    /**
     * Get correlation coefficient.
     * @return
     */
    public static double getCC(int n, int sum) {
        return 1 - 6 * (double) sum / (n * (n * n - 1));
    }
    
    /**
     * Sum of an array
     * @param array
     * @return
     */
    public static int sum(int[] array) {
        int sum = 0;
        for (int value : array) {
            sum += value;
        }
        return sum;
    }
    
    /**
     * A simple array squaring algorithm.
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
     * An optimized lossy standard deviation algorithm.
     * @param ranks
     * @return
     */
    public static int[] getSD(int[]... ranks) {
        if (ranks.length == 0) {
            return null;
        }
        int size = ranks[0].length;
        int arrays = ranks.length;
        int[] result = new int[size];
        for (int j = 0; j < size; j++) {
            int sum = 0;
            for (int[] rank : ranks) {
                sum += rank[j];
            }
            int mean = sum / arrays;
            int sd = 0;
            for (int[] rank : ranks) {
                int value = rank[j];
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
    /**
     * An optimized algorithm for ranking a very specific set of inputs
     * @param input
     * @return
     */
    public static int[] rank(int[] input) {
        return rank(input, 102400);
    }
    
    /**
     * An optimized algorithm for ranking a very specific set of inputs
     * @param input
     * @return
     */
    public static int[] rank(int[] input, int size) {
        int[] cache = new int[size];
        int max = 0;
        if (input.length < size) {
            for (int value : input) {
                if (value > max) {
                    max = value;
                }
                cache[value]++;
            }
        } else {
            max = cache.length - 1;
            for (int value : input) {
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

        int[] ranks = new int[input.length];
        for (int i = 0; i < input.length; i++) {
            int index = input[i];
            ranks[i] = cache[index];
            cache[index]--;
        }
        return ranks;
    }

    public static void sort(int[] input) {
        int SIZE = 10;
        List<Integer>[] bucket = new ArrayList[SIZE];
        for (int i = 0; i < bucket.length; i++) {
            bucket[i] = new ArrayList<Integer>();
        }
        boolean maxLength = false;
        int placement = 1;
        while (!maxLength) {
            maxLength = true;
            for (Integer i : input) {
                int tmp = i / placement;
                bucket[tmp % SIZE].add(i);
                if (maxLength && tmp > 0) {
                    maxLength = false;
                }
            }
            int a = 0;
            for (int b = 0; b < SIZE; b++) {
                for (Integer i : bucket[b]) {
                    input[a++] = i;
                }
                bucket[b].clear();
            }
            placement *= SIZE;
        }
    }

    public List<Integer> asList() {
        return Arrays.asList(this.changes, this.faces, this.data, this.air, this.variety, this.changes_sd, this.faces_sd, this.data_sd, this.air_sd,
                this.variety_sd);
    }

    public int getComplexity() {
        if (this.complexity != 0) {
            return this.complexity;
        }
        this.complexity = this.changes * MODIFIERS.changes
                + this.faces * MODIFIERS.faces
                + this.data * MODIFIERS.data
                + this.air * MODIFIERS.air
                + this.variety * MODIFIERS.variety
                + this.changes_sd * MODIFIERS.changes_sd
                + this.faces_sd * MODIFIERS.faces_sd
                + this.data_sd * MODIFIERS.data_sd
                + this.air_sd * MODIFIERS.air_sd
                + this.variety_sd * MODIFIERS.variety_sd;
        return this.complexity;
    }
}
