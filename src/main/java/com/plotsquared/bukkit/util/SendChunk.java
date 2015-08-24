package com.plotsquared.bukkit.util;

import static com.intellectualcrafters.plot.util.ReflectionUtils.getRefClass;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.bukkit.Chunk;

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

import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.Bukkit;


/**
 * An utility that can be used to send chunks, rather than using bukkit code to do so (uses heavy NMS)
 *
 * @author Empire92
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
    private RefMethod methodGetHandlePlayer;
    private RefMethod methodGetHandleChunk;
    private RefConstructor MapChunk;
    private RefField connection;
    private RefMethod send;

    /**
     * Constructor
     *
     * @throws NoSuchMethodException
     */
    public SendChunk() throws NoSuchMethodException {
        methodGetHandlePlayer = classCraftPlayer.getMethod("getHandle");
        methodGetHandleChunk = classCraftChunk.getMethod("getHandle");
        MapChunk = classMapChunk.getConstructor(classChunk.getRealClass(), boolean.class, int.class);
        connection = classEntityPlayer.getField("playerConnection");
        send = classConnection.getMethod("sendPacket", classPacket.getRealClass());
    }

    public void sendChunk(final Collection<Chunk> input) {
        HashSet<Chunk> chunks = new HashSet<Chunk>(input);
        HashMap<String, ArrayList<Chunk>> map = new HashMap<>();
        int view = Bukkit.getServer().getViewDistance();
        for (Chunk chunk : chunks) {
            String world = chunk.getWorld().getName();
            ArrayList<Chunk> list = map.get(world);
            if (list == null) {
                list = new ArrayList<>();
                map.put(world, list);
            }
            list.add(chunk);
        }
        for (PlotPlayer pp : UUIDHandler.getPlayers().values() ) {
            Plot plot = pp.getCurrentPlot();
            Location loc = null;
            String world;
            if (plot != null) {
                world = plot.world;
            }
            else {
                loc = pp.getLocation();
                world = loc.getWorld();
            }
            ArrayList<Chunk> list = map.get(world);
            if (list == null) {
                continue;
            }
            if (loc == null) {
                loc = pp.getLocation();
            }
            int cx = loc.getX() >> 4;
            int cz = loc.getZ() >> 4;
            Player player = ((BukkitPlayer) pp).player;
            Object entity = methodGetHandlePlayer.of(player).call();
            
            for (Chunk chunk : list) {
                int dx = Math.abs(cx - chunk.getX());
                int dz = Math.abs(cz - chunk.getZ());
                if (dx > view || dz > view) {
                    continue;
                }
                net.minecraft.server.v1_8_R2.Chunk c = (net.minecraft.server.v1_8_R2.Chunk) methodGetHandleChunk.of(chunk).call();
                c.initLighting();
                chunks.remove(chunk);
                Object con = connection.of(entity).get();
//                if (dx != 0 || dz != 0) {
//                    Object packet = MapChunk.create(c, true, 0);
//                    send.of(con).call(packet);
//                }
                Object packet = MapChunk.create(c, true, 65535);
                send.of(con).call(packet);
            }
        }
        for (final Chunk chunk : chunks) {
            TaskManager.runTask(new Runnable() {
                @Override
                public void run() {
                    try {
                        chunk.unload(true, true);
                    }
                    catch (Exception e) {
                        String worldname = chunk.getWorld().getName();
                        PS.debug("$4Could not save chunk: " + worldname + ";" + chunk.getX() + ";" + chunk.getZ());
                        PS.debug("$3 - $4File may be open in another process (e.g. MCEdit)");
                        PS.debug("$3 - $4" + worldname + "/level.dat or " + worldname + "level_old.dat may be corrupt (try repairing or removing these)");
                    }
                }
            });
        }
//        
//        
//        int diffx, diffz;
//         << 4;
//        for (final Chunk chunk : chunks) {
//            if (!chunk.isLoaded()) {
//                continue;
//            }
//            boolean unload = true;
//            final Object c = methodGetHandle.of(chunk).call();
//            final Object w = world.of(c).get();
//            final Object p = players.of(w).get();
//            for (final Object ep : (List<Object>) p) {
//                final int x = ((Double) locX.of(ep).get()).intValue();
//                final int z = ((Double) locZ.of(ep).get()).intValue();
//                diffx = Math.abs(x - (chunk.getX() << 4));
//                diffz = Math.abs(z - (chunk.getZ() << 4));
//                if ((diffx <= view) && (diffz <= view)) {
//                    unload = false;
//                    if (v1_7_10) {
//                        chunk.getWorld().refreshChunk(chunk.getX(), chunk.getZ());
//                        chunk.load(true);
//                    }
//                    else {
//                        final Object pair = ChunkCoordIntPairCon.create(chunk.getX(), chunk.getZ());
//                        final Object pq = chunkCoordIntPairQueue.of(ep).get();
//                        ((List) pq).add(pair);
//                    }
//                }
//            }
//            if (unload) {
//                TaskManager.runTask(new Runnable() {
//                    @Override
//                    public void run() {
//                        try {
//                            chunk.unload(true, true);
//                        }
//                        catch (Exception e) {
//                            String worldname = chunk.getWorld().getName();
//                            PS.debug("$4Could not save chunk: " + worldname + ";" + chunk.getX() + ";" + chunk.getZ());
//                            PS.debug("$3 - $4File may be open in another process (e.g. MCEdit)");
//                            PS.debug("$3 - $4" + worldname + "/level.dat or " + worldname + "level_old.dat may be corrupt (try repairing or removing these)");
//                        }
//                    }
//                });
//            }
//            
//        }
    }

    public void sendChunk(final String worldname, final List<ChunkLoc> locs) {
        final World myworld = Bukkit.getWorld(worldname);
        final ArrayList<Chunk> chunks = new ArrayList<>();
        for (final ChunkLoc loc : locs) {
            chunks.add(myworld.getChunkAt(loc.x, loc.z));
        }
        sendChunk(chunks);
    }
}
