package me.basiqueevangelist.dynreg.compat.quilt;

import me.basiqueevangelist.dynreg.DynReg;
import me.basiqueevangelist.dynreg.data.RegistryEntryLoader;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceReloader;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;
import org.jetbrains.annotations.NotNull;
import org.quiltmc.qsl.resource.loader.api.ResourceLoader;
import org.quiltmc.qsl.resource.loader.api.reloader.IdentifiableResourceReloader;
import org.quiltmc.qsl.resource.loader.api.reloader.ResourceReloaderKeys;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class QuiltResourceLoaderCompat {
    public static final Identifier ENTRY_LOADER_ID = DynReg.id("entry_loader");

    public static void init() {
        var helper = ResourceLoader.get(ResourceType.SERVER_DATA);

        helper.addReloaderOrdering(ENTRY_LOADER_ID, ResourceReloaderKeys.BEFORE_VANILLA);
        helper.registerReloader(new Reloader());
    }

    public static class Reloader implements IdentifiableResourceReloader, ResourceReloader {
        @Override
        public @NotNull Identifier getQuiltId() {
            return ENTRY_LOADER_ID;
        }

        @Override
        public CompletableFuture<Void> reload(Synchronizer synchronizer, ResourceManager manager, Profiler prepareProfiler, Profiler applyProfiler, Executor prepareExecutor, Executor applyExecutor) {
            return RegistryEntryLoader.INSTANCE.reload(synchronizer, manager, prepareProfiler, applyProfiler, prepareExecutor, applyExecutor);
        }
    }
}
