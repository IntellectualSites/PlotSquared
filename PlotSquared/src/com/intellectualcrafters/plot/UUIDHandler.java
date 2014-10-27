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

import java.util.Arrays;
import java.util.HashMap;
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

	private static BiMap<StringWrapper, UUID> uuidMap = HashBiMap.create(new HashMap<StringWrapper, UUID>());

    public static BiMap<StringWrapper, UUID> getUuidMap() {
        return uuidMap;
    }

	public static boolean uuidExists(UUID uuid) {
		return uuidMap.containsValue(uuid);
	}

	public static boolean nameExists(StringWrapper name) {
		return uuidMap.containsKey(name);
	}

	public static void add(StringWrapper name, UUID uuid) {
	    if (!uuidMap.containsKey(name) && !uuidMap.inverse().containsKey(uuid)) {
	        uuidMap.put(name, uuid);
	    }
	}

	/**
	 * @param name
	 * @return uuid
	 */
	public static UUID getUUID(String name) {
	    StringWrapper nameWrap = new StringWrapper(name);
		if (uuidMap.containsKey(nameWrap)) {
			return uuidMap.get(nameWrap);
		}
		Player player = Bukkit.getPlayer(name);
		if (player!=null) {
		    UUID uuid = player.getUniqueId();
		    uuidMap.put(nameWrap, uuid);
		    return uuid;
		}
		UUID uuid;
		if (online) {
            if(Settings.CUSTOM_API) {
                if ((uuid = getUuidOnlinePlayer(nameWrap)) != null) {
                    return uuid;
                }
                try {
                    return PlotMain.getUUIDSaver().mojangUUID(name);
                }
                catch(Exception e) {
                    try {
                        UUIDFetcher fetcher = new UUIDFetcher(Arrays.asList(name));
                        uuid = fetcher.call().get(name);
                        add(nameWrap, uuid);
                    }
                    catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            } else {
                try {
                    UUIDFetcher fetcher = new UUIDFetcher(Arrays.asList(name));
                    uuid = fetcher.call().get(name);
                    add(nameWrap, uuid);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
		}
		else {
			return getUuidOfflineMode(nameWrap);
		}
        return null;
	}

	/**
	 * @param uuid
	 * @return name (cache)
	 */
	private static StringWrapper loopSearch(UUID uuid) {
		return uuidMap.inverse().get(uuid);
	}

	/**
	 * @param uuid
	 * @return Name
	 */
	public static String getName(UUID uuid) {
		if (uuidExists(uuid)) {
			return loopSearch(uuid).value;
		}
		String name;
		if ((name = getNameOnlinePlayer(uuid)) != null) {
			return name;
		}
		if ((name = getNameOfflinePlayer(uuid)) != null) {
			return name;
		}
		if (online) {
            if(!Settings.CUSTOM_API) {
                try {
                    NameFetcher fetcher = new NameFetcher(Arrays.asList(uuid));
                    name = fetcher.call().get(uuid);
                    add(new StringWrapper(name), uuid);
                    return name;
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            } else {
                try {
                    return PlotMain.getUUIDSaver().mojangName(uuid);
                } catch(Exception e) {
                    try {
                        NameFetcher fetcher = new NameFetcher(Arrays.asList(uuid));
                        name = fetcher.call().get(uuid);
                        add(new StringWrapper(name), uuid);
                        return name;
                    } catch (Exception ex) {
                        e.printStackTrace();
                    }
                }
            }
            try {
                return PlotMain.getUUIDSaver().mojangName(uuid);
            } catch(Exception e) {
                try {
                    NameFetcher fetcher = new NameFetcher(Arrays.asList(uuid));
                    name = fetcher.call().get(uuid);
                    add(new StringWrapper(name), uuid);
                    return name;
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
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
	private static UUID getUuidOfflineMode(StringWrapper name) {
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
		add(new StringWrapper(name), uuid);
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
		add(new StringWrapper(name), uuid);
		return name;
	}

	/**
	 * @param name
	 * @return UUID
	 */
	private static UUID getUuidOnlinePlayer(StringWrapper name) {
		Player player = Bukkit.getPlayer(name.value);
		if (player == null) {
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
	private static UUID getUuidOfflinePlayer(StringWrapper name) {
		UUID uuid = UUID.nameUUIDFromBytes(("OfflinePlayer:" + name.value).getBytes(Charsets.UTF_8));
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
