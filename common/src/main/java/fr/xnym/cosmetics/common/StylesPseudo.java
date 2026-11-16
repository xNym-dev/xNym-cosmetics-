package fr.xnym.cosmetics.common;

/**
 * Styles de name tag. Les versions récentes utilisent les couleurs RVB (vrais dégradés),
 * la 1.8.9 utilise les 16 couleurs classiques (§) les plus proches.
 */
public final class StylesPseudo {
    private StylesPseudo() {
    }

    /** Couleurs RVB du style (dégradé si plusieurs), ou null si le style est inconnu. */
    public static int[] couleurs(String style) {
        if (style == null) return null;
        if (style.equals("violet")) return new int[]{0xA855F7};
        if (style.equals("degrade")) return new int[]{0x7C3AED, 0xDC2626};
        if (style.equals("feu")) return new int[]{0xFACC15, 0xF97316, 0xDC2626};
        if (style.equals("or")) return new int[]{0xFACC15};
        if (style.equals("rouge")) return new int[]{0xDC2626};
        if (style.equals("etoile")) return new int[]{0xFFFFFF};
        return null;
    }

    /** Couleur RVB de l'icône. */
    public static int couleurIcone(String style, int[] couleurs) {
        return "etoile".equals(style) ? 0xA855F7 : couleurs[0];
    }

    /** Icône affichée devant le pseudo, ou null. */
    public static String icone(String style) {
        return "etoile".equals(style) ? "✦" : null;
    }

    public static boolean gras(String style) {
        return "or".equals(style);
    }

    /** Couleur RVB du caractère i sur n, selon le dégradé. */
    public static int couleur(int[] couleurs, int i, int n) {
        if (couleurs.length == 1 || n <= 1) return couleurs[0];
        float t = (float) i / (n - 1) * (couleurs.length - 1);
        int a = Math.min((int) t, couleurs.length - 2);
        float f = t - a;
        int c1 = couleurs[a], c2 = couleurs[a + 1];
        int r = (int) (((c1 >> 16) & 255) + (((c2 >> 16) & 255) - ((c1 >> 16) & 255)) * f);
        int g = (int) (((c1 >> 8) & 255) + (((c2 >> 8) & 255) - ((c1 >> 8) & 255)) * f);
        int b = (int) ((c1 & 255) + ((c2 & 255) - (c1 & 255)) * f);
        return (r << 16) | (g << 8) | b;
    }

    private static final int[] LEGACY = {
            0x000000, 0x0000AA, 0x00AA00, 0x00AAAA, 0xAA0000, 0xAA00AA, 0xFFAA00, 0xAAAAAA,
            0x555555, 0x5555FF, 0x55FF55, 0x55FFFF, 0xFF5555, 0xFF55FF, 0xFFFF55, 0xFFFFFF};
    private static final String CODES = "0123456789abcdef";

    /** Code couleur § le plus proche d'une couleur RVB (pour la 1.8.9). */
    public static char codeProche(int rgb) {
        int best = 0;
        long dist = Long.MAX_VALUE;
        for (int i = 0; i < LEGACY.length; i++) {
            long dr = ((rgb >> 16) & 255) - ((LEGACY[i] >> 16) & 255);
            long dg = ((rgb >> 8) & 255) - ((LEGACY[i] >> 8) & 255);
            long db = (rgb & 255) - (LEGACY[i] & 255);
            long d = dr * dr * 3 + dg * dg * 4 + db * db * 2;
            if (d < dist) {
                dist = d;
                best = i;
            }
        }
        return CODES.charAt(best);
    }

    /** Pseudo formaté avec les codes § (1.8.9), ou null si le style est inconnu. */
    public static String formatLegacy(String style, String pseudo) {
        int[] c = couleurs(style);
        if (c == null) return null;
        StringBuilder sb = new StringBuilder();
        String icone = icone(style);
        if (icone != null) sb.append('§').append(codeProche(couleurIcone(style, c))).append(icone).append(' ');
        char precedent = 0;
        for (int i = 0; i < pseudo.length(); i++) {
            char code = codeProche(couleur(c, i, pseudo.length()));
            if (code != precedent) {
                sb.append('§').append(code);
                if (gras(style)) sb.append("§l");
                precedent = code;
            }
            sb.append(pseudo.charAt(i));
        }
        return sb.append("§r").toString();
    }

    /**
     * Remplace le pseudo dans un texte déjà formaté (préfixe d'équipe, grade du serveur…) par sa version stylée.
     * Si le pseudo n'est pas trouvé, renvoie le pseudo stylé seul.
     */
    public static String remplacerLegacy(String style, String texteFormate, String pseudo) {
        String stylise = formatLegacy(style, pseudo);
        if (stylise == null) return null;
        int idx = texteFormate.indexOf(pseudo);
        if (idx < 0) return stylise;
        // on reprend la couleur active juste avant le pseudo pour la suite du texte (suffixe d'équipe)
        String avant = texteFormate.substring(0, idx);
        String apres = texteFormate.substring(idx + pseudo.length());
        String couleurAvant = derniersCodes(avant);
        return avant + stylise + couleurAvant + apres;
    }

    private static String derniersCodes(String s) {
        StringBuilder codes = new StringBuilder();
        for (int i = 0; i < s.length() - 1; i++) {
            if (s.charAt(i) == '§') {
                char c = Character.toLowerCase(s.charAt(i + 1));
                if (CODES.indexOf(c) >= 0 || c == 'r') codes.setLength(0);
                codes.append('§').append(c);
                i++;
            }
        }
        return codes.toString();
    }
}
