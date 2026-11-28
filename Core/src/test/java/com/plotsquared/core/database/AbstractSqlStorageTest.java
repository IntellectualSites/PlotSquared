/*
 * PlotSquared, a land and world management plugin for Minecraft.
 * Copyright (C) IntellectualSites <https://intellectualsites.com>
 * Copyright (C) IntellectualSites team and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.plotsquared.core.database;

import com.plotsquared.core.configuration.file.YamlConfiguration;
import com.plotsquared.core.listener.PlotListener;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.PlotArea;
import com.plotsquared.core.plot.PlotCluster;
import com.plotsquared.core.plot.comment.PlotComment;
import com.plotsquared.core.plot.flag.implementations.DescriptionFlag;
import com.plotsquared.core.util.EventDispatcher;
import com.plotsquared.core.util.HashUtil;
import com.plotsquared.core.util.task.RunnableVal;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Characterisation tests for the SQL storage layer.
 *
 * <p>The tests record what the storage layer does today, including known bugs. A test named after a bug
 * asserts the buggy behaviour, so a fix has to flip that assertion on purpose. Each test runs against a fresh
 * database. Subclasses choose the dialect by providing {@link #createDatabase()}.</p>
 *
 * <p>Writes are queued by {@link SQLManager}. {@link #settle()} drives the queue on the test thread until it is
 * empty, so no test sleeps or waits on a background thread.</p>
 */
public abstract class AbstractSqlStorageTest {

    protected static final String WORLD = "world";
    protected static final UUID ALICE = UUID.fromString("00000000-0000-0000-0000-00000000000a");
    protected static final UUID BOB = UUID.fromString("00000000-0000-0000-0000-00000000000b");
    protected static final UUID CAROL = UUID.fromString("00000000-0000-0000-0000-00000000000c");

    private static final int MAX_SETTLE_ROUNDS = 100;

    @TempDir
    protected Path tempDir;

    protected SQLManager manager;
    protected PlotArea area;
    private SqlStorageTestSupport.ManualTaskManager taskManager;
    private Object previousPlotSquared;
    private Object previousFlagContainer;
    private boolean previousWorldEditInitialized;
    private AbstractDB previousDbManager;
    private Database database;
    private Connection verification;

    /**
     * The dialect under test, used where the behaviour differs between databases.
     */
    protected enum Dialect {
        SQLITE,
        MARIADB
    }

    protected abstract Dialect dialect();

    /**
     * Creates an empty database for one test. The returned database is passed to {@link SQLManager}.
     */
    protected abstract Database createDatabase() throws Exception;

    /**
     * Opens a separate connection for reading back what the manager wrote.
     */
    protected abstract Connection openConnection() throws SQLException;

    /**
     * Drops the database created by {@link #createDatabase()}. Runs after the manager is closed.
     */
    protected void dropDatabase() throws Exception {
    }

    @BeforeEach
    void setUp() throws Exception {
        this.previousDbManager = DBFunc.dbManager;
        DBFunc.dbManager = null;
        this.taskManager = SqlStorageTestSupport.installTaskManager();
        this.previousWorldEditInitialized = SqlStorageTestSupport.installWorldEdit();
        this.previousFlagContainer = SqlStorageTestSupport.installGlobalFlagContainer();
        this.previousPlotSquared = SqlStorageTestSupport.installPlotSquared(this.tempDir.resolve("platform").toFile());

        this.area = SqlStorageTestSupport.newPlotArea(WORLD);
        YamlConfiguration worldConfiguration = new YamlConfiguration();
        worldConfiguration.set("worlds." + WORLD + ".generator", "test");
        EventDispatcher eventDispatcher = new EventDispatcher(null);
        PlotListener plotListener = new PlotListener(eventDispatcher);

        this.database = createDatabase();
        this.manager = new SQLManager(this.database, "", eventDispatcher, plotListener, worldConfiguration);
        // The constructor queues the endless background loop. Tests drive the queue themselves instead.
        Assertions.assertEquals(
                1, this.taskManager.asyncTasks().size(),
                "the constructor should queue exactly one asynchronous task, the background loop"
        );
        this.taskManager.asyncTasks().clear();
    }

    @AfterEach
    void tearDown() throws Exception {
        try {
            if (this.verification != null) {
                this.verification.close();
            }
            if (this.manager != null) {
                this.manager.close();
            }
            dropDatabase();
        } finally {
            SqlStorageTestSupport.uninstallPlotSquared(this.previousPlotSquared);
            SqlStorageTestSupport.uninstallTaskManager(this.taskManager);
            SqlStorageTestSupport.uninstallGlobalFlagContainer(this.previousFlagContainer);
            SqlStorageTestSupport.uninstallWorldEdit(this.previousWorldEditInitialized);
            DBFunc.dbManager = this.previousDbManager;
        }
    }

