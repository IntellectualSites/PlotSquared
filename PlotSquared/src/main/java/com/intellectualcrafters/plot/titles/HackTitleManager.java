package com.intellectualcrafters.plot.titles;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

/**
 * Minecraft 1.8 Title
 * 
 * @version 1.0.4
 * @author Maxim Van de Wynckel
 */
public class HackTitleManager {
	/* Title packet */
	private Class<?> packetTitle;
	/* Title packet actions ENUM */
	private Class<?> packetActions;
	/* Chat serializer */
	private Class<?> nmsChatSerializer;
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
	 */
	public HackTitleManager(String title) {
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
	 */
	public HackTitleManager(String title, String subtitle) {
		this.title = title;
		this.subtitle = subtitle;
		loadClasses();
	}

	/**
	 * Copy 1.8 title
	 * 
	 * @param title
	 *            Title
	 */
	public HackTitleManager(HackTitleManager title) {
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
	 */
	public HackTitleManager(String title, String subtitle, int fadeInTime, int stayTime,
			int fadeOutTime) {
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
		packetTitle = getClass("org.spigotmc.ProtocolInjector$PacketTitle");
		packetActions = getClass("org.spigotmc.ProtocolInjector$PacketTitle$Action");
		nmsChatSerializer = getNMSClass("ChatSerializer");
	}

	/**
	 * Set title text
	 * 
	 * @param title
	 *            Title
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
	 * @param subtitle
	 *            Subtitle text
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
	 * @param color
	 *            Chat color
	 */
	public void setTitleColor(ChatColor color) {
		this.titleColor = color;
	}

	/**
	 * Set the subtitle color
	 * 
	 * @param color
	 *            Chat color
	 */
	public void setSubtitleColor(ChatColor color) {
		this.subtitleColor = color;
	}

	/**
	 * Set title fade in time
	 * 
	 * @param time
	 *            Time
	 */
	public void setFadeInTime(int time) {
		this.fadeInTime = time;
	}

	/**
	 * Set title fade out time
	 * 
	 * @param time
	 *            Time
	 */
	public void setFadeOutTime(int time) {
		this.fadeOutTime = time;
	}

	/**
	 * Set title stay time
	 * 
	 * @param time
	 *            Time
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
	 * @param player
	 *            Player
	 */
	public void send(Player player) throws Exception {
		if (getProtocolVersion(player) >= 47 && isSpigot()
				&& packetTitle != null) {
			// First reset previous settings
			resetTitle(player);
			// Send timings first
			Object handle = getHandle(player);
			Object connection = getField(handle.getClass(),
					"playerConnection").get(handle);
			Object[] actions = packetActions.getEnumConstants();
			Method sendPacket = getMethod(connection.getClass(),
					"sendPacket");
			Object packet = packetTitle.getConstructor(packetActions,
					Integer.TYPE, Integer.TYPE, Integer.TYPE).newInstance(
					actions[2], fadeInTime * (ticks ? 1 : 20),
					stayTime * (ticks ? 1 : 20),
					fadeOutTime * (ticks ? 1 : 20));
			// Send if set
			if (fadeInTime != -1 && fadeOutTime != -1 && stayTime != -1)
				sendPacket.invoke(connection, packet);

			// Send title
			Object serialized = getMethod(nmsChatSerializer, "a",
					String.class).invoke(
					null,
					"{text:\""
							+ ChatColor.translateAlternateColorCodes('&',
									title) + "\",color:"
							+ titleColor.name().toLowerCase() + "}");
			packet = packetTitle.getConstructor(packetActions,
					getNMSClass("IChatBaseComponent")).newInstance(
					actions[0], serialized);
			sendPacket.invoke(connection, packet);
			if (subtitle != "") {
				// Send subtitle if present
				serialized = getMethod(nmsChatSerializer, "a", String.class)
						.invoke(null,
								"{text:\""
										+ ChatColor
												.translateAlternateColorCodes(
														'&', subtitle)
										+ "\",color:"
										+ subtitleColor.name()
												.toLowerCase() + "}");
				packet = packetTitle.getConstructor(packetActions,
						getNMSClass("IChatBaseComponent")).newInstance(
						actions[1], serialized);
				sendPacket.invoke(connection, packet);
			}
		}
	}

	/**
	 * Broadcast the title to all players
	 */
	public void broadcast() throws Exception {
		for (Player p : Bukkit.getOnlinePlayers()) {
			send(p);
		}
	}

	/**
	 * Clear the title
	 * 
	 * @param player
	 *            Player
	 */
	public void clearTitle(Player player) {
		if (getProtocolVersion(player) >= 47 && isSpigot()) {
			try {
				// Send timings first
				Object handle = getHandle(player);
				Object connection = getField(handle.getClass(),
						"playerConnection").get(handle);
				Object[] actions = packetActions.getEnumConstants();
				Method sendPacket = getMethod(connection.getClass(),
						"sendPacket");
				Object packet = packetTitle.getConstructor(packetActions)
						.newInstance(actions[3]);
				sendPacket.invoke(connection, packet);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Reset the title settings
	 * 
	 * @param player
	 *            Player
	 */
	public void resetTitle(Player player) {
		if (getProtocolVersion(player) >= 47 && isSpigot()) {
			try {
				// Send timings first
				Object handle = getHandle(player);
				Object connection = getField(handle.getClass(),
						"playerConnection").get(handle);
				Object[] actions = packetActions.getEnumConstants();
				Method sendPacket = getMethod(connection.getClass(),
						"sendPacket");
				Object packet = packetTitle.getConstructor(packetActions)
						.newInstance(actions[4]);
				sendPacket.invoke(connection, packet);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Get the protocol version of the player
	 * 
	 * @param player
	 *            Player
	 * @return Protocol version
	 */
	private int getProtocolVersion(Player player) {
		int version = 0;
		try {
			Object handle = getHandle(player);
			Object connection = getField(handle.getClass(), "playerConnection")
					.get(handle);
			Object networkManager = getValue("networkManager", connection);
			version = (Integer) getMethod("getVersion",
					networkManager.getClass()).invoke(networkManager);

			return version;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return version;
	}

	/**
	 * Check if running spigot
	 * 
	 * @return Spigot
	 */
	private boolean isSpigot() {
		return Bukkit.getVersion().contains("Spigot");
	}

	/**
	 * Get class by url
	 * 
	 * @param namespace
	 *            Namespace url
	 * @return Class
	 */
	private Class<?> getClass(String namespace) {
		try {
			return Class.forName(namespace);
		} catch (Exception e) {

		}
		return null;
	}

	private Field getField(String name, Class<?> clazz) throws Exception {
		return clazz.getDeclaredField(name);
	}

	private Object getValue(String name, Object obj) throws Exception {
		Field f = getField(name, obj.getClass());
		f.setAccessible(true);
		return f.get(obj);
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

	private Object getHandle(Object obj) {
		try {
			return getMethod("getHandle", obj.getClass()).invoke(obj);
		} catch (Exception e) {
			e.printStackTrace();
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
}