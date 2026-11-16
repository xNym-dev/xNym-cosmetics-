"""Génère les textures des capes et bandanas xNym (PNG haute résolution x4).

Cape : texture 64x32 (x4 = 256x128). Face extérieure (vue de dos) en (1,1) taille 10x16,
face intérieure en (12,1), bords autour. Zone élytres (22,0)-(46,22) remplie de la couleur de base.
Bandana : texture 64x32 (x4) entièrement remplie par le motif (toutes les faces du modèle l'utilisent).
"""
import math
import random
from pathlib import Path
from PIL import Image, ImageDraw

S = 4  # facteur d'échelle
RACINE = Path(__file__).resolve().parent.parent / "textures"
NOIR = (14, 10, 22)
VIOLET = (124, 58, 237)
VIOLET_CLAIR = (192, 132, 252)
ROSE = (236, 72, 153)


def lerp(a, b, t):
    return tuple(int(a[i] + (b[i] - a[i]) * t) for i in range(3))


def face(w, h, motif):
    """Retourne une image w x h (en pixels finaux) dessinée par motif(img, draw)."""
    img = Image.new("RGBA", (w, h), NOIR + (255,))
    motif(img, ImageDraw.Draw(img))
    return img


# ── Motifs de capes (face extérieure 40x64) ─────────────────────────────────

def cape_violet_nuit(img, d):
    w, h = img.size
    for y in range(h):
        d.line([(0, y), (w, y)], fill=lerp(NOIR, VIOLET, y / (h - 1)))
    d.rectangle([0, 0, w - 1, h - 1], outline=VIOLET_CLAIR)


def cape_xnym(img, d):
    w, h = img.size
    d.rectangle([0, 0, w - 1, h - 1], fill=NOIR, outline=VIOLET)
    m = 8
    for k in range(-3, 4):
        d.line([(m + k, 16), (w - m + k, h - 16)], fill=VIOLET_CLAIR, width=1)
        d.line([(w - m + k, 16), (m + k, h - 16)], fill=VIOLET, width=1)


def cape_galaxie(img, d):
    w, h = img.size
    rnd = random.Random(42)
    for y in range(h):
        for x in range(w):
            n = 0.5 + 0.5 * math.sin(x * 0.25 + y * 0.12) * math.cos(y * 0.18 - x * 0.07)
            img.putpixel((x, y), lerp((10, 8, 40), (90, 30, 160), n) + (255,))
    for _ in range(60):
        x, y = rnd.randrange(w), rnd.randrange(h)
        c = rnd.choice([(255, 255, 255), VIOLET_CLAIR, (180, 200, 255)])
        img.putpixel((x, y), c + (255,))
        if rnd.random() < 0.2 and 0 < x < w - 1 and 0 < y < h - 1:
            for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)):
                img.putpixel((x + dx, y + dy), lerp(c, (60, 30, 110), 0.5) + (255,))


def cape_flammes(img, d):
    w, h = img.size
    for x in range(w):
        flamme = abs(math.sin(x * math.pi / 10))  # 4 langues de flamme
        lim = h - h * (0.25 + 0.4 * flamme + 0.05 * math.sin(x * 1.3))
        for y in range(h):
            if y > lim:
                t = (y - lim) / max(1, h - lim)
                img.putpixel((x, y), lerp(VIOLET_CLAIR, VIOLET, min(1, t * 1.5)) + (255,))
    d.rectangle([0, 0, w - 1, h - 1], outline=VIOLET)


def cape_eclair(img, d):
    w, h = img.size
    pts = [(w * 0.6, 0), (w * 0.3, h * 0.45), (w * 0.55, h * 0.45), (w * 0.35, h)]
    pts2 = [(w * 0.6, 0), (w * 0.3, h * 0.45), (w * 0.55, h * 0.45), (w * 0.35, h),
            (w * 0.75, h * 0.4), (w * 0.5, h * 0.4), (w * 0.8, 0)]
    d.polygon(pts2, fill=VIOLET)
    d.line(pts, fill=VIOLET_CLAIR, width=2)
    d.rectangle([0, 0, w - 1, h - 1], outline=VIOLET)


def cape_damier(img, d):
    w, h = img.size
    c = 8
    for y in range(0, h, c):
        for x in range(0, w, c):
            if (x // c + y // c) % 2 == 0:
                d.rectangle([x, y, x + c - 1, y + c - 1], fill=VIOLET)


CAPES = {
    "violet_nuit": cape_violet_nuit,
    "xnym": cape_xnym,
    "galaxie": cape_galaxie,
    "flammes": cape_flammes,
    "eclair": cape_eclair,
    "damier": cape_damier,
}


def construire_cape(motif):
    img = Image.new("RGBA", (64 * S, 32 * S), (0, 0, 0, 0))
    exterieur = face(10 * S, 16 * S, motif)
    base = exterieur.getpixel((exterieur.width // 2, exterieur.height - 2))
    d = ImageDraw.Draw(img)
    # bords et dessus/dessous : couleur sombre
    d.rectangle([0, 0, 22 * S - 1, 17 * S - 1], fill=lerp(NOIR, VIOLET, 0.3) + (255,))
    img.paste(exterieur, (1 * S, 1 * S))
    interieur = Image.new("RGBA", (10 * S, 16 * S), lerp(NOIR, VIOLET, 0.25) + (255,))
    img.paste(interieur, (12 * S, 1 * S))
    # élytres : couleur de base de la cape
    d.rectangle([22 * S, 0, 46 * S - 1, 22 * S - 1], fill=base)
    return img


# ── Bandanas ────────────────────────────────────────────────────────────────

def bandana(fond, motif_couleur, style):
    w, h = 64 * S, 32 * S
    img = Image.new("RGBA", (w, h), fond + (255,))
    d = ImageDraw.Draw(img)
    if style == "pois":
        for y in range(0, h, 8):
            for x in range((y // 8 % 2) * 4, w, 8):
                d.ellipse([x + 1, y + 1, x + 3, y + 3], fill=motif_couleur)
    elif style == "rayures":
        for x in range(-h, w, 10):
            d.line([(x, h), (x + h, 0)], fill=motif_couleur, width=3)
    elif style == "cachemire":
        rnd = random.Random(3)
        for _ in range(140):
            x, y = rnd.randrange(w), rnd.randrange(h)
            d.ellipse([x, y, x + 5, y + 3], outline=motif_couleur)
            d.point((x + 2, y + 1), fill=motif_couleur)
    return img


BANDANAS = {
    "violet": (VIOLET, (245, 240, 255), "cachemire"),
    "noir": (NOIR, (235, 235, 235), "pois"),
    "rouge": ((185, 28, 28), (250, 240, 240), "cachemire"),
    "xnym": (VIOLET, NOIR, "rayures"),
}


def main():
    (RACINE / "cape").mkdir(parents=True, exist_ok=True)
    (RACINE / "bandana").mkdir(parents=True, exist_ok=True)
    for nom, motif in CAPES.items():
        construire_cape(motif).save(RACINE / "cape" / f"{nom}.png")
    for nom, (fond, c, style) in BANDANAS.items():
        bandana(fond, c, style).save(RACINE / "bandana" / f"{nom}.png")
    print("Textures générées dans", RACINE)


if __name__ == "__main__":
    main()