    // ---------------------------------------------------------------------------------------------
    // Plots
    // ---------------------------------------------------------------------------------------------

    @Test
    void createPlotSafeInsertsRowWithOwnerAndWorld() throws SQLException {
        Plot plot = plot(1, 2, ALICE);

        createPlot(plot);

        Assertions.assertEquals(
                1, count("SELECT COUNT(*) FROM plot WHERE plot_id_x = ? AND plot_id_z = ? AND owner = ? AND world = ?",
                        1, 2, ALICE.toString(), this.area.toString()
                ),
                "createPlotSafe should insert exactly one plot row with the owner and world of the plot"
        );
    }

    @Test
    void createPlotSafeRefusesDuplicateCoordinatesAndKeepsFirstOwner() throws SQLException {
        createPlot(plot(1, 2, ALICE));
        AtomicInteger successes = new AtomicInteger();
        AtomicInteger failures = new AtomicInteger();

        this.manager.createPlotSafe(plot(1, 2, BOB), successes::incrementAndGet, failures::incrementAndGet);
        settle();

        Assertions.assertEquals(0, successes.get(), "a duplicate createPlotSafe must not report success");
        Assertions.assertEquals(1, failures.get(), "a duplicate createPlotSafe must call the failure callback once");
        Assertions.assertEquals(
                1, count("SELECT COUNT(*) FROM plot WHERE plot_id_x = 1 AND plot_id_z = 2"),
                "the duplicate must not insert a second row"
        );
        Assertions.assertEquals(
                ALICE.toString(), text("SELECT owner FROM plot WHERE plot_id_x = 1 AND plot_id_z = 2"),
                "the first owner must be kept"
        );
    }

    @Test
    void createPlotSafeCreatesPlotSettingsRowForTheNewPlot() throws SQLException {
        createPlot(plot(1, 2, ALICE));

        Assertions.assertEquals(
                1, count("SELECT COUNT(*) FROM plot_settings s JOIN plot p ON s.plot_plot_id = p.id "
                        + "WHERE p.plot_id_x = 1 AND p.plot_id_z = 2"),
                "creating a plot should also create its settings row"
        );
    }

    @Test
    void setOwnerUpdatesTheOwnerColumn() throws SQLException {
        Plot plot = plot(1, 2, ALICE);
        createPlot(plot);

        this.manager.setOwner(plot, BOB);
        settle();

        Assertions.assertEquals(
                BOB.toString(), text("SELECT owner FROM plot WHERE plot_id_x = 1 AND plot_id_z = 2"),
                "setOwner should replace the owner of the row"
        );
    }

    @Test
    void movePlotWritesNewCoordinatesAndWorldToTheOriginalRow() throws SQLException {
        Plot original = plot(1, 2, ALICE);
        createPlot(original);
        PlotArea otherArea = SqlStorageTestSupport.newPlotArea("other");
        Plot target = new Plot(otherArea, SqlStorageTestSupport.id(5, 6), ALICE);

        this.manager.movePlot(original, target);
        settle();

        Assertions.assertEquals(
                1, count("SELECT COUNT(*) FROM plot WHERE plot_id_x = 5 AND plot_id_z = 6 AND world = ?",
                        otherArea.toString()
                ),
                "movePlot should move the existing row to the new coordinates and world"
        );
        Assertions.assertEquals(
                1, count("SELECT COUNT(*) FROM plot"),
                "movePlot must not insert a second row"
        );
    }

    @Test
    void swapPlotsReturnsTrueAndKeepsRowCoordinatesWhenThePlotObjectsAreNotExchangedInMemory() throws SQLException {
        Plot first = plot(1, 1, ALICE);
        Plot second = plot(2, 2, BOB);
        createPlot(first);
        createPlot(second);

        CompletableFuture<Boolean> swapped = this.manager.swapPlots(first, second);
        settle();

        Assertions.assertTrue(swapped.isDone(), "swapPlots should complete its future on the asynchronous queue");
        Assertions.assertTrue(swapped.join(), "swapPlots should report success");
        Assertions.assertEquals(
                1, count("SELECT COUNT(*) FROM plot WHERE plot_id_x = 1 AND plot_id_z = 1 AND owner = ?",
                        ALICE.toString()
                ),
                "swapPlots writes each plot's own id, so without an in-memory exchange (Plot#swapData does it) "
                        + "the first row keeps its coordinates"
        );
    }

