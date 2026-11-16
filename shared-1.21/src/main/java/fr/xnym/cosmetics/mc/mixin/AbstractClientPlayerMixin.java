package fr.xnym.cosmetics.mc.mixin;

import fr.xnym.cosmetics.mc.XnymMc;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractClientPlayer.class)
public abstract class AbstractClientPlayerMixin {
    @Inject(method = "getSkin", at = @At("RETURN"), cancellable = true)
    private void xnym$cape(CallbackInfoReturnable<PlayerSkin> cir) {
        ResourceLocation cape = XnymMc.cape(((AbstractClientPlayer) (Object) this).getUUID());
        PlayerSkin s = cir.getReturnValue();
        if (cape != null && s != null) {
            cir.setReturnValue(new PlayerSkin(s.texture(), s.textureUrl(), cape, s.elytraTexture(), s.model(), s.secure()));
        }
    }
}
