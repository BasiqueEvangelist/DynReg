package me.basiqueevangelist.dynreg.compat.polymer;

import eu.pb4.polymer.api.utils.PolymerUtils;
import me.basiqueevangelist.dynreg.event.ResyncCallback;

public final class PolymerCompat {
    private PolymerCompat() {

    }

    public static void init() {
        ResyncCallback.EVENT.register(
            (server, player, reloadResourcePacks) -> PolymerUtils.reloadWorld(player));
    }
}
