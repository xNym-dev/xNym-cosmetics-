package fr.xnym.cosmetics.forge189;

import fr.xnym.cosmetics.common.Cosmetiques;
import fr.xnym.cosmetics.common.XnymCosmetics;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;

/** Dessine le bandana autour de la tête du joueur. */
public class CoucheBandana implements LayerRenderer<AbstractClientPlayer> {
    private final RenderPlayer rendu;
    private final ModeleBandana modele = new ModeleBandana();

    public CoucheBandana(RenderPlayer rendu) {
        this.rendu = rendu;
    }

    @Override
    public void doRenderLayer(AbstractClientPlayer joueur, float limbSwing, float limbSwingAmount, float partialTicks,
                              float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        if (joueur.isInvisible()) return;
        Cosmetiques c = XnymCosmetics.get(joueur.getUniqueID());
        if (c.bandana == null) return;
        GlStateManager.pushMatrix();
        if (joueur.isSneaking()) GlStateManager.translate(0.0F, 0.2F, 0.0F);
        rendu.getMainModel().bipedHead.postRender(0.0625F);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        rendu.bindTexture(XnymForge189.texture("bandana", c.bandana));
        modele.dessiner(scale);
        GlStateManager.popMatrix();
    }

    @Override
    public boolean shouldCombineTextures() {
        return false;
    }

    /** Bandeau sur le front + nœud et deux pans à l'arrière de la tête. Texture 64x32. */
    static class ModeleBandana extends ModelBase {
        private final ModelRenderer bandeau;
        private final ModelRenderer noeud;
        private final ModelRenderer pan1;
        private final ModelRenderer pan2;

        ModeleBandana() {
            textureWidth = 64;
            textureHeight = 32;
            bandeau = new ModelRenderer(this, 0, 0);
            bandeau.addBox(-4.0F, -7.5F, -4.0F, 8, 3, 8, 0.6F);
            noeud = new ModelRenderer(this, 0, 16);
            noeud.addBox(-1.0F, -6.8F, 4.5F, 2, 2, 1, 0.1F);
            pan1 = new ModelRenderer(this, 8, 16);
            pan1.setRotationPoint(-0.5F, -5.5F, 5.0F);
            pan1.addBox(-0.5F, 0.0F, -0.5F, 1, 4, 1, -0.1F);
            pan1.rotateAngleX = 0.35F;
            pan1.rotateAngleZ = 0.25F;
            pan2 = new ModelRenderer(this, 12, 16);
            pan2.setRotationPoint(0.5F, -5.5F, 5.0F);
            pan2.addBox(-0.5F, 0.0F, -0.5F, 1, 3, 1, -0.1F);
            pan2.rotateAngleX = 0.3F;
            pan2.rotateAngleZ = -0.3F;
        }

        void dessiner(float scale) {
            bandeau.render(scale);
            noeud.render(scale);
            pan1.render(scale);
            pan2.render(scale);
        }
    }
}
