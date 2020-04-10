package com.github.intellectualsites.plotsquared.util;

public class PremiumVerification {
    private static Boolean usingPremium;

    /**
     * @return Account ID if downloaded through SpigotMC
     */
    public static String getUserID() {
        return "%%__USER__%%";
    }

    /**
     * @return Download ID if downloaded through SpigotMC
     */
    public static String getDownloadID() {
        return "%%__NONCE__%%";
    }

    /**
     * @param userID Spigot user ID
     * @return true if userID does not contain __USER__
     */
    private static Boolean isPremium(String userID) {
        return !userID.contains("__USER__");
    }

    /**
     * Returns true if this plugin is premium
     */
    public static Boolean isPremium() {
        return usingPremium == null ? (usingPremium = isPremium(getUserID())) : usingPremium;
    }

}
