package com.plotsquared.util.tasks;

import lombok.RequiredArgsConstructor;

import java.util.Iterator;

@RequiredArgsConstructor public class ObjectTaskRunnable<T> implements Runnable {

    private final Iterator<T> iterator;
    private final RunnableVal<T> task;
    private final Runnable whenDone;

    @Override public void run() {
        long start = System.currentTimeMillis();
        boolean hasNext;
        while ((hasNext = iterator.hasNext()) && System.currentTimeMillis() - start < 5) {
            task.value = iterator.next();
            task.run();
        }
        if (!hasNext) {
            TaskManager.runTaskLater(whenDone, 1);
        } else {
            TaskManager.runTaskLater(this, 1);
        }
    }

}
