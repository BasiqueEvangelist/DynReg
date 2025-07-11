package me.basiqueevangelist.dynreg.mixin;

import me.basiqueevangelist.dynreg.api.event.ResyncCallback;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.c2s.play.AcknowledgeReconfigurationC2SPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ConnectedClientData;
import net.minecraft.server.network.ServerCommonNetworkHandler;
import net.minecraft.server.network.ServerConfigurationNetworkHandler;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayNetworkHandler.class)
public abstract class ServerPlayNetworkHandlerMixin extends ServerCommonNetworkHandler {
    public ServerPlayNetworkHandlerMixin(MinecraftServer server, ClientConnection connection, ConnectedClientData clientData) {
        super(server, connection, clientData);
    }

    @Inject(method = "onAcknowledgeReconfiguration", at = @At("RETURN"))
    private void addReconnect(AcknowledgeReconfigurationC2SPacket packet, CallbackInfo ci) {
        var handler = (ServerConfigurationNetworkHandler) connection.getPacketListener();

        ResyncCallback.EVENT.invoker().onResync(server, handler, true);
        handler.endConfiguration();
    }
}
