package com.intellectualcrafters.plot.commands;

import com.intellectualcrafters.plot.C;
import com.intellectualcrafters.plot.PlayerFunctions;
import com.intellectualcrafters.plot.Plot;
import com.intellectualcrafters.plot.listeners.PlotPlusListener;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
public class MusicSubcommand extends SubCommand {
    public MusicSubcommand() {
        super("music", "plots.music", "Play music in plot", "music", "mus", CommandCategory.ACTIONS, true);
    }
    @Override
    public boolean execute(Player player, String... args) {
        if(!PlayerFunctions.isInPlot(player)) {
            sendMessage(player, C.NOT_IN_PLOT);
            return true;
        }
        Plot plot = PlayerFunctions.getCurrentPlot(player);
        if(!plot.hasRights(player)) {
            sendMessage(player, C.NO_PLOT_PERMS);
            return true;
        }
        org.bukkit.inventory.Inventory inventory = Bukkit.createInventory(null, 9, ChatColor.RED + "Plot Jukebox");
        for(PlotPlusListener.RecordMeta meta : PlotPlusListener.RecordMeta.metaList) {
            ItemStack stack = new ItemStack(meta.getMaterial());
            ItemMeta itemMeta = stack.getItemMeta();
            itemMeta.setDisplayName(ChatColor.GOLD + meta.toString());
            itemMeta.setLore(Arrays.asList(ChatColor.GRAY + "Click to play the record"));
            stack.setItemMeta(itemMeta);
            inventory.addItem(stack);
        }
        player.openInventory(inventory);
        return true;
    }
}