package com.intellectualcrafters.plot.commands;

import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.config.Settings;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotId;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.object.RunnableVal;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.Permissions;
import com.intellectualcrafters.plot.util.SchematicHandler;
import com.intellectualcrafters.plot.util.SchematicHandler.Schematic;
import com.intellectualcrafters.plot.util.TaskManager;
import com.plotsquared.general.commands.CommandDeclaration;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

@CommandDeclaration(
        command = "load",
        aliases = {"restore"},
        category = CommandCategory.ACTIONS,
        requiredType = RequiredType.PLAYER,
        description = "Load your plot",
        permission = "plots.load",
        usage = "/plot restore"
)
public class Load extends SubCommand {

    @Override
    public boolean onCommand(final PlotPlayer plr, final String[] args) {
        
        if (!Settings.METRICS) {
            MainUtil.sendMessage(plr, "&cPlease enable metrics in order to use this command.\n&7 - Or host it yourself if you don't like the free service");
            return false;
        }
        final String world = plr.getLocation().getWorld();
        if (!PS.get().isPlotWorld(world)) {
            return !sendMessage(plr, C.NOT_IN_PLOT_WORLD);
        }
        final Plot plot = MainUtil.getPlot(plr.getLocation());
        if (plot == null) {
            return !sendMessage(plr, C.NOT_IN_PLOT);
        }
        if (!plot.hasOwner()) {
            MainUtil.sendMessage(plr, C.PLOT_UNOWNED);
            return false;
        }
        if (!plot.isOwner(plr.getUUID()) && !Permissions.hasPermission(plr, "plots.admin.command.load")) {
            MainUtil.sendMessage(plr, C.NO_PLOT_PERMS);
            return false;
        }
        if (MainUtil.runners.containsKey(plot)) {
            MainUtil.sendMessage(plr, C.WAIT_FOR_TIMER);
            return false;
        }
        
        if (args.length != 0) {
            if (args.length == 1) {
                // TODO load save here
                List<String> schematics = (List<String>) plr.getMeta("plot_schematics");
                if (schematics == null) {
                    // No schematics found:
                    MainUtil.sendMessage(plr, C.LOAD_NULL);
                    return false;
                }
                String schem;
                try {
                    schem = schematics.get(Integer.parseInt(args[0]) - 1);
                }
                catch (Exception e) {
                    // use /plot load <index>
                    MainUtil.sendMessage(plr, C.NOT_VALID_NUMBER, "(1, " + schematics.size() + ")");
                    return false;
                }
                final URL url;
                try {
                    url = new URL(Settings.WEB_URL + "saves/" + plr.getUUID() + "/" + schem);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                    MainUtil.sendMessage(plr, C.LOAD_FAILED);
                    return false;
                }
                
                MainUtil.runners.put(plot, 1);
                MainUtil.sendMessage(plr, C.GENERATING_COMPONENT);
                TaskManager.runTaskAsync(new Runnable() {
                    @Override
                    public void run() {
                        Schematic schematic = SchematicHandler.manager.getSchematic(url);
                        if (schematic == null) {
                            MainUtil.runners.remove(plot);
                            sendMessage(plr, C.SCHEMATIC_INVALID, "non-existent or not in gzip format");
                            return;
                        }
                        SchematicHandler.manager.paste(schematic, plot, 0, 0, new RunnableVal<Boolean>() {
                            @Override
                            public void run() {
                                MainUtil.runners.remove(plot);
                                if (this.value) {
                                    sendMessage(plr, C.SCHEMATIC_PASTE_SUCCESS);
                                }
                                else {
                                    sendMessage(plr, C.SCHEMATIC_PASTE_FAILED);
                                }
                            }
                        });
                    }
                });
                return true;
            }
            MainUtil.runners.remove(plot);
            MainUtil.sendMessage(plr, C.COMMAND_SYNTAX, "/plot load <index>");
            return false;
        }
        
        // list schematics
        
        List<String> schematics = (List<String>) plr.getMeta("plot_schematics");
        if (schematics == null) {
            MainUtil.runners.put(plot, 1);
            TaskManager.runTaskAsync(new Runnable() {
                @Override
                public void run() {
                    List<String> schematics = SchematicHandler.manager.getSaves(plr.getUUID());
                    MainUtil.runners.remove(plot);
                    if (schematics == null || schematics.size() == 0) {
                        MainUtil.sendMessage(plr, C.LOAD_FAILED);
                        return;
                    }
                    plr.setMeta("plot_schematics", schematics);
                    displaySaves(plr, 0);
                }
            });
        }
        else {
            displaySaves(plr, 0);
        }
        return true;
    }
    
    public void displaySaves(PlotPlayer player, int page) {
        List<String> schematics = (List<String>) player.getMeta("plot_schematics");
        for (int i = 0; i < Math.min(schematics.size(), 32); i++) {
            try {
                String schem = schematics.get(i);
                String[] split = schem.split("_");
                if (split.length != 6) {
                    continue;
                }
                String time = secToTime((System.currentTimeMillis() / 1000) - (Long.parseLong(split[0])));
                String world = split[1];
                PlotId id = PlotId.fromString(split[2] + ";" + split[3]);
                String size = split[4];
                String server = split[5].replaceAll(".schematic", "");
                String color;
                if (PS.get().IMP.getServerName().replaceAll("[^A-Za-z0-9]", "").equals(server)) {
                    color = "$4";
                }
                else {
                    color = "$1";
                }
                MainUtil.sendMessage(player, "$3[$2" + (i + 1) + "$3] " + color + time + "$3 | " + color + world + ";" + id + "$3 | " + color + size + "x" + size);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        MainUtil.sendMessage(player, C.LOAD_LIST);
    }
    
    public String secToTime(long time) {
        StringBuilder toreturn = new StringBuilder();
        int years = 0;
        int weeks = 0;
        int days = 0;
        int hours = 0;
        int minutes = 0;
        if (time>=33868800) {
            years = (int) (time/33868800);
            time-=years*33868800;
            toreturn.append(years+"y ");
        }
        if (time>=604800) {
            weeks = (int) (time/604800);
            time-=weeks*604800;
            toreturn.append(weeks+"w ");
        }
        if (time>=86400) {
            days = (int) (time/86400);
            time-=days*86400;
            toreturn.append(days+"d ");
        }
        if (time>=3600) {
            hours = (int) (time/3600);
            time-=hours*3600;
            toreturn.append(hours+"h ");
        }
        if (time>=60) {
            minutes = (int) (time/60);
            time-=minutes*60;
            toreturn.append(minutes+"m ");
        }
        if (toreturn.equals("")||time>0){
            toreturn.append((time)+"s ");
        }
        return toreturn.toString().trim();
    }
}
