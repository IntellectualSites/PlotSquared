package com.intellectualcrafters.plot.object;

import java.util.List;

import com.intellectualcrafters.plot.flag.Flag;
import com.intellectualcrafters.plot.flag.FlagManager;
import com.intellectualcrafters.plot.generator.BukkitHybridUtils;
import com.intellectualcrafters.plot.util.TaskManager;

public class PlotAnalysis {
    public double changes;
    public double faces;
    public double data;
    public double air;
    public double variety;
    public double complexity;
    
    public static double CHANGES_MODIFIER = 32;
    public static double FACES_MODIFIER = 32;
    public static double DATA_MODIFIER = 32;
    public static double AIR_MODIFIER = 32;
    public static double VARIETY_MODIFIER = 32;
    
    public static PlotAnalysis getAnalysis(Plot plot) {
        Flag flag = FlagManager.getPlotFlag(plot, "analysis");
        if (flag != null) {
            PlotAnalysis analysis = new PlotAnalysis();
            List<Double> values = (List<Double>) flag.getValue();
            analysis.changes = values.get(0);
            analysis.faces = values.get(1);
            analysis.data = values.get(3);
            analysis.air = values.get(4);
            analysis.variety = values.get(5);
            return analysis;
        }
        return null;
    }
    
    public static void analyzePlot(Plot plot, RunnableVal<PlotAnalysis> whenDone) {
        PlotAnalysis analysis = getAnalysis(plot);
        if (analysis != null) {
            whenDone.value = analysis;
            whenDone.run();
            return;
        }
        BukkitHybridUtils.manager.analyzePlot(plot, whenDone);
    }
    
    /**
     * 
     * @param whenDone
     */
    public static void calcOptimalModifiers(Runnable whenDone) {
        
    }
}
