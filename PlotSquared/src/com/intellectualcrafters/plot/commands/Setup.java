package com.intellectualcrafters.plot.commands;

import com.intellectualcrafters.plot.C;
import com.intellectualcrafters.plot.PlotMain;
import com.intellectualcrafters.plot.PlotWorld;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

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
        private String type;
        public SetupStep(String constant, Object default_value, String description, String type) {
            this.constant = constant;
            this.default_value = default_value;
            this.description = description;
            this.type = type;
        }

        public String getType() {
            return this.type;
        }

        public boolean setValue(Object o) {
            return true;
        }

        public boolean validValue(String string) {
            return true;
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

    private class SetupObject {
        String world;
        int current = 0;
        PlotWorld p;

        SetupStep[] step = new SetupStep[] {
            new SetupStep("road_height", 64, "Height of road", "integer") {
                @Override
                public boolean validValue(String string) {
                    try {
                        int t = Integer.parseInt(string);
                    } catch(Exception e) {
                        return false;
                    }
                    return true;
                }
            }
        };

        public SetupObject(String world) {
            this.world = world;

            this.p = new PlotWorld();
        }

        public SetupStep getNextStep() {
            return this.step[current++];
        }

        public int getCurrent() {
            return this.current;
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
        if(setupMap.containsKey(plr.getName())) {
            SetupObject object = setupMap.get(plr.getName());
            if(object.getCurrent() == object.getMax()) {
                sendMessage(plr, C.SETUP_FINISHED, object.world);
                setupMap.remove(plr.getName());
                return true;
            }
            SetupStep step = object.step[object.current];
            if(args.length < 1) {
                sendMessage(plr, C.SETUP_STEP, object.current + 1 + "", step.getDescription(), step.getType(), step.getDefaultValue() + "");
                return true;
            } else {
                boolean valid = step.validValue(args[0]);
                if(valid) {
                    sendMessage(plr, C.SETUP_VALID_ARG);
                    object.current++;
                } else {
                    sendMessage(plr, C.SETUP_INVALID_ARG);
                }
            }
        } else {
            if (args.length < 1) {
                sendMessage(plr, C.SETUP_MISSING_WORLD);
                return true;
            }
            String world = args[0];
            if (PlotMain.isPlotWorld(Bukkit.getWorld(world))) {
                sendMessage(plr, C.SETUP_WORLD_TAKEN, world);
                return true;
            }
            setupMap.put(plr.getName(), new SetupObject(world));
            sendMessage(plr, C.SETUP_INIT);
            return true;
        }
    }

}
