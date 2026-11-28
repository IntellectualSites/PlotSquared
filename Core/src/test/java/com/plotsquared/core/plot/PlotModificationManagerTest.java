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
package com.plotsquared.core.plot;

import com.plotsquared.core.permissions.Permission;
import com.plotsquared.core.permissions.PermissionHolder;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Covers the time between a merge marking the roads as part of the plots and the queue replacing them: the plots count
 * as running for exactly that time, and edits on a running plot are refused.
 */
public class PlotModificationManagerTest {

    @Test
    public void chainedCompleteTaskRunsExistingTaskBeforeTheAddedOne() {
        List<String> calls = new ArrayList<>();
        PlotModificationManager.chainCompleteTask(() -> calls.add("existing"), () -> calls.add("added")).run();
        Assertions.assertEquals(List.of("existing", "added"), calls, "both tasks must run, the existing one first");
    }

    @Test
    public void chainedCompleteTaskRunsAddedTaskWithoutExistingTask() {
        List<String> calls = new ArrayList<>();
        PlotModificationManager.chainCompleteTask(null, () -> calls.add("added")).run();
        Assertions.assertEquals(List.of("added"), calls, "the added task must run when no task was set before");
    }

    @Test
    public void chainedCompleteTaskRunsAddedTaskWhenExistingTaskThrows() {
        List<String> calls = new ArrayList<>();
        Runnable chained = PlotModificationManager.chainCompleteTask(() -> {
            throw new IllegalStateException("existing task failed");
        }, () -> calls.add("added"));
        Assertions.assertThrows(IllegalStateException.class, chained::run, "the failure of the existing task must stay visible");
        Assertions.assertEquals(List.of("added"), calls, "the added task must run although the existing one failed");
    }

    @Test
    public void plotsAreRunningAsSoonAsTheyAreMarked() {
        FakeMerge merge = new FakeMerge(2);
        merge.mark();
        Assertions.assertEquals(List.of(1, 1), merge.running(), "every plot of a merge must count as running once it is marked");
    }

    @Test
    public void plotsStayRunningWhileTheQueueIsEnqueuedButNotFinished() {
        FakeMerge merge = new FakeMerge(2);
        merge.enqueue(true);
        Assertions.assertEquals(
                List.of(1, 1),
                merge.running(),
                "plots must stay running until the queue has replaced the roads"
        );
    }

    @Test
    public void plotsAreReleasedWhenTheQueueFinishes() {
        FakeMerge merge = new FakeMerge(2);
        merge.enqueue(true);
        merge.finishQueue();
        Assertions.assertEquals(List.of(0, 0), merge.running(), "plots must not be running any more once the queue has finished");
    }

    @Test
    public void plotsAreReleasedWhenTheCompletionTaskOfTheQueueFails() {
        FakeMerge merge = new FakeMerge(2);
        merge.existingCompleteTask = () -> {
            throw new IllegalStateException("completion task failed");
        };
        merge.enqueue(true);
        Assertions.assertThrows(IllegalStateException.class, merge::finishQueue, "the failure must stay visible");
        Assertions.assertEquals(
                List.of(0, 0),
                merge.running(),
                "plots must not stay running when the completion task of the queue fails"
        );
    }

    @Test
    public void plotsAreReleasedWhenTheQueueDoesNotStart() {
        FakeMerge merge = new FakeMerge(2);
        boolean enqueued = merge.enqueue(false);
        Assertions.assertFalse(enqueued, "a queue that did not start must be reported as not enqueued");
        Assertions.assertEquals(List.of(0, 0), merge.running(), "plots must not stay running when the queue did not start");
    }

    @Test
    public void plotsAreReleasedWhenStartingTheQueueThrows() {
        FakeMerge merge = new FakeMerge(2);
        merge.mark();
        Assertions.assertThrows(
                IllegalStateException.class,
                () -> PlotModificationManager.enqueueOrRelease(() -> {
                    throw new IllegalStateException("queue failed to start");
                }, merge.release),
                "the failure must stay visible"
        );
        Assertions.assertEquals(List.of(0, 0), merge.running(), "plots must not stay running when starting the queue throws");
    }

    @Test
    public void plotsAreReleasedOnlyOnceWhenTheQueueFinishesTwice() {
        FakeMerge merge = new FakeMerge(1);
        // an operation that was already running on the plot before the merge started
        merge.plots.get(0).incrementAndGet();
        merge.enqueue(true);
        merge.finishQueue();
        merge.finishQueue();
        Assertions.assertEquals(
                List.of(1),
                merge.running(),
                "finishing twice must not release the mark of another operation running on the plot"
        );
    }

