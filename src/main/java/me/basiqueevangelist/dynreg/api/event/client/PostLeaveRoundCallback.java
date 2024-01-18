package me.basiqueevangelist.dynreg.api.event.client;

import me.basiqueevangelist.dynreg.impl.round.ModificationRoundImpl;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

public interface PostLeaveRoundCallback {
    Event<PostLeaveRoundCallback> EVENT = EventFactory.createArrayBacked(PostLeaveRoundCallback.class, callbacks -> round -> {
        for (var callback : callbacks) {
            callback.onClientDisconnect(round);
        }
    });

    void onClientDisconnect(ModificationRoundImpl round);
}
