package me.basiqueevangelist.dynreg.util;

import java.util.concurrent.atomic.AtomicInteger;

public class VersionTracker {
    private final AtomicInteger version = new AtomicInteger();

    public final int getVersion() {
        return version.get();
    }

    public final void bumpVersion() {
        version.incrementAndGet();
    }

    public ThreadLocalChecker threadLocalChecker() {
        return new ThreadLocalChecker();
    }

    public class ThreadLocalChecker {
        private final ThreadLocal<Integer> threadLocal = ThreadLocal.withInitial(version::get);

        private ThreadLocalChecker() {
        }

        public final boolean isUpdateNeeded() {
            int currentVersion = version.get();

            if (threadLocal.get() != currentVersion) {
                threadLocal.set(currentVersion);
                return true;
            } else {
                return false;
            }
        }
    }
}