    @Test
    public void plotsAddedToTheCollectionAfterMarkingAreNotReleased() {
        List<AtomicInteger> plots = new ArrayList<>(List.of(new AtomicInteger()));
        Runnable release = PlotModificationManager.markUntilReleased(
                plots,
                AtomicInteger::incrementAndGet,
                AtomicInteger::decrementAndGet
        );
        AtomicInteger later = new AtomicInteger();
        plots.add(later);
        release.run();
        Assertions.assertEquals(0, later.get(), "a plot that was never marked must not be released");
    }

    @Test
    public void editOnRunningPlotIsBlocked() {
        Assertions.assertTrue(
                PlotModificationManager.isEditBlocked(1, new FakePermissionHolder(), Permission.PERMISSION_ADMIN_DESTROY_OTHER),
                "breaking a block on a plot with a running operation must be refused"
        );
    }

    @Test
    public void editOnPlotWithSeveralRunningOperationsIsBlocked() {
        Assertions.assertTrue(
                PlotModificationManager.isEditBlocked(3, new FakePermissionHolder(), Permission.PERMISSION_ADMIN_BUILD_OTHER),
                "placing a block on a plot with several running operations must be refused"
        );
    }

    @Test
    public void editOnIdlePlotIsNotBlocked() {
        Assertions.assertFalse(
                PlotModificationManager.isEditBlocked(0, new FakePermissionHolder(), Permission.PERMISSION_ADMIN_DESTROY_OTHER),
                "a plot without a running operation must be editable as before"
        );
    }

    @Test
    public void editOnRunningPlotIsAllowedWithBypassPermission() {
        Assertions.assertFalse(
                PlotModificationManager.isEditBlocked(
                        1,
                        new FakePermissionHolder(Permission.PERMISSION_ADMIN_DESTROY_OTHER.toString()),
                        Permission.PERMISSION_ADMIN_DESTROY_OTHER
                ),
                "a player with the bypass permission must be able to edit a running plot"
        );
    }

    @Test
    public void bypassPermissionForBuildingDoesNotAllowBreaking() {
        Assertions.assertTrue(
                PlotModificationManager.isEditBlocked(
                        1,
                        new FakePermissionHolder(Permission.PERMISSION_ADMIN_BUILD_OTHER.toString()),
                        Permission.PERMISSION_ADMIN_DESTROY_OTHER
                ),
                "the bypass permission for building must not allow breaking on a running plot"
        );
    }

    @Test
    public void explosionOrPistonOnRunningPlotIsBlocked() {
        Assertions.assertTrue(
                PlotModificationManager.isEditBlocked(1),
                "an explosion or a piston must not change blocks on a plot with a running operation"
        );
    }

    @Test
    public void explosionOrPistonOnPlotWithSeveralRunningOperationsIsBlocked() {
        Assertions.assertTrue(
                PlotModificationManager.isEditBlocked(2),
                "an explosion or a piston must not change blocks on a plot with several running operations"
        );
    }

    @Test
    public void explosionOrPistonOnIdlePlotIsNotBlocked() {
        Assertions.assertFalse(
                PlotModificationManager.isEditBlocked(0),
                "explosions and pistons must work as before on a plot without a running operation"
        );
    }

    /**
     * Stands in for the plots of a merge and its queue. A plot is an operation counter here, as on the real plot.
     */
    private static final class FakeMerge {

        private final List<AtomicInteger> plots = new ArrayList<>();
        private @Nullable Runnable existingCompleteTask;
        private Runnable release;
        private Runnable completeTask;

        private FakeMerge(final int plotCount) {
            for (int i = 0; i < plotCount; i++) {
                this.plots.add(new AtomicInteger());
            }
        }

        private void mark() {
            this.release = PlotModificationManager.markUntilReleased(
                    this.plots,
                    AtomicInteger::incrementAndGet,
                    AtomicInteger::decrementAndGet
            );
        }

        private boolean enqueue(final boolean queueStarts) {
            mark();
            this.completeTask = PlotModificationManager.chainCompleteTask(this.existingCompleteTask, this.release);
            return PlotModificationManager.enqueueOrRelease(() -> queueStarts, this.release);
        }

        private void finishQueue() {
            this.completeTask.run();
        }

        private List<Integer> running() {
            return this.plots.stream().map(AtomicInteger::get).toList();
        }

    }

    private static final class FakePermissionHolder implements PermissionHolder {

        private final Set<String> permissions;

        private FakePermissionHolder(final String... permissions) {
            this.permissions = Set.of(permissions);
        }

        @Override
        public boolean hasPermission(final @NonNull String permission, final boolean notify) {
            return this.permissions.contains(permission);
        }

        @Override
        public boolean hasPermission(
                final @Nullable String world,
                final @NonNull String permission
        ) {
            return this.permissions.contains(permission);
        }

        @Override
        public boolean hasKeyedPermission(
                final @Nullable String world,
                final @NonNull String permission,
                final @NonNull String key
        ) {
            return false;
        }

    }

}
