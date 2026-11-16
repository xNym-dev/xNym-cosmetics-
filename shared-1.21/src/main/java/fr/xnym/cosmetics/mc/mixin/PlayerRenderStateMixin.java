package fr.xnym.cosmetics.mc.mixin;

import fr.xnym.cosmetics.mc.EtatBandana;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(PlayerRenderState.class)
public abstract class PlayerRenderStateMixin implements EtatBandana {
    @Unique
    private ResourceLocation xnym$bandana;

    @Override
    public ResourceLocation xnym$getBandana() {
        return xnym$bandana;
    }

    @Override
    public void xnym$setBandana(ResourceLocation texture) {
        xnym$bandana = texture;
    }
}
