package com.intellectualcrafters.plot.database;

import com.intellectualcrafters.plot.flag.Flag;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotArea;
import com.intellectualcrafters.plot.object.PlotCluster;
import com.intellectualcrafters.plot.object.PlotId;
import com.intellectualcrafters.plot.object.RunnableVal;
import com.intellectualcrafters.plot.object.comment.PlotComment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class AbstractDBTest implements AbstractDB {

    @Override public void setOwner(Plot plot, UUID uuid) {
    }

    @Override public void createPlotsAndData(ArrayList<Plot> plots, Runnable whenDone) {
    }

    @Override public void createPlot(Plot plot) {
    }

    @Override public void createTables() {
    }

    @Override public void delete(Plot plot) {
    }

    @Override public void deleteSettings(Plot plot) {
    }

    @Override public void deleteHelpers(Plot plot) {
    }

    @Override public void deleteTrusted(Plot plot) {
    }

    @Override public void deleteDenied(Plot plot) {
    }

    @Override public void deleteComments(Plot plot) {
    }

    @Override public void deleteRatings(Plot plot) {
    }

    @Override public void delete(PlotCluster cluster) {
    }

    @Override public void addPersistentMeta(UUID uuid, String key, byte[] meta, boolean delete) {
    }

    @Override public void removePersistentMeta(UUID uuid, String key) {
    }

    @Override public void getPersistentMeta(UUID uuid, RunnableVal<Map<String, byte[]>> result) {
    }

    @Override public void createPlotSettings(int id, Plot plot) {
    }

    @Override public int getId(Plot plot) {
        return 0;
    }

    @Override public int getClusterId(PlotCluster cluster) {
        return 0;
    }

    @Override public HashMap<String, HashMap<PlotId, Plot>> getPlots() {
        return null;
    }

    @Override public void validateAllPlots(Set<Plot> toValidate) {
    }

    @Override public HashMap<String, Set<PlotCluster>> getClusters() {
        return null;
    }

    @Override public void setMerged(Plot plot, boolean[] merged) {
    }

    @Override public void swapPlots(Plot plot1, Plot plot2) {
    }

    @Override public void setFlags(Plot plot, HashMap<Flag<?>, Object> flags) {
    }

    @Override public void setFlags(PlotCluster cluster, HashMap<Flag<?>, Object> flags) {
    }

    @Override public void setClusterName(PlotCluster cluster, String name) {
    }

    @Override public void setAlias(Plot plot, String alias) {
    }

    @Override public void purgeIds(Set<Integer> uniqueIds) {
    }

    @Override public void purge(PlotArea area, Set<PlotId> plotIds) {
    }

    @Override public void setPosition(Plot plot, String position) {
    }

    @Override public void setPosition(PlotCluster cluster, String position) {
    }

    @Override public void removeTrusted(Plot plot, UUID uuid) {
    }

    @Override public void removeHelper(PlotCluster cluster, UUID uuid) {
    }

    @Override public void removeMember(Plot plot, UUID uuid) {
    }

    @Override public void removeInvited(PlotCluster cluster, UUID uuid) {
    }

    @Override public void setTrusted(Plot plot, UUID uuid) {
    }

    @Override public void setHelper(PlotCluster cluster, UUID uuid) {
    }

    @Override public void setMember(Plot plot, UUID uuid) {
    }

    @Override public void setInvited(PlotCluster cluster, UUID uuid) {
    }

    @Override public void removeDenied(Plot plot, UUID uuid) {
    }

    @Override public void setDenied(Plot plot, UUID uuid) {
    }

    @Override public HashMap<UUID, Integer> getRatings(Plot plot) {
        return null;
    }

    @Override public void setRating(Plot plot, UUID rater, int value) {
    }

    @Override public void removeComment(Plot plot, PlotComment comment) {
    }

    @Override public void clearInbox(Plot plot, String inbox) {
    }

    @Override public void setComment(Plot plot, PlotComment comment) {
    }

    @Override public void getComments(Plot plot, String inbox, RunnableVal<List<PlotComment>> whenDone) {
    }

    @Override public void createPlotAndSettings(Plot plot, Runnable whenDone) {
    }

    @Override public void createCluster(PlotCluster cluster) {
    }

    @Override public void resizeCluster(PlotCluster current, PlotId min, PlotId max) {
    }

    @Override public void movePlot(Plot originalPlot, Plot newPlot) {
    }

    @Override public void replaceUUID(UUID old, UUID now) {
    }

    @Override public boolean deleteTables() {
        return false;
    }

    @Override public void close() {
    }

    @Override public void replaceWorld(String oldWorld, String newWorld, PlotId min, PlotId max) {
    }

    @Override public void updateTables(int[] oldVersion) {
    }
}
