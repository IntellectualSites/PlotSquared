////////////////////////////////////////////////////////////////////////////////////////////////////
// PlotSquared - A plot manager and world generator for the Bukkit API                             /
// Copyright (c) 2014 IntellectualSites/IntellectualCrafters                                       /
//                                                                                                 /
// This program is free software; you can redistribute it and/or modify                            /
// it under the terms of the GNU General Public License as published by                            /
// the Free Software Foundation; either version 3 of the License, or                               /
// (at your option) any later version.                                                             /
//                                                                                                 /
// This program is distributed in the hope that it will be useful,                                 /
// but WITHOUT ANY WARRANTY; without even the implied warranty of                                  /
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the                                   /
// GNU General Public License for more details.                                                    /
//                                                                                                 /
// You should have received a copy of the GNU General Public License                               /
// along with this program; if not, write to the Free Software Foundation,                         /
// Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301  USA                               /
//                                                                                                 /
// You can contact us via: support@intellectualsites.com                                           /
////////////////////////////////////////////////////////////////////////////////////////////////////
package com.intellectualcrafters.plot.commands;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleScriptContext;

import com.google.common.io.Files;
import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.config.Settings;
import com.intellectualcrafters.plot.database.DBFunc;
import com.intellectualcrafters.plot.flag.FlagManager;
import com.intellectualcrafters.plot.generator.HybridUtils;
import com.intellectualcrafters.plot.object.ChunkLoc;
import com.intellectualcrafters.plot.object.ConsolePlayer;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.OfflinePlotPlayer;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotAnalysis;
import com.intellectualcrafters.plot.object.PlotArea;
import com.intellectualcrafters.plot.object.PlotBlock;
import com.intellectualcrafters.plot.object.PlotId;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.object.RunnableVal;
import com.intellectualcrafters.plot.util.AbstractTitle;
import com.intellectualcrafters.plot.util.ChunkManager;
import com.intellectualcrafters.plot.util.EconHandler;
import com.intellectualcrafters.plot.util.EventUtil;
import com.intellectualcrafters.plot.util.ExpireManager;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.MathMan;
import com.intellectualcrafters.plot.util.SchematicHandler;
import com.intellectualcrafters.plot.util.SetupUtils;
import com.intellectualcrafters.plot.util.StringMan;
import com.intellectualcrafters.plot.util.TaskManager;
import com.intellectualcrafters.plot.util.UUIDHandler;
import com.intellectualcrafters.plot.util.WorldUtil;
import com.plotsquared.general.commands.Command;
import com.plotsquared.general.commands.CommandDeclaration;

@CommandDeclaration(command = "debugexec", permission = "plots.admin", description = "Mutli-purpose debug command", aliases = "exec",
        category = CommandCategory.DEBUG)
public class DebugExec extends SubCommand {
    
    private ScriptEngine engine;
    private Bindings scope;
    
    public DebugExec() {
        try {
            if (PS.get() != null) {
                final File file = new File(PS.get().IMP.getDirectory(), "scripts" + File.separator + "start.js");
                if (file.exists()) {
                    init();
                    final String script = StringMan.join(Files
                                    .readLines(new File(new File(PS.get().IMP.getDirectory() + File.separator + "scripts"), "start.js"),
                                            StandardCharsets.UTF_8),
                            System.getProperty("line.separator"));
                    scope.put("THIS", this);
                    scope.put("PlotPlayer", ConsolePlayer.getConsole());
                    engine.eval(script, scope);
                }
            }
        } catch (IOException | ScriptException e) {}
    }
    
    public ScriptEngine getEngine() {
        return engine;
    }
    
    public Bindings getScope() {
        return scope;
    }
    
