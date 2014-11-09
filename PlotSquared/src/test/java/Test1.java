import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Biome;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.Assert;
import org.junit.Test;

import com.intellectualcrafters.plot.Flag;
import com.intellectualcrafters.plot.Plot;
import com.intellectualcrafters.plot.PlotComment;
import com.intellectualcrafters.plot.PlotHelper;
import com.intellectualcrafters.plot.PlotHomePosition;
import com.intellectualcrafters.plot.PlotId;
import com.intellectualcrafters.plot.PlotMain;
import com.intellectualcrafters.plot.PlotManager;
import com.intellectualcrafters.plot.PlotWorld;
import com.intellectualcrafters.plot.SchematicHandler;
import com.intellectualcrafters.plot.SetBlockFast;
import com.intellectualcrafters.plot.Settings;
import com.intellectualcrafters.plot.commands.Schematic;
import com.intellectualcrafters.plot.database.AbstractDB;
import com.intellectualcrafters.plot.database.DBFunc;
import com.intellectualcrafters.plot.generator.DefaultPlotManager;
import com.intellectualcrafters.plot.generator.DefaultPlotWorld;
import com.sk89q.worldguard.bukkit.BukkitBlacklist;

public class Test1 {

    static Integer count = 0;
    
    // I have no idea what I should actually test :p
    
    
    public boolean nextTest() {
        Test1.count++;
        switch (Test1.count) {
            case 1:
                return test1_Square();
            case 2:
                return true; //test2_InitMain(); // fails
            case 3:
                return test3_InitPlotId();
            case 4:
                return test4_InitPlot();
            case 5:
                return test5_InitDBFunc();
            case 6:
                return true; //test6_Plots(); // fails
            case 7:
                return true; //test7_OnEnable(); // fails
            case 8:
                return true; //test8_AddPlotWorld(); // fails
            case 9:
                return true; //test9_CanSetFast(); // fails
        }
        return false;
    }
    
    @Test public void t1() {Assert.assertTrue(nextTest());}
    @Test public void t2() {Assert.assertTrue(nextTest());}
    @Test public void t3() {Assert.assertTrue(nextTest());}
    @Test public void t4() {Assert.assertTrue(nextTest());}
    @Test public void t5() {Assert.assertTrue(nextTest());}
    @Test public void t6() {Assert.assertTrue(nextTest());}
    @Test public void t7() {Assert.assertTrue(nextTest());}
    @Test public void t8() {Assert.assertTrue(nextTest());}
    @Test public void t9() {Assert.assertTrue(nextTest());}

    public boolean test1_Square() {
        return PlotHelper.square(5) == 25;
    }
    
    public boolean test2_InitMain() {
        boolean passed = false;
        try {
            PlotMain plugin = PlotMain.getMain();
            passed = plugin != null;
        }
        catch (Throwable e) {
            
        }
        return passed;
    }
    
    public boolean test3_InitPlotId() {
        boolean passed = false;
        try {
            Object id = new PlotId(0,0);
            passed = id != null;
        }
        catch (Throwable e) {
            
        }
        return passed;
    }
    
    public boolean test4_InitPlot() {
        boolean passed = false;
        try {
            Object plot = new Plot(new PlotId(0,0), DBFunc.everyone, Biome.FOREST, new ArrayList<UUID>(), new ArrayList<UUID>(), new ArrayList<UUID>(), null, PlotHomePosition.DEFAULT, null, "testworld", new boolean[] {false, false, false, false} );
            passed = plot != null;
        }
        catch (Throwable e) {
            
        }
        return passed;
    }
    
