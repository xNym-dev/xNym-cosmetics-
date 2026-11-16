"""Génère les textures des cosmétiques xNym (designs originaux).

Palette imposée : violet, noir, jaune, blanc, orange, rouge.

Cape   : texture 64x32 (x8 = 512x256). Face extérieure (vue de dos) en (1,1) taille 10x16,
         face intérieure en (12,1). Zone élytres (22,0)-(46,22) remplie de la couleur de base.
Bandana: texture 64x32 (x8), entièrement remplie par le tissu (toutes les faces du modèle l'utilisent).
Ailes  : plan sans épaisseur. Grandes ailes 20x20 (texture 40x20), petites 8x8 (texture 16x8), x8.
         Région gauche = face avant (racine de l'aile à gauche), région droite = la même image retournée.
"""
import math
import random
from pathlib import Path

import numpy as np
from PIL import Image, ImageDraw, ImageFilter

S = 8
RACINE = Path(__file__).resolve().parent.parent / "common/src/main/resources/assets/xnymcosmetics/textures"

NOIR = (10, 8, 14)
BLANC = (246, 244, 240)
VIOLET = (124, 58, 237)
VIOLET_CLAIR = (167, 139, 250)
VIOLET_SOMBRE = (59, 22, 120)
JAUNE = (250, 204, 21)
ORANGE = (249, 115, 22)
ROUGE = (220, 38, 38)
ROUGE_SOMBRE = (127, 20, 20)


def lerp(a, b, t):
    t = max(0.0, min(1.0, t))
    return tuple(int(a[i] + (b[i] - a[i]) * t) for i in range(3))


def degrade(couleurs, t):
    """Couleur à la position t (0..1) d'un dégradé multi-couleurs."""
    t = max(0.0, min(1.0, t)) * (len(couleurs) - 1)
    i = min(int(t), len(couleurs) - 2)
    return lerp(couleurs[i], couleurs[i + 1], t - i)


def bruit(w, h, echelle, octaves=4, graine=0):
    """Bruit de valeur lissé (0..1), taille w x h."""
    rnd = np.random.default_rng(graine)
    total = np.zeros((h, w))
    amp, somme = 1.0, 0.0
    for o in range(octaves):
        gw, gh = max(2, int(w / echelle * 2 ** o) + 2), max(2, int(h / echelle * 2 ** o) + 2)
        grille = Image.fromarray((rnd.random((gh, gw)) * 255).astype(np.uint8))
        couche = np.asarray(grille.resize((w, h), Image.BICUBIC), dtype=float) / 255
        total += couche * amp
        somme += amp
        amp *= 0.5
    total /= somme
    return (total - total.min()) / (total.max() - total.min() + 1e-9)


def nouvelle(w, h, fond):
    img = Image.new("RGBA", (w, h), fond + (255,))
    return img, ImageDraw.Draw(img)


def cadre(img, couleur, epaisseur=2):
    d = ImageDraw.Draw(img)
    w, h = img.size
    for i in range(epaisseur):
        d.rectangle([i, i, w - 1 - i, h - 1 - i], outline=couleur)


# ── Capes (face extérieure 80x128) ──────────────────────────────────────────

def cape_fuji(w, h):
    img, d = nouvelle(w, h, BLANC)
    # soleil noir
    cx, cy, r = w * 0.5, h * 0.27, w * 0.2
    d.ellipse([cx - r, cy - r, cx + r, cy + r], fill=NOIR)
    # montagne : pente concave, sommet plat
    base, sommet, demi_sommet = h * 0.86, h * 0.47, w * 0.07
    gauche, droite = [], []
    for i in range(41):
        t = i / 40
        y = sommet + (base - sommet) * t
        x = demi_sommet + (w * 0.62 - demi_sommet) * (t ** 1.6)
        gauche.append((w / 2 - x, y))
        droite.append((w / 2 + x, y))
    d.polygon(gauche + [(-2, base), (-2, h + 2), (w + 2, h + 2), (w + 2, base)] + droite[::-1], fill=NOIR)
    # neige : dents blanches sous le sommet
    neige = [gauche[0]]
    for i, t in enumerate([0.1, 0.16, 0.12, 0.2, 0.13, 0.18, 0.11]):
        x = w / 2 - demi_sommet - w * 0.06 + i * (2 * (demi_sommet + w * 0.06)) / 6
        neige.append((x, sommet + (base - sommet) * t))
    neige.append(droite[0])
    d.polygon(neige, fill=BLANC)
    # lac / sol
    for k in range(4):
        y = h * 0.9 + k * 3
        d.line([(w * 0.15 + k * 4, y), (w * 0.85 - k * 4, y)], fill=BLANC, width=1)
    cadre(img, NOIR, 2)
    return img


