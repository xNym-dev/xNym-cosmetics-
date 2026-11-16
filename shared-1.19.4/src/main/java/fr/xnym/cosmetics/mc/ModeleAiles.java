package fr.xnym.cosmetics.mc;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Deux ailes plates fixées dans le dos. Grandes : 20x20 (texture 40x20). Petites : 8x8 (texture 16x8).
 * L'aile gauche s'étend vers +x, la droite (miroir) vers -x.
 */
public final class ModeleAiles {
    private ModeleAiles() {
    }

    public static ModelPart creer(boolean grandes) {
        int t = grandes ? 20 : 8;
        float haut = grandes ? -9.0F : -1.0F;
        float y = grandes ? 1.0F : 3.0F;
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition racine = mesh.getRoot();
        racine.addOrReplaceChild("gauche", CubeListBuilder.create().texOffs(0, 0)
                .addBox(0.0F, haut, 0.0F, t, t, 0.0F), PartPose.offset(0.8F, y, 2.3F));
        racine.addOrReplaceChild("droite", CubeListBuilder.create().texOffs(0, 0).mirror()
                .addBox(-t, haut, 0.0F, t, t, 0.0F), PartPose.offset(-0.8F, y, 2.3F));
        return LayerDefinition.create(mesh, 2 * t, t).bakeRoot();
    }

    /** Ouvre les ailes selon l'angle donné (battement). */
    public static void ouvrir(ModelPart modele, float angle) {
        modele.getChild("gauche").yRot = -angle;
        modele.getChild("droite").yRot = angle;
        modele.getChild("gauche").zRot = -0.12F;
        modele.getChild("droite").zRot = 0.12F;
    }
}
