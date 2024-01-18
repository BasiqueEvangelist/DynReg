package me.basiqueevangelist.dynreg.impl.data;

import com.google.gson.JsonObject;
import me.basiqueevangelist.dynreg.api.entry.RegistrationEntry;
import me.basiqueevangelist.dynreg.api.event.StaticDataLoadCallback;
import me.basiqueevangelist.dynreg.api.round.ModificationRound;
import me.basiqueevangelist.dynreg.impl.DynReg;
import me.basiqueevangelist.dynreg.impl.entry.RegistrationEntriesImpl;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.profiler.Profiler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class RegistryEntryLoader implements IdentifiableResourceReloadListener {
    private static final Logger LOGGER = LoggerFactory.getLogger("DynReg/RegistryEntryLoader");
    static final HashSet<Identifier> ADDED_ENTRIES = new HashSet<>();
    static final HashSet<Identifier> STARTUP_ENTRIES = new HashSet<>();

    public static final RegistryEntryLoader INSTANCE = new RegistryEntryLoader();

    public static void init(){
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
            ADDED_ENTRIES.clear();
            ADDED_ENTRIES.addAll(STARTUP_ENTRIES);
        });

        StaticDataLoadCallback.EVENT.register((manager, round) -> {
            var entries = loadAll(manager);

            for (var entry : entries.values()) {
                round.entry(entry);
                RegistryEntryLoader.ADDED_ENTRIES.add(entry.id());
                RegistryEntryLoader.STARTUP_ENTRIES.add(entry.id());
            }
        });
    }

    public static Map<Identifier, RegistrationEntry> loadAll(ResourceManager manager) {
        Map<Identifier, RegistrationEntry> descriptions = new HashMap<>();

        var resources = manager.findResources("entries", id -> id.getPath().endsWith(".json"));

        for (Map.Entry<Identifier, Resource> entry : resources.entrySet()) {
            Identifier id = entry.getKey();
            var realId = new Identifier(id.getNamespace(), id.getPath().substring("entries".length() + 1, id.getPath().length() - 5));

            try (var br = new BufferedReader(new InputStreamReader(entry.getValue().getInputStream()))) {
                JsonObject obj = JsonHelper.deserialize(br, true);
                Identifier type = new Identifier(JsonHelper.getString(obj, "type"));
                var reader = RegistrationEntriesImpl.getReader(type);

                if (reader == null) {
                    throw new IllegalStateException(type + " is an unknown entry reader type.");
                }

                RegistrationEntry desc = reader.apply(realId, obj);

                descriptions.put(realId, desc);
            } catch (Exception e) {
                LOGGER.error("Encountered error while loading {}", id, e);
            }
        }

        LOGGER.info("Loaded {} registry entries", descriptions.size());

        return descriptions;
    }

    @Override
    public CompletableFuture<Void> reload(Synchronizer synchronizer, ResourceManager manager, Profiler prepareProfiler, Profiler applyProfiler, Executor prepareExecutor, Executor applyExecutor) {
        var data = loadAll(manager);

        ModificationRound round = ModificationRound.create(DynReg.SERVER);

        for (var key : ADDED_ENTRIES) {
            round.removeEntry(key);
        }
        ADDED_ENTRIES.clear();

        for (var entry : data.entrySet()) {
            round.entry(entry.getValue());
            ADDED_ENTRIES.add(entry.getKey());
        }

        round
            .reloadResourcePacks()
            .run();

        return synchronizer.whenPrepared(null);
    }

    @Override
    public Identifier getFabricId() {
        return DynReg.id("registry_entry");
    }
}
