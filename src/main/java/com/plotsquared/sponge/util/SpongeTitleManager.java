package com.plotsquared.sponge.util;

import org.spongepowered.api.text.title.Title;
import org.spongepowered.api.text.title.TitleBuilder;

import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.AbstractTitle;
import com.plotsquared.sponge.SpongeMain;
import com.plotsquared.sponge.object.SpongePlayer;

public class SpongeTitleManager extends AbstractTitle {
    
    @Override
    public void sendTitle(final PlotPlayer player, final String head, final String sub, final int in, final int delay, final int out) {
        final Title title = new TitleBuilder().title(SpongeMain.THIS.getText(head)).subtitle(SpongeMain.THIS.getText(sub)).fadeIn(in * 20).stay(delay * 20).fadeOut(out * 20).build();
        ((SpongePlayer) player).player.sendTitle(title);
    }
}
