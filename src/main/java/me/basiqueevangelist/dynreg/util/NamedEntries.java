package me.basiqueevangelist.dynreg.util;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import me.basiqueevangelist.dynreg.ap.NamesFor;
import me.basiqueevangelist.dynreg.generated.NamedEntriesSetters;
import net.minecraft.block.MapColor;
import net.minecraft.block.Material;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Rarity;

public final class NamedEntries {
    @NamesFor(Material.class)
    public static final BiMap<String, Material> MATERIALS = HashBiMap.create();

    @NamesFor(BlockSoundGroup.class)
    public static final BiMap<String, BlockSoundGroup> BLOCK_SOUND_GROUPS = HashBiMap.create();

    @NamesFor(Rarity.class)
    public static final BiMap<String, Rarity> RARITIES = HashBiMap.create();

    @NamesFor(MapColor.class)
    public static final BiMap<String, MapColor> MAP_COLORS = HashBiMap.create();

    public static final BiMap<String, SpawnGroup> SPAWN_GROUPS = HashBiMap.create();

    private NamedEntries() {

    }

    static {
        NamedEntriesSetters.init();

        for (SpawnGroup group : SpawnGroup.values()) {
            SPAWN_GROUPS.put(group.getName(), group);
        }
    }
}
