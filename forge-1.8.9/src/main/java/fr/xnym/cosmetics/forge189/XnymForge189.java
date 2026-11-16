package fr.xnym.cosmetics.forge189;

import fr.xnym.cosmetics.common.Cosmetiques;
import fr.xnym.cosmetics.common.StylesPseudo;
import fr.xnym.cosmetics.common.XnymCosmetics;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Mod(modid = XnymCosmetics.MOD_ID, name = "xNym Cosmetics", version = "1.1.0",
        clientSideOnly = true, acceptedMinecraftVersions = "[1.8.9]")
public class XnymForge189 {

    private static Field champCape;
    /** Joueurs à qui on a posé une cape xNym (pour la retirer s'ils l'enlèvent). */
    private final Map<UUID, ResourceLocation> capesPosees = new HashMap<UUID, ResourceLocation>();
    private int ticks;
    private int generationVue = -1;

    public static ResourceLocation texture(String type, String id) {
        return new ResourceLocation(XnymCosmetics.MOD_ID, "textures/" + type + "/" + id + ".png");
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent e) {
        XnymCosmetics.init();
        champCape = ReflectionHelper.findField(NetworkPlayerInfo.class, "locationCape", "field_178862_f");
        MinecraftForge.EVENT_BUS.register(new Pseudos());
        FMLCommonHandler.instance().bus().register(this);
    }

    @Mod.EventHandler
    @SuppressWarnings("unchecked")
    public void postInit(FMLPostInitializationEvent e) {
        for (RenderPlayer rendu : Minecraft.getMinecraft().getRenderManager().getSkinMap().values()) {
            List<Object> couches = ReflectionHelper.getPrivateValue(RendererLivingEntity.class, rendu,
                    "layerRenderers", "field_177097_h");
            couches.add(new CoucheBandana(rendu));
            couches.add(new CoucheAiles(rendu));
        }
    }

    /** Chaque tick : pose les capes et rafraîchit les pseudos quand les cosmétiques changent. */
    @SubscribeEvent
    public void tick(TickEvent.ClientTickEvent e) {
        if (e.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.theWorld == null || mc.getNetHandler() == null) return;
        boolean rafraichirPseudos = generationVue != XnymCosmetics.generation() || ++ticks % 100 == 0;
        generationVue = XnymCosmetics.generation();
        for (EntityPlayer joueur : mc.theWorld.playerEntities) {
            if (!(joueur instanceof AbstractClientPlayer)) continue;
            UUID uuid = joueur.getUniqueID();
            Cosmetiques c = XnymCosmetics.get(uuid);
            poserCape(mc, uuid, c);
            if (rafraichirPseudos) joueur.refreshDisplayName();
        }
    }

    private void poserCape(Minecraft mc, UUID uuid, Cosmetiques c) {
        NetworkPlayerInfo info = mc.getNetHandler().getPlayerInfo(uuid);
        if (info == null) return;
        try {
            if (c.cape != null) {
                ResourceLocation cape = texture("cape", c.cape);
                if (!cape.equals(champCape.get(info))) champCape.set(info, cape);
                capesPosees.put(uuid, cape);
            } else if (capesPosees.containsKey(uuid)) {
                if (capesPosees.remove(uuid).equals(champCape.get(info))) champCape.set(info, null);
            }
        } catch (IllegalAccessException ignored) {
        }
    }

    public static class Pseudos {
        @SubscribeEvent
        public void nom(PlayerEvent.NameFormat e) {
            if (e.entityPlayer == null || e.entityPlayer.worldObj == null || !e.entityPlayer.worldObj.isRemote) return;
            Cosmetiques c = XnymCosmetics.get(e.entityPlayer.getUniqueID());
            if (c.nametag == null) return;
            String stylise = StylesPseudo.remplacerLegacy(c.nametag, e.displayname, e.username);
            if (stylise != null) e.displayname = stylise;
        }
    }
}
