package fr.xnym.cosmetics.forge;

import fr.xnym.cosmetics.common.XnymCosmetics;
import net.minecraftforge.fml.common.Mod;

@Mod(XnymCosmetics.MOD_ID)
public class XnymForge {
    public XnymForge(net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext context) {
        XnymCosmetics.init();
    }
}
