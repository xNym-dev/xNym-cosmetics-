package fr.xnym.cosmetics.mc;

import net.minecraft.resources.ResourceLocation;

/** Ajouté à PlayerRenderState par mixin : cosmétiques du joueur rendu (bandana, ailes). */
public interface EtatBandana {
    ResourceLocation xnym$getBandana();

    void xnym$setBandana(ResourceLocation texture);

    ResourceLocation xnym$getAiles();

    boolean xnym$grandesAiles();

    void xnym$setAiles(ResourceLocation texture, boolean grandes);
}
