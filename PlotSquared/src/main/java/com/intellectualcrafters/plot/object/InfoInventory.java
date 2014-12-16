package com.intellectualcrafters.plot.object;

import com.intellectualcrafters.plot.util.UUIDHandler;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * Created 2014-11-18 for PlotSquared
 *
 * @author Citymonstret
 */
public class InfoInventory implements InventoryHolder {

    private final Plot plot;
    private final Inventory inventory;
    private final Player player;

    /**
     * Constructor
     *
     * @param plot from which we take information
     */
    public InfoInventory(final Plot plot, final Player player) {
        this.plot = plot;
        this.player = player;
        this.inventory = Bukkit.createInventory(this, 9, "Plot: " + plot.id.toString());
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public InfoInventory build() {
        ItemStack generalInfo =
                getItem(Material.EMERALD, "&cPlot Info",
                        "&cID: &6" + plot.getId().toString(),
                        "&cOwner: &6" + UUIDHandler.getName(plot.getOwner()),
                        "&cAlias: &6" + plot.settings.getAlias(),
                        "&cBiome: &6" + plot.settings.getBiome().toString().replaceAll("_", "").toLowerCase(),
                        "&cCan Build: &6" + plot.hasRights(player),
                        "&cIs Denied: &6" + plot.deny_entry(player)
                );
        ItemStack helpers =
                getItem(Material.EMERALD, "&cHelpers",
                        "&cAmount: &6" + plot.helpers.size(),
                        "&8Click to view a list of the plot helpers"
                );
        ItemStack trusted =
                getItem(Material.EMERALD, "&cTrusted",
                        "&cAmount: &6" + plot.trusted.size(),
                        "&8Click to view a list of trusted players"
                );
        ItemStack denied =
                getItem(Material.EMERALD, "&cDenied",
                        "&cAmount: &6" + plot.denied.size(),
                        "&8Click to view a list of denied players"
                );
        ItemStack flags =
                getItem(Material.EMERALD, "&cFlags",
                        "&cAmount: &6" + plot.settings.getFlags().size(),
                        "&8Click to view a list of plot flags"
                );
        inventory.setItem(2, generalInfo);
        inventory.setItem(3, helpers);
        inventory.setItem(4, trusted);
        inventory.setItem(5, denied);
        inventory.setItem(6, flags);
        return this;
    }

    public InfoInventory display() {
        player.closeInventory();
        player.openInventory(inventory);
        return this;
    }

    private ItemStack getItem(Material material, String name, String... lore) {
        ItemStack stack = new ItemStack(material);
        ItemMeta meta = stack.getItemMeta();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
        List<String> lList = new ArrayList<>();
        for (String l : lore)
            lList.add(ChatColor.translateAlternateColorCodes('&', l));
        meta.setLore(lList);
        stack.setItemMeta(meta);
        return stack;
    }
}
