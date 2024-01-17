package me.basiqueevangelist.dynreg.testmod;

import me.basiqueevangelist.dynreg.api.entry.RegistrationEntries;
import me.basiqueevangelist.dynreg.testmod.desc.*;
import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;

public class DynRegTest implements ModInitializer {
    public static final String MODID = "dynreg-testmod";

    public static Identifier id(String path) {
        return new Identifier(MODID, path);
    }

    @Override
    public void onInitialize() {
        RegistrationEntries.register(FlowerPotBlockEntry.ID, FlowerPotBlockEntry.class)
            .network(FlowerPotBlockEntry::write, FlowerPotBlockEntry::new)
            .json(FlowerPotBlockEntry::new);

        RegistrationEntries.register(StairsBlockEntry.ID, StairsBlockEntry.class)
            .network(StairsBlockEntry::write, StairsBlockEntry::new)
            .json(StairsBlockEntry::new);

        RegistrationEntries.register(SlabBlockEntry.ID, SlabBlockEntry.class)
            .network(SlabBlockEntry::write, SlabBlockEntry::new)
            .json(SlabBlockEntry::new);

        RegistrationEntries.register(PolymerBlockEntry.ID, PolymerBlockEntry.class)
            .network(PolymerBlockEntry::write, PolymerBlockEntry::new)
            .json(PolymerBlockEntry::new);

        RegistrationEntries.register(StatusEffectEntry.ID, StatusEffectEntry.class)
            .network(StatusEffectEntry::write, StatusEffectEntry::new)
            .json(StatusEffectEntry::new);

        RegistrationEntries.register(PotionEntry.ID, PotionEntry.class)
            .network(PotionEntry::write, PotionEntry::new)
            .json(PotionEntry::new);

        RegistrationEntries.register(SimpleEntityEntry.ID, SimpleEntityEntry.class)
            .network(SimpleEntityEntry::write, SimpleEntityEntry::new)
            .json(SimpleEntityEntry::new);
    }
}
