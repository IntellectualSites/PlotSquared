package com.plotsquared.bukkit.titles;

import com.plotsquared.bukkit.chat.Reflection;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;

public class DefaultTitleManager_183 extends DefaultTitleManager {

    /**
     * Create a new 1.8 title.
     *
     * @param title Title text
     * @param subtitle Subtitle text
     * @param fadeInTime Fade in time
     * @param stayTime Stay on screen time
     * @param fadeOutTime Fade out time
     */
    public DefaultTitleManager_183(String title, String subtitle, int fadeInTime, int stayTime, int fadeOutTime) {
        super(title, subtitle, fadeInTime, stayTime, fadeOutTime);
    }

    /**
     * Load spigot and NMS classes.
     */
    @Override
    void loadClasses() {
        this.packetTitle = Reflection.getNMSClass("PacketPlayOutTitle");
        this.chatBaseComponent = Reflection.getNMSClass("IChatBaseComponent");
        this.packetActions = Reflection.getNMSClass("PacketPlayOutTitle$EnumTitleAction");
        this.nmsChatSerializer = Reflection.getNMSClass("IChatBaseComponent$ChatSerializer");
    }

    @Override
    public void send(Player player) throws IllegalArgumentException, ReflectiveOperationException, SecurityException {
        if (this.packetTitle != null) {
            // First reset previous settings
            resetTitle(player);
            // Send timings first
            Object handle = getHandle(player);
            Object connection = getField(handle.getClass(), "playerConnection").get(handle);
            Object[] actions = this.packetActions.getEnumConstants();
            Method sendPacket = getMethod(connection.getClass(), "sendPacket");
            Object packet = this.packetTitle
                    .getConstructor(this.packetActions, this.chatBaseComponent, Integer.TYPE, Integer.TYPE, Integer.TYPE)
                    .newInstance(actions[2], null,
                            this.fadeInTime * (this.ticks ? 1 : 20),
                            this.stayTime * (this.ticks ? 1 : 20), this.fadeOutTime * (this.ticks ? 1 : 20));
            // Send if set
            if ((this.fadeInTime != -1) && (this.fadeOutTime != -1) && (this.stayTime != -1)) {
                sendPacket.invoke(connection, packet);
            }
            // Send title
            Object serialized = getMethod(this.nmsChatSerializer, "a", String.class).invoke(null,
                    "{text:\"" + ChatColor.translateAlternateColorCodes('&', this.getTitle()) + "\",color:" + this.titleColor.name().toLowerCase()
                            + "}");
            packet = this.packetTitle.getConstructor(this.packetActions, this.chatBaseComponent).newInstance(actions[0], serialized);
            sendPacket.invoke(connection, packet);
            if (!this.getSubtitle().isEmpty()) {
                // Send subtitle if present
                serialized = getMethod(this.nmsChatSerializer, "a", String.class).invoke(null,
                        "{text:\"" + ChatColor.translateAlternateColorCodes('&', this.getSubtitle()) + "\",color:" + this.subtitleColor.name()
                                .toLowerCase() + "}");
                packet = this.packetTitle.getConstructor(this.packetActions, this.chatBaseComponent).newInstance(actions[1], serialized);
                sendPacket.invoke(connection, packet);
            }
        }
    }

    private Method getMethod(Class<?> clazz, String name, Class<?>... args) {
        for (Method m : clazz.getMethods()) {
            if (m.getName().equals(name) && ((args.length == 0) || ClassListEqual(args, m.getParameterTypes()))) {
                m.setAccessible(true);
                return m;
            }
        }
        return null;
    }

}
