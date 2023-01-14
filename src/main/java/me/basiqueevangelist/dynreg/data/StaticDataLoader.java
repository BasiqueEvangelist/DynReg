package me.basiqueevangelist.dynreg.data;

import me.basiqueevangelist.dynreg.round.DynamicRound;
import net.minecraft.resource.*;
import net.minecraft.server.MinecraftServer;

import java.util.function.Consumer;

public class StaticDataLoader {
    // Run after mod init.
    public static void init() {
        try (var manager = loadPacks()) {
            var entries = RegistryEntryLoader.loadAll(manager);
            var round = new DynamicRound((MinecraftServer) null);

            round.markAsStartup();

            for (var entry : entries.values()) {
                round.addEntry(entry);
            }

            round.run();
        }
    }

    public static LifecycledResourceManager loadPacks() {
        ResourcePackManager packs = new ResourcePackManager(
            ResourceType.SERVER_DATA,
            new VanillaDataPackProvider(),
            new FakeFileResourcePackProvider(ResourcePackSource.PACK_SOURCE_WORLD)
        );

        packs.scanPacks();

        return new LifecycledResourceManagerImpl(ResourceType.SERVER_DATA, packs
            .getProfiles()
            .stream()
            .map(ResourcePackProfile::createResourcePack)
            .toList());
    }

    private static class FakeFileResourcePackProvider extends FileResourcePackProvider {
        public FakeFileResourcePackProvider(ResourcePackSource source) {
            super(null, source);
        }

        @Override
        public void register(Consumer<ResourcePackProfile> profileAdder, ResourcePackProfile.Factory factory) {

        }
    }
}
