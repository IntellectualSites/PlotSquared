package com.intellectualcrafters.plot.commands;

import com.intellectualcrafters.configuration.ConfigurationSection;
import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.config.Configuration;
import com.intellectualcrafters.plot.generator.AugmentedUtils;
import com.intellectualcrafters.plot.generator.HybridGen;
import com.intellectualcrafters.plot.generator.HybridPlotWorld;
import com.intellectualcrafters.plot.object.ChunkLoc;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.PlotArea;
import com.intellectualcrafters.plot.object.PlotId;
import com.intellectualcrafters.plot.object.PlotMessage;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.object.RegionWrapper;
import com.intellectualcrafters.plot.object.RunnableVal;
import com.intellectualcrafters.plot.object.RunnableVal3;
import com.intellectualcrafters.plot.object.SetupObject;
import com.intellectualcrafters.plot.util.ChunkManager;
import com.intellectualcrafters.plot.util.CmdConfirm;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.MathMan;
import com.intellectualcrafters.plot.util.Permissions;
import com.intellectualcrafters.plot.util.SetupUtils;
import com.intellectualcrafters.plot.util.StringMan;
import com.intellectualcrafters.plot.util.WorldUtil;
import com.plotsquared.general.commands.CommandDeclaration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Set;

@CommandDeclaration(command = "area", permission = "plots.area", category = CommandCategory.ADMINISTRATION, requiredType = RequiredType.NONE,
        description = "Create a new PlotArea", aliases = "world", usage = "/plot area <create|info|list|tp|regen>")
public class Area extends SubCommand {
    
