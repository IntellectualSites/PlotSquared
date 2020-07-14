/*
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
package com.plotsquared.core.command;

import com.google.common.io.Files;
import com.google.inject.Inject;
import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.configuration.Captions;
import com.plotsquared.core.configuration.Settings;
import com.plotsquared.core.database.DBFunc;
import com.plotsquared.core.events.PlotFlagRemoveEvent;
import com.plotsquared.core.events.Result;
import com.plotsquared.core.generator.HybridUtils;
import com.plotsquared.core.location.Location;
import com.plotsquared.core.player.ConsolePlayer;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.PlotArea;
import com.plotsquared.core.plot.PlotId;
import com.plotsquared.core.plot.expiration.ExpireManager;
import com.plotsquared.core.plot.expiration.PlotAnalysis;
import com.plotsquared.core.plot.flag.GlobalFlagContainer;
import com.plotsquared.core.plot.flag.PlotFlag;
import com.plotsquared.core.plot.message.PlotMessage;
import com.plotsquared.core.plot.world.PlotAreaManager;
import com.plotsquared.core.queue.GlobalBlockQueue;
import com.plotsquared.core.util.ChunkManager;
import com.plotsquared.core.util.EconHandler;
import com.plotsquared.core.util.EventDispatcher;
import com.plotsquared.core.util.MainUtil;
import com.plotsquared.core.util.MathMan;
import com.plotsquared.core.util.SchematicHandler;
import com.plotsquared.core.util.SetupUtils;
import com.plotsquared.core.util.StringMan;
import com.plotsquared.core.util.WEManager;
import com.plotsquared.core.util.WorldUtil;
import com.plotsquared.core.util.task.RunnableVal;
import com.plotsquared.core.util.task.RunnableVal2;
import com.plotsquared.core.util.task.RunnableVal3;
import com.plotsquared.core.util.task.TaskManager;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.world.block.BlockState;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleScriptContext;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@CommandDeclaration(command = "debugexec",
    permission = "plots.admin",
    description = "Mutli-purpose debug command",
    aliases = {"exec", "$"},
    category = CommandCategory.DEBUG)
public class DebugExec extends SubCommand {

    private static final Logger logger = LoggerFactory.getLogger("P2/" + DebugExec.class.getSimpleName());


    private final PlotAreaManager plotAreaManager;
    private final EventDispatcher eventDispatcher;
    private final WorldEdit worldEdit;
    private final GlobalBlockQueue blockQueue;
    private final SchematicHandler schematicHandler;
    private final EconHandler econHandler;
    private final ChunkManager chunkManager;
    private final WorldUtil worldUtil;
    private final SetupUtils setupUtils;
    private final HybridUtils hybridUtils;

    private ScriptEngine engine;
    private Bindings scope;

    @Inject public DebugExec(@Nonnull final PlotAreaManager plotAreaManager,
                     @Nonnull final EventDispatcher eventDispatcher,
                     @Nullable final WorldEdit worldEdit,
                     @Nonnull final GlobalBlockQueue blockQueue,
                     @Nonnull final SchematicHandler schematicHandler,
                     @Nullable final EconHandler econHandler,
                     @Nonnull final ChunkManager chunkManager,
                     @Nonnull final WorldUtil worldUtil,
                     @Nonnull final SetupUtils setupUtils,
                     @Nonnull final HybridUtils hybridUtils) {
        this.plotAreaManager = plotAreaManager;
        this.eventDispatcher = eventDispatcher;
        this.worldEdit = worldEdit;
        this.blockQueue = blockQueue;
        this.schematicHandler = schematicHandler;
        this.econHandler = econHandler;
        this.chunkManager = chunkManager;
        this.worldUtil = worldUtil;
        this.setupUtils = setupUtils;
        this.hybridUtils = hybridUtils;

        init();
    }

    public ScriptEngine getEngine() {
        if (this.engine == null) {
            init();
        }
        return this.engine;
    }

    public Bindings getScope() {
        return this.scope;
    }

    public void init() {
        if (this.engine != null) {
            return;
        }
        //create script engine manager
        ScriptEngineManager scriptEngineManager = new ScriptEngineManager();
        //create nashorn engine
        this.engine = scriptEngineManager.getEngineByName("nashorn");
        try {
            engine.eval("print('PlotSquared scripting engine started');");
        } catch (ScriptException e) {
            e.printStackTrace();
        }
        if (this.engine == null) {
            this.engine = new ScriptEngineManager(null).getEngineByName("JavaScript");
        }
        ScriptContext context = new SimpleScriptContext();
        this.scope = context.getBindings(ScriptContext.ENGINE_SCOPE);

        // stuff
        this.scope.put("MainUtil", new MainUtil());
        this.scope.put("Settings", new Settings());
        this.scope.put("StringMan", new StringMan());
        this.scope.put("MathMan", new MathMan());

        // Classes
        this.scope.put("Location", Location.class);
        this.scope.put("BlockState", BlockState.class);
        this.scope.put("Plot", Plot.class);
        this.scope.put("PlotId", PlotId.class);
        this.scope.put("Runnable", Runnable.class);
        this.scope.put("RunnableVal", RunnableVal.class);

        // Instances
        this.scope.put("PS", PlotSquared.get());
        this.scope.put("GlobalBlockQueue", this.blockQueue);
        this.scope.put("ExpireManager", ExpireManager.IMP);
        if (this.worldEdit != null) {
            this.scope.put("WEManager", new WEManager());
        }
        this.scope.put("TaskManager", TaskManager.getImplementation());
        this.scope.put("ConsolePlayer", ConsolePlayer.getConsole());
        this.scope.put("SchematicHandler", this.schematicHandler);
        this.scope.put("ChunkManager", this.chunkManager);
        this.scope.put("BlockManager", this.worldUtil);
        this.scope.put("SetupUtils", this.setupUtils);
        this.scope.put("EventUtil", this.eventDispatcher);
        this.scope.put("EconHandler", this.econHandler);
        this.scope.put("DBFunc", DBFunc.dbManager);
        this.scope.put("HybridUtils", this.hybridUtils);
        this.scope.put("IMP", PlotSquared.platform());
        this.scope.put("MainCommand", MainCommand.getInstance());

        // enums
        for (Enum<?> value : Captions.values()) {
            this.scope.put("C_" + value.name(), value);
        }
    }

    @Override public boolean onCommand(final PlotPlayer<?> player, String[] args) {
        List<String> allowed_params = Arrays
            .asList("analyze", "calibrate-analysis", "remove-flag", "stop-expire", "start-expire",
                "seen", "list-scripts", "start-rgar", "stop-rgar", "help", "addcmd", "runasync",
                "run", "allcmd", "all");
        if (args.length > 0) {
            String arg = args[0].toLowerCase();
            String script;
            boolean async = false;
            switch (arg) {
                case "analyze": {
                    Plot plot = player.getCurrentPlot();
                    if (plot == null) {
                        MainUtil.sendMessage(player, Captions.NOT_IN_PLOT);
                        return false;
                    }
                    PlotAnalysis analysis = plot.getComplexity(null);
                    if (analysis != null) {
                        MainUtil.sendMessage(player, "Changes/column: " + analysis.changes / 1.0);
                        return true;
                    }
                    MainUtil.sendMessage(player, "$1Starting task...");
                    this.hybridUtils.analyzePlot(plot, new RunnableVal<PlotAnalysis>() {
                        @Override public void run(PlotAnalysis value) {
                            MainUtil.sendMessage(player,
                                "$1Done: $2Use $3/plot debugexec analyze$2 for more information");
                        }
                    });
                    return true;
                }
                case "calibrate-analysis":
                    if (args.length != 2) {
                        MainUtil.sendMessage(player, Captions.COMMAND_SYNTAX,
                            "/plot debugexec analyze <threshold>");
                        MainUtil.sendMessage(player,
                            "$1<threshold> $2= $1The percentage of plots you want to clear (100 clears 100% of plots so no point calibrating "
                                + "it)");
                        return false;
                    }
                    double threshold;
                    try {
                        threshold = Integer.parseInt(args[1]) / 100d;
                    } catch (NumberFormatException ignored) {
                        MainUtil.sendMessage(player, "$2Invalid threshold: " + args[1]);
                        MainUtil.sendMessage(player,
                            "$1<threshold> $2= $1The percentage of plots you want to clear as a number between 0 - 100");
                        return false;
                    }
                    PlotAnalysis.calcOptimalModifiers(() -> MainUtil
                        .sendMessage(player, "$1Thank you for calibrating plot expiry"), threshold);
                    return true;
                case "stop-expire":
                    if (ExpireManager.IMP == null || !ExpireManager.IMP.cancelTask()) {
                        return MainUtil.sendMessage(player, "Task already halted");
                    }
                    return MainUtil.sendMessage(player, "Cancelled task.");
                case "remove-flag":
                    if (args.length != 2) {
                        MainUtil.sendMessage(player, Captions.COMMAND_SYNTAX,
                            "/plot debugexec remove-flag <flag>");
                        return false;
                    }
                    String flag = args[1];
                    final PlotFlag<?, ?> flagInstance =
                        GlobalFlagContainer.getInstance().getFlagFromString(flag);
                    if (flagInstance != null) {
                        for (Plot plot : PlotSquared.get().getBasePlots()) {
                            PlotFlagRemoveEvent event = this.eventDispatcher
                                .callFlagRemove(flagInstance, plot);
                            if (event.getEventResult() != Result.DENY) {
                                plot.removeFlag(event.getFlag());
                            }
                        }
                    }
                    return MainUtil.sendMessage(player, "Cleared flag: " + flag);
                case "start-rgar": {
                    if (args.length != 2) {
                        MainUtil.sendMessage(player,
                            "&cInvalid syntax: /plot debugexec start-rgar <world>");
                        return false;
                    }
                    PlotArea area = this.plotAreaManager.getPlotAreaByString(args[1]);
                    if (area == null) {
                        MainUtil.sendMessage(player, Captions.NOT_VALID_PLOT_WORLD, args[1]);
                        return false;
                    }
                    boolean result;
                    if (HybridUtils.regions != null) {
                        result = this.hybridUtils.scheduleRoadUpdate(area, HybridUtils.regions, 0, new HashSet<>());
                    } else {
                        result = this.hybridUtils.scheduleRoadUpdate(area, 0);
                    }
                    if (!result) {
                        MainUtil.sendMessage(player,
                            "&cCannot schedule mass schematic update! (Is one already in progress?)");
                        return false;
                    }
                    return true;
                }
                case "stop-rgar":
                    if (!HybridUtils.UPDATE) {
                        MainUtil.sendMessage(player, "&cTask not running!");
                        return false;
                    }
                    HybridUtils.UPDATE = false;
                    MainUtil.sendMessage(player, "&cCancelling task... (Please wait)");
                    return true;
                case "start-expire":
                    if (ExpireManager.IMP == null) {
                        ExpireManager.IMP = new ExpireManager(this.eventDispatcher);
                    }
                    if (ExpireManager.IMP.runAutomatedTask()) {
                        return MainUtil.sendMessage(player, "Started plot expiry task");
                    } else {
                        return MainUtil.sendMessage(player, "Plot expiry task already started");
                    }
                case "h":
                case "he":
                case "?":
                case "help":
                    MainUtil.sendMessage(player,
                        "Possible sub commands: /plot debugexec <" + StringMan
                            .join(allowed_params, "|") + ">");
                    return false;
                case "addcmd":
                    try {
                        final String cmd = StringMan.join(Files.readLines(MainUtil.getFile(new File(
                                PlotSquared.platform().getDirectory() + File.separator
                                    + Settings.Paths.SCRIPTS), args[1]), StandardCharsets.UTF_8),
                            System.getProperty("line.separator"));
                        new Command(MainCommand.getInstance(), true, args[1].split("\\.")[0], null,
                            RequiredType.NONE, CommandCategory.DEBUG) {
                            @Override
                            public CompletableFuture<Boolean> execute(PlotPlayer<?> player,
                                String[] args, RunnableVal3<Command, Runnable, Runnable> confirm,
                                RunnableVal2<Command, CommandResult> whenDone) {
                                try {
                                    DebugExec.this.scope.put("PlotPlayer", player);
                                    DebugExec.this.scope.put("args", args);
                                    DebugExec.this.engine.eval(cmd, DebugExec.this.scope);
                                } catch (ScriptException e) {
                                    e.printStackTrace();
                                    MainUtil.sendMessage(player, Captions.COMMAND_WENT_WRONG);
                                }
                                return CompletableFuture.completedFuture(true);
                            }
                        };
                        return true;
                    } catch (IOException e) {
                        e.printStackTrace();
                        MainUtil.sendMessage(player, Captions.COMMAND_SYNTAX,
                            "/plot debugexec addcmd <file>");
                        return false;
                    }
                case "runasync":
                    async = true;
                case "run":
                    try {
                        script = StringMan.join(Files.readLines(MainUtil.getFile(new File(
                                PlotSquared.platform().getDirectory() + File.separator
                                    + Settings.Paths.SCRIPTS), args[1]), StandardCharsets.UTF_8),
                            System.getProperty("line.separator"));
                        if (args.length > 2) {
                            HashMap<String, String> replacements = new HashMap<>();
                            for (int i = 2; i < args.length; i++) {
                                replacements.put("%s" + (i - 2), args[i]);
                            }
                            script = StringMan.replaceFromMap(script, replacements);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        return false;
                    }
                    break;
                case "list-scripts":
                    String path = PlotSquared.platform().getDirectory() + File.separator
                        + Settings.Paths.SCRIPTS;
                    File folder = new File(path);
                    File[] filesArray = folder.listFiles();

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
                            Captions.COMMAND_SYNTAX
                                .send(player, "/plot debugexec list-scripts [#]");
                            return false;
                    }

                    List<File> allFiles = Arrays.asList(filesArray);
                    paginate(player, allFiles, 8, page,
                        new RunnableVal3<Integer, File, PlotMessage>() {

                            @Override public void run(Integer i, File file, PlotMessage message) {
                                String name = file.getName();
                                message.text("[").color("$3").text(String.valueOf(i)).color("$1")
                                    .text("]").color("$3").text(' ' + name).color("$1");
                            }
                        }, "/plot debugexec list-scripts", "List of scripts");
                    return true;
                case "allcmd":
                    if (args.length < 3) {
                        Captions.COMMAND_SYNTAX
                            .send(player, "/plot debugexec allcmd <condition> <command>");
                        return false;
                    }
                    long start = System.currentTimeMillis();
                    Command cmd = MainCommand.getInstance().getCommand(args[3]);
                    String[] params = Arrays.copyOfRange(args, 4, args.length);
                    if ("true".equals(args[1])) {
                        Location location = player.getMeta(PlotPlayer.META_LOCATION);
                        Plot plot = player.getMeta(PlotPlayer.META_LAST_PLOT);
                        for (Plot current : PlotSquared.get().getBasePlots()) {
                            player.setMeta(PlotPlayer.META_LOCATION, current.getBottomAbs());
                            player.setMeta(PlotPlayer.META_LAST_PLOT, current);
                            cmd.execute(player, params, null, null);
                        }
                        if (location == null) {
                            player.deleteMeta(PlotPlayer.META_LOCATION);
                        } else {
                            player.setMeta(PlotPlayer.META_LOCATION, location);
                        }
                        if (plot == null) {
                            player.deleteMeta(PlotPlayer.META_LAST_PLOT);
                        } else {
                            player.setMeta(PlotPlayer.META_LAST_PLOT, plot);
                        }
                        player.sendMessage("&c> " + (System.currentTimeMillis() - start));
                        return true;
                    }
                    init();
                    this.scope.put("_2", params);
                    this.scope.put("_3", cmd);
                    script =
                        "_1=PS.getBasePlots().iterator();while(_1.hasNext()){plot=_1.next();if("
                            + args[1]
                            + "){PlotPlayer.setMeta(\"location\",plot.getBottomAbs());PlotPlayer.setMeta(\"lastplot\",plot);_3.onCommand"
                            + "(PlotPlayer,_2)}}";

                    break;
                case "all":
                    if (args.length < 3) {
                        Captions.COMMAND_SYNTAX
                            .send(player, "/plot debugexec all <condition> <code>");
                        return false;
                    }
                    script =
                        "_1=PS.getBasePlots().iterator();while(_1.hasNext()){plot=_1.next();if("
                            + args[1] + "){" + StringMan
                            .join(Arrays.copyOfRange(args, 2, args.length), " ") + "}}";

                    break;
                default:
                    script = StringMan.join(args, " ");
            }
            if (!(player instanceof ConsolePlayer)) {
                MainUtil.sendMessage(player, Captions.NOT_CONSOLE);
                return false;
            }
            init();
            this.scope.put("PlotPlayer", player);
            try {
                if (async) {
                    final String toExec = script;
                    TaskManager.runTaskAsync(() -> {
                        long start = System.currentTimeMillis();
                        Object result = null;
                        try {
                            result = DebugExec.this.engine.eval(toExec, DebugExec.this.scope);
                        } catch (ScriptException e) {
                            e.printStackTrace();
                        }
                        logger.info("[P2] > {}ms -> {}", System.currentTimeMillis() - start, result);
                    });
                } else {
                    long start = System.currentTimeMillis();
                    Object result = this.engine.eval(script, this.scope);
                    logger.info("[P2] > {}ms -> {}", System.currentTimeMillis() - start, result);
                }
                return true;
            } catch (ScriptException e) {
                e.printStackTrace();
                return false;
            }
        }
        return false;
    }
}
