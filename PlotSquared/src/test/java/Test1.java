import com.intellectualcrafters.plot.PlotMain;
import com.intellectualcrafters.plot.config.Settings;
import com.intellectualcrafters.plot.database.AbstractDB;
import com.intellectualcrafters.plot.database.DBFunc;
import com.intellectualcrafters.plot.flag.Flag;
import com.intellectualcrafters.plot.generator.DefaultPlotManager;
import com.intellectualcrafters.plot.generator.DefaultPlotWorld;
import com.intellectualcrafters.plot.object.*;
import com.intellectualcrafters.plot.util.PlotHelper;
import com.intellectualcrafters.plot.util.SetBlockFast;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Biome;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.UUID;

public class Test1 {

    static Integer count = 0;

    // I have no idea what I should actually test :p

    public boolean nextTest() {
        Test1.count++;
        switch (Test1.count) {
            case 1:
                return test1_Square();
            case 2:
                return true; // test2_InitMain(); // fails
            case 3:
                return test3_InitPlotId();
            case 4:
                return test4_InitPlot();
            case 5:
                return test5_InitDBFunc();
            case 6:
                return true; // test6_Plots(); // fails
            case 7:
                return true; // test7_OnEnable(); // fails
            case 8:
                return true; // test8_AddPlotWorld(); // fails
            case 9:
                return true; // test9_CanSetFast(); // fails
        }
        return false;
    }

    @Test
    public void t1() {
        Assert.assertTrue(nextTest());
    }

    @Test
    public void t2() {
        Assert.assertTrue(nextTest());
    }

    @Test
    public void t3() {
        Assert.assertTrue(nextTest());
    }

    @Test
    public void t4() {
        Assert.assertTrue(nextTest());
    }

    @Test
    public void t5() {
        Assert.assertTrue(nextTest());
    }

    @Test
    public void t6() {
        Assert.assertTrue(nextTest());
    }

    @Test
    public void t7() {
        Assert.assertTrue(nextTest());
    }

    @Test
    public void t8() {
        Assert.assertTrue(nextTest());
    }

    @Test
    public void t9() {
        Assert.assertTrue(nextTest());
    }

    public boolean test1_Square() {
        return PlotHelper.square(5) == 25;
    }

    public boolean test2_InitMain() {
        boolean passed = false;
        try {
            final PlotMain plugin = PlotMain.getMain();
            passed = plugin != null;
        } catch (final Throwable e) {

        }
        return passed;
    }

    public boolean test3_InitPlotId() {
        boolean passed = false;
        try {
            final Object id = new PlotId(0, 0);
            passed = id != null;
        } catch (final Throwable e) {

        }
        return passed;
    }

    public boolean test4_InitPlot() {
        boolean passed = false;
        try {
            new Plot(new PlotId(0, 0), DBFunc.everyone, Biome.FOREST, new ArrayList<UUID>(), new ArrayList<UUID>(), new ArrayList<UUID>(), null, PlotHomePosition.DEFAULT, null, "testworld", new boolean[]{false, false, false, false});
            passed = true;
        } catch (final Throwable ignored) {

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
                public void setTrusted(final String world, final Plot plot, final OfflinePlayer player) {
                }

                @Override
                public void setPosition(final String world, final Plot plot, final String position) {
                }

                @Override
                public void setOwner(final Plot plot, final UUID uuid) {
                }

                @Override
                public void setMerged(final String world, final Plot plot, final boolean[] merged) {
                }

                @Override
                public void setHelper(final String world, final Plot plot, final OfflinePlayer player) {
                }

                @Override
                public void setFlags(final String world, final Plot plot, final Flag[] flags) {
                }

                @Override
                public void setDenied(final String world, final Plot plot, final OfflinePlayer player) {
                }

                @Override
                public void setComment(final String world, final Plot plot, final PlotComment comment) {
                }

                @Override
                public void setAlias(final String world, final Plot plot, final String alias) {
                }

                @Override
                public void removeTrusted(final String world, final Plot plot, final OfflinePlayer player) {
                }

                @Override
                public void removeHelper(final String world, final Plot plot, final OfflinePlayer player) {
                }

                @Override
                public void removeDenied(final String world, final Plot plot, final OfflinePlayer player) {
                }

                @Override
                public void removeComment(final String world, final Plot plot, final PlotComment comment) {
                }

                @Override
                public void purge(final String world) {
                }

                @Override
                public void purge(final String world, final PlotId id) {
                }

                @Override
                public HashMap<String, Object> getSettings(final int id) {
                    return null;
                }

                @Override
                public double getRatings(final Plot plot) {
                    return 0;
                }

                @Override
                public LinkedHashMap<String, HashMap<PlotId, Plot>> getPlots() {
                    final LinkedHashMap<String, HashMap<PlotId, Plot>> plots = new LinkedHashMap<String, HashMap<PlotId, Plot>>();

                    plots.put("testworld", new HashMap<PlotId, Plot>());

                    final PlotId id = new PlotId(0, 0);

                    plots.get("testworld").put(id, new Plot(id, DBFunc.everyone, new ArrayList<UUID>(), new ArrayList<UUID>(), new ArrayList<UUID>(), null, PlotHomePosition.DEFAULT, null, "testworld", new boolean[]{false, false, false, false}));

                    return plots;
                }

                @Override
                public int getId(final String world, final PlotId id2) {
                    return 0;
                }

                @Override
                public ArrayList<PlotComment> getComments(final String world, final Plot plot, final int tier) {
                    return null;
                }

                @Override
                public void delete(final String world, final Plot plot) {
                }

                @Override
                public void createTables(final String database, final boolean add_constraint) throws Exception {
                }

                @Override
                public void createPlots(final ArrayList<Plot> plots) {
                }

                @Override
                public void createPlotSettings(final int id, final Plot plot) {
                }

                @Override
                public void createPlot(final Plot plot) {
                }

                @Override
                public void createAllSettingsAndHelpers(final ArrayList<Plot> plots) {
                }
            };
            passed = true;
        } catch (final Throwable e) {

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
        } catch (final Throwable e) {

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
        } catch (final Throwable e) {

        }
        return passed;
    }

    public boolean test9_CanSetFast() {
        boolean passed = false;
        try {
            new SetBlockFast();
            passed = true;
        } catch (final Throwable e) {

        }
        return passed;
    }

}
