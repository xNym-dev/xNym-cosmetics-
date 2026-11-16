package fr.xnym.cosmetics.common;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Cœur du mod, commun à toutes les versions de Minecraft (Java 8, sans dépendance à Minecraft).
 * Demande au serveur xNym les cosmétiques des joueurs visibles, par paquets, en arrière-plan.
 */
public final class XnymCosmetics {
    public static final String MOD_ID = "xnymcosmetics";

    /** Adresse du serveur des cosmétiques (Cloudflare Worker). */
    public static final String API = "https://xnym-cosmetics.xnym.workers.dev";

    private static final long DUREE_CACHE_MS = 5 * 60 * 1000L;   // on redemande toutes les 5 min
    private static final long DUREE_ERREUR_MS = 60 * 1000L;      // après une erreur réseau, on attend 1 min
    private static final int TAILLE_PAQUET = 100;
    private static final Charset UTF8 = Charset.forName("UTF-8");
    private static final Gson GSON = new Gson();

    private static final Map<UUID, Cosmetiques> CACHE = new ConcurrentHashMap<UUID, Cosmetiques>();
    private static final Map<UUID, Long> EXPIRATION = new ConcurrentHashMap<UUID, Long>();
    private static final Map<UUID, Boolean> FILE = new ConcurrentHashMap<UUID, Boolean>();
    /** Incrémenté à chaque changement : les versions qui mettent le pseudo en cache s'en servent pour se rafraîchir. */
    private static volatile int generation = 0;

    private static Thread thread;

    private XnymCosmetics() {
    }

    /** À appeler une fois au démarrage du jeu (côté client). */
    public static synchronized void init() {
        if (thread != null) return;
        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                boucle();
            }
        }, "xNym-Cosmetics");
        thread.setDaemon(true);
        thread.start();
    }

    /** Cosmétiques d'un joueur (jamais null). Le premier appel lance la demande au serveur. */
    public static Cosmetiques get(UUID uuid) {
        if (uuid == null) return Cosmetiques.AUCUN;
        Long exp = EXPIRATION.get(uuid);
        if (exp == null || exp < System.currentTimeMillis()) {
            if (exp == null) EXPIRATION.put(uuid, Long.MAX_VALUE); // évite de redemander en boucle avant la réponse
            FILE.put(uuid, Boolean.TRUE);
        }
        Cosmetiques c = CACHE.get(uuid);
        return c == null ? Cosmetiques.AUCUN : c;
    }

    /** Force une nouvelle demande pour ce joueur (ex. après un changement dans le launcher). */
    public static void rafraichir(UUID uuid) {
        if (uuid != null) FILE.put(uuid, Boolean.TRUE);
    }

    public static int generation() {
        return generation;
    }

    private static void boucle() {
        while (true) {
            try {
                Thread.sleep(1000);
                if (FILE.isEmpty()) continue;
                List<UUID> paquet = new ArrayList<UUID>();
                for (UUID u : FILE.keySet()) {
                    paquet.add(u);
                    if (paquet.size() >= TAILLE_PAQUET) break;
                }
                for (UUID u : paquet) FILE.remove(u);
                demander(paquet);
            } catch (InterruptedException e) {
                return;
            } catch (Throwable t) {
                // on ne fait jamais planter le jeu pour des cosmétiques
            }
        }
    }

    private static void demander(List<UUID> uuids) {
        long maintenant = System.currentTimeMillis();
        try {
            StringBuilder corps = new StringBuilder("{\"uuids\":[");
            for (int i = 0; i < uuids.size(); i++) {
                if (i > 0) corps.append(',');
                corps.append('"').append(uuids.get(i).toString().replace("-", "")).append('"');
            }
            corps.append("]}");
            JsonObject reponse = GSON.fromJson(post(API + "/lookup", corps.toString()), JsonObject.class);
            boolean change = false;
            for (UUID u : uuids) {
                String cle = u.toString().replace("-", "");
                Cosmetiques avant = CACHE.get(u);
                Cosmetiques apres = Cosmetiques.AUCUN;
                if (reponse != null && reponse.has(cle) && reponse.get(cle).isJsonObject()) {
                    JsonObject o = reponse.getAsJsonObject(cle);
                    apres = new Cosmetiques(texte(o, "cape"), texte(o, "bandana"), texte(o, "nametag"));
                }
                if (apres.estVide()) CACHE.remove(u);
                else CACHE.put(u, apres);
                EXPIRATION.put(u, maintenant + DUREE_CACHE_MS);
                if (!memes(avant, apres)) change = true;
            }
            if (change) generation++;
        } catch (Throwable t) {
            for (UUID u : uuids) EXPIRATION.put(u, maintenant + DUREE_ERREUR_MS);
        }
    }

    private static boolean memes(Cosmetiques a, Cosmetiques b) {
        if (a == null) a = Cosmetiques.AUCUN;
        if (b == null) b = Cosmetiques.AUCUN;
        return eq(a.cape, b.cape) && eq(a.bandana, b.bandana) && eq(a.nametag, b.nametag);
    }

    private static boolean eq(String a, String b) {
        return a == null ? b == null : a.equals(b);
    }

    private static String texte(JsonObject o, String cle) {
        JsonElement e = o.get(cle);
        if (e == null || !e.isJsonPrimitive()) return null;
        String s = e.getAsString();
        return s.matches("[a-z0-9_]{1,32}") ? s : null;
    }

    private static String post(String adresse, String json) throws Exception {
        HttpURLConnection c = (HttpURLConnection) new URL(adresse).openConnection();
        c.setConnectTimeout(5000);
        c.setReadTimeout(5000);
        c.setRequestMethod("POST");
        c.setDoOutput(true);
        c.setRequestProperty("Content-Type", "application/json");
        c.setRequestProperty("User-Agent", "xNym-Cosmetics");
        OutputStream out = c.getOutputStream();
        try {
            out.write(json.getBytes(UTF8));
        } finally {
            out.close();
        }
        if (c.getResponseCode() != 200) throw new IllegalStateException("HTTP " + c.getResponseCode());
        InputStream in = c.getInputStream();
        try {
            ByteArrayOutputStream buf = new ByteArrayOutputStream();
            byte[] b = new byte[4096];
            int n;
            while ((n = in.read(b)) > 0) buf.write(b, 0, n);
            return new String(buf.toByteArray(), UTF8);
        } finally {
            in.close();
        }
    }
}
