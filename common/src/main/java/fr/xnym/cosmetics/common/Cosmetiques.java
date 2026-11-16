package fr.xnym.cosmetics.common;

/** Cosmétiques portés par un joueur (chaque champ peut être null). */
public final class Cosmetiques {
    public static final Cosmetiques AUCUN = new Cosmetiques(null, null, null, null);

    public final String cape;
    public final String bandana;
    public final String nametag;
    public final String ailes;

    public Cosmetiques(String cape, String bandana, String nametag, String ailes) {
        this.cape = cape;
        this.bandana = bandana;
        this.nametag = nametag;
        this.ailes = ailes;
    }

    public boolean estVide() {
        return cape == null && bandana == null && nametag == null && ailes == null;
    }

    /** Vrai pour les grandes ailes ("grand_..."), faux pour les petites. */
    public boolean grandesAiles() {
        return ailes != null && ailes.startsWith("grand_");
    }
}
