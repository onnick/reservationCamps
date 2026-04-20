# reservationCamps

Spring Boot REST API pro rezervace táborů.

## Rychlé vysvětlivky
- Spring Boot: framework pro tvorbu Java web aplikací (spuštění, konfigurace, DI, web server).
- REST API: HTTP rozhraní s endpointy a JSON request/response.
- JPA a Hibernate: ORM vrstva, která mapuje Java entity na relační tabulky a řeší práci s DB přes objekty.
- Flyway: verzování databázového schématu pomocí migrací (SQL skripty), aby prostředí měla stejné schéma.
- PostgreSQL: produkčně použitelná relační DB (default profil).
- H2 (file-based): lehká lokální DB pro vývoj; file-based znamená, že data přežijí restart aplikace.
- Profil (`spring.profiles.active=local`): přepínání konfigurace pro různá prostředí (local vs default).
- Actuator: provozní endpointy aplikace (např. `/actuator/health`).
- Prometheus metriky: endpoint `/actuator/prometheus` pro export metrik (monitoring).
- JaCoCo: měření test coverage (kolik řádků/větví kódu bylo spuštěno testy) + report v CI.
- Checkstyle: statická kontrola stylu a pravidel kódu (kvalita a konzistence).
- Testcontainers: integrační testy s reálnou DB v Docker kontejneru, aby testy byly realistické.
- Docker image: zabalená aplikace do kontejneru pro spouštění a nasazení.
- Docker Compose: lokální orchestr pro spuštění více služeb najednou (app + DB).
- Kubernetes: orchestr pro nasazení kontejnerů do clusteru (staging/prod).
- Kustomize: správa K8s manifestů přes base + overlay (např. staging a prod).
- kind: lokální Kubernetes cluster používaný v CI (běží přímo na GitHub runneru).
- Smoke test: rychlé ověření, že po deployi aplikace reálně odpovídá (např. health endpoint).
- Artefakt v CI: soubor uložený z pipeline (např. JaCoCo report, test reporty, logy, metriky).

## Doména
Entity:
- `AppUser` (role: `ADMIN` or `CUSTOMER`)
- `Camp`
- `CampSession` (kapacita, rozsah dat)
- `Reservation` (životní cyklus stavu)

Business pravidla (příklady):
- termín nelze vytvořit v minulosti
- rezervaci nelze vytvořit/potvrdit/zrušit po začátku termínu
- rezervace je unikátní pro dvojici (uživatel, termín)
- potvrzení rezervace respektuje kapacitu termínu
- pouze `CREATED` lze potvrdit; pouze `CONFIRMED` lze zaplatit; `PAID` nelze zrušit

## Spuštění lokálně
Požadavky: Java 21+, Docker (volitelně, ale doporučeno).

Spuštění přes Docker Compose (aplikace + PostgreSQL, doporučená varianta pro realističtější prostředí):
```bash
docker compose up --build
```

Spuštění přímo (je potřeba běžící PostgreSQL, připojení se nastavuje přes env proměnné):
```bash
export DB_URL=jdbc:postgresql://localhost:5432/reservationcamps
export DB_USERNAME=reservationcamps
export DB_PASSWORD=reservationcamps
mvn -B spring-boot:run
```

Spuštění bez PostgreSQL (file-based H2, profil `local`, port 8081, data se ukládají do `./data/`):
```bash
mvn -B clean spring-boot:run -Dspring-boot.run.profiles=local
```

Pokud chceš v profilu `local` mít bootstrap admin účet s heslem `admin`, spusť:
```bash
BOOTSTRAP_ADMIN_PASSWORD=admin mvn -B clean spring-boot:run -Dspring-boot.run.profiles=local
```

Důvod: hesla (secrety) nechceme mít napevno v repozitáři v plaintextu. Proto se heslo pro lokální bootstrap bere z environment proměnné (případně se jednorázově vygeneruje a uloží do `./data/bootstrap-admin.txt`, která je v `.gitignore`).

Health:
```bash
curl -fsS http://localhost:8080/actuator/health
```

Health v profilu `local`:
```bash
curl -fsS http://localhost:8081/actuator/health
```

## API
Admin endpointy vyžadují hlavičku `X-Actor-Role: ADMIN`.

Vysvětlivka: hlavička `X-Actor-Role` simuluje „kdo volá“ (role aktéra). Pro admin-only operace server kontroluje, že je role ADMIN (a u některých endpointů se ověřuje i `X-Actor-Id` proti DB).

Vytvoření uživatele:
```bash
curl -sS -X POST http://localhost:8080/api/users \
  -H 'Content-Type: application/json' \
  -d '{"email":"a@example.com"}'
```

Vytvoření tábora:
```bash
curl -sS -X POST http://localhost:8080/api/camps \
  -H 'Content-Type: application/json' \
  -H 'X-Actor-Role: ADMIN' \
  -d '{"name":"Camp","basePriceCents":1000}'
```

