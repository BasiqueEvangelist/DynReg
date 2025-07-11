package me.basiqueevangelist.dynreg.mixin;

import me.basiqueevangelist.dynreg.impl.access.ExtendedClientConnection;
import net.minecraft.network.ClientConnection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ClientConnection.class)
public class ClientConnectionMixin implements ExtendedClientConnection {
    @Unique private boolean isResync = false;

    @Override
    public void dynreg$markAsResync() {
        isResync = true;
    }

    @Override
    public boolean dynreg$isResync() {
        return isResync;
    }
}
