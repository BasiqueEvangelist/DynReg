package me.basiqueevangelist.dynreg.impl.data;

import me.basiqueevangelist.dynreg.api.event.StaticDataLoadCallback;
import me.basiqueevangelist.dynreg.impl.round.ModificationRoundImpl;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resource.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.path.SymlinkFinder;
import net.minecraft.world.level.storage.LevelStorage;

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
        SymlinkFinder finder = LevelStorage.createSymlinkFinder(FabricLoader.getInstance().getGameDir().resolve("allowed_symlinks.txt"));

        ResourcePackManager packs = new ResourcePackManager(
            new VanillaDataPackProvider(finder),
            new FakeFileResourcePackProvider(ResourceType.SERVER_DATA, ResourcePackSource.WORLD, finder)
        );

        packs.scanPacks();

        return new LifecycledResourceManagerImpl(ResourceType.SERVER_DATA, packs
            .getProfiles()
            .stream()
            .map(ResourcePackProfile::createResourcePack)
            .toList());
    }

    private static class FakeFileResourcePackProvider extends FileResourcePackProvider {
        public FakeFileResourcePackProvider(ResourceType type, ResourcePackSource source, SymlinkFinder finder) {
            super(null, type, source, finder);
        }

        @Override
        public void register(Consumer<ResourcePackProfile> profileAdder) {

        }
    }
}
