package com.plotsquared.nukkit.util;

import cn.nukkit.Player;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.AbstractTitle;
import com.plotsquared.nukkit.object.NukkitPlayer;

public class NukkitTitleUtil extends AbstractTitle {
    @Override
    public void sendTitle(PlotPlayer player, String head, String sub, int in, int delay, int out) {
        Player plr = ((NukkitPlayer) player).player;
        plr.sendTitle(head, sub, in, delay, out);
    }
}