    @Test
    void setMergedStoresTheMergeHashInPlotSettings() throws SQLException {
        Plot plot = plot(1, 2, ALICE);
        createPlot(plot);
        boolean[] merged = {true, false, true, false};

        this.manager.setMerged(plot, merged);
        settle();

        Assertions.assertEquals(
                HashUtil.hash(merged),
                count("SELECT merged FROM plot_settings s JOIN plot p ON s.plot_plot_id = p.id "
                        + "WHERE p.plot_id_x = 1 AND p.plot_id_z = 2"),
                "the merged array should be stored as its hash in plot_settings.merged"
        );
    }

    @Test
    void deleteRemovesPlotRowSettingsAndMemberRows() throws SQLException {
        Plot plot = plot(1, 2, ALICE);
        createPlot(plot);
        plot.getTrusted().add(BOB);
        plot.getDenied().add(CAROL);
        this.manager.setTrusted(plot, BOB);
        this.manager.setDenied(plot, CAROL);
        settle();

        this.manager.delete(plot);
        settle();

        Assertions.assertEquals(0, count("SELECT COUNT(*) FROM plot"), "delete should remove the plot row");
        Assertions.assertEquals(0, count("SELECT COUNT(*) FROM plot_settings"), "delete should remove the settings");
        Assertions.assertEquals(0, count("SELECT COUNT(*) FROM plot_helpers"), "delete should remove helpers");
        Assertions.assertEquals(0, count("SELECT COUNT(*) FROM plot_denied"), "delete should remove denied players");
    }

    @Test
    void deleteLeavesHelperRowsBehindWhenThePlotObjectDoesNotListThemInMemory() throws SQLException {
        Plot plot = plot(1, 2, ALICE);
        createPlot(plot);
        this.manager.setTrusted(plot, BOB);
        settle();

        this.manager.delete(plot);
        settle();

        Assertions.assertEquals(0, count("SELECT COUNT(*) FROM plot"), "delete should remove the plot row");
        Assertions.assertEquals(
                1, count("SELECT COUNT(*) FROM plot_helpers WHERE user_uuid = ?", BOB.toString()),
                "known gap: deleteHelpers returns early unless the in-memory plot lists trusted players, so the "
                        + "helper row is orphaned"
        );
    }

    // ---------------------------------------------------------------------------------------------
    // Members and denied players
    // ---------------------------------------------------------------------------------------------

    @Test
    void setTrustedWritesToThePlotHelpersTable() throws SQLException {
        Plot plot = plot(1, 2, ALICE);
        createPlot(plot);

        this.manager.setTrusted(plot, BOB);
        settle();

        Assertions.assertEquals(
                1, count("SELECT COUNT(*) FROM plot_helpers WHERE user_uuid = ?", BOB.toString()),
                "trusted players are stored in plot_helpers"
        );
        Assertions.assertEquals(
                0, count("SELECT COUNT(*) FROM plot_trusted WHERE user_uuid = ?", BOB.toString()),
                "trusted players must not be stored in plot_trusted"
        );
    }

    @Test
    void setMemberWritesToThePlotTrustedTable() throws SQLException {
        Plot plot = plot(1, 2, ALICE);
        createPlot(plot);

        this.manager.setMember(plot, BOB);
        settle();

        Assertions.assertEquals(
                1, count("SELECT COUNT(*) FROM plot_trusted WHERE user_uuid = ?", BOB.toString()),
                "added members are stored in plot_trusted"
        );
        Assertions.assertEquals(
                0, count("SELECT COUNT(*) FROM plot_helpers WHERE user_uuid = ?", BOB.toString()),
                "added members must not be stored in plot_helpers"
        );
    }

    @Test
    void removeTrustedDeletesTheRowFromThePlotHelpersTable() throws SQLException {
        Plot plot = plot(1, 2, ALICE);
        createPlot(plot);
        this.manager.setTrusted(plot, BOB);
        settle();

        this.manager.removeTrusted(plot, BOB);
        settle();

        Assertions.assertEquals(
                0, count("SELECT COUNT(*) FROM plot_helpers WHERE user_uuid = ?", BOB.toString()),
                "removeTrusted should delete the trusted row"
        );
    }

    @Test
    void removeMemberDeletesTheRowFromThePlotTrustedTable() throws SQLException {
        Plot plot = plot(1, 2, ALICE);
        createPlot(plot);
        this.manager.setMember(plot, BOB);
        settle();

        this.manager.removeMember(plot, BOB);
        settle();

        Assertions.assertEquals(
                0, count("SELECT COUNT(*) FROM plot_trusted WHERE user_uuid = ?", BOB.toString()),
                "removeMember should delete the member row"
        );
    }

