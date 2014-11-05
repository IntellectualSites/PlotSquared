package com.intellectualcrafters.plot.commands;

import java.util.Arrays;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.intellectualcrafters.plot.C;
import com.intellectualcrafters.plot.PlayerFunctions;
import com.intellectualcrafters.plot.Plot;
import com.intellectualcrafters.plot.listeners.PlotPlusListener;

public class MusicSubcommand extends SubCommand {
    public MusicSubcommand() {
        super("music", "plots.music", "Play music in plot", "music", "mus", CommandCategory.ACTIONS, true);
    }

    @Override
    public boolean execute(final Player player, final String... args) {
        if (!PlayerFunctions.isInPlot(player)) {
            sendMessage(player, C.NOT_IN_PLOT);
            return true;
        }
        final Plot plot = PlayerFunctions.getCurrentPlot(player);
        if (!plot.hasRights(player)) {
            sendMessage(player, C.NO_PLOT_PERMS);
            return true;
        }
        final org.bukkit.inventory.Inventory inventory = Bukkit.createInventory(null, 9, ChatColor.RED + "Plot Jukebox");
        for (final PlotPlusListener.RecordMeta meta : PlotPlusListener.RecordMeta.metaList) {
            final ItemStack stack = new ItemStack(meta.getMaterial());
            final ItemMeta itemMeta = stack.getItemMeta();
            itemMeta.setDisplayName(ChatColor.GOLD + meta.toString());
            itemMeta.setLore(Arrays.asList(ChatColor.GRAY + "Click to play the record"));
            stack.setItemMeta(itemMeta);
            inventory.addItem(stack);
        }
        player.openInventory(inventory);
        return true;
    }
}
