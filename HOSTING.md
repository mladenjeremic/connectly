# Besplatno puštanje Connectly-ja u rad

Aplikacija je spremna za **jedan servis** (backend servira i React build) + besplatna Postgres baza.
Slike se mogu čuvati u bazi (`STORAGE_TYPE=database`), pa ti ne treba trajni disk.

> Realno: „besplatno“ znači ograničenja. Pogodno za demo, portfolio i mali krug korisnika, ne za hiljade ljudi.

## Opcija A — bez kartice: Render (web servis) + Neon (Postgres)

1. **Neon** (neon.com) → napravi projekat → kopiraj podatke za konekciju.
   `DB_URL=jdbc:postgresql://<host>/<db>?sslmode=require`, `DB_USERNAME`, `DB_PASSWORD`.
2. Gurni projekat na GitHub.
3. **Render** → New → Web Service → repo → *Runtime: Docker* (koristi `Dockerfile` iz korena) → plan **Free**.
4. Environment promenljive:

| Ključ | Vrednost |
|---|---|
| `DB_URL` | Neon JDBC URL (iznad) |
| `DB_USERNAME` / `DB_PASSWORD` | iz Neon-a |
| `JWT_SECRET` | dugačak nasumičan string (32+ karaktera) |
| `ADMIN_EMAIL`, `ADMIN_PASSWORD` | tvoj admin nalog (promeni podrazumevani!) |
| `STORAGE_TYPE` | `database` |
| `JAVA_OPTS` | `-Xmx300m -XX:+UseSerialGC -Xss512k` |

`CORS_ORIGINS` ne treba — frontend i API su na istom domenu.

**Ograničenja:** servis „zaspi“ posle ~15 min bez saobraćaja (prvi zahtev ~30–60 s), 512 MB RAM-a.
Neon free ima ~0,5–1 GB prostora i „uspavljuje“ bazu kad miruje. Slike u bazi brzo troše prostor —
frontend ih već smanjuje na max 1600 px, ali za više korisnika koristi Cloudflare R2 / S3 (novi `ImageStorage` adapter).

## Opcija B — uvek uključeno: Oracle Cloud „Always Free“ VM (traži karticu za verifikaciju)

Jedina stvarno trajno besplatna VM. Na njoj pokreni `docker compose up -d --build` (fajl u korenu projekta),
stavi ispred Caddy/nginx za HTTPS i svoj domen. Disk je trajan, pa možeš `STORAGE_TYPE=local` + volumen.

## Lokalno

```bash
docker compose up --build   # http://localhost:8080
```

## Pre javnog puštanja
- Promeni `JWT_SECRET` i admin lozinku.
- Jednom pusti sa `DDL_AUTO=update` da se napravi šema; kasnije razmisli o Flyway-u.
- Rate-limit je u memoriji (jedna instanca) — dovoljan za početak.

## Demo podaci (25 korisnika sa objavama, komentarima, porukama)
Postavi `DEMO_SEED=true` pri pokretanju (lokalno ili na hostingu). Aplikacija jednom ubaci ~25 korisnika
(praćenja, objave sa slikama, lajkove, komentare i odgovore, sačuvane objave, poruke). Ponovno pokretanje ne pravi duplikate.
Prijava: `ana.petrovic@demo.connectly` (i ostali: `ime.prezime@demo.connectly`), lozinka `Demo12345!` (menja se sa `DEMO_PASSWORD`).
Kad završiš sa demom, ukloni `DEMO_SEED` (ili ga stavi na false); podaci ostaju u bazi.