    @Test
    void setDeniedWritesToThePlotDeniedTable() throws SQLException {
        Plot plot = plot(1, 2, ALICE);
        createPlot(plot);

        this.manager.setDenied(plot, CAROL);
        settle();

        Assertions.assertEquals(
                1, count("SELECT COUNT(*) FROM plot_denied WHERE user_uuid = ?", CAROL.toString()),
                "denied players should be stored in plot_denied"
        );
    }

    @Test
    void removeDeniedDeletesTheRowFromThePlotDeniedTable() throws SQLException {
        Plot plot = plot(1, 2, ALICE);
        createPlot(plot);
        this.manager.setDenied(plot, CAROL);
        settle();

        this.manager.removeDenied(plot, CAROL);
        settle();

        Assertions.assertEquals(
                0, count("SELECT COUNT(*) FROM plot_denied WHERE user_uuid = ?", CAROL.toString()),
                "removeDenied should delete the denied row"
        );
    }

    @Test
    void setDeniedTwiceStoresTwoRowsBecausePlotDeniedHasNoUniqueKey() throws SQLException {
        Plot plot = plot(1, 2, ALICE);
        createPlot(plot);

        this.manager.setDenied(plot, CAROL);
        this.manager.setDenied(plot, CAROL);
        settle();

        Assertions.assertEquals(
                2, count("SELECT COUNT(*) FROM plot_denied WHERE user_uuid = ?", CAROL.toString()),
                "known gap: denying the same player twice inserts a second row"
        );
    }

    // ---------------------------------------------------------------------------------------------
    // Flags
    // ---------------------------------------------------------------------------------------------

    @Test
    void setFlagInsertsARowInPlotFlags() throws SQLException {
        Plot plot = plot(1, 2, ALICE);
        createPlot(plot);

        this.manager.setFlag(plot, DescriptionFlag.DESCRIPTION_FLAG_EMPTY.parse("hello"));
        settle();

        Assertions.assertEquals(
                "hello", text("SELECT value FROM plot_flags WHERE flag = 'description'"),
                "the flag name and its string value should be stored"
        );
    }

    @Test
    void setFlagTwiceUpdatesTheSameRow() throws SQLException {
        Plot plot = plot(1, 2, ALICE);
        createPlot(plot);

        this.manager.setFlag(plot, DescriptionFlag.DESCRIPTION_FLAG_EMPTY.parse("first"));
        this.manager.setFlag(plot, DescriptionFlag.DESCRIPTION_FLAG_EMPTY.parse("second"));
        settle();

        Assertions.assertEquals(
                1, count("SELECT COUNT(*) FROM plot_flags WHERE flag = 'description'"),
                "the flag table has a unique key on (plot, flag), so the second write is an upsert"
        );
        Assertions.assertEquals(
                "second", text("SELECT value FROM plot_flags WHERE flag = 'description'"),
                "the upsert should keep the latest value"
        );
    }

    @Test
    void removeFlagDeletesTheRowFromPlotFlags() throws SQLException {
        Plot plot = plot(1, 2, ALICE);
        createPlot(plot);
        this.manager.setFlag(plot, DescriptionFlag.DESCRIPTION_FLAG_EMPTY.parse("hello"));
        settle();

        this.manager.removeFlag(plot, DescriptionFlag.DESCRIPTION_FLAG_EMPTY.parse("hello"));
        settle();

        Assertions.assertEquals(
                0, count("SELECT COUNT(*) FROM plot_flags"),
                "removeFlag should delete the row for that flag"
        );
    }

    @Test
    void flagValueOf512CharactersIsStoredOnEveryDialect() throws SQLException {
        Plot plot = plot(1, 2, ALICE);
        createPlot(plot);
        String value = "x".repeat(512);

        this.manager.setFlag(plot, DescriptionFlag.DESCRIPTION_FLAG_EMPTY.parse(value));
        settle();

        Assertions.assertEquals(
                1, count("SELECT COUNT(*) FROM plot_flags WHERE flag = 'description' AND LENGTH(value) = 512"),
                "a value of exactly 512 characters fits the VARCHAR(512) column on every dialect"
        );
    }

    @Test
    void flagValueLongerThan512CharactersFailsOnMariaDb() throws SQLException {
        Plot plot = plot(1, 2, ALICE);
        createPlot(plot);
        String value = "x".repeat(600);

        this.manager.setFlag(plot, DescriptionFlag.DESCRIPTION_FLAG_EMPTY.parse(value));
        settle();

        if (dialect() == Dialect.MARIADB) {
            Assertions.assertEquals(
                    0, count("SELECT COUNT(*) FROM plot_flags WHERE flag = 'description'"),
                    "known bug: plot_flags.value is VARCHAR(512) on MariaDB, so the 600 character value is rejected "
                            + "and the failure is only logged"
            );
        } else {
            Assertions.assertEquals(
                    1, count("SELECT COUNT(*) FROM plot_flags WHERE flag = 'description'"),
                    "SQLite does not enforce the VARCHAR length, so the value is stored"
            );
        }
    }

