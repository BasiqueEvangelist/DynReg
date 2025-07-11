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
        var symlinkFinder = LevelStorage.createSymlinkFinder(FabricLoader.getInstance().getGameDir().resolve("allowed_symlinks.txt"));

        ResourcePackManager packs = new ResourcePackManager(
            new VanillaDataPackProvider(symlinkFinder),
            new FakeFileResourcePackProvider(ResourceType.SERVER_DATA, ResourcePackSource.WORLD, symlinkFinder)
        );

        packs.scanPacks();

        return new LifecycledResourceManagerImpl(ResourceType.SERVER_DATA, packs
            .getProfiles()
            .stream()
            .map(ResourcePackProfile::createResourcePack)
            .toList());
    }

    private static class FakeFileResourcePackProvider extends FileResourcePackProvider {
        public FakeFileResourcePackProvider(ResourceType type, ResourcePackSource source, SymlinkFinder symlinkFinder) {
            super(null, type, source, symlinkFinder);
        }

        @Override
        public void register(Consumer<ResourcePackProfile> profileAdder) {

        }
    }
}
