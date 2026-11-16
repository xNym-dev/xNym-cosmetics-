package fr.xnym.cosmetics.mc.mixin;

import fr.xnym.cosmetics.mc.CoucheBandana;
import fr.xnym.cosmetics.mc.EtatBandana;
import fr.xnym.cosmetics.mc.XnymMc;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerRenderer.class)
public abstract class PlayerRendererMixin extends LivingEntityRenderer<AbstractClientPlayer, PlayerRenderState, PlayerModel> {
    protected PlayerRendererMixin(EntityRendererProvider.Context ctx, PlayerModel model, float ombre) {
        super(ctx, model, ombre);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void xnym$ajouterBandana(EntityRendererProvider.Context ctx, boolean fin, CallbackInfo ci) {
        this.addLayer(new CoucheBandana((PlayerRenderer) (Object) this));
    }

    @Inject(method = "extractRenderState(Lnet/minecraft/client/player/AbstractClientPlayer;Lnet/minecraft/client/renderer/entity/state/PlayerRenderState;F)V",
            at = @At("RETURN"))
    private void xnym$etat(AbstractClientPlayer joueur, PlayerRenderState etat, float tick, CallbackInfo ci) {
        ((EtatBandana) etat).xnym$setBandana(XnymMc.bandana(joueur.getUUID()));
    }
}