    public void init() {
        if (engine != null) {
            return;
        }
        engine = new ScriptEngineManager(null).getEngineByName("nashorn");
        if (engine == null) {
            engine = new ScriptEngineManager(null).getEngineByName("JavaScript");
        }
        final ScriptContext context = new SimpleScriptContext();
        scope = context.getBindings(ScriptContext.ENGINE_SCOPE);
        
        // stuff
        scope.put("MainUtil", new MainUtil());
        scope.put("Settings", new Settings());
        scope.put("StringMan", new StringMan());
        scope.put("MathMan", new MathMan());
        scope.put("FlagManager", new FlagManager());
        
        // Classes
        scope.put("Location", Location.class);
        scope.put("PlotBlock", PlotBlock.class);
        scope.put("Plot", Plot.class);
        scope.put("PlotId", PlotId.class);
        scope.put("Runnable", Runnable.class);
        scope.put("RunnableVal", RunnableVal.class);
        
        // Instances
        scope.put("PS", PS.get());
        scope.put("TaskManager", PS.get().TASK);
        scope.put("TitleManager", AbstractTitle.TITLE_CLASS);
        scope.put("ConsolePlayer", ConsolePlayer.getConsole());
        scope.put("SchematicHandler", SchematicHandler.manager);
        scope.put("ChunkManager", ChunkManager.manager);
        scope.put("BlockManager", WorldUtil.IMP);
        scope.put("SetupUtils", SetupUtils.manager);
        scope.put("EventUtil", EventUtil.manager);
        scope.put("EconHandler", EconHandler.manager);
        scope.put("UUIDHandler", UUIDHandler.implementation);
        scope.put("DBFunc", DBFunc.dbManager);
        scope.put("HybridUtils", HybridUtils.manager);
        scope.put("IMP", PS.get().IMP);
        scope.put("MainCommand", MainCommand.getInstance());
        
        // enums
        for (final Enum<?> value : C.values()) {
            scope.put("C_" + value.name(), value);
        }
    }
    
