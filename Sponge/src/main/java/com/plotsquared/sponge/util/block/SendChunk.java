package com.plotsquared.sponge.util.block;

import com.flowpowered.math.vector.Vector3i;
import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.object.ChunkLoc;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.TaskManager;
import com.intellectualcrafters.plot.util.UUIDHandler;
import com.plotsquared.sponge.object.SpongePlayer;
import com.plotsquared.sponge.util.SpongeUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.play.server.S21PacketChunkData;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.world.Chunk;
import org.spongepowered.api.world.World;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;

/**
 * An utility that can be used to send chunks, rather than using bukkit code to do so (uses heavy NMS)
 *

 */
public class SendChunk {

    public void sendChunk(Collection<Chunk> input) {
        HashSet<Chunk> chunks = new HashSet<Chunk>(input);
        HashMap<String, ArrayList<Chunk>> map = new HashMap<>();
        for (Chunk chunk : chunks) {
            String world = chunk.getWorld().getName();
            ArrayList<Chunk> list = map.get(world);
            if (list == null) {
                list = new ArrayList<>();
                map.put(world, list);
            }
            list.add(chunk);
            ((net.minecraft.world.chunk.Chunk) chunk).generateSkylightMap();
        }
        for (Entry<String, PlotPlayer> entry : UUIDHandler.getPlayers().entrySet()) {
            PlotPlayer pp = entry.getValue();
            Plot plot = pp.getCurrentPlot();
            Location loc = null;
            String world;
            if (plot != null) {
                world = plot.getArea().worldname;
            } else {
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
            Player player = ((SpongePlayer) pp).player;
            int view = player.getViewDistance();
            EntityPlayer nmsPlayer = (EntityPlayer) player;
            if (!(nmsPlayer instanceof EntityPlayerMP)) {
                PS.debug("Cannot send chunk change to: " + pp.getName());
                return;
            }
            EntityPlayerMP nmsPlayerMP = (EntityPlayerMP) nmsPlayer;
            for (Chunk chunk : list) {
                Vector3i min = chunk.getBlockMin();
                int dx = Math.abs(cx - (min.getX() >> 4));
                int dz = Math.abs(cz - (min.getZ() >> 4));
                if ((dx > view) || (dz > view)) {
                    continue;
                }
                chunks.remove(chunk);
                NetHandlerPlayServer con = nmsPlayerMP.playerNetServerHandler;
                net.minecraft.world.chunk.Chunk  nmsChunk = (net.minecraft.world.chunk.Chunk) chunk;
                S21PacketChunkData packet = new S21PacketChunkData(nmsChunk, true, 65535);
                con.sendPacket(packet);
            }
        }
        for (Chunk chunk : chunks) {
            TaskManager.runTask(new Runnable() {
                @Override
                public void run() {
                    chunk.unloadChunk();
                }
            });
        }
    }

    public void sendChunk(String worldName, List<ChunkLoc> chunkLocations) {
        World spongeWorld = SpongeUtil.getWorld(worldName);
        ArrayList<Chunk> chunks = new ArrayList<>();
        for (ChunkLoc loc : chunkLocations) {
            chunks.add(spongeWorld.getChunk(loc.x, 0, loc.z).get());
        }
        sendChunk(chunks);
    }
}
