package PlotTesting;

import org.junit.Assert;
import org.junit.Test;

import com.intellectualcrafters.plot.PlotHelper;
import com.intellectualcrafters.plot.PlotMain;

public class Test1 {
    
    // I have no idea what I should actually test :p
    
    @Test
    public void testSquare() {
        Assert.assertEquals(PlotHelper.square(5), 25);
    }
}
