package com.intellectualcrafters.plot;

import com.google.common.base.Charsets;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.intellectualcrafters.plot.uuid.NameFetcher;
import com.intellectualcrafters.plot.uuid.UUIDFetcher;
import com.intellectualcrafters.plot.uuid.UUIDSaver;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.UUID;

/**
 * This class can be used to efficiently translate UUIDs and names back and forth.
 * It uses three primary methods of achieving this:
 * - Read From Cache
 * - Read from OfflinePlayer objects
 * - Read from (if onlinemode: mojang api) (else: playername hashing)
 * All UUIDs/Usernames will be stored in a map (cache) until the server is
 * restarted.
 *
 * You can use getUuidMap() to save the uuids/names to a file (SQLite db for example).
 * Primary methods: getUUID(String name) & getName(UUID uuid) <-- You should ONLY use these.
 * Call startFetch(JavaPlugin plugin) in your onEnable().
 *
 * Originally created by:
 * @author Citymonstret
 * @author Empire92
 * for PlotSquared.
 */
public class UUIDHandler {

	private static boolean online = Bukkit.getServer().getOnlineMode();

	private static BiMap<String, UUID> uuidMap = HashBiMap.create();

    public static BiMap<String, UUID> getUuidMap() {
        return uuidMap;
    }

	public static boolean uuidExists(UUID uuid) {
		return uuidMap.containsValue(uuid);
	}

	public static boolean nameExists(String name) {
		return uuidMap.containsKey(name);
	}

	public static void add(String name, UUID uuid) {
		uuidMap.put(name, uuid);
	}

	/**
	 * @param plugin
	 */
	public static void startFetch(JavaPlugin plugin) {
		plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {
			@Override
			public void run() {
				OfflinePlayer[] offlinePlayers = Bukkit.getOfflinePlayers();
				int length = offlinePlayers.length;
				long start = System.currentTimeMillis();

				String name;
				UUID uuid;
				for (OfflinePlayer player : offlinePlayers) {
					uuid = player.getUniqueId();
					if (!uuidExists(uuid)) {
						name = player.getName();
						add(name, uuid);
					}
				}

				long time = System.currentTimeMillis() - start;
                int size = uuidMap.size();
                double ups;
                if(time == 0l || size == 0) {
                    ups = size;
                } else {
                    ups = size / time;
                }

                //Plot Squared Only...
				PlotMain.sendConsoleSenderMessage("&cFinished caching of offline player UUIDs! Took &6" + time + "&cms (&6" + ups + "&c per millisecond), &6"
						+ length + " &cUUIDs were cached" + " and there is now a grand total of &6" + size
						+ " &ccached.");
			}
		});
	}

	/**
	 * @param name
	 * @return uuid
	 */
	public static UUID getUUID(String name) {
		if (nameExists(name)) {
			return uuidMap.get(name);
		}
		UUID uuid;
		if ((uuid = getUuidOnlinePlayer(name)) != null) {
			return uuid;
		}
		if ((uuid = getUuidOfflinePlayer(name)) != null) {
			return uuid;
		}
		if (online) {
			try {
				UUIDFetcher fetcher = new UUIDFetcher(Arrays.asList(name));
				uuid = fetcher.call().get(name);
				add(name, uuid);
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		else {
			return getUuidOfflineMode(name);
		}
		return null;
	}

	/**
	 * @param uuid
	 * @return name (cache)
	 */
	private static String loopSearch(UUID uuid) {
		return uuidMap.inverse().get(uuid);
	}

	/**
	 * @param uuid
	 * @return Name
	 */
	public static String getName(UUID uuid) {
		if (uuidExists(uuid)) {
			return loopSearch(uuid);
		}
		String name;
		if ((name = getNameOnlinePlayer(uuid)) != null) {
			return name;
		}
		if ((name = getNameOfflinePlayer(uuid)) != null) {
			return name;
		}
		if (online) {
			try {
				NameFetcher fetcher = new NameFetcher(Arrays.asList(uuid));
				name = fetcher.call().get(uuid);
				add(name, uuid);
				return name;
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		else {
			return "unknown";
		}
		return "";
	}

	/**
	 * @param name
	 * @return UUID (name hash)
	 */
	private static UUID getUuidOfflineMode(String name) {
		UUID uuid = UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes(Charsets.UTF_8));
		add(name, uuid);
		return uuid;
	}

	/**
	 * @param uuid
	 * @return String - name
	 */
	private static String getNameOnlinePlayer(UUID uuid) {
		Player player = Bukkit.getPlayer(uuid);
		if (player == null || !player.isOnline()) {
			return null;
		}
		String name = player.getName();
		add(name, uuid);
		return name;
	}

	/**
	 * @param uuid
	 * @return String - name
	 */
	private static String getNameOfflinePlayer(UUID uuid) {
		OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
		if (player == null || !player.hasPlayedBefore()) {
			return null;
		}
		String name = player.getName();
		add(name, uuid);
		return name;
	}

	/**
	 * @param name
	 * @return UUID
	 */
	private static UUID getUuidOnlinePlayer(String name) {
		Player player = Bukkit.getPlayer(name);
		if (player == null || !player.isOnline()) {
			return null;
		}
		UUID uuid = player.getUniqueId();
		add(name, uuid);
		return uuid;
	}

	/**
	 * @param name
	 * @return UUID (username hash)
	 */
	private static UUID getUuidOfflinePlayer(String name) {
		UUID uuid = UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes(Charsets.UTF_8));
		add(name, uuid);
		return uuid;
	}


    /* Save UUIDS */
    public static void handleSaving() {
        UUIDSaver saver = PlotMain.getUUIDSaver();
        // Should it save per UUIDSet or all of them? TODO: Let Jesse decide xD
        saver.globalSave(getUuidMap());
    }
}