    @Override
    public boolean onCommand(final PlotPlayer plr, String[] args) {
        if (args.length == 0) {
            C.COMMAND_SYNTAX.send(plr, getUsage());
            return false;
        }
        switch (args[0].toLowerCase()) {
            case "c":
            case "setup":
            case "create": {
                if (!Permissions.hasPermission(plr, "plots.area.create")) {
                    C.NO_PERMISSION.send(plr, "plots.area.create");
                    return false;
                }
                switch (args.length) {
                    case 1: {
                        C.COMMAND_SYNTAX.send(plr, "/plot area create [world[:id]] [<modifier>=<value>]...");
                        return false;
                    }
                    case 2: {
                        switch (args[1].toLowerCase()) {
                            case "pos1": { // Set position 1
                                HybridPlotWorld area = plr.getMeta("area_create_area");
                                if (area == null) {
                                    C.COMMAND_SYNTAX.send(plr, "/plot area create [world[:id]] [<modifier>=<value>]...");
                                    return false;
                                }
                                Location loc = plr.getLocation();
                                plr.setMeta("area_pos1", loc);
                                C.SET_ATTRIBUTE.send(plr, "area_pos1", loc.getX() + "," + loc.getZ());
                                MainUtil.sendMessage(plr, "You will now set pos2: /plot area create pos2"
                                + "\nNote: The chosen plot size may result in the created area not exactly matching your second position.");
                                return true;
                            }
                            case "pos2": { // Set position 2 and finish creation for type=2 (partial)
                                final HybridPlotWorld area = plr.getMeta("area_create_area");
                                if (area == null) {
                                    C.COMMAND_SYNTAX.send(plr, "/plot area create [world[:id]] [<modifier>=<value>]...");
                                    return false;
                                }
                                Location pos1 = plr.getLocation();
                                Location pos2 = plr.getMeta("area_pos1");
                                int dx = Math.abs(pos1.getX() - pos2.getX());
                                int dz = Math.abs(pos1.getZ() - pos2.getZ());
                                int numx = Math.max(1, (dx + 1 + area.ROAD_WIDTH + (area.SIZE / 2)) / area.SIZE);
                                int numz = Math.max(1, (dz + 1 + area.ROAD_WIDTH + (area.SIZE / 2)) / area.SIZE);
                                final int ddx = dx - (numx * area.SIZE - area.ROAD_WIDTH);
                                final int ddz = dz - (numz * area.SIZE - area.ROAD_WIDTH);
                                int bx = Math.min(pos1.getX(), pos2.getX()) + ddx;
                                int bz = Math.min(pos1.getZ(), pos2.getZ()) + ddz;
                                int tx = Math.max(pos1.getX(), pos2.getX()) - ddx;
                                int tz = Math.max(pos1.getZ(), pos2.getZ()) - ddz;
                                int lower = (area.ROAD_WIDTH & 1) == 0 ? area.ROAD_WIDTH / 2 - 1 : area.ROAD_WIDTH / 2;
                                final int offsetx = bx - (area.ROAD_WIDTH == 0 ? 0 : lower);
                                final int offsetz = bz - (area.ROAD_WIDTH == 0 ? 0 : lower);
                                final RegionWrapper region = new RegionWrapper(bx, tx, bz, tz);
                                Set<PlotArea> areas = PS.get().getPlotAreas(area.worldname, region);
                                if (!areas.isEmpty()) {
                                    C.CLUSTER_INTERSECTION.send(plr, areas.iterator().next().toString());
                                    return false;
                                }
                                final SetupObject object = new SetupObject();
                                object.world = area.worldname;
                                object.id = area.id;
                                object.terrain = area.TERRAIN;
                                object.type = area.TYPE;
                                object.min = new PlotId(1, 1);
                                object.max = new PlotId(numx, numz);
                                object.plotManager = "PlotSquared";
                                object.setupGenerator = "PlotSquared";
                                object.step = area.getSettingNodes();
                                final String path = "worlds." + area.worldname + ".areas." + area.id + "-" + object.min + "-" + object.max;
                                CmdConfirm.addPending(plr, "/plot area create pos2 (Creates world)", new Runnable() {
                                    @Override
                                    public void run() {
                                        if (offsetx != 0) {
                                            PS.get().config.set(path + ".road.offset.x", offsetx);
                                        }
                                        if (offsetz != 0) {
                                            PS.get().config.set(path + ".road.offset.z", offsetz);
                                        }
                                        final String world = SetupUtils.manager.setupWorld(object);
                                        if (WorldUtil.IMP.isWorld(world)) {
                                            PS.get().loadWorld(world, null);
                                            C.SETUP_FINISHED.send(plr);
                                            plr.teleport(WorldUtil.IMP.getSpawn(world));
                                            if (area.TERRAIN != 3) {
                                                ChunkManager.largeRegionTask(world, region, new RunnableVal<ChunkLoc>() {
                                                    @Override
                                                    public void run(final ChunkLoc value) {
                                                        AugmentedUtils.generate(world, value.x, value.z, null);
                                                    }
                                                }, null);
                                            }
                                        } else {
                                            MainUtil.sendMessage(plr, "An error occured while creating the world: " + area.worldname);
                                        }
                                    }
                                });
                                return true;
                            }
                        }
                    }
                    default: // Start creation
                        final SetupObject object = new SetupObject();
                        String[] split = args[1].split(":");
                        String id;
                        if (split.length == 2) {
                            id = split[1];
                        } else {
                            id = null;
                        }
                        object.world = split[0];
                        final HybridPlotWorld pa = new HybridPlotWorld(object.world, id, new HybridGen(), null, null);
                        PlotArea other = PS.get().getPlotArea(pa.worldname, id);
                        if (other != null && Objects.equals(pa.id, other.id)) {
                            C.SETUP_WORLD_TAKEN.send(plr, pa.toString());
                            return false;
                        }
                        Set<PlotArea> areas = PS.get().getPlotAreas(pa.worldname);
                        if (!areas.isEmpty()) {
                            PlotArea area = areas.iterator().next();
                            pa.TYPE = area.TYPE;
                        }
                        pa.SIZE = (short) (pa.PLOT_WIDTH + pa.ROAD_WIDTH);
                        for (int i = 2; i < args.length; i++) {
                            String[] pair = args[i].split("=");
                            if (pair.length != 2) {
                                C.COMMAND_SYNTAX.send(plr, "/plot area create [world[:id]] [<modifier>=<value>]...");
                                return false;
                            }
                            switch (pair[0].toLowerCase()) {
                                case "s":
                                case "size": {
                                    pa.PLOT_WIDTH = Integer.parseInt(pair[1]);
                                    pa.SIZE = (short) (pa.PLOT_WIDTH + pa.ROAD_WIDTH);
                                    break;
                                }
                                case "g":
                                case "gap": {
                                    pa.ROAD_WIDTH = Integer.parseInt(pair[1]);
                                    pa.SIZE = (short) (pa.PLOT_WIDTH + pa.ROAD_WIDTH);
                                    break;
                                }
                                case "h":
                                case "height": {
                                    int value = Integer.parseInt(pair[1]);
                                    pa.PLOT_HEIGHT = value;
                                    pa.ROAD_HEIGHT = value;
                                    pa.WALL_HEIGHT = value;
                                    break;
                                }
                                case "f":
                                case "floor": {
                                    pa.TOP_BLOCK = Configuration.BLOCKLIST.parseString(pair[1]);
                                    break;
                                }
                                case "m":
                                case "main": {
                                    pa.MAIN_BLOCK = Configuration.BLOCKLIST.parseString(pair[1]);
                                    break;
                                }
                                case "w":
                                case "wall": {
                                    pa.WALL_FILLING = Configuration.BLOCK.parseString(pair[1]);
                                    break;
                                }
                                case "b":
                                case "border": {
                                    pa.WALL_BLOCK = Configuration.BLOCK.parseString(pair[1]);
                                    break;
                                }
                                case "terrain": {
                                    pa.TERRAIN = Integer.parseInt(pair[1]);
                                    object.terrain = pa.TERRAIN;
                                    break;
                                }
                                case "type": {
                                    pa.TYPE = Integer.parseInt(pair[1]);
                                    object.type = pa.TYPE;
                                    break;
                                }
                                default: {
                                    C.COMMAND_SYNTAX.send(plr, "/plot area create [world[:id]] [<modifier>=<value>]...");
                                    return false;
                                }
                            }
                        }
                        if (pa.TYPE != 2) {
                            if (WorldUtil.IMP.isWorld(pa.worldname)) {
                                C.SETUP_WORLD_TAKEN.send(plr, pa.worldname);
                                return false;
                            }
                            CmdConfirm.addPending(plr, "/plot area " + StringMan.join(args, " "), new Runnable() {
                                @Override
                                public void run() {
                                    String path = "worlds." + pa.worldname;
                                    if (!PS.get().config.contains(path)) {
                                        PS.get().config.createSection(path);
                                    }
                                    ConfigurationSection section = PS.get().config.getConfigurationSection(path);
                                    pa.saveConfiguration(section);
                                    pa.loadConfiguration(section);
                                    object.plotManager = "PlotSquared";
                                    object.setupGenerator = "PlotSquared";
                                    String world = SetupUtils.manager.setupWorld(object);
                                    if (WorldUtil.IMP.isWorld(world)) {
                                        C.SETUP_FINISHED.send(plr);
                                        plr.teleport(WorldUtil.IMP.getSpawn(world));
                                    } else {
                                        MainUtil.sendMessage(plr, "An error occured while creating the world: " + pa.worldname);
                                    }
                                    try {
                                        PS.get().config.save(PS.get().configFile);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                            return true;
                        }
                        if (pa.id == null) {
                            C.COMMAND_SYNTAX.send(plr, "/plot area create [world[:id]] [<modifier>=<value>]...");
                            return false;
                        }
                        if (WorldUtil.IMP.isWorld(pa.worldname)) {
                            if (!plr.getLocation().getWorld().equals(pa.worldname)) {
                                plr.teleport(WorldUtil.IMP.getSpawn(pa.worldname));
                            }
                        } else {
                            object.terrain = 0;
                            object.type = 0;
                            SetupUtils.manager.setupWorld(object);
                            plr.teleport(WorldUtil.IMP.getSpawn(pa.worldname));
                        }
                        plr.setMeta("area_create_area", pa);
                        MainUtil.sendMessage(plr, "$1Go to the first corner and use: $2/plot area create pos1");
                        break;
                }
                return true;
            }
            case "i":
            case "info": {
                if (!Permissions.hasPermission(plr, "plots.area.info")) {
                    C.NO_PERMISSION.send(plr, "plots.area.info");
                    return false;
                }
                PlotArea area;
                switch (args.length) {
                    case 1:
                        area = plr.getApplicablePlotArea();
                        break;
                    case 2:
                        area = PS.get().getPlotAreaByString(args[1]);
                        break;
                    default:
                        C.COMMAND_SYNTAX.send(plr, "/plot area info [area]");
                        return false;
                }
                if (area == null) {
                    if (args.length == 2) {
                        C.NOT_VALID_PLOT_WORLD.send(plr, args[1]);
                    } else {
                        C.NOT_IN_PLOT_WORLD.send(plr);
                    }
                    return false;
                }
                String name;
                double percent;
                int claimed = area.getPlotCount();
                int clusters = area.getClusters().size();
                String region;
                String generator = area.getGenerator() + "";
                if (area.TYPE == 2) {
                    PlotId min = area.getMin();
                    PlotId max = area.getMax();
                    name = area.worldname + ";" + area.id + ";" + min + ";" + max;
                    int size = (max.x - min.x + 1) * (max.y - min.y + 1);
                    percent = claimed == 0 ? 0 : size / (double) claimed;
                    region = area.getRegion().toString();
                } else {
                    name = area.worldname;
                    percent = claimed == 0 ? 0 : (100d * claimed) / (Integer.MAX_VALUE);
                    region = "N/A";
                }
                String value = "&r$1NAME: " + name
                + "\n$1Type: $2" + area.TYPE
                + "\n$1Terrain: $2" + area.TERRAIN
                + "\n$1Usage: $2" + String.format("%.2f", percent) + "%"
                + "\n$1Claimed: $2" + claimed
                + "\n$1Clusters: $2" + clusters
                + "\n$1Region: $2" + region
                + "\n$1Generator: $2" + generator;
                MainUtil.sendMessage(plr, C.PLOT_INFO_HEADER.s() + '\n' + value + '\n' + C.PLOT_INFO_FOOTER.s(), false);
                return true;
            }
            case "l":
            case "list": {
                if (!Permissions.hasPermission(plr, "plots.area.list")) {
                    C.NO_PERMISSION.send(plr, "plots.area.list");
                    return false;
                }
                int page;
                switch (args.length) {
                    case 1:
                        page = 0;
                        break;
                    case 2:
                        if (MathMan.isInteger(args[1])) {
                            page = Integer.parseInt(args[1]) - 1;
                            break;
                        }
                    default:
                        C.COMMAND_SYNTAX.send(plr, "/plot area list [#]");
                        return false;
                }
                ArrayList<PlotArea> areas = new ArrayList<>(PS.get().getPlotAreas());
                paginate(plr, areas, 8, page, new RunnableVal3<Integer, PlotArea, PlotMessage>() {
                    @Override
                    public void run(Integer i, PlotArea area, PlotMessage message) {
                        String name;
                        double percent;
                        int claimed = area.getPlotCount();
                        int clusters = area.getClusters().size();
                        String region;
                        String generator = area.getGenerator() + "";
                        if (area.TYPE == 2) {
                            PlotId min = area.getMin();
                            PlotId max = area.getMax();
                            name = area.worldname + ";" + area.id + ";" + min + ";" + max;
                            int size = (max.x - min.x + 1) * (max.y - min.y + 1);
                            percent = claimed == 0 ? 0 : size / (double) claimed;
                            region = area.getRegion().toString();
                        } else {
                            name = area.worldname;
                            percent = claimed == 0 ? 0 : Short.MAX_VALUE * Short.MAX_VALUE / (double) claimed;
                            region = "N/A";
                        }
                        PlotMessage tooltip = new PlotMessage()
                        .text("Claimed=").color("$1").text("" + claimed).color("$2")
                        .text("\nUsage=").color("$1").text(String.format("%.2f", percent) + "%").color("$2")
                        .text("\nClusters=").color("$1").text("" + clusters).color("$2")
                        .text("\nRegion=").color("$1").text(region).color("$2")
                        .text("\nGenerator=").color("$1").text(generator).color("$2");
                        
                        // type / terrain
                        String visit = "/plot area tp " + area.toString();
                        message.text("[").color("$3")
                        .text(i + "").command(visit).tooltip(visit).color("$1")
                        .text("]").color("$3")
                        .text(" " + name).tooltip(tooltip).command("/plot area info " + area).color("$1").text(" - ").color("$2")
                        .text(area.TYPE + ":" + area.TERRAIN).color("$3");
                    }
                }, "/plot area list", C.AREA_LIST_HEADER_PAGED.s());
                return true;
            }
            case "regen":
            case "regenerate": {
                if (!Permissions.hasPermission(plr, "plots.area.regen")) {
                    C.NO_PERMISSION.send(plr, "plots.area.regen");
                    return false;
                }
                final PlotArea area = plr.getApplicablePlotArea();
                if (area == null) {
                    C.NOT_IN_PLOT_WORLD.send(plr);
                    return false;
                }
                if (area.TYPE != 2) {
                    MainUtil.sendMessage(plr, "$4Stop the server and delete: " + area.worldname + "/region");
                    return false;
                }
                ChunkManager.largeRegionTask(area.worldname, area.getRegion(), new RunnableVal<ChunkLoc>() {
                    @Override
                    public void run(ChunkLoc value) {
                        AugmentedUtils.generate(area.worldname, value.x, value.z, null);
                    }
                }, null);
                return true;
            }
            case "goto":
            case "v":
            case "teleport":
            case "visit":
            case "tp": {
                if (!Permissions.hasPermission(plr, "plots.area.tp")) {
                    C.NO_PERMISSION.send(plr, "plots.area.tp");
                    return false;
                }
                if (args.length != 2) {
                    C.COMMAND_SYNTAX.send(plr, "/plot visit [area]");
                    return false;
                }
                PlotArea area = PS.get().getPlotAreaByString(args[1]);
                if (area == null) {
                    C.NOT_VALID_PLOT_WORLD.send(plr, args[1]);
                    return false;
                }
                Location center;
                if (area.TYPE != 2) {
                    center = WorldUtil.IMP.getSpawn(area.worldname);
                } else {
                    RegionWrapper region = area.getRegion();
                    center = new Location(area.worldname, region.minX + (region.maxX - region.minX) / 2, 0, region.minZ + (region.maxZ - region.minZ) / 2);
                    center.setY(WorldUtil.IMP.getHighestBlock(area.worldname, center.getX(), center.getZ()));
                }
                plr.teleport(center);
                return true;
            }
            case "delete":
            case "remove": {
                MainUtil.sendMessage(plr, "$1World creation settings may be stored in multiple locations:"
                + "\n$3 - $2Bukkit bukkit.yml"
                + "\n$3 - $2PlotSquared settings.yml"
                + "\n$3 - $2Multiverse worlds.yml (or any world management plugin)"
                + "\n$1Stop the server and delete it from these locations.");
                return true;
            }
        }
        C.COMMAND_SYNTAX.send(plr, getUsage());
        return false;
    }
    
}
