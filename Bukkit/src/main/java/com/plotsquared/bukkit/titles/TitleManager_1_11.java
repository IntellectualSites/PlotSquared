package com.plotsquared.bukkit.titles;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Minecraft 1.8 Title
 * For 1.11
 *
 * @author Maxim Van de Wynckel
 * @version 1.1.0
 */
public class TitleManager_1_11 {
    /* Title packet */
    private static Class<?> packetTitle;
    /* Title packet actions ENUM */
    private static Class<?> packetActions;
    /* Chat serializer */
    private static Class<?> nmsChatSerializer;
    private static Class<?> chatBaseComponent;
    /* NMS player and connection */
    private static Class<?> nmsPlayer;
    private static Class<?> nmsPlayerConnection;
    private static Field playerConnection;
    private static Method sendPacket;
    private static Class<?> obcPlayer;
    private static Method methodPlayerGetHandle;
    /* Title text and color */
    private String title = "";
    private ChatColor titleColor = ChatColor.WHITE;
    /* Subtitle text and color */
    private String subtitle = "";
    private ChatColor subtitleColor = ChatColor.WHITE;
    /* Title timings */
    private int fadeInTime = -1;
    private int stayTime = -1;
    private int fadeOutTime = -1;
    private boolean ticks = false;

    private static final Map<Class<?>, Class<?>> CORRESPONDING_TYPES = new HashMap<Class<?>, Class<?>>();

    public TitleManager_1_11() {
        loadClasses();
    }

    /**
     * Create a new 1.8 title
     *
     * @param title Title
     */
    public TitleManager_1_11(String title) {
        this.title = title;
        loadClasses();
    }

    /**
     * Create a new 1.8 title
     *
     * @param title    Title text
     * @param subtitle Subtitle text
     */
    public TitleManager_1_11(String title, String subtitle) {
        this.title = title;
        this.subtitle = subtitle;
        loadClasses();
    }

    /**
     * Copy 1.8 title
     *
     * @param title Title
     */
    public TitleManager_1_11(TitleManager_1_11 title) {
        // Copy title
        this.title = title.getTitle();
        this.subtitle = title.getSubtitle();
        this.titleColor = title.getTitleColor();
        this.subtitleColor = title.getSubtitleColor();
        this.fadeInTime = title.getFadeInTime();
        this.fadeOutTime = title.getFadeOutTime();
        this.stayTime = title.getStayTime();
        this.ticks = title.isTicks();
        loadClasses();
    }

    /**
     * Create a new 1.8 title
     *
     * @param title       Title text
     * @param subtitle    Subtitle text
     * @param fadeInTime  Fade in time
     * @param stayTime    Stay on screen time
     * @param fadeOutTime Fade out time
     */
    public TitleManager_1_11(String title, String subtitle, int fadeInTime, int stayTime, int fadeOutTime) {
        this.title = title;
        this.subtitle = subtitle;
        this.fadeInTime = fadeInTime;
        this.stayTime = stayTime;
        this.fadeOutTime = fadeOutTime;
        loadClasses();
    }

    /**
     * Load spigot and NMS classes
     */
    private void loadClasses() {
        if (packetTitle == null) {
            packetTitle = getNMSClass("PacketPlayOutTitle");
            packetActions = getNMSClass("PacketPlayOutTitle$EnumTitleAction");
            chatBaseComponent = getNMSClass("IChatBaseComponent");
            nmsChatSerializer = getNMSClass("ChatComponentText");
            nmsPlayer = getNMSClass("EntityPlayer");
            nmsPlayerConnection = getNMSClass("PlayerConnection");
            playerConnection = getField(nmsPlayer,
                    "playerConnection");
            sendPacket = getMethod(nmsPlayerConnection, "sendPacket");
            obcPlayer = getOBCClass("entity.CraftPlayer");
            methodPlayerGetHandle = getMethod("getHandle", obcPlayer);
        }
    }

    /**
     * Set title text
     *
     * @param title Title
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Get title text
     *
     * @return Title text
     */
    public String getTitle() {
        return this.title;
    }

    /**
     * Set subtitle text
     *
     * @param subtitle Subtitle text
     */
    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    /**
     * Get subtitle text
     *
     * @return Subtitle text
     */
    public String getSubtitle() {
        return this.subtitle;
    }

    /**
     * Set the title color
     *
     * @param color Chat color
     */
    public void setTitleColor(ChatColor color) {
        this.titleColor = color;
    }

    /**
     * Set the subtitle color
     *
     * @param color Chat color
     */
    public void setSubtitleColor(ChatColor color) {
        this.subtitleColor = color;
    }