def cape_couchant(w, h):
    img, d = nouvelle(w, h, NOIR)
    for y in range(h):
        d.line([(0, y), (w, y)], fill=degrade([VIOLET_SOMBRE, VIOLET, ROUGE, ORANGE, JAUNE], y / (h * 0.7)))
    # soleil noir à bandes
    cx, cy, r = w / 2, h * 0.58, w * 0.33
    soleil = Image.new("L", (w, h), 0)
    ImageDraw.Draw(soleil).ellipse([cx - r, cy - r, cx + r, cy + r], fill=255)
    bandes = ImageDraw.Draw(soleil)
    for k in range(6):
        y = cy + r * (0.05 + k * 0.17)
        bandes.rectangle([0, y, w, y + 2 + k], fill=0)
    img.paste(Image.new("RGBA", (w, h), NOIR + (255,)), (0, 0), soleil)
    # montagnes noires
    pts = [(0, h)]
    rnd = random.Random(5)
    for x in range(0, w + 8, 8):
        pts.append((x, h * 0.78 - rnd.random() * h * 0.08 - (h * 0.06 if 20 < x < 50 else 0)))
    pts.append((w, h))
    d.polygon(pts, fill=NOIR)
    cadre(img, JAUNE, 1)
    return img


def cape_vague(w, h):
    img, d = nouvelle(w, h, BLANC)
    # soleil rouge
    d.ellipse([w * 0.6, h * 0.08, w * 0.88, h * 0.08 + w * 0.28], fill=ROUGE)
    # vagues noires en écailles
    for rangee in range(9):
        y0 = h * 0.42 + rangee * h * 0.068
        decal = (rangee % 2) * 8
        for x0 in range(-16 + decal, w + 16, 16):
            for k, rr in enumerate((9, 6, 3)):
                d.arc([x0 - rr, y0 - rr, x0 + rr, y0 + rr], 180, 360, fill=NOIR, width=2 if k == 0 else 1)
    # oiseaux noirs
    for bx, by in ((0.25, 0.18), (0.36, 0.24), (0.18, 0.28)):
        x, y = w * bx, h * by
        d.arc([x - 6, y - 3, x, y + 3], 200, 340, fill=NOIR, width=2)
        d.arc([x, y - 3, x + 6, y + 3], 200, 340, fill=NOIR, width=2)
    d.line([(0, h * 0.4), (w, h * 0.4)], fill=NOIR, width=2)
    cadre(img, NOIR, 2)
    return img


def cape_eclair(w, h):
    img, d = nouvelle(w, h, NOIR)
    n = bruit(w, h, 30, 3, graine=11)
    px = img.load()
    for y in range(h):
        for x in range(w):
            v = n[y, x] * (1 - y / h) * 0.6
            px[x, y] = lerp(NOIR, VIOLET_SOMBRE, v) + (255,)
    eclair = [(w * 0.62, 0), (w * 0.28, h * 0.48), (w * 0.52, h * 0.47), (w * 0.3, h),
              (w * 0.78, h * 0.4), (w * 0.53, h * 0.41), (w * 0.85, 0)]
    halo = Image.new("RGBA", (w, h), (0, 0, 0, 0))
    ImageDraw.Draw(halo).polygon(eclair, fill=ORANGE + (200,))
    halo = halo.filter(ImageFilter.GaussianBlur(5))
    img.alpha_composite(halo)
    d = ImageDraw.Draw(img)
    d.polygon(eclair, fill=JAUNE)
    coeur = [(w * 0.66, 0), (w * 0.38, h * 0.46), (w * 0.56, h * 0.45), (w * 0.36, h * 0.9),
             (w * 0.7, h * 0.43), (w * 0.52, h * 0.43), (w * 0.76, 0)]
    d.polygon(coeur, fill=BLANC)
    cadre(img, JAUNE, 1)
    return img


