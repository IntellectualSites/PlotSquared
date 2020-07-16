package com.plotsquared.bukkit.listener;

import com.plotsquared.bukkit.BukkitMain;
import com.plotsquared.bukkit.placeholder.MVdWPlaceholders;
import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.configuration.Captions;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerLoadEvent;

public class ServerListener implements Listener {

    private final BukkitMain plugin;

    public ServerListener(BukkitMain plugin) {
        this.plugin = plugin;
    }

    @EventHandler public void onServerLoad(ServerLoadEvent event) {
        if (Bukkit.getPluginManager().getPlugin("MVdWPlaceholderAPI") != null) {
            new MVdWPlaceholders(this.plugin, PlotSquared.get().getPlaceholderRegistry());
            PlotSquared.log(Captions.PREFIX + "&6PlotSquared hooked into MVdWPlaceholderAPI");
        } else {
            System.out.println("Nope");
        }

    }
}
