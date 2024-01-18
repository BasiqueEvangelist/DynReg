package me.basiqueevangelist.dynreg.impl.data;

import me.basiqueevangelist.dynreg.api.event.StaticDataLoadCallback;
import me.basiqueevangelist.dynreg.impl.round.ModificationRoundImpl;
import net.minecraft.resource.*;
import net.minecraft.server.MinecraftServer;

import java.util.function.Consumer;

public class StaticDataLoader {
    // Run after mod init.
    public static void init() {
        try (var manager = loadPacks()) {
            var round = new ModificationRoundImpl((MinecraftServer) null);

            round.markAsStartup();

            StaticDataLoadCallback.EVENT.invoker().onStaticDataLoad(manager, round);

            round.run();
        }
    }

    public static LifecycledResourceManager loadPacks() {
        ResourcePackManager packs = new ResourcePackManager(
            new VanillaDataPackProvider(),
            new FakeFileResourcePackProvider(ResourceType.SERVER_DATA, ResourcePackSource.WORLD)
        );

        packs.scanPacks();

        return new LifecycledResourceManagerImpl(ResourceType.SERVER_DATA, packs
            .getProfiles()
            .stream()
            .map(ResourcePackProfile::createResourcePack)
            .toList());
    }

    private static class FakeFileResourcePackProvider extends FileResourcePackProvider {
        public FakeFileResourcePackProvider(ResourceType type, ResourcePackSource source) {
            super(null, type, source);
        }

        @Override
        public void register(Consumer<ResourcePackProfile> profileAdder) {

        }
    }
}
