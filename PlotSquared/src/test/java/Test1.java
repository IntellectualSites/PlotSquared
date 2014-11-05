import org.junit.Assert;
import org.junit.Test;

import com.intellectualcrafters.plot.PlotHelper;
import com.intellectualcrafters.plot.PlotMain;
import com.intellectualcrafters.plot.PlotManager;
import com.intellectualcrafters.plot.PlotWorld;
import com.intellectualcrafters.plot.generator.DefaultPlotManager;
import com.intellectualcrafters.plot.generator.DefaultPlotWorld;

public class Test1 {

    // I have no idea what I should actually test :p

    @Test
    public void testSquare() {
        Assert.assertEquals(PlotHelper.square(5), 25);
    }

    @Test
    public void testPlots() {
        Assert.assertNotNull(PlotMain.getAllPlotsRaw());
    }

    @Test
    public void testAddPlotWorld() {
        boolean passed = false;
        try {
            final PlotWorld plotworld = new DefaultPlotWorld("poop");
            final PlotManager manager = new DefaultPlotManager();
            PlotMain.addPlotWorld("poop", plotworld, manager);
            passed = (PlotMain.getPlotManager("poop") != null) && (PlotMain.getWorldSettings("poop") != null);
        }
        catch (final Throwable e) {

        }
        finally {
            Assert.assertTrue(passed);
        }
    }

    @Test
    public void testCanSetFast() {
        Assert.assertTrue(PlotHelper.canSetFast);
    }

}
