package me.basiqueevangelist.dynreg.mixin;

import me.basiqueevangelist.dynreg.api.event.PreSyncCallback;
import net.minecraft.network.ClientConnection;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerLoginNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ServerLoginNetworkHandler.class, priority = 900)
public class ServerLoginNetworkHandlerMixin {
    @Shadow @Final MinecraftServer server;

    @Shadow @Final ClientConnection connection;

//    @Inject(method = "addToServer", at = @At("HEAD"))
//    private void beforeQuilt(ServerPlayerEntity player, CallbackInfo ci) {
//        PreSyncCallback.EVENT.invoker().onPreSync(server, player, connection);
//    }
}
