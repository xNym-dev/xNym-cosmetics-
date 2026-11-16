package fr.xnym.cosmetics.mc;

import net.minecraft.resources.ResourceLocation;

/** Ajouté à PlayerRenderState par mixin : texture du bandana du joueur rendu. */
public interface EtatBandana {
    ResourceLocation xnym$getBandana();

    void xnym$setBandana(ResourceLocation texture);
}
