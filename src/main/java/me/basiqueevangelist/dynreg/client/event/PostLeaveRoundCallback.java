package me.basiqueevangelist.dynreg.client.event;

import me.basiqueevangelist.dynreg.client.round.ClientDynamicRound;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

public interface PostLeaveRoundCallback {
    Event<PostLeaveRoundCallback> EVENT = EventFactory.createArrayBacked(PostLeaveRoundCallback.class, callbacks -> round -> {
        for (var callback : callbacks) {
            callback.onClientDisconnect(round);
        }
    });

    void onClientDisconnect(ClientDynamicRound round);
}
