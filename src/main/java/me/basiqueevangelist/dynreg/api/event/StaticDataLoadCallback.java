package me.basiqueevangelist.dynreg.api.event;

import me.basiqueevangelist.dynreg.round.DynamicRound;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.resource.ResourceManager;

public interface StaticDataLoadCallback {
    Event<StaticDataLoadCallback> EVENT = EventFactory.createArrayBacked(StaticDataLoadCallback.class, callbacks -> (manager, round) -> {
       for (var cb : callbacks) {
           cb.onStaticDataLoad(manager, round);
       }
    });

    void onStaticDataLoad(ResourceManager manager, DynamicRound round);
}
