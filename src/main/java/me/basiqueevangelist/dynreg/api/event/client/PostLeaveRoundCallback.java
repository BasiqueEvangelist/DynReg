package me.basiqueevangelist.dynreg.api.event.client;

import me.basiqueevangelist.dynreg.round.DynamicRound;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

public interface PostLeaveRoundCallback {
    Event<PostLeaveRoundCallback> EVENT = EventFactory.createArrayBacked(PostLeaveRoundCallback.class, callbacks -> round -> {
        for (var callback : callbacks) {
            callback.onClientDisconnect(round);
        }
    });

    void onClientDisconnect(DynamicRound round);
}
