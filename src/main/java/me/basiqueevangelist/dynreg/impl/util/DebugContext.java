package me.basiqueevangelist.dynreg.impl.util;

import me.basiqueevangelist.dynreg.impl.DynReg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public final class DebugContext {
    private static final Logger LOGGER = LoggerFactory.getLogger("DynReg/DebugContext");
    private static final Map<String, Supplier<Object>> KEYS = new HashMap<>();

    private DebugContext() {

    }

    public static void addSupplied(String key, Supplier<Object> supplier) {
        if (!DynReg.DEBUG) return;

        KEYS.put(key, supplier);
    }

    public static void addData(String key, Object data) {
        if (!DynReg.DEBUG) return;

        KEYS.put(key, () -> data);
    }

    public static void removeData(String key) {
        if (!DynReg.DEBUG) return;
      
        KEYS.remove(key);
    }

    public static void dump() {
        if (!DynReg.DEBUG) return;

        LOGGER.info("Dumping debug context");

        for (var entry : KEYS.entrySet()) {
            LOGGER.info(" - {} = {}", entry.getKey(), entry.getValue().get());
        }
    }
}
