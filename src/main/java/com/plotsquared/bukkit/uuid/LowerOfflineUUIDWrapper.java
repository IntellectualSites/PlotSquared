package com.plotsquared.bukkit.uuid;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.entity.Player;

import com.google.common.base.Charsets;
import com.google.common.collect.BiMap;
import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.object.OfflinePlotPlayer;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.object.StringWrapper;
import com.intellectualcrafters.plot.util.UUIDHandler;
import com.plotsquared.bukkit.object.BukkitOfflinePlayer;

public class LowerOfflineUUIDWrapper extends OfflineUUIDWrapper
{
    private Method getOnline = null;
    private final Object[] arg = new Object[0];

    public LowerOfflineUUIDWrapper()
    {
        try
        {
            getOnline = Server.class.getMethod("getOnlinePlayers", new Class[0]);
        }
        catch (final NoSuchMethodException e)
        {
            e.printStackTrace();
        }
        catch (final SecurityException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public UUID getUUID(final PlotPlayer player)
    {
        return UUID.nameUUIDFromBytes(("OfflinePlayer:" + player.getName().toLowerCase()).getBytes(Charsets.UTF_8));
    }

    @Override
    public UUID getUUID(final OfflinePlotPlayer player)
    {
        return UUID.nameUUIDFromBytes(("OfflinePlayer:" + player.getName().toLowerCase()).getBytes(Charsets.UTF_8));
    }

    @Override
    public UUID getUUID(final OfflinePlayer player)
    {
        return UUID.nameUUIDFromBytes(("OfflinePlayer:" + player.getName().toLowerCase()).getBytes(Charsets.UTF_8));
    }

    @Override
    public OfflinePlotPlayer getOfflinePlayer(final UUID uuid)
    {
        final BiMap<UUID, StringWrapper> map = UUIDHandler.getUuidMap().inverse();
        String name;
        try
        {
            name = map.get(uuid).value;
        }
        catch (final NullPointerException e)
        {
            name = null;
        }
        if (name != null)
        {
            final OfflinePlayer op = Bukkit.getOfflinePlayer(name);
            if (op.hasPlayedBefore()) { return new BukkitOfflinePlayer(op); }
        }
        for (final OfflinePlayer player : Bukkit.getOfflinePlayers())
        {
            if (getUUID(player).equals(uuid)) { return new BukkitOfflinePlayer(player); }
        }
        return null;
    }

    @Override
    public Player[] getOnlinePlayers()
    {
        if (getOnline == null) { return Bukkit.getOnlinePlayers().toArray(new Player[0]); }
        try
        {
            final Object players = getOnline.invoke(Bukkit.getServer(), arg);
            if (players instanceof Player[])
            {
                return (Player[]) players;
            }
            else
            {
                @SuppressWarnings("unchecked")
                final Collection<? extends Player> p = (Collection<? extends Player>) players;
                return p.toArray(new Player[0]);
            }
        }
        catch (final Exception e)
        {
            PS.debug("Failed to resolve online players");
            getOnline = null;
            return Bukkit.getOnlinePlayers().toArray(new Player[0]);
        }
    }

    @Override
    public UUID getUUID(final String name)
    {
        return UUID.nameUUIDFromBytes(("OfflinePlayer:" + name.toLowerCase()).getBytes(Charsets.UTF_8));
    }

    @Override
    public OfflinePlotPlayer[] getOfflinePlayers()
    {
        final OfflinePlayer[] ops = Bukkit.getOfflinePlayers();
        final BukkitOfflinePlayer[] toReturn = new BukkitOfflinePlayer[ops.length];
        for (int i = 0; i < ops.length; i++)
        {
            toReturn[i] = new BukkitOfflinePlayer(ops[i]);
        }
        return toReturn;
    }
}
