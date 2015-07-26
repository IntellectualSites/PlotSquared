package com.plotsquared.bukkit.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.inventory.Inventory;

import com.plotsquared.bukkit.object.InfoInventory;
import com.intellectualcrafters.plot.util.MainUtil;
import com.plotsquared.bukkit.util.bukkit.BukkitUtil;

/**
 * Created 2014-11-18 for PlotSquared
 * @author Citymonstret
 */
public class InventoryListener implements Listener {
    @EventHandler
    public void onInventoryAction(final InventoryInteractEvent event) {
        if (event.getInventory().getHolder() instanceof InfoInventory) {
            event.setResult(Event.Result.DENY);
        }
    }

    @EventHandler
    public void onInventoryClick(final InventoryClickEvent event) {
        final Inventory inventory = event.getInventory();
        final Player player = (Player) event.getWhoClicked();
        if (inventory.getHolder() instanceof InfoInventory) {
            switch (event.getSlot()) {
                case 3:
                case 4:
                case 5:
                case 6:
                    MainUtil.sendMessage(BukkitUtil.getPlayer(player), "This is not implemented yet");
                    break;
                default:
                    break;
            }
        }
    }
}
