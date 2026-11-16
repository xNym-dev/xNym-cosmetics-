package fr.xnym.cosmetics.mc;

import com.mojang.blaze3d.vertex.PoseStack;
import fr.xnym.cosmetics.common.XnymCosmetics;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

public class CoucheAiles extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
    private final ModelPart grandes = ModeleAiles.creer(true);
    private final ModelPart petites = ModeleAiles.creer(false);

    public CoucheAiles(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> parent) {
        super(parent);
    }

    @Override
    public void render(PoseStack pose, MultiBufferSource buffers, int lumiere, AbstractClientPlayer joueur,
                       float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks,
                       float netHeadYaw, float headPitch) {
        if (joueur.isInvisible()) return;
        ResourceLocation texture = XnymMc.ailes(joueur.getUUID());
        if (texture == null) return;
        boolean grand = XnymMc.grandesAiles(joueur.getUUID());
        ModelPart modele = grand ? grandes : petites;
        ModeleAiles.ouvrir(modele, XnymCosmetics.angleAiles(ageInTicks, grand));
        pose.pushPose();
        getParentModel().body.translateAndRotate(pose);
        modele.render(pose, buffers.getBuffer(RenderType.entityCutoutNoCull(texture)), lumiere, OverlayTexture.NO_OVERLAY);
        pose.popPose();
    }
}
