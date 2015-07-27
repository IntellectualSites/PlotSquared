package com.plotsquared.bukkit.titles;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * [ PlotSquared DefaultTitleManager by Maxim Van de Wynckel ]
 *
 * @version 1.1.0
 * @author Maxim Van de Wynckel
 *
 */
public class DefaultTitleManager_183 {
    /* Title packet */
    private Class<?> packetTitle;
    /* Title packet actions ENUM */
    private Class<?> packetActions;
    /* Chat serializer */
    private Class<?> nmsChatSerializer;
    private Class<?> chatBaseComponent;
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

    /**
     * Create a new 1.8 title
     *
     * @param title
     *            Title
     * @throws ClassNotFoundException
     */
    public DefaultTitleManager_183(final String title) throws ClassNotFoundException {
        this.title = title;
        loadClasses();
    }

    /**
     * Create a new 1.8 title
     *
     * @param title
     *            Title text
     * @param subtitle
     *            Subtitle text
     * @throws ClassNotFoundException
     */
    public DefaultTitleManager_183(final String title, final String subtitle) throws ClassNotFoundException {
        this.title = title;
        this.subtitle = subtitle;
        loadClasses();
    }

    /**
     * Copy 1.8 title
     *
     * @param title
     *            Title
     * @throws ClassNotFoundException
     */
    public DefaultTitleManager_183(final DefaultTitleManager_183 title) throws ClassNotFoundException {
        // Copy title
        this.title = title.title;
        this.subtitle = title.subtitle;
        this.titleColor = title.titleColor;
        this.subtitleColor = title.subtitleColor;
        this.fadeInTime = title.fadeInTime;
        this.fadeOutTime = title.fadeOutTime;
        this.stayTime = title.stayTime;
        this.ticks = title.ticks;
        loadClasses();
    }

    /**
     * Create a new 1.8 title
     *
     * @param title
     *            Title text
     * @param subtitle
     *            Subtitle text
     * @param fadeInTime
     *            Fade in time
     * @param stayTime
     *            Stay on screen time
     * @param fadeOutTime
     *            Fade out time
     * @throws ClassNotFoundException
     */
    public DefaultTitleManager_183(final String title, final String subtitle, final int fadeInTime, final int stayTime, final int fadeOutTime) throws ClassNotFoundException {
        this.title = title;
        this.subtitle = subtitle;
        this.fadeInTime = fadeInTime;
        this.stayTime = stayTime;
        this.fadeOutTime = fadeOutTime;
        loadClasses();
    }

    /**
     * Load spigot and NMS classes
     * @throws ClassNotFoundException
     */
    private void loadClasses() throws ClassNotFoundException {
        this.packetTitle = getNMSClass("PacketPlayOutTitle");
        this.chatBaseComponent = getNMSClass("IChatBaseComponent");
        this.packetActions = getNMSClass("PacketPlayOutTitle$EnumTitleAction");
        this.nmsChatSerializer = getNMSClass("IChatBaseComponent$ChatSerializer");
        
    }

