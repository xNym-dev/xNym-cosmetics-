package fr.xnym.cosmetics.mc;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

public class CoucheBandana extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
    private final ModelPart modele = ModeleBandana.creer();

    public CoucheBandana(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> parent) {
        super(parent);
    }

    @Override
    public void render(PoseStack pose, MultiBufferSource buffers, int lumiere, AbstractClientPlayer joueur,
                       float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks,
                       float netHeadYaw, float headPitch) {
        if (joueur.isInvisible()) return;
        ResourceLocation texture = XnymMc.bandana(joueur.getUUID());
        if (texture == null) return;
        pose.pushPose();
        getParentModel().head.translateAndRotate(pose);
        modele.render(pose, buffers.getBuffer(RenderType.entityCutoutNoCull(texture)), lumiere, OverlayTexture.NO_OVERLAY);
        pose.popPose();
    }
}