    // ---------------------------------------------------------------------------------------------
    // Alias and position
    // ---------------------------------------------------------------------------------------------

    @Test
    void setAliasWritesTheAliasToPlotSettings() throws SQLException {
        Plot plot = plot(1, 2, ALICE);
        createPlot(plot);

        this.manager.setAlias(plot, "home");
        settle();

        Assertions.assertEquals(
                "home", text("SELECT s.alias FROM plot_settings s JOIN plot p ON s.plot_plot_id = p.id "
                        + "WHERE p.plot_id_x = 1 AND p.plot_id_z = 2"),
                "the alias should be written to plot_settings.alias"
        );
    }

    @Test
    void setPositionWritesThePositionToPlotSettings() throws SQLException {
        Plot plot = plot(1, 2, ALICE);
        createPlot(plot);

        this.manager.setPosition(plot, "CENTER");
        settle();

        Assertions.assertEquals(
                "CENTER", text("SELECT s.position FROM plot_settings s JOIN plot p ON s.plot_plot_id = p.id "
                        + "WHERE p.plot_id_x = 1 AND p.plot_id_z = 2"),
                "the home position should be written to plot_settings.position"
        );
    }

    // ---------------------------------------------------------------------------------------------
    // Comments
    // ---------------------------------------------------------------------------------------------

    @Test
    void setCommentThenGetCommentsReturnsItFromItsInbox() throws SQLException {
        Plot plot = plot(1, 2, ALICE);
        createPlot(plot);
        this.manager.setComment(plot, comment(plot, "hello", "inbox-a", 1_000_000L));
        settle();
        AtomicReference<List<PlotComment>> delivered = new AtomicReference<>();
        RunnableVal<List<PlotComment>> result = new RunnableVal<>() {
            @Override
            public void run(List<PlotComment> value) {
                delivered.set(value);
            }
        };

        this.manager.getComments(plot, "inbox-a", result);
        settle();

        Assertions.assertNotNull(delivered.get(), "getComments should deliver a list through the callback");
        Assertions.assertEquals(1, delivered.get().size(), "one comment was written to the inbox");
        Assertions.assertEquals("hello", delivered.get().get(0).comment(), "the comment text should round-trip");
        Assertions.assertEquals(
                1_000_000L, delivered.get().get(0).timestamp(),
                "timestamps are stored in whole seconds and read back as milliseconds"
        );
    }

    @Test
    void removeCommentDeletesTheMatchingRow() throws SQLException {
        Plot plot = plot(1, 2, ALICE);
        createPlot(plot);
        PlotComment comment = comment(plot, "hello", "inbox-a", 1_000_000L);
        this.manager.setComment(plot, comment);
        settle();

        this.manager.removeComment(plot, comment);
        settle();

        Assertions.assertEquals(0, count("SELECT COUNT(*) FROM plot_comments"), "removeComment should delete the row");
    }

    @Test
    void clearInboxWithPlotDeletesOnlyThatInboxOfThatPlot() throws SQLException {
        Plot plot = plot(1, 2, ALICE);
        createPlot(plot);
        this.manager.setComment(plot, comment(plot, "one", "inbox-a", 1_000_000L));
        this.manager.setComment(plot, comment(plot, "two", "inbox-b", 1_000_000L));
        settle();

        this.manager.clearInbox(plot, "inbox-a");
        settle();

        Assertions.assertEquals(
                0, count("SELECT COUNT(*) FROM plot_comments WHERE inbox = 'inbox-a'"),
                "the named inbox of the plot should be emptied"
        );
        Assertions.assertEquals(
                1, count("SELECT COUNT(*) FROM plot_comments WHERE inbox = 'inbox-b'"),
                "other inboxes must stay untouched"
        );
    }

    @Test
    void clearInboxWithoutPlotThrowsAndLeavesCommentsUntouched() throws SQLException {
        Plot plot = plot(1, 2, ALICE);
        createPlot(plot);
        this.manager.setComment(plot, comment(plot, "one", "inbox-a", 1_000_000L));
        settle();

        Assertions.assertThrows(
                NullPointerException.class, () -> this.manager.clearInbox(null, "inbox-a"),
                "known bug: clearInbox(null, ...) passes the null plot to addPlotTask, which throws at the call site"
        );
        settle();

        Assertions.assertEquals(
                1, count("SELECT COUNT(*) FROM plot_comments WHERE inbox = 'inbox-a'"),
                "nothing is deleted when clearInbox throws"
        );
    }