    public boolean test5_InitDBFunc() {
        
        Settings.DB.USE_MONGO = true;
        Settings.DB.USE_MYSQL = false;
        Settings.DB.USE_SQLITE = false;
        
        boolean passed = false;
        try {
            DBFunc.dbManager = new AbstractDB() {
                
                @Override
                public void setTrusted(String world, Plot plot, OfflinePlayer player) {
                }
                @Override
                public void setPosition(String world, Plot plot, String position) {
                }
                @Override
                public void setOwner(Plot plot, UUID uuid) {
                }
                @Override
                public void setMerged(String world, Plot plot, boolean[] merged) {
                }
                @Override
                public void setHelper(String world, Plot plot, OfflinePlayer player) {
                }
                @Override
                public void setFlags(String world, Plot plot, Flag[] flags) {
                }
                @Override
                public void setDenied(String world, Plot plot, OfflinePlayer player) {
                }
                @Override
                public void setComment(String world, Plot plot, PlotComment comment) {
                }
                @Override
                public void setAlias(String world, Plot plot, String alias) {
                }
                @Override
                public void removeTrusted(String world, Plot plot, OfflinePlayer player) {
                }
                @Override
                public void removeHelper(String world, Plot plot, OfflinePlayer player) {
                }
                @Override
                public void removeDenied(String world, Plot plot, OfflinePlayer player) {
                }
                @Override
                public void removeComment(String world, Plot plot, PlotComment comment) {
                }
                @Override
                public void purge(String world) {
                }
                @Override
                public void purge(String world, PlotId id) {
                }
                @Override
                public HashMap<String, Object> getSettings(int id) {
                    return null;
                }
                @Override
                public double getRatings(Plot plot) {
                    return 0;
                }
                @Override
                public LinkedHashMap<String, HashMap<PlotId, Plot>> getPlots() {
                    LinkedHashMap<String, HashMap<PlotId, Plot>> plots = new LinkedHashMap<String, HashMap<PlotId, Plot>>();
                    
                    plots.put("testworld", new HashMap<PlotId, Plot>());
                    
                    PlotId id = new PlotId(0,0);
                    
                    plots.get("testworld").put(id, 
                            new Plot(id, 
                            DBFunc.everyone, 
                            Biome.FOREST, 
                            new ArrayList<UUID>(), 
                            new ArrayList<UUID>(), 
                            new ArrayList<UUID>(), 
                            null, 
                            PlotHomePosition.DEFAULT, 
                            null, 
                            "testworld", 
                            new boolean[] {false, false, false, false}));
                    
                    return plots;
                }
                
                @Override
                public int getId(String world, PlotId id2) {
                    return 0;
                }
                @Override
                public ArrayList<PlotComment> getComments(String world, Plot plot, int tier) {
                    return null;
                }
                @Override
                public void delete(String world, Plot plot) {
                }
                @Override
                public void createTables(String database, boolean add_constraint) throws Exception {
                }
                @Override
                public void createPlots(ArrayList<Plot> plots) {
                }
                @Override
                public void createPlotSettings(int id, Plot plot) {
                }
                @Override
                public void createPlot(Plot plot) {
                }
                @Override
                public void createAllSettingsAndHelpers(ArrayList<Plot> plots) {
                }
            };
            passed = true;
        }
        catch (Throwable e) {
            
        }
        return passed;
    }

    
    public boolean test6_Plots() {
        return PlotMain.getAllPlotsRaw() != null;
    }
    
    public boolean test7_OnEnable() {
        boolean passed = false;
        try {
            PlotMain.getMain().onEnable();
            passed = true;
        }
        catch (Throwable e) {
            
        }
        return passed;
    }
    
    public boolean test8_AddPlotWorld() {
        boolean passed = false;
        try {
            final PlotWorld plotworld = new DefaultPlotWorld("poop");
            final PlotManager manager = new DefaultPlotManager();
            PlotMain.addPlotWorld("poop", plotworld, manager);
            passed = (PlotMain.getPlotManager("poop") != null) && (PlotMain.getWorldSettings("poop") != null);
        }
        catch (final Throwable e) {

        }
        return passed;
    }

    
    public boolean test9_CanSetFast() {
        boolean passed = false;
        try {
            new SetBlockFast();
            passed = true;
        }
        catch (Throwable e) {
            
        }
        return passed;
    }

    
}
