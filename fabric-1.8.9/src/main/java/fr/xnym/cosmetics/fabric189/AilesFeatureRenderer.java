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

/** Dessine les ailes dans le dos du joueur, avec un battement lent. */
public class AilesFeatureRenderer implements FeatureRenderer<AbstractClientPlayerEntity> {
    private final PlayerEntityRenderer rendu;
    private final ModeleAiles grandes = new ModeleAiles(true);
    private final ModeleAiles petites = new ModeleAiles(false);

    public AilesFeatureRenderer(PlayerEntityRenderer rendu) {
        this.rendu = rendu;
    }

    @Override
    public void render(AbstractClientPlayerEntity joueur, float limbAngle, float limbDistance, float tickDelta,
                       float age, float headYaw, float headPitch, float scale) {
        if (joueur.isInvisible()) return;
        Cosmetiques c = XnymCosmetics.get(joueur.getUuid());
        if (c.ailes == null) return;
        ModeleAiles modele = c.grandesAiles() ? grandes : petites;
        GlStateManager.pushMatrix();
        if (joueur.isSneaking()) GlStateManager.translate(0.0F, 0.2F, 0.0F);
        ((BiPedModel) rendu.getModel()).body.preRender(0.0625F);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.disableCull();
        rendu.bindTexture(XnymFabric189.texture("ailes", c.ailes));
        modele.dessiner(XnymCosmetics.angleAiles(age, c.grandesAiles()), scale);
        GlStateManager.enableCull();
        GlStateManager.popMatrix();
    }

    @Override
    public boolean combineTextures() {
        return false;
    }

    static class ModeleAiles extends EntityModel {
        private final ModelPart gauche;
        private final ModelPart droite;

        ModeleAiles(boolean grandes) {
            int t = grandes ? 20 : 8;
            float haut = grandes ? -9.0F : -1.0F;
            float y = grandes ? 1.0F : 3.0F;
            textureWidth = 2 * t;
            textureHeight = t;
            gauche = new ModelPart(this, 0, 0);
            gauche.addCuboid(0.0F, haut, 0.0F, t, t, 0, 0.0F);
            gauche.setPivot(0.8F, y, 2.3F);
            droite = new ModelPart(this, 0, 0);
            droite.mirror = true;
            droite.addCuboid((float) -t, haut, 0.0F, t, t, 0, 0.0F);
            droite.setPivot(-0.8F, y, 2.3F);
        }

        void dessiner(float angle, float scale) {
            gauche.posY = -angle;
            droite.posY = angle;
            gauche.posZ = -0.12F;
            droite.posZ = 0.12F;
            gauche.render(scale);
            droite.render(scale);
        }
    }
}
