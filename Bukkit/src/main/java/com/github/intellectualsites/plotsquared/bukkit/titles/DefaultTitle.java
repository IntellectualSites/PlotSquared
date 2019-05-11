package com.github.intellectualsites.plotsquared.bukkit.titles;

import com.github.intellectualsites.plotsquared.bukkit.object.BukkitPlayer;
import com.github.intellectualsites.plotsquared.plot.object.PlotPlayer;
import com.github.intellectualsites.plotsquared.plot.util.AbstractTitle;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class DefaultTitle extends AbstractTitle {

  @Override
  public void sendTitle(PlotPlayer player, String head, String sub, int in, int delay, int out) {
    //Titles are now a Paper Exclusive feature.
    if (Bukkit.getVersion().contains("git-Paper")) {
      final Player playerObj = ((BukkitPlayer) player).player;
      playerObj.sendTitle(head, sub, in, delay, out);
    }
  }
}
