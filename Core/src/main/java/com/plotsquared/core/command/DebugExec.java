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
import com.google.inject.internal.cglib.transform.$ClassTransformer;
import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.configuration.Captions;
import com.plotsquared.core.configuration.Settings;
import com.plotsquared.core.configuration.caption.StaticCaption;
import com.plotsquared.core.configuration.caption.TranslatableCaption;
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
import com.plotsquared.core.plot.world.PlotAreaManager;
import com.plotsquared.core.queue.GlobalBlockQueue;
import com.plotsquared.core.util.ChunkManager;
import com.plotsquared.core.util.EconHandler;
import com.plotsquared.core.util.EventDispatcher;
import com.plotsquared.core.util.FileUtils;
import com.plotsquared.core.util.MathMan;
import com.plotsquared.core.util.SchematicHandler;
import com.plotsquared.core.util.SetupUtils;
import com.plotsquared.core.util.StringMan;
import com.plotsquared.core.util.WEManager;
import com.plotsquared.core.util.WorldUtil;
import com.plotsquared.core.util.query.PlotQuery;
import com.plotsquared.core.util.task.RunnableVal;
import com.plotsquared.core.util.task.RunnableVal2;
import com.plotsquared.core.util.task.RunnableVal3;
import com.plotsquared.core.util.task.TaskManager;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.world.block.BlockState;
import net.kyori.adventure.text.minimessage.Template;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
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
        this.scope.put("TaskManager", TaskManager.getPlatformImplementation());
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
                        player.sendMessage(TranslatableCaption.of("errors.not_in_plot"));
                        return false;
                    }
                    PlotAnalysis analysis = plot.getComplexity(null);
                    if (analysis != null) {
                        player.sendMessage(
                                TranslatableCaption.of("debugexec.changes_column"),
                                Template.of("value", String.valueOf(analysis.changes / 1.0))
                        );
                        return true;
                    }
                    player.sendMessage(TranslatableCaption.of("debugexec.starting_task"));
                    this.hybridUtils.analyzePlot(plot, new RunnableVal<PlotAnalysis>() {
                        @Override public void run(PlotAnalysis value) {
                            player.sendMessage(StaticCaption.of("&6Done: &7Use &6/plot debugexec analyze &7for more information."));
                        }
                    });
                    return true;
                }
                case "calibrate-analysis":
                    if (args.length != 2) {
                        player.sendMessage(
                                TranslatableCaption.of("commandconfig.command_syntax"),
                                Template.of("value", "/plot debugexec analyze <threshold>")
                        );
                        player.sendMessage(TranslatableCaption.of("debugexec.threshold_default"));
                        return false;
                    }
                    double threshold;
                    try {
                        threshold = Integer.parseInt(args[1]) / 100d;
                    } catch (NumberFormatException ignored) {
                        player.sendMessage(
                                TranslatableCaption.of("debugexec.invalid_threshold"),
                                Template.of("value", args[1])
                        );
                        player.sendMessage(TranslatableCaption.of("debugexec.threshold_default_double"));
                        return false;
                    }
                    PlotAnalysis.calcOptimalModifiers(() -> player.sendMessage(TranslatableCaption.of("debugexec.calibration_done")), threshold);
                    return true;
                case "stop-expire":
                    if (ExpireManager.IMP == null || !ExpireManager.IMP.cancelTask()) {
                        player.sendMessage(TranslatableCaption.of("debugexec.task_halted"));
                    }
                    player.sendMessage(TranslatableCaption.of("debugexec.task_cancelled"));
                case "remove-flag":
                    if (args.length != 2) {
                        player.sendMessage(
                                TranslatableCaption.of("commandconfig.command_syntax"),
                                Template.of("value", "/plot debugexec remove-flag <flag>")
                        );
                        return false;
                    }
                    String flag = args[1];
                    final PlotFlag<?, ?> flagInstance =
                        GlobalFlagContainer.getInstance().getFlagFromString(flag);
                    if (flagInstance != null) {
                        for (Plot plot : PlotQuery.newQuery().whereBasePlot()) {
                            PlotFlagRemoveEvent event = this.eventDispatcher
                                .callFlagRemove(flagInstance, plot);
                            if (event.getEventResult() != Result.DENY) {
                                plot.removeFlag(event.getFlag());
                            }
                        }
                    }
                    player.sendMessage(
                            TranslatableCaption.of("debugexec.cleared_flag"),
                            Template.of("value", flag)
                    );
                case "start-rgar": {
                    if (args.length != 2) {
                        player.sendMessage(
                                TranslatableCaption.of("commandconfig.command_syntax"),
                                Template.of("value", "Invalid syntax: /plot debugexec start-rgar <world>")
                        );
                        return false;
                    }
                    PlotArea area = this.plotAreaManager.getPlotAreaByString(args[1]);
                    if (area == null) {
                        player.sendMessage(
                                TranslatableCaption.of("errors.not_valid_plot_world"),
                                Template.of("value", args[1])
                        );
                        return false;
                    }
                    boolean result;
                    if (HybridUtils.regions != null) {
                        result = this.hybridUtils.scheduleRoadUpdate(area, HybridUtils.regions, 0, new HashSet<>());
                    } else {
                        result = this.hybridUtils.scheduleRoadUpdate(area, 0);
                    }
                    if (!result) {
                        player.sendMessage(TranslatableCaption.of("debugexec.mass_schematic_update_in_progress"));
                        return false;
                    }
                    return true;
                }
                case "stop-rgar":
                    if (!HybridUtils.UPDATE) {
                        player.sendMessage(TranslatableCaption.of("debugexec.task_not_running"));
                        return false;
                    }
                    HybridUtils.UPDATE = false;
                    player.sendMessage(TranslatableCaption.of("debugexec.cancelling_task"));
                    return true;
                case "start-expire":
                    if (ExpireManager.IMP == null) {
                        ExpireManager.IMP = new ExpireManager(this.eventDispatcher);
                    }
                    if (ExpireManager.IMP.runAutomatedTask()) {
                        player.sendMessage(TranslatableCaption.of("debugexec.expiry_started"));
                    } else {
                        player.sendMessage(TranslatableCaption.of("debugexec.expiry_already_started"));
                    }
                case "h":
                case "he":
                case "?":
                case "help":
                    player.sendMessage(StaticCaption.of("Possible sub commands: /plot debugexec <" + StringMan.join(allowed_params, "|") + ">"));
                    return false;
                case "addcmd":
                    try {
                        final String cmd = StringMan.join(Files.readLines(FileUtils.getFile(new File(
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
                                    player.sendMessage(TranslatableCaption.of("error.command_went_wrong"));
                                }
                                return CompletableFuture.completedFuture(true);
                            }
                        };
                        return true;
                    } catch (IOException e) {
                        e.printStackTrace();
                        player.sendMessage(
                                TranslatableCaption.of("commandconfig.command_syntax"),
                                Template.of("value", "/plot debugexec addcmd <file>")
                        );
                        return false;
                    }
                case "runasync":
                    async = true;
                case "run":
                    try {
                        script = StringMan.join(Files.readLines(FileUtils.getFile(new File(
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
                            player.sendMessage(
                                    TranslatableCaption.of("commandconfig.command_syntax"),
                                    Template.of("value", "/plot debugexec list-scripts [#]")
                            );
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
                case "all":
                    if (args.length < 3) {
                        player.sendMessage(
                                TranslatableCaption.of("commandconfig.command_syntax"),
                                Template.of("value", "/plot debugexec all <condition> <code>")
                        );
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
                player.sendMessage(TranslatableCaption.of("console.not_console"));
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
