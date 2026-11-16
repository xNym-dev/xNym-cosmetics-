package fr.xnym.cosmetics.fabric;

import fr.xnym.cosmetics.common.XnymCosmetics;
import net.fabricmc.api.ClientModInitializer;

public class XnymFabric implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        XnymCosmetics.init();
    }
}