    @Override
    public boolean onCommand(final PlotPlayer player, final String... args) {
        final List<String> allowed_params = Arrays.asList("calibrate-analysis", "remove-flag", "stop-expire", "start-expire", "show-expired", "update-expired", "seen", "trim-check");
        if (args.length > 0) {
            final String arg = args[0].toLowerCase();
            String script;
            boolean async = false;
            switch (arg) {
                case "analyze": {
                    final Plot plot = player.getCurrentPlot();
                    if (plot == null) {
                        MainUtil.sendMessage(player, C.NOT_IN_PLOT);
                        return false;
                    }
                    final PlotAnalysis analysis = plot.getComplexity();
                    if (analysis != null) {
                        final int complexity = analysis.getComplexity();
                        MainUtil.sendMessage(player, "Changes/column: " + analysis.changes / 1.0);
                        MainUtil.sendMessage(player, "Complexity: " + complexity);
                        return true;
                    }
                    MainUtil.sendMessage(player, "$1Starting task...");
                    HybridUtils.manager.analyzePlot(plot, new RunnableVal<PlotAnalysis>() {
                        @Override
                        public void run(PlotAnalysis value) {
                            MainUtil.sendMessage(player, "$1Done: $2use $3/plot debugexec analyze$2 for more information");
                        }
                    });
                    return true;
                }
                case "calibrate-analysis":
                    if (args.length != 2) {
                        MainUtil.sendMessage(player, C.COMMAND_SYNTAX, "/plot debugexec analyze <threshold>");
                        MainUtil.sendMessage(player, "$1<threshold> $2= $1The percentage of plots you want to clear (100 clears 100% of plots so no point calibrating it)");
                        return false;
                    }
                    double threshold;
                    try {
                        threshold = Integer.parseInt(args[1]) / 100d;
                    } catch (final NumberFormatException e) {
                        MainUtil.sendMessage(player, "$2Invalid threshold: " + args[1]);
                        MainUtil.sendMessage(player, "$1<threshold> $2= $1The percentage of plots you want to clear as a number between 0 - 100");
                        return false;
                    }
                    PlotAnalysis.calcOptimalModifiers(new Runnable() {
                        @Override
                        public void run() {
                            MainUtil.sendMessage(player, "$1Thank you for calibrating PlotSquared plot expiry");
                        }
                    }, threshold);
                    return true;
                case "stop-expire":
                    if (ExpireManager.task != -1) {
                        PS.get().TASK.cancelTask(ExpireManager.task);
                    } else {
                        return MainUtil.sendMessage(player, "Task already halted");
                    }
                    ExpireManager.task = -1;
                    return MainUtil.sendMessage(player, "Cancelled task.");
                case "remove-flag":
                    if (args.length != 2) {
                        MainUtil.sendMessage(player, C.COMMAND_SYNTAX, "/plot debugexec remove-flag <flag>");
                        return false;
                    }
                    final String flag = args[1];
                    for (final Plot plot : PS.get().getBasePlots()) {
                        if (FlagManager.getPlotFlagRaw(plot, flag) != null) {
                            FlagManager.removePlotFlag(plot, flag);
                        }
                    }
                    return MainUtil.sendMessage(player, "Cleared flag: " + flag);
                case "start-rgar": {
                    if (args.length != 2) {
                        MainUtil.sendMessage(player, "&cInvalid syntax: /plot debugexec start-rgar <world>");
                        return false;
                    }
                    PlotArea area = PS.get().getPlotAreaByString(args[1]);
                    if (area == null) {
                        MainUtil.sendMessage(player, C.NOT_VALID_PLOT_WORLD, args[1]);
                        return false;
                    }
                    boolean result;
                    if (HybridUtils.regions != null) {
                        result = HybridUtils.manager.scheduleRoadUpdate(area, HybridUtils.regions, 0);
                    } else {
                        result = HybridUtils.manager.scheduleRoadUpdate(area, 0);
                    }
                    if (!result) {
                        MainUtil.sendMessage(player, "&cCannot schedule mass schematic update! (Is one already in progress?)");
                        return false;
                    }
                    return true;
                }
                case "stop-rgar":
                    if (!HybridUtils.UPDATE) {
                        MainUtil.sendMessage(player, "&cTASK NOT RUNNING!");
                        return false;
                    }
                    HybridUtils.UPDATE = false;
                    MainUtil.sendMessage(player, "&cCancelling task... (please wait)");
                    return true;
                case "start-expire":
                    if (ExpireManager.task == -1) {
                        ExpireManager.runTask();
                    } else {
                        return MainUtil.sendMessage(player, "Plot expiry task already started");
                    }
                    return MainUtil.sendMessage(player, "Started plot expiry task");
                case "update-expired":
                    if (args.length > 1) {
                        PlotArea area = PS.get().getPlotAreaByString(args[1]);
                        if (area == null || !WorldUtil.IMP.isWorld(area.worldname)) {
                            C.NOT_VALID_PLOT_WORLD.send(player, args[1]);
                            return false;
                        }
                        MainUtil.sendMessage(player, "Updating expired plot list");
                        ExpireManager.updateExpired(area);
                        return true;
                    }
                    return MainUtil.sendMessage(player, "Use /plot debugexec update-expired <world>");
                case "show-expired":
                    if (args.length > 1) {
                        final String world = args[1];
                        if (!WorldUtil.IMP.isWorld(world)) {
                            return MainUtil.sendMessage(player, "Invalid world: " + args[1]);
                        }
                        if (!ExpireManager.expiredPlots.containsKey(args[1])) {
                            return MainUtil.sendMessage(player, "No task for world: " + args[1]);
                        }
                        MainUtil.sendMessage(player, "Expired plots (" + ExpireManager.expiredPlots.get(args[1]).size() + "):");
                        for (final Plot plot : ExpireManager.expiredPlots.get(args[1])) {
                            MainUtil.sendMessage(player,
                            " - " + plot.getArea() + ";" + plot.getId().x + ";" + plot.getId().y + ";" + UUIDHandler.getName(plot.owner) + " : " + ExpireManager.dates.get(plot.owner));
                        }
                        return true;
                    }
                    return MainUtil.sendMessage(player, "Use /plot debugexec show-expired <world>");
                case "seen":
                    if (args.length != 2) {
                        return MainUtil.sendMessage(player, "Use /plot debugexec seen <player>");
                    }
                    final UUID uuid = UUIDHandler.getUUID(args[1], null);
                    if (uuid == null) {
                        return MainUtil.sendMessage(player, "player not found: " + args[1]);
                    }
                    final OfflinePlotPlayer op = UUIDHandler.getUUIDWrapper().getOfflinePlayer(uuid);
                    if (op == null || op.getLastPlayed() == 0) {
                        return MainUtil.sendMessage(player, "player hasn't connected before: " + args[1]);
                    }
                    final Timestamp stamp = new Timestamp(op.getLastPlayed());
                    final Date date = new Date(stamp.getTime());
                    MainUtil.sendMessage(player, "PLAYER: " + args[1]);
                    MainUtil.sendMessage(player, "UUID: " + uuid);
                    MainUtil.sendMessage(player, "Object: " + date.toGMTString());
                    MainUtil.sendMessage(player, "GMT: " + date.toGMTString());
                    MainUtil.sendMessage(player, "Local: " + date.toLocaleString());
                    return true;
                case "trim-check":
                    if (args.length != 2) {
                        MainUtil.sendMessage(player, "Use /plot debugexec trim-check <world>");
                        MainUtil.sendMessage(player, "&7 - Generates a list of regions to trim");
                        return MainUtil.sendMessage(player, "&7 - Run after plot expiry has run");
                    }
                    final String world = args[1];
                    if (!WorldUtil.IMP.isWorld(world) || !PS.get().hasPlotArea(args[1])) {
                        return MainUtil.sendMessage(player, "Invalid world: " + args[1]);
                    }
                    final ArrayList<ChunkLoc> empty = new ArrayList<>();
                    final boolean result = Trim.getTrimRegions(empty, world, new Runnable() {
                        @Override
                        public void run() {
                            Trim.sendMessage("Processing is complete! Here's how many chunks would be deleted:");
                            Trim.sendMessage(" - MCA #: " + empty.size());
                            Trim.sendMessage(" - CHUNKS: " + empty.size() * 1024 + " (max)");
                            Trim.sendMessage("Exporting log for manual approval...");
                            final File file = new File(PS.get().IMP.getDirectory() + File.separator + "trim.txt");
                            try {
                                PrintWriter writer = new PrintWriter(file);
                                for (final ChunkLoc loc : empty) {
                                    writer.println(world + "/region/r." + loc.x + "." + loc.z + ".mca");
                                }
                                writer.close();
                                Trim.sendMessage("File saved to 'plugins/PlotSquared/trim.txt'");
                            } catch (final FileNotFoundException e) {
                                e.printStackTrace();
                                Trim.sendMessage("File failed to save! :(");
                            }
                            Trim.sendMessage("How to get the chunk coords from a region file:");
                            Trim.sendMessage(" - Locate the x,z values for the region file (the two numbers which are separated by a dot)");
                            Trim.sendMessage(" - Multiply each number by 32; this gives you the starting position");
                            Trim.sendMessage(" - Add 31 to each number to get the end position");
                        }
                    });
                    if (!result) {
                        MainUtil.sendMessage(player, "Trim task already started!");
                    }
                    return result;
                case "h":
                case "he":
                case "?":
                case "help":
                    MainUtil.sendMessage(player, "Possible sub commands: /plot debugexec <" + StringMan.join(allowed_params, "|") + ">");
                    return false;
                case "addcmd":
                    try {
                        final String cmd = StringMan.join(Files
                                        .readLines(new File(new File(PS.get().IMP.getDirectory() + File.separator + "scripts"), args[1]),
                                                StandardCharsets.UTF_8),
                                System.getProperty("line.separator"));
                        final Command<PlotPlayer> subcommand = new Command<PlotPlayer>(args[1].split("\\.")[0]) {
                            @Override
                            public boolean onCommand(final PlotPlayer plr, final String[] args) {
                                try {
                                    scope.put("PlotPlayer", plr);
                                    scope.put("args", args);
                                    engine.eval(cmd, scope);
                                    return true;
                                } catch (final ScriptException e) {
                                    e.printStackTrace();
                                    MainUtil.sendMessage(player, C.COMMAND_WENT_WRONG);
                                    return false;
                                }
                            }
                        };
                        MainCommand.getInstance().addCommand(subcommand);
                        return true;
                    } catch (IOException e) {
                        e.printStackTrace();
                        MainUtil.sendMessage(player, C.COMMAND_SYNTAX, "/plot debugexec addcmd <file>");
                        return false;
                    }
                case "runasync":
                    async = true;
                case "run":
                    try {
                        script = StringMan.join(Files.readLines(new File(new File(PS.get().IMP.getDirectory() + File.separator + "scripts"), args[1]), StandardCharsets.UTF_8),
                        System.getProperty("line.separator"));
                        if (args.length > 2) {
                            final HashMap<String, String> replacements = new HashMap<>();
                            for (int i = 2; i < args.length; i++) {
                                replacements.put("%s" + (i - 2), args[i]);
                            }
                            script = StringMan.replaceFromMap(script, replacements);
                        }
                    } catch (final IOException e) {
                        e.printStackTrace();
                        return false;
                    }
                    break;
                case "allcmd":
                    if (args.length < 3) {
                        C.COMMAND_SYNTAX.send(player, "/plot debugexec allcmd <condition> <command>");
                        return false;
                    }
                    long start = System.currentTimeMillis();
                    Command<PlotPlayer> cmd = MainCommand.getInstance().getCommand(args[3]);
                    String[] params = Arrays.copyOfRange(args, 4, args.length);
                    if ("true".equals(args[1])) {
                        Location loc = player.getMeta("location");
                        Plot plot = player.getMeta("lastplot");
                        for (Plot current : PS.get().getBasePlots()) {
                            player.setMeta("location", current.getBottomAbs());
                            player.setMeta("lastplot", current);
                            cmd.onCommand(player, params);
                        }
                        if (loc == null) {
                            player.deleteMeta("location");
                        } else {
                            player.setMeta("location", loc);
                        }
                        if (plot == null) {
                            player.deleteMeta("lastplot");
                        } else {
                            player.setMeta("lastplot", plot);
                        }
                        player.sendMessage("&c> " + (System.currentTimeMillis() - start));
                        return true;
                    }
                    init();
                    scope.put("_2", params);
                    scope.put("_3", cmd);
                    script = "_1=PS.getBasePlots().iterator();while(_1.hasNext()){plot=_1.next();if(" + args[1] + "){PlotPlayer.setMeta(\"location\",plot.getBottomAbs());PlotPlayer.setMeta(\"lastplot\",plot);_3.onCommand(PlotPlayer,_2)}}";

                    break;
                case "all":
                    if (args.length < 3) {
                        C.COMMAND_SYNTAX.send(player, "/plot debugexec all <condition> <code>");
                        return false;
                    }
                    script = "_1=PS.getBasePlots().iterator();while(_1.hasNext()){plot=_1.next();if(" + args[1] + "){" + StringMan
                            .join(Arrays.copyOfRange(args, 2, args.length), " ")
                            + "}}";

                    break;
                default:
                    script = StringMan.join(args, " ");
            }
            if (!ConsolePlayer.isConsole(player)) {
                MainUtil.sendMessage(player, C.NOT_CONSOLE);
                return false;
            }
            init();
            scope.put("PlotPlayer", player);
            PS.debug("> " + script);
            try {
                if (async) {
                    final String toExec = script;
                    TaskManager.runTaskAsync(new Runnable() {
                        @Override
                        public void run() {
                            final long start = System.currentTimeMillis();
                            Object result = null;
                            try {
                                result = engine.eval(toExec, scope);
                            } catch (final ScriptException e) {
                                e.printStackTrace();
                            }
                            ConsolePlayer.getConsole().sendMessage("> " + (System.currentTimeMillis() - start) + "ms -> " + result);
                        }
                    });
                } else {
                    final long start = System.currentTimeMillis();
                    Object result = engine.eval(script, scope);
                    ConsolePlayer.getConsole().sendMessage("> " + (System.currentTimeMillis() - start) + "ms -> " + result);
                }
                return true;
            } catch (final ScriptException e) {
                e.printStackTrace();
                return false;
            }
        }
        return false;
    }
}
