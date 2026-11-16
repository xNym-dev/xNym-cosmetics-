package fr.xnym.cosmetics.fabric189;

import com.mojang.blaze3d.platform.GlStateManager;
import fr.xnym.cosmetics.common.Cosmetiques;
import fr.xnym.cosmetics.common.XnymCosmetics;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.model.BiPedModel;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.model.ModelPart;

/** Dessine le bandana autour de la tête du joueur. */
public class BandanaFeatureRenderer implements FeatureRenderer<AbstractClientPlayerEntity> {
    private final PlayerEntityRenderer rendu;
    private final ModeleBandana modele = new ModeleBandana();

    public BandanaFeatureRenderer(PlayerEntityRenderer rendu) {
        this.rendu = rendu;
    }

    @Override
    public void render(AbstractClientPlayerEntity joueur, float limbAngle, float limbDistance, float tickDelta,
                       float age, float headYaw, float headPitch, float scale) {
        if (joueur.isInvisible()) return;
        Cosmetiques c = XnymCosmetics.get(joueur.getUuid());
        if (c.bandana == null) return;
        GlStateManager.pushMatrix();
        if (joueur.isSneaking()) GlStateManager.translate(0.0F, 0.2F, 0.0F);
        ((BiPedModel) rendu.getModel()).head.preRender(0.0625F);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        rendu.bindTexture(XnymFabric189.texture("bandana", c.bandana));
        modele.dessiner(scale);
        GlStateManager.popMatrix();
    }

    @Override
    public boolean combineTextures() {
        return false;
    }

    static class ModeleBandana extends EntityModel {
        private final ModelPart bandeau;
        private final ModelPart noeud;
        private final ModelPart pan1;
        private final ModelPart pan2;

        ModeleBandana() {
            textureWidth = 64;
            textureHeight = 32;
            bandeau = new ModelPart(this, 0, 0);
            bandeau.addCuboid(-4.0F, -7.5F, -4.0F, 8, 3, 8, 0.6F);
            noeud = new ModelPart(this, 0, 16);
            noeud.addCuboid(-1.0F, -6.8F, 4.5F, 2, 2, 1, 0.1F);
            pan1 = new ModelPart(this, 8, 16);
            pan1.setPivot(-0.5F, -5.5F, 5.0F);
            pan1.addCuboid(-0.5F, 0.0F, -0.5F, 1, 4, 1, -0.1F);
            pan1.posX = 0.35F;
            pan1.posZ = 0.25F;
            pan2 = new ModelPart(this, 12, 16);
            pan2.setPivot(0.5F, -5.5F, 5.0F);
            pan2.addCuboid(-0.5F, 0.0F, -0.5F, 1, 3, 1, -0.1F);
            pan2.posX = 0.3F;
            pan2.posZ = -0.3F;
        }

        void dessiner(float scale) {
            bandeau.render(scale);
            noeud.render(scale);
            pan1.render(scale);
            pan2.render(scale);
        }
    }
}
