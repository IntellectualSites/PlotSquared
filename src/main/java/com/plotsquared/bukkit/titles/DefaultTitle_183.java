package com.plotsquared.bukkit.titles;

import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.AbstractTitle;
import com.plotsquared.bukkit.object.BukkitPlayer;

public class DefaultTitle_183 extends AbstractTitle
{
    @Override
    public void sendTitle(final PlotPlayer player, final String head, final String sub, final int in, final int delay, final int out)
    {
        try
        {
            final DefaultTitleManager_183 title = new DefaultTitleManager_183(head, sub, in, delay, out);
            title.send(((BukkitPlayer) player).player);
        }
        catch (final Throwable e)
        {
            AbstractTitle.TITLE_CLASS = new HackTitle();
            AbstractTitle.TITLE_CLASS.sendTitle(player, head, sub, in, delay, out);
        }
    }
}
