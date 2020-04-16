/*
 *
 *       _____  _       _    _____                                _
 *      |  __ \| |     | |  / ____|                              | |
 *      | |__) | | ___ | |_| (___   __ _ _   _  __ _ _ __ ___  __| |
 *      |  ___/| |/ _ \| __|\___ \ / _` | | | |/ _` | '__/ _ \/ _` |
 *      | |    | | (_) | |_ ____) | (_| | |_| | (_| | | |  __/ (_| |
 *      |_|    |_|\___/ \__|_____/ \__, |\__,_|\__,_|_|  \___|\__,_|
 *                                    | |
 *                                    |_|
 *            PlotSquared plot management system for Minecraft
 *                  Copyright (C) 2020 IntellectualSites
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.plotsquared.bukkit.util;

import com.plotsquared.bukkit.player.BukkitPlayer;
import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.location.Location;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.util.ReflectionUtils.RefClass;
import com.plotsquared.core.util.ReflectionUtils.RefConstructor;
import com.plotsquared.core.util.ReflectionUtils.RefField;
import com.plotsquared.core.util.ReflectionUtils.RefMethod;
import com.plotsquared.core.util.task.TaskManager;
import com.plotsquared.core.util.uuid.UUIDHandler;
import com.sk89q.worldedit.math.BlockVector2;
import io.papermc.lib.PaperLib;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;

import static com.plotsquared.core.util.ReflectionUtils.getRefClass;

/**
 * An utility that can be used to send chunks, rather than using bukkit code
 * to do so (uses heavy NMS).
 */
public class SendChunk {

    private final RefMethod methodGetHandlePlayer;
    private final RefMethod methodGetHandleChunk;
    private final RefConstructor mapChunk;
    private final RefField connection;
    private final RefMethod send;
    private final RefMethod methodInitLighting;

    /**
     * Constructor.
     */
    public SendChunk() throws ClassNotFoundException, NoSuchMethodException, NoSuchFieldException {
        RefClass classCraftPlayer = getRefClass("{cb}.entity.CraftPlayer");
        this.methodGetHandlePlayer = classCraftPlayer.getMethod("getHandle");
        RefClass classCraftChunk = getRefClass("{cb}.CraftChunk");
        this.methodGetHandleChunk = classCraftChunk.getMethod("getHandle");
        RefClass classChunk = getRefClass("{nms}.Chunk");
        this.methodInitLighting = classChunk.getMethod("initLighting");
        RefClass classMapChunk = getRefClass("{nms}.PacketPlayOutMapChunk");
        this.mapChunk = classMapChunk.getConstructor(classChunk.getRealClass(), int.class);
        RefClass classEntityPlayer = getRefClass("{nms}.EntityPlayer");
        this.connection = classEntityPlayer.getField("playerConnection");
        RefClass classPacket = getRefClass("{nms}.Packet");
        RefClass classConnection = getRefClass("{nms}.PlayerConnection");
        this.send = classConnection.getMethod("sendPacket", classPacket.getRealClass());
    }

    public void sendChunk(Collection<Chunk> input) {
        HashSet<Chunk> chunks = new HashSet<>(input);
        HashMap<String, ArrayList<Chunk>> map = new HashMap<>();
        int view = Bukkit.getServer().getViewDistance();
        for (Chunk chunk : chunks) {
            String world = chunk.getWorld().getName();
            ArrayList<Chunk> list = map.computeIfAbsent(world, k -> new ArrayList<>());
            list.add(chunk);
            Object c = this.methodGetHandleChunk.of(chunk).call();
            this.methodInitLighting.of(c).call();
        }
        for (Entry<String, PlotPlayer> entry : UUIDHandler.getPlayers().entrySet()) {
            PlotPlayer pp = entry.getValue();
            Plot plot = pp.getCurrentPlot();
            Location location = null;
            String world;
            if (plot != null) {
                world = plot.getWorldName();
            } else {
                location = pp.getLocation();
                world = location.getWorld();
            }
            ArrayList<Chunk> list = map.get(world);
            if (list == null) {
                continue;
            }
            if (location == null) {
                location = pp.getLocation();
            }
            int chunkX = location.getX() >> 4;
            int chunkZ = location.getZ() >> 4;
            Player player = ((BukkitPlayer) pp).player;
            Object entity = this.methodGetHandlePlayer.of(player).call();

            for (Chunk chunk : list) {
                int dx = Math.abs(chunkX - chunk.getX());
                int dz = Math.abs(chunkZ - chunk.getZ());
                if ((dx > view) || (dz > view)) {
                    continue;
                }
                Object c = this.methodGetHandleChunk.of(chunk).call();
                chunks.remove(chunk);
                Object con = this.connection.of(entity).get();
                Object packet = null;
                try {
                    packet = this.mapChunk.create(c, 65535);
                } catch (Exception ignored) {
                }
                if (packet == null) {
                    PlotSquared.debug("Error with PacketPlayOutMapChunk reflection.");
                }
                this.send.of(con).call(packet);
            }
        }
        for (final Chunk chunk : chunks) {
            TaskManager.runTask(() -> {
                try {
                    chunk.unload(true);
                } catch (Throwable ignored) {
                    String worldName = chunk.getWorld().getName();
                    PlotSquared.debug(
                        "$4Could not save chunk: " + worldName + ';' + chunk.getX() + ";" + chunk
                            .getZ());
                    PlotSquared.debug("$3 - $4File may be open in another process (e.g. MCEdit)");
                    PlotSquared.debug("$3 - $4" + worldName + "/level.dat or " + worldName
                        + "/level_old.dat may be corrupt (try repairing or removing these)");
                }
            });
        }
    }

    public void sendChunk(String worldName, Collection<BlockVector2> chunkLocations) {
        World myWorld = Bukkit.getWorld(worldName);
        ArrayList<Chunk> chunks = new ArrayList<>();
        for (BlockVector2 loc : chunkLocations) {
            if (myWorld.isChunkLoaded(loc.getX(), loc.getZ())) {
                PaperLib.getChunkAtAsync(myWorld, loc.getX(), loc.getZ()).thenAccept(chunks::add);
            }
        }
        sendChunk(chunks);
    }
}
