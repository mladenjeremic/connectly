# Connectly

Full-stack društvena mreža (Instagram-stil): **Java 21 + Spring Boot** (Maven multi-module, clean/hexagonal arhitektura kao u MiniJira) i **React + TypeScript + Vite + Tailwind CSS**.

```
drustvenemreze
├── connectly   (backend, Maven multi-module)
│   ├── domain           modeli, enumi, izuzeci, portovi (interfejsi) - bez Spring-a
│   ├── application      use case interfejsi + servisi (poslovna logika, transakcije)
│   ├── infrastructure   JPA entiteti/adapteri, JWT, BCrypt, lokalno skladište slika
│   ├── presentation     REST kontroleri, validacija, @RestControllerAdvice, Spring Security
│   └── app              Spring Boot start, wiring servisa, listener notifikacija, seed admina, job za istek zabrana
└── frontend    (React + Vite + Tailwind)
```

Pravila zavisnosti proverava `ArchitectureTest` (ArchUnit): domain je čist, application ne zna za JPA, presentation ne zna za infrastructure.

## Pokretanje

### 1. Baza (MySQL)
```bash
cd connectly
docker compose up -d        # MySQL 8.4, baza/korisnik/lozinka: connectly
```
Ili koristi svoj MySQL i podesi `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` (vidi `app/src/main/resources/application.yml`).
Tabele se prave automatski (`ddl-auto: update`, samo za razvoj).

### 2. Backend
```bash
cd connectly
./mvnw spring-boot:run -pl app -am        # Windows: mvnw.cmd ...
# ili iz IntelliJ-a: pokreni ConnectlyApplication
```
API je na `http://localhost:8080`. Testovi: `./mvnw test`.

Pri prvom pokretanju se pravi **administratorski nalog** (jedini način da se dobije ADMIN rola):

| email | lozinka |
|---|---|
| `admin@connectly.local` | `Admin12345!` |

Promeni ih preko `ADMIN_EMAIL`, `ADMIN_USERNAME`, `ADMIN_PASSWORD` (prazan email isključuje seed). Obavezno promeni `JWT_SECRET` van razvoja.

### 3. Frontend
```bash
cd frontend
npm install
npm run dev          # http://localhost:5173  (proxy: /api -> localhost:8080)
```

## Šta je urađeno (po specifikaciji)

**Korisnik:** registracija (ime, prezime, username, email, lozinka - BCrypt), prijava (JWT), profil sa fotografijom/biografijom/brojem objava, pratilaca i praćenih, izmena profila i fotografije, promena lozinke, deaktivacija naloga, pretraga korisnika, predlozi za praćenje, liste pratilaca/praćenih.
**Objave:** tekst + do 10 fotografija (carousel), izmena i brisanje samo svojih objava, dodavanje/uklanjanje fotografija, feed „Praćeni“ i „Najnovije“ (sortirano po datumu, paginacija), lajk (najviše jedan po korisniku; dupli klik na sliku), čuvanje objava (stranica „Sačuvano“, vidi je samo vlasnik), kopiranje linka objave, prijava objave/komentara.
**Komentari:** lajkovanje komentara, odgovori na komentare (niti u jednom nivou; odgovor na odgovor ulazi u istu nit, `@korisnik` je link na profil), „Prikaži odgovore (n)“, brisanje svog komentara (autor objave i admin brišu tuđe; brisanjem komentara nestaje i njegova nit).
**Priče (stories):** fotografija koja nestaje posle 24 h, red priča na vrhu feed-a i viewer (pauza dok držiš priču, pregledane priče dobijaju siv prsten). **Odgovor na priču:** brze reakcije (❤️ 😂 😮 😢 👏 🔥) ili tekst, moguće samo za priče korisnika koje pratiš; odgovor stiže kao poruka u razgovoru sa autorom priče.
**Poruke (chat):** razgovor između dva korisnika (`/messages`, `/messages/:username`), lista razgovora sa poslednjom porukom i brojem nepročitanih, badge u meniju, „Viđeno“, novi razgovor preko pretrage, dugme „Poruka“ na profilu. Nema WebSocket-a: razgovor se osvežava na 4 s, a broj nepročitanih na 8 s.
**Notifikacije:** lajk, komentar, lajk komentara, odgovor na komentar, praćenje, promena statusa naloga; grupisano „Poslednja 24 sata / Ranije“; označavanje kao pročitano; badge sa brojem.
**Admin panel** (`/admin`): dashboard, pretraga korisnika po username/email-u, detalji sa istorijom zabrana i akcija, privremeni ban (1/3/7/30 dana + obavezan razlog), trajni ban, deaktivacija/aktivacija, skidanje zabrane, obrada prijava (odbaci / obrađeno / ukloni sadržaj), uklanjanje objava, audit log.
**Bezbednost:** rola se uvek čita iz baze (ne iz tokena/frontenda), `/api/admin/**` zaštićen na backend-u, ban važi odmah (JWT filter proverava nalog pri svakom zahtevu), istekla privremena zabrana se automatski skida (pri prijavi/zahtevu + periodični job), admin ne može da moderira sebe ni drugog admina, upload: samo JPEG/PNG/WebP proveren po sadržaju (magic bytes), max 5 MB i 10 slika, imena fajlova su UUID, vlasništvo nad resursima se proverava, `passwordHash` se nikad ne vraća kroz API.
**UI:** moderan dizajn sa gradijentnim brendom (boje se menjaju na jednom mestu: `@theme` u `frontend/src/index.css`), kartice objava, leva traka koja se na širokim ekranima širi sa nazivima, priče, desna kolona sa predlozima, mreža objava na profilu, tamna tema, responsive (staklena donja navigacija na telefonu), skeleton učitavanje, animacije (srce, modali), toast poruke, modali za potvrdu.

