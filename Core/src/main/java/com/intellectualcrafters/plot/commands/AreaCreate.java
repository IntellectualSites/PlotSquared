package com.intellectualcrafters.plot.commands;

import com.intellectualcrafters.configuration.ConfigurationSection;
import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.config.Configuration;
import com.intellectualcrafters.plot.generator.AugmentedUtils;
import com.intellectualcrafters.plot.generator.HybridPlotWorld;
import com.intellectualcrafters.plot.object.*;
import com.intellectualcrafters.plot.util.*;
import com.plotsquared.general.commands.Command;
import com.plotsquared.general.commands.CommandDeclaration;

import java.io.IOException;
import java.util.Objects;
import java.util.Set;

@CommandDeclaration(command = "create",
        permission = "plots.area.create",
        category = CommandCategory.ADMINISTRATION,
        requiredType = RequiredType.NONE,
        description = "Create a new PlotArea",
        aliases = {"c","setup"},
        usage = "/plot area create [world[:id]] [<modifier>=<value>]...",
        confirmation = true)
public class AreaCreate extends SubCommand {

    public AreaCreate(Command parent, boolean isStatic) { super(parent, isStatic); }

    @Override
    public boolean onCommand(final PlotPlayer player, String[] args) {
        if (args.length == 0) {
            C.COMMAND_SYNTAX.send(player, getUsage());
            return false;
        }
        if (args.length == 1) {
            switch (args[0].toLowerCase()) {
                case "pos1": // Set position 1
                    HybridPlotWorld area = player.getMeta("area_create_area");
                    if (area == null) {
                        C.COMMAND_SYNTAX.send(player, getUsage());
                        return false;
                    }
                    Location location = player.getLocation();
                    player.setMeta("area_pos1", location);
                    C.SET_ATTRIBUTE.send(player, "area_pos1", location.getX() + "," + location.getZ());
                    MainUtil.sendMessage(player, "You will now set pos2: /plot area create pos2"
                            + "\nNote: The chosen plot size may result in the created area not exactly matching your second position.");
                    return true;
                case "pos2":  // Set position 2 and finish creation for type=2 (partial)
                    final HybridPlotWorld area2 = player.getMeta("area_create_area");
                    if (area2 == null) {
                        C.COMMAND_SYNTAX.send(player, getUsage());
                        return false;
                    }
                    Location pos1 = player.getLocation();
                    Location pos2 = player.getMeta("area_pos1");
                    int dx = Math.abs(pos1.getX() - pos2.getX());
                    int dz = Math.abs(pos1.getZ() - pos2.getZ());
                    int numX = Math.max(1, (dx + 1 + area2.ROAD_WIDTH + area2.SIZE / 2) / area2.SIZE);
                    int numZ = Math.max(1, (dz + 1 + area2.ROAD_WIDTH + area2.SIZE / 2) / area2.SIZE);
                    int ddx = dx - (numX * area2.SIZE - area2.ROAD_WIDTH);
                    int ddz = dz - (numZ * area2.SIZE - area2.ROAD_WIDTH);
                    int bx = Math.min(pos1.getX(), pos2.getX()) + ddx;
                    int bz = Math.min(pos1.getZ(), pos2.getZ()) + ddz;
                    int tx = Math.max(pos1.getX(), pos2.getX()) - ddx;
                    int tz = Math.max(pos1.getZ(), pos2.getZ()) - ddz;
                    int lower = (area2.ROAD_WIDTH & 1) == 0 ? area2.ROAD_WIDTH / 2 - 1 : area2.ROAD_WIDTH / 2;
                    final int offsetX = bx - (area2.ROAD_WIDTH == 0 ? 0 : lower);
                    final int offsetZ = bz - (area2.ROAD_WIDTH == 0 ? 0 : lower);
                    final RegionWrapper region = new RegionWrapper(bx, tx, bz, tz);
                    java.util.Set<PlotArea> areas = PS.get().getPlotAreas(area2.worldname, region);
                    if (!areas.isEmpty()) {
                        C.CLUSTER_INTERSECTION.send(player, areas.iterator().next().toString());
                        return false;
                    }
                    final SetupObject object = new SetupObject();
                    object.world = area2.worldname;
                    object.id = area2.id;
                    object.terrain = area2.TERRAIN;
                    object.type = area2.TYPE;
                    object.min = new PlotId(1, 1);
                    object.max = new PlotId(numX, numZ);
                    object.plotManager = PS.imp().getPluginName();
                    object.setupGenerator = PS.imp().getPluginName();
                    object.step = area2.getSettingNodes();
                    final String path = "worlds." + area2.worldname + ".areas." + area2.id + '-' + object.min + '-' + object.max;
                    Runnable run = new Runnable() {
                        @Override
                        public void run() {
                            if (offsetX != 0) {
                                PS.get().worlds.set(path + ".road.offset.x", offsetX);
                            }
                            if (offsetZ != 0) {
                                PS.get().worlds.set(path + ".road.offset.z", offsetZ);
                            }
                            final String world = SetupUtils.manager.setupWorld(object);
                            if (WorldUtil.IMP.isWorld(world)) {
                                PS.get().loadWorld(world, null);
                                C.SETUP_FINISHED.send(player);
                                player.teleport(WorldUtil.IMP.getSpawn(world));
                                if (area2.TERRAIN != 3) {
                                    ChunkManager.largeRegionTask(world, region, new RunnableVal<ChunkLoc>() {
                                        @Override
                                        public void run(ChunkLoc value) {
                                            AugmentedUtils.generate(world, value.x, value.z, null);
                                        }
                                    }, null);
                                }
                            } else {
                                MainUtil.sendMessage(player, "An error occurred while creating the world: " + area2.worldname);
                            }
                        }
                    };
                    if (hasConfirmation(player)) {
                        CmdConfirm.addPending(player, getCommandString() + " pos2 (Creates world)", run);
                    } else {
                        run.run();
                    }
                    return true;
            }
        } else { // Start creation
            final SetupObject object = new SetupObject();
            String[] split = args[0].split(":");
            String id;
            if (split.length == 2) {
                id = split[1];
            } else {
                id = null;
            }
            object.world = split[0];
            final HybridPlotWorld pa = new HybridPlotWorld(object.world, id, PS.get().IMP.getDefaultGenerator(), null, null);
            PlotArea other = PS.get().getPlotArea(pa.worldname, id);
            if (other != null && Objects.equals(pa.id, other.id)) {
                C.SETUP_WORLD_TAKEN.send(player, pa.toString());
                return false;
            }
            Set<PlotArea> areas = PS.get().getPlotAreas(pa.worldname);
            if (!areas.isEmpty()) {
                PlotArea area = areas.iterator().next();
                pa.TYPE = area.TYPE;
            }
            pa.SIZE = (short) (pa.PLOT_WIDTH + pa.ROAD_WIDTH);
            for (int i = 1; i < args.length; i++) {
                String[] pair = args[i].split("=");
                if (pair.length != 2) {
                    C.COMMAND_SYNTAX.send(player, getUsage());
                    return false;
                }
                switch (pair[0].toLowerCase()) {
                    case "s":
                    case "size":
                        pa.PLOT_WIDTH = Integer.parseInt(pair[1]);
                        pa.SIZE = (short) (pa.PLOT_WIDTH + pa.ROAD_WIDTH);
                        break;
                    case "g":
                    case "gap":
                        pa.ROAD_WIDTH = Integer.parseInt(pair[1]);
                        pa.SIZE = (short) (pa.PLOT_WIDTH + pa.ROAD_WIDTH);
                        break;
                    case "h":
                    case "height":
                        int value = Integer.parseInt(pair[1]);
                        pa.PLOT_HEIGHT = value;
                        pa.ROAD_HEIGHT = value;
                        pa.WALL_HEIGHT = value;
                        break;
                    case "f":
                    case "floor":
                        pa.TOP_BLOCK = Configuration.BLOCKLIST.parseString(pair[1]);
                        break;
                    case "m":
                    case "main":
                        pa.MAIN_BLOCK = Configuration.BLOCKLIST.parseString(pair[1]);
                        break;
                    case "w":
                    case "wall":
                        pa.WALL_FILLING = Configuration.BLOCK.parseString(pair[1]);
                        break;
                    case "b":
                    case "border":
                        pa.WALL_BLOCK = Configuration.BLOCK.parseString(pair[1]);
                        break;
                    case "terrain":
                        pa.TERRAIN = Integer.parseInt(pair[1]);
                        object.terrain = pa.TERRAIN;
                        break;
                    case "type":
                        pa.TYPE = Integer.parseInt(pair[1]);
                        object.type = pa.TYPE;
                        break;
                    default:
                        C.COMMAND_SYNTAX.send(player, getUsage());
                        return false;
                }
            }
            if (pa.TYPE != 2) {
                if (WorldUtil.IMP.isWorld(pa.worldname)) {
                    C.SETUP_WORLD_TAKEN.send(player, pa.worldname);
                    return false;
                }
                Runnable run = new Runnable() {
                    @Override
                    public void run() {
                        String path = "worlds." + pa.worldname;
                        if (!PS.get().worlds.contains(path)) {
                            PS.get().worlds.createSection(path);
                        }
                        ConfigurationSection section = PS.get().worlds.getConfigurationSection(path);
                        pa.saveConfiguration(section);
                        pa.loadConfiguration(section);
                        object.plotManager = PS.imp().getPluginName();
                        object.setupGenerator = PS.imp().getPluginName();
                        String world = SetupUtils.manager.setupWorld(object);
                        if (WorldUtil.IMP.isWorld(world)) {
                            C.SETUP_FINISHED.send(player);
                            player.teleport(WorldUtil.IMP.getSpawn(world));
                        } else {
                            MainUtil.sendMessage(player, "An error occurred while creating the world: " + pa.worldname);
                        }
                        try {
                            PS.get().worlds.save(PS.get().worldsFile);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                };
                if (hasConfirmation(player)) {
                    CmdConfirm.addPending(player, getCommandString() + ' ' + StringMan.join(args, " "), run);
                } else {
                    run.run();
                }
                return true;
            }
            if (pa.id == null) {
                C.COMMAND_SYNTAX.send(player, getUsage());
                return false;
            }
            if (WorldUtil.IMP.isWorld(pa.worldname)) {
                if (!player.getLocation().getWorld().equals(pa.worldname)) {
                    player.teleport(WorldUtil.IMP.getSpawn(pa.worldname));
                }
            } else {
                object.terrain = 0;
                object.type = 0;
                SetupUtils.manager.setupWorld(object);
                player.teleport(WorldUtil.IMP.getSpawn(pa.worldname));
            }
            player.setMeta("area_create_area", pa);
            MainUtil.sendMessage(player, "$1Go to the first corner and use: $2 " + getCommandString() + " pos1");
        }
        return true;
    }
}
