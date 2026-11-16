// Serveur des cosmétiques xNym (Cloudflare Worker + KV).
//
// GET  /catalog                 → catalogue des cosmétiques
// POST /lookup   {uuids:[...]}  → { "<uuid>": {cape, bandana, nametag}, ... } (uniquement ceux qui ont des cosmétiques)
// POST /auth/start {name}       → { nonce }   (le client appelle ensuite Mojang "join" avec serverId = nonce)
// POST /auth/finish {nonce}     → { token, uuid, name }  (vérifié auprès de Mojang "hasJoined")
// GET  /me   (Authorization: Bearer <token>) → cosmétiques du joueur
// PUT  /me   (Authorization: Bearer <token>) {cape, bandana, nametag} → enregistre
//
// Clés KV : "u:<uuid sans tirets>" → JSON des cosmétiques ; "n:<nonce>" → pseudo (5 min) ; "cfg:secret" → clé HMAC.

import catalog from "../../catalog.json";

const MAX_LOOKUP = 100;
const DUREE_TOKEN = 30 * 24 * 3600; // 30 jours
const IDS = {
  cape: new Set(catalog.capes.map((c) => c.id)),
  bandana: new Set(catalog.bandanas.map((c) => c.id)),
  nametag: new Set(catalog.nametags.map((c) => c.id)),
};

const json = (data, status = 200, extra = {}) =>
  new Response(JSON.stringify(data), {
    status,
    headers: { "content-type": "application/json; charset=utf-8", "access-control-allow-origin": "*", ...extra },
  });
const erreur = (message, status = 400) => json({ erreur: message }, status);

const normUuid = (u) => (typeof u === "string" ? u.replace(/-/g, "").toLowerCase() : "");
const uuidValide = (u) => /^[0-9a-f]{32}$/.test(u);

function hex(buf) {
  return [...new Uint8Array(buf)].map((b) => b.toString(16).padStart(2, "0")).join("");
}

function b64url(str) {
  return btoa(str).replace(/\+/g, "-").replace(/\//g, "_").replace(/=+$/, "");
}
function deB64url(str) {
  return atob(str.replace(/-/g, "+").replace(/_/g, "/"));
}

async function cleHmac(env) {
  let secret = await env.COSMETICS.get("cfg:secret");
  if (!secret) {
    secret = hex(crypto.getRandomValues(new Uint8Array(32)));
    await env.COSMETICS.put("cfg:secret", secret);
  }
  return crypto.subtle.importKey("raw", new TextEncoder().encode(secret), { name: "HMAC", hash: "SHA-256" }, false, [
    "sign",
  ]);
}

async function signer(env, payload) {
  const cle = await cleHmac(env);
  return hex(await crypto.subtle.sign("HMAC", cle, new TextEncoder().encode(payload)));
}

async function creerToken(env, uuid) {
  const payload = b64url(JSON.stringify({ u: uuid, e: Math.floor(Date.now() / 1000) + DUREE_TOKEN }));
  return `${payload}.${await signer(env, payload)}`;
}

async function lireToken(env, request) {
  const auth = request.headers.get("authorization") || "";
  const m = auth.match(/^Bearer\s+([A-Za-z0-9_-]+)\.([0-9a-f]{64})$/);
  if (!m) return null;
  const attendu = await signer(env, m[1]);
  if (attendu !== m[2]) return null;
  try {
    const { u, e } = JSON.parse(deB64url(m[1]));
    if (!uuidValide(u) || e < Date.now() / 1000) return null;
    return u;
  } catch {
    return null;
  }
}

async function lireJson(request) {
  try {
    return await request.json();
  } catch {
    return null;
  }
}

async function routes(request, env) {
  const url = new URL(request.url);
  const chemin = url.pathname.replace(/\/+$/, "") || "/";

  if (request.method === "OPTIONS") {
    return new Response(null, {
      headers: {
        "access-control-allow-origin": "*",
        "access-control-allow-methods": "GET, POST, PUT, OPTIONS",
        "access-control-allow-headers": "content-type, authorization",
      },
    });
  }

  if (chemin === "/" && request.method === "GET") return json({ service: "xnym-cosmetics", ok: true });

  if (chemin === "/catalog" && request.method === "GET") {
    return json(catalog, 200, { "cache-control": "public, max-age=300" });
  }

  if (chemin === "/lookup" && request.method === "POST") {
    const corps = await lireJson(request);
    if (!corps || !Array.isArray(corps.uuids)) return erreur("uuids manquants");
    const uuids = [...new Set(corps.uuids.map(normUuid).filter(uuidValide))].slice(0, MAX_LOOKUP);
    const valeurs = await Promise.all(uuids.map((u) => env.COSMETICS.get(`u:${u}`, "json")));
    const res = {};
    uuids.forEach((u, i) => {
      if (valeurs[i]) res[u] = valeurs[i];
    });
    return json(res);
  }

  if (chemin === "/auth/start" && request.method === "POST") {
    const corps = await lireJson(request);
    const name = corps && typeof corps.name === "string" ? corps.name : "";
    if (!/^[A-Za-z0-9_]{1,16}$/.test(name)) return erreur("pseudo invalide");
    const nonce = hex(crypto.getRandomValues(new Uint8Array(16)));
    await env.COSMETICS.put(`n:${nonce}`, name, { expirationTtl: 300 });
    return json({ nonce });
  }

  if (chemin === "/auth/finish" && request.method === "POST") {
    const corps = await lireJson(request);
    const nonce = corps && typeof corps.nonce === "string" ? corps.nonce : "";
    if (!/^[0-9a-f]{32}$/.test(nonce)) return erreur("nonce invalide");
    const name = await env.COSMETICS.get(`n:${nonce}`);
    if (!name) return erreur("nonce expiré", 401);
    await env.COSMETICS.delete(`n:${nonce}`);
    const r = await fetch(
      `https://sessionserver.mojang.com/session/minecraft/hasJoined?username=${encodeURIComponent(name)}&serverId=${nonce}`
    );
    if (r.status !== 200) return erreur("vérification Mojang refusée", 401);
    const profil = await r.json();
    const uuid = normUuid(profil.id);
    if (!uuidValide(uuid)) return erreur("profil Mojang invalide", 401);
    return json({ token: await creerToken(env, uuid), uuid, name: profil.name });
  }

  if (chemin === "/me") {
    const uuid = await lireToken(env, request);
    if (!uuid) return erreur("non connecté", 401);
    if (request.method === "GET") {
      return json((await env.COSMETICS.get(`u:${uuid}`, "json")) || {});
    }
    if (request.method === "PUT") {
      const corps = await lireJson(request);
      if (!corps || typeof corps !== "object") return erreur("corps invalide");
      const choix = {};
      for (const type of ["cape", "bandana", "nametag"]) {
        const v = corps[type];
        if (v === null || v === undefined || v === "") continue;
        if (typeof v !== "string" || !IDS[type].has(v)) return erreur(`${type} inconnu : ${v}`);
        choix[type] = v;
      }
      if (Object.keys(choix).length === 0) await env.COSMETICS.delete(`u:${uuid}`);
      else await env.COSMETICS.put(`u:${uuid}`, JSON.stringify(choix));
      return json(choix);
    }
  }

  return erreur("introuvable", 404);
}

export default {
  async fetch(request, env) {
    try {
      return await routes(request, env);
    } catch (e) {
      return erreur("erreur serveur", 500);
    }
  },
};