    /**
     * Set title fade in time
     *
     * @param time Time
     */
    public void setFadeInTime(int time) {
        this.fadeInTime = time;
    }

    /**
     * Set title fade out time
     *
     * @param time Time
     */
    public void setFadeOutTime(int time) {
        this.fadeOutTime = time;
    }

    /**
     * Set title stay time
     *
     * @param time Time
     */
    public void setStayTime(int time) {
        this.stayTime = time;
    }

    /**
     * Set timings to ticks
     */
    public void setTimingsToTicks() {
        ticks = true;
    }

    /**
     * Set timings to seconds
     */
    public void setTimingsToSeconds() {
        ticks = false;
    }

    /**
     * Send the title to a player
     *
     * @param player Player
     */
    public void send(Player player) {
        if (packetTitle != null) {
            // First reset previous settings
            resetTitle(player);
            try {
                // Send timings first
                Object handle = getHandle(player);
                Object connection = playerConnection.get(handle);
                Object[] actions = packetActions.getEnumConstants();
                Object packet = packetTitle.getConstructor(packetActions,
                        chatBaseComponent, Integer.TYPE, Integer.TYPE,
                        Integer.TYPE).newInstance(actions[3], null,
                        fadeInTime * (ticks ? 1 : 20),
                        stayTime * (ticks ? 1 : 20),
                        fadeOutTime * (ticks ? 1 : 20));
                // Send if set
                if (fadeInTime != -1 && fadeOutTime != -1 && stayTime != -1)
                    sendPacket.invoke(connection, packet);

                Object serialized;
                if (!subtitle.equals("")) {
                    // Send subtitle if present
                    serialized = nmsChatSerializer.getConstructor(String.class)
                            .newInstance(subtitleColor +
                                    ChatColor.translateAlternateColorCodes('&',
                                            subtitle));
                    packet = packetTitle.getConstructor(packetActions,
                            chatBaseComponent).newInstance(actions[1],
                            serialized);
                    sendPacket.invoke(connection, packet);
                }

                // Send title
                serialized = nmsChatSerializer.getConstructor(
                        String.class).newInstance(titleColor +
                        ChatColor.translateAlternateColorCodes('&', title));
                packet = packetTitle.getConstructor(packetActions,
                        chatBaseComponent).newInstance(actions[0], serialized);
                sendPacket.invoke(connection, packet);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void updateTimes(Player player) {
        if (TitleManager_1_11.packetTitle != null) {
            try {
                Object handle = getHandle(player);
                Object connection = playerConnection.get(handle);
                Object[] actions = TitleManager_1_11.packetActions.getEnumConstants();
                Object packet = TitleManager_1_11.packetTitle.getConstructor(
                        new Class[]{TitleManager_1_11.packetActions, chatBaseComponent,
                                Integer.TYPE, Integer.TYPE, Integer.TYPE})
                        .newInstance(
                                actions[3],
                                null,
                                this.fadeInTime
                                        * (this.ticks ? 1 : 20),
                                this.stayTime
                                        * (this.ticks ? 1 : 20),
                                this.fadeOutTime
                                        * (this.ticks ? 1 : 20));
                if ((this.fadeInTime != -1) && (this.fadeOutTime != -1)
                        && (this.stayTime != -1)) {
                    sendPacket.invoke(connection, packet);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void updateTitle(Player player) {
        if (TitleManager_1_11.packetTitle != null) {
            try {
                Object handle = getHandle(player);
                Object connection = getField(handle.getClass(),
                        "playerConnection").get(handle);
                Object[] actions = TitleManager_1_11.packetActions.getEnumConstants();
                Method sendPacket = getMethod(connection.getClass(),
                        "sendPacket");
                Object serialized = nmsChatSerializer.getConstructor(
                        String.class)
                        .newInstance(titleColor +
                                ChatColor.translateAlternateColorCodes('&',
                                        this.title));
                Object packet = TitleManager_1_11.packetTitle
                        .getConstructor(
                                new Class[]{TitleManager_1_11.packetActions,
                                        chatBaseComponent}).newInstance(
                                actions[0], serialized);
                sendPacket.invoke(connection, packet);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void updateSubtitle(Player player) {
        if (TitleManager_1_11.packetTitle != null) {
            try {
                Object handle = getHandle(player);
                Object connection = playerConnection.get(handle);
                Object[] actions = TitleManager_1_11.packetActions.getEnumConstants();
                Object serialized = nmsChatSerializer.getConstructor(
                        String.class)
                        .newInstance(subtitleColor +
                                ChatColor.translateAlternateColorCodes('&',
                                        this.subtitle));
                Object packet = TitleManager_1_11.packetTitle
                        .getConstructor(
                                new Class[]{TitleManager_1_11.packetActions,
                                        chatBaseComponent}).newInstance(
                                actions[1], serialized);
                sendPacket.invoke(connection, packet);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Broadcast the title to all players
     */
    public void broadcast() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            send(p);
        }
    }

    /**
     * Clear the title
     *
     * @param player Player
     */
    public void clearTitle(Player player) {
        try {
            // Send timings first
            Object handle = getHandle(player);
            Object connection = playerConnection.get(handle);
            Object[] actions = packetActions.getEnumConstants();
            Object packet = packetTitle.getConstructor(packetActions,
                    chatBaseComponent).newInstance(actions[4], null);
            sendPacket.invoke(connection, packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Reset the title settings
     *
     * @param player Player
     */
    public void resetTitle(Player player) {
        try {
            // Send timings first
            Object handle = getHandle(player);
            Object connection = playerConnection.get(handle);
            Object[] actions = packetActions.getEnumConstants();
            Object packet = packetTitle.getConstructor(packetActions,
                    chatBaseComponent).newInstance(actions[5], null);
            sendPacket.invoke(connection, packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Class<?> getPrimitiveType(Class<?> clazz) {
        return CORRESPONDING_TYPES.containsKey(clazz) ? CORRESPONDING_TYPES
                .get(clazz) : clazz;
    }

    private Class<?>[] toPrimitiveTypeArray(Class<?>[] classes) {
        int a = classes != null ? classes.length : 0;
        Class<?>[] types = new Class<?>[a];
        for (int i = 0; i < a; i++)
            types[i] = getPrimitiveType(classes[i]);
        return types;
    }

    private static boolean equalsTypeArray(Class<?>[] a, Class<?>[] o) {
        if (a.length != o.length)
            return false;
        for (int i = 0; i < a.length; i++)
            if (!a[i].equals(o[i]) && !a[i].isAssignableFrom(o[i]))
                return false;
        return true;
    }

    private Object getHandle(Player player) {
        try {
            return methodPlayerGetHandle.invoke(player);
        } catch (Exception e) {
            //don't print a stacktrace. It just encourages uninformed users to report non-bugs.
            //e.printStackTrace();
            return null;
        }
    }

    private Method getMethod(String name, Class<?> clazz,
                             Class<?>... paramTypes) {
        Class<?>[] t = toPrimitiveTypeArray(paramTypes);
        for (Method m : clazz.getMethods()) {
            Class<?>[] types = toPrimitiveTypeArray(m.getParameterTypes());
            if (m.getName().equals(name) && equalsTypeArray(types, t))
                return m;
        }
        return null;
    }

    private String getVersion() {
        String name = Bukkit.getServer().getClass().getPackage().getName();
        String version = name.substring(name.lastIndexOf('.') + 1) + ".";
        return version;
    }

    private Class<?> getNMSClass(String className) {
        String fullName = "net.minecraft.server." + getVersion() + className;
        Class<?> clazz = null;
        try {
            clazz = Class.forName(fullName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return clazz;
    }

    private Class<?> getOBCClass(String className) {
        String fullName = "org.bukkit.craftbukkit." + getVersion() + className;
        Class<?> clazz = null;
        try {
            clazz = Class.forName(fullName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return clazz;
    }


    private Field getField(Class<?> clazz, String name) {
        try {
            Field field = clazz.getDeclaredField(name);
            field.setAccessible(true);
            return field;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private Method getMethod(Class<?> clazz, String name, Class<?>... args) {
        for (Method m : clazz.getMethods())
            if (m.getName().equals(name)
                    && (args.length == 0 || ClassListEqual(args,
                    m.getParameterTypes()))) {
                m.setAccessible(true);
                return m;
            }
        return null;
    }

    private boolean ClassListEqual(Class<?>[] l1, Class<?>[] l2) {
        boolean equal = true;
        if (l1.length != l2.length)
            return false;
        for (int i = 0; i < l1.length; i++)
            if (l1[i] != l2[i]) {
                equal = false;
                break;
            }
        return equal;
    }

    public ChatColor getTitleColor() {
        return titleColor;
    }

    public ChatColor getSubtitleColor() {
        return subtitleColor;
    }

    public int getFadeInTime() {
        return fadeInTime;
    }

    public int getFadeOutTime() {
        return fadeOutTime;
    }

    public int getStayTime() {
        return stayTime;
    }

    public boolean isTicks() {
        return ticks;
    }
}