    // ---------------------------------------------------------------------------------------------
    // Ratings
    // ---------------------------------------------------------------------------------------------

    @Test
    void setRatingStoresOneRatingPerRater() throws SQLException {
        Plot plot = plot(1, 2, ALICE);
        createPlot(plot);

        this.manager.setRating(plot, BOB, 5);
        settle();

        Map<UUID, Integer> ratings = this.manager.getRatings(plot);
        Assertions.assertEquals(Map.of(BOB, 5), ratings, "getRatings should return the stored rating");
    }

    @Test
    void ratingTheSamePlotTwiceStoresTwoRows() throws SQLException {
        Plot plot = plot(1, 2, ALICE);
        createPlot(plot);

        this.manager.setRating(plot, BOB, 3);
        this.manager.setRating(plot, BOB, 5);
        settle();

        Assertions.assertEquals(
                2, count("SELECT COUNT(*) FROM plot_rating WHERE player = ?", BOB.toString()),
                "known bug: plot_rating has no unique key on (plot, player), so every rating is a new row"
        );
    }

    // ---------------------------------------------------------------------------------------------
    // Clusters
    // ---------------------------------------------------------------------------------------------

    @Test
    void createClusterWritesClusterRowAndSettingsRow() throws SQLException {
        PlotCluster cluster = cluster(0, 0, 1, 1, ALICE);

        this.manager.createCluster(cluster);
        settle();

        Assertions.assertEquals(1, count("SELECT COUNT(*) FROM cluster WHERE owner = ?", ALICE.toString()),
                "createCluster should insert the cluster row");
        Assertions.assertEquals(1, count("SELECT COUNT(*) FROM cluster_settings"),
                "createCluster should insert the cluster settings row");
        Assertions.assertEquals(1, this.manager.getClusters().get(WORLD).size(),
                "getClusters should load the new cluster for its world");
    }

    @Test
    void resizeClusterKeepsThePreviousCornersInTheDatabase() throws SQLException {
        PlotCluster cluster = cluster(0, 0, 1, 1, ALICE);
        this.manager.createCluster(cluster);
        settle();

        this.manager.resizeCluster(cluster, SqlStorageTestSupport.id(0, 0), SqlStorageTestSupport.id(3, 4));
        settle();

        Assertions.assertEquals(
                1, count("SELECT COUNT(*) FROM cluster WHERE pos2_x = 1 AND pos2_z = 1"),
                "known bug: resizeCluster captures the corners before it updates the object and writes those old "
                        + "corners, so the database keeps (1,1)"
        );
        Assertions.assertEquals(
                0, count("SELECT COUNT(*) FROM cluster WHERE pos2_x = 3 AND pos2_z = 4"),
                "the new corner (3,4) is not written"
        );
    }

    @Test
    void setClusterNameWritesTheAliasToClusterSettings() throws SQLException {
        PlotCluster cluster = cluster(0, 0, 1, 1, ALICE);
        this.manager.createCluster(cluster);
        settle();

        this.manager.setClusterName(cluster, "spawn");
        settle();

        Assertions.assertEquals(
                "spawn", text("SELECT alias FROM cluster_settings"),
                "the cluster name is stored as the alias in cluster_settings"
        );
    }

    @Test
    void setHelperWritesToClusterHelpersAndRemoveHelperDeletesIt() throws SQLException {
        PlotCluster cluster = cluster(0, 0, 1, 1, ALICE);
        this.manager.createCluster(cluster);
        settle();

        this.manager.setHelper(cluster, BOB);
        settle();
        Assertions.assertEquals(1, count("SELECT COUNT(*) FROM cluster_helpers WHERE user_uuid = ?", BOB.toString()),
                "setHelper should insert into cluster_helpers");

        this.manager.removeHelper(cluster, BOB);
        settle();
        Assertions.assertEquals(0, count("SELECT COUNT(*) FROM cluster_helpers"),
                "removeHelper should delete from cluster_helpers");
    }

    @Test
    void setInvitedWritesToClusterInvitedAndRemoveInvitedDeletesIt() throws SQLException {
        PlotCluster cluster = cluster(0, 0, 1, 1, ALICE);
        this.manager.createCluster(cluster);
        settle();

        this.manager.setInvited(cluster, CAROL);
        settle();
        Assertions.assertEquals(1, count("SELECT COUNT(*) FROM cluster_invited WHERE user_uuid = ?", CAROL.toString()),
                "setInvited should insert into cluster_invited");

        this.manager.removeInvited(cluster, CAROL);
        settle();
        Assertions.assertEquals(0, count("SELECT COUNT(*) FROM cluster_invited"),
                "removeInvited should delete from cluster_invited");
    }