## REST API (skraćeno)

| | |
|---|---|
| Auth | `POST /api/auth/register`, `POST /api/auth/login` |
| Korisnici | `GET/PUT /api/users/me`, `POST /api/users/me/photo`, `PUT /api/users/me/password`, `POST /api/users/me/deactivate`, `GET /api/users/search?q=`, `GET /api/users/suggestions`, `GET /api/users/{username}`, `GET /api/users/{username}/posts`, `GET /api/users/{id}/followers|following`, `POST/DELETE /api/users/{id}/follow` |
| Objave | `POST /api/posts` (multipart: `content`, `images`; ili JSON), `GET /api/posts/feed`, `GET /api/posts/following`, `GET/PUT/DELETE /api/posts/{id}`, `POST /api/posts/{id}/images`, `DELETE /api/posts/{id}/images/{imageId}` |
| Interakcije | `POST/DELETE /api/posts/{id}/likes`, `GET/POST /api/posts/{id}/comments` (telo: `content`, opciono `parentId` za odgovor), `GET /api/comments/{id}/replies`, `POST/DELETE /api/comments/{id}/likes`, `DELETE /api/comments/{id}` |
| Poruke | `GET /api/messages/conversations`, `GET /api/messages/unread-count`, `GET/POST /api/messages/with/{userId}` (telo: `content`, do 2000 karaktera), `PATCH /api/messages/with/{userId}/read` |
| Sačuvano | `GET /api/posts/saved`, `POST/DELETE /api/posts/{id}/bookmark` |
| Priče | `GET/POST /api/stories`, `DELETE /api/stories/{id}`, `POST /api/stories/{id}/replies` (telo: `content`, do 500 karaktera; postaje poruka u chatu) |
| Slike | `GET /api/images/{fileName}` (javno, UUID imena) |
| Notifikacije | `GET /api/notifications`, `GET /api/notifications/unread-count`, `PATCH /api/notifications/{id}/read`, `PATCH /api/notifications/read-all` |
| Prijave | `POST /api/reports` |
| Admin | `GET /api/admin/dashboard`, `GET /api/admin/users?q=&status=`, `GET /api/admin/users/{id}`, `POST /api/admin/users/{id}/ban|unban|permanent-ban|deactivate|reactivate`, `GET /api/admin/posts`, `DELETE /api/admin/posts/{id}`, `GET /api/admin/reports?status=`, `PATCH /api/admin/reports/{id}`, `GET /api/admin/audit-log` |

Greške su uvek u formatu `{ status, error, message, timestamp }`.

## Nadogradnja postojeće baze

Hibernate `ddl-auto: update` dodaje nove tabele (`comment_likes`, `bookmarks`, `conversations`, `messages`) i kolonu `comments.parent_id`, ali **ne menja postojeće kolone**. Ako si bazu napravio pre ove verzije, kolona `notifications.type` je MySQL `ENUM` sa starim vrednostima, pa nova obaveštenja (`COMMENT_LIKE`, `COMMENT_REPLY`, `STORY_REPLY`) ne bi mogla da se upišu. Jednom pokreni:

```sql
ALTER TABLE notifications MODIFY COLUMN type VARCHAR(30) NOT NULL;
```

(Nove baze to dobijaju automatski.)

## Svesno izostavljeno / za sledeću fazu
- Refresh token (JWT traje 12 h, podesivo `JWT_TTL_MINUTES`).
- Polje `visibility` na objavi (sve objave su javne; feed „Praćeni“ prikazuje samo praćene).
- Objave suspendovanih/blokiranih korisnika ostaju vidljive dok ih admin ne ukloni.
- Poruke u realnom vremenu (WebSocket/SSE umesto osvežavanja na nekoliko sekundi), slike u porukama, blokiranje korisnika, hashtag-ovi, preporuke, real-time notifikacije (WebSocket/SSE), video priče - sekcija 13 specifikacije.
- Flyway migracije umesto `ddl-auto: update`, Docker image za aplikaciju, objekt storage umesto lokalnog foldera (`ImageStorage` port je spreman za zamenu).


## Nove funkcije (treća runda)
- Blokiranje korisnika (`/api/blocks`): blokirani ne mogu da se prate, šalju poruke ni odgovaraju na priče; lista u Podešavanjima.
- Beskonačno skrolovanje feed-a, pauziranje osvežavanja kad je tab skriven, kompresija slika pre otpremanja.
- Rate limiting (prijava 20/min, API 600/min po IP-u).
- Spremno za hosting: `Dockerfile`, `docker-compose.yml`, PostgreSQL drajver, slike u bazi (`STORAGE_TYPE=database`), CI. Vidi **HOSTING.md**.
- Ako baza već postoji: tabela `user_blocks` i `stored_images` nastaju automatski (`ddl-auto: update`).

## Demo podaci
`DEMO_SEED=true ./mvnw -pl app spring-boot:run` (ili env promenljiva u Docker-u) ubacuje ~25 demo korisnika. Detalji u HOSTING.md.