def cape_flammes(w, h):
    img, _ = nouvelle(w, h, NOIR)
    n = bruit(w, h, 14, 4, graine=3)
    px = img.load()
    for y in range(h):
        for x in range(w):
            hauteur = 1 - y / h
            flamme = abs(math.sin(x * math.pi / (w / 3.5)))
            v = (1 - hauteur) * 1.25 + n[y, x] * 0.55 + flamme * 0.25 - 0.55
            if v > 0.12:
                px[x, y] = degrade([ROUGE_SOMBRE, ROUGE, ORANGE, JAUNE, BLANC], min(1, (v - 0.12) * 1.5)) + (255,)
    cadre(img, ORANGE, 1)
    return img


def cape_galaxie(w, h):
    img, _ = nouvelle(w, h, NOIR)
    n1, n2 = bruit(w, h, 40, 5, graine=21), bruit(w, h, 18, 4, graine=22)
    px = img.load()
    for y in range(h):
        for x in range(w):
            v = n1[y, x] ** 1.6
            c = degrade([NOIR, VIOLET_SOMBRE, VIOLET, VIOLET_CLAIR], v * 1.2)
            if n2[y, x] > 0.72:
                c = lerp(c, ORANGE, (n2[y, x] - 0.72) * 1.2)
            px[x, y] = c + (255,)
    rnd = random.Random(8)
    d = ImageDraw.Draw(img)
    for _ in range(110):
        x, y = rnd.randrange(w), rnd.randrange(h)
        c = rnd.choice([BLANC, BLANC, JAUNE])
        d.point((x, y), fill=c)
    for _ in range(9):
        x, y = rnd.randrange(4, w - 4), rnd.randrange(4, h - 4)
        c = rnd.choice([BLANC, JAUNE])
        d.line([(x - 3, y), (x + 3, y)], fill=c)
        d.line([(x, y - 3), (x, y + 3)], fill=c)
    return img


def cape_xnym(w, h):
    img, d = nouvelle(w, h, NOIR)
    for y in range(h):
        d.line([(0, y), (w, y)], fill=lerp(NOIR, VIOLET_SOMBRE, y / h * 0.7))
    m, e = 14, 9
    for (a, b) in (((m, 22), (w - m, h - 22)), ((w - m, 22), (m, h - 22))):
        d.line([a, b], fill=BLANC, width=e + 4)
    for (a, b), c in ((((m, 22), (w - m, h - 22)), VIOLET_CLAIR), (((w - m, 22), (m, h - 22)), VIOLET)):
        d.line([a, b], fill=c, width=e)
    for i in range(5):
        d.ellipse([w / 2 - 2, 8 + i * 0, w / 2 + 2, 12], fill=JAUNE)
        d.ellipse([w / 2 - 2, h - 12, w / 2 + 2, h - 8], fill=JAUNE)
    cadre(img, VIOLET, 2)
    d.rectangle([4, 4, w - 5, h - 5], outline=BLANC)
    return img


def cape_lune(w, h):
    img, d = nouvelle(w, h, NOIR)
    for y in range(h):
        d.line([(0, y), (w, y)], fill=degrade([NOIR, VIOLET_SOMBRE, VIOLET], y / h))
    rnd = random.Random(4)
    for _ in range(40):
        d.point((rnd.randrange(w), rnd.randrange(int(h * 0.6))), fill=BLANC)
    cx, cy, r = w * 0.5, h * 0.3, w * 0.24
    halo = Image.new("RGBA", (w, h), (0, 0, 0, 0))
    ImageDraw.Draw(halo).ellipse([cx - r * 1.4, cy - r * 1.4, cx + r * 1.4, cy + r * 1.4], fill=BLANC + (70,))
    img.alpha_composite(halo.filter(ImageFilter.GaussianBlur(6)))
    d = ImageDraw.Draw(img)
    d.ellipse([cx - r, cy - r, cx + r, cy + r], fill=BLANC)
    d.ellipse([cx - r * 0.4, cy - r * 0.2, cx - r * 0.1, cy + r * 0.1], fill=(225, 222, 215))
    d.ellipse([cx + r * 0.2, cy + r * 0.3, cx + r * 0.45, cy + r * 0.55], fill=(225, 222, 215))
    # forêt de sapins noirs
    for i in range(-1, 9):
        bx = i * w / 7 + rnd.uniform(-3, 3)
        hb = h * rnd.uniform(0.22, 0.36)
        for k in range(4):
            yk = h - hb + k * hb / 4.5
            lk = (k + 1) * w * 0.035
            d.polygon([(bx, yk), (bx - lk, yk + hb / 3), (bx + lk, yk + hb / 3)], fill=NOIR)
        d.rectangle([bx - 1, h - 6, bx + 1, h], fill=NOIR)
    d.rectangle([0, h - 5, w, h], fill=NOIR)
    return img


