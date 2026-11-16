package fr.xnym.cosmetics.fabric189.mixin;

import fr.xnym.cosmetics.common.Cosmetiques;
import fr.xnym.cosmetics.common.XnymCosmetics;
import fr.xnym.cosmetics.fabric189.XnymFabric189;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractClientPlayerEntity.class)
public abstract class AbstractClientPlayerEntityMixin {
    @Inject(method = "getCapeId", at = @At("HEAD"), cancellable = true)
    private void xnym$cape(CallbackInfoReturnable<Identifier> cir) {
        Cosmetiques c = XnymCosmetics.get(((AbstractClientPlayerEntity) (Object) this).getUuid());
        if (c.cape != null) cir.setReturnValue(XnymFabric189.texture("cape", c.cape));
    }
}
