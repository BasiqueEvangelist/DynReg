package me.basiqueevangelist.dynreg.impl.util;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

public class ExecutorFreezer {
    private ExecutorFreezer() {

    }

    /**
     * Freezes a (single-threaded) executor until some action is finished.
     *
     * @implNote The calling thread will be recorded and used in the timeout exception
     * @param executor the executor to freeze
     * @return a future that will return the executor unfreezer when the executor is frozen.
     */
    public static CompletableFuture<InfallibleCloseable> freeze(Executor executor) {
        //noinspection resource
        var closeable = new InfallibleCloseable() {
            private boolean closed;

            @Override
            public void close() {
                synchronized (this) {
                    closed = true;
                    this.notifyAll();
                }
            }
        };
        var cf = new CompletableFuture<InfallibleCloseable>();
        var source = Thread.currentThread();

        executor.execute(() -> {
            cf.complete(closeable);

            synchronized (closeable) {
                if (!closeable.closed) {
                    try {
                        closeable.wait(TimeUnit.SECONDS.toMillis(5));
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }

                    if (!closeable.closed) {
                        var sourcePos = new Throwable(source.getName() + " is currently here");
                        sourcePos.setStackTrace(source.getStackTrace());

                        throw new IllegalStateException("Executor freeze timed out", sourcePos);
                    }
                }
            }
        });

        return cf;
    }
}
