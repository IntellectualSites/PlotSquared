package com.intellectualcrafters.plot.object;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.intellectualcrafters.plot.util.UUIDHandler;

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
        return this.inventory;
    }
    
    public String getName(UUID uuid) {
        String name = UUIDHandler.getName(this.plot.getOwner());
        if (name == null) {
            return "unknown";
        }
        return name;
    }

    public InfoInventory build() {
        final ItemStack generalInfo = getItem(Material.EMERALD, "&cPlot Info", "&cID: &6" + this.plot.getId().toString(), "&cOwner: &6" + getName(this.plot.getOwner()), "&cAlias: &6" + this.plot.settings.getAlias(), "&cBiome: &6" + this.plot.settings.getBiome().toString().replaceAll("_", "").toLowerCase(), "&cCan Build: &6" + this.plot.hasRights(this.player), "&cIs Denied: &6" + this.plot.deny_entry(this.player));
        final ItemStack helpers = getItem(Material.EMERALD, "&cHelpers", "&cAmount: &6" + this.plot.helpers.size(), "&8Click to view a list of the plot helpers");
        final ItemStack trusted = getItem(Material.EMERALD, "&cTrusted", "&cAmount: &6" + this.plot.trusted.size(), "&8Click to view a list of trusted players");
        final ItemStack denied = getItem(Material.EMERALD, "&cDenied", "&cAmount: &6" + this.plot.denied.size(), "&8Click to view a list of denied players");
        final ItemStack flags = getItem(Material.EMERALD, "&cFlags", "&cAmount: &6" + this.plot.settings.flags.size(), "&8Click to view a list of plot flags");
        this.inventory.setItem(2, generalInfo);
        this.inventory.setItem(3, helpers);
        this.inventory.setItem(4, trusted);
        this.inventory.setItem(5, denied);
        this.inventory.setItem(6, flags);
        return this;
    }

    public InfoInventory display() {
        this.player.closeInventory();
        this.player.openInventory(this.inventory);
        return this;
    }

    private ItemStack getItem(final Material material, final String name, final String... lore) {
        final ItemStack stack = new ItemStack(material);
        final ItemMeta meta = stack.getItemMeta();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
        final List<String> lList = new ArrayList<>();
        for (final String l : lore) {
            lList.add(ChatColor.translateAlternateColorCodes('&', l));
        }
        meta.setLore(lList);
        stack.setItemMeta(meta);
        return stack;
    }
}
