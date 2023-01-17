package me.basiqueevangelist.dynreg.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

public final class RoundEvents {
    public static final Event<Pre> PRE = EventFactory.createArrayBacked(Pre.class, callbacks -> () -> {
        for (var cb : callbacks) {
            cb.preRound();
        }
    });

    public static final Event<Post> POST = EventFactory.createArrayBacked(Post.class, callbacks -> () -> {
        for (var cb : callbacks) {
            cb.postRound();
        }
    });

    private RoundEvents() {

    }

    public interface Pre {
        void preRound();
    }

    public interface Post {
        void postRound();
    }
}
