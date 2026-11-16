package fr.xnym.cosmetics.mc.mixin;

import fr.xnym.cosmetics.mc.XnymMc;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.scores.PlayerTeam;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class PlayerMixin {
    @Inject(method = "getDisplayName", at = @At("RETURN"), cancellable = true)
    private void xnym$pseudo(CallbackInfoReturnable<Component> cir) {
        Object self = this;
        if (!(self instanceof AbstractClientPlayer)) return;
        Player joueur = (Player) self;
        MutableComponent stylise = XnymMc.pseudo(joueur.getUUID(), joueur.getGameProfile().getName());
        if (stylise != null) cir.setReturnValue(PlayerTeam.formatNameForTeam(joueur.getTeam(), stylise));
    }
}
