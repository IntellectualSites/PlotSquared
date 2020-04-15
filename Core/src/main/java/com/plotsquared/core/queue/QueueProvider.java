package com.plotsquared.core.queue;

public abstract class QueueProvider {
    public static QueueProvider of(final Class<? extends LocalBlockQueue> primary,
        final Class<? extends LocalBlockQueue> fallback) {
        return new QueueProvider() {

            private boolean failed = false;

            @Override public LocalBlockQueue getNewQueue(String world) {
                if (!failed) {
                    try {
                        return (LocalBlockQueue) primary.getConstructors()[0].newInstance(world);
                    } catch (Throwable e) {
                        e.printStackTrace();
                        failed = true;
                    }
                }
                try {
                    return (LocalBlockQueue) fallback.getConstructors()[0].newInstance(world);
                } catch (Throwable e) {
                    e.printStackTrace();
                }
                return null;
            }
        };
    }

    public abstract LocalBlockQueue getNewQueue(String world);
}
