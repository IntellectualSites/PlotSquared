package com.github.intellectualsites.plotsquared.plot.commands;

import com.github.intellectualsites.plotsquared.commands.CommandDeclaration;
import com.github.intellectualsites.plotsquared.configuration.ConfigurationSection;
import com.github.intellectualsites.plotsquared.plot.PlotSquared;
import com.github.intellectualsites.plotsquared.plot.config.Captions;
import com.github.intellectualsites.plotsquared.plot.config.Configuration;
import com.github.intellectualsites.plotsquared.plot.generator.AugmentedUtils;
import com.github.intellectualsites.plotsquared.plot.generator.HybridPlotWorld;
import com.github.intellectualsites.plotsquared.plot.object.Location;
import com.github.intellectualsites.plotsquared.plot.object.PlotArea;
import com.github.intellectualsites.plotsquared.plot.object.PlotId;
import com.github.intellectualsites.plotsquared.plot.object.PlotMessage;
import com.github.intellectualsites.plotsquared.plot.object.PlotPlayer;
import com.github.intellectualsites.plotsquared.plot.object.RunnableVal;
import com.github.intellectualsites.plotsquared.plot.object.RunnableVal3;
import com.github.intellectualsites.plotsquared.plot.object.SetupObject;
import com.github.intellectualsites.plotsquared.plot.object.TeleportCause;
import com.github.intellectualsites.plotsquared.plot.util.ChunkManager;
import com.github.intellectualsites.plotsquared.plot.util.CmdConfirm;
import com.github.intellectualsites.plotsquared.plot.util.MainUtil;
import com.github.intellectualsites.plotsquared.plot.util.MathMan;
import com.github.intellectualsites.plotsquared.plot.util.Permissions;
import com.github.intellectualsites.plotsquared.plot.util.SetupUtils;
import com.github.intellectualsites.plotsquared.plot.util.StringMan;
import com.github.intellectualsites.plotsquared.plot.util.WorldUtil;
import com.github.intellectualsites.plotsquared.plot.util.world.RegionUtil;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.regions.CuboidRegion;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Set;

