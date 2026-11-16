package fr.xnym.cosmetics.forge189;

import fr.xnym.cosmetics.common.Cosmetiques;
import fr.xnym.cosmetics.common.XnymCosmetics;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;

/** Dessine les ailes dans le dos du joueur, avec un battement lent. */
public class CoucheAiles implements LayerRenderer<AbstractClientPlayer> {
    private final RenderPlayer rendu;
    private final ModeleAiles grandes = new ModeleAiles(true);
    private final ModeleAiles petites = new ModeleAiles(false);

    public CoucheAiles(RenderPlayer rendu) {
        this.rendu = rendu;
    }

    @Override
    public void doRenderLayer(AbstractClientPlayer joueur, float limbSwing, float limbSwingAmount, float partialTicks,
                              float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        if (joueur.isInvisible()) return;
        Cosmetiques c = XnymCosmetics.get(joueur.getUniqueID());
        if (c.ailes == null) return;
        ModeleAiles modele = c.grandesAiles() ? grandes : petites;
        GlStateManager.pushMatrix();
        if (joueur.isSneaking()) GlStateManager.translate(0.0F, 0.2F, 0.0F);
        rendu.getMainModel().bipedBody.postRender(0.0625F);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.disableCull();
        rendu.bindTexture(XnymForge189.texture("ailes", c.ailes));
        modele.dessiner(XnymCosmetics.angleAiles(ageInTicks, c.grandesAiles()), scale);
        GlStateManager.enableCull();
        GlStateManager.popMatrix();
    }

    @Override
    public boolean shouldCombineTextures() {
        return false;
    }

    /** Grandes : 20x20 (texture 40x20). Petites : 8x8 (texture 16x8). */
    static class ModeleAiles extends ModelBase {
        private final ModelRenderer gauche;
        private final ModelRenderer droite;

        ModeleAiles(boolean grandes) {
            int t = grandes ? 20 : 8;
            float haut = grandes ? -9.0F : -1.0F;
            float y = grandes ? 1.0F : 3.0F;
            textureWidth = 2 * t;
            textureHeight = t;
            gauche = new ModelRenderer(this, 0, 0);
            gauche.addBox(0.0F, haut, 0.0F, t, t, 0);
            gauche.setRotationPoint(0.8F, y, 2.3F);
            droite = new ModelRenderer(this, 0, 0);
            droite.mirror = true;
            droite.addBox(-t, haut, 0.0F, t, t, 0);
            droite.setRotationPoint(-0.8F, y, 2.3F);
        }

        void dessiner(float angle, float scale) {
            gauche.rotateAngleY = -angle;
            droite.rotateAngleY = angle;
            gauche.rotateAngleZ = -0.12F;
            droite.rotateAngleZ = 0.12F;
            gauche.render(scale);
            droite.render(scale);
        }
    }
}
