package fr.xnym.cosmetics.mc;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/** Bandeau sur le front + nœud et deux pans à l'arrière de la tête. Texture 64x32. */
public final class ModeleBandana {
    private ModeleBandana() {
    }

    public static ModelPart creer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition racine = mesh.getRoot();
        racine.addOrReplaceChild("bandeau", CubeListBuilder.create().texOffs(0, 0)
                .addBox(-4.0F, -7.5F, -4.0F, 8.0F, 3.0F, 8.0F, new CubeDeformation(0.6F)), PartPose.ZERO);
        racine.addOrReplaceChild("noeud", CubeListBuilder.create().texOffs(0, 16)
                .addBox(-1.0F, -6.8F, 4.5F, 2.0F, 2.0F, 1.0F, new CubeDeformation(0.1F)), PartPose.ZERO);
        racine.addOrReplaceChild("pan1", CubeListBuilder.create().texOffs(8, 16)
                        .addBox(-0.5F, 0.0F, -0.5F, 1.0F, 4.0F, 1.0F, new CubeDeformation(-0.1F)),
                PartPose.offsetAndRotation(-0.5F, -5.5F, 5.0F, 0.35F, 0.0F, 0.25F));
        racine.addOrReplaceChild("pan2", CubeListBuilder.create().texOffs(12, 16)
                        .addBox(-0.5F, 0.0F, -0.5F, 1.0F, 3.0F, 1.0F, new CubeDeformation(-0.1F)),
                PartPose.offsetAndRotation(0.5F, -5.5F, 5.0F, 0.3F, 0.0F, -0.3F));
        return LayerDefinition.create(mesh, 64, 32).bakeRoot();
    }
}