    @Test
    void deleteClusterRemovesTheClusterAndItsSubTableRows() throws SQLException {
        PlotCluster cluster = cluster(0, 0, 1, 1, ALICE);
        this.manager.createCluster(cluster);
        settle();
        this.manager.setHelper(cluster, BOB);
        this.manager.setInvited(cluster, CAROL);
        settle();

        this.manager.delete(cluster);
        settle();

        Assertions.assertEquals(0, count("SELECT COUNT(*) FROM cluster"), "delete should remove the cluster row");
        Assertions.assertEquals(0, count("SELECT COUNT(*) FROM cluster_settings"), "delete should remove its settings");
        Assertions.assertEquals(0, count("SELECT COUNT(*) FROM cluster_helpers"), "delete should remove its helpers");
        Assertions.assertEquals(0, count("SELECT COUNT(*) FROM cluster_invited"), "delete should remove its invited");
    }

    // ---------------------------------------------------------------------------------------------
    // Player meta
    // ---------------------------------------------------------------------------------------------

    @Test
    void persistentMetaWrittenWithoutReplaceCanBeReadBack() {
        this.manager.addPersistentMeta(ALICE, "key", bytes("first"), false);
        settle();

        Assertions.assertEquals(
                "first", readMeta(ALICE, "key"),
                "getPersistentMeta should return the stored bytes for the key"
        );
    }

    @Test
    void persistentMetaWithReplaceUpdatesTheExistingRow() throws SQLException {
        this.manager.addPersistentMeta(ALICE, "key", bytes("first"), false);
        settle();

        this.manager.addPersistentMeta(ALICE, "key", bytes("second"), true);
        settle();

        Assertions.assertEquals(
                1, count("SELECT COUNT(*) FROM player_meta WHERE uuid = ? AND `key` = ?", ALICE.toString(), "key"),
                "replace should keep a single row for the key"
        );
        Assertions.assertEquals("second", readMeta(ALICE, "key"), "replace should overwrite the value");
    }

    @Test
    void persistentMetaWithReplaceAndNoExistingRowStoresNothing() throws SQLException {
        this.manager.addPersistentMeta(ALICE, "key", bytes("first"), true);
        settle();

        Assertions.assertEquals(
                0, count("SELECT COUNT(*) FROM player_meta"),
                "replace is an UPDATE, so without an existing row the value is silently dropped"
        );
    }

    @Test
    void persistentMetaIsInsertedAgainOnEveryWriteWithoutReplace() throws SQLException {
        this.manager.addPersistentMeta(ALICE, "key", bytes("first"), false);
        this.manager.addPersistentMeta(ALICE, "key", bytes("second"), false);
        settle();

        Assertions.assertEquals(
                2, count("SELECT COUNT(*) FROM player_meta WHERE uuid = ? AND `key` = ?", ALICE.toString(), "key"),
                "known bug (#3274): without replace every write inserts another row for the same key"
        );
    }

    @Test
    void removePersistentMetaDeletesEveryDuplicateRowOfTheKey() throws SQLException {
        this.manager.addPersistentMeta(ALICE, "key", bytes("first"), false);
        this.manager.addPersistentMeta(ALICE, "key", bytes("second"), false);
        settle();

        this.manager.removePersistentMeta(ALICE, "key");
        settle();

        Assertions.assertEquals(
                0, count("SELECT COUNT(*) FROM player_meta WHERE uuid = ? AND `key` = ?", ALICE.toString(), "key"),
                "removePersistentMeta should delete all rows for that uuid and key"
        );
    }

    // ---------------------------------------------------------------------------------------------
    // Schema
    // ---------------------------------------------------------------------------------------------

    @Test
    void createTablesCreatesTheMainAndClusterTablesOnAFreshDatabase() throws SQLException {
        for (String table : List.of(
                "plot", "plot_denied", "plot_helpers", "plot_comments", "plot_trusted", "plot_rating",
                "plot_settings", "cluster", "player_meta", "plot_flags",
                "cluster_helpers", "cluster_invited", "cluster_settings"
        )) {
            Assertions.assertTrue(tableExists(table), "a fresh database should contain the table " + table);
        }
    }

