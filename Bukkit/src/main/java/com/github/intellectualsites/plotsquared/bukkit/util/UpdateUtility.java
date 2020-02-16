package com.github.intellectualsites.plotsquared.bukkit.util;

import com.github.intellectualsites.plotsquared.plot.PlotSquared;
import com.github.intellectualsites.plotsquared.plot.config.Captions;
import com.github.intellectualsites.plotsquared.plot.config.Settings;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

public class UpdateUtility implements Listener {

    private final JavaPlugin javaPlugin;
    private final String internalVersion;
    private String spigotVersion;

    public UpdateUtility(final JavaPlugin javaPlugin) {
        this.javaPlugin = javaPlugin;
        this.internalVersion = javaPlugin.getDescription().getVersion();
    }

    public void updateChecker() {
        new BukkitRunnable() {
            public void run() {
                Bukkit.getScheduler().runTaskAsynchronously(UpdateUtility.this.javaPlugin, () -> {
                    if (Settings.Enabled_Components.UPDATE_NOTIFICATIONS) {
                        try {
                            HttpsURLConnection connection = (HttpsURLConnection) new URL("https://api.spigotmc.org/legacy/update.php?resource=1177").openConnection();
                            connection.setRequestMethod("GET");
                            UpdateUtility.this.spigotVersion = (new BufferedReader(new InputStreamReader(connection.getInputStream()))).readLine();
                        } catch (IOException e) {
                            PlotSquared.log(Captions.PREFIX + "&6Unable to check for updates because: " + e);
                            this.cancel();
                            return;
                        }

                        if (!UpdateUtility.this.internalVersion.equals(UpdateUtility.this.spigotVersion)) {
                            PlotSquared.log(Captions.PREFIX + "&6There appears to be a PlotSquared update available!");
                            PlotSquared.log(Captions.PREFIX + "&6https://www.spigotmc.org/resources/1177/updates");
                            Bukkit.getScheduler().runTask(UpdateUtility.this.javaPlugin, () -> Bukkit.getPluginManager().registerEvents(new Listener() {
                                @EventHandler(priority = EventPriority.MONITOR)
                                public void onPlayerJoin(final PlayerJoinEvent event) {
                                    final Player player = event.getPlayer();
                                    if (player.hasPermission("plots.admin.update.notify")) {
                                        PlotSquared.log(Captions.PREFIX + "&6There appears to be a PlotSquared update available!");
                                        PlotSquared.log(Captions.PREFIX + "&6https://www.spigotmc.org/resources/1177/updates");
                                    }
                                }
                            }, UpdateUtility.this.javaPlugin));
                        } else {
                            PlotSquared.log(Captions.PREFIX + "Congratulations! You are running the latest PlotSquared version.");
                        }
                    } this.cancel();
                });
            }
        }.runTaskTimer(this.javaPlugin, 0L, 12000L);
    }
}