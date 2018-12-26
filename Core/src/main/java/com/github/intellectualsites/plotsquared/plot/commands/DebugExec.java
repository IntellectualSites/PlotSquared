package com.github.intellectualsites.plotsquared.plot.commands;

import com.github.intellectualsites.plotsquared.commands.Command;
import com.github.intellectualsites.plotsquared.commands.CommandDeclaration;
import com.github.intellectualsites.plotsquared.plot.PlotSquared;
import com.github.intellectualsites.plotsquared.plot.config.C;
import com.github.intellectualsites.plotsquared.plot.config.Settings;
import com.github.intellectualsites.plotsquared.plot.database.DBFunc;
import com.github.intellectualsites.plotsquared.plot.flag.Flag;
import com.github.intellectualsites.plotsquared.plot.flag.FlagManager;
import com.github.intellectualsites.plotsquared.plot.generator.HybridUtils;
import com.github.intellectualsites.plotsquared.plot.listener.WEManager;
import com.github.intellectualsites.plotsquared.plot.object.*;
import com.github.intellectualsites.plotsquared.plot.util.*;
import com.github.intellectualsites.plotsquared.plot.util.block.GlobalBlockQueue;
import com.github.intellectualsites.plotsquared.plot.util.expiry.ExpireManager;
import com.github.intellectualsites.plotsquared.plot.util.expiry.PlotAnalysis;
import com.google.common.io.Files;

import javax.script.*;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.util.*;