    /**
     * Set title text
     *
     * @param title
     *            Title
     */
    public void setTitle(final String title) {
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
     * @param subtitle
     *            Subtitle text
     */
    public void setSubtitle(final String subtitle) {
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
     * @param color
     *            Chat color
     */
    public void setTitleColor(final ChatColor color) {
        this.titleColor = color;
    }

    /**
     * Set the subtitle color
     *
     * @param color
     *            Chat color
     */
    public void setSubtitleColor(final ChatColor color) {
        this.subtitleColor = color;
    }

    /**
     * Set title fade in time
     *
     * @param time
     *            Time
     */
    public void setFadeInTime(final int time) {
        this.fadeInTime = time;
    }

    /**
     * Set title fade out time
     *
     * @param time
     *            Time
     */
    public void setFadeOutTime(final int time) {
        this.fadeOutTime = time;
    }

    /**
     * Set title stay time
     *
     * @param time
     *            Time
     */
    public void setStayTime(final int time) {
        this.stayTime = time;
    }

    /**
     * Set timings to ticks
     */
    public void setTimingsToTicks() {
        this.ticks = true;
    }

    /**
     * Set timings to seconds
     */
    public void setTimingsToSeconds() {
        this.ticks = false;
    }

    /**
     * Send the title to a player
     *
     * @param player
     *            Player
     * @throws InvocationTargetException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     */
    public void send(final Player player) throws Exception {
        if (this.packetTitle != null) {
            // First reset previous settings
            resetTitle(player);
            // Send timings first
            final Object handle = getHandle(player);
            final Object connection = getField(handle.getClass(), "playerConnection").get(handle);
            final Object[] actions = this.packetActions.getEnumConstants();
            final Method sendPacket = getMethod(connection.getClass(), "sendPacket");
            Object packet = this.packetTitle.getConstructor(this.packetActions, this.chatBaseComponent, Integer.TYPE, Integer.TYPE, Integer.TYPE).newInstance(actions[2], null, this.fadeInTime * (this.ticks ? 1 : 20), this.stayTime * (this.ticks ? 1 : 20), this.fadeOutTime * (this.ticks ? 1 : 20));
            // Send if set
            if ((this.fadeInTime != -1) && (this.fadeOutTime != -1) && (this.stayTime != -1)) {
                sendPacket.invoke(connection, packet);
            }
            // Send title
            Object serialized = getMethod(this.nmsChatSerializer, "a", String.class).invoke(null, "{text:\"" + ChatColor.translateAlternateColorCodes('&', this.title) + "\",color:" + this.titleColor.name().toLowerCase() + "}");
            packet = this.packetTitle.getConstructor(this.packetActions, this.chatBaseComponent).newInstance(actions[0], serialized);
            sendPacket.invoke(connection, packet);
            if (this.subtitle != "") {
                // Send subtitle if present
                serialized = getMethod(this.nmsChatSerializer, "a", String.class).invoke(null, "{text:\"" + ChatColor.translateAlternateColorCodes('&', this.subtitle) + "\",color:" + this.subtitleColor.name().toLowerCase() + "}");
                packet = this.packetTitle.getConstructor(this.packetActions, this.chatBaseComponent).newInstance(actions[1], serialized);
                sendPacket.invoke(connection, packet);
            }
        }
    }

    /**
     * Broadcast the title to all players
     * @throws Exception
     */
    public void broadcast() throws Exception {
        for (final Player p : Bukkit.getOnlinePlayers()) {
            send(p);
        }
    }

    /**
     * Clear the title
     *
     * @param player
     *            Player
     * @throws IllegalAccessException 
     * @throws IllegalArgumentException 
     */
    public void clearTitle(final Player player) throws Exception {
            // Send timings first
            final Object handle = getHandle(player);
            final Object connection = getField(handle.getClass(), "playerConnection").get(handle);
            final Object[] actions = this.packetActions.getEnumConstants();
            final Method sendPacket = getMethod(connection.getClass(), "sendPacket");
            final Object packet = this.packetTitle.getConstructor(this.packetActions, this.chatBaseComponent).newInstance(actions[3], null);
            sendPacket.invoke(connection, packet);
    }

    /**
     * Reset the title settings
     *
     * @param player
     *            Player
     * @throws SecurityException 
     * @throws NoSuchMethodException 
     * @throws InvocationTargetException 
     * @throws IllegalArgumentException 
     * @throws IllegalAccessException 
     * @throws InstantiationException 
     */
    public void resetTitle(final Player player) throws Exception {
        // Send timings first
        final Object handle = getHandle(player);
        final Object connection = getField(handle.getClass(), "playerConnection").get(handle);
        final Object[] actions = this.packetActions.getEnumConstants();
        final Method sendPacket = getMethod(connection.getClass(), "sendPacket");
        final Object packet = this.packetTitle.getConstructor(this.packetActions, this.chatBaseComponent).newInstance(actions[4], null);
        sendPacket.invoke(connection, packet);
    }

    private Class<?> getPrimitiveType(final Class<?> clazz) {
        return CORRESPONDING_TYPES.containsKey(clazz) ? CORRESPONDING_TYPES.get(clazz) : clazz;
    }

    private Class<?>[] toPrimitiveTypeArray(final Class<?>[] classes) {
        final int a = classes != null ? classes.length : 0;
        final Class<?>[] types = new Class<?>[a];
        for (int i = 0; i < a; i++) {
            types[i] = getPrimitiveType(classes[i]);
        }
        return types;
    }

    private static boolean equalsTypeArray(final Class<?>[] a, final Class<?>[] o) {
        if (a.length != o.length) {
            return false;
        }
        for (int i = 0; i < a.length; i++) {
            if (!a[i].equals(o[i]) && !a[i].isAssignableFrom(o[i])) {
                return false;
            }
        }
        return true;
    }

    private Object getHandle(final Object obj) {
        try {
            return getMethod("getHandle", obj.getClass()).invoke(obj);
        } catch (final Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private Method getMethod(final String name, final Class<?> clazz, final Class<?>... paramTypes) {
        final Class<?>[] t = toPrimitiveTypeArray(paramTypes);
        for (final Method m : clazz.getMethods()) {
            final Class<?>[] types = toPrimitiveTypeArray(m.getParameterTypes());
            if (m.getName().equals(name) && equalsTypeArray(types, t)) {
                return m;
            }
        }
        return null;
    }

    private String getVersion() {
        final String name = Bukkit.getServer().getClass().getPackage().getName();
        final String version = name.substring(name.lastIndexOf('.') + 1) + ".";
        return version;
    }

    private Class<?> getNMSClass(final String className) throws ClassNotFoundException {
        final String fullName = "net.minecraft.server." + getVersion() + className;
        Class<?> clazz = null;
        clazz = Class.forName(fullName);
        return clazz;
    }

    private Field getField(final Class<?> clazz, final String name) {
        try {
            final Field field = clazz.getDeclaredField(name);
            field.setAccessible(true);
            return field;
        } catch (final Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private Method getMethod(final Class<?> clazz, final String name, final Class<?>... args) {
        for (final Method m : clazz.getMethods()) {
            if (m.getName().equals(name) && ((args.length == 0) || ClassListEqual(args, m.getParameterTypes()))) {
                m.setAccessible(true);
                return m;
            }
        }
        return null;
    }

    private boolean ClassListEqual(final Class<?>[] l1, final Class<?>[] l2) {
        boolean equal = true;
        if (l1.length != l2.length) {
            return false;
        }
        for (int i = 0; i < l1.length; i++) {
            if (l1[i] != l2[i]) {
                equal = false;
                break;
            }
        }
        return equal;
    }
}
