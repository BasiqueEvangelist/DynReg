package me.basiqueevangelist.dynreg.data;

import com.google.gson.JsonObject;
import me.basiqueevangelist.dynreg.DynReg;
import me.basiqueevangelist.dynreg.entry.EntryDescription;
import me.basiqueevangelist.dynreg.entry.json.EntryDescriptionReaders;
import me.basiqueevangelist.dynreg.round.DynamicRound;
import me.basiqueevangelist.dynreg.util.RegistryUtils;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.resource.SimpleResourceReloadListener;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class RegistryEntryLoader implements SimpleResourceReloadListener<Map<RegistryKey<?>, EntryDescription<?>>> {
    private static final Logger LOGGER = LoggerFactory.getLogger("DynReg/RegistryEntryLoader");
    private static final HashSet<RegistryKey<?>> ADDED_RESOURCES = new HashSet<>();

    public static final RegistryEntryLoader INSTANCE = new RegistryEntryLoader();

    static {
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> ADDED_RESOURCES.clear());
    }

    @Override
    public CompletableFuture<Map<RegistryKey<?>, EntryDescription<?>>> load(ResourceManager manager, Profiler profiler, Executor executor) {
        Map<RegistryKey<?>, EntryDescription<?>> descriptions = new TreeMap<>((a, b) -> {
            if (a.method_41185().equals(b.method_41185()))
                return a.getValue().compareTo(b.getValue());

            return Integer.compare(RegistryUtils.getRawIdOfRegistryOf(a), RegistryUtils.getRawIdOfRegistryOf(b));
        });

        for (Registry<?> registry : Registry.REGISTRIES) {
            var startPath = "entries/" + registry.getKey().getValue().getPath();
            var startLen = startPath.length() + 1;
            var resources = manager.findResources(startPath, path -> path.endsWith(".json"));

            for (Identifier resourceId : resources) {
                var realId = new Identifier(resourceId.getNamespace(), resourceId.getPath().substring(startLen, resourceId.getPath().length() - 5));

                try (Resource resource = manager.getResource(resourceId);
                     var br = new BufferedReader(new InputStreamReader(resource.getInputStream()))) {
                    JsonObject obj = JsonHelper.deserialize(br, true);
                    Identifier type = new Identifier(JsonHelper.getString(obj, "type"));
                    EntryDescription<?> desc = EntryDescriptionReaders.getReader(type).apply(obj);

                    descriptions.put(RegistryKey.of(desc.registry().getKey(), realId), desc);
                } catch (IOException e) {
                    LOGGER.error("Encountered error while loading {} of {}", resourceId, registry.getKey().getValue(), e);
                }
            }
        }

        return CompletableFuture.completedFuture(descriptions);
    }

    @Override
    public CompletableFuture<Void> apply(Map<RegistryKey<?>, EntryDescription<?>> data, ResourceManager manager, Profiler profiler, Executor executor) {
        DynamicRound round = DynamicRound.getRound(DynReg.SERVER);

        round.addTask(ctx -> {
            for (var key : ADDED_RESOURCES) {
                ctx.removeEntry(key);
            }
            ADDED_RESOURCES.clear();

            for (var entry : data.entrySet()) {
                var id = entry.getKey().getValue();
                var desc = entry.getValue();

                ctx.register(id, desc);
                ADDED_RESOURCES.add(entry.getKey());
            }
        });

        round.noDataPackReload();

        round.run();

        return CompletableFuture.completedFuture(null);
    }

    @Override
    public Identifier getFabricId() {
        return DynReg.id("registry_entry");
    }
}
