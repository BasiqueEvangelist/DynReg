package me.basiqueevangelist.dynreg.impl.compat.polymer;

import eu.pb4.polymer.core.api.utils.PolymerUtils;
import me.basiqueevangelist.dynreg.api.event.ResyncCallback;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public final class PolymerCompat {
    private PolymerCompat() {

    }

    public static void init() {
        ResyncCallback.EVENT.register(
            (server, player, reloadResourcePacks) -> PolymerUtils.reloadWorld(player));
    }
}
