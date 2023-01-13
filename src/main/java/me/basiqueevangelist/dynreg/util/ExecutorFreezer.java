package me.basiqueevangelist.dynreg.util;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class ExecutorFreezer {
    private ExecutorFreezer() {

    }

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

        executor.execute(() -> {
            cf.complete(closeable);

            synchronized (closeable) {
                if (!closeable.closed) {
                    try {
                        closeable.wait();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });

        return cf;
    }
}
