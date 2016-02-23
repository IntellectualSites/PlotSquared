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
public class DefaultTitleManager {

    private static final Map<Class<?>, Class<?>> CORRESPONDING_TYPES = new HashMap<>();
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

    /**
     * Create a new 1.8 title
     *
     * @param title
     *            Title
     * @throws ClassNotFoundException
     */
    public DefaultTitleManager(final String title) throws ClassNotFoundException {
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
    public DefaultTitleManager(final String title, final String subtitle) throws ClassNotFoundException {
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
    public DefaultTitleManager(final DefaultTitleManager title) throws ClassNotFoundException {
        // Copy title
        this.title = title.title;
        subtitle = title.subtitle;
        titleColor = title.titleColor;
        subtitleColor = title.subtitleColor;
        fadeInTime = title.fadeInTime;
        fadeOutTime = title.fadeOutTime;
        stayTime = title.stayTime;
        ticks = title.ticks;
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
    public DefaultTitleManager(final String title, final String subtitle, final int fadeInTime, final int stayTime, final int fadeOutTime)
            throws ClassNotFoundException {
        this.title = title;
        this.subtitle = subtitle;
        this.fadeInTime = fadeInTime;
        this.stayTime = stayTime;
        this.fadeOutTime = fadeOutTime;
        loadClasses();
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

    /**
     * Load spigot and NMS classes
     * @throws ClassNotFoundException
     */
    private void loadClasses() throws ClassNotFoundException {
        packetTitle = getNMSClass("PacketPlayOutTitle");
        packetActions = getNMSClass("EnumTitleAction");
        chatBaseComponent = getNMSClass("IChatBaseComponent");
        nmsChatSerializer = getNMSClass("ChatSerializer");
    }

    /**
     * Get title text
     *
     * @return Title text
     */
    public String getTitle() {
        return title;
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
     * Get subtitle text
     *
     * @return Subtitle text
     */
    public String getSubtitle() {
        return subtitle;
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
     * Set the title color
     *
     * @param color
     *            Chat color
     */
    public void setTitleColor(final ChatColor color) {
        titleColor = color;
    }

    /**
     * Set the subtitle color
     *
     * @param color
     *            Chat color
     */
    public void setSubtitleColor(final ChatColor color) {
        subtitleColor = color;
    }

    /**
     * Set title fade in time
     *
     * @param time
     *            Time
     */
    public void setFadeInTime(final int time) {
        fadeInTime = time;
    }

    /**
     * Set title fade out time
     *
     * @param time
     *            Time
     */
    public void setFadeOutTime(final int time) {
        fadeOutTime = time;
    }

    /**
     * Set title stay time
     *
     * @param time
     *            Time
     */
    public void setStayTime(final int time) {
        stayTime = time;
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
     * @param player
     *            Player
     * @throws InvocationTargetException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     */
    public void send(final Player player)
            throws IllegalArgumentException, IllegalAccessException, InstantiationException, InvocationTargetException, NoSuchMethodException,
            SecurityException {
        if (packetTitle != null) {
            // First reset previous settings
            resetTitle(player);
            // Send timings first
            final Object handle = getHandle(player);
            final Object connection = getField(handle.getClass(), "playerConnection").get(handle);
            final Object[] actions = packetActions.getEnumConstants();
            final Method sendPacket = getMethod(connection.getClass(), "sendPacket");
            Object packet = packetTitle.getConstructor(packetActions, chatBaseComponent, Integer.TYPE, Integer.TYPE, Integer.TYPE)
                    .newInstance(actions[2], null, fadeInTime * (ticks ? 1 : 20),
                            stayTime * (ticks ? 1 : 20), fadeOutTime * (ticks ? 1 : 20));
            // Send if set
            if (fadeInTime != -1 && fadeOutTime != -1 && stayTime != -1) {
                sendPacket.invoke(connection, packet);
            }
            // Send title
            Object serialized = getMethod(nmsChatSerializer, "a", String.class).invoke(null,
                    "{text:\"" + ChatColor.translateAlternateColorCodes('&', title) + "\",color:" + titleColor.name().toLowerCase() + "}");
            packet = packetTitle.getConstructor(packetActions, chatBaseComponent).newInstance(actions[0], serialized);
            sendPacket.invoke(connection, packet);
            if (!subtitle.isEmpty()) {
                // Send subtitle if present
                serialized = getMethod(nmsChatSerializer, "a", String.class).invoke(null,
                        "{text:\"" + ChatColor.translateAlternateColorCodes('&', subtitle) + "\",color:" + subtitleColor.name().toLowerCase() + "}");
                packet = packetTitle.getConstructor(packetActions, chatBaseComponent).newInstance(actions[1], serialized);
                sendPacket.invoke(connection, packet);
            }
        }
    }

    /**
     * Broadcast the title to all players
     * @throws IllegalArgumentException, IllegalAccessException, InstantiationException, InvocationTargetException, NoSuchMethodException,
    SecurityException
     */
    public void broadcast()
            throws IllegalArgumentException, IllegalAccessException, InstantiationException, InvocationTargetException, NoSuchMethodException,
            SecurityException {
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
    public void clearTitle(final Player player)
            throws IllegalArgumentException, IllegalAccessException, InstantiationException, InvocationTargetException, NoSuchMethodException,
            SecurityException {
        // Send timings first
        final Object handle = getHandle(player);
        final Object connection = getField(handle.getClass(), "playerConnection").get(handle);
        final Object[] actions = packetActions.getEnumConstants();
        final Method sendPacket = getMethod(connection.getClass(), "sendPacket");
        final Object packet = packetTitle.getConstructor(packetActions, chatBaseComponent).newInstance(actions[3], null);
        sendPacket.invoke(connection, packet);
    }

    /**
     * Reset the title settings
     *
     * @param player
     *            Player
     */
    public void resetTitle(final Player player)
            throws IllegalArgumentException, IllegalAccessException, InstantiationException, InvocationTargetException, NoSuchMethodException,
            SecurityException {
        // Send timings first
        final Object handle = getHandle(player);
        final Object connection = getField(handle.getClass(), "playerConnection").get(handle);
        final Object[] actions = packetActions.getEnumConstants();
        final Method sendPacket = getMethod(connection.getClass(), "sendPacket");
        final Object packet = packetTitle.getConstructor(packetActions, chatBaseComponent).newInstance(actions[4], null);
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

    private Object getHandle(final Object obj) {
        try {
            return getMethod("getHandle", obj.getClass()).invoke(obj);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return null;
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return null;
        } catch (InvocationTargetException e) {
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
        return name.substring(name.lastIndexOf('.') + 1) + ".";
    }

    private Class<?> getNMSClass(final String className) throws ClassNotFoundException {
        final String fullName = "net.minecraft.server." + getVersion() + className;
        return Class.forName(fullName);
    }

    private Field getField(final Class<?> clazz, final String name) {
        try {
            final Field field = clazz.getDeclaredField(name);
            field.setAccessible(true);
            return field;
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
            return null;
        } catch (SecurityException e) {
            e.printStackTrace();
            return null;
        }
    }

    private Method getMethod(final Class<?> clazz, final String name, final Class<?>... args) {
        for (final Method m : clazz.getMethods()) {
            if (m.getName().equals(name) && (args.length == 0 || ClassListEqual(args, m.getParameterTypes()))) {
                m.setAccessible(true);
                return m;
            }
        }
        return null;
    }

    private boolean ClassListEqual(final Class<?>[] l1, final Class<?>[] l2) {
        if (l1.length != l2.length) {
            return false;
        }
        boolean equal = true;
        for (int i = 0; i < l1.length; i++) {
            if (l1[i] != l2[i]) {
                equal = false;
                break;
            }
        }
        return equal;
    }
}
