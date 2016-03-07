package com.plotsquared.bukkit.util;

import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.object.ChunkLoc;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.ReflectionUtils.RefClass;
import com.intellectualcrafters.plot.util.ReflectionUtils.RefConstructor;
import com.intellectualcrafters.plot.util.ReflectionUtils.RefField;
import com.intellectualcrafters.plot.util.ReflectionUtils.RefMethod;
import com.intellectualcrafters.plot.util.TaskManager;
import com.intellectualcrafters.plot.util.UUIDHandler;
import com.plotsquared.bukkit.object.BukkitPlayer;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.Map.Entry;

import static com.intellectualcrafters.plot.util.ReflectionUtils.getRefClass;

/**
 * An utility that can be used to send chunks, rather than using bukkit code to do so (uses heavy NMS)
 *

 */
public class SendChunk {

    //    // Ref Class
    private final RefClass classEntityPlayer = getRefClass("{nms}.EntityPlayer");
    private final RefClass classMapChunk = getRefClass("{nms}.PacketPlayOutMapChunk");
    private final RefClass classPacket = getRefClass("{nms}.Packet");
    private final RefClass classConnection = getRefClass("{nms}.PlayerConnection");
    private final RefClass classChunk = getRefClass("{nms}.Chunk");
    private final RefClass classCraftPlayer = getRefClass("{cb}.entity.CraftPlayer");
    private final RefClass classCraftChunk = getRefClass("{cb}.CraftChunk");
    private final RefMethod methodGetHandlePlayer;
    private final RefMethod methodGetHandleChunk;
    private final RefConstructor MapChunk;
    private final RefField connection;
    private final RefMethod send;
    private final RefMethod methodInitLighting;

    /**
     * Constructor
     */
    public SendChunk() {
        methodGetHandlePlayer = classCraftPlayer.getMethod("getHandle");
        methodGetHandleChunk = classCraftChunk.getMethod("getHandle");
        methodInitLighting = classChunk.getMethod("initLighting");
        MapChunk = classMapChunk.getConstructor(classChunk.getRealClass(), boolean.class, int.class);
        connection = classEntityPlayer.getField("playerConnection");
        send = classConnection.getMethod("sendPacket", classPacket.getRealClass());
    }

    public void sendChunk(final Collection<Chunk> input) {
        final HashSet<Chunk> chunks = new HashSet<Chunk>(input);
        final HashMap<String, ArrayList<Chunk>> map = new HashMap<>();
        final int view = Bukkit.getServer().getViewDistance();
        for (final Chunk chunk : chunks) {
            final String world = chunk.getWorld().getName();
            ArrayList<Chunk> list = map.get(world);
            if (list == null) {
                list = new ArrayList<>();
                map.put(world, list);
            }
            list.add(chunk);
            final Object c = methodGetHandleChunk.of(chunk).call();
            methodInitLighting.of(c).call();
        }
        for (Entry<String, PlotPlayer> entry : UUIDHandler.getPlayers().entrySet()) {
            PlotPlayer pp = entry.getValue();
            final Plot plot = pp.getCurrentPlot();
            Location loc = null;
            String world;
            if (plot != null) {
                world = plot.getArea().worldname;
            } else {
                loc = pp.getLocation();
                world = loc.getWorld();
            }
            final ArrayList<Chunk> list = map.get(world);
            if (list == null) {
                continue;
            }
            if (loc == null) {
                loc = pp.getLocation();
            }
            final int cx = loc.getX() >> 4;
            final int cz = loc.getZ() >> 4;
            final Player player = ((BukkitPlayer) pp).player;
            final Object entity = methodGetHandlePlayer.of(player).call();

            for (final Chunk chunk : list) {
                final int dx = Math.abs(cx - chunk.getX());
                final int dz = Math.abs(cz - chunk.getZ());
                if ((dx > view) || (dz > view)) {
                    continue;
                }
                final Object c = methodGetHandleChunk.of(chunk).call();
                chunks.remove(chunk);
                final Object con = connection.of(entity).get();
                final Object packet = MapChunk.create(c, true, 65535);
                send.of(con).call(packet);
            }
        }
        for (final Chunk chunk : chunks) {
            TaskManager.runTask(new Runnable() {
                @Override
                public void run() {
                    try {
                        chunk.unload(true, false);
                    } catch (final Throwable e) {
                        final String worldname = chunk.getWorld().getName();
                        PS.debug("$4Could not save chunk: " + worldname + ";" + chunk.getX() + ";" + chunk.getZ());
                        PS.debug("$3 - $4File may be open in another process (e.g. MCEdit)");
                        PS.debug("$3 - $4" + worldname + "/level.dat or " + worldname
                                + "/level_old.dat may be corrupt (try repairing or removing these)");
                    }
                }
            });
        }
    }

    public void sendChunk(final String worldname, final Collection<ChunkLoc> locs) {
        final World myworld = Bukkit.getWorld(worldname);
        final ArrayList<Chunk> chunks = new ArrayList<>();
        for (final ChunkLoc loc : locs) {
            if (myworld.isChunkLoaded(loc.x, loc.z)) {
                chunks.add(myworld.getChunkAt(loc.x, loc.z));
            }
        }
        sendChunk(chunks);
    }
}