@CommandDeclaration(command = "area", permission = "plots.area",
    category = CommandCategory.ADMINISTRATION, requiredType = RequiredType.NONE,
    description = "Create a new PlotArea", aliases = "world",
    usage = "/plot area <create|info|list|tp|regen>", confirmation = true) public class Area
    extends SubCommand {

    @Override public boolean onCommand(final PlotPlayer player, String[] args) {
        if (args.length == 0) {
            Captions.COMMAND_SYNTAX.send(player, getUsage());
            return false;
        }
        switch (args[0].toLowerCase()) {
            case "c":
            case "setup":
            case "create":
                if (!Permissions.hasPermission(player, Captions.PERMISSION_AREA_CREATE)) {
                    Captions.NO_PERMISSION.send(player, Captions.PERMISSION_AREA_CREATE);
                    return false;
                }
                switch (args.length) {
                    case 1:
                        Captions.COMMAND_SYNTAX
                            .send(player, "/plot area create [world[:id]] [<modifier>=<value>]...");
                        return false;
                    case 2:
                        switch (args[1].toLowerCase()) {
                            case "pos1": { // Set position 1
                                HybridPlotWorld area = player.getMeta("area_create_area");
                                if (area == null) {
                                    Captions.COMMAND_SYNTAX.send(player,
                                        "/plot area create [world[:id]] [<modifier>=<value>]...");
                                    return false;
                                }
                                Location location = player.getLocation();
                                player.setMeta("area_pos1", location);
                                Captions.SET_ATTRIBUTE.send(player, "area_pos1",
                                    location.getX() + "," + location.getZ());
                                MainUtil.sendMessage(player,
                                    "You will now set pos2: /plot area create pos2"
                                        + "\nNote: The chosen plot size may result in the created area not exactly matching your second position.");
                                return true;
                            }
                            case "pos2":  // Set position 2 and finish creation for type=2 (partial)
                                final HybridPlotWorld area = player.getMeta("area_create_area");
                                if (area == null) {
                                    Captions.COMMAND_SYNTAX.send(player,
                                        "/plot area create [world[:id]] [<modifier>=<value>]...");
                                    return false;
                                }
                                Location pos1 = player.getLocation();
                                Location pos2 = player.getMeta("area_pos1");
                                int dx = Math.abs(pos1.getX() - pos2.getX());
                                int dz = Math.abs(pos1.getZ() - pos2.getZ());
                                int numX = Math.max(1,
                                    (dx + 1 + area.ROAD_WIDTH + area.SIZE / 2) / area.SIZE);
                                int numZ = Math.max(1,
                                    (dz + 1 + area.ROAD_WIDTH + area.SIZE / 2) / area.SIZE);
                                int ddx = dx - (numX * area.SIZE - area.ROAD_WIDTH);
                                int ddz = dz - (numZ * area.SIZE - area.ROAD_WIDTH);
                                int bx = Math.min(pos1.getX(), pos2.getX()) + ddx;
                                int bz = Math.min(pos1.getZ(), pos2.getZ()) + ddz;
                                int tx = Math.max(pos1.getX(), pos2.getX()) - ddx;
                                int tz = Math.max(pos1.getZ(), pos2.getZ()) - ddz;
                                int lower = (area.ROAD_WIDTH & 1) == 0 ?
                                    area.ROAD_WIDTH / 2 - 1 :
                                    area.ROAD_WIDTH / 2;
                                final int offsetX = bx - (area.ROAD_WIDTH == 0 ? 0 : lower);
                                final int offsetZ = bz - (area.ROAD_WIDTH == 0 ? 0 : lower);
                                final CuboidRegion region = RegionUtil.createRegion(bx, tx, bz, tz);
                                Set<PlotArea> areas =
                                    PlotSquared.get().getPlotAreas(area.worldname, region);
                                if (!areas.isEmpty()) {
                                    Captions.CLUSTER_INTERSECTION
                                        .send(player, areas.iterator().next().toString());
                                    return false;
                                }
                                final SetupObject object = new SetupObject();
                                object.world = area.worldname;
                                object.id = area.id;
                                object.terrain = area.TERRAIN;
                                object.type = area.TYPE;
                                object.min = new PlotId(1, 1);
                                object.max = new PlotId(numX, numZ);
                                object.plotManager = PlotSquared.imp().getPluginName();
                                object.setupGenerator = PlotSquared.imp().getPluginName();
                                object.step = area.getSettingNodes();
                                final String path =
                                    "worlds." + area.worldname + ".areas." + area.id + '-'
                                        + object.min + '-' + object.max;
                                Runnable run = () -> {
                                    if (offsetX != 0) {
                                        PlotSquared.get().worlds
                                            .set(path + ".road.offset.x", offsetX);
                                    }
                                    if (offsetZ != 0) {
                                        PlotSquared.get().worlds
                                            .set(path + ".road.offset.z", offsetZ);
                                    }
                                    final String world = SetupUtils.manager.setupWorld(object);
                                    if (WorldUtil.IMP.isWorld(world)) {
                                        PlotSquared.get().loadWorld(world, null);
                                        Captions.SETUP_FINISHED.send(player);
                                        player.teleport(WorldUtil.IMP.getSpawn(world), TeleportCause.COMMAND);
                                        if (area.TERRAIN != 3) {
                                            ChunkManager.largeRegionTask(world, region,
                                                new RunnableVal<BlockVector2>() {
                                                    @Override public void run(BlockVector2 value) {
                                                        AugmentedUtils
                                                            .generate(world, value.getX(), value.getZ(),
                                                                null);
                                                    }
                                                }, null);
                                        }
                                    } else {
                                        MainUtil.sendMessage(player,
                                            "An error occurred while creating the world: "
                                                + area.worldname);
                                    }
                                };
                                if (hasConfirmation(player)) {
                                    CmdConfirm.addPending(player,
                                        getCommandString() + " create pos2 (Creates world)", run);
                                } else {
                                    run.run();
                                }
                                return true;
                        }
                    default: // Start creation
                        String[] split = args[1].split(":");
                        String id;
                        if (split.length == 2) {
                            id = split[1];
                        } else {
                            id = null;
                        }
                        final SetupObject object = new SetupObject();
                        object.world = split[0];
                        final HybridPlotWorld pa = new HybridPlotWorld(object.world, id,
                            PlotSquared.get().IMP.getDefaultGenerator(), null, null);
                        PlotArea other = PlotSquared.get().getPlotArea(pa.worldname, id);
                        if (other != null && Objects.equals(pa.id, other.id)) {
                            Captions.SETUP_WORLD_TAKEN.send(player, pa.toString());
                            return false;
                        }
                        Set<PlotArea> areas = PlotSquared.get().getPlotAreas(pa.worldname);
                        if (!areas.isEmpty()) {
                            PlotArea area = areas.iterator().next();
                            pa.TYPE = area.TYPE;
                        }
                        pa.SIZE = (short) (pa.PLOT_WIDTH + pa.ROAD_WIDTH);
                        for (int i = 2; i < args.length; i++) {
                            String[] pair = args[i].split("=");
                            if (pair.length != 2) {
                                Captions.COMMAND_SYNTAX.send(player, getCommandString()
                                    + " create [world[:id]] [<modifier>=<value>]...");
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
                                    pa.TOP_BLOCK = Configuration.BLOCK_BUCKET.parseString(pair[1]);
                                    break;
                                case "m":
                                case "main":
                                    pa.MAIN_BLOCK = Configuration.BLOCK_BUCKET.parseString(pair[1]);
                                    break;
                                case "w":
                                case "wall":
                                    pa.WALL_FILLING =
                                        Configuration.BLOCK_BUCKET.parseString(pair[1]);
                                    break;
                                case "b":
                                case "border":
                                    pa.WALL_BLOCK = Configuration.BLOCK_BUCKET.parseString(pair[1]);
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
                                    Captions.COMMAND_SYNTAX.send(player, getCommandString()
                                        + " create [world[:id]] [<modifier>=<value>]...");
                                    return false;
                            }
                        }
                        if (pa.TYPE != 2) {
                            if (WorldUtil.IMP.isWorld(pa.worldname)) {
                                Captions.SETUP_WORLD_TAKEN.send(player, pa.worldname);
                                return false;
                            }
                            Runnable run = () -> {
                                String path = "worlds." + pa.worldname;
                                if (!PlotSquared.get().worlds.contains(path)) {
                                    PlotSquared.get().worlds.createSection(path);
                                }
                                ConfigurationSection section =
                                    PlotSquared.get().worlds.getConfigurationSection(path);
                                pa.saveConfiguration(section);
                                pa.loadConfiguration(section);
                                object.plotManager = PlotSquared.imp().getPluginName();
                                object.setupGenerator = PlotSquared.imp().getPluginName();
                                String world = SetupUtils.manager.setupWorld(object);
                                if (WorldUtil.IMP.isWorld(world)) {
                                    Captions.SETUP_FINISHED.send(player);
                                    player.teleport(WorldUtil.IMP.getSpawn(world), TeleportCause.COMMAND);
                                } else {
                                    MainUtil.sendMessage(player,
                                        "An error occurred while creating the world: "
                                            + pa.worldname);
                                }
                                try {
                                    PlotSquared.get().worlds.save(PlotSquared.get().worldsFile);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            };
                            if (hasConfirmation(player)) {
                                CmdConfirm.addPending(player,
                                    getCommandString() + ' ' + StringMan.join(args, " "), run);
                            } else {
                                run.run();
                            }
                            return true;
                        }
                        if (pa.id == null) {
                            Captions.COMMAND_SYNTAX.send(player, getCommandString()
                                + " create [world[:id]] [<modifier>=<value>]...");
                            return false;
                        }
                        if (WorldUtil.IMP.isWorld(pa.worldname)) {
                            if (!player.getLocation().getWorld().equals(pa.worldname)) {
                                player.teleport(WorldUtil.IMP.getSpawn(pa.worldname), TeleportCause.COMMAND);
                            }
                        } else {
                            object.terrain = 0;
                            object.type = 0;
                            SetupUtils.manager.setupWorld(object);
                            player.teleport(WorldUtil.IMP.getSpawn(pa.worldname), TeleportCause.COMMAND);
                        }
                        player.setMeta("area_create_area", pa);
                        MainUtil.sendMessage(player,
                            "$1Go to the first corner and use: $2 " + getCommandString()
                                + " create pos1");
                        break;
                }
                return true;
            case "i":
            case "info": {
                if (!Permissions.hasPermission(player, Captions.PERMISSION_AREA_INFO)) {
                    Captions.NO_PERMISSION.send(player, Captions.PERMISSION_AREA_INFO);
                    return false;
                }
                PlotArea area;
                switch (args.length) {
                    case 1:
                        area = player.getApplicablePlotArea();
                        break;
                    case 2:
                        area = PlotSquared.get().getPlotAreaByString(args[1]);
                        break;
                    default:
                        Captions.COMMAND_SYNTAX.send(player, getCommandString() + " info [area]");
                        return false;
                }
                if (area == null) {
                    if (args.length == 2) {
                        Captions.NOT_VALID_PLOT_WORLD.send(player, args[1]);
                    } else {
                        Captions.NOT_IN_PLOT_WORLD.send(player);
                    }
                    return false;
                }
                String name;
                double percent;
                int claimed = area.getPlotCount();
                int clusters = area.getClusters().size();
                String region;
                String generator = String.valueOf(area.getGenerator());
                if (area.TYPE == 2) {
                    PlotId min = area.getMin();
                    PlotId max = area.getMax();
                    name = area.worldname + ';' + area.id + ';' + min + ';' + max;
                    int size = (max.x - min.x + 1) * (max.y - min.y + 1);
                    percent = claimed == 0 ? 0 : size / (double) claimed;
                    region = area.getRegion().toString();
                } else {
                    name = area.worldname;
                    percent = claimed == 0 ? 0 : 100d * claimed / Integer.MAX_VALUE;
                    region = "N/A";
                }
                String value = "&r$1NAME: " + name + "\n$1Type: $2" + area.TYPE + "\n$1Terrain: $2"
                    + area.TERRAIN + "\n$1Usage: $2" + String.format("%.2f", percent) + '%'
                    + "\n$1Claimed: $2" + claimed + "\n$1Clusters: $2" + clusters + "\n$1Region: $2"
                    + region + "\n$1Generator: $2" + generator;
                MainUtil.sendMessage(player,
                    Captions.PLOT_INFO_HEADER.getTranslated() + '\n' + value + '\n'
                        + Captions.PLOT_INFO_FOOTER.getTranslated(), false);
                return true;
            }
            case "l":
            case "list":
                if (!Permissions.hasPermission(player, Captions.PERMISSION_AREA_LIST)) {
                    Captions.NO_PERMISSION.send(player, Captions.PERMISSION_AREA_LIST);
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
                        Captions.COMMAND_SYNTAX.send(player, getCommandString() + " list [#]");
                        return false;
                }
                ArrayList<PlotArea> areas = new ArrayList<>(PlotSquared.get().getPlotAreas());
                paginate(player, areas, 8, page,
                    new RunnableVal3<Integer, PlotArea, PlotMessage>() {
                        @Override public void run(Integer i, PlotArea area, PlotMessage message) {
                            String name;
                            double percent;
                            int claimed = area.getPlotCount();
                            int clusters = area.getClusters().size();
                            String region;
                            String generator = String.valueOf(area.getGenerator());
                            if (area.TYPE == 2) {
                                PlotId min = area.getMin();
                                PlotId max = area.getMax();
                                name = area.worldname + ';' + area.id + ';' + min + ';' + max;
                                int size = (max.x - min.x + 1) * (max.y - min.y + 1);
                                percent = claimed == 0 ? 0 : size / (double) claimed;
                                region = area.getRegion().toString();
                            } else {
                                name = area.worldname;
                                percent = claimed == 0 ?
                                    0 :
                                    Short.MAX_VALUE * Short.MAX_VALUE / (double) claimed;
                                region = "N/A";
                            }
                            PlotMessage tooltip = new PlotMessage().text("Claimed=").color("$1")
                                .text(String.valueOf(claimed)).color("$2").text("\nUsage=")
                                .color("$1").text(String.format("%.2f", percent) + '%').color("$2")
                                .text("\nClusters=").color("$1").text(String.valueOf(clusters))
                                .color("$2").text("\nRegion=").color("$1").text(region).color("$2")
                                .text("\nGenerator=").color("$1").text(generator).color("$2");

                            // type / terrain
                            String visit = "/plot area tp " + area.toString();
                            message.text("[").color("$3").text(String.valueOf(i)).command(visit)
                                .tooltip(visit).color("$1").text("]").color("$3").text(' ' + name)
                                .tooltip(tooltip).command(getCommandString() + " info " + area)
                                .color("$1").text(" - ").color("$2")
                                .text(area.TYPE + ":" + area.TERRAIN).color("$3");
                        }
                    }, "/plot area list", Captions.AREA_LIST_HEADER_PAGED.getTranslated());
                return true;
            case "regen":
            case "clear":
            case "reset":
            case "regenerate": {
                if (!Permissions.hasPermission(player, Captions.PERMISSION_AREA_REGEN)) {
                    Captions.NO_PERMISSION.send(player, Captions.PERMISSION_AREA_REGEN);
                    return false;
                }
                final PlotArea area = player.getApplicablePlotArea();
                if (area == null) {
                    Captions.NOT_IN_PLOT_WORLD.send(player);
                    return false;
                }
                if (area.TYPE != 2) {
                    MainUtil.sendMessage(player,
                        "$4Stop the server and delete: " + area.worldname + "/region");
                    return false;
                }
                ChunkManager
                    .largeRegionTask(area.worldname, area.getRegion(), new RunnableVal<BlockVector2>() {
                        @Override public void run(BlockVector2 value) {
                            AugmentedUtils.generate(area.worldname, value.getX(), value.getZ(), null);
                        }
                    }, () -> player.sendMessage("Regen complete"));
                return true;
            }
            case "goto":
            case "v":
            case "teleport":
            case "visit":
            case "tp":
                if (!Permissions.hasPermission(player, Captions.PERMISSION_AREA_TP)) {
                    Captions.NO_PERMISSION.send(player, Captions.PERMISSION_AREA_TP);
                    return false;
                }
                if (args.length != 2) {
                    Captions.COMMAND_SYNTAX.send(player, "/plot visit [area]");
                    return false;
                }
                PlotArea area = PlotSquared.get().getPlotAreaByString(args[1]);
                if (area == null) {
                    Captions.NOT_VALID_PLOT_WORLD.send(player, args[1]);
                    return false;
                }
                Location center;
                if (area.TYPE != 2) {
                    center = WorldUtil.IMP.getSpawn(area.worldname);
                } else {
                    CuboidRegion region = area.getRegion();
                    center =
                        new Location(area.worldname, region.getMinimumPoint().getX() + (region.getMaximumPoint().getX() - region.getMinimumPoint().getX()) / 2,
                            0, region.getMinimumPoint().getZ() + (region.getMaximumPoint().getZ() - region.getMinimumPoint().getZ()) / 2);
                    center.setY(1 + WorldUtil.IMP
                        .getHighestBlock(area.worldname, center.getX(), center.getZ()));
                }
                player.teleport(center, TeleportCause.COMMAND);
                return true;
            case "delete":
            case "remove":
                MainUtil.sendMessage(player,
                    "$1World creation settings may be stored in multiple locations:"
                        + "\n$3 - $2Bukkit bukkit.yml" + "\n$3 - $2" + PlotSquared.imp()
                        .getPluginName() + " settings.yml"
                        + "\n$3 - $2Multiverse worlds.yml (or any world management plugin)"
                        + "\n$1Stop the server and delete it from these locations.");
                return true;
        }
        Captions.COMMAND_SYNTAX.send(player, getUsage());
        return false;
    }

}