    @Test
    void createTablesSkipsClusterSubTablesWhenMainTablesExist() throws SQLException {
        execute("DROP TABLE cluster_helpers");
        execute("DROP TABLE cluster_invited");
        execute("DROP TABLE cluster_settings");

        this.manager.createTables();

        Assertions.assertFalse(
                tableExists("cluster_helpers"),
                "known gap: createTables returns early when all ten main tables exist, so cluster_helpers is not "
                        + "recreated"
        );
        Assertions.assertFalse(tableExists("cluster_invited"), "known gap: cluster_invited is not recreated");
        Assertions.assertFalse(tableExists("cluster_settings"), "known gap: cluster_settings is not recreated");
    }

    // ---------------------------------------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------------------------------------

    protected Plot plot(int x, int z, UUID owner) {
        return new Plot(this.area, SqlStorageTestSupport.id(x, z), owner);
    }

    protected PlotCluster cluster(int x1, int z1, int x2, int z2, UUID owner) {
        return new PlotCluster(
                this.area, SqlStorageTestSupport.id(x1, z1), SqlStorageTestSupport.id(x2, z2), owner);
    }

    private PlotComment comment(Plot plot, String text, String inbox, long timestamp) {
        return new PlotComment(plot.getWorldName(), plot.getId(), text, "sender", inbox, timestamp);
    }

    private void createPlot(Plot plot) {
        AtomicInteger successes = new AtomicInteger();
        AtomicInteger failures = new AtomicInteger();
        this.manager.createPlotSafe(plot, successes::incrementAndGet, failures::incrementAndGet);
        settle();
        Assertions.assertEquals(1, successes.get(), "the test plot should be created");
        Assertions.assertEquals(0, failures.get(), "the test plot should not be refused");
    }

    /**
     * Runs queued writes and queued callbacks on the calling thread until nothing is left. Bounded, so a
     * callback that keeps queuing work fails the test instead of hanging it.
     */
    protected void settle() {
        for (int round = 0; round < MAX_SETTLE_ROUNDS; round++) {
            boolean progressed = false;
            while (this.manager.sendBatch()) {
                progressed = true;
            }
            Runnable notify;
            while ((notify = this.manager.getNotifyTasks().poll()) != null) {
                notify.run();
                progressed = true;
            }
            Runnable async;
            while ((async = this.taskManager.asyncTasks().poll()) != null) {
                async.run();
                progressed = true;
            }
            if (!progressed) {
                return;
            }
        }
        Assertions.fail("queued storage work did not settle within " + MAX_SETTLE_ROUNDS + " rounds");
    }

    private String readMeta(UUID uuid, String key) {
        AtomicReference<Map<String, byte[]>> delivered = new AtomicReference<>();
        RunnableVal<Map<String, byte[]>> result = new RunnableVal<>() {
            @Override
            public void run(Map<String, byte[]> value) {
                delivered.set(value);
            }
        };
        this.manager.getPersistentMeta(uuid, result);
        settle();
        Assertions.assertNotNull(delivered.get(), "getPersistentMeta should deliver a map through the callback");
        byte[] value = delivered.get().get(key);
        Assertions.assertNotNull(value, "no meta stored for key " + key);
        return new String(value, StandardCharsets.UTF_8);
    }

    private static byte[] bytes(String value) {
        return value.getBytes(StandardCharsets.UTF_8);
    }

    private boolean tableExists(String table) throws SQLException {
        String sql = dialect() == Dialect.SQLITE
                ? "SELECT COUNT(*) FROM sqlite_master WHERE type = 'table' AND name = ?"
                : "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = ?";
        return count(sql, table) == 1;
    }

    /**
     * Returns the first column of the first row as a long. Used for counts and for single numeric values.
     */
    protected long count(String sql, Object... parameters) throws SQLException {
        try (PreparedStatement statement = prepare(sql, parameters);
             ResultSet resultSet = statement.executeQuery()) {
            Assertions.assertTrue(resultSet.next(), "count query returned no row: " + sql);
            return resultSet.getLong(1);
        }
    }

    protected String text(String sql, Object... parameters) throws SQLException {
        try (PreparedStatement statement = prepare(sql, parameters);
             ResultSet resultSet = statement.executeQuery()) {
            if (!resultSet.next()) {
                return null;
            }
            return resultSet.getString(1);
        }
    }

    private void execute(String sql) throws SQLException {
        try (PreparedStatement statement = prepare(sql)) {
            statement.execute();
        }
    }

    private PreparedStatement prepare(String sql, Object... parameters) throws SQLException {
        if (this.verification == null) {
            this.verification = openConnection();
        }
        PreparedStatement statement = this.verification.prepareStatement(sql);
        for (int i = 0; i < parameters.length; i++) {
            statement.setObject(i + 1, parameters[i]);
        }
        return statement;
    }

}