def cape_course(w, h):
    img, d = nouvelle(w, h, BLANC)
    c = 10
    for y in range(0, int(h * 0.42), c):
        for x in range(0, w, c):
            if (x // c + y // c) % 2 == 0:
                d.rectangle([x, y, x + c - 1, y + c - 1], fill=NOIR)
    d.rectangle([0, h * 0.42, w, h], fill=NOIR)
    for k, coul in enumerate((ROUGE, ORANGE, JAUNE)):
        y = h * 0.5 + k * 9
        d.polygon([(0, y + 18), (w, y - 6), (w, y), (0, y + 24)], fill=coul)
    cadre(img, NOIR, 2)
    return img


CAPES = {
    "fuji": cape_fuji,
    "couchant": cape_couchant,
    "vague": cape_vague,
    "eclair": cape_eclair,
    "flammes": cape_flammes,
    "galaxie": cape_galaxie,
    "xnym": cape_xnym,
    "lune": cape_lune,
    "course": cape_course,
}


def construire_cape(motif):
    img = Image.new("RGBA", (64 * S, 32 * S), (0, 0, 0, 0))
    exterieur = motif(10 * S, 16 * S)
    base = exterieur.getpixel((exterieur.width // 2, exterieur.height - 4))[:3]
    d = ImageDraw.Draw(img)
    d.rectangle([0, 0, 22 * S - 1, 17 * S - 1], fill=lerp(base, NOIR, 0.5) + (255,))
    img.paste(exterieur, (1 * S, 1 * S))
    interieur = Image.new("RGBA", (10 * S, 16 * S), lerp(base, NOIR, 0.65) + (255,))
    img.paste(interieur, (12 * S, 1 * S))
    d.rectangle([22 * S, 0, 46 * S - 1, 22 * S - 1], fill=base + (255,))
    return img


# ── Bandanas ────────────────────────────────────────────────────────────────

def goutte(d, x, y, taille, angle, couleur, fond):
    """Motif cachemire : goutte avec un point au centre."""
    pts = []
    for i in range(24):
        a = i / 24 * 2 * math.pi
        r = taille * (0.55 + 0.45 * math.cos(a / 2) ** 2)
        px = math.cos(a) * r
        py = math.sin(a) * r * 0.62
        pts.append((x + px * math.cos(angle) - py * math.sin(angle), y + px * math.sin(angle) + py * math.cos(angle)))
    d.polygon(pts, fill=couleur)
    d.ellipse([x - taille * 0.22, y - taille * 0.22, x + taille * 0.22, y + taille * 0.22], fill=fond)
    d.ellipse([x - taille * 0.08, y - taille * 0.08, x + taille * 0.08, y + taille * 0.08], fill=couleur)


def bandana(fond, motif, style, graine):
    w, h = 64 * S, 32 * S
    img = Image.new("RGBA", (w, h), fond + (255,))
    d = ImageDraw.Draw(img)
    rnd = random.Random(graine)
    if style == "cachemire":
        for y in range(6, h, 22):
            for x in range((y // 22 % 2) * 14, w, 28):
                goutte(d, x + rnd.uniform(-2, 2), y, 8, rnd.uniform(0, 6.28), motif, fond)
                d.ellipse([x + 12, y + 9, x + 15, y + 12], fill=motif)
    elif style == "pois":
        for y in range(4, h, 12):
            for x in range((y // 12 % 2) * 6, w, 12):
                d.ellipse([x, y, x + 4, y + 4], fill=motif)
    elif style == "rayures":
        for x in range(-h, w, 20):
            d.line([(x, h), (x + h, 0)], fill=motif, width=7)
    elif style == "flammes":
        n = bruit(w, h, 10, 3, graine=graine)
        px = img.load()
        for y in range(h):
            for x in range(w):
                v = n[y, x] + 0.35 * math.sin(y * 0.25)
                if v > 0.75:
                    px[x, y] = motif + (255,)
    # liseré sur les bords de chaque face visible (8 px de tissu = 64 px ici)
    for y in (0, 8 * S - 1):
        d.line([(0, y), (w, y)], fill=motif, width=3)
    return img


BANDANAS = {
    "rouge": (ROUGE, BLANC, "cachemire"),
    "noir": (NOIR, BLANC, "cachemire"),
    "blanc": (BLANC, NOIR, "cachemire"),
    "violet": (VIOLET, BLANC, "cachemire"),
    "orange": (ORANGE, NOIR, "pois"),
    "xnym": (NOIR, VIOLET, "rayures"),
    "feu": (ROUGE, JAUNE, "flammes"),
}


# ── Ailes ───────────────────────────────────────────────────────────────────
# Image d'une aile gauche : racine à gauche, pointe à droite, haut en haut.

def aile_plumes(w, h, couleurs, contour):
    """Aile à plumes : bras en haut, plumes qui pendent et s'allongent vers la pointe."""
    img = Image.new("RGBA", (w, h), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)

    def bras(s):
        return (w * 0.03 + s * w * 0.55, h * 0.32 - s * h * 0.26)

    def plume(base, angle, longueur, largeur, couleur):
        dx, dy = math.cos(angle), math.sin(angle)
        nx, ny = -dy, dx
        bout = (base[0] + dx * longueur, base[1] + dy * longueur)
        pts = []
        for i in range(13):
            t = i / 12
            r = largeur * math.sin(math.pi * min(1, t * 1.15)) ** 0.7
            pts.append((base[0] + dx * longueur * t + nx * r, base[1] + dy * longueur * t + ny * r))
        for i in range(12, -1, -1):
            t = i / 12
            r = largeur * 0.55 * math.sin(math.pi * min(1, t * 1.15)) ** 0.7
            pts.append((base[0] + dx * longueur * t - nx * r, base[1] + dy * longueur * t - ny * r))
        d.polygon(pts, fill=couleur, outline=contour)
        d.line([base, bout], fill=contour, width=1)

    # rangées : rémiges (longues), puis couvertures, puis petites plumes près du bras
    for rang, (lg, couleur, nb) in enumerate(((0.78, couleurs[0], 11), (0.48, couleurs[1], 10), (0.24, couleurs[2], 9))):
        for i in range(nb):
            s = 0.05 + i / (nb - 1) * 0.93
            base = bras(s)
            angle = math.radians(95 - 78 * s ** 1.2)
            longueur = h * lg * (0.6 + 0.75 * s)
            plume(base, angle, longueur, w * (0.05 + 0.025 * (2 - rang)), couleur)
    pts = [bras(i / 20) for i in range(21)]
    d.line(pts, fill=contour, width=max(2, w // 50))
    return img


def aile_membrane(w, h, membrane, os_couleur, bord):
    img = Image.new("RGBA", (w, h), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    epaule = (w * 0.04, h * 0.2)
    coude = (w * 0.45, h * 0.04)
    doigts = [(w * 0.98, h * 0.08), (w * 0.9, h * 0.5), (w * 0.68, h * 0.85), (w * 0.38, h * 0.95)]
    contour = [epaule, coude, doigts[0]]
    for a, b in zip(doigts, doigts[1:]):
        mx, my = (a[0] + b[0]) / 2, (a[1] + b[1]) / 2
        contour.append((mx - (b[1] - a[1]) * 0.18 - w * 0.03, my - h * 0.12))  # festons
        contour.append(b)
    contour += [(w * 0.08, h * 0.62)]
    d.polygon(contour, fill=membrane)
    # nervures de la membrane
    for k in range(1, 4):
        d.line([coude, (doigts[k][0] * 0.85, doigts[k][1] * 0.85)], fill=lerp(membrane, os_couleur, 0.4), width=1)
    d.line([epaule, coude], fill=os_couleur, width=max(2, w // 40))
    for p in doigts:
        d.line([coude, p], fill=os_couleur, width=max(1, w // 60))
    d.polygon(contour, outline=bord)
    d.polygon([(coude[0] - 2, coude[1]), (coude[0] + 4, coude[1] - h * 0.04), (coude[0] + 3, coude[1] + 2)], fill=os_couleur)
    return img


def aile_papillon(w, h, couleur, bord, points):
    """Aile de papillon originale : deux lobes arrondis, bord épais à pois."""
    img = Image.new("RGBA", (w, h), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    racine = (w * 0.04, h * 0.5)

    def lobe(rx, ry, cx, cy, a0, a1):
        pts = [racine]
        for i in range(25):
            a = math.radians(a0 + (a1 - a0) * i / 24)
            pts.append((cx + math.cos(a) * rx, cy + math.sin(a) * ry))
        return pts

    haut = lobe(w * 0.5, h * 0.42, w * 0.5, h * 0.36, 200, 400)
    bas = lobe(w * 0.36, h * 0.32, w * 0.36, h * 0.7, -30, 160)
    for forme in (haut, bas):
        d.polygon(forme, fill=bord)
    interieur = Image.new("RGBA", (w, h), (0, 0, 0, 0))
    di = ImageDraw.Draw(interieur)
    for forme, (cx, cy) in ((haut, (w * 0.45, h * 0.36)), (bas, (w * 0.32, h * 0.68))):
        di.polygon([(cx + (x - cx) * 0.78, cy + (y - cy) * 0.78) for x, y in forme], fill=couleur)
    img.alpha_composite(interieur)
    d = ImageDraw.Draw(img)
    for a, b in ((racine, (w * 0.9, h * 0.2)), (racine, (w * 0.75, h * 0.55)), (racine, (w * 0.45, h * 0.95))):
        d.line([a, b], fill=bord, width=max(1, w // 40))
    for x, y in ((0.93, 0.32), (0.86, 0.12), (0.7, 0.04), (0.6, 0.97), (0.7, 0.85)):
        d.ellipse([w * x - w * 0.025, h * y - w * 0.025, w * x + w * 0.025, h * y + w * 0.025], fill=points)
    return img


def construire_ailes(aile, larg, haut):
    """Texture (2*larg x haut) : face avant + même image retournée."""
    img = Image.new("RGBA", (2 * larg, haut), (0, 0, 0, 0))
    img.paste(aile, (0, 0))
    img.paste(aile.transpose(Image.FLIP_LEFT_RIGHT), (larg, 0))
    return img


G, P = (20 * S, 20 * S), (8 * S, 8 * S)
AILES = {
    "grand_dragon": (G, lambda w, h: aile_membrane(w, h, VIOLET_SOMBRE, NOIR, VIOLET_CLAIR)),
    "grand_ange": (G, lambda w, h: aile_plumes(w, h, [BLANC, (236, 232, 222), (252, 240, 200)], (200, 190, 160))),
    "grand_phenix": (G, lambda w, h: aile_plumes(w, h, [ROUGE, ORANGE, JAUNE], ROUGE_SOMBRE)),
    "grand_demon": (G, lambda w, h: aile_membrane(w, h, ROUGE_SOMBRE, NOIR, ROUGE)),
    "petit_ange": (P, lambda w, h: aile_plumes(w, h, [BLANC, (236, 232, 222), (252, 240, 200)], (190, 180, 150))),
    "petit_demon": (P, lambda w, h: aile_membrane(w, h, ROUGE, NOIR, NOIR)),
    "petit_papillon": (P, lambda w, h: aile_papillon(w, h, ORANGE, NOIR, BLANC)),
    "petit_violet": (P, lambda w, h: aile_plumes(w, h, [VIOLET, VIOLET_CLAIR, BLANC], VIOLET_SOMBRE)),
}


def main():
    for dossier in ("cape", "bandana", "ailes"):
        (RACINE / dossier).mkdir(parents=True, exist_ok=True)
        for ancien in (RACINE / dossier).glob("*.png"):
            ancien.unlink()
    for nom, motif in CAPES.items():
        construire_cape(motif).save(RACINE / "cape" / f"{nom}.png")
    for i, (nom, (fond, c, style)) in enumerate(BANDANAS.items()):
        bandana(fond, c, style, i).save(RACINE / "bandana" / f"{nom}.png")
    for nom, ((w, h), dessin) in AILES.items():
        construire_ailes(dessin(w, h), w, h).save(RACINE / "ailes" / f"{nom}.png")
    print("Textures générées dans", RACINE)


if __name__ == "__main__":
    main()
