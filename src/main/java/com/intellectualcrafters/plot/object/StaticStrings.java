package com.intellectualcrafters.plot.object;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings({"unused"})
public class StaticStrings {

    public static final String
        PERMISSION_ADMIN = "plots.admin",
        PERMISSION_PROJECTILE_UNOWNED = "plots.projectile.unowned",
        PERMISSION_PROJECTILE_OTHER = "plots.projectile.other",
        PERMISSION_ADMIN_INTERACT_BLOCKED_CMDS = "plots.admin.interact.blockedcommands",
        PERMISSION_WORLDEDIT_BYPASS = "plots.worldedit.bypass",
        PERMISSION_ADMIN_EXIT_DENIED = "plots.admin.exit.denied",
        PERMISSION_ADMIN_ENTRY_DENIED = "plots.admin.entry.denied",
        PERMISSION_COMMANDS_CHAT = "plots.admin.command.chat",
        PERMISSION_ADMIN_DESTROY_UNOWNED = "plots.admin.destroy.unowned",
        PERMISSION_ADMIN_DESTROY_OTHER =  "plots.admin.destroy.other",
        PERMISSION_ADMIN_DESTROY_ROAD = "plots.admin.destroy.road",
        PERMISSION_ADMIN_BUILD_ROAD = "plots.admin.build.road",
        PERMISSION_ADMIN_BUILD_UNOWNED = "plots.admin.build.unowned",
        PERMISSION_ADMIN_BUILD_OTHER = "plots.admin.build.other",
        PERMISSION_ADMIN_INTERACT_ROAD = "plots.admin.interact.road",
        PERMISSION_ADMIN_INTERACT_UNOWNED = "plots.admin.interact.unowned",
        PERMISSION_ADMIN_INTERACT_OTHER = "plots.admin.interact.other",
        PERMISSION_ADMIN_BUILD_HEIGHTLIMIT = "plots.admin.build.heightlimit";

    public static final String
        FLAG_USE = "use",
        FLAG_PLACE = "place",
        FLAG_PVP = "pvp",
        FLAG_HANGING_PLACE = "hanging-place",
        FLAG_HANGING_BREAK = "hanging-break",
        FLAG_HOSTILE_INTERACT = "hostile-interact",
        FLAG_ANIMAL_INTERACT = "animal-interact",
        FLAG_VEHICLE_USE = "vehicle-use",
        FLAG_TAMED_INTERACT = "tamed-interact",
        FLAG_DISABLE_PHYSICS = "disable-physics";

    public static final String
        META_INVENTORY = "inventory";


    public static final String
        PREFIX_META = "META_",
        PREFIX_FLAG = "FLAG_",
        PREFIX_PERMISSION = "PERMISSION_";

    public static Map<String, String> getStrings(final String prefix) {
        final Field[] fields = StaticStrings.class.getDeclaredFields();
        Map<String, String> strings = new HashMap<>();
        for (final Field field : fields) {
            if (field.getGenericType() != String.class) {
                continue;
            }
            if (field.getName().startsWith(prefix)) {
                field.setAccessible(true);
                try {
                    String value = field.get(StaticStrings.class).toString();
                    strings.put(field.getName(), value);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        return strings;
    }
}