@CommandDeclaration(command = "debugexec", permission = "plots.admin",
    description = "Mutli-purpose debug command", aliases = {"exec", "$"},
    category = CommandCategory.DEBUG) public class DebugExec extends SubCommand {
    private ScriptEngine engine;
    private Bindings scope;

    public DebugExec() {
        try {
            if (PlotSquared.get() != null) {
                File file = new File(PlotSquared.get().IMP.getDirectory(),
                    Settings.Paths.SCRIPTS + File.separator + "start.js");
                if (file.exists()) {
                    init();
                    String script = StringMan.join(Files.readLines(new File(new File(
                            PlotSquared.get().IMP.getDirectory() + File.separator
                                + Settings.Paths.SCRIPTS), "start.js"), StandardCharsets.UTF_8),
                        System.getProperty("line.separator"));
                    this.scope.put("THIS", this);
                    this.scope.put("PlotPlayer", ConsolePlayer.getConsole());
                    this.engine.eval(script, this.scope);
                }
            }
        } catch (IOException | ScriptException ignored) {
            ignored.printStackTrace();
        }
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
        this.engine = new ScriptEngineManager(null).getEngineByName("nashorn");
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
        this.scope.put("FlagManager", new FlagManager());

        // Classes
        this.scope.put("Location", Location.class);
        this.scope.put("PlotBlock", PlotBlock.class);
        this.scope.put("Plot", Plot.class);
        this.scope.put("PlotId", PlotId.class);
        this.scope.put("Runnable", Runnable.class);
        this.scope.put("RunnableVal", RunnableVal.class);

        // Instances
        this.scope.put("PS", PlotSquared.get());
        this.scope.put("GlobalBlockQueue", GlobalBlockQueue.IMP);
        this.scope.put("ExpireManager", ExpireManager.IMP);
        if (PlotSquared.get().worldedit != null) {
            this.scope.put("WEManager", new WEManager());
        }
        this.scope.put("TaskManager", TaskManager.IMP);
        this.scope.put("TitleManager", AbstractTitle.TITLE_CLASS);
        this.scope.put("ConsolePlayer", ConsolePlayer.getConsole());
        this.scope.put("SchematicHandler", SchematicHandler.manager);
        this.scope.put("ChunkManager", ChunkManager.manager);
        this.scope.put("BlockManager", WorldUtil.IMP);
        this.scope.put("SetupUtils", SetupUtils.manager);
        this.scope.put("EventUtil", EventUtil.manager);
        this.scope.put("EconHandler", EconHandler.manager);
        this.scope.put("UUIDHandler", UUIDHandler.implementation);
        this.scope.put("DBFunc", DBFunc.dbManager);
        this.scope.put("HybridUtils", HybridUtils.manager);
        this.scope.put("IMP", PlotSquared.get().IMP);
        this.scope.put("MainCommand", MainCommand.getInstance());

        // enums
        for (Enum<?> value : C.values()) {
            this.scope.put("C_" + value.name(), value);
        }
    }

    @Override public boolean onCommand(final PlotPlayer player, String[] args) {
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
                        MainUtil.sendMessage(player, C.NOT_IN_PLOT);
                        return false;
                    }
                    PlotAnalysis analysis = plot.getComplexity(null);
                    if (analysis != null) {
                        MainUtil.sendMessage(player, "Changes/column: " + analysis.changes / 1.0);
                        return true;
                    }
                    MainUtil.sendMessage(player, "$1Starting task...");
                    HybridUtils.manager.analyzePlot(plot, new RunnableVal<PlotAnalysis>() {
                        @Override public void run(PlotAnalysis value) {
                            MainUtil.sendMessage(player,
                                "$1Done: $2Use $3/plot debugexec analyze$2 for more information");
                        }
                    });
                    return true;
                }
                case "calibrate-analysis":
                    if (args.length != 2) {
                        MainUtil.sendMessage(player, C.COMMAND_SYNTAX,
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
                    PlotAnalysis.calcOptimalModifiers(new Runnable() {
                        @Override public void run() {
                            MainUtil.sendMessage(player, "$1Thank you for calibrating plot expiry");
                        }
                    }, threshold);
                    return true;
                case "stop-expire":
                    if (ExpireManager.IMP == null || !ExpireManager.IMP.cancelTask()) {
                        return MainUtil.sendMessage(player, "Task already halted");
                    }
                    return MainUtil.sendMessage(player, "Cancelled task.");
                case "remove-flag":
                    if (args.length != 2) {
                        MainUtil.sendMessage(player, C.COMMAND_SYNTAX,
                            "/plot debugexec remove-flag <flag>");
                        return false;
                    }
                    String flag = args[1];
                    for (Plot plot : PlotSquared.get().getBasePlots()) {
                        Flag<?> flag1 = FlagManager.getFlag(flag);
                        if (plot.getFlag(flag1).isPresent()) {
                            plot.removeFlag(flag1);
                        }
                    }
                    return MainUtil.sendMessage(player, "Cleared flag: " + flag);
                case "start-rgar": {
                    if (args.length != 2) {
                        MainUtil.sendMessage(player,
                            "&cInvalid syntax: /plot debugexec start-rgar <world>");
                        return false;
                    }
                    PlotArea area = PlotSquared.get().getPlotAreaByString(args[1]);
                    if (area == null) {
                        MainUtil.sendMessage(player, C.NOT_VALID_PLOT_WORLD, args[1]);
                        return false;
                    }
                    boolean result;
                    if (HybridUtils.regions != null) {
                        result =
                            HybridUtils.manager.scheduleRoadUpdate(area, HybridUtils.regions, 0);
                    } else {
                        result = HybridUtils.manager.scheduleRoadUpdate(area, 0);
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
                        ExpireManager.IMP = new ExpireManager();
                    }
                    if (ExpireManager.IMP.runAutomatedTask()) {
                        return MainUtil.sendMessage(player, "Started plot expiry task");
                    } else {
                        return MainUtil.sendMessage(player, "Plot expiry task already started");
                    }
                case "seen":
                    if (args.length != 2) {
                        return MainUtil.sendMessage(player, "Use /plot debugexec seen <player>");
                    }
                    UUID uuid = UUIDHandler.getUUID(args[1], null);
                    if (uuid == null) {
                        return MainUtil.sendMessage(player, "Player not found: " + args[1]);
                    }
                    OfflinePlotPlayer op = UUIDHandler.getUUIDWrapper().getOfflinePlayer(uuid);
                    if (op == null || op.getLastPlayed() == 0) {
                        return MainUtil
                            .sendMessage(player, "Player hasn't connected before: " + args[1]);
                    }
                    Timestamp stamp = new Timestamp(op.getLastPlayed());
                    Date date = new Date(stamp.getTime());
                    MainUtil.sendMessage(player, "PLAYER: " + args[1]);
                    MainUtil.sendMessage(player, "UUID: " + uuid);
                    MainUtil.sendMessage(player, "Object: " + date.toGMTString());
                    MainUtil.sendMessage(player, "GMT: " + date.toGMTString());
                    MainUtil.sendMessage(player, "Local: " + date.toLocaleString());
                    return true;
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
                                PlotSquared.get().IMP.getDirectory() + File.separator
                                    + Settings.Paths.SCRIPTS), args[1]), StandardCharsets.UTF_8),
                            System.getProperty("line.separator"));
                        new Command(MainCommand.getInstance(), true, args[1].split("\\.")[0], null,
                            RequiredType.NONE, CommandCategory.DEBUG) {
                            @Override public void execute(PlotPlayer player, String[] args,
                                RunnableVal3<Command, Runnable, Runnable> confirm,
                                RunnableVal2<Command, CommandResult> whenDone) {
                                try {
                                    DebugExec.this.scope.put("PlotPlayer", player);
                                    DebugExec.this.scope.put("args", args);
                                    DebugExec.this.engine.eval(cmd, DebugExec.this.scope);
                                } catch (ScriptException e) {
                                    e.printStackTrace();
                                    MainUtil.sendMessage(player, C.COMMAND_WENT_WRONG);
                                }
                            }
                        };
                        return true;
                    } catch (IOException e) {
                        e.printStackTrace();
                        MainUtil
                            .sendMessage(player, C.COMMAND_SYNTAX, "/plot debugexec addcmd <file>");
                        return false;
                    }
                case "runasync":
                    async = true;
                case "run":
                    try {
                        script = StringMan.join(Files.readLines(MainUtil.getFile(new File(
                                PlotSquared.get().IMP.getDirectory() + File.separator
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
                    String path = PlotSquared.get().IMP.getDirectory() + File.separator
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
                            C.COMMAND_SYNTAX.send(player, "/plot debugexec list-scripts [#]");
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
                        C.COMMAND_SYNTAX
                            .send(player, "/plot debugexec allcmd <condition> <command>");
                        return false;
                    }
                    long start = System.currentTimeMillis();
                    Command cmd = MainCommand.getInstance().getCommand(args[3]);
                    String[] params = Arrays.copyOfRange(args, 4, args.length);
                    if ("true".equals(args[1])) {
                        Location loc = player.getMeta(PlotPlayer.META_LOCATION);
                        Plot plot = player.getMeta(PlotPlayer.META_LAST_PLOT);
                        for (Plot current : PlotSquared.get().getBasePlots()) {
                            player.setMeta(PlotPlayer.META_LOCATION, current.getBottomAbs());
                            player.setMeta(PlotPlayer.META_LAST_PLOT, current);
                            cmd.execute(player, params, null, null);
                        }
                        if (loc == null) {
                            player.deleteMeta(PlotPlayer.META_LOCATION);
                        } else {
                            player.setMeta(PlotPlayer.META_LOCATION, loc);
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
                        C.COMMAND_SYNTAX.send(player, "/plot debugexec all <condition> <code>");
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
                MainUtil.sendMessage(player, C.NOT_CONSOLE);
                return false;
            }
            init();
            this.scope.put("PlotPlayer", player);
            PlotSquared.debug("> " + script);
            try {
                if (async) {
                    final String toExec = script;
                    TaskManager.runTaskAsync(new Runnable() {
                        @Override public void run() {
                            long start = System.currentTimeMillis();
                            Object result = null;
                            try {
                                result = DebugExec.this.engine.eval(toExec, DebugExec.this.scope);
                            } catch (ScriptException e) {
                                e.printStackTrace();
                            }
                            PlotSquared.log(
                                "> " + (System.currentTimeMillis() - start) + "ms -> " + result);
                        }
                    });
                } else {
                    long start = System.currentTimeMillis();
                    Object result = this.engine.eval(script, this.scope);
                    PlotSquared
                        .log("> " + (System.currentTimeMillis() - start) + "ms -> " + result);
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
