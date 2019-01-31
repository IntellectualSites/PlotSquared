package com.github.intellectualsites.plotsquared.bukkit.titles;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public abstract class TitleManager {

    private static final Map<Class<?>, Class<?>> CORRESPONDING_TYPES = new HashMap<>();
    /* Title packet */ Class<?> packetTitle;
    /* Title packet actions ENUM */ Class<?> packetActions;
    /* Chat serializer */ Class<?> nmsChatSerializer;
    Class<?> chatBaseComponent;
    ChatColor titleColor = ChatColor.WHITE;
    ChatColor subtitleColor = ChatColor.WHITE;
    /* Title timings */ int fadeInTime = -1;
    int stayTime = -1;
    int fadeOutTime = -1;
    boolean ticks = false;
    /* Title text and color */
    private String title = "";
    /* Subtitle text and color */
    private String subtitle = "";

    /**
     * Create a new 1.8 title.
     *
     * @param title       Title text
     * @param subtitle    Subtitle text
     * @param fadeInTime  Fade in time
     * @param stayTime    Stay on screen time
     * @param fadeOutTime Fade out time
     */
    TitleManager(String title, String subtitle, int fadeInTime, int stayTime, int fadeOutTime) {
        this.title = title;
        this.subtitle = subtitle;
        this.fadeInTime = fadeInTime;
        this.stayTime = stayTime;
        this.fadeOutTime = fadeOutTime;
        loadClasses();
    }

    abstract void loadClasses();

    /**
     * Gets title text.
     *
     * @return Title text
     */
    public final String getTitle() {
        return this.title;
    }

    /**
     * Sets the text for the title.
     *
     * @param title Title
     */
    public final void setTitle(String title) {
        this.title = title;
    }

    /**
     * Gets the subtitle text.
     *
     * @return Subtitle text
     */
    public final String getSubtitle() {
        return this.subtitle;
    }

    /**
     * Sets subtitle text.
     *
     * @param subtitle Subtitle text
     */
    public final void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    /**
     * Sets the title color.
     *
     * @param color Chat color
     */
    public final void setTitleColor(ChatColor color) {
        this.titleColor = color;
    }

    /**
     * Sets the subtitle color.
     *
     * @param color Chat color
     */
    public final void setSubtitleColor(ChatColor color) {
        this.subtitleColor = color;
    }

    /**
     * Sets title fade in time.
     *
     * @param time Time
     */
    public final void setFadeInTime(int time) {
        this.fadeInTime = time;
    }

    /**
     * Sets title fade out time.
     *
     * @param time Time
     */
    public final void setFadeOutTime(int time) {
        this.fadeOutTime = time;
    }

    /**
     * Sets title stay time.
     *
     * @param time Time
     */
    public final void setStayTime(int time) {
        this.stayTime = time;
    }

    /**
     * Sets timings to ticks.
     */
    public final void setTimingsToTicks() {
        this.ticks = true;
    }

    /**
     * Sets timings to seconds.
     */
    public final void setTimingsToSeconds() {
        this.ticks = false;
    }

    /**
     * Sends the title to a player.
     *
     * @param player Player
     * @throws IllegalArgumentException
     * @throws ReflectiveOperationException
     * @throws SecurityException
     */
    public abstract void send(Player player)
        throws IllegalArgumentException, ReflectiveOperationException, SecurityException;

    /**
     * Broadcasts the title to all players.
     *
     * @throws Exception
     */
    public final void broadcast() throws Exception {
        for (Player player : Bukkit.getOnlinePlayers()) {
            send(player);
        }
    }

    /**
     * Clears the title.
     *
     * @param player Player
     * @throws IllegalArgumentException
     * @throws ReflectiveOperationException
     * @throws SecurityException
     */
    public abstract void clearTitle(Player player)
        throws IllegalArgumentException, ReflectiveOperationException, SecurityException;

    /**
     * Resets the title settings.
     *
     * @param player Player
     * @throws IllegalArgumentException
     * @throws ReflectiveOperationException
     * @throws SecurityException
     */
    public abstract void resetTitle(Player player)
        throws IllegalArgumentException, ReflectiveOperationException, SecurityException;

    private Class<?> getPrimitiveType(Class<?> clazz) {
        return CORRESPONDING_TYPES.getOrDefault(clazz, clazz);
    }

    private Class<?>[] toPrimitiveTypeArray(Class<?>[] classes) {
        int a;
        if (classes != null) {
            a = classes.length;
        } else {
            a = 0;
        }
        Class<?>[] types = new Class<?>[a];
        for (int i = 0; i < a; i++) {
            types[i] = getPrimitiveType(classes[i]);
        }
        return types;
    }

    final Object getHandle(Object obj) {
        try {
            return getMethod("getHandle", obj.getClass()).invoke(obj);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            e.printStackTrace();
            return null;
        }
    }

    final Method getMethod(String name, Class<?> clazz, Class<?>... paramTypes) {
        Class<?>[] t = toPrimitiveTypeArray(paramTypes);
        for (Method m : clazz.getMethods()) {
            Class<?>[] types = toPrimitiveTypeArray(m.getParameterTypes());
            if (m.getName().equals(name) && equalsTypeArray(types, t)) {
                return m;
            }
        }
        return null;
    }

    private boolean equalsTypeArray(Class<?>[] a, Class<?>[] o) {
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

    boolean classListEqual(Class<?>[] l1, Class<?>[] l2) {
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
