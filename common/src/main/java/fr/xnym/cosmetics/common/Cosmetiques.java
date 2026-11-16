package fr.xnym.cosmetics.common;

/** Cosmétiques portés par un joueur (chaque champ peut être null). */
public final class Cosmetiques {
    public static final Cosmetiques AUCUN = new Cosmetiques(null, null, null);

    public final String cape;
    public final String bandana;
    public final String nametag;

    public Cosmetiques(String cape, String bandana, String nametag) {
        this.cape = cape;
        this.bandana = bandana;
        this.nametag = nametag;
    }

    public boolean estVide() {
        return cape == null && bandana == null && nametag == null;
    }
}
