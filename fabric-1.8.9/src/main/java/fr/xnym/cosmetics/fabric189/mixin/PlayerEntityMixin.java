package fr.xnym.cosmetics.fabric189.mixin;

import fr.xnym.cosmetics.common.Cosmetiques;
import fr.xnym.cosmetics.common.StylesPseudo;
import fr.xnym.cosmetics.common.XnymCosmetics;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin {
    @Inject(method = "getName()Lnet/minecraft/text/Text;", at = @At("RETURN"), cancellable = true)
    private void xnym$pseudo(CallbackInfoReturnable<Text> cir) {
        PlayerEntity self = (PlayerEntity) (Object) this;
        if (self.world == null || !self.world.isClient) return;
        Cosmetiques c = XnymCosmetics.get(self.getUuid());
        if (c.nametag == null || cir.getReturnValue() == null) return;
        String stylise = StylesPseudo.remplacerLegacy(c.nametag, cir.getReturnValue().asFormattedString(),
                self.getGameProfile().getName());
        if (stylise != null) cir.setReturnValue(new LiteralText(stylise));
    }
}
