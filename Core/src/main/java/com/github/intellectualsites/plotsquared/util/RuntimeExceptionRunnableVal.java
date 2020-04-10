package com.github.intellectualsites.plotsquared.util;

import com.github.intellectualsites.plotsquared.util.tasks.RunnableVal;
import lombok.RequiredArgsConstructor;

import java.util.concurrent.atomic.AtomicBoolean;

@RequiredArgsConstructor public class RuntimeExceptionRunnableVal<T> extends RunnableVal<RuntimeException> {

    private final RunnableVal<T> function;
    private final AtomicBoolean running;

    @Override public void run(RuntimeException value) {
        try {
            function.run();
        } catch (RuntimeException e) {
            this.value = e;
        } catch (Throwable neverHappens) {
            neverHappens.printStackTrace();
        } finally {
            running.set(false);
        }
        synchronized (function) {
            function.notifyAll();
        }
    }

}
