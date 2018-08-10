package com.github.intellectualsites.plotsquared.nukkit.util;

import cn.nukkit.Player;
import com.github.intellectualsites.plotsquared.nukkit.object.NukkitPlayer;
import com.github.intellectualsites.plotsquared.plot.object.PlotPlayer;
import com.github.intellectualsites.plotsquared.plot.util.AbstractTitle;

public class NukkitTitleUtil extends AbstractTitle {
    @Override
    public void sendTitle(PlotPlayer player, String head, String sub, int in, int delay, int out) {
        Player plr = ((NukkitPlayer) player).player;
        plr.sendTitle(head, sub, in, delay, out);
    }
}
