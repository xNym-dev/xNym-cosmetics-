package fr.xnym.cosmetics.mc;

import fr.xnym.cosmetics.common.Cosmetiques;
import fr.xnym.cosmetics.common.StylesPseudo;
import fr.xnym.cosmetics.common.XnymCosmetics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;

/** Fonctions communes aux versions récentes (noms officiels Mojang), pour Forge et Fabric. */
public final class XnymMc {
    private XnymMc() {
    }

    public static ResourceLocation texture(String type, String id) {
        return new ResourceLocation(XnymCosmetics.MOD_ID, "textures/" + type + "/" + id + ".png");
    }

    public static ResourceLocation cape(java.util.UUID uuid) {
        Cosmetiques c = XnymCosmetics.get(uuid);
        return c.cape == null ? null : texture("cape", c.cape);
    }

    public static ResourceLocation bandana(java.util.UUID uuid) {
        Cosmetiques c = XnymCosmetics.get(uuid);
        return c.bandana == null ? null : texture("bandana", c.bandana);
    }

    /** Pseudo coloré selon le style (dégradé RVB), ou null. */
    public static MutableComponent pseudo(java.util.UUID uuid, String pseudo) {
        String style = XnymCosmetics.get(uuid).nametag;
        int[] couleurs = StylesPseudo.couleurs(style);
        if (couleurs == null) return null;
        final boolean gras = StylesPseudo.gras(style);
        MutableComponent res = Component.empty();
        String icone = StylesPseudo.icone(style);
        if (icone != null) {
            final int c0 = couleurs[0];
            res.append(Component.literal(icone + " ").withStyle(s -> s.withColor(TextColor.fromRgb(c0))));
        }
        for (int i = 0; i < pseudo.length(); i++) {
            final int rgb = StylesPseudo.couleur(couleurs, i, pseudo.length());
            res.append(Component.literal(String.valueOf(pseudo.charAt(i)))
                    .withStyle(s -> s.withColor(TextColor.fromRgb(rgb)).withBold(gras)));
        }
        return res;
    }
}
