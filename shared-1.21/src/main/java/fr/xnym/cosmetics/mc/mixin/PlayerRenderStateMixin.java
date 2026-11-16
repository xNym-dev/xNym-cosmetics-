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
    @Unique
    private ResourceLocation xnym$ailes;
    @Unique
    private boolean xnym$grandes;

    @Override
    public ResourceLocation xnym$getBandana() {
        return xnym$bandana;
    }

    @Override
    public void xnym$setBandana(ResourceLocation texture) {
        xnym$bandana = texture;
    }

    @Override
    public ResourceLocation xnym$getAiles() {
        return xnym$ailes;
    }

    @Override
    public boolean xnym$grandesAiles() {
        return xnym$grandes;
    }

    @Override
    public void xnym$setAiles(ResourceLocation texture, boolean grandes) {
        xnym$ailes = texture;
        xnym$grandes = grandes;
    }
}
