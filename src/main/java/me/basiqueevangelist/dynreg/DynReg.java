package me.basiqueevangelist.dynreg;

import me.basiqueevangelist.dynreg.fixer.BlockFixer;
import me.basiqueevangelist.dynreg.fixer.ItemFixer;
import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;
import org.slf4j.LoggerFactory;

public class DynReg implements ModInitializer {
    public static String MODID = "dynreg";

    public static Identifier id(String path) {
        return new Identifier(MODID, path);
    }

    @Override
    public void onInitialize() {
        LoggerFactory.getLogger("DynReg").info("I have become DynReg, destroyer of immutability");

        ItemFixer.init();
        BlockFixer.init();
    }
}
