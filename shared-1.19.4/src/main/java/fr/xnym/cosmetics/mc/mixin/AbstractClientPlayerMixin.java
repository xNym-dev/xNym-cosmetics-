package fr.xnym.cosmetics.mc.mixin;

import fr.xnym.cosmetics.mc.XnymMc;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractClientPlayer.class)
public abstract class AbstractClientPlayerMixin {
    @Inject(method = "isCapeLoaded", at = @At("HEAD"), cancellable = true)
    private void xnym$capeChargee(CallbackInfoReturnable<Boolean> cir) {
        if (XnymMc.cape(((AbstractClientPlayer) (Object) this).getUUID()) != null) cir.setReturnValue(true);
    }

    @Inject(method = "getCloakTextureLocation", at = @At("HEAD"), cancellable = true)
    private void xnym$cape(CallbackInfoReturnable<ResourceLocation> cir) {
        ResourceLocation cape = XnymMc.cape(((AbstractClientPlayer) (Object) this).getUUID());
        if (cape != null) cir.setReturnValue(cape);
    }
}
