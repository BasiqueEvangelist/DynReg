package me.basiqueevangelist.dynreg.compat.quilt;

// Called by reflection.
public class QuiltCompatInit {
    public static void init() {
        QuiltRegistryCompat.init();
        QuiltResourceLoaderCompat.init();
    }
}
