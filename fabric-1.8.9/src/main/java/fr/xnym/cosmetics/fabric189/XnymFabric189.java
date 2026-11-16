package fr.xnym.cosmetics.fabric189;

import fr.xnym.cosmetics.common.XnymCosmetics;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.util.Identifier;

public class XnymFabric189 implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        XnymCosmetics.init();
    }

    public static Identifier texture(String type, String id) {
        return new Identifier(XnymCosmetics.MOD_ID, "textures/" + type + "/" + id + ".png");
    }
}
