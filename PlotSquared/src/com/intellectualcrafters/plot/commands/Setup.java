package com.intellectualcrafters.plot.commands;

import com.intellectualcrafters.plot.C;
import com.intellectualcrafters.plot.PlotMain;
import com.intellectualcrafters.plot.PlotWorld;
import com.intellectualcrafters.plot.WorldGenerator;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Citymonstret on 2014-09-26.
 */
public class Setup extends SubCommand implements Listener {

    public static Map<String, SetupObject> setupMap = new HashMap<>();

    private static class SetupStep {
        private String constant;
        private Object default_value;
        private String description;
        private Object value = 0;
        private Class type;
        public SetupStep(String constant, Object default_value, String description, Class type) {
            this.constant = constant;
            this.default_value = default_value;
            this.description = description;
            this.type = type;
        }

        public Class getType() {
            if (this.type == Integer.class) {
                return Integer.class;
            }
            if (this.type == Boolean.class) {
                return Boolean.class;
            }
            if (this.type == Double.class) {
                return Double.class;
            }
            if (this.type == Float.class) {
                return Float.class;
            }
            if (this.type == String.class) {
                return String.class;
            }
            return Object.class;
        }

        public boolean setValue(Object o) {
            return true;
        }

        public boolean validValue(String string) {
            try {
                if (this.type == Integer.class) {
                    Integer.parseInt(string);
                    return true;
                }
                if (this.type == Boolean.class) {
                    Boolean.parseBoolean(string);
                    return true;
                }
                if (this.type == Double.class) {
                    Double.parseDouble(string);
                    return true;
                }
                if (this.type == Float.class) {
                    Float.parseFloat(string);
                    return true;
                }
                if (this.type == String.class) {
                    return true;
                }
            }
            catch (Exception e) {}
            return false;
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
        /*
        ROAD_HEIGHT - Integer
        PLOT_HEIGHT - Integer
        WALL_HEIGHT - Integer
        PLOT_WIDTH - Integer
        ROAD_WIDTH - Integer
        PLOT_BIOME - BIOME
        MAIN_BLOCK - Block[] (as you can have several blocks, with IDS)
        TOP_BLOCK - Block[] (as you can have several blocks, with IDS)
        WALL_BLOCK - Block
        WALL_FILLING - Block
        ROAD_STRIPES - Block
        ROAD_STRIPES_ENABLED - Boolean
        ROAD_BLOCK - Block
        PLOT_CHAT - Boolean
        BLOCKS - wtf is this?
        SCHEMATIC_ON_CLAIM - Boolean
        SCHEMATIC_FILE - String
        DEFAULT_FLAGS - String[]
         */
        SetupStep[] step = new SetupStep[] { new SetupStep("road_height", 64, "Height of road", Integer.class) };

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
    
    /*
     *   /plot setup {world} <default> - setup a world using default values
     *      (display current default settings)
     *      (use ordinary chat to get/set)
     *      <value> <option> - modify a value
     *      /plot setup create - create the world
     *   
     *   /plot setup {world} <world> - setup a world using the values for an existing world
     *      (display current world settings)
     *      (use ordinary chat to get/set)
     *      <value> <option> - modify a value
     *      /plot setup create - create the world
     *      
     *   /plot setup {world} - setup the world manually
     *      (display current world settings)
     *      (use ordinary chat to set)
     *      <option> - set the current value
     *      back - to go back a step
     *      /plot setup create - create the world
     * 
     */

    @Override
    public boolean execute(Player plr, String... args) {
        if(setupMap.containsKey(plr.getName())) {
            SetupObject object = setupMap.get(plr.getName());
            if(object.getCurrent() == object.getMax()) {
                sendMessage(plr, C.SETUP_FINISHED, object.world);
                setupMap.remove(plr.getName());
                
                // Save stuff to config
                
                // Generate a world
//                String name = "{world}";
//                World world = WorldCreator.name(name).generator(new WorldGenerator(name)).createWorld();
                
                return true;
            }
            SetupStep step = object.step[object.current];
            if(args.length < 1) {
                sendMessage(plr, C.SETUP_STEP, object.current + 1 + "", step.getDescription(), step.getType().getName(), step.getDefaultValue() + "");
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
        return true;
    }

}
