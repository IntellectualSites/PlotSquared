package com.plotsquared.bukkit.titles;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public abstract class TitleManager {

    private static final Map<Class<?>, Class<?>> CORRESPONDING_TYPES = new HashMap<>();
    /* Title packet */
    Class<?> packetTitle;
    /* Title packet actions ENUM */
    Class<?> packetActions;
    /* Chat serializer */
    Class<?> nmsChatSerializer;
    Class<?> chatBaseComponent;
    ChatColor titleColor = ChatColor.WHITE;
    ChatColor subtitleColor = ChatColor.WHITE;
    /* Title timings */
    int fadeInTime = -1;
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
     * @param title Title text
     * @param subtitle Subtitle text
     * @param fadeInTime Fade in time
     * @param stayTime Stay on screen time
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
     * Get title text.
     *
     * @return Title text
     */
    public final String getTitle() {
        return this.title;
    }

    /**
     * Set title text.
     *
     * @param title Title
     */
    public final void setTitle(String title) {
        this.title = title;
    }

    /**
     * Get subtitle text.
     *
     * @return Subtitle text
     */
    public final String getSubtitle() {
        return this.subtitle;
    }

    /**
     * Set subtitle text.
     *
     * @param subtitle Subtitle text
     */
    public final void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    /**
     * Set the title color.
     *
     * @param color Chat color
     */
    public final void setTitleColor(ChatColor color) {
        this.titleColor = color;
    }

    /**
     * Set the subtitle color.
     *
     * @param color Chat color
     */
    public final void setSubtitleColor(ChatColor color) {
        this.subtitleColor = color;
    }

    /**
     * Set title fade in time.
     *
     * @param time Time
     */
    public final void setFadeInTime(int time) {
        this.fadeInTime = time;
    }

    /**
     * Set title fade out time.
     *
     * @param time Time
     */
    public final void setFadeOutTime(int time) {
        this.fadeOutTime = time;
    }

    /**
     * Set title stay time.
     *
     * @param time Time
     */
    public final void setStayTime(int time) {
        this.stayTime = time;
    }

    /**
     * Set timings to ticks.
     */
    public final void setTimingsToTicks() {
        this.ticks = true;
    }

    /**
     * Set timings to seconds.
     */
    public final void setTimingsToSeconds() {
        this.ticks = false;
    }

    /**
     * Send the title to a player.
     *
     * @param player Player
     * @throws Exception
     */
    public abstract void send(Player player) throws Exception;

    /**
     * Broadcast the title to all players.
     *
     * @throws Exception
     */
    public final void broadcast() throws Exception {
        for (Player p : Bukkit.getOnlinePlayers()) {
            send(p);
        }
    }

    /**
     * Clear the title.
     *
     * @param player Player
     * @throws Exception
     */
    public abstract void clearTitle(Player player) throws Exception;

    /**
     * Reset the title settings.
     *
     * @param player Player
     * @throws Exception
     */
    public abstract void resetTitle(Player player) throws Exception;

    private Class<?> getPrimitiveType(Class<?> clazz) {
        if (CORRESPONDING_TYPES.containsKey(clazz)) {
            return CORRESPONDING_TYPES.get(clazz);
        } else {
            return clazz;
        }
    }

    final Class<?>[] toPrimitiveTypeArray(Class<?>[] classes) {
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

}
