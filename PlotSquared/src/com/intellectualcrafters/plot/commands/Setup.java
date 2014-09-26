package com.intellectualcrafters.plot.commands;

import com.intellectualcrafters.plot.C;
import com.intellectualcrafters.plot.PlotMain;
import com.intellectualcrafters.plot.PlotWorld;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Citymonstret on 2014-09-26.
 */
public class Setup extends SubCommand implements Listener {

    public static Map<String, SetupObject> setupMap = new HashMap<>();

    /*
    ROAD_HEIGHT
    PLOT_HEIGHT
    WALL_HEIGHT
    PLOT_WIDTH
    ROAD_WIDTH
    PLOT_BIOME
    MAIN_BLOCK
    TOP_BLOCK
    WALL_BLOCK
    WALL_FILLING
    ROAD_STRIPES
    ROAD_STRIPES_ENABLED
    ROAD_BLOCK
    PLOT_CHAT
    BLOCKS
    SCHEMATIC_ON_CLAIM
    SCHEMATIC_FILE
    DEFAULT_FLAGS
     */
    private static class SetupStep {
        private String constant;
        private Object default_value;
        private String description;
        private Object value = 0;

        public SetupStep(String constant, Object default_value, String description) {
            this.constant = constant;
            this.default_value = default_value;
            this.description = description;
        }

        public boolean setValue(Object o) {

        }

        public Object getValue() {
            return this.value;
        }

        public String getConstant() {
            return this.constant;
        }

        public Object getDefaultValue() {
            return this.default_value;
        }

        public String getDescription() {
            return this.description;
        }
    }

    private static class SetupObject {
        private String world;
        private int current = 0;

        private SetupStep[] step = new SetupStep[] {
            new SetupStep("road_height", 64, "Height of road")
        };

        public SetupObject(String world) {
            this.world = world;
            PlotWorld p = new PlotWorld();
        }

        public SetupStep getNextStep() {
            return this.step[current++];
        }


        public int getMax() {
            return this.step.length;
        }
    }

    public Setup() {
        super("setup", "plots.admin", "Setup a PlotWorld", "/plot setup {world}", "setup", CommandCategory.ACTIONS);
    }

    @Override
    public boolean execute(Player plr, String... args) {
        if(args.length < 1) {
            sendMessage(plr, C.SETUP_MISSING_WORLD);
            return true;
        }
        String world = args[0];
        if(PlotMain.isPlotWorld(Bukkit.getWorld(world))) {
            sendMessage(plr, C.SETUP_WORLD_TAKEN, world);
            return true;
        }
        setupMap.put(plr.getName(), new SetupObject(world));
        sendMessage(plr, C.SETUP_INIT);
        return true;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        if(!setupMap.containsKey(player.getName())) {
            return;
        }
        event.setCancelled(true);
        SetupObject object = setupMap.get(player.getName());

    }
}
