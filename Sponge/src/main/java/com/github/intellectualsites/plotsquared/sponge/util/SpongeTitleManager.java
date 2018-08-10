package com.github.intellectualsites.plotsquared.sponge.util;

import com.github.intellectualsites.plotsquared.plot.object.PlotPlayer;
import com.github.intellectualsites.plotsquared.plot.util.AbstractTitle;
import com.github.intellectualsites.plotsquared.sponge.object.SpongePlayer;
import org.spongepowered.api.text.title.Title;

public class SpongeTitleManager extends AbstractTitle {

    @Override
    public void sendTitle(PlotPlayer player, String head, String sub, int in, int delay, int out) {
        Title title =
            Title.builder().title(SpongeUtil.getText(head)).subtitle(SpongeUtil.getText(sub))
                .fadeIn(in * 20).stay(delay * 20).fadeOut(out * 20).build();
        ((SpongePlayer) player).player.sendTitle(title);
    }
}