## Architektura
Aplikace má jednoduchou vrstvenou architekturu:
- API vrstva: REST controllery v `src/main/java/.../api` vystavují HTTP endpointy a mapují request/response.
  Vysvětlivka: controller je „vstupní brána“ přes HTTP, ale pravidla držíme v service vrstvě.
- Aplikační/doménová logika: služby v `src/main/java/.../service` implementují business pravidla (stavové přechody, kontrola kapacity, kontrola rolí, prevence duplicit).
  Vysvětlivka: service vrstva je místo, kde je doménová logika testovatelná a nezávislá na UI.
- Perzistence: JPA entity v `src/main/java/.../domain` a Spring Data repository v `src/main/java/.../domain/repo` ukládají data do relační databáze.
  Vysvětlivka: Hibernate/JPA mapuje entity a vztahy do tabulek; repository poskytují dotazy a CRUD.
- Migrace: Flyway migrace v `src/main/resources/db/migration` (a `db/migration/h2` pro profil `local`) drží databázové schéma verzované.
  Vysvětlivka: migrace jsou auditovatelný „zdroj pravdy“ pro DB schéma napříč prostředími.
- Error handling: `ApiExceptionHandler` převádí doménové výjimky na konzistentní HTTP odpovědi (`403/404/409` + `ApiError`); validační chyby těla requestu jsou `400`.
  Vysvětlivka: klient dostane stabilní HTTP kód a zprávu místo náhodných chyb.
- Resolving aktéra/role: `ActorResolver` čte request hlavičky (`X-Actor-Role`, volitelně `X-Actor-Id`) pro autorizaci admin-only operací.
  Vysvětlivka: je to školní zjednodušení autorizace bez JWT/session.
- UI: statické stránky v `src/main/resources/static/` jsou lehký klient nad API.
  Vysvětlivka: UI je „thin client“ a business pravidla se vynucují na serveru.

## Dokumentace
- SRS: [docs/SRS.md](docs/SRS.md)
- SDD: [docs/SDD.md](docs/SDD.md)
- Uživatelská příručka: [docs/USER_MANUAL.md](docs/USER_MANUAL.md)
- Administrátorská příručka: [docs/ADMIN_MANUAL.md](docs/ADMIN_MANUAL.md)

## Testy
Jednotkové testy: `*Test.java` (doménová pravidla a controllery).
Vysvětlivka: unit testy jsou rychlé a ověřují hlavně business pravidla a hraniční stavy.

Integrační testy: `*IT.java` s Testcontainers, když je dostupný Docker.
Vysvětlivka: integrační test spouští reálný stack controller -> service -> DB (PostgreSQL v kontejneru).

Spuštění:
```bash
mvn -B verify
```
Vysvětlivka: `verify` spustí unit testy, integrační testy (pokud je Docker), vygeneruje JaCoCo report a v CI vyhodnotí coverage pravidla.

## Testovací strategie
Cílem je mít většinu business pravidel pokrytou rychlými unit testy a k tomu jen pár realistických integračních testů:
- Unit testy (`*Test.java`): zaměřují se na business pravidla a hraniční stavy ve službách (např. neplatné přechody stavů, plná kapacita, duplicity) a na `wiring` controllerů (HTTP statusy, tvar JSONu, header-based actor resolution).
- Integrační testy (`*IT.java`): ověřují end-to-end chování přes vrstvy (controller -> service -> DB) s reálnou PostgreSQL databází přes Testcontainers.
- Mocky/test doubles: Mockito je použité pro repository a pro `NotificationPort` (externí side effect). `Clock.fixed(...)` je použité, aby byla pravidla závislá na čase deterministická.
- BDD: v projektu nepoužíváme; místo toho ověřujeme API flow integračními testy.
- Co záměrně netestujeme: čistý `plumbing`/boilerplate (detaily JPA mapování anotacemi, jednoduché DTO/recordy) a statické UI HTML (UI je tenký klient nad už otestovanými pravidly v API).

## CI/CD
GitHub Actions:
- build + unit/integration testy
- JaCoCo coverage check + report jako artefakt
- Checkstyle
- build + push Docker image do GHCR na `main`
- deploy do Kubernetes `staging` na `main` (kind na runneru) + smoke test
- observability (bonus): po staging deploy se stáhnou logy z Kubernetes a export metrik z `/actuator/prometheus` jako artefakt

Vysvětlivka: CI je automatická kontrola kvality (build, testy, coverage, statická analýza). CD je automatizovaný deploy do staging, aby bylo vidět, že aplikace je nasaditelná a provozně ověřitelná.

## Kubernetes
Manifesty používají Kustomize overlay:
- staging: `k8s/overlays/staging`
- prod: `k8s/overlays/prod`

Secrety nejsou committované. Vytvoř `secret.env` podle `secret.env.example` a aplikuj:
```bash
kubectl apply -k k8s/overlays/staging
kubectl -n staging port-forward svc/reservationcamps 8080:80
```
