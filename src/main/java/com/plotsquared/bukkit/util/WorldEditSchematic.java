package com.plotsquared.bukkit.util;

import java.io.File;

import org.bukkit.Bukkit;

import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.PlotId;
import com.intellectualcrafters.plot.util.MainUtil;
import com.plotsquared.bukkit.BukkitMain;
import com.sk89q.worldedit.CuboidClipboard;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.bukkit.BukkitWorld;

public class WorldEditSchematic
{
    public void saveSchematic(final String file, final String world, final PlotId id)
    {
        final Location bot = MainUtil.getPlotBottomLoc(world, id).add(1, 0, 1);
        final Location top = MainUtil.getPlotTopLoc(world, id);
        final Vector size = new Vector((top.getX() - bot.getX()) + 1, top.getY() - bot.getY() - 1, (top.getZ() - bot.getZ()) + 1);
        final Vector origin = new Vector(bot.getX(), bot.getY(), bot.getZ());
        final CuboidClipboard clipboard = new CuboidClipboard(size, origin);
        new Vector(bot.getX(), bot.getY(), bot.getZ());
        new Vector(top.getX(), top.getY(), top.getZ());
        final EditSession session = BukkitMain.worldEdit.getWorldEdit().getEditSessionFactory().getEditSession(new BukkitWorld(Bukkit.getWorld(world)), 999999999);
        clipboard.copy(session);
        try
        {
            clipboard.saveSchematic(new File(file));
            MainUtil.sendMessage(null, "&7 - &a  success: " + id);
        }
        catch (final Exception e)
        {
            e.printStackTrace();
            MainUtil.sendMessage(null, "&7 - Failed to save &c" + id);
        }
    }
}
